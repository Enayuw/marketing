package com.br.marketing.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class MarketingPreUserSyncDetailVO {

    @ApiModelProperty(value = "apicode")
    private String apiCode;

    @ApiModelProperty(value = "任务id")
    private String taskId;

    @ApiModelProperty(value = "请求批次id")
    private String requestId;

    @ApiModelProperty(value = "同步状态1-进行中；2-全部成功；3-全部失败；4-部分成功")
    private Integer status;

    @ApiModelProperty(value = "错误信息")
    private String errorInfo;
}
