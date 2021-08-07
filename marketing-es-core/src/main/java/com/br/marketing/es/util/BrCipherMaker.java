package com.br.marketing.es.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * <p>Description: 加密解密工具类-位移</p>
 *
 * @author Wang Weiwei <email>weiwei02@vip.qq.com / weiwei.wang@100credit.com</email>
 * @version 1.0
 * @sine 2017/11/17
 */
public class BrCipherMaker {
    private static final Logger log = LoggerFactory.getLogger(BrCipherMaker.class);
    //	static final char sp = 31;  因放入json，会默认编码，\u001F 所以换成特殊 Α 升級版本換成 B
    private static final String SP = "Β";
    private static final String SP2 = "Α";
    private static final byte[] CHUNK_SEPARATOR = {'\r', '\n'};
    private static final String[] KEYS = new String[]{"c849a06defd23bac", "c9e50bfe3ccbe05a", "025371b9fef1098f", "0b74d38da0a8789d",
            "89b66e0f0917d044", "afb283dbd5a1c950", "b898901aded8d6a9", "385baab99a0038c6", "17360f5c5de62d1e", "9f057500d30f94fa"};
    private static final int KEYS_LEN = KEYS.length;

    //私有变量
    private static BrCipherMaker instance = new BrCipherMaker();

    //私有构造函数 不能被实例化
    private BrCipherMaker() {
    }

    public static BrCipherMaker getInstance() {
        return instance;
    }



    public int indexFor(String src) {
        int idx = src.hashCode() % KEYS_LEN;
        return Math.abs(idx);
    }

    /**
     * 加密
     *
     * @param orginal
     * @return
     */
    public String encode(String orginal) {
        if (StringUtils.isBlank(orginal)) {
            return orginal;
        }
        int idx = indexFor(orginal);
        String mw = encode(orginal, KEYS[idx]);
        //随机埋入索引
        if (StringUtils.isNotBlank(mw)) {
            int lt = mw.length();
            int s = Math.abs(mw.hashCode() % lt);
            if (s == 0) {
                s = 2;
            }
            mw = StringUtils.substring(mw, 0, s) + SP + idx + StringUtils.substring(mw, s);
        }
        return mw;
    }

    /**
     * 解密
     *
     * @param text
     * @return 异常返回 null ，如果不是此工具加密的，原样返回
     */
    public String decode(String text) {
        if (StringUtils.isBlank(text) || !(StringUtils.contains(text, SP) || StringUtils.contains(text, SP2))) {
            log.info("解密信息失败：{} SP：{} SP2：{}", text, SP, SP2);
            return text;
        } else if (StringUtils.contains(text, SP2)) {
            String decode = BrCipherMakerOld.getInstance().decode(text);
            log.info("解密信息成功：{} 解密信息：{} SP2：{}", text, decode, SP2);
            return decode;
        }
        //先解析索引位置
        String[] txts = StringUtils.split(text, SP);
        if (txts.length > 1) {
            String src = txts[0];
            String srcEnd = txts[1];
            int idx = Integer.parseInt(StringUtils.substring(srcEnd, 0, 1));
            src = new StringBuilder().append(src).append(StringUtils.substring(srcEnd, 1)).toString();
            String decode = decode(src, KEYS[idx]);
            log.info("解密信息成功：{} 解密信息：{} SP：{}", text, decode, SP);
            return decode;
        }
        return null;
    }

    private String encode(String orginal, String key) {
        int cyc = key.length();
        char[] keyChar = key.toLowerCase().toCharArray();
        char[] off = Arrays.copyOf(keyChar, cyc);
        int len = orginal.length();
        char[] orgChar = orginal.toCharArray();
        int j = 0;
        for (int i = 0; i < len; i++) {
            orgChar[i] = (char) (orgChar[i] ^ off[j % cyc]);
            j++;
        }
        return Base64.encodeBase64URLSafeString(new String(orgChar).getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String text, String key) {
        text = new String(new Base64(0, CHUNK_SEPARATOR, true).decode(text), StandardCharsets.UTF_8);
        int cyc = key.length();
        char[] keyChar = key.toLowerCase().toCharArray();
        char[] off = Arrays.copyOf(keyChar, cyc);
        int len = text.length();
        char[] orgChar = text.toCharArray();
        int j = 0;
        for (int i = 0; i < len; i++) {
            orgChar[i] = (char) (orgChar[i] ^ off[j % cyc]);
            j++;
        }
        return new String(orgChar);
    }

}
