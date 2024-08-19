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

    @ApiModelProperty(value = "跑分时间")
    private String scoreBeginTime;

    @ApiModelProperty(value = "apiCode")
    private String apiCode;

    @ApiModelProperty(value = "跑分数量")
    private Integer actualNum;

    @ApiModelProperty(value = "模型名称")
    private String productName;

    @ApiModelProperty(value = "场景")
    private String userType;

    @ApiModelProperty(value = "cid")
    private String cid;
}
