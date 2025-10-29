package com.br.marketing.dto.rulecenter;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class XcDeleteMagnitudeDistDTO {

    @ApiModelProperty(value = "releaseTime开始时间")
    @NotNull(message = "releaseTime开始时间不能为空")
    private LocalDateTime releaseTimeBegin;

    @ApiModelProperty(value = "releaseTime结束时间")
    @NotNull(message = "releaseTime结束时间不能为空")
    private LocalDateTime releaseTimeEnd;

    @ApiModelProperty(value = "剔除量级")
    private Long deleteNum;

    @ApiModelProperty(value = "剩余周期量级")
    private Long remainingNum;

    @ApiModelProperty(value = "空挡量级")
    private Long freeNum;
}
