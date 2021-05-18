package com.br.marketing.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author puxuan.song 2018年3月20日
 */
@Slf4j
public class EnvironmentManager {
    public static String localhostIpAddr = null;
    public static String localhostIpLastByte = null;
    /**
     *ip的后两位（以"."分隔的后两位）的16进制的值
     */
    public static String localHostIpHex = null;
    

    /**
     * 
     * @return
     */
    public static int initLocalhostIpAddr() {
        try {
            InetAddress localHostAddr = InetAddress.getLocalHost();
            String hostName = localHostAddr.getHostName();
            String localAddrStr = localHostAddr.getHostAddress();
            log.info("localAddrStr----{}",localAddrStr);
            InetAddress[] addrList = InetAddress.getAllByName(hostName);

            List<InetAddress> siteLocalAddrs = new ArrayList<>();
            for (InetAddress addr : addrList) {
                if (!(addr instanceof Inet4Address) || addr.isLoopbackAddress() || addr.isLinkLocalAddress()) {
                    continue;
                } else if (addr.isSiteLocalAddress()) {
                    siteLocalAddrs.add(addr);
                }
            }

            String ipAddrStr = null;
            if (siteLocalAddrs.isEmpty()) {
                ipAddrStr = localAddrStr;
                log.warn("Not find Site Local Addr, use localHostAddr:{}" , localAddrStr);
            }
            if (siteLocalAddrs.size() == 1) {
                ipAddrStr = siteLocalAddrs.get(0).getHostAddress();
            } else if (siteLocalAddrs.size() > 1) {
                ipAddrStr = siteLocalAddrs.get(0).getHostAddress();
            }

            StringBuilder suffixBuilder = new StringBuilder();
            StringBuilder suffixBuilder2 = new StringBuilder();
            String ipThirdByte = ipAddrStr.split("\\.")[2];
            String ipLastByte = ipAddrStr.split("\\.")[3];

            String ipThirdHex = ipToLong(ipThirdByte);
            String ipLastHex = ipToLong(ipLastByte);
            if(ipThirdHex.length() == 1){
                suffixBuilder2.append("0");
            }
            suffixBuilder2.append(ipThirdHex);
            if(ipLastHex.length() == 1){
                suffixBuilder2.append("0");
            }
            suffixBuilder2.append(ipLastHex);
            localHostIpHex = suffixBuilder2.toString();
            if (ipThirdByte.length() == 1) {
                suffixBuilder.append("0");
            }
            if(ipThirdByte.length() == 3){
                ipThirdByte=ipThirdByte.substring(1,ipThirdByte.length());
            }
            suffixBuilder.append(ipThirdByte.toUpperCase());
            if (ipLastByte.length() == 1) {
                suffixBuilder.append("0");
            }
            if(ipLastByte.length() == 3){
                ipLastByte=ipLastByte.substring(1,ipLastByte.length());
            }
            suffixBuilder.append(ipLastByte.toUpperCase());
            localhostIpLastByte = suffixBuilder.toString();
            localhostIpAddr = ipAddrStr;
        } catch (UnknownHostException e) {
            log.error("Got exception for localhost IP Address:", e);
            return -1;
        }

        if (localhostIpLastByte != null && localhostIpLastByte.length() ==4) {
            log.info("Got Localhost IP Success! LocalhostIpLastByte: {}" , localhostIpLastByte);
            return 0;
        } else {
            log.info("Got Localhost IP Failed! LocalhostIpLastByte:{} " , localhostIpLastByte);
            return -1;
        }
    }
    
    /**
     * 将ip转换为16进制
     * @param ipString
     * @return
     */
    public static String ipToLong(String ipString) {
        if(StringUtils.isBlank(ipString)){
            return null;  
        }  
        String[] ip=ipString.split("\\.");
        StringBuilder sb=new StringBuilder();
        for (String str : ip) {
            sb.append(Integer.toHexString(Integer.parseInt(str)));
        }
        return sb.toString();
    }
}
