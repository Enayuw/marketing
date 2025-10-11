package com.br.marketing.client.smy.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

public class AESUtil{

	public static final Charset CHARSET = Charset.forName("utf-8");
	public static final byte keyStrSzie = 16;
	public static final String ALGORITHM = "AES";
	public static final String AES_CBC_NOPADDING = "AES/CBC/NoPadding";

	private static String base64Encode(byte[] bytes) {
		return Base64.encodeBase64String(bytes);
	}

	private static byte[] base64Decode(String base64Code) throws Exception {
		return StringUtils.isBlank(base64Code) ? null : Base64.decodeBase64(base64Code);
	}

	private static byte[] aesEncryptToBytes(String content, String encryptKey)
			throws Exception {
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(
				base64Decode(encryptKey), "AES"));
		return cipher.doFinal(content.getBytes("utf-8"));
	}

	public static String aesEncrypt(String content, String encryptKey)
			throws Exception {
		return base64Encode(aesEncryptToBytes(content, encryptKey));
	}

	private static String aesDecryptByBytes(byte[] encryptBytes,
			String decryptKey) throws Exception {
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(
				base64Decode(decryptKey), "AES"));
		byte[] decryptBytes = cipher.doFinal(encryptBytes);
		return new String(decryptBytes);
	}

	public static String aesDecrypt(String encryptStr, String decryptKey)
			throws Exception {
		return StringUtils.isBlank(encryptStr) ? null : aesDecryptByBytes(
				base64Decode(encryptStr), decryptKey);
	}

//////////////////////////////////


	/**
	 * AES/CBC/NoPadding encrypt
	 * 16 bytes secretKeyStr
	 * 16 bytes intVector
	 *
	 * @param secretKeyBytes
	 * @param intVectorBytes
	 * @param input
	 * @return
	 */
	public static byte[] encryptCBCNoPadding(byte[] secretKeyBytes, byte[] intVectorBytes, byte[] input) throws Exception {
		IvParameterSpec iv = new IvParameterSpec(intVectorBytes);
		SecretKey secretKey = new SecretKeySpec(secretKeyBytes, ALGORITHM);
		int inputLength = input.length;
		int srcLength;

		Cipher cipher = Cipher.getInstance(AES_CBC_NOPADDING);
		int blockSize = cipher.getBlockSize();
		byte[] srcBytes;
		if (0 != inputLength % blockSize) {
			srcLength = inputLength + (blockSize - inputLength % blockSize);
			srcBytes = new byte[srcLength];
			System.arraycopy(input, 0, srcBytes, 0, inputLength);
		} else {
			srcBytes = input;
		}

		cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
		byte[] encryptBytes = cipher.doFinal(srcBytes);
		return encryptBytes;
	}


	/**
	 * AES/CBC/NoPadding decrypt
	 * 16 bytes secretKeyStr
	 * 16 bytes intVector
	 *
	 * @param secretKeyBytes
	 * @param intVectorBytes
	 * @param input
	 * @return
	 */
	public static byte[] decryptCBCNoPadding(byte[] secretKeyBytes, byte[] intVectorBytes, byte[] input) throws Exception {
		try {
			IvParameterSpec iv = new IvParameterSpec(intVectorBytes);
			SecretKey secretKey = new SecretKeySpec(secretKeyBytes, ALGORITHM);

			Cipher cipher = Cipher.getInstance(AES_CBC_NOPADDING);
			cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
			byte[] encryptBytes = cipher.doFinal(input);
			return encryptBytes;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 用 AES 算法加密 inputStr。
	 * 使用 secretStr 作为 key，secretStr 的前 16 个字节作为 iv。
	 *
	 * @param secretStr
	 * @param inputStr
	 * @return
	 */
	public static byte[] encode(String secretStr, String inputStr) throws Exception {
		if (keyStrSzie != secretStr.length()) {
			return null;
		}
		byte[] secretKeyBytes = secretStr.getBytes(CHARSET);
		byte[] ivBytes = Arrays.copyOfRange(secretKeyBytes, 0, 16);
		byte[] inputBytes = inputStr.getBytes(CHARSET);

		byte[] outputBytes = encryptCBCNoPadding(secretKeyBytes, ivBytes, inputBytes);
		return outputBytes;
	}

	/**
	 * 用 AES 算法加密 inputStr。
	 * 使用 secretStr 作为 key，secretStr 的前 16 个字节作为 iv。
	 * 并对加密后的字节数组调用 Base64.encodeBase64String 方法，
	 * 转换成 base64 字符串返回。
	 *
	 * @param secretStr
	 * @param inputStr
	 * @return
	 */
	public static String strEncodBase64(String secretStr, String inputStr) throws Exception {
		String base64Str = Base64.encodeBase64String(encode(secretStr, inputStr));
		return base64Str;
	}

	/**
	 * 用 AES 算法加密 inputStr。
	 * 使用 secretStr 作为 key，secretStr 的前 16 个字节作为 iv。
	 *
	 * @param secretStr
	 * @param inputBytes
	 * @return
	 */
	public static byte[] decode(String secretStr, byte[] inputBytes) throws Exception {
		if (keyStrSzie != secretStr.length()) {
			return null;
		}
		byte[] secretKeyBytes = secretStr.getBytes(CHARSET);
		byte[] ivBytes = Arrays.copyOfRange(secretKeyBytes, 0, 16);

		byte[] outputBytes = decryptCBCNoPadding(secretKeyBytes, ivBytes, inputBytes);
		return outputBytes;
	}

	/**
	 * 用 AES 算法解密 inputStr。
	 * 使用 secretStr 作为 key，secretStr 的前 16 个字节作为 iv。
	 * 并对加密后的字节数组调用 Base64.decodeBase64 方法，
	 * 从 base64 字符串解码。
	 *
	 * （仅作为测试用途，具体加密流程以接口文档为准）
	 *
	 * @param secretStr
	 * @param inputStr
	 * @return
	 * @throws IOException
	 */
	public static String base64StrDecode(String secretStr, String inputStr) throws Exception {
		byte[] inputBytes;
		inputBytes = Base64.decodeBase64(inputStr);
		String outputStr = new String(decode(secretStr, inputBytes), CHARSET);
//		System.out.println("base64Decode > base64 decrypt " + outputStr);
		return outputStr;
	}

}
