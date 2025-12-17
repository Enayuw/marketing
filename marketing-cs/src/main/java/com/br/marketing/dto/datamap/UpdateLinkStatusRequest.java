package com.br.marketing.dto.datamap;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 更新链路状态请求
 * 
 * @author Austin
 * @since 2025/10/16
 */
@Data
@Schema(description = "更新链路状态请求")
public class UpdateLinkStatusRequest {

    @Schema(description = "链路ID列表")
    @NotNull(message = "链路ID列表不能为空")
    private List<Long> ids;

    @Schema(description = "状态（0-禁用 1-启用）")
    @NotNull(message = "状态不能为空")
    private Integer status;
}


