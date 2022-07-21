package com.br.marketing.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class StatisticsDataDayVO {
    @ApiModelProperty(value = "日期")
    private String day;

    @ApiModelProperty(value = "统计记录id")
    private Long id;

    @ApiModelProperty(value = "数量")
    private Integer num;
}
