package com.br.marketing.dto.rulecenter;

import com.br.marketing.common.commondto.PageSearchDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class XcDeleteTaskQueryDTO extends PageSearchDTO {

    @Schema(description = "releaseTime开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private String releaseTimeBegin;

    @Schema(description = "releaseTime结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private String releaseTimeEnd;

    @Schema(description = "任务类型")
    private Integer taskType;

    @Schema(description = "任务状态")
    private Integer taskStatus;

}
