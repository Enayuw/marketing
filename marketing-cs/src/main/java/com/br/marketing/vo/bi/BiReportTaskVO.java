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
}
