package com.br.marketing.client.shunfeng.utils;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
/**
 * @Description 顺丰提供AES加解密code
 * @Author zhen.Li1
 * @CreateTime 2024/10/29
 */
@Slf4j
public class AESUtil {

    private static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/ECB/PKCS5Padding";

    public static String encrypt(String plaintext, String key) throws Exception {
        SecretKeySpec secretKeySpec = new
                SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), AES_ALGORITHM);
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decrypt(String ciphertext, String key){
        try {
            SecretKeySpec secretKeySpec = new
                    SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), AES_ALGORITHM);
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] decodedBytes = Base64.getDecoder().decode(ciphertext);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        }catch (Exception ex){
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.SUNING_SERVICEERROR.getCode(), "顺丰获取企业信息解密失败！"), ex);
            return "";
        }
    }

    public static void main(String[] args) {
        try {
            String key = "91d23007684c45f5a750fa97d4594821";
            String decryptedText = decrypt("cUJ9dF2HBTMwn7pPUip7MQ==", key);
            System.out.println("解密后的文本: " + decryptedText);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
