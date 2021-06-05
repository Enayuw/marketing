package com.br.marketing.es.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;

/**
 * @author Wang Weiwei <email>weiwei02@vip.qq.com / weiwei.wang@100credit.com</email>
 * @version 1.0
 * @sine 2017/11/17
 */
public class BrCipherMakerOld {
    //	static final char sp = 31;  因放入json，会默认编码，\u001F 所以换成特殊 Α
    private static final String SP = "Α";
    private static final byte[] CHUNK_SEPARATOR = {'\r', '\n'};
    private static final String[] KEYS = new String[]{"c849a06defd23bac", "c9e50bfe3ccbe05a", "025371b9fef1098f", "0b74d38da0a8789d",
            "89b66e0f0917d044", "afb283dbd5a1c950", "b898901aded8d6a9", "385baab99a0038c6", "17360f5c5de62d1e", "9f057500d30f94fa"};

    //私有变量
    private static BrCipherMakerOld instance = new BrCipherMakerOld();

    //私有构造函数 不能被实例化
    private BrCipherMakerOld() {
    }

    public static BrCipherMakerOld getInstance() {
        return instance;
    }



    /**
     * 解密
     *
     * @param text
     * @return 异常返回 null ，如果不是此工具加密的，原样返回
     */
    public String decode(String text) {
        if (StringUtils.isBlank(text) || !StringUtils.contains(text, SP)) {
            return text;
        }
        //先解析索引位置
        String[] txts = StringUtils.split(text, SP);
        if (txts.length > 1) {
            String src = txts[0];
            String srcEnd = txts[1];
            int idx = Integer.parseInt(StringUtils.substring(srcEnd, 0, 1));
            src = new StringBuilder().append(src).append(StringUtils.substring(srcEnd, 1)).toString();
            String key = KEYS[idx];
            return decode(src, key);
        }
        return null;
    }

    /**
     * 解密
     *
     * @param text
     * @param key
     * @return
     */
    private String decode(String text, String key) {
        text = new String(new Base64(0, CHUNK_SEPARATOR, true).decode(text), StandardCharsets.UTF_8);
        int cyc = key.length();
        char[] keyChar = key.toLowerCase().toCharArray();
        int[] off = new int[cyc];
        for (int i = 0; i < cyc; i++) {
            off[i] = keyChar[i] - 'a';
        }
        int len = text.length();
        char[] orgChar = text.toCharArray();
        int j = 0;
        for (int i = 0; i < len; i++) {
            if (orgChar[i] == ' ') {
                continue;
            }
            orgChar[i] -= off[j % cyc];
            j++;
        }
        return new String(orgChar);
    }

}
