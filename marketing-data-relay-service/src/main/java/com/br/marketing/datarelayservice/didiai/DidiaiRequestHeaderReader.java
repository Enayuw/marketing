package com.br.marketing.datarelayservice.didiai;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Locale;

/**
 * 滴滴 AI 上传请求头读取工具类。
 *
 * <p>背景：网关或代理可能使用与约定字面量不同的大小写、连字符形式传递 Header 名称，直接调用
 * HttpServletRequest.getHeader 若名称不完全一致会取不到值。本类先按一组常见别名顺序尝试
 * 精确匹配，再遍历全部请求头名称，将名称统一为小写并去掉连字符后与候选名做等价比较，从而在保持对端约定含义不变的前提下
 * 提高取值的健壮性。
 *
 * <p>本类为无状态工具类，仅包含静态方法，不允许实例化。
 *
 * @author yueping.bai
 */
public final class DidiaiRequestHeaderReader {

    private DidiaiRequestHeaderReader() {}

    /**
     * 读取应用标识 appKey。
     *
     * <p>依次尝试常见命名形式，包括驼峰、全大写、带连字符等形式，直到取到非空白值。
     *
     * @param request 当前 HTTP 请求，为空时返回 null
     * @return 去掉首尾空白后的 appKey 字符串；未找到或值为空白时返回 null
     */
    public static String readAppKey(HttpServletRequest request) {
        return firstHeader(request, "appKey", "AppKey", "APPKEY", "app-key", "App-Key");
    }

    /**
     * 读取毫秒级时间戳字符串，供解密派生 IV 与验签使用。
     *
     * @param request 当前 HTTP 请求，为空时返回 null
     * @return 去掉首尾空白后的时间戳字符串；未找到或值为空白时返回 null
     */
    public static String readTimestamp(HttpServletRequest request) {
        return firstHeader(request, "timestamp", "Timestamp", "TIMESTAMP", "time-stamp");
    }

    /**
     * 读取 Base64 编码的请求签名 sign。
     *
     * @param request 当前 HTTP 请求，为空时返回 null
     * @return 去掉首尾空白后的签名字符串；未找到或值为空白时返回 null
     */
    public static String readSign(HttpServletRequest request) {
        return firstHeader(request, "sign", "Sign", "SIGN");
    }

    /**
     * 解析当前请求对应的客户端 IP 地址。
     *
     * <p>若存在反向代理常见转发头 X-Forwarded-For（大小写不敏感尝试两种常见写法），则取其中第一个
     * 逗号前的片段作为客户端地址，以适配多级代理场景；否则使用 HttpServletRequest.getRemoteAddr。
     *
     * @param request 当前 HTTP 请求，为空时返回 null
     * @return 推断得到的 IPv4 或 IPv6 字符串；request 为空时返回 null
     */
    public static String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String xff = request.getHeader("X-Forwarded-For");
        if (StringUtils.isBlank(xff)) {
            xff = request.getHeader("x-forwarded-for");
        }
        if (StringUtils.isNotBlank(xff)) {
            int comma = xff.indexOf(',');
            String first = comma > 0 ? xff.substring(0, comma) : xff;
            return first.trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * 按候选名称列表查找第一个非空的请求头取值。
     *
     * <p>处理步骤：
     *
     * <ul>
     *   <li>若 request 或 names 为空，直接返回 null；</li>
     *   <li>先用每个候选名调用 HttpServletRequest.getHeader，命中非空白即返回；</li>
     *   <li>若仍未命中，遍历 HttpServletRequest.getHeaderNames，将实际头名转为小写并去掉连字符，
     *       与候选名同样规范化后的形式比较，相等则取该实际头名对应的值。</li>
     * </ul>
     *
     * @param request 当前 HTTP 请求
     * @param names   一个或多个逻辑等价的头名称候选，顺序表示优先尝试顺序
     * @return 首个非空白头值（已 trim）；不存在则返回 null
     */
    private static String firstHeader(HttpServletRequest request, String... names) {
        if (request == null || names == null) {
            return null;
        }
        for (String name : names) {
            if (StringUtils.isBlank(name)) {
                continue;
            }
            String v = request.getHeader(name);
            if (StringUtils.isNotBlank(v)) {
                return v.trim();
            }
        }
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames == null) {
            return null;
        }
        while (headerNames.hasMoreElements()) {
            String hn = headerNames.nextElement();
            if (hn == null) {
                continue;
            }
            String normalized = hn.toLowerCase(Locale.ROOT).replace("-", "");
            for (String name : names) {
                if (name == null) {
                    continue;
                }
                String key = name.toLowerCase(Locale.ROOT).replace("-", "");
                if (normalized.equals(key)) {
                    String v = request.getHeader(hn);
                    if (StringUtils.isNotBlank(v)) {
                        return v.trim();
                    }
                }
            }
        }
        return null;
    }
}
