package com.br.marketing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class StatisticsDataDayVO {
    @Schema(description = "日期")
    private String day;

    @Schema(description = "统计记录id")
    private Long id;

    @Schema(description = "数量")
    private Integer num;
}
