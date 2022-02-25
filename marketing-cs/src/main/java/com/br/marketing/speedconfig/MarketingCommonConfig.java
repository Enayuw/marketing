package com.br.marketing.speedconfig;


import com.br.speed.client.common.annotations.SpeedFile;
import lombok.Data;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

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
}
