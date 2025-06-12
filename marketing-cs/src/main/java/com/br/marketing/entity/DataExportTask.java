package com.br.marketing.entity;

import lombok.Data;
import java.util.Date;

/**
 * 数据导出任务配置表
 * @author 
 */
@Data
public class DataExportTask {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 数据源名称(marketing_tikv/marketing_bi等)
     */
    private String dataSource;

    /**
     * 导出表头(逗号分隔)
     */
    private String exportHeaders;

    /**
     * 字段映射关系JSON
     */
    private String fieldMapping;

    /**
     * 查询条件配置JSON
     */
    private String queryCondition;

    /**
     * 预估数据量
     */
    private Long estimatedRows;

    /**
     * 导出文件名
     */
    private String fileNameTemplate;

    /**
     * 状态:1-启用,0-禁用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;
} 