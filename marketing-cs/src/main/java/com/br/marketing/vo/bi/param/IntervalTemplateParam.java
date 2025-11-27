package com.br.marketing.vo.bi.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @ClassName IntervalTemplateParam
 * @Description 评分分布模板
 * @Author kongbx
 * @Date 2025/8/6 18:01
 */
@Data
public class IntervalTemplateParam {
    /**
     * apiCode
     */
    @Schema(description = "apiCode")
    private String apiCode;
    /**
     * 评分分布模板名称
     */
    @Schema(description = "评分分布模板名称")
    private String templateName;

}
