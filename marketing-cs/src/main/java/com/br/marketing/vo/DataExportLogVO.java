package com.br.marketing.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 数据导出执行记录表VO
 */
@Data
@ApiModel(value = "数据导出执行记录表")
public class DataExportLogVO {

    @ApiModelProperty(value = "主键ID")
    private Long id;

    @ApiModelProperty(value = "任务ID")
    private Long taskId;

    @ApiModelProperty(value = "批次号")
    private String batchNo;

    @ApiModelProperty(value = "实际导出行数")
    private Long exportRows;

    @ApiModelProperty(value = "生成文件路径")
    private String filePath;

    @ApiModelProperty(value = "SFTP推送状态:0-未推送,1-成功,2-失败")
    private Integer sftpStatus;

    @ApiModelProperty(value = "执行状态:1-执行中,2-成功,3-失败")
    private Integer executeStatus;

    @ApiModelProperty(value = "错误信息")
    private String errorMsg;

    @ApiModelProperty(value = "开始时间")
    private String startTime;

    @ApiModelProperty(value = "结束时间")
    private String endTime;

    @ApiModelProperty(value = "创建时间")
    private String createTime;
} 