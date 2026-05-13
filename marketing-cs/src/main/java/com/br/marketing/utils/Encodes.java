package com.br.marketing.utils;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import java.io.UnsupportedEncodingException;


/**
 * 封装各种格式的编码解码工具类.
 *
 */
public class Encodes {

	// 默认URL编码
	private static final String DEFAULT_URL_ENCODING = "UTF-8";

	/**
	 * Hex编码.
	 */
	public static String encodeHex(byte[] input) {
		return String.valueOf(Hex.encodeHex(input));
	}

	/**
	 * Hex解码.
	 */
	public static byte[] decodeHex(String input) {
		try {
			return Hex.decodeHex(input.toCharArray());
		}catch (DecoderException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Base64编码.
	 */
	public static String encodeBase64(byte[] input) {
		return Base64.encodeBase64String(input);
	}

	/**
	 * Base64编码.
	 */
	public static String encodeBase642String(String input) {
		try {
			return Base64.encodeBase64String(input.getBytes(DEFAULT_URL_ENCODING));
		}catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Base64解码.
	 */
	public static byte[] decodeBase64(String input) {
		return Base64.decodeBase64(input.getBytes());
	}

	/**
	 * Base64解码.
	 * @param input: 字节数组
	 * @return: byte[] 编码后的字节数组
	 */
	public static byte[] decodeBase64(byte[] input) {
		return Base64.decodeBase64(input);
	}


}
