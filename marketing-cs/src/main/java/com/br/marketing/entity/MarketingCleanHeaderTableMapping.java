package com.br.marketing.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * b_marketing_clean_header_table_mapping
 */
@Data
public class MarketingCleanHeaderTableMapping implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 关联 b_sync_config.id
     */
    private Long syncConfigId;

    /**
     * 原始完整表头，如 姓名,手机号,身份证号
     */
    private String headerSchema;

    /**
     * 表头唯一标识，如 MD5(归一化 header_schema)，用于匹配
     */
    private String headerSign;

    /**
     * 建表用英文字段名，逗号分隔
     */
    private String columnSchemaEn;

    /**
     * 对应动态业务表名
     */
    private String tableName;

    /**
     *
     */
    private Date createTime;

    /**
     *
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}
