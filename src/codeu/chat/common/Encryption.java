package codeu.chat.common;

/**
 * Interface for encrypting and decrypting messages and for hashing strings.
 */
public interface Encryption {

  /**
   * Uses RSA algorithm to encrypt message. Should be implemented using
   * existing code base that can handle large messages and keys.
   * @param message Data to be encrypted.
   * @param key Public Key of recipient.
   * @return Message to send to recipient.
   */
  int publicKeyEncrypt(int message, int key);

  /**
   * Uses RSA algorithm to decrypt incoming message.
   * @param message Data to be decrypted.
   * @return Message data.
   */
  int publicKeyDecrypt(int message);

  /**
   * Sets the internal symmetric key for symmetric key encryption.
   * @param key Value to be remembered.
   */
  void setSymmetricKey(int key);

  /**
   * Encrypts message using stored key.
   * @param message Message to be encrypted.
   * @return Encrypted message.
   */
  int symmetricKeyEncrypt(int message);

  /**
   * Decrypts message using stored key.
   * @param message Message to be decrypted.
   * @return Decrypted message.
   */
  int symmetricKeyDecrypt(int message);

  /**
   * Hash function for hashing passwords and such.
   * @param input Input to the hash.
   * @return Output of the function.
   */
  int hash(int input);
}
