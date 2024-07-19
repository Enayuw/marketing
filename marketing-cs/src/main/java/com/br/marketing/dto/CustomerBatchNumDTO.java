package com.br.marketing.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class CustomerBatchNumDTO {

    @ApiModelProperty(value = "商户编号")
    @NotNull(message = "商户编个号不能为空")
    private String apiCode;

    @ApiModelProperty(value = "产品名称")
    private String productName;

    @ApiModelProperty(value = "产品版本")
    private String productVersion;

    @ApiModelProperty(value = "上传开始时间")
    private String uploadBeginTime;

    @ApiModelProperty(value = "上传结束时间")
    private String uploadEndTime;

    @ApiModelProperty(value = "跑分时间区间，多段")
    private List<ScoreTimeDTO> scoreTimeList;

    @ApiModelProperty(value = "场景")
    private String userType;

    private Integer current;

    private Integer size;

    private List<String> moduleList;



}
