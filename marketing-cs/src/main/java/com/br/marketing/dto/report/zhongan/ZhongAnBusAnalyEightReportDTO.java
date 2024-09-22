package com.br.marketing.dto.report.zhongan;

import com.br.marketing.common.annoation.DecimalFieldConvertor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel(value = "经营分析8场景报表dto")
public class ZhongAnBusAnalyEightReportDTO {
    @ApiModelProperty(value = "日期")
    private String reportDate;
    @ApiModelProperty(value = "组别")
    private String constituencies;
    @ApiModelProperty(value = "总数据量")
    private Long totalNum;
    @ApiModelProperty(value = "进件人数")
    private Long incomingNum;
    @DecimalFieldConvertor(scale = 4)
    @ApiModelProperty(value = "进件增量提升率")
    private BigDecimal incomingIncreaseRate;
    @DecimalFieldConvertor(scale = 5)
    @ApiModelProperty(value = "进件穿透率")
    private BigDecimal incomingTotalRate;
    @ApiModelProperty(value = "批核人数")
    private Long approversNum;
    @ApiModelProperty(value = "批核通过率")
    @DecimalFieldConvertor(scale = 3)
    private BigDecimal approversRate;
    @ApiModelProperty(value = "批核增量提升率")
    @DecimalFieldConvertor(scale = 4)
    private BigDecimal approversIncreaseRate;

    @ApiModelProperty(value = "批核通过穿透率")
    @DecimalFieldConvertor(scale = 6)
    private BigDecimal approversTotalRate;


    @ApiModelProperty(value = "综合增量件数")
    private Long compositeIncrNum;


    @ApiModelProperty(value = "成本")
    @DecimalFieldConvertor(scale = 2, isPercent = false)
    private BigDecimal cost;

    @ApiModelProperty(value = "收入")
    @DecimalFieldConvertor(scale = 2, isPercent = false)
    private BigDecimal income;

    @ApiModelProperty(value = "收入总计")
    @DecimalFieldConvertor(scale = 2, isPercent = false)
    private BigDecimal incomeTotal;

    @ApiModelProperty(value = "ROI")
    @DecimalFieldConvertor(scale = 2, isPercent = false)
    private BigDecimal roi;


}
