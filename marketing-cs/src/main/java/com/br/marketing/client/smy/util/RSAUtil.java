package com.br.marketing.client.smy.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.poi.util.IOUtils;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("restriction")
public class RSAUtil {

	public static final String KEY_ALGORITHM = "RSA";
	public static final String CONTENT_TYPE = "UTF-8";


/*	private static final String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCl05+JYY+3ES9kcouinh" +
			"pipUDvgMZ5WOH6xOxbYOw9TJycqzkRBJzXzHuud9ZpC+EYLmi0YX5eoS" +
			"6dyU+jvwoFLaFNpYMyxByOlaFFME0DAmgu6FbNrohYDECGgBe3pgNECIYXVgM" +
			"4S+05QVfO4DEx4ybCW6ai7DzA6/fy+ihWXQIDAQAB";
	private static final String PRIVATE_KEY = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAKXTn4lhj7cRL2Ryi6KeGmKlQO+Ax" +
			"nlY4frE7Ftg7D1MnJyrOREEnNfMe6531mkL4RguaLRhfl6hLp3JT6O/CgUtoU2lgzLEHI6VoUUwTQMCaC7oVs2uiFgMQIaAF7e" +
			"mA0QIhhdWAzhL7TlBV87gMTHjJsJbpqLsPMDr9/L6KFZdAgMBAAECgYAN1eZNtBIlmA14OAZmfwJ8kIUmdnwYy82u+pct2JxdUPMQL" +
			"0kHnnbnmmwHVoBRK2iQd+7MfgXpJ1GYYJ+AbgQL4A6OMlbG0syReqDu8B+crxvHywygEbntJ6mulEuJhx1xRe/TRjHVa23FoOtx4hdDKumj" +
			"5UY+9YgrsAVvr1DQEQJBAN/ssfaNTbLBYU1Hut79XLIXfiJFc4WGwmXfvE+GcLfCWGU/hAnhW9CDUJvbttykU7oe+NAzV0tNA816BMgmue" +
			"sCQQC9lHquili6Ta4ptiqSjKmOp6LIe5OK2w8dWZGKi8LHuVFa7//R+r1V/01FvSIN+9xffRMxdbgRtvA8NyfW+BbXAkAlpHuSL9TU7O83d4xxefQA" +
			"ODTK/BPcaXf6ei8Ey1vdeD014ASJSKoPllORr9DArJsqmZRII6lUDkFKZG2zW9c7AkB59/EGIlP7aMbIZz70kjL0aL2+JmNCTrVmuJ1+eXSTcpL44re/rIXr" +
			"shlQ6OHkBUo+1QVoKMiH/R6KXyHumCZ5AkBX5o9TaXtIjyfd06nz69AIOwzMDDdnDD7BXFCklopCBwY7sD6NpP+KrgiqFQJ5cbggy4A3pYIOyjx/YkLWT7JI"*/;

	private RSAUtil() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * <P>
	 * 公钥解密
	 * </p>
	 *
	 * @param encryptedData
	 *            已加密数据
	 * @param publicKey
	 *            公钥(BASE64编码)
	 */
	public static byte[] decryptByPublicKey(byte[] encryptedData, String publicKey) throws Exception {

		RSAPublicKey publicK = getPublicKey(publicKey);
		Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, publicK);

