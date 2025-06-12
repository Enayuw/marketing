package com.br.marketing.dto;

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
    private String taskName;
    private String dataSource;
    // "姓名,手机号,年龄"
    private String exportHeaders;
    // {"姓名":"name","手机号":"phone"}
    private Map<String, String> fieldMapping;
    private QueryConditionDTO queryCondition;
    private Long estimatedRows;
    // "用户数据_{yyyyMMdd}.txt"
    private String fileNameTemplate;
    private String sftpPath;
}
