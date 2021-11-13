import com.github.ffalcinelli.jdivert.*;
import com.github.ffalcinelli.jdivert.exceptions.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class main {

    public static void main(String[] args) throws WinDivertException {
        //  WinDivert w = new WinDivert("(tcp.DstPort == 80 and outbound) or (inbound)
        WinDivert w = new WinDivert("inbound and (tcp.DstPort = 21 or tcp.DstPort = 20)");

        w.open(); // packets will be captured from now on
        while (true) {

            try {
                Packet packet = w.recv();  // read a single packet
                System.out.println(packet);
                String msg = new String(packet.getPayload());
                System.out.println(msg);
                w.send(packet,true);
            } catch (Exception e) {
                System.out.println("********** EXCEPTION ********** " + e.toString());
            }

        }
    }
}
