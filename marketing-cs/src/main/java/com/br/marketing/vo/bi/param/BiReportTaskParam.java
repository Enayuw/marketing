package com.br.marketing.vo.bi.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BiReportTaskParam {

    @ApiModelProperty(value = "apiCode")
    private String apiCode;

    /**
     * 报表类型
     */
    @ApiModelProperty(value = "报表类型")
    private String reportTypeName;

    /**
     * 报告名称
     */
    @ApiModelProperty(value = "报告名称")
    private String reportName;

    /**
     * 场景
     */
    @ApiModelProperty(value = "场景")
    private String userType;

    /**
     * 分组维度
     */
    @ApiModelProperty(value = "分组维度")
    private String dimensionsField;

    /**
     * 报表类型,将reportTypeName转为reportType,进行mapper查询字段
     */
    private Integer reportType;


}
