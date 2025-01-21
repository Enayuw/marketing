package com.br.marketing.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;


@Data
public class CarClueReportDTO {
    @NotNull(message = "Page number cannot be null")
    private Integer current;

    @NotNull(message = "Page size cannot be null")
    private Integer size;
    @ApiModelProperty(value = "品牌、车系、城市")
    private String search;
    @ApiModelProperty(value = "上传开始时间")
    private String createTimeStart;
    @ApiModelProperty(value = "上传结束时间")
    private String createTimeEnd;
    @ApiModelProperty(value = "外呼意向")
    private String intention;
    @ApiModelProperty(value = "线索状态")
    private Integer clueDataStatus;
    @ApiModelProperty(value = "修改开始时间")
    private String updateTimeStart;
    @ApiModelProperty(value = "修改结束时间")
    private String updateTimeEnd;
    @ApiModelProperty(value = "推送渠道")
    private String cluePushChannel;
    @ApiModelProperty(value = "推送状态")
    private Integer cluePushStatus;
    @ApiModelProperty(value = "推送开始时间")
    private String pushTimeStart;
    @ApiModelProperty(value = "推送结束时间")
    private String pushTimeEnd;
    @ApiModelProperty(value = "数据入库状态")
    private Integer status;
    @ApiModelProperty(value = "回调开始时间")
    private String callBackTimeStart;
    @ApiModelProperty(value = "回调结束时间")
    private String callBackTimeEnd;
}
