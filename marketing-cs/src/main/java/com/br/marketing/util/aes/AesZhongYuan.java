package com.br.marketing.util.aes;

import com.br.marketing.utils.Encodes;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

/**
 * AES加解密工具类
 * 功能：AES加密、解密、生成向量、生成AES key
 */
public class AesZhongYuan {

	private static final String AES = "AES";

	/**
	 * 默认初始化的向量
	 */
	public static final String DEFAULT_IV_STRING = "16-Bytes--String";

	/**
	 * 默认向量长度
	 */
	private static final int DEFAULT_IV_SIZE = 16;

	/**
	 * 默认AES密钥长度
	 */
	private static final int DEFAULT_AES_KEY_SIZE = 128;

	/**
	 * CBC算法和偏移量，需要IV
	 */
	public static final String CBC_ALGORITHM_PADDING = "AES/CBC/PKCS5Padding";

	/**
	 * ECB算法和偏移量，不需要IV
	 */
	public static final String ECB_ALGORITHM_PADDING = "AES/ECB/PKCS5Padding";

	/**
	 * 随机数生成器
	 */
	private static final SecureRandom RANDOM = new SecureRandom();

	/**
	 * AES加密
	 * @param content 原始字符串
	 * @param base64Key AES base64编码的key
	 * @param iv 初始化向量，可以为空
	 * @param algorithmPadding 算法&偏移量
	 * @return 加密后的字节数组
	 * @throws Exception 抛出异常
	 */
	public static byte[] encryptWithBase64Key2Byte(String content, String base64Key, String iv, String algorithmPadding) throws Exception {
		byte[] key = Encodes.decodeBase64(base64Key);
		return encrypt2Byte(content.getBytes(StandardCharsets.UTF_8), key, iv, algorithmPadding);
	}

	/**
	 * AES加密
	 * @param content 原始字符串
	 * @param base64Key AES base64编码的key
	 * @param iv 初始化向量，可以为空
	 * @param algorithmPadding 算法&偏移量
	 * @return 加密后的base64编码字符串
	 * @throws Exception 抛出异常
	 */
	public static String encryptWithBase64Key2Base64String(String content, String base64Key, String iv, String algorithmPadding) throws Exception {
		byte[] key = Encodes.decodeBase64(base64Key);
		return encrypt2Base64String(content.getBytes(StandardCharsets.UTF_8), key, iv, algorithmPadding);
	}

	/**
	 * AES加密
	 * @param content 原始字符串
	 * @param base64Key AES base64编码的key
	 * @param iv 初始化向量，可以为空
	 * @param algorithmPadding 算法&偏移量
	 * @return 加密后的hex编码字符串
	 * @throws Exception 抛出异常
	 */
	public static String encryptWithBase64Key2HexString(String content, String base64Key, String iv, String algorithmPadding) throws Exception {
		byte[] key = Encodes.decodeBase64(base64Key);
		return encrypt2HexString(content.getBytes(StandardCharsets.UTF_8), key, iv, algorithmPadding);
	}

	/**
	 * AES加密
	 * @param content 原始字符串
	 * @param hexKey AES hex编码的key
	 * @param iv 初始化向量，可以为空
	 * @param algorithmPadding 算法&偏移量
	 * @return 加密后的字节数组
	 * @throws Exception 抛出异常
	 */
	public static byte[] encryptWithHexKey2Byte(String content, String hexKey, String iv, String algorithmPadding) throws Exception {
		byte[] key = Encodes.decodeHex(hexKey);
		return encrypt2Byte(content.getBytes(StandardCharsets.UTF_8), key, iv, algorithmPadding);
	}

