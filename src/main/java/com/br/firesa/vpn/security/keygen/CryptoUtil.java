package com.br.firesa.vpn.security.keygen;

import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtil {

	private static final int GCM_TAG_LENGTH = 128; // em bits
	private static final int IV_LENGTH = 12; // 96 bits, recomendado para GCM

	public static byte[] encryptData(byte[] data, SecretKeySpec aesKey) throws Exception {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] hashOriginal = md.digest(data);
		System.out.println("Hash dos dados originais: " + Base64.getEncoder().encodeToString(hashOriginal));
		// Gera um IV seguro
		byte[] iv = new byte[IV_LENGTH];
		SecureRandom random = new SecureRandom();
		random.nextBytes(iv);

		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
		GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
		cipher.init(Cipher.ENCRYPT_MODE, aesKey, gcmSpec);

		byte[] encryptedData = cipher.doFinal(data);

		byte[] encryptedDataWithIv = new byte[IV_LENGTH + encryptedData.length];
		System.arraycopy(iv, 0, encryptedDataWithIv, 0, IV_LENGTH);
		System.arraycopy(encryptedData, 0, encryptedDataWithIv, IV_LENGTH, encryptedData.length);

		return encryptedDataWithIv;
	}

	public static byte[] decryptData(byte[] encryptedDataWithIv, SecretKeySpec aesKey) throws Exception {

		// Extrair o IV dos primeiros 12 bytes dos dados recebidos
		byte[] iv = new byte[IV_LENGTH];
		byte[] encryptedData = new byte[encryptedDataWithIv.length - IV_LENGTH];
		System.arraycopy(encryptedDataWithIv, 0, iv, 0, IV_LENGTH); // Corrigido: Copiar o IV primeiro
		System.arraycopy(encryptedDataWithIv, IV_LENGTH, encryptedData, 0, encryptedData.length);

		// Configurar o cipher para descriptografia
		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
		GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
		cipher.init(Cipher.DECRYPT_MODE, aesKey, gcmSpec);
		
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] decryptedData = cipher.doFinal(encryptedData);
		byte[] hashDescriptografado = md.digest(decryptedData);
		System.out.println("Hash dos dados descriptografados: " + Base64.getEncoder().encodeToString(hashDescriptografado));

		// Descriptografar os dados
		return cipher.doFinal(encryptedData);
	}

	public static SecretKeySpec generateSharedSecret(PublicKey otherPartyPublicKey, PrivateKey ownPrivateKey) throws Exception {
	    KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
	    keyAgreement.init(ownPrivateKey);
	    keyAgreement.doPhase(otherPartyPublicKey, true);
	    byte[] sharedSecret = keyAgreement.generateSecret();

	    // Opcionalmente, você pode derivar a chave AES usando SHA-256
	    MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
	    byte[] aesKeyBytes = sha256.digest(sharedSecret);

	    return new SecretKeySpec(aesKeyBytes, "AES");
	}

	public static PrivateKey convertBytesToPrivateKey(byte[] privateKeyBytes) throws Exception {
		KeyFactory keyFactory = KeyFactory.getInstance("EC");
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
		return keyFactory.generatePrivate(keySpec);
	}

	public static PublicKey convertBytesToPublicKey(byte[] publicKeyBytes) throws Exception {
		KeyFactory keyFactory = KeyFactory.getInstance("EC");
		return keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
	}

}