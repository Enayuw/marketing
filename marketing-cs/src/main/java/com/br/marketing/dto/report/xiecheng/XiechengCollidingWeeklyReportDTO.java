package com.br.marketing.dto.report.xiecheng;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 携程7日撞库结果分布报表dto
 *
 * @author senyang.zheng
 * @date 2024/08/28
 */
@Data
@ApiModel(value = "携程7日撞库结果分布报表dto")
public class XiechengCollidingWeeklyReportDTO implements Serializable {
    private static final long serialVersionUID = 7438790119320620550L;
    @ApiModelProperty(value = "dataPacket")
    private String dataPacket;
    @ApiModelProperty(value = "锁定周期")
    private String lockPeriod;
    @ApiModelProperty(value = "交集量级（定值）")
    private Long intersectionNum;
    @ApiModelProperty(value = "锁定量级")
    private Long lockNum;
    @ApiModelProperty(value = "撞回率")
    private BigDecimal collidingBackRatio;
}
