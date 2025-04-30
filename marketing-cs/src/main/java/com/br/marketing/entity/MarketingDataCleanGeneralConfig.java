package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * b_marketing_data_clean_general_config
 * @author 
 */
@Data
public class MarketingDataCleanGeneralConfig implements Serializable {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * API编码
     */
    private String apiCode;

    /**
     * 账号类型
     */
    private String accountType;

    /**
     * 数据类型：0:上传，1:转化
     */
    private Integer dataType;

    /**
     * 接收类型：0:通用,1:定制,2:FTP
     */
    private Integer acceptType;

    /**
     * 是否删除：1-正常；9-删除
     */
    private Integer isDel;

    /**
     * 操作人id
     */
    private Long optUserId;

    /**
     * 操作人账户名
     */
    private String optUserName;

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