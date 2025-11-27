package com.br.marketing.vo.bi;

import com.br.marketing.entity.ReportTask;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "apiCode")
    private String apiCodes;
    @Schema(description = "跑分文件")
    private String batchNumbers;
    @Schema(description = "报表模型")
    private List<AxisWrapVO> axisWrapVOS;
}
