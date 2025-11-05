package com.br.marketing.dto.rulecenter;

import com.br.marketing.common.commondto.PageSearchDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class XcDeleteTaskQueryDTO extends PageSearchDTO {

    @ApiModelProperty(value = "releaseTime开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private String releaseTimeBegin;

    @ApiModelProperty(value = "releaseTime结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private String releaseTimeEnd;

    @ApiModelProperty(value = "任务类型")
    private Integer taskType;

    @ApiModelProperty(value = "任务状态")
    private Integer taskStatus;

}
