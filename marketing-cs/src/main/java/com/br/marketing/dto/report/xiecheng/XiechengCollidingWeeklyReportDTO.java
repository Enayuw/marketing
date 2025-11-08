package com.br.marketing.dto.report.xiecheng;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "携程7日撞库结果分布报表dto")
public class XiechengCollidingWeeklyReportDTO implements Serializable {
    private static final long serialVersionUID = 7438790119320620550L;
    @Schema(description = "dataPacket")
    private String dataPacket;
    @Schema(description = "锁定周期")
    private String lockPeriod;
    @Schema(description = "交集量级（定值）")
    private Long intersectionNum;
    @Schema(description = "锁定量级")
    private Long lockNum;
    @Schema(description = "撞回率")
    private BigDecimal collidingBackRatio;
}
