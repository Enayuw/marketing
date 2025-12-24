package com.br.marketing.common.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 字符串工具类
 * Created by jack on 2016/12/4.
 */
public class StringUtils {

    private static final String NumberRegex = "[+-]?(?:\\d+\\.\\d*|\\.\\d+|\\d+)(?:[eE][+-]?\\d+)?";

    //正则表达式检测任意汉字
    private static final Pattern CHINESE_CHAR_PATTERN = Pattern.compile("[\u4e00-\u9fa5]");

    public static boolean isEmpty(Object obj) {
        return (obj == null || obj.toString().length() == 0);
    }

    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }

    public static boolean isNull(Object obj) {
        return (obj == null);
    }

    public static boolean isNotNull(Object obj) {
        return !isNull(obj);
    }

    public static long time() {
        return System.currentTimeMillis();
    }

    /**
     * md5
     *
     * @param value
     * @return
     */
    public static String md5(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        String md5 = DigestUtils.md5Hex(value);
        return md5;
    }

    public static boolean isNotBlank(String str) {
        boolean blank = !isBlank(str);
        if (blank && str.equalsIgnoreCase("null")) {
            blank = false;
        }
        return blank;
    }

    public static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(str.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }

    public static boolean contains(String str, String searchStr) {
        if (str == null || searchStr == null) {
            return false;
        }
        return str.indexOf(searchStr) >= 0;
    }

    public static String[] split(String str, String separatorChars) {
        return splitWorker(str, separatorChars, -1, false);
    }

    private static String[] splitWorker(String str, String separatorChars, int max, boolean preserveAllTokens) {
        // Performance tuned for 2.0 (JDK1.4)
        // Direct code is quicker than StringTokenizer.
        // Also, StringTokenizer uses isSpace() not isWhitespace()

        if (str == null) {
            return new String[0];
        }
        int len = str.length();
        if (len == 0) {
            return new String[0];
        }
        List list = new ArrayList();
        int sizePlus1 = 1;
        int i = 0, start = 0;
        boolean match = false;
        boolean lastMatch = false;
        if (separatorChars == null) {
            // Null separator means use whitespace
            while (i < len) {
                if (Character.isWhitespace(str.charAt(i))) {
                    if (match || preserveAllTokens) {
                        lastMatch = true;
                        if (sizePlus1++ == max) {
                            i = len;
                            lastMatch = false;
                        }
                        list.add(str.substring(start, i));
                        match = false;
                    }
                    start = ++i;
                    continue;
                }
                lastMatch = false;
                match = true;
                i++;
            }
        } else if (separatorChars.length() == 1) {
            // Optimise 1 character case
            char sep = separatorChars.charAt(0);
            while (i < len) {
                if (str.charAt(i) == sep) {
                    if (match || preserveAllTokens) {
                        lastMatch = true;
                        if (sizePlus1++ == max) {
                            i = len;
                            lastMatch = false;
                        }
                        list.add(str.substring(start, i));
                        match = false;
                    }
                    start = ++i;
                    continue;
                }
                lastMatch = false;
                match = true;
                i++;
            }
        } else {
            // standard case
            while (i < len) {
                if (separatorChars.indexOf(str.charAt(i)) >= 0) {
                    if (match || preserveAllTokens) {
                        lastMatch = true;
                        if (sizePlus1++ == max) {
                            i = len;
                            lastMatch = false;
                        }
                        list.add(str.substring(start, i));
                        match = false;
                    }
                    start = ++i;
                    continue;
                }
                lastMatch = false;
                match = true;
                i++;
            }
        }
        if (match || (preserveAllTokens && lastMatch)) {
            list.add(str.substring(start, i));
        }
        String[] strArray = new String[list.size()];
        return (String[]) list.toArray(strArray);
    }

    private static Pattern humpPattern = Pattern.compile("[A-Z]");

    public static String humpToLine2(String str) {
        Matcher matcher = humpPattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String getGenderByIdCard(String IdCard) {

        if (org.apache.commons.lang3.StringUtils.isNotBlank(IdCard)) {
            int gender;
            int idCardLen = 18;
            int length = IdCard.length();
            if (length == idCardLen) {
                gender = Integer.parseInt(IdCard.substring(16, 17));
            } else {
                gender = Integer.parseInt(IdCard.substring(length - 1, length));
            }
            return (gender % 2 == 0) ? "女" : "男";
        }
        return null;
    }

    /**
     * 判断字符串数组中是否有重复
     * @param array
     * @return
     */
    public static boolean hasDuplicate(String[] array) {
        HashSet<String> set = new HashSet<>();
        for (String s : array) {
            if (!set.add(s)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNumeric(String str) {
        return str != null && str.matches(NumberRegex);
    }

    public static boolean isJson(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        try {
            str = str.trim();
            if (str.startsWith("{") && str.endsWith("}")) {
                JSONObject.parseObject(str);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 将字符串按split分割，并返回至少limit个元素
     * @param input
     * @param split
     * @param limit
     * @return
     */
    public static List<String> splitAndLimit(String input, String split, int limit) {
        if (input == null || input.trim().isEmpty()) {
            return Collections.nCopies(limit, "");
        }
        //保留空项（如 "a,,c"）
        String[] parts = input.trim().split(split, -1);
        List<String> result = new ArrayList<>(parts.length);
        for (String p : parts) {
            result.add(p == null ? "" : p.trim());
        }
        if (result.size() <= limit) {
            while (result.size() < limit) {
                result.add("");
            }
        }
        //大于limit时直接返回全部
        return result;
    }

    /**
     * 检查单个字符串是否包含汉字
     * @param str 输入字符串
     * @return 包含汉字返回true，否则false
     */
    public static boolean containsChinese(String str) {
        if (str == null || str.isEmpty()) return false;
        return CHINESE_CHAR_PATTERN.matcher(str).find();
    }

    public static List<Long> StrsConvertLongs(String strs) {
        if(isBlank(strs)){
            return Collections.emptyList();
        }
        List<Long> longs = Arrays.stream(strs.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toList());
        return longs;
    }

    public static String LongsConvertStr(List<Long> longs) {
        if (longs == null || longs.isEmpty()) {
            return "";
        }
        return longs.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    public static String join(List<Long> longs) {
        StringJoiner joiner = new StringJoiner(",", "(", ")");
        for (Long num : longs) {
            joiner.add(num.toString());
        }
        return joiner.toString();
    }

}
