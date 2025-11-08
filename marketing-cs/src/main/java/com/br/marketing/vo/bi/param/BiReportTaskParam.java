package com.br.marketing.vo.bi.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class BiReportTaskParam {

    @Schema(description = "current")
    private Integer current;

    @Schema(description = "size")
    private Integer size;

    @Schema(description = "apiCode")
    private String apiCode;

    /**
     * 报表类型
     */
    @Schema(description = "报表类型")
    private String reportTypeName;

    /**
     * 报告名称
     */
    @Schema(description = "报告名称")
    private String reportName;

    /**
     * 场景
     */
    @Schema(description = "场景")
    private String userType;

    /**
     * 分组维度
     */
    @Schema(description = "分组维度")
    private String dimensionsField;

    /**
     * 报表类型,将reportTypeName转为reportType,进行mapper查询字段
     */
    private Integer reportType;


}
