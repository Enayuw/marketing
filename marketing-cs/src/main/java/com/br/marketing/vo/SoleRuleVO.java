package com.br.marketing.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class SoleRuleVO {

    @ApiModelProperty(value = "去重规则id")
    private Long id;

    @ApiModelProperty(value = "去重规则名称")
    private String soleName;

    @ApiModelProperty(value = "去重字段")
    private String soleFields;

    @ApiModelProperty(value = "去重字段统计")
    private Integer soleFieldsNum;

    @ApiModelProperty(value = "去重时间周期")
    private Integer soleCycleTimes;

    @ApiModelProperty(value = "使用商户统计")
    private Integer cusNum;

    @ApiModelProperty(value = "开启状态")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "修改时间")
    private String updateTime;
}
