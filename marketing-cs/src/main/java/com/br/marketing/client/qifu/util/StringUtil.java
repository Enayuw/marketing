package com.br.marketing.client.qifu.util;

/**
 * @Author: wangshaowen
 * @Date: 2021/11/22 14:19
 * @Description:
 */
public class StringUtil {

    /**
     * byte数组转16进制字符串
     *
     * @param bs
     * @return
     */
    public static final String bytes2HexStr(byte[] bs) {
        StringBuilder buffer = new StringBuilder();
        String tmp;
        for (int i = 0; i < bs.length; i++) {
            buffer.append((tmp = Integer.toHexString(bs[i] & 0xff)).length() == 1 ? "0" + tmp : tmp);
        }
        return buffer.toString().toUpperCase();
    }

    /**
     * 16进制字符串转byte数组
     *
     * @param data
     * @return
     */
    public static final byte[] hexStr2Bytes(String data) {
        String str = "0123456789ABCDEF";
        char[] hexs = data.toCharArray();
        byte[] bs = new byte[data.length() / 2];
        int n;
        for (int i = 0; i < bs.length; i++) {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bs[i] = (byte) (n & 0xff);
        }
        return bs;
    }
}