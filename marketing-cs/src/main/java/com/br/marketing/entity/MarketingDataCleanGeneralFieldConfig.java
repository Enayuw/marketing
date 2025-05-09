package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * b_marketing_data_clean_general_field_config
 * @author 
 */
@Data
public class MarketingDataCleanGeneralFieldConfig implements Serializable {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * API编码
     */
    private String apiCode;

    /**
     * 数据类型：0:上传，1:转化
     */
    private Integer dataType;

    /**
     * 接收类型：0:通用,1:定制,2:FTP
     */
    private Integer acceptType;

    /**
     * 字段集合，多个字段用,分割
     */
    private String fieldCollect;

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