package com.br.marketing.dto.report.zhongan;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel(value = "报表规则")
public class ReportStatisticRule {

    private String reportId;
    private String reportTaskId;
    private String reportType;
    private String reportOrder;
    private String reportRule;
    private String reportStatus;
    private String reportDate;
    private Integer deleteFlag;
    private Date createTime;
    private Date updateTime;

}
