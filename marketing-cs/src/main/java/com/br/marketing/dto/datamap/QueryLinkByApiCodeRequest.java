package com.br.marketing.dto.datamap;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 根据apiCode查询链路详情列表请求
 * 
 * @author Austin
 * @since 2025/10/16
 */
@Data
@Schema(description = "根据apiCode查询链路详情列表请求")
public class QueryLinkByApiCodeRequest {

    @NotNull(message = "apiCode不能为空")
    @Schema(description = "apiCode", requiredMode = Schema.RequiredMode.REQUIRED)
    private String apiCode;

    @Schema(description = "来源：MANUAL/AUTO")
    private String sourceType;

    @Schema(description = "开始日期，格式：yyyy-MM-dd，不传则默认为当天")
    private String startDate;

    @Schema(description = "结束日期，格式：yyyy-MM-dd，不传则默认为当天")
    private String endDate;

}

