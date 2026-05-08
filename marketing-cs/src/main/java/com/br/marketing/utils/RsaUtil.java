package com.br.marketing.utils;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

/**
 * RSA加解密算法工具类
 * 功能：生成公钥、私钥、加密、解密
 *
 * @author xuxiao
 * @version 1.0
 * @date 2025/2/20 下午2:31
 */
public class RsaUtil {

	// 算法名称
	private static final String RSA = "RSA";
	// PKCS#1 v1.5，安全性中，使用广泛，旧版本填充方案
	public static final String PK_CS1 = "RSA/ECB/PKCS1Padding";
	// OAEP (SHA-1 + MGF1)，安全性高
	public static final String OAEP_WITH_SHA_1 = "RSA/ECB/OAEPWithSHA-1AndMGF1Padding";
	// OAEP (SHA-256 + MGF1)，安全性高
	public static final String OAEP_WITH_SHA_256 = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
	// OAEP (SHA-384 + MGF1)，安全性高
	public static final String OAEP_WITH_SHA_384 = "RSA/ECB/OAEPWithSHA-384AndMGF1Padding";
	// OAEP (SHA-512 + MGF1)，安全性高
	public static final String OAEP_WITH_SHA_512 = "RSA/ECB/OAEPWithSHA-512AndMGF1Padding";

