package com.br.marketing.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class VariableAllocationVO {

    @Schema(description = "任务流水号")
    private Long id;

    @Schema(description = "apicode")
    private String apiCode;

    @Schema(description = "配置类型")
    private String allocationType;

    @Schema(description = "配置值")
    private String allocationValue;

    @Schema(description = "配置值Map")
    private Map<String, Object> allocationValueMap;

    @Schema(description = "撞得总量级")
    private Integer normalQuantity;

    @Schema(description = "异常总量级")
    private Integer abnormalQuantity;

    @Schema(description = "即将撞库量级")
    private Integer releaseTimeNum;

    @Schema(description = "可补充的量级")
    private Integer falseNum;

    @Schema(description = "请求时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date requestTime;

    @Schema(description = "请求结束时间")
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
