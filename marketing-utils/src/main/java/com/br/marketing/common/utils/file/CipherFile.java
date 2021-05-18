package com.br.marketing.common.utils.file;

/**
 * Created by Bairong on 2019/10/30.
 */
import lombok.extern.slf4j.Slf4j;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.SecureRandom;

/**
 * 使用 Cipher CipherInputStream CipherOutputStream 实现对文件的加解密
 * @auther jinsx
 * @date 2019-04-05 15:22
 */
@Slf4j
public class CipherFile {

    // 加密类型，支持这三种DESede,Blowfish,AES
    private static final String ENCRYPT_TYPE = "AES";
    // 加密秘钥，长度为24字节
    private static final String ENCRYPT_KEY = "mQbJILokBccRHUkS+XBk7A==";

    /**
     * 加密文件
     * @param srcFileName  要加密的文件
     * @param destFileName 加密后存放的文件名
     */
    public static  boolean encryptFile(String srcFileName, String destFileName,String encryptKey) {
        log.info("要加密的文件 {} 加密后存放的文件名:{} 密码：{}" ,srcFileName,destFileName,encryptKey);
        CipherInputStream cis = null;
        try (InputStream is =java.nio.file.Files.newInputStream(Paths.get(srcFileName));
             OutputStream out = java.nio.file.Files.newOutputStream(Paths.get(destFileName));) {

            //1.获取加密生成器
            KeyGenerator keygen=KeyGenerator.getInstance(ENCRYPT_TYPE);
            //2.根据ecnodeRules规则初始化密钥生成器
            //生成一个128位的随机源,根据传入的字节数组
            keygen.init(128, new SecureRandom(encryptKey.getBytes(StandardCharsets.UTF_8)));
            //3.产生原始对称密钥
            SecretKey originalKey=keygen.generateKey();
            //4.获得原始对称密钥的字节数组
            byte [] raw=originalKey.getEncoded();
            //5.根据字节数组生成AES密钥
            SecretKey key=new SecretKeySpec(raw, ENCRYPT_TYPE);
            //6.根据指定算法AES自成密码器
            Cipher cipher=Cipher.getInstance(ENCRYPT_TYPE);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            // 创建加密流
            cis = new CipherInputStream(is, cipher);
            byte[] buffer = new byte[1024];
            int r;
            while ((r = cis.read(buffer)) > 0) {
                out.write(buffer, 0, r);
            }
            log.info("文件 {}  加密完成，加密后的文件是:{}" ,srcFileName ,destFileName);
            return true;
        } catch (Exception e) {
            log.error("加密文件{}出现异常,{}",srcFileName,e);
            log.error("异常",e);
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (cis != null) {cis.close();}
            } catch (IOException e) {
                log.error("error",e);
            }
        }
    }

    /**
     * 解密文件
     * @param srcFileName  要解密的文件
     * @param destFileName 解密后存放的文件名
     */
    public static  boolean decryptFile(String srcFileName, String destFileName,String encryptKey) {
        log.info("要解密的文件 {} 解密后存放的文件名:{} 密码：{}" ,srcFileName,destFileName,encryptKey);
        CipherOutputStream cos = null;
        try (InputStream is =java.nio.file.Files.newInputStream(Paths.get(srcFileName));
             OutputStream out = java.nio.file.Files.newOutputStream(Paths.get(destFileName));){

            //1.获取加密生成器
            KeyGenerator keygen=KeyGenerator.getInstance(ENCRYPT_TYPE);
            //2.根据ecnodeRules规则初始化密钥生成器
            //生成一个128位的随机源,根据传入的字节数组
            keygen.init(128, new SecureRandom(encryptKey.getBytes(StandardCharsets.UTF_8)));
            //3.产生原始对称密钥
            SecretKey originalKey=keygen.generateKey();
            //4.获得原始对称密钥的字节数组
            byte [] raw=originalKey.getEncoded();
            //5.根据字节数组生成AES密钥
            SecretKey key=new SecretKeySpec(raw, ENCRYPT_TYPE);
            //6.根据指定算法AES自成密码器
            Cipher cipher=Cipher.getInstance(ENCRYPT_TYPE);
            cipher.init(Cipher.DECRYPT_MODE, key);
            // 创建解密流
            cos = new CipherOutputStream(out, cipher);
            byte[] buffer = new byte[1024];
            int r;
            while ((r = is.read(buffer)) > 0) {
                cos.write(buffer, 0, r);
            }
            log.info("文件 {} 解密完成，解密后的文件是:{}" ,srcFileName,destFileName);
            return true;
        } catch (Exception e) {
            log.error("解密文件{}出现异常",srcFileName);
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (cos != null) {cos.close();}
            } catch (IOException e) {
                log.error("error",e);
            }
        }
    }
}
