package com.br.marketing.dto.report.zhongan;

import com.br.marketing.common.annoation.DecimalFieldConvertor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName ZhonganOutboundCallReportDTO
 * @Description 外呼统计报表dto
 * @Author kongbx
 * @Date 2024/9/21 11:55
 */
@Data
@ApiModel(value = "携程单日撞库结果分布报表dto")
public class ZhonganOutboundCallReportDTO implements Serializable {

    private static final long serialVersionUID = -6616583716934011016L;

    @ApiModelProperty(value = "触达日期")
    private String reportDate;
    @ApiModelProperty(value = "场景")
    private String userType;
    @ApiModelProperty(value = "实际外呼量")
    private Long actualOutboundNum;
    @ApiModelProperty(value = "接通量")
    private Long throughputNum;
    @ApiModelProperty(value = "通话总时长(分钟)")
    private Long durationTotal;
    @ApiModelProperty(value = "短信触发量")
    private Long smsTriggersNum;
    @ApiModelProperty(value = "短信成功发送量")
    private Long smsSucSendNum;
    @ApiModelProperty(value = "接通率")
    private Long continuityRatio;
    @ApiModelProperty(value = "接通短信触发率")
    private Long smsTriggerRatio;
    @ApiModelProperty(value = "短信成功发送率")
    private Long smsSucSendRatio;
    @ApiModelProperty("成本")
    @DecimalFieldConvertor(scale = 2)
    private BigDecimal cost;

}
