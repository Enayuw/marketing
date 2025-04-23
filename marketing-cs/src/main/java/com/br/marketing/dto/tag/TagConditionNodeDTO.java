package com.br.marketing.dto.tag;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "标签条件节点DTO")
public class TagConditionNodeDTO {
    
    @ApiModelProperty(value = "节点类型（CONDITION-叶子条件节点，GROUP-条件组节点）", required = true)
    private String type;
    
    @ApiModelProperty(value = "条件关系（AND-且，OR-或），仅当type=GROUP时有效")
    private String operator;
    
    @ApiModelProperty(value = "子节点列表，仅当type=GROUP时有效")
    private List<TagConditionNodeDTO> children;
    
    @ApiModelProperty(value = "字段编码，仅当type=CONDITION时有效")
    private String fieldCode;
    
    @ApiModelProperty(value = "字段名称，仅当type=CONDITION时有效")
    private String fieldName;
    
    @ApiModelProperty(value = "字段类型，仅当type=CONDITION时有效")
    private String fieldType;
    
    @ApiModelProperty(value = "操作符，仅当type=CONDITION时有效")
    private String operation;
    
    @ApiModelProperty(value = "字段值，仅当type=CONDITION时有效")
    private String value;
    
    @ApiModelProperty(value = "子条件，仅当需要添加子条件时有效")
    private TagConditionNodeDTO subCondition;
    
    @ApiModelProperty(value = "计算操作符（+、-），仅当字段类型为数字或日期时有效")
    private String calcOperator;
    
    @ApiModelProperty(value = "计算值，仅当设置了计算操作符时有效")
    private String calcValue;
    
    @ApiModelProperty(value = "计算值单位，仅当设置了计算操作符时有效")
    private String calcUnit;
} 