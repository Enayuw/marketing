package com.br.marketing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "转化文件任务表")
public class TransferFileTaskVO {

    @Schema(description = "主键id")
    private Long id;

    @Schema(description = "apiCode")
    private String apiCode;

    @Schema(description = "文件类型")
    private String fileType;

    @Schema(description = "文件名称")
    private String fileName;

    @Schema(description = "文件路径")
    private String filePath;

    @Schema(description = "子路径")
    private String fileChildDir;

    @Schema(description = "任务状态 1-待开始,2-进行中，3-待推送，4-已完成")
    private Integer status;

    @Schema(description = "文件数据量")
    private Integer taskNumber;

    @Schema(description = "执行日期")
    private String startDate;

    @Schema(description = "开始时间")
    private String createTime;

    @Schema(description = "结束时间")
    private String updateTime;

    @Schema(description = "是否可以操作")
    private Integer isOperation;
}