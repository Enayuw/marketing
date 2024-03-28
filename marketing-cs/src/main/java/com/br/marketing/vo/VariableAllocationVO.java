package com.br.marketing.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class VariableAllocationVO {

    @ApiModelProperty(value = "任务流水号")
    private Long id;

    @ApiModelProperty(value = "apicode")
    private String apiCode;

    @ApiModelProperty(value = "配置类型")
    private String allocationType;

    @ApiModelProperty(value = "配置值")
    private String allocationValue;

    @ApiModelProperty(value = "撞得总量级")
    private Integer normalQuantity;

    @ApiModelProperty(value = "异常总量级")
    private Integer abnormalQuantity;

    @ApiModelProperty(value = "即将撞库量级")
    private Integer releaseTimeNum;

    @ApiModelProperty(value = "可补充的量级")
    private Integer falseNum;

    @ApiModelProperty(value = "请求时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date requestTime;

    @ApiModelProperty(value = "请求结束时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date requestEndTime;

    @Override
    public String toString() {
        return "VariableAllocationVO{" +
                "id=" + id +
                ", apiCode='" + apiCode + '\'' +
                ", allocationType='" + allocationType + '\'' +
                ", allocationValue='" + allocationValue + '\'' +
                ", normalQuantity=" + normalQuantity +
                ", abnormalQuantity=" + abnormalQuantity +
                ", releaseTimeNum=" + releaseTimeNum +
                ", falseNum=" + falseNum +
                ", requestTime='" + requestTime +
                ", requestTime='" + requestEndTime + '\'' +
                '}';
    }
}
