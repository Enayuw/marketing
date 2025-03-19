package com.br.marketing.dto.tag;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "标签字段分类DTO")
public class TagFieldCategoryDTO {
    
    @ApiModelProperty(value = "分类编码")
    private String categoryCode;
    
    @ApiModelProperty(value = "分类名称")
    private String categoryName;
    
    @ApiModelProperty(value = "字段配置列表")
    private List<TagFieldConfigDTO> fields;
} 