	/**
	 * 生成RSA公钥私钥字符串，放到List中，用于日常生成公钥、私钥
	 * @param size 长度：1024、2048、4096
	 * @return  List<String> 0：公钥   1：私钥
	 * @throws NoSuchAlgorithmException 异常
	 */
	public static List<String> initRsaKey(int size) throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(RSA);
        keyPairGen.initialize(size);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        String publicKey = Encodes.encodeBase64(keyPair.getPublic().getEncoded());
        String privateKey = Encodes.encodeBase64(keyPair.getPrivate().getEncoded());
        List<String> list = new ArrayList<String>();
		//公钥
		list.add(publicKey);
		//私钥
		list.add(privateKey);
        return list;
    }
	
	/**
	 * 公钥字符串生成 RSA的公钥对象
	 * @param key 公钥字符串
	 * @return 公钥对象
	 * @throws Exception 异常
	 */
	public static PublicKey getPublicKey(String key) throws Exception {
        byte[] keyBytes = Encodes.decodeBase64(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA);
        return keyFactory.generatePublic(keySpec);
    }
	
	/**
	 * 私钥字符串生成RSA私钥对象
	 * @param key 私钥字符串
	 * @return 私钥对象
	 * @throws Exception 异常
	 */
	public static PrivateKey getPrivateKey(String key) throws Exception {
        byte[] keyBytes = Encodes.decodeBase64(key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA);
        return keyFactory.generatePrivate(keySpec);
    }

	/**
	 * 方法功能说明：使用RSA公钥加密字符串, 返回加密后的Base64字符串.
	 * 默认使用RSA/ECB/PKCS1Padding填充方案
	 * @param data 需要加密字符串
	 * @param rsaPublicKey 公钥对象
	 * @return 加密后字符串
	 */
	public static String encrypt2Base64String(String data, Key rsaPublicKey) {
		byte[] raw = data.getBytes();
		return encrypt2Base64String(raw, rsaPublicKey, null, PK_CS1);
	}

	/**
	 * 方法功能说明：使用RSA公钥加密字节数组, 返回加密后的字符串.
	 * @param raw 需要加密的字节数组
	 * @param rsaPublicKey RSA公钥
	 * @param provider 算法提供者
	 * @param paddingPattern 加密填充模式
	 * @return 加密后字符串
	 */
	public static String encrypt2Base64String(byte[] raw, Key rsaPublicKey, Provider provider, String paddingPattern) {
		try {
			byte[] data = encrypt2Byte(raw, rsaPublicKey, provider, paddingPattern);
			return Encodes.encodeBase64(data);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 方法功能说明：使用RSA公钥加密字符串, 返回加密后的字节数组
	 * @param data 需要加密字符串
	 * @param rsaPublicKey 公钥对象
	 * @return 加密后字节数组
	 */
	public static byte[] encrypt2Byte(String data, Key rsaPublicKey) {
		byte[] raw = data.getBytes();
		return encrypt2Byte(raw, rsaPublicKey, null, PK_CS1);
	}

	/**
	 * 方法功能说明：使用RSA公钥加密字符串, 返回加密后的字节数组
	 * @param raw: 需要加密的字节数组
	 * @param rsaPublicKey: 公钥
	 * @param provider: 算法提供者
	 * @param paddingPattern: 填充模式
	 * @return: byte[] 加密后的字节数组
	 */
	public static byte[] encrypt2Byte(byte[] raw, Key rsaPublicKey, Provider provider, String paddingPattern) {
		if(null == rsaPublicKey) {
			throw new RuntimeException("Key rsaPublicKey must be not null.");
		}
		try {
			return rsa(raw, Cipher.ENCRYPT_MODE, rsaPublicKey, provider, paddingPattern);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 方法功能说明：使用RSA私钥解密字节数组, 返回解密后字节数组.
	 * 默认使用RSA/ECB/PKCS1Padding填充方案
	 * @param base64Content 需要解密的base64加密字符串
	 * @param rsaPrivateKey RSA私钥对象
	 * @return 解密后字节数组
	 */
	public static byte[] decryptBase64Content2Byte(String base64Content, Key rsaPrivateKey) {
		byte[] raw = Encodes.decodeBase64(base64Content);
		return decrypt2Byte(raw, rsaPrivateKey, null, PK_CS1);
	}

	/**
	 * 方法功能说明：使用RSA私钥解密字节数组, 返回解密后字节数组.
	 * 默认使用RSA/ECB/PKCS1Padding填充方案
	 * @param raw 需要解密的字节数组
	 * @param rsaPrivateKey RSA私钥对象
	 * @return 解密后字节数组
	 */
	public static byte[] decrypt2Byte(byte[] raw, Key rsaPrivateKey) {
		return decrypt2Byte(raw, rsaPrivateKey, null, PK_CS1);
	}

	/**
	 * 方法功能说明：使用RSA私钥解密字节数组, 返回解密后字符串.
	 * @param base64Content 需要解密的base64编码字符串
	 * @param rsaPrivateKey RSA私钥
	 * @param provider 算法提供者
	 * @param paddingPattern 加密填充模式
	 * @return 解密后字符串
	 */
	public static String decryptBase64Content2String(String base64Content, Key rsaPrivateKey, Provider provider, String paddingPattern) {
		byte[] raw = Encodes.decodeBase64(base64Content);
		return decrypt2String(raw, rsaPrivateKey, provider, paddingPattern);
	}
	
	/**
	 * 方法功能说明：使用RSA私钥解密字节数组, 返回解密后字符串.
	 * @param raw 需要解密的字节数组
	 * @param rsaPrivateKey RSA私钥
	 * @param provider 算法提供者
	 * @param paddingPattern 加密填充模式
	 * @return 解密后字符串
	 */
	public static String decrypt2String(byte[] raw, Key rsaPrivateKey, Provider provider, String paddingPattern) {
		byte[] data = decrypt2Byte(raw, rsaPrivateKey, provider, paddingPattern);
		return new String(data, StandardCharsets.UTF_8);
	}

	/**
	 * 方法功能说明：使用RSA私钥解密字节数组, 返回解密后字节数组
	 * @param base64Content 需要解密的base64编码字符串
	 * @param rsaPrivateKey RSA私钥
	 * @param provider 算法提供者
	 * @param paddingPattern 加密填充模式
	 * @return 解密后字符串
	 */
	public static byte[] decryptBase64Content2Byte(String base64Content, Key rsaPrivateKey, Provider provider, String paddingPattern) {
		byte[] raw = Encodes.decodeBase64(base64Content);
		return decrypt2Byte(raw, rsaPrivateKey, provider, paddingPattern);
	}

	/**
	 * 方法功能说明：使用RSA私钥解密字节数组, 返回解密后字节数组.
	 * @param raw 需要解密的字节数组
	 * @param rsaPrivateKey RSA私钥
	 * @param provider 算法提供者
	 * @param paddingPattern 加密填充模式
	 * @return 解密后字节数组
	 */
	public static byte[] decrypt2Byte(byte[] raw, Key rsaPrivateKey, Provider provider, String paddingPattern) {
		if(null == rsaPrivateKey) {
			throw new RuntimeException("Key rsaPrivateKey must be not null.");
		}
		try {
			return rsa(raw, Cipher.DECRYPT_MODE, rsaPrivateKey, provider, paddingPattern);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 方法功能说明：使用RSA加解密最基础api
	 * @param raw 需要加解密的数据
	 * @param mode 加解密模式 Cipher.ENCRYPT_MODE/Cipher.DECRYPT_MODE
	 * @param key 公钥/私钥
	 * @param provider 算法提供者，可以为空
	 * @param paddingPattern 加密填充模式
	 * @return 解密后的byte[]数据
	 */
	private static byte[] rsa(byte[] raw, int mode, Key key, Provider provider, String paddingPattern) {
		try {
			Cipher cipher = null;
			if(null == provider) {
				cipher = Cipher.getInstance(paddingPattern);
			}else {
			    cipher = Cipher.getInstance(paddingPattern, provider);
			}
			cipher.init(mode, key);
			return cipher.doFinal(raw);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 分段RSA加密
	 * @param data 待加密数据
	 * @param publicKey 公钥
	 * @param paddingPattern 填充模式
	 * @param keySize RSA密钥长度
	 * @return 加密后的数据
	 */
	public static byte[] segmentEncrypt(String data, PublicKey publicKey, String paddingPattern, int keySize) {
		return segmentEncrypt(data.getBytes(), publicKey, paddingPattern, keySize);
	}

	/**
	 * 分段RSA加密
	 * @param data 待加密数据
	 * @param publicKey 公钥
	 * @param paddingPattern 填充模式
	 * @param keySize RSA密钥长度
	 * @return 加密后的数据
	 */
	public static byte[] segmentEncrypt(byte[] data, PublicKey publicKey, String paddingPattern, int keySize) {
		try {
			// 减去填充字节
			int maxBlockSize = keySize / 8 - 11;
			Cipher cipher = Cipher.getInstance(paddingPattern);
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			int inputLen = data.length;
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int offSet = 0;
			byte[] cache;
			int i = 0;
			// 对数据分段加密
			while (inputLen - offSet > 0) {
				if (inputLen - offSet > maxBlockSize) {
					cache = cipher.doFinal(data, offSet, maxBlockSize);
				} else {
					cache = cipher.doFinal(data, offSet, inputLen - offSet);
				}
				out.write(cache, 0, cache.length);
				i++;
				offSet = i * maxBlockSize;
			}
			byte[] encryptedData = out.toByteArray();
			out.close();
			return encryptedData;
		}catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * 分段RSA解密
	 * @param base64Content 待解密Base64编码的字符串数据
	 * @param privateKey  私钥
	 * @param paddingPattern 填充模式
	 * @param keySize RSA密钥长度
	 * @return 解密后的数据
	 */
	public static byte[] segmentDecryptBase64Content(String base64Content, PrivateKey privateKey, String paddingPattern, int keySize) {
		// 原始报文是经过base64编码的，需要首先解码
		byte[] data = Encodes.decodeBase64(base64Content);
		return segmentDecrypt(data, privateKey, paddingPattern, keySize);
	}

	/**
	 * 分段RSA解密
	 * @param data 待解密数据，字节数组
	 * @param privateKey  私钥
	 * @param paddingPattern 填充模式
	 * @param keySize RSA密钥长度
	 * @return 解密后的数据
	 */
	public static byte[] segmentDecrypt(byte[] data, PrivateKey privateKey, String paddingPattern, int keySize) {
		try {
			Cipher cipher = Cipher.getInstance(paddingPattern);
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			// 最大解密的块
			int maxDecryptBlock = keySize / 8;
			int inputLen = data.length;
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int offSet = 0;
			byte[] cache;
			int i = 0;
			// 对数据分段解密
			while (inputLen - offSet > 0) {
				if (inputLen - offSet > maxDecryptBlock) {
					cache = cipher.doFinal(data, offSet, maxDecryptBlock);
				} else {
					cache = cipher.doFinal(data, offSet, inputLen - offSet);
				}
				out.write(cache, 0, cache.length);
				i++;
				offSet = i * maxDecryptBlock;
			}
			byte[] decryptedData = out.toByteArray();
			out.close();
			return decryptedData;

		}catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}