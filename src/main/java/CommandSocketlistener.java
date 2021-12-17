import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class CommandSocketlistener extends Thread {


    private ServerSocket server;
    private Socket socket   = null;
    private int port;
    private DataInputStream in       =  null;
    private DataOutputStream out     = null;
    private production prodInstance = null;

    public CommandSocketlistener(production prod,int port){
        this.prodInstance = prod;
        this.port = port;
        start();
    }

    @Override
    public void run() {
        try {
            while(!(Thread.currentThread().isInterrupted())){
                server = new ServerSocket(port);
                socket = server.accept();
                in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                out = new DataOutputStream(socket.getOutputStream());
                String cmd = in.readUTF();
                if (cmd.contains("server encryption")) {
                    String clientIp = socket.getInetAddress().getHostAddress();
                    ClientInstance ci = this.prodInstance.getClientInstancce(clientIp);
                    if(cmd.contains("disable")) {
                        ci.setEncryptingBack(false);
                    }
                    else if(cmd.contains("enable")){
                        ci.setEncryptingBack(true);
                    }
                    out.writeUTF("command-ok");
                }
                server.close();
                in.close();
                out.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
