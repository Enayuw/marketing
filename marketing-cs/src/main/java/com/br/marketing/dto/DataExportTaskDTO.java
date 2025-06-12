package com.br.marketing.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;


/**
 * @ClassName ExecuteCarClueDTO
 * @Description 中台数据导出
 * @Author kongbx
 * @Date 2025/5/6 14:51
 */
@Data
public class DataExportTaskDTO {
    @ApiModelProperty(value = "任务名称")
    private String taskName;

    @ApiModelProperty(value = "数据源名称")
    private String dataSource;

    @ApiModelProperty(value = "导出表头(逗号分隔)")
    private String exportHeaders;

    @ApiModelProperty(value = "字段映射关系JSON")
    private Map<String, String> fieldMapping;

    @ApiModelProperty(value = "查询条件配置JSON")
    private Map<String, String> queryCondition;

    @ApiModelProperty(value = "预估数据量")
    private Long estimatedRows;

    @ApiModelProperty(value = "导出文件名")
    private String fileNameTemplate;

}
