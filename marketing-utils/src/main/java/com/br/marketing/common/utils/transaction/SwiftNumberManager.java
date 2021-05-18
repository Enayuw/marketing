package com.br.marketing.common.utils.transaction;

import com.br.common.util.DateUtils;
import com.br.marketing.common.utils.EnvironmentManager;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * 生成 swift_num工具类
 */
@Slf4j
public class SwiftNumberManager {
    private static long sequenceId = System.currentTimeMillis();
    private final static Object SYNCLOCK = new Object();
    private SwiftNumberManager() {}

    private static volatile SwiftNumberManager swiftNumberManager = null;

    public static SwiftNumberManager instance() {
        if (swiftNumberManager == null) {
            synchronized (SYNCLOCK) {
                if (swiftNumberManager == null) {
                    swiftNumberManager = new SwiftNumberManager();
                }
            }
        }
        return swiftNumberManager;
    }

    public static String generate() {

        String date = DateUtils.format(new Date(), DateUtils.yyyyMMddHHmmss);
        String nextTime = getNextId() + "";
        date = date + "_" + nextTime.substring(nextTime.length() - 4);
        return date;

    }

    /**
     * 流水号生成2.0版本，格式：${apiCode}_${时间}_${序号}${节点区分码}${微服务区分码}
     * 举例：5200230_20190524202206_2898 0C22 A 13
     * 说明：
     *     序号：4位自增长数据，计数到9999后从0重新计数
     *     微服务区分码：phone-check-api是13（http://c.100credit.cn/pages/viewpage.action?pageId=16551449）
     *     节点区分码：(假设节点ip 为192.168.12.34)
     *     		a.	ip 以点分隔分别取最后2组，12和34
     * 			b.	把12和34转为16进制分别为C和22
     * 			c.	如果b步骤结果的长度不足2位，补0,结果为0C和22
     * 			d.	取集群配置标识，假设为A
     * 			e.	拼接结果0C22A
     * @return
     */
    public String generatePlus(){
        String date = DateUtils.format(new Date(), DateUtils.yyyyMMddHHmmss);
        String hostNum;
        if(EnvironmentManager.initLocalhostIpAddr()==0){
            hostNum = EnvironmentManager.localHostIpHex;
        }else{
            hostNum = "0000";
            log.error("获取节点ip失败---{}",EnvironmentManager.localhostIpLastByte);
        }
        String nextTime=getNextId()+"";
        date += "_"+nextTime.substring(nextTime.length()-4)+hostNum;
        return date;
    }


    private static  synchronized long getNextId() {
        return sequenceId++;
    }

}

	

