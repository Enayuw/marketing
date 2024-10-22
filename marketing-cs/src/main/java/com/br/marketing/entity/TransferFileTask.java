package com.br.marketing.entity;

import lombok.Data;

import java.util.Date;

@Data
public class TransferFileTask {
    /**
     * 
     */
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 文件类型 1-萨摩耶断点转化数据文件；2-萨摩耶活跃用户文件
     */
    private Integer fileType;

    /**
     * 批次号
     */
    private String batchNumber;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件子级目录
     */
    private String fileChildDir;

    /**
     * 任务状态 1-正常,2-文件生成成功，3-文件传输至内部sftp，4-文件传输给客户
     */
    private Integer status;

    /**
     * 任务上传数据量
     */
    private Integer taskNumber;

    /**
     * 执行日期
     */
    private String startDate;

    /**
     * 分片自增id
     */
    private Long contextId;

    /**
     * 扩展字段
     */
    private String extend;

    /**
     * 入库日期
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;
}