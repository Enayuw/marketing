package com.br.marketing.speedconfig;


import com.br.speed.client.common.annotations.SpeedFile;
import lombok.Data;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Configuration
@SpeedFile(filename = "marketingcommon.properties",topic = "marketing")
@Data
public class MarketingCommonConfig {
    /**
     * 推送客服
     */
    private List<String> apiCodeOfpushCustomer;

    /**
     * 推送哈啰
     */
    private List<String> apiCodeOfpushHaluoByTransfer;

    /**
     * 是否是宜信客服转化接口
     */
    private HashMap<String,Boolean> customerTransferIsYx;

    /**
     * 海尔apicode
     */
    private List<String> haierApiCode;

    /**
     * 玖富apicode
     */
    private List<String> jfApiCode;

    /**
     * 记录taskid时间的apicode
     */
    private List<String> apiCodeOfRecordTaskTime;

    /**
     * 去重线程数
     */
    private Integer soleNum;

    /**
     * 萨摩耶场景
     */
    private HashSet<String> groupTypeSaMoye;

    /**
     * 数禾apicode
     */
    private List<String> shuheApiCode;

    /**
     * 客户使用规则映射
     */
    private HashMap<String,String> customerRuleMapping;

    /**
     * 配置走通用流程apiCode
     */
    private List<String> universalProcessApiCode;


    /**
     * 数禾转化数据提取分场景, T 代表当前天到月底； T+/-day 代表当前天到day天
     */
    private Map<String, String> shuHeTransferExtractDayMap;

    /**
     * 数禾转化数据提取apiCode集合
     */
    private List<String> shuHeTransferExtractApiCodes;

    /**
     * 数禾转化数据提取任务开始时间
     */
    private String shuHeTransferExtractJobStartTime;

    /**
     * 数禾转化数据提取任务是否使用准全量转化数据
     */
    private Boolean shuHeTransferExtractIfUseQuasiTotalQuantity;

    /**
     * 数禾有效期, T 代表当前天到月底； T+/-day 代表当前天到day天
     */
    private Map<String, String> shuHePeriodOfValidityDayMap;

    /**
     * 消息队列过期时间
     */
    private String messageQueueExpireTime;
    /**
     * 宜信实时转化数据提取apiCode集合
     */
    private List<String> yinXinTransferRealTimeApiCodes;

    /**
     * 宜信实时转化数据执行时间
     */
    private String yinXinTransferRealTimeExecuteTime;

    /**
     * 宜信非实时转化数据执行时间
     */
    private String yinXinTransferNoRealTimeExecuteTime;

    /**
     * 是否开启宜信非实时数据提取
     */
    private Boolean isOpenYinXinTransferNoRealTimeExtract;

    /**
     * 宜信非实时typelist
     */
    private List<String> yixinNoRealTimeType;

    /**
     * 宜信非实时推客服typelist
     */
    private List<String> yixinNoRealTimePushRobotAIType;

    /**
     * 电销文件定制化处理
     */
    private HashMap<String, List<String>> dxFileCustomize;

    /**
     * 宜信apicode
     */
    private List<String> yiXinApiCode;

    /**
     * 壹钱包加密公钥
     */
    private String yiQianBaoPubKey;
    /**
     * ppd客服类型
     */
    private HashMap<String, List<String>> ppdCustomerType;

    /**
     * 促复借可用额度
     */
    private Double clcUsrAvlLmtLv0;

    /**
     * 哈啰回调数据落库配置线程数
     */
    private Integer haloSaveDataThreadNum;

    /**
     * 数禾推送电销情况;eg:{"促复借":["a","b","c"],"促首借":["a","b"]}
     */
    private HashMap<String, List<String>> shuHePushDXStatusMap;

    /**
     * # 数禾用户可用额度区间;大于:&gt;、小于:&lt;、等于:&eq;、不等于:&nq;、大于等于:&ge;、小于等于:&le;;eg:["&ge;","1","&le;","100"]
     */
    private List<String> shuHeUserAvailableQuotaRange;

    /**
     * 2022/5/17 15:05
     * 拨打记录数据配置apicode推送mq
     */
    private List<String> callRecordDataPushMqApiCodes;

    /**
     * 玖富转化数据提取apiCode集合
     */
    private List<String> JiuFuTransferApiCodes;

    /**
     * 玖富转化数据提取执行时间
     */
    private String JiuFuTransferExecuteTime;

    /**
     *  哈罗转化数据规则
     *  {"status":"a,b,d","Ddate":"4","ABCdate":"5","taskIddate":"35","dTimes":"7"}
     */
    private HashMap<String,String> haluoTransferRule;

}
