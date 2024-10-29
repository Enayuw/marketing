package com.br.marketing.vo.zhongan.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName ZhongAnCustomInfoVO
 * @Author kongbx
 * @Date 2024/9/18 16:18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZhongAnCustomInfo {

    @ApiModelProperty(value = "组别")
    private Integer constituencies;
    @ApiModelProperty(value = "总数据量")
    private Integer totalNum;
    @ApiModelProperty(value = "进件人数")
    private Integer incomingNum;
    @ApiModelProperty(value = "批核人数")
    private Integer approversNum;
    @ApiModelProperty(value = "登录率")
    private String loginRate;
    @ApiModelProperty(value = "批核件均")
    private Integer approvalAvailable;
    @ApiModelProperty(value = "发起提现人数")
    private Integer applyPayNum;
    @ApiModelProperty(value = "提现通过通过率")
    private String payPassRate;
    @ApiModelProperty(value = "放款成功人数")
    private Integer lendersSucNum;
    @ApiModelProperty(value = "放款成功金额")
    private String lendersSucAmount;
}
