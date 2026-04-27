package com.br.marketing.vo;

import lombok.Data;

/**
 * 无表头文件按列顺序的字段配置（field_config_column 单条结构）
 * columnIndex 从 1 开始，对应文件列下标 columnIndex-1
 */
@Data
public class FileToMarketingFieldByColumnVO {
    /**
     * 列序号，从 1 开始（第1列、第2列…），对应文件列下标 columnIndex-1
     */
    private Integer columnIndex;
    /**
     * 对接接口字段名，如 cell、custNum、userType
     */
    private String interfaceField;
    /**
     * 是否必填
     */
    private Boolean isMust;
    /**
     * 是否为扩展字段
     */
    private Boolean isExtend;
    /**
     * 默认值
     */
    private String defaultValue;
    /**
     * 字典映射 JSON（与现有 conversion 一致）
     */
    private String conversion;
}
