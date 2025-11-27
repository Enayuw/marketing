package com.br.marketing.dto.report.xiecheng;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;


/**
 * 携程单日撞库结果分布报表dto
 *
 * @author senyang.zheng
 * @date 2024/08/28
 */
@Data
@Schema(description = "携程单日撞库结果分布报表dto")
public class XiechengCollidingDailyReportDTO implements Serializable {

    private static final long serialVersionUID = -6616583716934011016L;
    @Schema(description = "日期")
    private String reportDate;
    @Schema(description = "dataPacket")
    private String dataPacket;
    @Schema(description = "orgChannel")
    private String orgChannel;
    @Schema(description = "锁定量级")
    private Long lockNum;

}
