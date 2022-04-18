package com.br.marketing.api.controller;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class test {

    private static final String  ALGORITHM       = "RSA/ECB/PKCS1Padding";

    public static String encrypt(String content,String pubKeyStr){
        try {
            RSAPublicKey rsaPubKey = generatePublicRSAKey(pubKeyStr);
            byte[] data = content.getBytes(StandardCharsets.UTF_8);
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
            return new String(Base64.encodeBase64(ciphered), StandardCharsets.UTF_8);
        }catch (Exception e){
        }
        return null;
    }

    public static String decrypt(String data, String privateKeyStr) {
        try {
            RSAPrivateKey rsaPriKey = generatePrivateRSAKey(privateKeyStr);
            byte[] plainText = null;
            byte[] ciphered = Base64.decodeBase64(data.getBytes(StandardCharsets.UTF_8));

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
            return new String(plainText, StandardCharsets.UTF_8);
        }catch (Exception e){
        }
        return null;
    }
    private static RSAPublicKey generatePublicRSAKey(String publicKeyStr) throws Exception {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(
                Base64.decodeBase64(publicKeyStr.getBytes(StandardCharsets.UTF_8)));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }    private static RSAPrivateKey generatePrivateRSAKey(String privateKeyStr) throws Exception {
        byte[] pribyte = Base64.decodeBase64(privateKeyStr.getBytes(StandardCharsets.UTF_8));
        PKCS8EncodedKeySpec encodedKey = new PKCS8EncodedKeySpec(pribyte);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) keyFactory.generatePrivate(encodedKey);
    }


    private static int getMaxBlockSize(RSAKey key) {
        return key.getModulus().bitLength() / 8;
    }

    public static void main(String[] args) {
        String encrypt = encrypt("123","MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuJ1QfWZeQad5zFb/w3Wu6Z7YZ1kEeqA95Rd/bHedc5vl4c9Lui2l0PaQCTaHfvbah+WINrK6C2qYPeh9L5nrggLekVmYlnI1FKo9iqtBr/2IhnGsw9rN9Kb8zLLUDQ7zyHqw5WuMWj8DTTQZry4fCU+aM6jxmA6mipXNxEsE1tYDgy4sY0L6r2dkWGfl208nOdEJHyZ6B3762en6IpTI5GI2dm6hZVQ+avdz9wv6mIkpSktTjFy1n66NyuXf1F4aRgy5EtwkXm56xSaxJlEHE9XjI4tR3pv5tcSjQxX9z52koA+y5nejtVAKbWwggG6MbO2qDdIkXfTSrU7BoDdOnQIDAQAB");
        String decrypt = decrypt(encrypt, "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC4nVB9Zl5Bp3nMVv/Dda7pnthnWQR6oD3lF39sd51zm+Xhz0u6LaXQ9pAJNod+9tqH5Yg2sroLapg96H0vmeuCAt6RWZiWcjUUqj2Kq0Gv/YiGcazD2s30pvzMstQNDvPIerDla4xaPwNNNBmvLh8JT5ozqPGYDqaKlc3ESwTW1gODLixjQvqvZ2RYZ+XbTyc50QkfJnoHfvrZ6foilMjkYjZ2bqFlVD5q93P3C/qYiSlKS1OMXLWfro3K5d/UXhpGDLkS3CRebnrFJrEmUQcT1eMji1Hem/m1xKNDFf3PnaSgD7Lmd6O1UAptbCCAboxs7aoN0iRd9NKtTsGgN06dAgMBAAECggEASyRk5ZWsGccEEUL7+V/GIPrxlCcsZokgiEWnLMwG/05eJCoO5am8yzAAcm+KeQga7KNlbPYUOZ9adiBSC/T3YcSvOLQiImI77rxYLkAEjZCBaE+OhW1i1Qi+7sZ+/w2t9lTR+Z9r0jPBsUPajG4WXeDTn+FM7JpR+Sh+Rz5Nqu9hrMM7vRzuyk86OM/eYYjAHA2AejtlqekKYdhVqmuYwDlAiBfsi9TfWnOgNDqCwAVF3bFq0q4gLfj6yp0yEHBwvCT1LkBC66norfqmWMixHxpP+foqo9zg68zmHGNJxnKeIuYoCLkKUymeIA7eRsg1lDyx0v7X8SguumTiyCC2YQKBgQDp/2ByapJcg9M3TBI2v+Segp+NMFhptMh8m7c/dCWjwRJcxAjD/4VQNQ2lo2dgn7OG0erwTxA9lm43YdoXzMn6gJJk2qtC8CKWG3ACkP6p9LGA8W4J2Cjkn91c7xBS8UTAVkV56IyxiGTuMo86XCpp4X7PBTnCHHjtoRJzsP8CaQKBgQDJ+TlLX03gD5gCebrkYsJNUleWtgVLphgtnqGs3TKk2NHpSebBijmUiHn8KdTiWPU1uhgUMpbFLQcEVMIR5EG95UQcxo7mmEGVLoEPdfpBLVni1z0veczuetsip0W06Nezbz6FDJwsYRuBU+4pKS9L3+mtbAhwYfCYaQNzOm+8FQKBgDr9wMjXiTJ9oWANc4IN+orjj2m+yGtNwkV31EjQ9TRFVmXAnh0ba/Z+iYQ6n7NCT7YrcblHlWaRaBPaPWtGm5zlkQKQBiEH/RAutpxPBYS/RYimVQFyV0zb6KinNExUuupqVXWYZO/U3O0YHfeuiJrM2HVEcf03xFzUAJzeAEwRAoGAczxPBEIMHJrJECCEEWqHkbY5YzPXMaJCiUTI2egazRMoMQKJNkO3NpfngYgcfkhjH5RdyKhUfYuDRWVWlP62w7Hbes+PGOImCQfPexLFBgrqDhLgRgYodqiB/vORQfbXmvkNorvfHzMIjnFkoPdulMvcd6X76qL2sv63iTb+KUECgYEAqmUllannkuc4L+tGmc+Z6qQDrV3sPTeMetLSh6Hoe1sw6M8TCXgcGas2Og44Wy0jM49o21QWYSOBBSkmeKia5akK2xfYpDMfmfLUiJRkPETeNLy+GY5rgVL+UgOO33ypJ+wW7pHlRASO4Z9f6oXwMxJnEUSi2rE8dXCflJvIJWU=");
        System.out.println(decrypt);
    }
}
