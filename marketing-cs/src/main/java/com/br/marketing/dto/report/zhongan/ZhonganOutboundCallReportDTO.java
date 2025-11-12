package com.br.marketing.dto.report.zhongan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName ZhonganOutboundCallReportDTO
 * @Description 外呼统计报表dto
 * @Author kongbx
 * @Date 2024/9/21 11:55
 */
@Data
@Schema(description = "携程单日撞库结果分布报表dto")
public class ZhonganOutboundCallReportDTO {

    @Schema(description = "触达日期")
    private String reportDate;
    @Schema(description = "场景")
    private String userType;
    @Schema(description = "实际外呼量")
    private Long actualOutboundNum;
    @Schema(description = "接通量")
    private Long throughputNum;
    @Schema(description = "通话总时长(分钟)")
    private Long durationTotal;
    @Schema(description = "短信触发量")
    private Long smsTriggersNum;
    @Schema(description = "短信成功发送量")
    private Long smsSucSendNum;
    @Schema(description = "接通率")
    private BigDecimal continuityRatio;
    @Schema(description = "接通短信触发率")
    private BigDecimal smsTriggerRatio;
    @Schema(description = "短信成功发送率")
    private BigDecimal smsSucSendRatio;
    @Schema(description = "成本")
    private BigDecimal cost;
    @Schema(description = "维度,0-总计 1-首登 2-非首登")
    private String dimension;

}
