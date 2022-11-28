package com.br.marketing.monkeydata.entity.commonobj;

import com.br.marketing.monkeydata.entity.InputDataCondition;
import lombok.Data;

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
}
