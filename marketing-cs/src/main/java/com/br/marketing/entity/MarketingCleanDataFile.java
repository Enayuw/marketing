package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * b_marketing_clean_data_file
 * @author 
 */
@Data
public class MarketingCleanDataFile implements Serializable {
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 清洗类型：0上传，1转化
     */
    private Integer cleanType;

    /**
     * 数据库操作类型：0：insert 1：update
     */
    private Integer dbOperateType;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 压缩包名称
     */
    private String zipName;

    /**
     * 文件表头
     */
    private String fileHeader;

    /**
     * 虚拟文件头
     */
    private String virtualHeaders;

    /**
     * 文件数据
     */
    private String fileData;

    /**
     * 试跑数据：存储为json结构，用于试跑
     */
    private String testRunData;

    /**
     * 目标sftp路径
     */
    private String targetSftpPath;

    /**
     * 本地文件路径
     */
    private String localPath;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    /**
     * 1-有效；9-无效
     */
    private Integer isDel;

    /**
     * sftp账号配置id
     */
    private Long syncConfigId;

    /**
     * 文件MD5值
     */
    private String md5Value;

    /**
     * 执行状态0:未执行，1:执行中，2:执行成功，3:执行失败
     */
    private Integer processStatus;

    /**
     * 文件类型
     */
    private Integer dataType;

    /**
     * 接收时间
     */
    private String receiveDate;

    private static final long serialVersionUID = 1L;
}