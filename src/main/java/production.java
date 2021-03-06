import com.github.ffalcinelli.jdivert.*;
import com.github.ffalcinelli.jdivert.exceptions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

public class production {

    private static int diffiePort = 5300;
    private static int managementPort = 5301;
    private static Server server;
    private ArrayList<ClientInstance> clientInstances;

    public production(){
        this.clientInstances  = new ArrayList<ClientInstance>();
    }

    public static void main(String[] args) throws WinDivertException, FileNotFoundException {
        production prod = new production();

        /* read unsecured protocols file , server version*/
        File file = new File("unsecured.config");
        Scanner myReader = new Scanner(file);
        ArrayList<Integer> unsecuredProtocols = new ArrayList<Integer>();
        while (myReader.hasNextLine())
            unsecuredProtocols.add(Integer.parseInt(myReader.nextLine()));

        String filter = new String();
        for(int i=0;i<unsecuredProtocols.size();i++) {
            if(i < unsecuredProtocols.size() -1)
                filter += "tcp.DstPort = " + unsecuredProtocols.get(i) + " or ";
            else
                filter += "tcp.DstPort = " + unsecuredProtocols.get(i) + " or tcp.DstPort = 5301";
        }

        /* Open Windivert Handle */
        WinDivert w = new WinDivert(filter);
        w.open(); // packets will be captured from now on
        System.out.println("Listening...");
        /* Main Loop */
        while (true) {

            try {
                Packet packet = w.recv();  // read a single packet
                String clientAddr = packet.getSrcAddr();
                /* Do Client Wants To Establish Diffie? */
                if (packet.getTcp().getDstPort() == managementPort) {

                    String managementMsg = new String(packet.getPayload());
                    if(managementMsg.indexOf("start-diffie") > -1) {
                        boolean isClientExists = prod.isClientExists(clientAddr);
                        if(isClientExists){
                            /* if client is trying to re-exchange diffie - delete current instance of client */
                            System.out.println("Deleting Client Instance: " + clientAddr);
                            prod.deleteClientInstance(clientAddr);

                        }
                        diffieThread df = new diffieThread(diffiePort,prod);
                    }

                }
                else {
                    /* Is Client Exists */
                    boolean isClientExists = prod.isClientExists(clientAddr);
                    if(isClientExists) {
                        class RunMe implements Runnable {
                            private production prod;
                            private String hostAddress;
                            public RunMe(production prodInstance,String hostAddress){
                                this.prod=prodInstance;
                                this.hostAddress=hostAddress;
                            }

                            @Override
                            public void run() {
                                ClientInstance ci = prod.getClientInstancce(clientAddr);
                                try {
                                    byte [] payload = ci.getAes().decrypt(packet.getPayload());
                                    Packet p = this.prod.generateNewPacketWithPaylod(packet, payload); //create new packet with encrypted payload
                                    p.recalculateChecksum(); //checksum
                                    w.send(p, true); //send to server
                                } catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }

                        new RunMe(prod,clientAddr).run();
                    } //else client should intialize diffie

                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /* Add Client Instance To Production Client Instances, this function is called by diffie thread */
    public void addClientInstance(String clientAddress,AES aes){
        ClientInstance ci = new ClientInstance(clientAddress,aes);
        clientInstances.add(ci);
    }

    public void deleteClientInstance(String clientAddress) {
        ClientInstance found = null;
        for (ClientInstance ci : clientInstances)
                if (ci.getClientSrcAddress().equals(clientAddress))
                    found = ci;

        clientInstances.remove(found);

    }

    /* get client from clientInstance list , return null otherwise */
    public ClientInstance getClientInstancce(String ip){
        ClientInstance result = null;
        for(ClientInstance ci : clientInstances)
            if (ci.getClientSrcAddress().equals(ip))
                result = ci;

        return result;
    }

    public boolean isClientExists(String ip){
        boolean result=false;
        for(ClientInstance ci : clientInstances)
            if (ci.getClientSrcAddress().equals(ip))
                result = true;
        return result;
    }

    public int getManagementPort(){
        return managementPort;
    }

    public Packet generateNewPacketWithPaylod(Packet oldPacket,byte [] newPayLoad) throws WinDivertException { /*old packet and new payload*/
        /* clone header */
        byte[] header = Util.getBytesAtOffset(ByteBuffer.wrap(oldPacket.getRaw()), 0, oldPacket.getRaw().length - oldPacket.getPayload().length); //clone header

        /* create new  raw */
        byte[] myraw = new byte[oldPacket.getHeadersLength() + newPayLoad.length];

        /* write header to new raw */
        Util.setBytesAtOffset(ByteBuffer.wrap(myraw), 0, header.length, header);

        /* write new payload to new raw */
        Util.setBytesAtOffset(ByteBuffer.wrap(myraw), header.length, newPayLoad.length, newPayLoad);

        /* create packet */
        Packet p = new Packet(myraw, oldPacket.getWinDivertAddress());
        p.getIpv4().setTotalLength(myraw.length);
        return p;

    }

}

