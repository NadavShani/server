import javax.swing.*;

public class diffieThread extends Thread {

    private int port;
    private production prodInstance;

    public diffieThread(int port,production prod){
        super();
        this.port = port;
        this.prodInstance = prod;
        start();
    }

    public void run(){
       Server server = new Server(this.port);
       this.prodInstance.addClientInstance(server.getSocket().getInetAddress().getHostAddress(),server.getAes());
    }
}