	/**
	 * AES加密
	 * @param content 原始字符串
	 * @param hexKey AES hex编码的key
	 * @param iv 初始化向量，可以为空
	 * @param algorithmPadding 算法&偏移量
	 * @return 加密后的base64编码字符串
	 * @throws Exception 抛出异常
	 */
	public static String encryptWithHexKey2Base64String(String content, String hexKey, String iv, String algorithmPadding) throws Exception {
		byte[] key = Encodes.decodeHex(hexKey);
		return encrypt2Base64String(content.getBytes(StandardCharsets.UTF_8), key, iv, algorithmPadding);
	}

	/**
	 * AES加密
	 * @param content 原始字符串
	 * @param hexKey AES hex编码的key
	 * @param iv 初始化向量，可以为空
	 * @param algorithmPadding 算法&偏移量
	 * @return 加密后的hex编码字符串
	 * @throws Exception 抛出异常
	 */
	public static String encryptWithHexKey2HexString(String content, String hexKey, String iv, String algorithmPadding) throws Exception {
		byte[] key = Encodes.decodeHex(hexKey);
		return encrypt2HexString(content.getBytes(StandardCharsets.UTF_8), key, iv, algorithmPadding);
	}

	/**
	 * AES加密
	 * @param content 原始字符串
	 * @param key AES key
	 * @param iv 初始化向量，可以为空
	 * @param algorithmPadding 算法&偏移量
	 * @return 加密后的字节数组
	 * @throws Exception 抛出异常
	 */
	public static byte[] encrypt2Byte(String content, byte[] key, String iv, String algorithmPadding) throws Exception {
		return encrypt2Byte(content.getBytes(StandardCharsets.UTF_8), key, iv, algorithmPadding);
	}

	/**
	 * AES加密
	 * @param content 原始加密数据
	 * @param key AES key
	 * @param iv 初始化向量，可以为空
	 * @param algorithmPadding 算法&偏移量
	 * @return 加密后Base64编码字符串
	 * @throws Exception 抛出异常
	 */
	public static String encrypt2Base64String(String content, byte[] key, String iv, String algorithmPadding) throws Exception {
		byte[] encryptedBytes = encrypt2Byte(content, key, iv, algorithmPadding);
		// 对加密后数据进行base64编码
		return Encodes.encodeBase64(encryptedBytes);
	}

	/**
	 * AES加密
	 * @param content 原始字节数组
	 * @param key AES key
	 * @param iv 初始化向量，可以为空
	 * @param algorithmPadding 算法&偏移量
	 * @return 加密后Base64编码字符串
	 * @throws Exception 抛出异常
	 */
	public static String encrypt2Base64String(byte[] content, byte[] key, String iv, String algorithmPadding) throws Exception {
		byte[] encryptedBytes = encrypt2Byte(content, key, iv, algorithmPadding);
		// 对加密后数据进行base64编码
		return Encodes.encodeBase64(encryptedBytes);
	}

	/**
	 * AES加密
	 * @param content 原始加密数据
	 * @param key AES key
	 * @param iv 初始化向量，可以为空
	 * @param algorithmPadding 算法&偏移量
	 * @return 加密后hex编码字符串
	 * @throws Exception 抛出异常
	 */
	public static String encrypt2HexString(String content, byte[] key, String iv, String algorithmPadding) throws Exception {
		byte[] encryptedBytes = encrypt2Byte(content, key, iv, algorithmPadding);
		// 对加密后数据进行hex编码
		return Encodes.encodeHex(encryptedBytes);
	}

	/**
	 * AES加密
	 * @param content 原始字节数组
	 * @param key AES key
	 * @param iv 初始化向量，可以为空
	 * @param algorithmPadding 算法&偏移量
	 * @return 加密后hex编码字符串
	 * @throws Exception 抛出异常
	 */
	public static String encrypt2HexString(byte[] content, byte[] key, String iv, String algorithmPadding) throws Exception {
		byte[] encryptedBytes = encrypt2Byte(content, key, iv, algorithmPadding);
		// 对加密后数据进行hex编码
		return Encodes.encodeHex(encryptedBytes);
	}

