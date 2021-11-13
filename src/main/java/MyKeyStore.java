
import java.io.*;
import java.security.KeyStore;
import java.security.KeyStoreException;
import sun.security.x509.*;
import java.security.cert.*;
import java.security.*;
import java.math.BigInteger;
import java.security.cert.Certificate;
import java.util.Date;


public class MyKeyStore {

    private String storeName;
    private String password;
    private KeyStore keyStore;

    public MyKeyStore(String storeName,String password) throws KeyStoreException {
        this.storeName = storeName;
        this.password = password;
        this.keyStore = KeyStore.getInstance("PKCS12");
    }
    public KeyStore getKeyStore(){
        return this.keyStore;
    }
    public String getPassword(){
        return this.password;
    }
    public String getStoreName(){
        return this.storeName;
    }


    public void createSelfSignedCertificate() throws KeyStoreException, NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA"); //ALGORITHM IS RSA
        keyPairGenerator.initialize(2048); //initialize KEY SIZE IS 2048
        KeyPair keyPair = keyPairGenerator.generateKeyPair(); //GENERATE PUBLIC AND PRIVATE KEY
        try {
            FileOutputStream fos = new FileOutputStream(this.storeName);
            keyStore.load(null, null); //LOAD STORE
            Certificate[] chain = {this.generateCertificate("cn=tcpencrypt", keyPair, 365, "SHA256withRSA","51.116.128.233")}; //CHAIN CERTIFICATE
            //X509Certificate cert = this.generateCertificate("cn=TcpEncrypt", keyPair, 365, "SHA256withRSA",51.116.128.233""); //CHAIN CERTIFICATE
            keyStore.setKeyEntry("tcpencrypt", keyPair.getPrivate(), this.password.toCharArray(), chain); //SAVE ENTRY IN STORE
           // keyStore.setCertificateEntry("tcpencrypt", chain[0]);
            System.out.println(keyPair.getPrivate());
            keyStore.store(fos, this.password.toCharArray()); //SAVE CHAIN IN STORE
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    public Certificate getCertificate(String alias) throws KeyStoreException, FileNotFoundException {
        try{
            InputStream keyStoreData = new FileInputStream(this.storeName);
            this.keyStore.load(keyStoreData, this.password.toCharArray());
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return this.keyStore.getCertificate(alias);
    }


    /*
    public void loadStore() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        KeyStore keyStore = KeyStore.getInstance("PKCS12"); //GET STORE
        try(InputStream keyStoreData = new FileInputStream(this.storeName)){
            keyStore.load(keyStoreData, this.password.toCharArray());
        }
    }
    */

    public X509Certificate generateCertificate(String dn, KeyPair pair, int days, String algorithm,String ip)
            throws GeneralSecurityException, IOException {

        PrivateKey privkey = pair.getPrivate();
        X509CertInfo info = new X509CertInfo();
        Date from = new Date();
        Date to = new Date(from.getTime() + days * 86400000l);
        CertificateValidity interval = new CertificateValidity(from, to);
        BigInteger sn = new BigInteger(64, new SecureRandom());
        X500Name owner = new X500Name(dn);


        info.set(X509CertInfo.VALIDITY, interval);
        info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(sn));
        info.set(X509CertInfo.SUBJECT, owner);
        info.set(X509CertInfo.ISSUER, owner);
        info.set(X509CertInfo.KEY, new CertificateX509Key(pair.getPublic()));
        info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
        AlgorithmId algo = new AlgorithmId(AlgorithmId.SHA256_oid);
        info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algo));

        final CertificateExtensions extensions = new CertificateExtensions();
        final GeneralNames generalNames = new GeneralNames();
        generalNames.add(new GeneralName(new IPAddressName(ip)));
        generalNames.add(new GeneralName(new DNSName(ip)));
        final SubjectAlternativeNameExtension san = new SubjectAlternativeNameExtension(false, generalNames);
        extensions.set(SubjectAlternativeNameExtension.NAME, san);
        info.set(X509CertInfo.EXTENSIONS, extensions);

        // Sign the cert to identify the algorithm that's used.
        X509CertImpl cert = new X509CertImpl(info);
        cert.sign(privkey, algorithm);

        // Update the algorith, and resign.
        algo = (AlgorithmId) cert.get(X509CertImpl.SIG_ALG);
        info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, algo);
        cert = new X509CertImpl(info);
        cert.sign(privkey, algorithm);
        return cert;
    }



    public static void main(String[] args) throws KeyStoreException, NoSuchAlgorithmException {
        MyKeyStore myKeyStore = new MyKeyStore("test.jks","123456");
        File f = new File(myKeyStore.storeName);
        if(!f.exists()) {
            try {
                myKeyStore.createSelfSignedCertificate();
                System.out.println(myKeyStore.getCertificate("tcpencrypt"));
            }
            catch (Exception e) {
                e.printStackTrace();
            }

        }


    }


}
