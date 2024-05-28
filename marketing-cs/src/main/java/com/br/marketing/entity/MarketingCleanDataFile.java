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
     * 文件名称
     */
    private String fileName;

    /**
     * 文件表头
     */
    private String fileHeader;

    /**
     * 文件数据
     */
    private String fileData;

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

    private Date updateTime;

    /**
     * 1-有效；9-无效
     */
    private Integer isDel;

    private static final long serialVersionUID = 1L;
}