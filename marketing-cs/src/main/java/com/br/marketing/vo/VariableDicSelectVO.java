package com.br.marketing.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 客户配置变量值字典 下拉列表vo
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/9/1 17:49
 */
@Setter
@Getter
@NoArgsConstructor
@ApiModel(value = "客户配置变量值字典")
@AllArgsConstructor
public class VariableDicSelectVO {

    /**
     * 字段名称
     */
    @ApiModelProperty(value = "字段名称", dataType = "string", position = 1)
    private String fieldName;

    /**
     * 字段值
     */
    @ApiModelProperty(value = "字段值", dataType = "string", position = 2)
    private String fieldValue;

    /**
     * 字段描述
     */
    @ApiModelProperty(value = "字段描述", dataType = "string", position = 3)
    private String fieldDesc;
}
