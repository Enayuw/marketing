package com.br.marketing.dto.tag;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;

@Data
@Schema(description = "标签条件树DTO")
public class TagConditionTreeDTO {
    
    @Schema(description = "条件关系（AND-且，OR-或）")
    private String operator = "AND";
    
    @NotEmpty(message = "条件列表不能为空")
    @Schema(description = "子条件列表", required = true)
    private List<TagConditionNodeDTO> children;
} 