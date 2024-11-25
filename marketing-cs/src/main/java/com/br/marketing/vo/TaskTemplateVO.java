package com.br.marketing.vo;

import lombok.Data;

/**
 * @ClassName TaskTemplateVO
 * @Author kongbx
 * @Date 2024/11/7 21:09
 */
@Data
public class TaskTemplateVO {
    private String fileId;
    private String batchNumber;
    private String userType;
    private String taskNumber;
    private String taskCreateTime;
    // 是否勾选，0-否 1-是
    private Integer status;
}