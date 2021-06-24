package com.br.marketing.es.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 流水号${apiCode}_${时间}_${序号}v2追加${节点区分码}${微服务区分码}
 *
 * @Author linquan.guo
 * @CreateDate 2019/6/21 14:21
 * @UpdateUser linquan.guo
 * @UpdateDate 2019/6/21 14:21
 * @UpdateRemark 修改内容
 * @Version 1.0
 */
@Slf4j
public class SwiftNumberV2Additional {
    /**
     * 微服务区分码：http://c.100credit.cn/pages/viewpage.action?pageId=16551449
     */
    private static final String SERVICE_CODE = "39";


    public static String getAdditional() {
        //获取当前ip
        String ip = null;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("UnknownHostException", e);
        }
        String clusterValue = System.getProperty("CONF_CLUSTER", System.getenv("CONF_CLUSTER"));
        String clusterCode = SwiftNumberEnum.getCode(clusterValue);
        if (StringUtils.isBlank(clusterCode)) {
            clusterCode = "A";
        }
        return new StringBuilder().append(ipToHexStr(ip)).append(clusterCode).append(SERVICE_CODE).toString();
    }

    /**
     * 节点区分码
     *
     * @param ipStr
     * @return
     */
    public static String ipToHexStr(String ipStr) {
        if (StringUtils.isBlank(ipStr)) {
            return null;
        }
        String[] ipArr = ipStr.split("\\.");
        //ip 以点分隔分别取最后2组,长度不足2位，补0
        String ipHexOne = Integer.toHexString(Integer.parseInt(ipArr[2])).toUpperCase();
        String ipHexTwo = Integer.toHexString(Integer.parseInt(ipArr[3])).toUpperCase();
        return new StringBuilder().append(ipHexOne.length() > 1 ? ipHexOne : String.format("0%s", ipHexOne))
                .append(ipHexTwo.length() > 1 ? ipHexTwo : String.format("0%s", ipHexTwo)).toString();
    }

}
