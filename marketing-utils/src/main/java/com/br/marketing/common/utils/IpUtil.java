package com.br.marketing.common.utils;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * ip
 *
 * @Author linquan.guo
 * @CreateDate 2021/11/3 15:13
 * @UpdateUser linquan.guo
 * @UpdateDate 2021/11/3 15:13
 * @UpdateRemark 修改内容
 * @Version 1.0
 */
@Slf4j
public class IpUtil {
    /**
     * 默认未知
     */
    private static final String BASE_UNKNOWN = "unknown";

    /**
     * 获取ip
     *
     * @param
     * @return
     */
    public static String getHostName() {
        String hostName = BASE_UNKNOWN;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            log.warn("ipcontext", e);
        }
        log.debug("HostName:{}", hostName);
        return hostName;
    }

    /**
     * 获取请求ip地址
     *
     * @param request
     * @return
     */
    public static String getRemortIP(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || BASE_UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || BASE_UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || BASE_UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || BASE_UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || BASE_UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

}