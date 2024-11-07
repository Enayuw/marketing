package com.br.marketing.vo;

import lombok.Data;

/**
 * @ClassName TaskTemplateVO
 * @Author kongbx
 * @Date 2024/11/7 21:09
 */
@Data
public class TaskTemplateVO {
    private Long id;
    private String batchNumber;
    private String userType;
    private String taskNumber;
    private String taskCreateTime;
}