package com.br.marketing.util;

import java.util.Random;

public class RandomUtil {
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz123456789"; // 随机数的字符集
    private static final Random RANDOM = new Random();

    /**
     * 随机生成由数字、字母组成的N位验证码
     *
     * @return 返回一个字符串
     */
    public static String getCode(int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }
}
