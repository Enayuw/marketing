package com.br.marketing.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 表头与建表字段名转换
 * - 归一化 header_schema（trim、统一逗号）用于 MD5
 * - header_schema 转 column_schema_en：header_1, header_2, ...
 */
public final class HeaderToColumnUtil {

    private HeaderToColumnUtil() {
    }

    /**
     * 归一化表头：trim 每段，统一逗号分隔（无空格）
     */
    public static String normalizeHeaderSchema(String headerSchema) {
        if (headerSchema == null || headerSchema.isEmpty()) {
            return "";
        }
        return Arrays.stream(headerSchema.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(","));
    }

    /**
     * MD5(归一化 header_schema)，小写十六进制
     */
    public static String headerSignMd5(String headerSchema) {
        String normalized = normalizeHeaderSchema(headerSchema);
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
     * 表头转建表用英文字段名，逗号分隔：header_1, header_2, ...
     * 列数 = 归一化后 split 长度
     */
    public static String headerSchemaToColumnSchemaEn(String headerSchema) {
        if (headerSchema == null || headerSchema.isEmpty()) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        int i = 1;
        for (String s : headerSchema.split(",")) {
            String t = s.trim();
            if (!t.isEmpty()) {
                parts.add("header_" + i++);
            }
        }
        return String.join(",", parts);
    }

    /**
     * 返回归一化后的表头列数
     */
    public static int headerColumnCount(String headerSchema) {
        String n = normalizeHeaderSchema(headerSchema);
        return n.isEmpty() ? 0 : n.split(",").length;
    }
}
