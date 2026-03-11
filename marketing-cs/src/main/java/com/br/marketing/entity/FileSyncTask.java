package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * b_file_sync_task
 * @author 
 */
@Data
public class FileSyncTask implements Serializable {
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
     * 创建日期
     */
    private String createDate;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 推送目标类型：0-从配置中获取，1-指定目标路径，默认0
     */
    private Integer pushTargetType;

    /**
     * 目的sftp host
     */
    private String targetSftpHost;

    /**
     * 目的sftp port
     */
    private Integer targetSftpPort;

    /**
     * 目的sftp 账号
     */
    private String targetSftpUser;

    /**
     * 目的sftp 账号密码
     */
    private String targetSftpPwd;

    /**
     * 公司文件服务器类型
     */
    private String targetType;

    /**
     * 指定目标路径（pushTargetType=1 时使用，不查配置）
     */
    private String targetPath;

    private static final long serialVersionUID = 1L;
}