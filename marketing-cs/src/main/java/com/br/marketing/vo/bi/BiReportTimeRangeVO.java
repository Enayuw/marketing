package com.br.marketing.vo.bi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * BI报表数据时间范围VO
 *
 * @author senyang.zheng
 * @date 2024/08/28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BiReportTimeRangeVO {

    @Schema(description = "开始日期")
    private String requestStartDate;
    @Schema(description = "结束日期")
    private String requestEndDate;

}
