package com.br.marketing.utils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import com.br.marketing.util.DataCleanDelimiterUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 表头与建表字段名转换
 * - 归一化 header_schema（trim、统一逗号）用于 MD5
 * - header_schema 转 column_schema_en：中文转拼音（如 证件号码 -> zheng_jian_hao_ma），英文原值规范化（小写、非法字符替为下划线）
 */
public final class HeaderToColumnUtil {

    private static final HanyuPinyinOutputFormat PINYIN_FORMAT = new HanyuPinyinOutputFormat();

    private static final Pattern MULTIPLE_UNDERSCORE = Pattern.compile("_+");
    private static final Pattern LEADING_TRAILING_UNDERSCORE = Pattern.compile("^_|_$");

    static {
        PINYIN_FORMAT.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        PINYIN_FORMAT.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        PINYIN_FORMAT.setVCharType(HanyuPinyinVCharType.WITH_V);
    }

    private HeaderToColumnUtil() {
    }

    /**
     * 归一化表头：trim 每段，统一为逗号分隔的规范串（内部存储与 MD5 均用逗号拼接）
     *
     * @param fieldDelimiterConfig 与数据行一致的分隔符，见 {@link DataCleanDelimiterUtils#resolveDelimiter}；null/空 视为逗号
     */
    public static String normalizeHeaderSchema(String headerSchema, String fieldDelimiterConfig) {
        if (headerSchema == null || headerSchema.isEmpty()) {
            return "";
        }
        return Arrays.stream(DataCleanDelimiterUtils.splitLine(headerSchema, fieldDelimiterConfig))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(","));
    }

    /**
     * 归一化表头：分隔符默认逗号（兼容旧调用）
     */
    public static String normalizeHeaderSchema(String headerSchema) {
        return normalizeHeaderSchema(headerSchema, null);
    }

    /**
     * MD5(归一化 header_schema)，小写十六进制
     *
     * @param fieldDelimiterConfig 表头原始串的分隔，与 {@link #normalizeHeaderSchema(String, String)} 一致；null 表示逗号
     */
    public static String headerSignMd5(String headerSchema, String fieldDelimiterConfig) {
        String normalized = normalizeHeaderSchema(headerSchema, fieldDelimiterConfig);
        if (normalized.isEmpty()) {
            return "";
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(normalized.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(32);
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not available", e);
        }
    }

    /**
     * MD5(归一化 header_schema)，分隔符默认逗号
     */
    public static String headerSignMd5(String headerSchema) {
        return headerSignMd5(headerSchema, null);
    }

    /**
     * 判断字符串是否包含中文字符
     */
    private static boolean containsChinese(CharSequence s) {
        if (s == null || s.length() == 0) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            if (Character.UnicodeBlock.of(s.charAt(i)) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                    || Character.UnicodeBlock.of(s.charAt(i)) == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                    || Character.UnicodeBlock.of(s.charAt(i)) == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION) {
                return true;
            }
        }
        return false;
    }

    /**
     * 中文转拼音用下划线连接，中间夹杂的英文/数字原样小写连写，如 证件号码 -> zheng_jian_hao_ma，姓名ABC -> xing_ming_abc
     */
    private static String toPinyinColumn(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (containsChinese(String.valueOf(c))) {
                try {
                    String[] pinyins = PinyinHelper.toHanyuPinyinStringArray(c, PINYIN_FORMAT);
                    if (pinyins != null && pinyins.length > 0) {
                        if (sb.length() > 0) {
                            sb.append('_');
                        }
                        sb.append(pinyins[0]);
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    // 忽略无法转换的字符
                }
                i++;
            } else {
                StringBuilder enBlock = new StringBuilder();
                while (i < s.length() && !containsChinese(String.valueOf(s.charAt(i)))) {
                    char ch = s.charAt(i);
                    if (Character.isLetterOrDigit(ch)) {
                        enBlock.append(Character.toLowerCase(ch));
                    }
                    i++;
                }
                if (enBlock.length() > 0) {
                    if (sb.length() > 0) {
                        sb.append('_');
                    }
                    sb.append(enBlock);
                }
            }
        }
        String temp = MULTIPLE_UNDERSCORE.matcher(sb.toString()).replaceAll("_");
        return LEADING_TRAILING_UNDERSCORE.matcher(temp).replaceAll("");
    }

    /**
     * 英文列名归一化时，可作为连续下划线合并触发的分隔字符
     */
    private static boolean isEnglishColumnSeparator(char c) {
        return c == ' ' || c == '\t' || c == '-' || c == '.' || c == '_';
    }

    /**
     * 英文/数字表头规范化为列名：小写，非字母数字替为下划线，多下划线合并，首尾去下划线；若为空则返回 null 供 fallback
     */
    private static String normalizeEnglishColumn(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                sb.append(Character.toLowerCase(c));
            } else if (isEnglishColumnSeparator(c)
                    && sb.length() > 0 && sb.charAt(sb.length() - 1) != '_') {
                sb.append('_');
            }
        }
        String temp = MULTIPLE_UNDERSCORE.matcher(sb.toString()).replaceAll("_");
        String r = LEADING_TRAILING_UNDERSCORE.matcher(temp).replaceAll("");
        return r.isEmpty() ? "" : r;
    }

    /**
     * 表头转建表用英文字段名，逗号分隔。
     * 中文表头转拼音加下划线（如 证件号码 -> zheng_jian_hao_ma），英文表头原值规范化（小写、非法字符替为下划线）。
     * 若转换后为空或重复，则使用 header_1, header_2 等兜底。
     *
     * @param fieldDelimiterConfig 原始表头串的分隔符，与数据行、{@link #normalizeHeaderSchema(String, String)} 一致；null 为逗号
     */
    public static String headerSchemaToColumnSchemaEn(String headerSchema, String fieldDelimiterConfig) {
        if (headerSchema == null || headerSchema.isEmpty()) {
            return "";
        }
        String normalized = normalizeHeaderSchema(headerSchema, fieldDelimiterConfig);
        return headerSchemaToColumnSchemaEnFromNormalized(normalized);
    }

    /**
     * 入参已为逗号分隔的归一化表头（例如已调用 {@link #normalizeHeaderSchema(String, String)}）
     */
    public static String headerSchemaToColumnSchemaEn(String headerSchema) {
        return headerSchemaToColumnSchemaEnFromNormalized(headerSchema);
    }

    private static String headerSchemaToColumnSchemaEnFromNormalized(String normalizedCommaSeparated) {
        if (normalizedCommaSeparated == null || normalizedCommaSeparated.isEmpty()) {
            return "";
        }
        List<String> rawParts = Arrays.stream(normalizedCommaSeparated.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        if (rawParts.isEmpty()) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        Map<String, Integer> nameCount = new HashMap<>();
        int fallbackIndex = 1;
        for (String s : rawParts) {
            String col;
            if (containsChinese(s)) {
                col = toPinyinColumn(s);
            } else {
                col = normalizeEnglishColumn(s);
            }
            if (col == null || col.isEmpty()) {
                col = "header_" + fallbackIndex++;
            } else {
                int cnt = nameCount.merge(col, 1, (a, b) -> a + b);
                if (cnt > 1) {
                    col = col + "_" + cnt;
                }
            }
            parts.add(col);
        }
        return String.join(",", parts);
    }

    /**
     * 返回归一化后的表头列数
     */
    public static int headerColumnCount(String headerSchema, String fieldDelimiterConfig) {
        String n = normalizeHeaderSchema(headerSchema, fieldDelimiterConfig);
        return n.isEmpty() ? 0 : n.split(",").length;
    }

    public static int headerColumnCount(String headerSchema) {
        return headerColumnCount(headerSchema, null);
    }
}
