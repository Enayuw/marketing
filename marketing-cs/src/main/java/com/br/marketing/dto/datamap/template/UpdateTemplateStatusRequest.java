package com.br.marketing.dto.datamap.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 更新模板状态请求
 * 
 * @author bingxu.kong
 * @since 2025/01/27
 */
@Data
@Schema(description = "更新模板状态请求")
public class UpdateTemplateStatusRequest {

    @NotEmpty(message = "模板ID列表不能为空")
    @Schema(description = "模板ID列表")
    private List<Long> ids;

    @NotNull(message = "状态不能为空")
    @Schema(description = "状态（0-禁用 1-启用）")
    private Byte status;
}
