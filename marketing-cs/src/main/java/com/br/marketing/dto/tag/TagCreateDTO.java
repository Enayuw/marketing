package com.br.marketing.dto.tag;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 标签创建DTO
 */
@Data
public class TagCreateDTO extends TagRuleBaseDTO {
    @NotBlank(message = "标签名称不能为空")
    @ApiModelProperty(value = "标签名称", required = true)
    private String tagName;

    @NotNull(message = "时间范围不能为空")
    @ApiModelProperty(value = "时间范围数值", required = true)
    private Integer timeNumber;

    @NotBlank(message = "时间单位不能为空")
    @ApiModelProperty(value = "时间单位（d-天，m-月）", required = true)
    private String timeUnit;

    @NotEmpty(message = "标签条件不能为空")
    @ApiModelProperty(value = "标签条件列表", required = true)
    private List<TagConditionDTO> conditions;

    @ApiModelProperty(value = "条件关系（AND/OR）")
    private String operator = "AND";

    @NotEmpty(message = "用户范围不能为空")
    @ApiModelProperty(value = "用户范围APICode列表", required = true)
    private List<String> apiCodeScope;

    @ApiModelProperty(value = "标签授权APICode列表")
    private List<String> apiCodeLicense;

    @NotBlank(message = "数据源编码不能为空")
    @ApiModelProperty(value = "数据源编码", required = true)
    private String sourceCode;
}