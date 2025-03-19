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
    
    @ApiModelProperty(value = "字段名称，仅当type=CONDITION时有效")
    private String field;
    
    @ApiModelProperty(value = "操作符（等于、不等于、大于等），仅当type=CONDITION时有效")
    private String operation;
    
    @ApiModelProperty(value = "字段值，仅当type=CONDITION时有效")
    private String value;
} 