package com.br.marketing.util.xyf;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    /** 用于 unicode 转中文，避免重复编译 (S4248) */
    private static final Pattern UNICODE_ESCAPE = Pattern.compile("\\\\u(\\p{XDigit}{4})");

    /**
     * 判断字符是否为中文（CJK 统一汉字范围 0x4e00～0x9fa5）
     *
     * @param c
     * @return
     */
    public static boolean isChineseChar(char c) {
        return c >= 0x4e00 && c <= 0x9fa5;
    }

    /**
     * 中文转unicode
     *
     * @param s
     * @return
     */
    public static String toUnicode(String s) {
        String as[] = new String[s.length()];
        String s1 = "";
        for (int i = 0; i < s.length(); i++) {
            if (!isChineseChar(s.charAt(i))) {
                s1 += s.charAt(i);
            } else {
                as[i] = Integer.toHexString(s.charAt(i) & 0xffff);
                s1 += "\\u" + as[i];
            }
        }
        return s1;
    }

    /**
     * unicode转中文
     * @param s
     * @return
     */
    public static String toChinese(String s) {
        Matcher matcher = UNICODE_ESCAPE.matcher(s);
        char ch;
        while (matcher.find()) {
            ch = (char) Integer.parseUnsignedInt(matcher.group(1), 16);
            s = s.replace(matcher.group(), ch + "");
        }
        return s;
    }
}
