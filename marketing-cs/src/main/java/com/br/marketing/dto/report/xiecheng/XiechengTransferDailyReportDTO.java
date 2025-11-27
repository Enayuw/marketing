package com.br.marketing.dto.report.xiecheng;

import com.br.marketing.common.annoation.DecimalFieldConvertor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 携程日转化报表dto
 *
 * @author senyang.zheng
 * @date 2024/08/28
 */
@Data
@Schema(description = "携程日转化报表dto")
public class XiechengTransferDailyReportDTO implements Serializable {

    private static final long serialVersionUID = -224492148007765778L;
    @Schema(description = "日期")
    private String reportDate;

    @Schema(description = "当日运营量")
    private Long operateNum;

    @Schema(description = "当日登录量")
    private Long loginNum;

    @Schema(description = "当日身份认证量")
    private Long certifyNum;

    @Schema(description = "当日申请量")
    private Long applyNum;

    @Schema(description = "当日授信量")
    private Long creditNum;

    @Schema(description = "当日申请提现")
    private Long applyWithdrawNum;

    @Schema(description = "当日提现量")
    private Long withdrawNum;

    @Schema(description = "当日登录率")
    @DecimalFieldConvertor
    private BigDecimal loginRatio;

    @Schema(description = "当日身份认证率")
    @DecimalFieldConvertor
    private BigDecimal certifyRatio;

    @Schema(description = "当日申请率")
    @DecimalFieldConvertor
    private BigDecimal applyRatio;

    @Schema(description = "当日授信率")
    @DecimalFieldConvertor
    private BigDecimal creditRatio;

    @Schema(description = "当日申请提现率")
    @DecimalFieldConvertor
    private BigDecimal applyWithdrawRatio;

    @Schema(description = "当日提现率")
    @DecimalFieldConvertor
    private BigDecimal withdrawRatio;

    @Schema(description = "当日申请身份认证率")
    @DecimalFieldConvertor(scale = 2)
    private BigDecimal applyCertifyRatio;

    @Schema(description = "当日身份认证完成率")
    @DecimalFieldConvertor(scale = 2)
    private BigDecimal certifyCompleteRatio;

    @Schema(description = "当日过件率")
    @DecimalFieldConvertor(scale = 2)
    private BigDecimal overPieceRatio;

    @Schema(description = "当日提现发起率")
    @DecimalFieldConvertor(scale = 2)
    private BigDecimal withdrawLaunchRatio;

    @Schema(description = "当日提现成功率")
    @DecimalFieldConvertor(scale = 2)
    private BigDecimal withdrawSucRatio;

    @Schema(description = "当日收入")
    @DecimalFieldConvertor(scale = 2, isPercent = false)
    private BigDecimal income;

    @Schema(description = "当日成本")
    @DecimalFieldConvertor(scale = 2, isPercent = false)
    private BigDecimal cost;

    @Schema(description = "ROI")
    @DecimalFieldConvertor(scale = 2, isPercent = false)
    private BigDecimal roi;

    @Schema(description = "当日授信后提现发起")
    private Long creditWithdrawLaunchNum;

    @Schema(description = "当日授信后提现成功")
    private Long creditWithdrawSucNum;

    @Schema(description = "当日授信后发起提现率")
    @DecimalFieldConvertor(scale = 2)
    private BigDecimal creditWithdrawLaunchRatio;

    @Schema(description = "当日授信后提现成功率")
    @DecimalFieldConvertor(scale = 2)
    private BigDecimal creditWithdrawSucRatio;

    @Schema(description = "上报数据百万转化")
    private Long submitMillionTransferNum;

    @Schema(description = "外呼数据百万转化")
    private Long outboundMillionTransferNum;

}
