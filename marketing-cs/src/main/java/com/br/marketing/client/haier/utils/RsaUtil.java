package com.br.marketing.client.haier.utils;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

/**
 * RSA加、解密算法工具类
 */
public class RsaUtil {
    /**
     * 加密算法AES
     */
    private static final String KEY_ALGORITHM = "RSA";

    /**
     * 算法名称/加密模式/数据填充方式
     * 默认：RSA/ECB/PKCS1Padding
     */
    private static final String ALGORITHMS = "RSA/ECB/PKCS1Padding";

    /**
     * Map获取公钥的key
     */
    private static final String PUBLIC_KEY = "publicKey";

    /**
     * Map获取私钥的key
     */
    private static final String PRIVATE_KEY = "privateKey";

    /**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 117;

    /**
     * RSA最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = 128;

    /**
     * RSA 位数 如果采用2048 上面最大加密和最大解密则须填写:  245 256
     * RSA 位数 如果采用1024 上面最大加密和最大解密则须填写:  117 128
     * RSA 位数 如果采用512 上面最大加密和最大解密则须填写:  53 64
     */
    private static final int INITIALIZE_LENGTH = 1024;

    /**
     * 后端RSA的密钥对(公钥和私钥)Map，由静态代码块赋值
     */
    private static Map<String, Object> genKeyPair = new HashMap<>();

//    static {
//        try {
//            genKeyPair.putAll(genKeyPair());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * 生成密钥对(公钥和私钥)
     */
    public static Map<String, Object> genKeyPair() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
//        SecureRandom sr = new SecureRandom();
//
//        keyPairGen.initialize(INITIALIZE_LENGTH,sr);
        keyPairGen.initialize(INITIALIZE_LENGTH);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        Map<String, Object> keyMap = new HashMap<String, Object>(2);
        //公钥
        keyMap.put(PUBLIC_KEY, publicKey);
        //私钥
        keyMap.put(PRIVATE_KEY, privateKey);

        genKeyPair.putAll(keyMap);
        return keyMap;
    }

    /**
     * 私钥解密
     *
     * @param encryptedData 已加密数据
     * @param privateKey    私钥(BASE64编码)
     */
    public static byte[] decryptByPrivateKey(byte[] encryptedData, String privateKey) throws Exception {
        //base64格式的key字符串转Key对象
        byte[] keyBytes = Base64.decodeBase64(privateKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);

        //设置加密、填充方式
        /*
            如需使用更多加密、填充方式，引入
            <dependency>
                <groupId>org.bouncycastle</groupId>
                <artifactId>bcprov-jdk16</artifactId>
                <version>1.46</version>
            </dependency>
            并改成
            Cipher cipher = Cipher.getInstance(ALGORITHMS ,new BouncyCastleProvider());
         */
        Cipher cipher = Cipher.getInstance(ALGORITHMS);
        cipher.init(Cipher.DECRYPT_MODE, privateK);

        //分段进行解密操作
        return encryptAndDecryptOfSubsection(encryptedData, cipher, MAX_DECRYPT_BLOCK);
    }

    /**
     * 公钥加密
     *
     * @param data      源数据
     * @param publicKey 公钥(BASE64编码)
     */
    public static byte[] encryptByPublicKey(byte[] data, String publicKey) throws Exception {
        //base64格式的key字符串转Key对象
        byte[] keyBytes = Base64.decodeBase64(publicKey);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key publicK = keyFactory.generatePublic(x509KeySpec);

        //设置加密、填充方式
        /*
            如需使用更多加密、填充方式，引入
            <dependency>
                <groupId>org.bouncycastle</groupId>
                <artifactId>bcprov-jdk16</artifactId>
                <version>1.46</version>
            </dependency>
            并改成
            Cipher cipher = Cipher.getInstance(ALGORITHMS ,new BouncyCastleProvider());
         */
        Cipher cipher = Cipher.getInstance(ALGORITHMS);
        cipher.init(Cipher.ENCRYPT_MODE, publicK);

        //分段进行加密操作
        return encryptAndDecryptOfSubsection(data, cipher, MAX_ENCRYPT_BLOCK);
    }

    /**
     * 获取私钥
     */
    public static String getPrivateKey() {
        Key key = (Key) genKeyPair.get(PRIVATE_KEY);
        return Base64.encodeBase64String(key.getEncoded());
    }

    /**
     * 获取公钥
     */
    public static String getPublicKey() {
        Key key = (Key) genKeyPair.get(PUBLIC_KEY);
        return Base64.encodeBase64String(key.getEncoded());
    }

    /**
     * 分段进行加密、解密操作
     */
    private static byte[] encryptAndDecryptOfSubsection(byte[] data, Cipher cipher, int encryptBlock) throws Exception {
        int inputLen = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段加密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > encryptBlock) {
                cache = cipher.doFinal(data, offSet, encryptBlock);
            } else {
                cache = cipher.doFinal(data, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * encryptBlock;
        }
        byte[] toByteArray = out.toByteArray();
        out.close();
        return toByteArray;
    }


    /**
     * RSA加密
     *
     * @param input           待加密
     * @param publicKeyString 公钥
     * @return {@link String }
     * @throws Exception 异常
     * @author senyang.zheng
     * @date 2023/12/23
     */
    public static String encrypt(byte[] input, String publicKeyString) throws Exception {
        byte[] bt = Base64.decodeBase64(publicKeyString);
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(bt);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] output = cipher.doFinal(input);
        return Base64.encodeBase64String(output);
    }


    /**
     * RSA解密
     *
     * @param input            待解密
     * @param privateKeyString 私钥
     * @return {@link String }
     * @throws Exception 异常
     * @author senyang.zheng
     * @date 2023/12/23
     */
    public static String decrypt(String input, String privateKeyString) throws Exception {
        byte[] bt = Base64.decodeBase64(privateKeyString);
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(bt);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] output = cipher.doFinal(Base64.decodeBase64(input));
        return new String(output);
    }


    /**
     * RSA签名
     *
     * @param content    内容
     * @param privateKey 私钥
     * @return {@link String }
     * @throws Exception 异常
     * @author senyang.zheng
     * @date 2023/12/23
     */
    public static String sign(String content, String privateKey) throws Exception {
        byte[] bt = Base64.decodeBase64(privateKey);
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(bt);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey priKey = keyFactory.generatePrivate(privateKeySpec);
        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initSign(priKey);
        signature.update(content.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeBase64String(signature.sign());
    }


    /**
     * RSA验签
     *
     * @param data      数据
     * @param sign      标志
     * @param publicKey 公钥
     * @return boolean
     * @throws Exception 异常
     * @author senyang.zheng
     * @date 2023/12/23
     */
    private static boolean checkSign(String data, String sign, String publicKey) throws Exception{
        byte[] bt = Base64.decodeBase64(publicKey);
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(bt);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pubKey = keyFactory.generatePublic(publicKeySpec);
        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initVerify(pubKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        return signature.verify(Base64.decodeBase64(sign));
    }

}
