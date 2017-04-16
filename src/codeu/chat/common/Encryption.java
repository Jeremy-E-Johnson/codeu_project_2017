package codeu.chat.common;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
import java.security.Key;

/**
 * Interface for encrypting and decrypting messages and for hashing strings.
 */
public interface Encryption {

  /**
   * Method for setting up RSA encryption.
   * @return Public key to send to other party.
   */
  Key getPublicKey();

  /**
   * Uses RSA algorithm to encrypt message. Should be implemented using
   * existing code base that can handle large messages and keys.
   * @param message Data to be encrypted.
   * @param key Public Key of recipient.
   * @return Message to send to recipient.
   */
  byte[] publicKeyEncrypt(byte[] message, Key key) throws InvalidKeyException,
      IllegalBlockSizeException, BadPaddingException;

  /**
   * Uses RSA algorithm to decrypt incoming message.
   * @param message Data to be decrypted.
   * @return Message data.
   */
  byte[] publicKeyDecrypt(byte[] message) throws InvalidKeyException,
      IllegalBlockSizeException, BadPaddingException;

  /**
   * Sets the internal symmetric key for symmetric key encryption.
   * @param key Value to be remembered.
   */
  void setSymmetricKey(byte[] key);

  /**
   * Encrypts message using stored key.
   * @param message Message to be encrypted.
   * @return Encrypted message.
   */
  byte[] symmetricKeyEncrypt(byte[] message) throws InvalidKeyException,
      IllegalBlockSizeException, BadPaddingException;

  /**
   * Decrypts message using stored key.
   * @param message Message to be decrypted.
   * @return Decrypted message.
   */
  byte[] symmetricKeyDecrypt(byte[] message) throws InvalidKeyException,
      IllegalBlockSizeException, BadPaddingException;

  /**
   * Hash function for hashing passwords and such.
   * @param input Input to the hash.
   * @return Output of the function.
   */
  byte[] hash(byte[] input);
}
