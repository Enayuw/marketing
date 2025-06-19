package com.br.marketing.util;

import java.util.Random;

public class RandomUtil {
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz123456789";
    private static final Random RANDOM = new Random();
    private static final String NUMBERS = "0123456789";
    private static final String LETTERS = "abcdefghijklmnopqrstuvwxyz";

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

    /**
     * 随机生成由数字、字母组成的N位验证码，乱序且至少存在数字和字母
     * @param length
     * @return
     */
    public static String generateCode(int length) {
        if (length < 2) {
            throw new IllegalArgumentException("验证码长度必须大于等于2");
        }
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        // 确保至少包含一个数字
        code.append(NUMBERS.charAt(random.nextInt(NUMBERS.length())));
        // 确保至少包含一个字母
        code.append(LETTERS.charAt(random.nextInt(LETTERS.length())));
        // 生成剩余字符
        String allChars = NUMBERS + LETTERS;
        for (int i = 2; i < length; i++) {
            code.append(allChars.charAt(random.nextInt(allChars.length())));
        }
        // 打乱顺序
        char[] codeArray = code.toString().toCharArray();
        for (int i = codeArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = codeArray[i];
            codeArray[i] = codeArray[j];
            codeArray[j] = temp;
        }
        return new String(codeArray);
    }
}
