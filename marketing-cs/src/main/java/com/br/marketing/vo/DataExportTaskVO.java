package com.br.marketing.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 数据导出任务配置表VO
 */
@Data
@ApiModel(value = "数据导出任务配置表")
public class DataExportTaskVO {

    @ApiModelProperty(value = "主键ID")
    private Long id;

    @ApiModelProperty(value = "任务名称")
    private String taskName;

    @ApiModelProperty(value = "数据源名称")
    private String dataSource;

    @ApiModelProperty(value = "导出表头(逗号分隔)")
    private String exportHeaders;

    @ApiModelProperty(value = "字段映射关系JSON")
    private String fieldMapping;

    @ApiModelProperty(value = "查询条件配置JSON")
    private String queryCondition;

    @ApiModelProperty(value = "预估数据量")
    private Long estimatedRows;

    @ApiModelProperty(value = "导出文件名")
    private String fileNameTemplate;

    @ApiModelProperty(value = "状态:1-启用,0-禁用")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "更新时间")
    private String updateTime;

    @ApiModelProperty(value = "创建人")
    private String createBy;

    @ApiModelProperty(value = "更新人")
    private String updateBy;
} 