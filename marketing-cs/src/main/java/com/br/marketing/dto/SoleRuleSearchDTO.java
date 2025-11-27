package com.br.marketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
public class SoleRuleSearchDTO {

    @Schema(description = "去重规则名称")
    private String soleName;

    @Schema(description = "开启状态;(1-开启;2-禁用;不传查全部)")
    private Integer status;

    @Schema(description = "开始创建时间")
    private String createTimeStart;

    @Schema(description = "结束创建时间")
    private String createTimeEnd;

    @Schema(description = "开始变更时间")
    private String updateTimeStart;

    @Schema(description = "结束变更时间")
    private String updateTimeEnd;

}
