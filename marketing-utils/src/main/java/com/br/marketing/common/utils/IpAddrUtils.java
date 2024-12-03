package com.br.marketing.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-11-22
 */
@Slf4j
public class IpAddrUtils {
    private static String LocalhostIpAddr = null;
    private static String LocalhostIpDecimalism = null;
    private static String LocalHostIpHex = null;

    public IpAddrUtils(){
        initLocalhostIpAddr();
    }

    public static void initLocalhostIpAddr() {
        try {
            InetAddress localHostAddr = InetAddress.getLocalHost();
            String hostName = localHostAddr.getHostName();
            String localAddrStr = localHostAddr.getHostAddress();
            if(log.isInfoEnabled()){
                log.info("localAddrStr:{}", localAddrStr);
            }
            InetAddress[] addrList = InetAddress.getAllByName(hostName);
            List<InetAddress> siteLocalAddrs = new ArrayList();
            InetAddress[] var5 = addrList;
            int var6 = addrList.length;
            for(int var7 = 0; var7 < var6; ++var7) {
                InetAddress addr = var5[var7];
                if (addr instanceof Inet4Address && !addr.isLoopbackAddress() && !addr.isLinkLocalAddress() && addr.isSiteLocalAddress()) {
                    siteLocalAddrs.add(addr);
                }
            }
            String ipAddrStr = null;
            if (siteLocalAddrs.isEmpty()) {
                ipAddrStr = localAddrStr;
                log.warn("Not find Site Local Addr, use localHostAddr:" + localAddrStr);
            }
            if (siteLocalAddrs.size() == 1) {
                ipAddrStr = (siteLocalAddrs.get(0)).getHostAddress();
            } else if (siteLocalAddrs.size() > 1) {
                ipAddrStr = (siteLocalAddrs.get(0)).getHostAddress();
            }
            LocalhostIpAddr = ipAddrStr;

            StringBuilder suffixBuilder = new StringBuilder();
            StringBuilder suffixBuilder2 = new StringBuilder();
            String[] split = ipAddrStr.split("\\.");
            String ipFirstByte = split[0];
            String ipSecondByte = split[1];
            String ipThirdByte = split[2];
            String ipLastByte = split[3];

            // 16进制 开始
            String ipFirstHex = ipToLong(ipFirstByte);
            String ipSecondHex = ipToLong(ipSecondByte);
            String ipThirdHex = ipToLong(ipThirdByte);
            String ipLastHex = ipToLong(ipLastByte);
            autoFillHex(ipFirstHex, suffixBuilder2);
//            if (ipFirstHex.length() == 1) {
//                suffixBuilder2.append("0");
//            }
            suffixBuilder2.append(ipFirstHex);
            autoFillHex(ipSecondHex, suffixBuilder2);
//            if (ipSecondHex.length() == 1) {
//                suffixBuilder2.append("0");
//            }
            suffixBuilder2.append(ipSecondHex);
            autoFillHex(ipThirdHex, suffixBuilder2);
//            if (ipThirdHex.length() == 1) {
//                suffixBuilder2.append("0");
//            }
            suffixBuilder2.append(ipThirdHex);
            autoFillHex(ipLastHex, suffixBuilder2);
//            if (ipLastHex.length() == 1) {
//                suffixBuilder2.append("0");
//            }
            suffixBuilder2.append(ipLastHex);
            LocalHostIpHex = suffixBuilder2.toString();
            // 16进制 结束

            // 10进制 开始
            autoFill(ipFirstByte, suffixBuilder);
            suffixBuilder.append(ipFirstByte.toUpperCase());
            autoFill(ipSecondByte, suffixBuilder);
            suffixBuilder.append(ipSecondByte.toUpperCase());
            autoFill(ipThirdByte, suffixBuilder);
            suffixBuilder.append(ipThirdByte.toUpperCase());
            autoFill(ipLastByte, suffixBuilder);
            suffixBuilder.append(ipLastByte.toUpperCase());
            LocalhostIpDecimalism = suffixBuilder.toString();
        } catch (UnknownHostException var12) {
            log.error("Got exception for localhost IP Address:{}", var12.getMessage());
//            return -1;
        }
//        if (LocalhostIpDecimalism != null && LocalhostIpDecimalism.length() == 4) {
//            log.warn("Got Localhost IP Success! LocalhostIpLastByte:{} ", LocalhostIpDecimalism);
//            return 0;
//        } else {
//            log.error("Got Localhost IP Failed! LocalhostIpLastByte:{} ", LocalhostIpDecimalism);
//            return -1;
//        }
    }

    public static void autoFillHex(String byteString, StringBuilder sb){
        if (byteString.length() == 1) {
            sb.append("0");
        }
    }

    public static void autoFill(String byteString, StringBuilder sb){
        if (byteString.length() == 1) {
            sb.append("00");
        }else if(byteString.length() == 2) {
            sb.append("0");
        }
    }

    public static String ipToLong(String ipString) {
        if (StringUtils.isBlank(ipString)) {
            return null;
        } else {
            String[] ip = ipString.split("\\.");
            StringBuffer sb = new StringBuffer();
            String[] var3 = ip;
            int var4 = ip.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                String str = var3[var5];
                sb.append(Integer.toHexString(Integer.parseInt(str)));
            }

            return sb.toString();
        }
    }

    public String getLocalhostIpAddr(){
        return LocalhostIpAddr;
    }
    public String getLocalhostIpDecimalism(){
        return LocalhostIpDecimalism;
    }
    public String getLocalHostIpHex(){
        return LocalHostIpHex;
    }
    public static void main(String[] args) {
        IpAddrUtils ipAddrUtils = new IpAddrUtils();
        System.out.println(ipAddrUtils.getLocalhostIpDecimalism());
        System.out.println(ipAddrUtils.getLocalHostIpHex());
        System.out.println(ipAddrUtils.getLocalhostIpAddr());
//        IpAddrUtils.initLocalhostIpAddr();
//        System.out.println(IpAddrUtils.LocalhostIpLastByte);
//        System.out.println(IpAddrUtils.LocalHostIpHex);
//        System.out.println(IpAddrUtils.LocalhostIpAddr);
    }
}
