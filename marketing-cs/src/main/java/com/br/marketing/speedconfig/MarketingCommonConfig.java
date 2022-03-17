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

}
