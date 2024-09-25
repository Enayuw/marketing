package com.br.marketing.vo.bi;

import com.br.marketing.entity.ReportTask;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 报告任务vo
 *
 * @author senyang.zheng
 * @date 2024/08/19
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BiReportTaskVO extends ReportTask {

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
     * 报表类型
     */
    @ApiModelProperty(value = "报表类型")
    private String reportTypeName;

    /**
     * 跑分文件
     */
    @ApiModelProperty(value = "跑分文件")
    private String batchNumber;

    /**
     * 转化数据开始日期
     */
    @ApiModelProperty(value = "转化数据开始日期")
    private String requestStartDate;

    /**
     * 转化数据结束日期
     */
    @ApiModelProperty(value = "转化数据结束日期")
    private String requestEndDate;

    /**
     * 转化数据生成范围
     */
    @ApiModelProperty(value = "报表数据生成范围")
    private String transferDateTimeRange;
}
