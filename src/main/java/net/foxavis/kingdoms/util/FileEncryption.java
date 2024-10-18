package net.foxavis.kingdoms.util;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * This utility class handles the encryption and decryption of strings on an algorithm.
 * <p>
 *     The encryption algorithm used is AES with CBC mode and PKCS5 padding.
 *     The key is derived from the password using PBKDF2 with HmacSHA256.
 * </p>
 * @see #encrypt(String, String, String)
 * @see #decrypt(String, String, String)
 *
 * @author Kyomi (with assistance from ChatGPT o1-preview (Sept. 17, 2024))
 */
public class FileEncryption {

	// -- Constants -- \\
	private static final int SALT_LENGTH = 16; // 128 bits
	private static final int IV_LENGTH = 16; // 128 bits
	private static final int KEY_SIZE = 256; // 256 bits
	private static final int ITERATION_COUNT = 65536; // 2^16

	/**
	 * Encrypts the input text using AES encryption with CBC mode and PKCS5 padding.
	 * The method generates a random salt and IV, which are stored with the encrypted result.
	 *
	 * @param algorithm The encryption algorithm and mode (e.g., "AES/CBC/PKCS5Padding").
	 * @param input     The plaintext string to encrypt.
	 * @param password  The password used for key derivation.
	 * @return The Base64-encoded string containing the salt, IV, and ciphertext.
	 * @throws NoSuchAlgorithmException             If the specified algorithm is not available.
	 * @throws InvalidKeySpecException              If the key specification is invalid.
	 * @throws NoSuchPaddingException               If the padding scheme is not available.
	 * @throws InvalidAlgorithmParameterException   If the algorithm parameters are invalid.
	 * @throws InvalidKeyException                  If the key is invalid.
	 * @throws IllegalBlockSizeException            If the block size is invalid.
	 * @throws BadPaddingException                  If the padding is invalid.
	 * @see #decrypt(String, String, String)
	 */
	public static String encrypt(String algorithm, String input, String password)
			throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
			InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException {
		byte[] salt = new byte[SALT_LENGTH];
		SecureRandom sRandom = new SecureRandom();
		sRandom.nextBytes(salt);

		// Deriving the key from the password and salt.
		SecretKey key = deriveKey(password, salt);

		// Generate a random IV
		byte[] iv = new byte[IV_LENGTH];
		sRandom.nextBytes(iv);
		IvParameterSpec ivSpec = new IvParameterSpec(iv);

		// Initialize the cipher for encryption
		Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);

		// Encrypt the input text
		byte[] cipherText = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));

		// Concatenate the salt, IV, and cipher text
		byte[] encrypted = new byte[salt.length + iv.length + cipherText.length];
		System.arraycopy(salt, 0, encrypted, 0, salt.length);
		System.arraycopy(iv, 0, encrypted, salt.length, iv.length);
		System.arraycopy(cipherText, 0, encrypted, salt.length + iv.length, cipherText.length);

		// Return the encrypted text as a base64-encoded string
		return Base64.getEncoder().encodeToString(encrypted);
	}

	/**
	 * Decrypts the input text using AES decryption with CBC mode and PKCS5 padding.
	 * The method extracts the salt and IV from the input data for key derivation and decryption.
	 *
	 * @param algorithm The decryption algorithm and mode (e.g., "AES/CBC/PKCS5Padding").
	 * @param input     The Base64-encoded string containing the salt, IV, and ciphertext.
	 * @param password  The password used for key derivation.
	 * @return The decrypted plaintext string.
	 * @throws NoSuchAlgorithmException             If the specified algorithm is not available.
	 * @throws InvalidKeySpecException              If the key specification is invalid.
	 * @throws NoSuchPaddingException               If the padding scheme is not available.
	 * @throws InvalidAlgorithmParameterException   If the algorithm parameters are invalid.
	 * @throws InvalidKeyException                  If the key is invalid.
	 * @throws IllegalBlockSizeException            If the block size is invalid.
	 * @throws BadPaddingException                  If the padding is invalid.
	 * @see #encrypt(String, String, String)
	 */
	public static String decrypt(String algorithm, String input, String password)
			throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
			InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException {
		// Decode the base64-encoded input
		byte[] encrypted = Base64.getDecoder().decode(input);

		// Extract the salt, IV, and cipher text
		byte[] salt = new byte[SALT_LENGTH];
		byte[] iv = new byte[IV_LENGTH];
		byte[] cipherText = new byte[encrypted.length - salt.length - iv.length];

		System.arraycopy(encrypted, 0, salt, 0, salt.length);
		System.arraycopy(encrypted, salt.length, iv, 0, iv.length);
		System.arraycopy(encrypted, salt.length + iv.length, cipherText, 0, cipherText.length);

		// Derive the key from the password and salt
		SecretKey key = deriveKey(password, salt);

		// Initialize the cipher for decryption
		Cipher cipher = Cipher.getInstance(algorithm);
		IvParameterSpec ivSpec = new IvParameterSpec(iv);
		cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);

		// Decrypt the cipher text
		byte[] plainText = cipher.doFinal(cipherText);

		// Convert the decrypted text to a string
		return new String(plainText, StandardCharsets.UTF_8);
	}

	/**
	 * Derives a SecretKey using PBKDF2 with HmacSHA256 from the given password and salt.
	 *
	 * @param password The password used for key derivation.
	 * @param salt     The salt used in the key derivation function.
	 * @return The derived SecretKey.
	 * @throws NoSuchAlgorithmException If the algorithm is not available.
	 * @throws InvalidKeySpecException  If the key specification is invalid.
	 */
	private static SecretKey deriveKey(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_SIZE);

		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
	}
}