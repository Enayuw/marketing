package com.br.marketing.dto.report.xiecheng;

import com.br.marketing.common.annoation.DecimalFieldConvertor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel(value = "携程7日滚动转化报表dto")
public class XiechengTransferWeeklyReportDTO implements Serializable {

    private static final long serialVersionUID = 1049740263038976458L;

    @ApiModelProperty("日期")
    private String rollPeriod;

    @ApiModelProperty("实际外呼量级")
    private Long outboundNum;

    @ApiModelProperty("身份认证量")
    private Long certifyNum;

    @ApiModelProperty("申请量")
    private Long applyNum;

    @ApiModelProperty("授信量")
    private Long creditNum;

    @ApiModelProperty("申请提现量")
    private Long applyWithdrawNum;

    @ApiModelProperty("提现量")
    private Long withdrawNum;

    @ApiModelProperty("期均授信量")
    private Long creditAvgNum;

    @ApiModelProperty("身份认证率")
    @DecimalFieldConvertor(scale = 2)
    private BigDecimal certifyRatio;

    @ApiModelProperty("申请率")
    @DecimalFieldConvertor(scale = 2)
    private BigDecimal applyRatio;

    @ApiModelProperty("授信率")
    @DecimalFieldConvertor(scale = 2)
    private BigDecimal creditRatio;

    @ApiModelProperty("提现率")
    @DecimalFieldConvertor(scale = 2)
    private BigDecimal withdrawRatio;

    @ApiModelProperty("身份认证完成率")
    @DecimalFieldConvertor(scale = 2)
    private BigDecimal certifyCompleteRatio;

    @ApiModelProperty("过件率")
    @DecimalFieldConvertor(scale = 2)
    private BigDecimal overPieceRatio;

    @ApiModelProperty("提现成功率（授信后提现）")
    @DecimalFieldConvertor(scale = 2)
    private BigDecimal withdrawSucRatio;
}
