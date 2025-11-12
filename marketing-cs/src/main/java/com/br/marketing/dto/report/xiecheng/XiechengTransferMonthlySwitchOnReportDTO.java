package com.br.marketing.dto.report.xiecheng;

import com.br.marketing.common.annoation.DecimalFieldConvertor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 携程月接通转化报表dto
 * @author guangxiu.li
 * @date 2024/10/10 16:58
 */
@Data
@Schema(description = "携程月接通转化报表dto")
public class XiechengTransferMonthlySwitchOnReportDTO implements Serializable {

    private static final long serialVersionUID = -3827396898483783969L;
    @Schema(description = "日期")
    private String reportDate;

    @Schema(description = "锁定名单量")
    private Long lockNum;

    @Schema(description = "上报名单量")
    private Long submitNum;

    @Schema(description = "实际外呼量")
    private Long outboundNum;

    @Schema(description = "累计运营量")
    private Long operateNum;

    @Schema(description = "未去重累计接通量级")
    private Long callNum;

    @Schema(description = "去重累计接通量级")
    private Long distinctCallNum;

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

    @Schema(description = "提现成功量")
    private Long withdrawSucNum;

    @Schema(description = "日均授信量")
    private Long creditAvgNum;

    @Schema(description = "登录率")
    @DecimalFieldConvertor
    private BigDecimal loginRatio;

    @Schema(description = "身份认证率")
    @DecimalFieldConvertor
    private BigDecimal certifyRatio;

    @Schema(description = "申请率")
    @DecimalFieldConvertor
    private BigDecimal applyRatio;

    @Schema(description = "授信率")
    @DecimalFieldConvertor
    private BigDecimal creditRatio;

    @Schema(description = "申请提现率")
    @DecimalFieldConvertor
    private BigDecimal applyWithdrawRatio;

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

    @Schema(description = "授信后提现发起率")
    @DecimalFieldConvertor(scale = 2)
    private BigDecimal creditWithdrawLaunchRatio;

    @Schema(description = "授信后提现成功率")
    @DecimalFieldConvertor(scale = 2)
    private BigDecimal creditWithdrawSucRatio;

    @Schema(description = "申请提现量2")
    private Long applyCreditNum2;

    @Schema(description = "提现成功量2")
    private Long withdrawSucNum2;

    @Schema(description = "申请提现率2")
    @DecimalFieldConvertor
    private BigDecimal applyWithdrawRatio2;

    @Schema(description = "提现率2")
    @DecimalFieldConvertor
    private BigDecimal withdrawRatio2;

    @Schema(description = "提现发起率")
    @DecimalFieldConvertor(scale = 2)
    private BigDecimal withdrawLaunchRatio;

    @Schema(description = "提现成功率")
    @DecimalFieldConvertor(scale = 2)
    private BigDecimal withdrawSucRatio;

    @Schema(description = "总收入")
    @DecimalFieldConvertor(scale = 2, isPercent = false)
    private BigDecimal income;

    @Schema(description = "总成本")
    @DecimalFieldConvertor(scale = 2, isPercent = false)
    private BigDecimal cost;

    @Schema(description = "ROI")
    @DecimalFieldConvertor(scale = 2, isPercent = false)
    private BigDecimal roi;

}
