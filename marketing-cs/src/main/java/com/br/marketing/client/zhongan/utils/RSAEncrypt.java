package com.br.marketing.client.zhongan.utils;

import lombok.extern.slf4j.Slf4j;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.*;
import java.util.*;
import org.apache.commons.lang.StringUtils;


/**
 * ======================================================== RSA加解密工具类
 *
 * @author AngleSuper.Wei
 * @date 2017/11/23 ========================================================
 */
@Slf4j
public class RSAEncrypt {

    private static final String ALG_RSA             = "RSA";
    private static final String ALG_DSA             = "DSA";

    /** 签名算法 */
    public static final String  SIGNATURE_ALGORITHM = "MD5withRSA";

    /** 不需要叠加的字段 **/
    private static String       SIGNATURE           = "signature";

    /**
     * genKeyPair
     */
    public static Map<String, String> genKeyPair() {
        Map<String, String> map;
        try {
            map = new HashMap<String, String>();
            KeyPairGenerator keyPair = KeyPairGenerator.getInstance("RSA");
            keyPair.initialize(1024);
            KeyPair kp = keyPair.generateKeyPair();
            String pubKeyStr = Base64Util.encode(kp.getPublic().getEncoded());
            String priKeyStr = Base64Util.encode(kp.getPrivate().getEncoded());
            map.put("publicKey", pubKeyStr);
            map.put("privateKey", priKeyStr);
        } catch (Exception e) {
            log.warn("加密加签出错", e);
            map = null;
        }
        return map;
    }

    public static byte[] encryptData(byte[] dataToEncrypt, String publicKeyStr, String encryptAlgorithm)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException, InvalidKeySpecException {
        encryptAlgorithm = StringUtils.isEmpty(encryptAlgorithm) ? ALG_RSA : encryptAlgorithm;
        Cipher cipher = Cipher.getInstance(encryptAlgorithm);
        KeyFactory keyFac = KeyFactory.getInstance("RSA");
        PublicKey puk = keyFac.generatePublic(new X509EncodedKeySpec(Base64Util.decode(publicKeyStr)));
        cipher.init(Cipher.ENCRYPT_MODE, puk);
        return cipher.doFinal(dataToEncrypt);
    }

    /**
     * 根据业务参数拼接待签的字符串 业务参数按照ASCII值从小到大排序组成json串
     *
     * @param bizParamMap
     * @param
     * @return
     */
    public static String getSignContent(Map<String, Object> bizParamMap) {
        String signContent = null;
        StringBuilder stringBuilder = new StringBuilder();
        List<String> keys = new ArrayList<String>(bizParamMap.keySet());
        Collections.sort(keys);
        for (String key : keys) {
            if (StringUtils.equalsIgnoreCase(key, SIGNATURE)) {
                continue;
            }
            String value = bizParamMap.get(key) + "";
            if (StringUtils.isEmpty(value)) {
                continue;
            }
            stringBuilder.append(key).append("=").append(value).append("&");
        }
        signContent = stringBuilder.toString();
        if (signContent.endsWith("&")) {
            signContent = signContent.substring(0, signContent.length() - 1);
        }
        return signContent;
    }

