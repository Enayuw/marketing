package com.br.marketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class RequestPushInfoDTO {

    @Schema(description = "商户编号")
    @NotNull(message = "商户编号不能为空")
    private String mApiCode;

    @Schema(description = "推送最小时间")
    @NotNull(message = "推送最小时间不能为空")
    private String pushBeginTime;

    @Schema(description = "推送最大时间")
    @NotNull(message = "推送最大时间不能为空")
    private String pushEndTime;

    @Schema(description = "客户批次号")
    private String cusBatchNumber;

    @Schema(description = "执行状态 1-执行中；2-执行成功；3-执行失败")
    private Integer mStatus;
}
