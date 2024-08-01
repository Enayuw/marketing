package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * b_marketing_data_valid_config_default
 * @author 
 */
@Data
public class MarketingDataValidConfigDefault implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * apiCode
     */
    private String apiCode;

    /**
     * 上传日期
     */
    private Date uploadDate;

    /**
     * 场景
     */
    private String userType;

    /**
     * 有效期类型：0按日维度,1按月维度
     */
    private Integer validType;

    /**
     * 默认有效期是N天，代表T+N范围
     */
    private Integer validDaysDefault;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 1-有效；9-无效
     */
    private Integer isDel;

    private static final long serialVersionUID = 1L;
}