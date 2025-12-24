package com.br.marketing.client.taikang.util;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.KeyUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import cn.hutool.crypto.symmetric.SM4;
import com.alibaba.fastjson2.JSON;
import com.br.marketing.api.customer.upload.service.weiju.util.AESUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

/**
 * @author itw_xuzw01
 * @date 2025/7/24 17:17
 *
 * 算法说明 :
 * 对称算法 : SM4, Mode =   ECB  Padding =  PKCS5Padding
 *
 *  非对称算法：
 *  sm2.mode :设置SM2模式，旧版是C1C2C3，新版本是C1C3C2
 *  默认曲线：
 *  SM2_CURVE_NAME = "sm2p256v1";
 *
 *  C# -> Java 需要特殊处理及其分析的...
 *  C# 也是这个库，
 *  C#.NET BC BouncyCastle 国密 SM2 解密遇到 Invalid point encoding 77
 *  https://www.cnblogs.com/runliuv/p/16544671.html
 */
@Slf4j
@Component
public class SimpleDataPackToolsV2 {

    public static final String SIGN_CONTENT_TS = "content={}&ts={}";


    /**
     * 客户端打包
     * @param localPrivateKey      本端私钥
     * @param remotePublicKey      远端公钥 泰康
     * @param data                 业务数据
     * @return                     打包好数据，待发送
     */
    public <T> ChannelRequest clientPacking(
            final String remotePublicKey,
            final String localPrivateKey,
            final T data) {

        // 获取当前时间戳
        long ts = System.currentTimeMillis();

        //生成对称密钥key
        byte[] keyBytes = SimpleDataPackToolsV2.getKey(SymmetricCryEnum.SM4);
        //转成base64不会有问题，如果采用new String("utf-8"),再转回来字节数会变大
        String key = SimpleDataPackToolsV2.bytesToBase64(keyBytes);

        //对key使用非对称加密
        String cryKey = SimpleDataPackToolsV2.cry(remotePublicKey, key);

        // JSON序列化
        String jsonData = JSON.toJSONString(data);

        //对内容进行对称加密
        byte[] byteCryContent = SimpleDataPackToolsV2.cry(jsonData.getBytes(StandardCharsets.UTF_8), keyBytes);
        String strCryContent = SimpleDataPackToolsV2.bytesToBase64(byteCryContent);

        // 获取签名
        String sign = SimpleDataPackToolsV2.sign(localPrivateKey,
                StrUtil.format(SIGN_CONTENT_TS, strCryContent, String.valueOf(ts)));

        // 输出那四个字段都有了...
        return ChannelRequest.of(cryKey, strCryContent, ts, sign);

    }


    /**
     * 客户端解析
     *
     * @param serverResponse 业务数据
     * @param tClass 业务数据
     * @return 业务数据
     */
    public <T> T clientParser(
            final String localPrivateKey,
            final Result<ChannelResponse> serverResponse,
            final Class<T> tClass) {

        // 解密key
        String sourceKey = SimpleDataPackToolsV2.deCry(localPrivateKey, serverResponse.getData().getKey());

        // 解密明文
        String sourceBody = SimpleDataPackToolsV2.deCry(serverResponse.getData().getContent(), SimpleDataPackToolsV2.base64ToBytes(sourceKey));

        // 返回数据
        return JSON.parseObject(sourceBody, tClass);

    }


    /**
     * 获取对称加密密钥
     * @param symmetricCryEnum 加密算法
     * @return 加密密钥
     */
    private static byte[] getKey(SymmetricCryEnum symmetricCryEnum) {
        //随机数作为种子
        String key = UUID.randomUUID().toString();

        //SM4密钥长度也为128位(可以与AES采用项目算法产生密钥), SecureRandom当种子一样时产生一样的序列
        SecretKey secretKey = KeyUtil.generateKey(symmetricCryEnum.name(), 128);
        return secretKey.getEncoded();
    }


    public static String bytesToBase64(byte[] bytes) {
        byte[] encodedBytes = Base64.getEncoder().encode(bytes);
        return new String(encodedBytes, StandardCharsets.UTF_8);
    }


    /**
     * 对称密钥加密
     * @param publicKey
     * @param content
     * @return
     */
    public static String cry(String publicKey, String content) {
        SM2 sm2 = SmUtil.sm2(null, publicKey);
        return sm2.encryptBcd(content, KeyType.PublicKey);
    }

