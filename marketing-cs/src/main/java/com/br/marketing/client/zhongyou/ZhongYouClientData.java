package com.br.marketing.client.zhongyou;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 描述：： 中邮数据加密处理组装
 * <p>
 * ------------------------------------
 *
 * @program: marketing
 * @ClassName ZhongYouClientData
 * @author: it-yml
 * @create: 2023-08-03 15:14
 * @Version 1.0
 * --------------------------------------
 **/

@Slf4j
@Component

public class ZhongYouClientData {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMddHHmmss");
    /**
     * 加密算法RSA
     */
    private static final String KEY_ALGORITHM = "RSA";

    /**
     * 签名算法
     */
    private static final String SIGN_ALGORITHMS = "SHA1WithRSA";

    /**
     * 中邮提供
     */
    private static final String AES_KEY = "3nbkz1h7kg7mueaq";

    /**
     * 中邮提供
     */
    private static final String AES_IV = "hfhogkpfypizg2gi";
    /**
     * 中邮提供
     */
    private static final String CHANNEL_CODE = "BRYXkHIAvr0m2U0T";
    /**
     * 中邮公钥
     */
    private static final String RSA_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhnl5uzpi0XZ2LIlc0C7kLe9JQ3v9els+K7WcsgRRrtszGCae6ko48NHf8xJvr8tWZqJf3P1YDzQe86pff9x7u6Xx4XqCbYIaH/DDsoXaZG+gvRtlRLDq5rXwncGFvx7lXAE/dOfyvK2OW7/bWbA9iZ5Za4mWKr3qwSHwOEptd93KPsvi5Xk0I8hcfik9BnTPds/P9nGNesHKk9E+Rt2GnPxa8h9aF8jFweie4MlaXzb3CkrM7vEaQZA+mwPTKvvYdCd20rIJmKwUMdG/k/KA80x3l6XwC5QCpa82AoBFVvUJyCXkxG9VRJ9q2nawLMiq6sES5Lq6sYIvu2O+b/z0DQIDAQAB";

