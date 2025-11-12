package com.br.marketing.dto.tag;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "标签创建人DTO")
public class TagCreatorDTO {
    @Schema(description = "创建人ID")
    private Long userId;

    @Schema(description = "创建人名称")
    private String userName;
} 