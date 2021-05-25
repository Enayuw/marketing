package com.br.marketing.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ScoreDetailVo {

    @ApiModelProperty(value = "客户批次号")
    private String cusBatchNumber;

    @ApiModelProperty(value = "统计下载路径")
    private String statisticFilePath;
}
