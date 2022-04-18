package com.br.marketing.client.yiqianbao.utils;

import com.br.marketing.common.exception.BusinessException;
import com.google.common.base.Charsets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.security.KeyFactory;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Slf4j
public class RSAUtil {
    private static final String  ALGORITHM   = "RSA/ECB/PKCS1Padding";

    public static String encrypt(String content,String pubKeyStr){
        try {
            RSAPublicKey rsaPubKey = generatePublicRSAKey(pubKeyStr);
            byte[] data = content.getBytes(Charsets.UTF_8);
            int blockSize = getMaxBlockSize(rsaPubKey) - 11;
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, rsaPubKey);
            byte[] tmp = null;
            int offset = 0;
            int length = data.length;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while (length - offset > 0) {
                if (length - offset > blockSize) {
                    tmp = cipher.doFinal(data, offset, blockSize);
                } else {
                    tmp = cipher.doFinal(data, offset, length - offset);
                }
                baos.write(tmp, 0, tmp.length);
                offset += blockSize;
            }
            byte[] ciphered = baos.toByteArray();
            return new String(Base64.encodeBase64(ciphered), Charsets.UTF_8);
        }catch (Exception e){
            log.error("RSA encrypt error",e);
            throw new BusinessException("REA 加密失败");
        }

    }

    public static String decrypt(String data, String privateKeyStr) {
        try {
            RSAPrivateKey rsaPriKey = generatePrivateRSAKey(privateKeyStr);
            byte[] plainText = null;
            byte[] ciphered = Base64.decodeBase64(data.getBytes(Charsets.UTF_8));

            int length = ciphered.length;
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, rsaPriKey);
            byte[] tmp = null;
            int offset = 0;
            int blockSize = getMaxBlockSize(rsaPriKey);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while (length - offset > 0) {
                if (length - offset > blockSize) {
                    tmp = cipher.doFinal(ciphered, offset, blockSize);
                } else {
                    tmp = cipher.doFinal(ciphered, offset, length - offset);
                }
                baos.write(tmp, 0, tmp.length);
                offset += blockSize;
            }
            plainText = baos.toByteArray();
            return new String(plainText, Charsets.UTF_8);
        }catch (Exception e){
            log.error("RSA decrypt error",e);
            throw new BusinessException("REA 解密失败");
        }
    }
    private static RSAPublicKey generatePublicRSAKey(String publicKeyStr) throws Exception {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(
                Base64.decodeBase64(publicKeyStr.getBytes(Charsets.UTF_8)));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }    private static RSAPrivateKey generatePrivateRSAKey(String privateKeyStr) throws Exception {
        byte[] pribyte = Base64.decodeBase64(privateKeyStr.getBytes(Charsets.UTF_8));
        PKCS8EncodedKeySpec encodedKey = new PKCS8EncodedKeySpec(pribyte);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) keyFactory.generatePrivate(encodedKey);
    }


    private static int getMaxBlockSize(RSAKey key) {
        return key.getModulus().bitLength() / 8;
    }
}
