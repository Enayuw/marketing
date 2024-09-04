package com.br.marketing.dto.report.xiecheng;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;


/**
 * 携程单日撞库结果分布报表dto
 *
 * @author senyang.zheng
 * @date 2024/08/28
 */
@Data
@ApiModel(value = "携程单日撞库结果分布报表dto")
public class XiechengCollidingDailyReportDTO implements Serializable {

    private static final long serialVersionUID = -6616583716934011016L;
    @ApiModelProperty(value = "日期")
    private String reportDate;
    @ApiModelProperty(value = "dataPacket")
    private String dataPacket;
    @ApiModelProperty(value = "orgChannel")
    private String orgChannel;
    @ApiModelProperty(value = "info")
    private String info;
    @ApiModelProperty(value = "锁定量级")
    private Long lockNum;

}
