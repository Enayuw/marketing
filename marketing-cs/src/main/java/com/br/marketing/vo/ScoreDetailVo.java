package com.br.marketing.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ScoreDetailVo {

    @ApiModelProperty(value = "客户批次号")
    private String cusBatchNumber;

    @ApiModelProperty(value = "内部客户批次号")
    private String batchNumber;

    @ApiModelProperty(value = "跑分文件id")
    private Long fileId;

    @ApiModelProperty(value = "统计下载路径")
    private String statisticFilePath;
}