		return rsaSplitCodec(cipher,Cipher.DECRYPT_MODE,encryptedData,publicK.getModulus().bitLength());
	}



	/**
	 * <p>
	 * 私钥加密
	 * </p>
	 *
	 * @param data
	 *            源数据
	 * @param privateKey
	 *            私钥(BASE64编码)
	 */
	public static byte[] encryptByPrivateKey(byte[] data, String privateKey) throws Exception {

		RSAPrivateKey privateK = getPrivateKey(privateKey);
		// 对数据加密
		Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, privateK);

		return rsaSplitCodec(cipher,Cipher.ENCRYPT_MODE,data,privateK.getModulus().bitLength());
	}

	/**
	 * 初始化密钥
	 * 
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> initKey() throws Exception {
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
		keyPairGen.initialize(1024);

		KeyPair keyPair = keyPairGen.generateKeyPair();

		// 公钥
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

		// 私钥
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

		Map<String, Object> keyMap = new HashMap<>(2);

		keyMap.put("publicKey", Base64.encodeBase64String(publicKey.getEncoded()));
		keyMap.put("privateKey", Base64.encodeBase64String(privateKey.getEncoded()));
		return keyMap;
	}

	/**
	 * <P>
	 * 私钥解密
	 * </p>
	 *
	 * @param encryptedData 已加密数据
	 * @param privateKey 私钥(BASE64编码)
	 * @return
	 * @throws Exception
	 */
	public static byte[] decryptByPrivateKey(byte[] encryptedData, String privateKey)
			throws Exception {

		RSAPrivateKey privateK = getPrivateKey(privateKey);
		Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, privateK);

		return rsaSplitCodec(cipher,Cipher.DECRYPT_MODE,encryptedData,privateK.getModulus().bitLength());
	}


	/**
	 * <p>
	 * 公钥加密
	 * </p>
	 *
	 * @param data 源数据
	 * @param publicKey 公钥(BASE64编码)
	 * @return
	 * @throws Exception
	 */
	public static byte[] encryptByPublicKey(byte[] data, String publicKey)
			throws Exception {
		RSAPublicKey publicK = getPublicKey(publicKey);
		// 对数据加密
		Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, publicK);
		return rsaSplitCodec(cipher,Cipher.ENCRYPT_MODE,data,publicK.getModulus().bitLength());
	}

	/**
	 * 得到公钥
	 * @param publicKey  密钥字符串（经过base64编码）
	 * @throws Exception
	 */
	public static RSAPublicKey getPublicKey(String publicKey) {
		// 通过X509编码的Key指令获得公钥对象
		try {
			KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
			X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(Base64.decodeBase64(publicKey));
			RSAPublicKey key = (RSAPublicKey) keyFactory.generatePublic(x509KeySpec);
			return key;
		}catch (Exception e) {
			throw new RuntimeException("初始化公钥[" + publicKey + "]的数据时发生异常", e);
		}
	}

	/**
	 * 得到私钥
	 * @param privateKey  密钥字符串（经过base64编码）
	 * @throws Exception
	 */
	public static RSAPrivateKey getPrivateKey(String privateKey){
		// 通过PKCS#8编码的Key指令获得私钥对象
		try {
			KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
			PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKey));
			RSAPrivateKey key = (RSAPrivateKey) keyFactory.generatePrivate(pkcs8KeySpec);
			return key;
		}catch (Exception e) {
			throw new RuntimeException("初始化私钥[" + privateKey + "]的数据时发生异常", e);
		}
	}

	//rsa切割解码  , ENCRYPT_MODE,加密数据   ,DECRYPT_MODE,解密数据
	private static byte[] rsaSplitCodec(Cipher cipher, int opmode, byte[] datas, int keySize) {
		int maxBlock = 0;  //最大块
		if (opmode == Cipher.DECRYPT_MODE) {
			maxBlock = keySize / 8;
		} else {
			maxBlock = keySize / 8 - 11;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] buff;
		int i = 0;
		try {
			while (datas.length > offSet) {
				if (datas.length - offSet > maxBlock) {
					//可以调用以下的doFinal（）方法完成加密或解密数据：
					buff = cipher.doFinal(datas, offSet, maxBlock);
				} else {
					buff = cipher.doFinal(datas, offSet, datas.length - offSet);
				}
				out.write(buff, 0, buff.length);
				i++;
				offSet = i * maxBlock;
			}
		} catch (Exception e) {
			throw new RuntimeException("加解密阀值为[" + maxBlock + "]的数据时发生异常", e);
		}
		byte[] resultDatas = out.toByteArray();
		IOUtils.closeQuietly(out);
		return resultDatas;
	}

}
