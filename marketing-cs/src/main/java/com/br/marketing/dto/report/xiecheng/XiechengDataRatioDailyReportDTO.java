package com.br.marketing.dto.report.xiecheng;

import com.br.marketing.common.annoation.DecimalFieldConvertor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 携程数据使用率报表dto
 *
 * @author senyang.zheng
 * @date 2024/08/28
 */
@Data
@ApiModel(value = "携程数据使用率报表dto")
public class XiechengDataRatioDailyReportDTO {

    @ApiModelProperty(value = "日期")
    private String reportDate;
    @ApiModelProperty(value = "撞得量")
    private Long collidingBackNum;
    @ApiModelProperty(value = "析出量")
    private Long extractionNum;
    @ApiModelProperty(value = "可外呼量")
    private Long callableNum;
    @ApiModelProperty(value = "析出率")
    @DecimalFieldConvertor(scale = 0)
    private BigDecimal extractionRatio;
    @ApiModelProperty(value = "可外呼率")
    @DecimalFieldConvertor(scale = 0)
    private BigDecimal callableRatio;
}
