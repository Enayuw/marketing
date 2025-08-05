package com.br.marketing.vo;

import com.br.marketing.entity.ScoreRuleConfig;
import lombok.Data;

@Data
public class CustomerScoreRuleVO extends ScoreRuleConfig {
    private String apiCode;
    private String startDate;
    private Integer dataLimit;
    private Integer isOrNoScoreVer;
    private Integer priority;
    // 生成任务方式 1：手动（包括页面勾选和自定义上传数据时间范围JOB） 2：自动(包括每个任务的周期和每日定时)
    private Integer buildType;
    private Long autoBuildConfigId;
    private String conditionInfo;
    private String cid;
    private String labelName;
}