	/**
	 * AES加密
	 * @param content 原始字节数组
	 * @param key AES key
	 * @param iv 初始化向量，可以为空
	 * @param algorithmPadding 算法&偏移量
	 * @return 加密后字节数组
	 * @throws Exception 抛出异常
	 */
	public static byte[] encrypt2Byte(byte[] content, byte[] key, String iv, String algorithmPadding) throws Exception {
		SecretKeySpec secretKeySpec = new SecretKeySpec(key, AES);
		Cipher cipher = Cipher.getInstance(algorithmPadding);
		if (StringUtils.isEmpty(iv)) {
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
		}else {
			byte[] initParam = iv.getBytes();
			IvParameterSpec ivParameterSpec = new IvParameterSpec(initParam);
			// 指定加密的算法、工作模式和填充方式
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
		}
		return cipher.doFinal(content);
	}

	/**
	 * AES 解密
	 * @param base64Content 需要解密的字符串
	 * @param base64Key AES base64编码的key
	 * @param iv 初始化向量
	 * @param algorithmPadding 算法&偏移量
	 * @return 解密后的字节数组
	 * @throws Exception 抛出异常
	 */
	public static byte[] decryptBase64ContentWithBase64Key2Byte(String base64Content,
																String base64Key,
																String iv,
																String algorithmPadding) throws Exception {
		byte[] keyBytes = Encodes.decodeBase64(base64Key);
		byte[] content = Encodes.decodeBase64(base64Content);
		return decrypt2Byte(content, keyBytes, iv, algorithmPadding);
	}

	/**
	 * AES 解密
	 * @param base64Content 需要解密的字符串
	 * @param base64Key AES base64编码的key
	 * @param iv 初始化向量
	 * @param algorithmPadding 算法&偏移量
	 * @return 解密后的base64编码字符串
	 * @throws Exception 抛出异常
	 */
	public static String decryptBase64ContentWithBase64Key2Base64String(String base64Content,
																		String base64Key,
																		String iv,
																		String algorithmPadding) throws Exception {
		byte[] keyBytes = Encodes.decodeBase64(base64Key);
		byte[] content = Encodes.decodeBase64(base64Content);
		return decrypt2Base64String(content, keyBytes, iv, algorithmPadding);
	}

	/**
	 * AES 解密
	 * @param base64Content 需要解密的字符串
	 * @param base64Key AES base64编码的key
	 * @param iv 初始化向量
	 * @param algorithmPadding 算法&偏移量
	 * @return 解密后的hex编码字符串
	 * @throws Exception 抛出异常
	 */
	public static String decryptBase64ContentWithBase64Key2HexString(String base64Content,
																	 String base64Key,
																	 String iv,
																	 String algorithmPadding) throws Exception {
		byte[] keyBytes = Encodes.decodeBase64(base64Key);
		byte[] content = Encodes.decodeBase64(base64Content);
		return decrypt2HexString(content, keyBytes, iv, algorithmPadding);
	}

	/**
	 * AES 解密
	 * @param base64Content 需要解密的加密字符串，base64编码
	 * @param hexKey AES hex编码的key
	 * @param iv 初始化向量
	 * @param algorithmPadding 算法&偏移量
	 * @return 解密后的字节数组
	 * @throws Exception 抛出异常
	 */
	public static byte[] decryptBase64ContentWithHexKey2Byte(String base64Content,
															 String hexKey,
															 String iv,
															 String algorithmPadding) throws Exception {
		byte[] keyBytes = Encodes.decodeHex(hexKey);
		byte[] content = Encodes.decodeBase64(base64Content);
		return decrypt2Byte(content, keyBytes, iv, algorithmPadding);
	}

