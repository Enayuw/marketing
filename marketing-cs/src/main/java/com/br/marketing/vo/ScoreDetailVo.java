package com.br.marketing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ScoreDetailVo {

    @Schema(description = "客户批次号")
    private String cusBatchNumber;

    @Schema(description = "内部客户批次号")
    private String batchNumber;

    @Schema(description = "跑分文件id")
    private Long fileId;

    @Schema(description = "统计下载路径")
    private String statisticFilePath;

    @Schema(description = "跑分时间")
    private String scoreBeginTime;

    @Schema(description = "apiCode")
    private String apiCode;

    @Schema(description = "跑分数量")
    private Integer actualNum;

    @Schema(description = "模型名称")
    private String productName;

    @Schema(description = "场景")
    private String userType;

    @Schema(description = "cid")
    private String cid;
}
