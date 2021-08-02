package com.br.marketing.common.utils.file;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Bairong on 2020/2/27.
 */
@Slf4j
public class AesCrpyt {
    private static String TYPE = "AES";
    private static int KeySizeAES128 = 16;
    private static int BUFFER_SIZE = 8192;

    private static Cipher getCipher(int mode, String key) {
        Cipher mCipher;
        byte[] keyPtr = new byte[KeySizeAES128];
        IvParameterSpec ivParam = new IvParameterSpec(keyPtr);
        byte[] passPtr = key.getBytes(StandardCharsets.UTF_8);
        try {
            mCipher = Cipher.getInstance(TYPE + "/CBC/PKCS5Padding");
            for (int i = 0; i < KeySizeAES128; i++) {
                if (i < passPtr.length) {
                    keyPtr[i] = passPtr[i];
                } else {
                    keyPtr[i] = 0;
                }
            }
            SecretKeySpec keySpec = new SecretKeySpec(keyPtr, TYPE);
            mCipher.init(mode, keySpec, ivParam);
            return mCipher;
        } catch (InvalidKeyException e) {
            log.error("异常", e);
        } catch (NoSuchAlgorithmException e) {
            log.error("异常", e);
        } catch (NoSuchPaddingException e) {
            log.error("异常", e);
        } catch (InvalidAlgorithmParameterException e) {
            log.error("异常", e);
        }
        return null;
    }

    /**
     * 解密文件
     *
     * @param srcFile
     * @param destFile
     * @param privateKey
     */

    public static void decrypt(String srcFile, String destFile, String privateKey) {
        log.info("要解密的文件 {} 解密后存放的文件名:{} 密码：{}", srcFile, destFile, privateKey);
        byte[] readBuffer = new byte[BUFFER_SIZE];
        Cipher deCipher = getCipher(Cipher.DECRYPT_MODE, privateKey);
        if (deCipher == null) {
            return; //init failed.
        }
        int size;
        try (CipherInputStream fis = new CipherInputStream(
                new BufferedInputStream(
                        java.nio.file.Files.newInputStream(Paths.get(srcFile))), deCipher);
             BufferedOutputStream fos = new BufferedOutputStream(
                     java.nio.file.Files.newOutputStream(Paths.get(destFile))
             );) {

            while ((size = fis.read(readBuffer, 0, BUFFER_SIZE)) >= 0) {
                fos.write(readBuffer, 0, size);
            }
            fos.flush();
            log.info("文件 {} 解密完成，解密后的文件是:{}", srcFile, destFile);
        } catch (FileNotFoundException e) {
            log.error("解密异常", e);
        } catch (IOException e) {
            log.error("解密异常", e);
        }
    }

    /**
     * 加密文件
     *
     * @param srcFile
     * @param destFile
     * @param privateKey
     */

    public static void crypt(String srcFile, String destFile, String privateKey) {
        log.info("要加密的文件 {} 加密后存放的文件名:{} 密码：{}", srcFile, destFile, privateKey);
        byte[] readBuffer = new byte[BUFFER_SIZE];
        Cipher enCipher = getCipher(Cipher.ENCRYPT_MODE, privateKey);
        //init failed.
        if (enCipher == null) {
            return;
        }
        int size;
        try (CipherOutputStream fos = new CipherOutputStream(
                new BufferedOutputStream(
                        java.nio.file.Files.newOutputStream(Paths.get(destFile))), enCipher);
             BufferedInputStream fis = new BufferedInputStream(
                     java.nio.file.Files.newInputStream(Paths.get(srcFile)))) {

            while ((size = fis.read(readBuffer, 0, BUFFER_SIZE)) >= 0) {
                fos.write(readBuffer, 0, size);
            }
            fos.flush();
            log.info("文件 {}  加密完成，加密后的文件是:{}", srcFile, destFile);
        } catch (FileNotFoundException e) {
            log.error("加密异常", e);
        } catch (IOException e) {
            log.error("加密异常", e);
        }
    }
}
