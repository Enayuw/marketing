package com.br.marketing.dto.report.xiecheng;

import com.br.marketing.common.annoation.DecimalFieldConvertor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 携程数据使用率报表dto
 *
 * @author senyang.zheng
 * @date 2024/08/28
 */
@Data
@Schema(description = "携程数据使用率报表dto")
public class XiechengDataRatioDailyReportDTO {

    @Schema(description = "日期")
    private String reportDate;
    @Schema(description = "撞得量")
    private Long collidingBackNum;
    @Schema(description = "析出量")
    private Long extractionNum;
    @Schema(description = "可外呼量")
    private Long callableNum;
    @Schema(description = "析出率")
    @DecimalFieldConvertor(scale = 0)
    private BigDecimal extractionRatio;
    @Schema(description = "可外呼率")
    @DecimalFieldConvertor(scale = 0)
    private BigDecimal callableRatio;
}
