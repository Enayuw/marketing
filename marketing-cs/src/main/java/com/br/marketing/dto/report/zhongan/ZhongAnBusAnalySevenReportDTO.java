package com.br.marketing.dto.report.zhongan;

import com.br.marketing.common.annoation.DecimalFieldConvertor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
@Data
@Schema(description = "经营分析7场景报表dto")
public class ZhongAnBusAnalySevenReportDTO {


    @Schema(description = "日期")
    private String reportDate;

    @Schema(description = "观测日")
    private String queryDate;

    @Schema(description = "客群组别")
    private String constituencies;
    @Schema(description = "数据量")
    private Long totalNum;

    @DecimalFieldConvertor(scale = 4)
    @Schema(description = "登录率")
    private BigDecimal loginRate;

    @Schema(description = "登录量")
    private Long loginNum;

    @Schema(description = "进件人数")
    private Long incomingNum;

    @DecimalFieldConvertor(scale = 4)
    @Schema(description = "进件穿透率")
    private BigDecimal incomingTotalRate;

    @DecimalFieldConvertor(scale = 1)
    @Schema(description = "进件增量提升率")
    private BigDecimal incomingIncreaseRate;

    @Schema(description = "批核人数")
    private Long approversNum;
    @Schema(description = "批核通过率")
    @DecimalFieldConvertor(scale = 1)
    private BigDecimal approversRate;

    @Schema(description = "批核通过穿透率")
    @DecimalFieldConvertor(scale = 4)
    private BigDecimal approversTotalRate;

    @Schema(description = "批核通过穿透率提升比")
    @DecimalFieldConvertor(scale = 1)
    private BigDecimal approversIncreaseRate;


    @Schema(description = "增量批核人数")
    private Long approversIncrNum;

    @Schema(description = "批核件均")
    private Long approvalsAvgNum;

    @Schema(description = "发起提现人数")
    private Long applyPayNum;

    @Schema(description = "发起提现率")
    @DecimalFieldConvertor(scale = 1)
    private BigDecimal applyPayRate;

    @Schema(description = "发起提现率提升比")
    @DecimalFieldConvertor(scale = 1)
    private BigDecimal applyPayIncrRate;

    @Schema(description = "提现成功人数")
    private Long applyPaySuccessNum;

    @Schema(description = "放款成功人数")
    private Long lendersSucNum;

    @Schema(description = "提现通过通过率")
    @DecimalFieldConvertor(scale = 1)
    private BigDecimal applyPaySuccessRate;

    @Schema(description = "放款成功率")
    @DecimalFieldConvertor(scale = 2)
    private BigDecimal lendersSucRate;


    @Schema(description = "批核放款穿透率")
    @DecimalFieldConvertor(scale = 2)
    private BigDecimal lendersApproversRate;


    @Schema(description = "放款成功金额")
    private Long lendersSucAmount;

    @Schema(description = "增量放款金额")
    private Long lendersSucIncrAmount;

    @Schema(description = "放款成功穿透率")
    @DecimalFieldConvertor(scale = 3)
    private BigDecimal lendersSucTotalRate;

    @Schema(description = "放款成功穿透率提升比")
    @DecimalFieldConvertor(scale = 1)
    private BigDecimal lendersSucIncrRate;

    @Schema(description = "放款人均")
    private Long lendersSucAvgAmount;

    @Schema(description = "名单产能")
    @DecimalFieldConvertor(scale = 2, isPercent = false)
    private BigDecimal productCapacity;

    @Schema(description = "收入")
    @DecimalFieldConvertor(scale = 2, isPercent = false)
    private BigDecimal income;

    @Schema(description = "成本")
    @DecimalFieldConvertor(scale = 2, isPercent = false)
    private BigDecimal cost;

    @Schema(description = "ROI")
    @DecimalFieldConvertor(scale = 2, isPercent = false)
    private BigDecimal roi;

}
