package com.br.marketing.common.utils.net;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;

@Slf4j
public class IpUtil {
    public static String getHostName() {
        String hostName = "unknown";
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            log.error("getHostName error", e);
        }
        log.debug("HostName:{}", hostName);
        return hostName;
    }

}