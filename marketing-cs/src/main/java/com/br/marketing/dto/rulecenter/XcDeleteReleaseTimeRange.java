package com.br.marketing.dto.rulecenter;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class XcDeleteReleaseTimeRange {

    @ApiModelProperty(value = "releaseTime开始时间")
    private LocalDateTime releaseTimeBegin;

    @ApiModelProperty(value = "releaseTime结束时间")
    private LocalDateTime releaseTimeEnd;

}
