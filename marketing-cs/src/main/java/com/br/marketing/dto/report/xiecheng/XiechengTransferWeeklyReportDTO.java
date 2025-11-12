package com.br.marketing.dto.report.xiecheng;

import com.br.marketing.common.annoation.DecimalFieldConvertor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 携程7日滚动转化报表dto
 *
 * @author senyang.zheng
 * @date 2024/08/28
 */
@Data
@Schema(description = "携程7日滚动转化报表dto")
public class XiechengTransferWeeklyReportDTO implements Serializable {

    private static final long serialVersionUID = 1049740263038976458L;

    @Schema(description = "日期")
    private String rollPeriod;

    @Schema(description = "实际外呼量")
    private Long outboundNum;

    @Schema(description = "登录量")
    private Long loginNum;

    @Schema(description = "身份认证量")
    private Long certifyNum;

    @Schema(description = "申请量")
    private Long applyNum;

    @Schema(description = "授信量")
    private Long creditNum;

    @Schema(description = "申请提现量")
    private Long applyWithdrawNum;

    @Schema(description = "提现量")
    private Long withdrawNum;

    @Schema(description = "期均授信量")
    private Long creditAvgNum;

    @Schema(description = "登录率")
    @DecimalFieldConvertor
    private BigDecimal loginRatio;

    @Schema(description = "身份认证率")
    @DecimalFieldConvertor(scale = 2)
    private BigDecimal certifyRatio;

    @Schema(description = "申请率")
    @DecimalFieldConvertor(scale = 2)
    private BigDecimal applyRatio;

    @Schema(description = "授信率")
    @DecimalFieldConvertor(scale = 2)
    private BigDecimal creditRatio;

    @Schema(description = "提现率")
    @DecimalFieldConvertor
    private BigDecimal withdrawRatio;

    @Schema(description = "申请身份认证率")
    @DecimalFieldConvertor(scale = 2)
    private BigDecimal applyCertifyRatio;

    @Schema(description = "身份认证完成率")
    @DecimalFieldConvertor(scale = 2)
    private BigDecimal certifyCompleteRatio;

    @Schema(description = "过件率")
    @DecimalFieldConvertor(scale = 2)
    private BigDecimal overPieceRatio;

    @Schema(description = "提现成功率（授信后提现）")
    @DecimalFieldConvertor(scale = 2)
    private BigDecimal withdrawSucRatio;
}