	/**
	 * AES 解密
	 * @param base64Content 需要解密的加密字符串，base64编码
	 * @param hexKey AES hex编码的key
	 * @param iv 初始化向量
	 * @param algorithmPadding 算法&偏移量
	 * @return 解密后的base64编码字符串
	 * @throws Exception 抛出异常
	 */
	public static String decryptBase64ContentWithHexKey2Base64String(String base64Content,
																	 String hexKey,
																	 String iv,
																	 String algorithmPadding) throws Exception {
		byte[] keyBytes = Encodes.decodeHex(hexKey);
		byte[] content = Encodes.decodeBase64(base64Content);
		return decrypt2Base64String(content, keyBytes, iv, algorithmPadding);
	}

	/**
	 * AES 解密
	 * @param base64Content 需要解密的加密字符串，base64编码
	 * @param hexKey AES hex编码的key
	 * @param iv 初始化向量
	 * @param algorithmPadding 算法&偏移量
	 * @return 解密后的hex编码字符串
	 * @throws Exception 抛出异常
	 */
	public static String decryptBase64ContentWithHexKey2HexString(String base64Content,
																  String hexKey,
																  String iv,
																  String algorithmPadding) throws Exception {
		byte[] keyBytes = Encodes.decodeHex(hexKey);
		byte[] content = Encodes.decodeBase64(base64Content);
		return decrypt2HexString(content, keyBytes, iv, algorithmPadding);
	}

	/**
	 * AES 解密
	 * @param content 需要解密的字节数组
	 * @param base64Key AES base64编码的key
	 * @param iv 初始化向量
	 * @param algorithmPadding 算法&偏移量
	 * @return 解密后的字节数组
	 * @throws Exception 抛出异常
	 */
	public static byte[] decryptWithBase64Key2Byte(byte[] content, String base64Key, String iv, String algorithmPadding) throws Exception {
		byte[] keyBytes = Encodes.decodeBase64(base64Key);
		return decrypt2Byte(content, keyBytes, iv, algorithmPadding);
	}

	/**
	 * AES 解密
	 * @param content 需要解密的字节数组
	 * @param base64Key AES base64编码的key
	 * @param iv 初始化向量
	 * @param algorithmPadding 算法&偏移量
	 * @return 解密后的base64编码字符串
	 * @throws Exception 抛出异常
	 */
	public static String decryptWithBase64Key2Base64String(byte[] content, String base64Key, String iv, String algorithmPadding) throws Exception {
		byte[] keyBytes = Encodes.decodeBase64(base64Key);
		return decrypt2Base64String(content, keyBytes, iv, algorithmPadding);
	}

	/**
	 * AES 解密
	 * @param content 需要解密的字节数组
	 * @param base64Key AES base64编码的key
	 * @param iv 初始化向量
	 * @param algorithmPadding 算法&偏移量
	 * @return 解密后的base64编码字符串
	 * @throws Exception 抛出异常
	 */
	public static String decryptWithBase64Key2HexString(byte[] content, String base64Key, String iv, String algorithmPadding) throws Exception {
		byte[] keyBytes = Encodes.decodeBase64(base64Key);
		return decrypt2HexString(content, keyBytes, iv, algorithmPadding);
	}

	/**
	 * AES 解密
	 * @param content 需要解密的字节数组
	 * @param hexKey AES hex编码的key
	 * @param iv 初始化向量
	 * @param algorithmPadding 算法&偏移量
	 * @return 解密后的字节数组
	 * @throws Exception 抛出异常
	 */
	public static byte[] decryptWithHexKey2Byte(byte[] content, String hexKey, String iv, String algorithmPadding) throws Exception {
		byte[] keyBytes = Encodes.decodeHex(hexKey);
		return decrypt2Byte(content, keyBytes, iv, algorithmPadding);
	}

	/**
	 * AES 解密
	 * @param content 需要解密的字节数组
	 * @param hexKey AES hex编码的key
	 * @param iv 初始化向量
	 * @param algorithmPadding 算法&偏移量
	 * @return 解密后的base64编码字符串
	 * @throws Exception 抛出异常
	 */
	public static String decryptWithHexKey2Base64String(byte[] content, String hexKey, String iv, String algorithmPadding) throws Exception {
		byte[] keyBytes = Encodes.decodeHex(hexKey);
		return decrypt2Base64String(content, keyBytes, iv, algorithmPadding);
	}

