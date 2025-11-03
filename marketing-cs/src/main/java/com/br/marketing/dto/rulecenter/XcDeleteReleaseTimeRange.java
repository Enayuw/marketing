package com.br.marketing.dto.rulecenter;

import com.br.marketing.common.commondto.PageSearchDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class XcDeleteReleaseTimeRange extends PageSearchDTO {

    @ApiModelProperty(value = "releaseTime开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime releaseTimeBegin;

    @ApiModelProperty(value = "releaseTime结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime releaseTimeEnd;

}
