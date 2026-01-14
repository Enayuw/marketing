package com.br.marketing.client.suiyiji;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;

@Slf4j
public class SuiyijiRSAUtil {

    public static String decryptByPrivateKey(String data, String
            privateKey) {
        try {
            PKCS8EncodedKeySpec pkcs8KeySpec = new
                    PKCS8EncodedKeySpec(org.apache.commons.codec.binary.Base64.decodeBase64(privateKey));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            Cipher cipher =
                    Cipher.getInstance(keyFactory.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE,
                    keyFactory.generatePrivate(pkcs8KeySpec));
            byte[] encryptedData =
                    org.apache.commons.codec.binary.Base64.decodeBase64(data);
            ByteArrayOutputStream out = outputStream(encryptedData,
                    256, cipher);
            out.close();
            return out.toString("utf-8");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("随意记私钥解密异常： {}", e);
            return null;
        }
    }

    private static ByteArrayOutputStream outputStream(byte[] data,
                                                      int maxBlock, Cipher cipher) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int inputLen = data.length;
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段加密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > maxBlock) {
                cache = cipher.doFinal(data, offSet, maxBlock);
            } else {
                cache = cipher.doFinal(data, offSet, inputLen -
                        offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * maxBlock;
        }
        return out;
    }
}
