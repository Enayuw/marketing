package com.br.marketing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class SoleRuleVO {

    @Schema(description = "去重规则id")
    private Long id;

    @Schema(description = "去重规则名称")
    private String soleName;

    @Schema(description = "去重字段")
    private String soleFields;

    @Schema(description = "去重字段统计")
    private Integer soleFieldsNum;

    @Schema(description = "去重时间周期")
    private Integer soleCycleTimes;

    @Schema(description = "使用商户统计")
    private Integer cusNum;

    @Schema(description = "apicodes")
    private List<String> apicodes;

    @Schema(description = "开启状态")
    private Integer status;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "修改时间")
    private String updateTime;
}
