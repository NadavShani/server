public class ClientInstance {

    private String clientSrcAddress;
    private AES aes;

    public ClientInstance(){

    }

    public ClientInstance(String clientSrcAddress, AES aes) {
        this.clientSrcAddress = clientSrcAddress;
        this.aes = aes;
    }

    public String getClientSrcAddress() {
        return clientSrcAddress;
    }

    public AES getAes() {
        return aes;
    }

    public void setClientSrcAddress(String clientSrcAddress) {
        this.clientSrcAddress = clientSrcAddress;
    }

    public void setAes(AES aes) {
        this.aes = aes;
    }
}
