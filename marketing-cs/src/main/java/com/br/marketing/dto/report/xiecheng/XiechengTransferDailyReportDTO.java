package com.br.marketing.dto.report.xiecheng;

import com.br.marketing.common.annoation.DecimalFieldConvertor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel(value = "携程日转化报表dto")
public class XiechengTransferDailyReportDTO implements Serializable {

    private static final long serialVersionUID = -224492148007765778L;
    @ApiModelProperty("日期")
    private String reportDate;

    @ApiModelProperty("当日运营量级")
    private Long operateNum;

    @ApiModelProperty("当日身份认证量级")
    private Long certifyNum;

    @ApiModelProperty("当日申请量级")
    private Long applyNum;

    @ApiModelProperty("当日授信量")
    private Long creditNum;

    @ApiModelProperty("当日申请提现")
    private Long applyWithdrawNum;

    @ApiModelProperty("当日提现量")
    private Long withdrawNum;

    @ApiModelProperty("当日身份认证率")
    @DecimalFieldConvertor
    private BigDecimal certifyRatio;

    @ApiModelProperty("当日申请率")
    @DecimalFieldConvertor
    private BigDecimal applyRatio;

    @ApiModelProperty("当日授信率")
    @DecimalFieldConvertor
    private BigDecimal creditRatio;

    @ApiModelProperty("当日申请提现率")
    @DecimalFieldConvertor
    private BigDecimal applyWithdrawRatio;

    @ApiModelProperty("当日提现率")
    @DecimalFieldConvertor
    private BigDecimal withdrawRatio;

    @ApiModelProperty("当日身份认证完成率")
    @DecimalFieldConvertor
    private BigDecimal certifyCompleteRatio;

    @ApiModelProperty("当日过件率")
    @DecimalFieldConvertor
    private BigDecimal overPieceRatio;

    @ApiModelProperty("当日提现发起率")
    @DecimalFieldConvertor
    private BigDecimal withdrawLaunchRatio;

    @ApiModelProperty("当日提现成功率")
    @DecimalFieldConvertor
    private BigDecimal withdrawSucRatio;

    @ApiModelProperty("当日收入")
    private BigDecimal income;

    @ApiModelProperty("当日成本")
    private BigDecimal cost;

    @ApiModelProperty("ROI")
    @DecimalFieldConvertor
    private BigDecimal roi;

    @ApiModelProperty("当日授信后提现发起")
    private Long creditWithdrawLaunchNum;

    @ApiModelProperty("当日授信后提现成功")
    private Long creditWithdrawSucNum;

    @ApiModelProperty("当日授信后提现发起率")
    @DecimalFieldConvertor
    private BigDecimal creditWithdrawLaunchRatio;

    @ApiModelProperty("授信后提现成功率")
    @DecimalFieldConvertor
    private BigDecimal creditWithdrawSucRatio;

    @ApiModelProperty("上报数据百万转化")
    private Long submitMillionTransferNum;

    @ApiModelProperty("外呼数据百万转化")
    private Long outboundMillionTransferNum;

}
