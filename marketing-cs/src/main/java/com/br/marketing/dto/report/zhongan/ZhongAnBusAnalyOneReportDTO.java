package com.br.marketing.dto.report.zhongan;

import com.br.marketing.common.annoation.DecimalFieldConvertor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName ZhongAnBusAnalyOneReportDTO
 * @Description 经营分析1场景报表dto
 * @Author zhen.Li1
 * @Date 2024/9/21 19:55
 */
@Data
@ApiModel(value = "经营分析1场景报表dto")
public class ZhongAnBusAnalyOneReportDTO implements Serializable {

    @ApiModelProperty(value = "日期")
    private String reportDate;

    @ApiModelProperty(value = "观测日")
    private String queryDate;

    @ApiModelProperty(value = "组别")
    private String constituencies;
    @ApiModelProperty(value = "总数据量")
    private Long totalNum;
    @ApiModelProperty(value = "进件人数")
    private Long incomingNum;
    @DecimalFieldConvertor(scale = 0)
    @ApiModelProperty(value = "进件增量提升率")
    private BigDecimal incomingIncreaseRate;
    @DecimalFieldConvertor(scale = 3)
    @ApiModelProperty(value = "进件穿透率")
    private BigDecimal incomingTotalRate;
    @ApiModelProperty(value = "批核人数")
    private Long approversNum;
    @ApiModelProperty(value = "批核通过率")
    @DecimalFieldConvertor(scale = 2)
    private BigDecimal approversRate;
    @ApiModelProperty(value = "批核增量提升率")
    @DecimalFieldConvertor(scale = 0)
    private BigDecimal approversIncreaseRate;

    @ApiModelProperty(value = "批核通过穿透率")
    @DecimalFieldConvertor(scale = 4)
    private BigDecimal approversTotalRate;


    @ApiModelProperty(value = "综合增量件数")
    @DecimalFieldConvertor(scale = 0, isPercent = false)
    private BigDecimal compositeIncrNum;


    @ApiModelProperty(value = "成本")
    @DecimalFieldConvertor(scale = 2,isPercent = false)
    private BigDecimal cost;

    @ApiModelProperty(value = "收入")
    @DecimalFieldConvertor(scale = 2,isPercent = false)
    private BigDecimal income;

    @ApiModelProperty(value = "ROI")
    @DecimalFieldConvertor(scale = 2,isPercent = false)
    private BigDecimal roi;

}
