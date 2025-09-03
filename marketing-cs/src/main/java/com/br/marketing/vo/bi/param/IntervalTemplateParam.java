package com.br.marketing.vo.bi.param;

import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(value = "apiCode")
    private String apiCode;
    /**
     * 评分分布模板名称
     */
    @ApiModelProperty(value = "评分分布模板名称")
    private String templateName;

}
