package com.br.marketing.dto.report.zhongan;

import com.br.marketing.common.annoation.DecimalFieldConvertor;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "经营分析1场景报表dto")
public class ZhongAnBusAnalyOneReportDTO implements Serializable {

    @Schema(description = "日期")
    private String reportDate;

    @Schema(description = "观测日")
    private String queryDate;

    @Schema(description = "组别")
    private String constituencies;
    @Schema(description = "总数据量")
    private Long totalNum;
    @Schema(description = "进件人数")
    private Long incomingNum;
    @DecimalFieldConvertor(scale = 0)
    @Schema(description = "进件增量提升率")
    private BigDecimal incomingIncreaseRate;
    @DecimalFieldConvertor(scale = 3)
    @Schema(description = "进件穿透率")
    private BigDecimal incomingTotalRate;
    @Schema(description = "批核人数")
    private Long approversNum;
    @Schema(description = "批核通过率")
    @DecimalFieldConvertor(scale = 2)
    private BigDecimal approversRate;
    @Schema(description = "批核增量提升率")
    @DecimalFieldConvertor(scale = 0)
    private BigDecimal approversIncreaseRate;

    @Schema(description = "批核通过穿透率")
    @DecimalFieldConvertor(scale = 4)
    private BigDecimal approversTotalRate;


    @Schema(description = "综合增量件数")
    @DecimalFieldConvertor(scale = 0, isPercent = false)
    private BigDecimal compositeIncrNum;


    @Schema(description = "成本")
    @DecimalFieldConvertor(scale = 2,isPercent = false)
    private BigDecimal cost;

    @Schema(description = "收入")
    @DecimalFieldConvertor(scale = 2,isPercent = false)
    private BigDecimal income;

    @Schema(description = "ROI")
    @DecimalFieldConvertor(scale = 2,isPercent = false)
    private BigDecimal roi;

}
