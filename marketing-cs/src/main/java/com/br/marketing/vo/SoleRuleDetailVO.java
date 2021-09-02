package com.br.marketing.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import java.util.List;

/**
 * 页面新增去重规则 请求参数
 */
@Data
public class SoleRuleDetailVO {

    @ApiModelProperty(value = "去重规则id")
    private Long id;

    @NotBlank(message = "名称必填")
    @ApiModelProperty(value = "去重规则名称")
    private String soleName;

    @NotBlank(message = "字段必选")
    @ApiModelProperty(value = "去重字段")
    private String soleFields;

    @ApiModelProperty(value = "去重时间周期(T-n)")
    private String soleCycleTimes;

    @ApiModelProperty(value = "匹配商户列表")
    private List<CustUserTypeSelect> soleCustom;
}
