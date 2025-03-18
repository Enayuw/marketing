package com.br.marketing.dto.tag;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 标签规则基础DTO
 * @author your.name
 * @date 2024/1/x
 */
@Data
@ApiModel("标签规则基础DTO")
public class TagRuleBaseDTO {

    @ApiModelProperty("标签名称")
    @NotEmpty(message = "标签名称不能为空")
    private String tagName;

    @ApiModelProperty("时间范围")
    @NotNull(message = "时间范围不能为空")
    private Integer timeNumber;

    @ApiModelProperty("时间单位")
    @NotEmpty(message = "时间单位不能为空")
    private String timeUnit;

    @ApiModelProperty("规则条件列表")
    @NotEmpty(message = "规则条件不能为空")
    private List<TagConditionDTO> conditions;

    @ApiModelProperty("条件组合方式")
    @NotEmpty(message = "条件组合方式不能为空")
    private String operator;

    @ApiModelProperty("数据源范围")
    @NotEmpty(message = "数据源范围不能为空")
    private List<String> apiCodeScope;

    @ApiModelProperty("数据源授权")
    private List<String> apiCodeLicense;
}