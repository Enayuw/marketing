package com.br.marketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


/**
 * @ClassName ExecuteCarClueDTO
 * @Description 中台数据导出
 * @Author kongbx
 * @Date 2025/5/6 14:51
 */
@Data
public class DataExportTaskDTO {
    @Schema(description = "任务名称")
    private String taskName;

    @Schema(description = "数据源名称")
    private int dataSource;

    @Schema(description = "导出表头(逗号分隔)")
    private String exportHeaders;

    @Schema(description = "字段映射关系JSON")
    private String fieldMapping;

    @Schema(description = "查询条件配置JSON")
    private String queryCondition;

    @Schema(description = "预估数据量")
    private Long estimatedRows;

    @Schema(description = "导出文件名")
    private String fileNameTemplate;

}
