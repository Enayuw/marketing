package com.br.marketing.dto.rulecenter;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class XcDeleteMagnitudeDistDTO {

    @ApiModelProperty(value = "releaseTime开始时间")
    @NotNull(message = "releaseTime开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime releaseTimeBegin;

    @ApiModelProperty(value = "releaseTime结束时间")
    @NotNull(message = "releaseTime结束时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime releaseTimeEnd;

    @ApiModelProperty(value = "剔除量级")
    private Integer deleteNum;

    @ApiModelProperty(value = "剩余周期量级")
    private Integer remainingNum;

    @ApiModelProperty(value = "空挡量级")
    private Integer freeNum;
}
