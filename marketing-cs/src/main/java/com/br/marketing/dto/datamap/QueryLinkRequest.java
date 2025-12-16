package com.br.marketing.dto.datamap;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 查询链路详情
 * 
 * @author Austin
 * @since 2025/10/16
 */
@Data
@Schema(description = "查询链路详情")
public class QueryLinkRequest {

    @NotNull(message = "链路ID不能为空")
    @Schema(description = "链路id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long linkId;

    @Schema(description = "开始日期，格式：yyyy-MM-dd，不传则默认为当天")
    private String startDate;

    @Schema(description = "结束日期，格式：yyyy-MM-dd，不传则默认为当天")
    private String endDate;

}


