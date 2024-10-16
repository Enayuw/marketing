package com.br.marketing.dto.report.zhongan;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName ZhongAnControlGroupDTO
 * @Description TODO
 * @Author kongbx
 * @Date 2024/9/18 16:57
 */
@Data
@ApiModel(value = "众安对照组配置")
public class ZhongAnControlGroupDTO implements Serializable {

    @ApiModelProperty("数据日期")
    private String reportDate;
    @ApiModelProperty("场景")
    private Integer userType;
    @ApiModelProperty("组别")
    private Integer constituencies;
    @ApiModelProperty("总数据量")
    private Integer totalNum;
    @ApiModelProperty("进件人数")
    private Integer incomingNum;
    @ApiModelProperty("批核人数")
    private Integer approversNum;
    @ApiModelProperty("批核件均")
    private Integer approvalAvailable;
    @ApiModelProperty("登录率")
    private BigDecimal loginRate;
    @ApiModelProperty("发起提现人数")
    private Integer applyPayNum;
    @ApiModelProperty("提现通过通过率")
    private BigDecimal payPassRate;
    @ApiModelProperty("放款成功金额")
    private BigDecimal lendersSucAmount;
    @ApiModelProperty("放款成功人数")
    private Integer lendersSucNum;

}
