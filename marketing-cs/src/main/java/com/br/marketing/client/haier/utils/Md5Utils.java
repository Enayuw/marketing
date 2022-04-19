package com.br.marketing.client.haier.utils;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Utils {

	private static String md5(String data) throws NoSuchAlgorithmException,
			UnsupportedEncodingException {
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		byte b[] = data.getBytes("UTF8");
		md5.update(b, 0, b.length);
		return byteArrayToHexString(md5.digest());
	}

	private static String byteArrayToHexString(byte b[]) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < b.length; i++) {
			sb.append(byteToHexString(b[i]));
		}
		return sb.toString();
	}

	private static String byteToHexString(byte b) {
		int n = b;
		if (n < 0) {
			n = 256 + n;
		}
		int d1 = n / 16;
		int d2 = n % 16;
		return hexDigits[d1] + hexDigits[d2];
	}

	private static String[] hexDigits = {"0", "1", "2", "3", "4", "5", "6",
			"7", "8", "9", "a", "b", "c", "d", "e", "f"};

	/**
	 * @param msg
	 * @return
	 */
	public static String genMd5(String msg) {
		String messageDigest = null;
		try {
			messageDigest = md5(msg);
		} catch (Exception e) {
			throw new RuntimeException("Md5 Error. Cause: ", e);
		}

		return messageDigest;
	}

	/**
	 * 生成32位小写MD5字符串(建议使用guava实现)
	 * @param key 待加密字符串
	 * @return MD5加密串
	 */
	public static String stringToMD5(String key) {
		return Hashing.md5().newHasher().putString(key, Charsets.UTF_8).hash().toString();
	}

}
