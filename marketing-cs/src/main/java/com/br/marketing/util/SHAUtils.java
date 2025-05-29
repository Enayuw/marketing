package com.br.marketing.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

/**
 * @author peng.kang
 * @description: SHA算法
 * @date 2025/5/21 14:56
 */
@Slf4j
public class SHAUtils {
    /**
     * SHA加密
     *
     * @param data 待加密数据
     * @return byte[] 消息摘要
     * @throws Exception
     */
    public static byte[] encodeSHA(String data) throws Exception {

        // 执行消息摘要
        return DigestUtils.sha(data);
    }

    /**
     * SHAHex加密
     *
     * @param data 待加密数据
     * @return String 消息摘要
     * @throws Exception
     */
    public static String encodeSHAHex(String data) throws Exception {

        // 执行消息摘要
        return DigestUtils.shaHex(data);
    }

    /**
     * SHA-224加密
     *
     * @param data 待加密数据
     * @return byte[] 消息摘要
     * @throws Exception
     */
    public static byte[] encodeSHA224(byte[] data) throws Exception {
        // 加入BouncyCastleProvider支持
        Security.addProvider(new BouncyCastleProvider());

        // 初始化MessageDigest
        MessageDigest md = MessageDigest.getInstance("SHA-224");

        // 执行消息摘要
        return md.digest(data);
    }

    public static String encryptSHA1(String content) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-1");
            digest.update(content.getBytes());
            byte messageDigest[] = digest.digest();
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String shaHex = Integer.toHexString(messageDigest[i]& 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage(),e);
        }
        return null;
    }

    public static String encryptSHA1(byte[] buf) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-1");
            digest.update(buf);
            byte messageDigest[] = digest.digest();
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String shaHex = Integer.toHexString(messageDigest[i]& 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage(),e);
        }
        return null;
    }

    /**
     * SHA-224加密
     *
     * @param data 待加密数据
     * @return byte[] 消息摘要
     * @throws Exception
     */
    public static String encodeSHA224Hex(byte[] data) throws Exception {

        // 执行消息摘要
        byte[] b = encodeSHA224(data);

        // 做十六进制编码处理
        return new String(Hex.encode(b));

    }

    /**
     * SHA256加密
     *
     * @param data 待加密数据
     * @return byte[] 消息摘要
     * @throws Exception
     */
    public static byte[] encodeSHA256(String data) throws Exception {

        // 执行消息摘要
        return DigestUtils.sha256(data);
    }

    /**
     * SHA256Hex加密
     *
     * @param data 待加密数据
     * @return String 消息摘要
     * @throws Exception
     */
    public static String encodeSHA256Hex(String data) throws Exception {

        // 执行消息摘要
        return DigestUtils.sha256Hex(data);
    }

    /**
     * SHA384加密
     *
     * @param data 待加密数据
     * @return byte[] 消息摘要
     * @throws Exception
     */
    public static byte[] encodeSHA384(String data) throws Exception {

        // 执行消息摘要
        return DigestUtils.sha384(data);
    }

    /**
     * SHA384Hex加密
     *
     * @param data 待加密数据
     * @return String 消息摘要
     * @throws Exception
     */
    public static String encodeSHA384Hex(String data) throws Exception {

        // 执行消息摘要
        return DigestUtils.sha384Hex(data);
    }

    /**
     * SHA512Hex加密
     *
     * @param data 待加密数据
     * @return byte[] 消息摘要
     * @throws Exception
     */
    public static byte[] encodeSHA512(String data) throws Exception {

        // 执行消息摘要
        return DigestUtils.sha512(data);
    }

    /**
     * SHA512Hex加密
     *
     * @param data 待加密数据
     * @return String 消息摘要
     * @throws Exception
     */
    public static String encodeSHA512Hex(String data) throws Exception {

        // 执行消息摘要
        return DigestUtils.sha512Hex(data);
    }

    /**
     * 计算文件的sha1
     * @param file
     * @return
     */
    public static String getFileSha1(File file) {
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] buffer = new byte[1024 * 1024 * 10];

            int len = 0;
            while ((len = in.read(buffer)) > 0) {
                digest.update(buffer, 0, len);
            }
            String sha1 = new BigInteger(1, digest.digest()).toString(16);
            int length = 40 - sha1.length();
            if (length > 0) {
                for (int i = 0; i < length; i++) {
                    sha1 = "0" + sha1;
                }
            }
            return sha1;
        }  catch (Exception e) {
            System.out.println(e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }
        return "";
    }
    public static void main(String[] args) {


    }

}
