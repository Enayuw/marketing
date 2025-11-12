package com.br.marketing.vo.bi.param;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * BI报表查询参数
 *
 * @author senyang.zheng
 * @date 2024/08/28
 */
@Data
@Schema(description = "BI报表配置字典请求参数")
public class BiReportConfigDictParam {

    @Schema(description = "字典key")
    private String dictKey;

    @Schema(description = "apiCode")
    private String apiCode;

    @Schema(description = "字典值")
    private String dictValue;

    @Schema(description = "字典描述")
    @JsonProperty(value = "dictDesc")
    private String dictDesc;

    @Schema(description = "配置开始时间")
    private Date startDate;

    @Schema(description = "配置结束时间")
    private Date endDate;

    @Schema(description = "1-有效；9-无效")
    private Integer isDel;
}
