package com.br.marketing.dto.report.xiecheng;

import com.br.marketing.common.annoation.DecimalFieldConvertor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 携程月接通转化报表dto
 * @author guangxiu.li
 * @date 2024/10/10 16:58
 */
@Data
@ApiModel(value = "携程月接通转化报表dto")
public class XiechengTransferMonthlySwitchOnReportDTO implements Serializable {

    private static final long serialVersionUID = -3827396898483783969L;
    @ApiModelProperty("日期")
    private String reportDate;

    @ApiModelProperty("锁定名单量")
    private Long lockNum;

    @ApiModelProperty("上报名单量")
    private Long submitNum;

    @ApiModelProperty("实际外呼量")
    private Long outboundNum;

    @ApiModelProperty("累计运营量")
    private Long operateNum;

    @ApiModelProperty("未去重累计接通量级")
    private Long callNum;

    @ApiModelProperty("去重累计接通量级")
    private Long distinctCallNum;

    @ApiModelProperty("身份认证量")
    private Long certifyNum;

    @ApiModelProperty("申请量")
    private Long applyNum;

    @ApiModelProperty("授信量")
    private Long creditNum;

    @ApiModelProperty("申请提现量")
    private Long applyWithdrawNum;

    @ApiModelProperty("提现成功量")
    private Long withdrawSucNum;

    @ApiModelProperty("日均授信量")
    private Long creditAvgNum;

    @ApiModelProperty("身份认证率")
    @DecimalFieldConvertor
    private BigDecimal certifyRatio;

    @ApiModelProperty("申请率")
    @DecimalFieldConvertor
    private BigDecimal applyRatio;

    @ApiModelProperty("授信率")
    @DecimalFieldConvertor
    private BigDecimal creditRatio;

    @ApiModelProperty("申请提现率")
    @DecimalFieldConvertor
    private BigDecimal applyWithdrawRatio;

    @ApiModelProperty("提现率")
    @DecimalFieldConvertor
    private BigDecimal withdrawRatio;

    @ApiModelProperty("身份认证完成率")
    @DecimalFieldConvertor(scale = 2)
    private BigDecimal certifyCompleteRatio;

    @ApiModelProperty("过件率")
    @DecimalFieldConvertor(scale = 2)
    private BigDecimal overPieceRatio;

    @ApiModelProperty("授信后提现发起率")
    @DecimalFieldConvertor(scale = 2)
    private BigDecimal creditWithdrawLaunchRatio;

    @ApiModelProperty("授信后提现成功率")
    @DecimalFieldConvertor(scale = 2)
    private BigDecimal creditWithdrawSucRatio;

    @ApiModelProperty("申请授信量2")
    private Long applyCreditNum2;

    @ApiModelProperty("提现成功量2")
    private Long withdrawSucNum2;

    @ApiModelProperty("申请提现率2")
    @DecimalFieldConvertor
    private BigDecimal applyWithdrawRatio2;

    @ApiModelProperty("提现率2")
    @DecimalFieldConvertor
    private BigDecimal withdrawRatio2;

    @ApiModelProperty("提现发起率")
    @DecimalFieldConvertor(scale = 2)
    private BigDecimal withdrawLaunchRatio;

    @ApiModelProperty("提现成功率")
    @DecimalFieldConvertor(scale = 2)
    private BigDecimal withdrawSucRatio;

    @ApiModelProperty("总收入")
    @DecimalFieldConvertor(scale = 2, isPercent = false)
    private BigDecimal income;

    @ApiModelProperty("总成本")
    @DecimalFieldConvertor(scale = 2, isPercent = false)
    private BigDecimal cost;

    @ApiModelProperty("ROI")
    @DecimalFieldConvertor(scale = 2, isPercent = false)
    private BigDecimal roi;

    @ApiModelProperty("授信目标完成率")
    @DecimalFieldConvertor(scale = 2)
    private BigDecimal creditCompleteRatio;
}
