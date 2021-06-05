package com.br.marketing.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CustomerBatchNumDTO {

    @ApiModelProperty(value = "商户编号")
    @NotNull(message = "商户编个号不能为空")
    private String apiCode;

    @ApiModelProperty(value = "产品名称")
    @NotNull(message = "产品名称不能为空")
    private String productName;

    @ApiModelProperty(value = "产品版本")
    @NotNull(message = "产品版本不能为空")
    private String productVersion;

    @ApiModelProperty(value = "上传开始时间")
    @NotNull(message = "上传开始时间不能为空")
    private String uploadBeginTime;

    @ApiModelProperty(value = "上传结束时间")
    @NotNull(message = "上传结束时间不能为空")
    private String uploadEndTime;

    @ApiModelProperty(value = "跑分执行开始时间")
    @NotNull(message = "跑分执行开始时间不能为空")
    private String scoreBeginTime;

    @ApiModelProperty(value = "跑分执行结束时间")
    @NotNull(message = "跑分执行结束时间不能为空")
    private String scoreEndTime;
}
