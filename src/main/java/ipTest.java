import java.net.InetAddress;
import java.net.UnknownHostException;

public class ipTest {

    public static void main(String [] args) throws UnknownHostException {
        System.out.println(InetAddress.getLocalHost().getHostAddress());
    }

}
