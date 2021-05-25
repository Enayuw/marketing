package com.br.marketing.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class RequestPushInfoDTO {

    @ApiModelProperty(value = "商户编号")
    @NotNull(message = "商户编号不能为空")
    private String mApiCode;

    @ApiModelProperty(value = "推送最小时间")
    @NotNull(message = "推送最小时间不能为空")
    private String pushBeginTime;

    @ApiModelProperty(value = "推送最大时间")
    @NotNull(message = "推送最大时间不能为空")
    private String pushEndTime;

    @ApiModelProperty(value = "客户批次号")
    private String cusBatchNumber;

    @ApiModelProperty(value = "执行状态 1-执行中；2-执行成功；3-执行失败")
    private Integer mStatus;
}