	/**
	 * AES 解密
	 * @param content 需要解密的字节数组
	 * @param hexKey AES hex编码的key
	 * @param iv 初始化向量
	 * @param algorithmPadding 算法&偏移量
	 * @return 解密后的hex编码字符串
	 * @throws Exception 抛出异常
	 */
	public static String decryptWithHexKey2HexString(byte[] content, String hexKey, String iv, String algorithmPadding) throws Exception {
		byte[] keyBytes = Encodes.decodeHex(hexKey);
		return decrypt2HexString(content, keyBytes, iv, algorithmPadding);
	}

	/**
	 * AES解密
	 * @param base64Content 需要解密的字符串，base64编码
	 * @param key AES key
	 * @param iv 初始化向量
	 * @param algorithmPadding 算法&偏移量
	 * @return 解密后的字节数组
	 * @throws Exception 抛出异常
	 */
	public static byte[] decryptBase64Content2Byte(String base64Content, byte[] key, String iv, String algorithmPadding) throws Exception {
		byte[] content = Encodes.decodeBase64(base64Content);
		return decrypt2Byte(content, key, iv, algorithmPadding);
	}

	/**
	 * AES解密
	 * @param base64Content 需要解密的字符串，base64编码
	 * @param key AES key
	 * @param iv 初始化向量
	 * @param algorithmPadding 算法&偏移量
	 * @return 解密后的字符串
	 * @throws Exception 抛出异常
	 */
	public static String decryptBase64Content2String(String base64Content, byte[] key, String iv, String algorithmPadding) throws Exception {
		byte[] content = Encodes.decodeBase64(base64Content);
		return decrypt2String(content, key, iv, algorithmPadding);
	}

	/**
	 * AES解密
	 * @param base64Content 需要解密的字符串，base64编码
	 * @param key AES key
	 * @param iv 初始化向量
	 * @param algorithmPadding 算法&偏移量
	 * @return 解密后的base64编码字符串
	 * @throws Exception 抛出异常
	 */
	public static String decryptBase64Cotent2Base64String(String base64Content, byte[] key, String iv, String algorithmPadding) throws Exception {
		byte[] content = Encodes.decodeBase64(base64Content);
		return decrypt2Base64String(content, key, iv, algorithmPadding);
	}

	/**
	 * AES解密
	 * @param base64Content 需要解密的字符串，base64编码
	 * @param key AES key
	 * @param iv 初始化向量
	 * @param algorithmPadding 算法&偏移量
	 * @return 解密后的hex编码字符串
	 * @throws Exception 抛出异常
	 */
	public static String decryptBase64Cotent2HexString(String base64Content, byte[] key, String iv, String algorithmPadding) throws Exception {
		byte[] content = Encodes.decodeBase64(base64Content);
		return decrypt2HexString(content, key, iv, algorithmPadding);
	}

	/**
	 * AES解密
	 * @param content 需要解密的字节数组
	 * @param key AES key
	 * @param iv 初始化向量
	 * @param algorithmPadding 算法&偏移量
	 * @return 解密后的字符串
	 * @throws Exception 抛出异常
	 */
	public static String decrypt2String(byte[] content, byte[] key, String iv, String algorithmPadding) throws Exception {
		byte[] result = decrypt2Byte(content, key, iv, algorithmPadding);
		return new String(result, StandardCharsets.UTF_8);
	}

	/**
	 * AES解密
	 * @param content 需要解密的字节数组
	 * @param key AES key
	 * @param iv 初始化向量
	 * @param algorithmPadding 算法&偏移量
	 * @return 解密后的base64编码字符串
	 * @throws Exception 抛出异常
	 */
	public static String decrypt2Base64String(byte[] content, byte[] key, String iv, String algorithmPadding) throws Exception {
		byte[] result = decrypt2Byte(content, key, iv, algorithmPadding);
		return Encodes.encodeBase64(result);
	}

