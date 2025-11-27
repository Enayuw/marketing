package com.br.marketing.dto.tag;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Schema(description = "标签批量删除DTO")
public class TagBatchDeleteDTO {
    @NotEmpty(message = "标签编码列表不能为空")
    @Schema(description = "标签编码列表", required = true)
    private List<String> tagCodes;

    @NotNull(message = "操作人账户名不能为空")
    @Schema(description = "当前用户ID", required = true)
    private Long currentUserId;
} 