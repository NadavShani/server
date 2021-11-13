
// A Java program for a Server
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Array;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.TimeUnit;

public class Server
{

    //initialize socket and input stream
    private Socket          socket   = null;
    private ServerSocket    server   = null;
    private DataInputStream in       =  null;
    private DataOutputStream out     = null;
    private bobDH bob = null;
    private AES aes;



    public Server(int port)
    {
        try {
            server = new ServerSocket(port);
            System.out.println("Server started");
            System.out.println("Waiting for a client ...");

            socket = server.accept();

            System.out.println("Client accepted");

            /* takes input from the client socket */
            in = new DataInputStream(
                    new BufferedInputStream(socket.getInputStream()));

            out = new DataOutputStream(socket.getOutputStream());
            byte[] payload = new byte[4096];
            byte [] encrypted = new byte[4096];
            byte [] iv = new byte[4096];
            /* Server is waiting for start signal from client */
            while(!in.readUTF().equals("dh-start"));

            try {
                bobDH bob = new bobDH();

                /* bob is ready */
                out.writeUTF("bob-ok");
                in.read(payload);

                /* next msg from alice should be her public key */
                bob.generatePublicKeyFrom(payload); /* generating bob public key */

                /* bob is sending his public key to alice */
                out.writeUTF("bob-public-ok");
                out.write(bob.getPublicKey());
                bob.phase();
                bob.generateSecret();

                /* alice is sending her iv */
                in.read(iv);
                out.writeUTF("iv-ok"); /* in is ok , continute */

                aes = new AES(bob.getSecret(),Decode(iv));

                /* reading encrypted message */
               // in.read(encrypted);


                /* trying to descrypt alice message */
              //  System.out.println("decrypted is: " + new String(aes.decrypt(Decode(encrypted))));

              //  out.write(aes.encrypt("hi there you??".getBytes()));
            }
            catch (InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } //catch (IllegalBlockSizeException e) {
               // e.printStackTrace();
          //  } catch (BadPaddingException e) {
             //   e.printStackTrace();
          //  }


            /* close connection */
            socket.close();
            in.close();
            System.out.println("secret generated");
            System.out.println("closing Socket");
        }
        catch(IOException i)
        {
            System.out.println(i);
        }


    }

    public byte[] Decode(byte[] packet)
    {
        var i = 0;
        while(packet[i] != 0)
            i++;
        byte [] result = new byte[i];

        for(int index=0;index<i;index++)
            result[index]=packet[index];

        return result;
    }


    public static void main(String args[])
    {
        Server server = new Server(5300);
    }


    /************** GETTERS **************/
    public Socket getSocket() {
        return socket;
    }

    public AES getAes() {
        return aes;
    }

    /************** SETTERS **************/
    public void setSocket(Socket socket) {
        this.socket = socket;
    }
    public void setAes(AES aes) {
        this.aes = aes;
    }


}