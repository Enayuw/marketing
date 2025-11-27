package com.br.marketing.dto.tag;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 更新标签状态DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTagStatusDTO {
    @Schema(description = "标签编码", required = true)
    private String tagCode;

    @Schema(description = "状态（1-启用, 0-禁用）", required = true)
    private Integer status;

    @Schema(description = "操作人ID", required = true)
    private Long optUserId;
}