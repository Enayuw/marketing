package com.br.marketing.dto.report.zhongan;

import com.br.marketing.common.annoation.DecimalFieldConvertor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
@Data
@ApiModel(value = "经营分析7场景报表dto")
public class ZhongAnBusAnalySevenReportDTO {


    @ApiModelProperty(value = "日期")
    private String reportDate;
    @ApiModelProperty(value = "客群组别")
    private String constituencies;
    @ApiModelProperty(value = "数据量")
    private Long totalNum;

    @DecimalFieldConvertor(scale = 4)
    @ApiModelProperty(value = "登录率")
    private BigDecimal loginRate;

    @ApiModelProperty(value = "登录量")
    private Long loginNum;

    @ApiModelProperty(value = "进件人数")
    private Long incomingNum;
    @DecimalFieldConvertor(scale = 1)
    @ApiModelProperty(value = "进件增量提升率")
    private BigDecimal incomingIncreaseRate;
    @DecimalFieldConvertor(scale = 4)
    @ApiModelProperty(value = "进件穿透率")
    private BigDecimal incomingTotalRate;
    @ApiModelProperty(value = "批核人数")
    private Long approversNum;
    @ApiModelProperty(value = "批核通过率")
    @DecimalFieldConvertor(scale = 1)
    private BigDecimal approversRate;
    @ApiModelProperty(value = "批核通过穿透率提升比")
    @DecimalFieldConvertor(scale = 1)
    private BigDecimal approversIncreaseRate;

    @ApiModelProperty(value = "批核通过穿透率")
    @DecimalFieldConvertor(scale = 4)
    private BigDecimal approversTotalRate;

    @ApiModelProperty(value = "增量批核人数")
    private Long approversIncrNum;

    @ApiModelProperty(value = "批核件均")
    private Long approvalsAvgNum;

    @ApiModelProperty(value = "发起提现人数")
    private Long applyPayNum;

    @ApiModelProperty(value = "发起提现率")
    @DecimalFieldConvertor(scale = 1)
    private BigDecimal applyPayRate;

    @ApiModelProperty(value = "发起提现率提升比")
    @DecimalFieldConvertor(scale = 1)
    private BigDecimal applyPayIncrRate;

    @ApiModelProperty(value = "提现成功人数")
    private Long applyPaySuccessNum;

    @ApiModelProperty(value = "放款成功人数")
    private Long lendersSucNum;

    @ApiModelProperty(value = "提现通过通过率")
    @DecimalFieldConvertor(scale = 1)
    private BigDecimal applyPaySuccessRate;

    @ApiModelProperty(value = "放款成功率")
    @DecimalFieldConvertor(scale = 2)
    private BigDecimal lendersSucRate;


    @ApiModelProperty(value = "批核放款穿透")
    @DecimalFieldConvertor(scale = 2)
    private BigDecimal lendersApproversRate;


    @ApiModelProperty(value = "放款成功金额")
    private Long lendersSucAmount;

    @ApiModelProperty(value = "增量放款金额")
    private Long lendersSucIncrAmount;

    @ApiModelProperty(value = "放款成功穿透率")
    @DecimalFieldConvertor(scale = 3)
    private BigDecimal lendersSucTotalRate;

    @ApiModelProperty(value = "放款成功穿透率提升比")
    @DecimalFieldConvertor(scale = 1)
    private BigDecimal lendersSucIncrRate;

    @ApiModelProperty(value = "放款人均")
    private Long lendersSucAvgAmount;

    @ApiModelProperty(value = "名单产能")
    @DecimalFieldConvertor(scale = 2, isPercent = false)
    private BigDecimal productCapacity;

    @ApiModelProperty(value = "成本")
    @DecimalFieldConvertor(scale = 2, isPercent = false)
    private BigDecimal cost;

    @ApiModelProperty(value = "收入")
    @DecimalFieldConvertor(scale = 2, isPercent = false)
    private BigDecimal income;

    @ApiModelProperty(value = "ROI")
    @DecimalFieldConvertor(scale = 2, isPercent = false)
    private BigDecimal roi;

}
