import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class AES {

    private SecretKeySpec secret;
    private Cipher cipher;
    private byte [] encodedParams;
    private boolean isEncryptMode;
    protected AlgorithmParameters aesParams;

    public AES(byte [] sharedsecret) throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, InvalidAlgorithmParameterException, InvalidKeyException {
        this.secret = new SecretKeySpec(sharedsecret,0,16,"AES");
        this.cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        this.cipher.init(Cipher.ENCRYPT_MODE, this.secret);
        this.encodedParams = this.cipher.getParameters().getEncoded();
        this.aesParams = AlgorithmParameters.getInstance("AES");
        this.aesParams.init(encodedParams);
        isEncryptMode = false;


    }
    public AES(byte [] sharedsecret , byte [] encodedParams) throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, InvalidAlgorithmParameterException, InvalidKeyException {
        this.secret = new SecretKeySpec(sharedsecret,0,16,"AES");
        this.cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        this.encodedParams = encodedParams;
        this.aesParams = AlgorithmParameters.getInstance("AES");
        this.aesParams.init(this.encodedParams);
        this.cipher.init(Cipher.DECRYPT_MODE, this.secret,this.aesParams);
        isEncryptMode = false;

    }

    public byte [] getEncodedParams (){
        return this.encodedParams;
    }

    public SecretKeySpec getAesSecret(){
        return this.secret;

    }

    public byte [] encrypt(byte [] clearText) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        if(isEncryptMode == false) {
            this.cipher.init(Cipher.ENCRYPT_MODE, this.secret);
            isEncryptMode = true;
        }
        return this.cipher.doFinal(clearText);
    }
    public byte [] decrypt(byte [] encryptedMessage) throws IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        if(isEncryptMode == true) {
            this.cipher.init(Cipher.DECRYPT_MODE, this.secret, this.aesParams);
            isEncryptMode = false;
        }
        return this.cipher.doFinal(encryptedMessage);

    }


}