    /**
     * 百融私钥
     */
    private static final String RSA_PRIVATE_KEY = "MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEAr1BcLtKFLRQ92jl9\n" +
            "mS4DyMyMOgYf0EZfmhE0R5WGs92XstydpUr8xVN5pwYkU3RNeKzNjXMR3K4aBkso\n" +
            "ren2mwIDAQABAkBc3ogGsbSkva1KVdwn8g1FKL472pStwynPtr9oEFisHJ024o7C\n" +
            "JSHq6tqThFmNNg2vH9R1anILVC5JgfeRsWSBAiEA2t8fKnx5D41rywHyoSCBxhbo\n" +
            "kBS8qmO5O9PzSgEVPEECIQDNDavQZ5wTE151HkRXjlt12G5b+b/PVe3FC+p6MYCr\n" +
            "2wIhAJ3/3/G9tW7iumPsXgiu/L/RHcWVErU2FCv6T3Cm43uBAiB37kYOh3r+oTZ+\n" +
            "86vvNeEChQrPGrz4DH8b38NNosRqPwIgW8lPWHq4OlsW1G/KojSjigkRUtu2BEk+\n" +
            "GJxW/aIYOrY=+gRbjjk24TOAth6Zpo7m/IXgxovkSnRfPEimnfbbkvyqQZps0F64y9i8SxsIQ3cwzE0EcJQTXuIz2yowPnzYBtbkqcLS0kyk/EK6mM7nf9JI8ONTvXq1s9xgbIoWMsaNsmCyb2Bvad+Y2gAJ0ady6hH4slYI6nLCLcsK9Od5BwRVFQcNaIbeT4/7FSZLpsZS/YlSdlG5CqztkNO2nS9jo6P0HM7CNpvvASrv5jJmXjRfC/Vzf26WEQbMIRCGV3kytrmpylBTN7AgMBAAECggEAJ5v8SSW+oZeo/35ZSZBcYLn7Ptqw1TYTMn4XnrW3xVXMvYxUiQQmi+GGxNHP/qlLbZgw4Fpy7Qq1eoaJNyLHNwpejBHzD61AufKMgtuHrn76PydtFkpklrKw4QBI2KtNbrX/5a4t4drkc/kFQGxnC0fbXnp+cj8/66HgTK6OBANJTxaX2wJo122TPIRVLvhwN7sbDhKvrqehpvM+B9RMIhDL+jyQ73QRxVoS4m4+ajEmggKnejcCsokCtwQ/rask2KU82fp0zZS2gz5BrUG9VWKcU3gLnCOXP6VkrhJnXQylX5oJz+wipbZFlfUI4Fni+1/C8SSRUeAuhyDHyz434QKBgQDnN6BrkFYRpAWWxI7BYLtP7yI8AmSmvJs2UAP2OO6EpfDeRmP/CvGZd4gTej+RKP337/idvDnLLphSlIp+DBUddUvzFibgPwqGTyByGDF6/jxp6hf9ygb0wf2FmCgiGacdAyOB04p4OEfSJWvksjqpWnnB4k8aXZCSZldCQwm4GQKBgQCsH4KUtLf7+6AEfWo7P9+2rS6mXC4dJ+kc1u8qroadmIl/Mb4NVYIw58rqTqohROjkA/lTc4pxbsD/l5SuX0wEE2pQgbutt1eYNqAFHzyG1OTUC7y/dt8XTc9rwZyksLXCf88LcrnYmvEw14KpcF1ZlrFg4LIUo3JLZOEBN1yKswKBgARKQ++6/d3V7USe4Qc1hEQ3a6sxYCy30ylu0qP+6m4Lpix5oeFZkqIGVcAGxKcs24l+Kl7C30+lza26k9dC4iFpy7726kG+6bMURMXZLRHbJcPRVCChXv+rmcigyh3X3AHtzPrbsfYJFUwQ5a+Ynv4Fb7zpNg6HLeeJfpT8KXIBAoGAdOIQ8pqmNd9xkpr4ALQnXw3Ll/0Q84ueqY7rariJgYuME2vb+4INnthI20QAFAePfweT0C+t28myFd8BgEgGft4QXAs9P4I5YYv2roO/vm/j1HsD+aDnbhPQvwQDM3MseqAAIW4O9iCBmQFAIX/EZIoIehkb1RgJDVm70e+eUDECgYEAzEUhGHnELGEVej1NUXjNtmJ3546c+JebJ05pKauG+b1bO2/1w1SNmdfTN0n/Wd+jLBHOv+nakJ1LSvPIqq8nob8YBrGTzWRrUMcjfqcAYoFwfR545e1z7XpkGIOrYtmp8GGPrzF05DmXAC7V58ZEsD5V9xPzBZTEtSwO25FJRBs=";

    private static final String QUERY_URL = "https://alissl.youcash.com/facadeuat/outmarketing-fileserver/file/out/query";

    private static final String DOWNLOAD_URL = "https://alissl.youcash.com/facadeuat/outmarketing-fileserver/file/out/download";
    private Object data;

    private String url;

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ZhongYouClientData(){

    }
    public ZhongYouClientData(LocalDate localDate) {
        String fileDate = localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        this.data = getFileNameListData(fileDate);
        this.url = QUERY_URL;
    }

    public ZhongYouClientData(String fileName) {
        this.data = fileDownLoadData(fileName);
        this.url = DOWNLOAD_URL;
    }

    /**
     * 中邮接口列表查询数据组装
     *
     * @return
     */
    private static Object getFileNameListData(String fileDate) {
        JSONObject data = new JSONObject();
        try {
            String format = SDF.format(new Date());
            JSONObject requestData = new JSONObject();
            requestData.put("fileType", "OUTMARKETING");
            requestData.put("startTime", fileDate);
            requestData.put("endTime", fileDate);
            String encryptDataStr = encryptData(requestData.toString());
            String signDataStr = signData(requestData.toString());
            data.put("channelCode", CHANNEL_CODE);
            data.put("version", "1.0");
            data.put("requestTime", format);
            data.put("sysSign", signDataStr);
//            data.put("requestData", requestData.toString());
            data.put("requestData", encryptDataStr);
        } catch (Exception e) {
            log.error("中邮文件名称请求数据加密异常:{}", e);
        }
        return data;
    }

