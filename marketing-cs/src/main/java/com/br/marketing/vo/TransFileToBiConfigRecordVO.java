package com.br.marketing.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class TransFileToBiConfigRecordVO {
    // b_file_bi_config
    private Long configId;
    private String apiCode;
    private String busType;
    private String dbName;
    private String dbFields;
    private String dbColFieldsMap;
    // b_transfer_file_task
    private Long taskId;
    private Integer fileType;
    private String fileName;
    private String filePath;
    private String startDate;
} 