    /**
     * sign
     */
    public static String sign(String data, String privateKey) {
        String sign;
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64Util.decode(privateKey));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey priKey = keyFactory.generatePrivate(keySpec);
            Signature si = Signature.getInstance("SHA1WithRSA");
            si.initSign(priKey);
            si.update(data.getBytes("UTF-8"));
            byte[] dataSign = si.sign();
            sign = Base64Util.encode(dataSign);
        } catch (Exception e) {
            log.warn("加签出错", e);
            sign = null;
        }
        return sign;
    }

    /**
     * sign
     */
    public static String signByInstance(String data, String privateKey, String instance) {
        String sign;
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64Util.decode(privateKey));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey priKey = keyFactory.generatePrivate(keySpec);
            Signature si = Signature.getInstance(instance);
            si.initSign(priKey);
            si.update(data.getBytes("UTF-8"));
            byte[] dataSign = si.sign();
            sign = Base64Util.encode(dataSign);
        } catch (Exception e) {
            log.debug("加签出错", e);
            sign = null;
        }
        return sign;
    }

    public static byte[] genSignature(byte[] input, String key, String signAlgorithm) throws NoSuchAlgorithmException,
            InvalidKeyException, SignatureException, IOException, NoSuchProviderException, InvalidKeySpecException {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64Util.decode(key));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey priKey = keyFactory.generatePrivate(keySpec);
        signAlgorithm = StringUtils.isEmpty(signAlgorithm) ? SIGNATURE_ALGORITHM : signAlgorithm;
        Signature sig = Signature.getInstance(signAlgorithm);
        sig.initSign(priKey);
        sig.update(input);
        return sig.sign();
    }

    /**
     * 用私钥对信息生成数字签名
     *
     * @param data //加密数据
     * @param privateKey //私钥
     * @return
     * @throws Exception
     */
    public static String sign(byte[] data, String privateKey) throws Exception {
        /** 解密私钥 **/
        byte[] keyBytes = decryptBASE64(privateKey);
        //构造PKCS8EncodedKeySpec对象
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
        //指定加密算法
        KeyFactory keyFactory = KeyFactory.getInstance(ALG_RSA);
        //取私钥匙对象
        PrivateKey privateSignKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        //用私钥对信息生成数字签名
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(privateSignKey);
        signature.update(data);
        return encryptBASE64(signature.sign());
    }

    /**
     * verify
     */
    public static boolean verify(String data, String sign, String publicKey) {
        boolean succ;
        try {
            Signature verf = Signature.getInstance("SHA1WithRSA");
            KeyFactory keyFac = KeyFactory.getInstance("RSA");
            PublicKey puk = keyFac.generatePublic(new X509EncodedKeySpec(Base64Util.decode(publicKey)));
            verf.initVerify(puk);
            verf.update(data.getBytes("UTF-8"));
            succ = verf.verify(Base64Util.decode(sign));
        } catch (Exception e) {
            log.debug("验签出错", e);
            succ = false;
        }
        return succ;
    }

    /**
     * 校验数字签名
     *
     * @param data 加密数据
     * @param publicKey 公钥 合作机构公钥
     * @return
     * @throws Exception
     */
    public static boolean verifySign(String data, String sign, String publicKey, String signAture) {
        try {
            // 解密有base64编码的公钥
            byte[] keyBytes = decryptBASE64(publicKey);

            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            // 取公钥对象
            PublicKey pKey = keyFactory.generatePublic(keySpec);
            Signature signature = null;
            if (StringUtils.isNotBlank(signAture)) {
                signature = Signature.getInstance(signAture);
            } else {
                signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            }
            signature.initVerify(pKey);
            signature.update(data.getBytes());
            // 验证签名是否正常
            return signature.verify(decryptBASE64(sign));
        } catch (Exception e) {
            log.debug("验签出错", e);
            return false;
        }
    }


    /**
     * 校验数字签名
     *
     * @param data 加密数据
     * @param publicKey 公钥
     * @param sign 数字签名
     * @return
     * @throws Exception
     */
    public static boolean verify(byte[] data, String publicKey, String sign) throws Exception {
        //解密公钥
        byte[] keyBytes = decryptBASE64(publicKey);
        //构造X509EncodedKeySpec对象
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
        //指定加密算法
        KeyFactory keyFactory = KeyFactory.getInstance(ALG_RSA);
        //取公钥匙对象
        PublicKey publicSignKey = keyFactory.generatePublic(x509EncodedKeySpec);
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(publicSignKey);
        signature.update(data);
        //验证签名是否正常
        return signature.verify(decryptBASE64(sign));
    }

    /**
     * BASE64解密
     *
     * @param key
     * @return
     * @throws Exception
     */
    public static byte[] decryptBASE64(String key) throws Exception {
        return Base64.getDecoder().decode(key);
    }

    /**
     * BASE64加密
     *
     * @param key
     * @return
     * @throws Exception
     */
    public static String encryptBASE64(byte[] key) throws Exception {
        return Base64.getEncoder().encodeToString(key);
    }

    /**
     * encrypt
     */
    public static String encrypt(String data, String publicKey) {
        //log.info("调用银商加密，银商公钥={}", publicKey);
        String encryptData;
        try {
            KeyFactory keyFac = KeyFactory.getInstance("RSA");
            //log.info("开始得到pubKey");
            PublicKey pubKey = keyFac.generatePublic(new X509EncodedKeySpec(Base64Util.decode(publicKey)));
            //log.info("转换格式后的公钥={}", pubKey);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            byte[] result = cipher(data.getBytes("UTF-8"), cipher, getBlockSize(pubKey) - 11);
            //log.info("加密结果={}", result);
            encryptData = Base64Util.encode(result);
        } catch (Exception e) {
            log.debug("加密出错", e);
            encryptData = null;
        }
        return encryptData;
    }

    /**
     * decrypt
     */
    public static String decrypt(String encryptedData, String privateKey) {
        String decryptData;
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64Util.decode(privateKey));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey priKey = keyFactory.generatePrivate(keySpec);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, priKey);
            byte[] result = cipher(Base64Util.decode(encryptedData), cipher, getBlockSize(priKey));
            decryptData = new String(result, "UTF-8");
        } catch (Exception e) {
            System.out.println("解密出错");
            decryptData = null;
        }
        return decryptData;
    }

    /**
     * decrypt
     */
    public static byte[] decryptList(String encryptedData, String privateKey) {
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64Util.decode(privateKey));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey priKey = keyFactory.generatePrivate(keySpec);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, priKey);
            return cipher(Base64Util.decode(encryptedData), cipher, getBlockSize(priKey));
        } catch (Exception e) {
            System.out.println("解密出错");
//            LogUtil.Warn_rolling("解密出错", e);
        }
        return null;
    }

    private static byte[] cipher(byte[] data, Cipher cipher, int blockSize) throws Exception {
        final ByteArrayInputStream in = new ByteArrayInputStream(data);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final byte[] cache = new byte[blockSize];
        while (true) {
            final int r = in.read(cache);
            if (r < 0) {
                break;
            }
            final byte[] temp = cipher.doFinal(cache, 0, r);
            out.write(temp, 0, temp.length);
        }
        return out.toByteArray();
    }

    private static int getBlockSize(final Key key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        final String alg = key.getAlgorithm();
        final KeyFactory keyFactory = KeyFactory.getInstance(alg);
        if (key instanceof PublicKey) {
            final BigInteger prime;
            if (ALG_RSA.equals(alg)) {
                prime = keyFactory.getKeySpec(key, RSAPublicKeySpec.class).getModulus();
            } else if (ALG_DSA.equals(alg)) {
                prime = keyFactory.getKeySpec(key, DSAPublicKeySpec.class).getP();
            } else {
                throw new NoSuchAlgorithmException("不支持的解密算法：" + alg);
            }
            return prime.toString(2).length() / 8;
        } else if (key instanceof PrivateKey) {
            final BigInteger prime;
            if (ALG_RSA.equals(alg)) {
                prime = keyFactory.getKeySpec(key, RSAPrivateKeySpec.class).getModulus();
            } else if (ALG_DSA.equals(alg)) {
                prime = keyFactory.getKeySpec(key, DSAPrivateKeySpec.class).getP();
            } else {
                throw new NoSuchAlgorithmException("不支持的解密算法：" + alg);
            }
            return prime.toString(2).length() / 8;
        } else {
            throw new RuntimeException("不支持的密钥类型：" + key.getClass());
        }
    }


}