    private static Object fileDownLoadData(String fileName) {
        JSONObject data = new JSONObject();
        try {
            String format = SDF.format(new Date());
            JSONObject requestData = new JSONObject();
            requestData.put("fileName", fileName);
            String encryptDataStr = encryptData(requestData.toString());
            String signDataStr = signData(requestData.toString());
            data.put("channelCode", CHANNEL_CODE);
            data.put("version", "1.0");
            data.put("requestTime", format);
            data.put("sysSign", signDataStr);
            data.put("requestData", encryptDataStr);
//            data.put("requestData", requestData.toString());
        } catch (Exception e) {
            log.error("中邮文件内容获取请求加密异常:{}", e);
        }

        return data;
    }

    /**
     * 对请求参数加签demo
     *
     * @param requestData
     * @return
     */
    private static String signData(String requestData) throws Exception {
        //aes加密
        byte[] encryptDataByte = encrypt(requestData, AES_KEY, AES_IV);

        //rsa加签
        return sign(encryptDataByte, RSA_PRIVATE_KEY);
    }

    /**
     * 数据签名
     *
     * @param content
     * @param privateKey
     * @return
     */
    private static String sign(byte[] content, String privateKey) {
        try {
            PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKey));
            KeyFactory keyf = KeyFactory.getInstance(KEY_ALGORITHM);
            PrivateKey priKey = keyf.generatePrivate(priPKCS8);
            Signature signature = Signature.getInstance(SIGN_ALGORITHMS);
            signature.initSign(priKey);
            signature.update(content);
            byte[] signed = signature.sign();
            return Base64.encodeBase64String(signed);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("数据签名异常", e);
        }
    }

    /**
     * 对请求参数加密demo
     *
     * @param requestData
     * @return
     * @throws Exception
     */
    private static String encryptData(String requestData) throws Exception {
        //aes加密
        byte[] encryptDataByte = encrypt(requestData, AES_KEY, AES_IV);

        //加密后的数据
        return Base64.encodeBase64String(encryptDataByte);
    }

    private static byte[] encrypt(String data, String encryptKey, String ivKeys) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] raw = ivKeys.getBytes("utf-8");
        IvParameterSpec iv = new IvParameterSpec(raw);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(encryptKey.getBytes("utf-8"), "AES"), iv);
        byte[] encryptedData = cipher.doFinal(data.getBytes("UTF-8"));
        return encryptedData;
    }

    private static String decrypt(String data, String encryptKey, String ivKeys) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] raw = ivKeys.getBytes("utf-8");
        IvParameterSpec iv = new IvParameterSpec(raw);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(encryptKey.getBytes("utf-8"), "AES"), iv);
        byte[] decryptedData = cipher.doFinal(Base64.decodeBase64(data));
        return new String(decryptedData, "UTF-8");
    }

    /**
     * 对响应参数解密demo
     *
     * @param responseData 响应的数据
     * @param sign         响应的签名
     * @return
     */
    public static String decryptData(String responseData, String sign) throws Exception {

        //对sign进行签名验证
        if (!doCheck(responseData, sign, RSA_PUBLIC_KEY)) {
//            log.error("报文验签失败");
            throw new Exception();
        }

        //使用AES解密后的数据
        String decryptData = null;
        try {
            decryptData = decrypt(responseData, AES_KEY, AES_IV);
        } catch (Exception e) {
//            log.error("请求参数解密失败:{}", e);
            throw e;
        }
        return decryptData;
    }
    /**
     * 验签
     *
     * @param content
     * @param sign
     * @param publicKey
     * @return
     */
    private static boolean doCheck(String content, String sign, String publicKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] encodedKey = Base64.decodeBase64(publicKey);
            PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));

            Signature signature = Signature
                    .getInstance(SIGN_ALGORITHMS);

            signature.initVerify(pubKey);
            signature.update(Base64.decodeBase64(content));

            boolean bverify = signature.verify(Base64.decodeBase64(sign));
            return bverify;

        } catch (Exception e) {
//            log.error("验签失败,内容为={}",e);
        }
        return false;
    }
}
