package com.br.marketing.util;

import org.apache.commons.lang3.StringUtils;

/**
 * 文件上传清洗：按配置分隔符拆分表头/数据行（字面量分隔，避免 {@link String#split(String)} 正则误解析）。
 */
public final class DataCleanDelimiterUtils {

    private static final String DEFAULT_DELIMITER = ",";

    private DataCleanDelimiterUtils() {
    }

    /**
     * 配置库或入参中的分隔符；null/空/空白则返回逗号。
     */
    public static String resolveDelimiter(String raw) {
        if (raw == null || StringUtils.isBlank(raw)) {
            return DEFAULT_DELIMITER;
        }
        return raw.trim();
    }

    /**
     * 按字面量分隔符拆分一行，保留尾部空段（与 {@code split(sep, -1)} 语义对齐）。
     */
    public static String[] splitLine(String line, String delimiterConfig) {
        String sep = resolveDelimiter(delimiterConfig);
        if (line == null) {
            return new String[0];
        }
        return StringUtils.splitByWholeSeparatorPreserveAllTokens(line, sep);
    }
}
