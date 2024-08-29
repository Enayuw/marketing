package com.br.marketing.dto.report.xiecheng;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * 携程单日撞库结果分布报表dto
 *
 * @author senyang.zheng
 * @date 2024/08/28
 */
@Data
@ApiModel(value = "携程单日撞库结果分布报表dto")
public class XiechengCollidingDailyReportDTO {

    @ApiModelProperty(value = "日期")
    private String reportDate;
    @ApiModelProperty(value = "dataPacket")
    private String dataPacket;
    @ApiModelProperty(value = "orgChannel")
    private String orgChannel;
    @ApiModelProperty(value = "info")
    private String info;
    @ApiModelProperty(value = "撞库结果TRUE量级")
    private Long count;

}