	/**
	 * AES解密
	 * @param content 需要解密的字节数组
	 * @param key AES key
	 * @param iv 初始化向量
	 * @param algorithmPadding 算法&偏移量
	 * @return 解密后的hex编码字符串
	 * @throws Exception 抛出异常
	 */
	public static String decrypt2HexString(byte[] content, byte[] key, String iv, String algorithmPadding) throws Exception {
		byte[] result = decrypt2Byte(content, key, iv, algorithmPadding);
		return Encodes.encodeHex(result);
	}

	/**
	 * AES解密
	 * @param content 需要解密的字节数组
	 * @param key AES key
	 * @param iv 初始化向量
	 * @param algorithmPadding 算法&偏移量
	 * @return 解密后的字节数组
	 * @throws Exception 抛出异常
	 */
	public static byte[] decrypt2Byte(byte[] content, byte[] key, String iv, String algorithmPadding) throws Exception {
		SecretKeySpec secretKey = new SecretKeySpec(key, AES);
		Cipher cipher = Cipher.getInstance(algorithmPadding);
		if (StringUtils.isEmpty(iv)) {
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
		}else {
			byte[] initParam = iv.getBytes();
			IvParameterSpec ivParameterSpec = new IvParameterSpec(initParam);
			cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
		}
		return cipher.doFinal(content);
	}

	/**
	 * 生成AES密钥16进制编码字符串, 默认长度为128位(16字节).
	 */
	public static String generateKeyHexString() {
		return Encodes.encodeHex(generateAesKey(DEFAULT_AES_KEY_SIZE));
	}

	/**
	 * 生成AES密钥字16进制编码字符串
	 * @param keySize 可选长度为128,192,256位
	 * @return AES密钥字符串
	 */
	public static String generateKeyHexString(int keySize) {
		return Encodes.encodeHex(generateAesKey(keySize));
	}

	/**
	 * 生成AES密钥Base64编码字符串, 默认长度为128位(16字节).
	 */
	public static String generateKeyBase64String() {
		return Encodes.encodeBase64(generateAesKey(DEFAULT_AES_KEY_SIZE));
	}

	/**
	 * 生成AES密钥Base64编码字符串
	 * @param keySize 可选长度为128,192,256位
	 * @return AES密钥字符串
	 */
	public static String generateKeyBase64String(int keySize) {
		return Encodes.encodeBase64(generateAesKey(keySize));
	}

	/**
	 * 生成AES密钥,返回字节数组, 默认长度为128位(16字节).
	 */
	public static byte[] generateAesKey() {
		return generateAesKeyWithSeed(DEFAULT_AES_KEY_SIZE,  null);
	}

	/**
	 * 生成AES密钥,可选长度为128,192,256位.
	 */
	public static byte[] generateAesKey(int keySize) {
		return generateAesKeyWithSeed(keySize, null);
	}

	/**
	 * 带Seed的AES密钥生成,返回字节数组.
	 */
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	public static byte[] generateAesKeyWithSeed(int keySize, String seed) {
		try {
			KeyGenerator keyGenerator = KeyGenerator.getInstance(AES);
			if (StringUtils.isNotBlank(seed)) {
				// 使用 clone 或新的 SecureRandom，但基于系统熵池
				SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
				secureRandom.setSeed(seed.getBytes(StandardCharsets.UTF_8));
				keyGenerator.init(keySize, secureRandom);
			} else {
				keyGenerator.init(keySize, SECURE_RANDOM);
			}
			SecretKey secretKey = keyGenerator.generateKey();
			return secretKey.getEncoded();
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 生成随机向量,默认大小为cipher.getBlockSize(), 16字节.
	 */
	public static byte[] generateIv() {
		byte[] bytes = new byte[DEFAULT_IV_SIZE];
		RANDOM.nextBytes(bytes);
		return bytes;
	}

}
