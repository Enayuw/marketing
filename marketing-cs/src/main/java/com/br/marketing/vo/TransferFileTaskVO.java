package com.br.marketing.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel(value = "转化文件任务表")
public class TransferFileTaskVO {

    @ApiModelProperty(value = "主键id")
    private Long id;

    @ApiModelProperty(value = "apiCode")
    private String apiCode;

    @ApiModelProperty(value = "文件名称")
    private String fileName;

    @ApiModelProperty(value = "文件路径")
    private String filePath;

    @ApiModelProperty(value = "文件子级目录")
    private String fileChildDir;

    @ApiModelProperty(value = "任务状态 1-待开始,2-进行中，3-待推送，4-已完成")
    private Integer status;

    @ApiModelProperty(value = "文件数据量")
    private Integer taskNumber;

    @ApiModelProperty(value = "执行日期")
    private String startDate;

    @ApiModelProperty(value = "入库日期")
    private String createTime;

    @ApiModelProperty(value = "修改时间")
    private String updateTime;

    @ApiModelProperty(value = "是否可以操作")
    private Integer isOperation;
}