    public static byte[] cry(byte[] content, byte[] keyBytes) {
        SM4 sm4 = new SM4(keyBytes);
        return sm4.encrypt(content);
    }

    public static byte[] crySpec(byte[] content, byte[] keyBytes) {
        SM4 sm4 = new SM4(Mode.CBC, Padding.PKCS5Padding, keyBytes);
        return sm4.encrypt(content);
    }


    public static String sign(String privateKey, String content) {
        byte[] data = content.getBytes(StandardCharsets.UTF_8);
        SM2 sm2 = SmUtil.sm2(privateKey, null);
        return sm2.signHex(HexUtil.encodeHexStr(data));
    }

    public static String deCry(String privateKey, String content) {
        SM2 sm2 = SmUtil.sm2(privateKey, null);
        return StrUtil.utf8Str(sm2.decryptFromBcd(content, KeyType.PrivateKey));
    }

    public static String deCry(String content, byte[] keyBytes) {
        SM4 sm4 = new SM4(keyBytes);
        return sm4.decryptStr(content);
    }

    public static byte[] base64ToBytes(String base64Str) {
        byte[] bytes = base64Str.getBytes(StandardCharsets.UTF_8);
        return Base64.getDecoder().decode(bytes);
    }

    private static enum SymmetricCryEnum {
        /**
         * 国密SM4
         */
        SM4
    }


    public static void main(String[] args) {
        String s = AESUtil.encryptAES("nX7zCFT1HaUllNbM", "15122334455");
//        System.out.println(s);
//
//
//        JSONObject jsonObject = new JSONObject();
//        HttpProxyClient httpProxyClient = new HttpProxyClient();
//        try {
//            jsonObject.put("applicantPhone",s);
//            jsonObject.put("eventId","channel_browse_interruption");
//            jsonObject.put("channelCode","RongDa");
//            jsonObject.put("applicantName","");
//            jsonObject.put("browseDate","2025-11-20 12:04:00");
//            SimpleDataPackToolsV2 simpleDataPackToolsV2 = new SimpleDataPackToolsV2();
//            ChannelRequest channelRequest = simpleDataPackToolsV2.clientPacking(
//             "MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEs9zja+l2Fd9B664O1q1Oy4fsiEoLhNiBS9zhKPuUI075vZ/dADBdE2zMbCP5oVDFBOter9IH/C1iX8C2HFrl0w==",
//             "MIGTAgEAMBMGByqGSM49AgEGCCqBHM9VAYItBHkwdwIBAQQgaOxhL7Oj8kLi8zpgXaGJIyfBOjxzq" +
//             "Vf68ITblLXsYIOgCgYIKoEcz1UBgi2hRANCAATjyRdmnS4msSglH4Vv9QdLyC7Bl1Em8myRlzVqKmU9+pSYIPAqv8F4sIn9eYz9XHObW1aIcH4uqHeK6TYtSoQj\n" +
//                            "\n",
//                    jsonObject);
//            String send = httpProxyClient.send(channelRequest.toString(), "http://49.233.178.183/e/channel/dataReplay", false);
//            System.out.println(send);
//
//        } catch (JSONException e) {
//            throw new RuntimeException(e);
//        }

//        //生成对称密钥key
//        byte[] keyBytes = SimpleDataPackToolsV2.getKey(SymmetricCryEnum.SM4);
//        //转成base64不会有问题，如果采用new String("utf-8"),再转回来字节数会变大
//        String key = SimpleDataPackToolsV2.bytesToBase64(keyBytes);
//
//        byte[] des11 = SimpleDataPackToolsV2.cry("abc11".getBytes(StandardCharsets.UTF_8), keyBytes);
//        System.out.println(bytesToBase64(des11));
//
//        //SM4 sm4 = new SM4(Mode.ECB, Padding.ZeroPadding, keyBytes);
//        //System.out.println(bytesToBase64(sm4.encrypt("abc11".getBytes(StandardCharsets.UTF_8))));
//
//        SM4 sm4 = new SM4(Mode.ECB, Padding.PKCS5Padding, keyBytes);
//        System.out.println(bytesToBase64(sm4.encrypt("abc11".getBytes(StandardCharsets.UTF_8))));
//
//
//        System.out.println("sfd");
    }
}
