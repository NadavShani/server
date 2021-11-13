import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.*;
import java.security.interfaces.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.crypto.interfaces.*;
import com.sun.crypto.provider.SunJCE;

/* bob is server */
public class bobDH {


    private KeyFactory bobKeyFac;
    private KeyPairGenerator bobKpairGen;
    private KeyPair bobKpair;
    private KeyAgreement bobKeyAgree;
    private byte[] bobPubKeyEnc; /* bob public */
    private PublicKey alicePubKey;
    private byte[] bobSharedSecret; /* bob secret */

    public bobDH(){
        bobKeyFac = null;
        bobKpairGen = null;
        bobKpair = null;
        bobKeyAgree = null;
        bobPubKeyEnc = null;
        alicePubKey = null;
        bobSharedSecret = new byte[1000];
    }

    protected  byte [] getSecret(){
        return this.bobSharedSecret;
    }

    public byte[]  getPublicKey(){
        return bobPubKeyEnc;
    }

    public void generateSecret(){
        bobSharedSecret = bobKeyAgree.generateSecret();
        System.out.println("Bob secret: " +
                toHexString(bobSharedSecret));
    }


    public void phase() throws InvalidKeyException {
        System.out.println("BOB: Execute PHASE1 ...");
        bobKeyAgree.doPhase(this.alicePubKey, true);
    }


    /* Bob has received Alice's public key in encoded format. He instantiates a DH public key from the encoded key material.*/
    public void generatePublicKeyFrom(byte[] alicePubKeyEnc) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, InvalidKeyException {
        System.out.println("BOB: received public key from alice ...");
        /*
         * Let's turn over to Bob. Bob has received Alice's public key
         * in encoded format.
         * He instantiates a DH public key from the encoded key material.
         */

        bobKeyFac = KeyFactory.getInstance("DH");
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(alicePubKeyEnc);
        PublicKey alicePubKey = bobKeyFac.generatePublic(x509KeySpec);
        this.alicePubKey = alicePubKey;
        System.out.println("BOB: Generate DH keypair ...");

        /*
         * Bob gets the DH parameters associated with Alice's public key.
         * He must use the same parameters when he generates his own key
         * pair.
         */
        DHParameterSpec dhParamFromAlicePubKey = ((DHPublicKey)alicePubKey).getParams();
        bobKpairGen = KeyPairGenerator.getInstance("DH");
        bobKpairGen.initialize(dhParamFromAlicePubKey);
        bobKpair = bobKpairGen.generateKeyPair();

        // Bob creates and initializes his DH KeyAgreement object
        System.out.println("BOB: Initialization ...");
        bobKeyAgree = KeyAgreement.getInstance("DH");
        bobKeyAgree.init(bobKpair.getPrivate());

        // Bob encodes his public key, and sends it over to Alice.
        bobPubKeyEnc = bobKpair.getPublic().getEncoded();
    }

    private static void byte2hex(byte b, StringBuffer buf) {
        char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        int high = ((b & 0xf0) >> 4);
        int low = (b & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
    }

    /*
     * Converts a byte array to hex string
     */
    private static String toHexString(byte[] block) {
        StringBuffer buf = new StringBuffer();
        int len = block.length;
        for (int i = 0; i < len; i++) {
            byte2hex(block[i], buf);
            if (i < len-1) {
                buf.append(":");
            }
        }
        return buf.toString();
    }
}
