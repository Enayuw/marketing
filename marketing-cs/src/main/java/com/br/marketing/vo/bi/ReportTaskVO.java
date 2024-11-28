package com.br.marketing.vo.bi;

import com.br.marketing.entity.ReportTask;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 报告任务vo
 *
 * @author senyang.zheng
 * @date 2024/08/19
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ReportTaskVO extends ReportTask {
    @ApiModelProperty(value = "apiCode")
    private String apiCodes;
    @ApiModelProperty(value = "跑分文件")
    private String batchNumbers;
    @ApiModelProperty(value = "报表模型")
    private List<AxisWrapVO> axisWrapVOS;
}
