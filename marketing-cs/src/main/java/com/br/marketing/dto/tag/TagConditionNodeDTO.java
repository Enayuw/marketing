package com.br.marketing.dto.tag;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "标签条件节点DTO")
public class TagConditionNodeDTO {
    
    @Schema(description = "节点类型（CONDITION-叶子条件节点，GROUP-条件组节点）", required = true)
    private String type;
    
    @Schema(description = "条件关系（AND-且，OR-或），仅当type=GROUP时有效")
    private String operator;
    
    @Schema(description = "子节点列表，仅当type=GROUP时有效")
    private List<TagConditionNodeDTO> children;
    
    @Schema(description = "字段编码，仅当type=CONDITION时有效")
    private String fieldCode;
    
    @Schema(description = "字段名称，仅当type=CONDITION时有效")
    private String fieldName;
    
    @Schema(description = "字段类型，仅当type=CONDITION时有效")
    private String fieldType;
    
    @Schema(description = "操作符，仅当type=CONDITION时有效")
    private String operation;
    
    @Schema(description = "字段值，仅当type=CONDITION时有效")
    private String value;
    
    @Schema(description = "子条件，仅当需要添加子条件时有效")
    private TagConditionNodeDTO subCondition;
    
    @Schema(description = "计算操作符（+、-），仅当字段类型为数字或日期时有效")
    private String calcOperator;
    
    @Schema(description = "计算值，仅当设置了计算操作符时有效")
    private String calcValue;
    
    @Schema(description = "计算值单位，仅当设置了计算操作符时有效")
    private String calcUnit;
} 