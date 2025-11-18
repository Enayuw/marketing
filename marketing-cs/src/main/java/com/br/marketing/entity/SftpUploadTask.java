package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * b_sftp_upload_task
 * @author 
 */
@Data
public class SftpUploadTask implements Serializable {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 本地文件路径
     */
    private String localPath;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件类型
     */
    private Integer dataType;

    /**
     * 任务状态：0-待上传，1-上传中，2-上传成功，3-上传失败
     */
    private Integer status;

    /**
     * 后置SQL处理
     */
    private String postSqlProcess;

    /**
     * 扩展字段
     */
    private String extend;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}