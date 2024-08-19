package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * b_zhijia_city_config
 * @author 
 */
@Data
public class ZhijiaCityConfig implements Serializable {
    private Long id;

    /**
     * apiCode
     */
    private String apiCode;

    /**
     * 上传日期
     */
    private String uploadDate;

    /**
     * 城市ID
     */
    private Integer cId;

    /**
     * 城市名称
     */
    private String cName;

    /**
     * 省份名称配置，多个用，分割
     */
    private String cNameConfig;

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