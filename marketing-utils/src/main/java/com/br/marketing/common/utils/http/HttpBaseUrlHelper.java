package com.br.marketing.common.utils.http;

import java.util.Locale;

/**
 * 将仅含 host:port 的根地址补全为带 scheme 的绝对 URL，供 {@code RestTemplate} 等使用。
 */
public final class HttpBaseUrlHelper {

    private HttpBaseUrlHelper() {
    }

    /**
     * 若已以 {@code http://} 或 {@code https://} 开头则原样返回（去首尾空白）；
     * 若以 {@code /} 开头则视为相对路径，不补 scheme；
     * 否则前缀补 {@code http://}。
     */
    public static String ensureHttpScheme(String baseUrl) {
        if (baseUrl == null) {
            return "";
        }
        String t = baseUrl.trim();
        if (t.isEmpty()) {
            return t;
        }
        String lower = t.toLowerCase(Locale.ROOT);
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            return t;
        }
        if (t.startsWith("/")) {
            return t;
        }
        return "http://" + t;
    }
}
