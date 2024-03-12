package com.br.marketing.monkeydata.entity.commonobj;

import com.br.marketing.monkeydata.entity.InputDataCondition;
import lombok.Data;

import java.util.List;

/**
 * @author zhen.li1
 * @desc 上传接口通用入参
 */
@Data
public class MarketingSyncCondition extends InputDataCondition {

    private String apiCode;

    private Long minId;

    private String appletDateStart;

    private String appletDateEnd;
    /**
     * 执行日期集合：精确到天
     */
    private List<String> executeDateList;
    /**
     * 场景 userType
     */
    private String userType;
}
