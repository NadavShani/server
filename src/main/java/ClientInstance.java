public class ClientInstance {

    private String clientSrcAddress;
    private AES aes;
    private boolean EncryptingBack;

    public ClientInstance(){

    }

    public ClientInstance(String clientSrcAddress, AES aes) {
        this.clientSrcAddress = clientSrcAddress;
        this.aes = aes;
        this.EncryptingBack=false;
    }

    /************** GETTERS **************/
    public String getClientSrcAddress() {
        return clientSrcAddress;
    }
    public AES getAes() {
        return aes;
    }

    public boolean isEncryptingBack() {
        return EncryptingBack;
    }
    /************** SETTERS **************/
    public void setClientSrcAddress(String clientSrcAddress) {
        this.clientSrcAddress = clientSrcAddress;
    }
    public void setAes(AES aes) {
        this.aes = aes;
    }
    public void setEncryptingBack(boolean encryptingback){ this.EncryptingBack = encryptingback;}

}
