package com.br.marketing.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * b_marketing_clean_persist_task
 */
@Data
public class MarketingCleanPersistTask implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 关联 b_marketing_clean_data_file.id
     */
    private Long cleanDataFileRecordId;

    /**
     * 关联 b_sync_config.id
     */
    private Long syncConfigId;

    /**
     * 表头快照，用于匹配映射
     */
    private String fileHeader;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * SFTP 文件分隔符
     */
    private String sftpFileSeparator;

    /**
     * 文件在服务器上的路径
     */
    private String localPath;

    /**
     * 落库完成后回填：使用的映射表 id
     */
    private Long headerMappingId;

    /**
     * 状态：0-待执行 1-执行中 2-成功 3-失败
     * @see com.br.marketing.enums.clean.CleanPersistTaskStatusEnum
     */
    private Integer status;

    /**
     * 落库行数
     */
    private Integer totalRowCount;

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
