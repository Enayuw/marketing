package com.br.marketing.client.zhongan.utils;

import java.security.MessageDigest;

/**
 * ======================================================== MD5加密工具类
 *
 * @author AngleSuper.Wei
 * @date 2017/11/15 ========================================================
 */

public class Md5OfZanUtils {

    /** 16进制的字符数组 */
    private final static String[] hexDigits = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d",
            "e", "f"                       };

    /**
     * @param source 需要加密的原字符串
     * @param encoding 指定编码类型
     * @param uppercase 是否转为大写字符串
     * @return
     */
    public static String MD5Encode(String source, String encoding, boolean uppercase) {
        String result = null;
        try {
            result = source;
            // 获得MD5摘要对象
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            // 使用指定的字节数组更新摘要信息
            messageDigest.update(result.getBytes(encoding));
            // messageDigest.digest()获得16位长度
            result = byteArrayToHexString(messageDigest.digest());

        } catch (Exception e) {
        	System.out.println("MD5Encode error:"+ e);
        }
        return uppercase ? result.toUpperCase() : result;
    }

    /**
     * 转换字节数组为16进制字符串
     *
     * @param bytes
     *            字节数组
     * @return
     */
    private static String byteArrayToHexString(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte tem : bytes) {
            stringBuilder.append(byteToHexString(tem));
        }
        return stringBuilder.toString();
    }

    /**
     * 转换byte到16进制
     *
     * @param b 要转换的byte
     * @return 16进制对应的字符
     */
    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0) {
            n = 256 + n;
        }
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    /*public static void main(String[] args) {
        String signKey = new StringBuilder("Req-1234567890123").append("NYDDXJD").append("nyddxjd")
                .append("14700000004").append("nyddxjd").append("0").append("1.0.0").toString();

        System.out.println(utils.Md5Utils.MD5Encode(signKey, "utf-8", true));
    }*/



    /**
     * 对字符串md5加密(小写+字母)
     *
     * @param str 传入要加密的字符串
     * @return MD5加密后的字符串
     */
    public static String getMD5(String str) {
        if (str == null) {
            return null;
        }
        try {
            StringBuilder md5StrBuff = new StringBuilder("");
            // 生成一个MD5加密计算摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.reset();
            md.update(str.getBytes("UTF-8"));
            byte[] byteArray = md.digest();
            for (int i = 0; i < byteArray.length; i++) {
                if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
                    md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
                else
                    md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
            }
            return md5StrBuff.toString();
        } catch (Exception e) {
//            log.warn("diandianChannelAccess.ZhongAnUtil.getMD5 error:{}", e);
            return null;
        }
    }
}
