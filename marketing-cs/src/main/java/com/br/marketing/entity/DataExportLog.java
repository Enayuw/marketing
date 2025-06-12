package com.br.marketing.entity;

import lombok.Data;
import java.util.Date;

/**
 * 数据导出执行记录表
 * @author 
 */
@Data
public class DataExportLog {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 实际导出行数
     */
    private Long exportRows;

    /**
     * 生成文件路径
     */
    private String filePath;

    /**
     * SFTP推送状态:0-未推送,1-成功,2-失败
     */
    private Integer sftpStatus;

    /**
     * 执行状态:1-执行中,2-成功,3-失败
     */
    private Integer executeStatus;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 创建时间
     */
    private Date createTime;
} 