package codeu.chat.common;

import codeu.chat.util.Logger;
import codeu.chat.common.Encryption;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;


public class RsaAesSha implements Encryption{

  private final static Logger.Log LOG = Logger.newLog(RsaAesSha.class);

  private Key publicKey;
  private Key privateKey;
  private Cipher rsa;
  private Cipher aes;
  private SecretKeySpec symmetricKey;
  private MessageDigest md;

  public RsaAesSha() {
    try {
      KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
      kpg.initialize(2048);
      KeyPair kp = kpg.genKeyPair();
      publicKey = kp.getPublic();
      privateKey = kp.getPrivate();
    } catch(Exception ex) {
      System.out.println("ERROR: Exception creating key pair. Check log for details.");
      LOG.error(ex, "Exception creating key pair.");
    }

    try {
      rsa = Cipher.getInstance("RSA");
      aes = Cipher.getInstance("AES");
      md = MessageDigest.getInstance("SHA-256");
    } catch(Exception ex) {
      System.out.println("ERROR: Exception creating Ciphers. Check log for details.");
      LOG.error(ex, "Exception creating Ciphers.");
    }
  }

  public byte[] publicKeyEncrypt(byte[] message, Key pubKey) throws InvalidKeyException,
      IllegalBlockSizeException, BadPaddingException {
    try {
      rsa.init(Cipher.ENCRYPT_MODE, pubKey);
    } catch(InvalidKeyException ex) {
      System.out.println("ERROR: Given invalid public key. Check log for details.");
      LOG.error(ex, "Exception using public key.");
      throw ex;
    }

    byte[] cipherData;
    try {
      cipherData = rsa.doFinal(message);
    } catch(Exception ex){
      System.out.println("ERROR: Exception during public key encryption. Check log for details.");
      LOG.error(ex, "Exception during public key encryption.");
      throw ex;
    }
    return cipherData;
  }

  public byte[] publicKeyDecrypt(byte[] message) throws InvalidKeyException,
      IllegalBlockSizeException, BadPaddingException {
    try {
      rsa.init(Cipher.DECRYPT_MODE, privateKey);
    } catch(InvalidKeyException ex) {
      System.out.println("ERROR: Given invalid private key. Check log for details.");
      LOG.error(ex, "Exception using private key.");
      throw ex;
    }

    byte[] cipherData;
    try {
      cipherData = rsa.doFinal(message);
    } catch(Exception ex){
      System.out.println("ERROR: Exception during public key decryption. Check log for details.");
      LOG.error(ex, "Exception during public key decryption.");
      throw ex;
    }
    return cipherData;
  }

  public Key getPublicKey(){
    return publicKey;
  }

  public void setSymmetricKey(byte[] key){
    symmetricKey = new SecretKeySpec(key, "AES");
  }

  public byte[] symmetricKeyEncrypt(byte[] message) throws InvalidKeyException,
      IllegalBlockSizeException, BadPaddingException {
    try {
      aes.init(Cipher.ENCRYPT_MODE, symmetricKey);
    } catch(InvalidKeyException ex) {
      System.out.println("ERROR: Given invalid symmetric key. Check log for details.");
      LOG.error(ex, "Exception using symmetric key.");
      throw ex;
    }

    byte[] cipherData;
    try {
      cipherData = aes.doFinal(message);
    } catch(Exception ex){
      System.out.println("ERROR: Exception during symmetric key encryption. Check log for details.");
      LOG.error(ex, "Exception during symmetric key encryption.");
      throw ex;
    }
    return cipherData;
  }

  public byte[] symmetricKeyDecrypt(byte[] message) throws InvalidKeyException,
      IllegalBlockSizeException, BadPaddingException {
    try {
      aes.init(Cipher.DECRYPT_MODE, symmetricKey);
    } catch(InvalidKeyException ex) {
      System.out.println("ERROR: Given invalid symmetric key. Check log for details.");
      LOG.error(ex, "Exception using symmetric key.");
      throw ex;
    }

    byte[] cipherData;
    try {
      cipherData = aes.doFinal(message);
    } catch(Exception ex){
      System.out.println("ERROR: Exception during symmetric key decryption. Check log for details.");
      LOG.error(ex, "Exception during symmetric key decryption.");
      throw ex;
    }
    return cipherData;
  }


  public byte[] hash(byte[] data) {
    md.update(data);
    byte[] value = md.digest();
    return value;
  }
}
