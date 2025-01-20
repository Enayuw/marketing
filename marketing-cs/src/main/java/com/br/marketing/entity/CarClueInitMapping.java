package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * b_car_clue_init_mapping
 * @author 
 */
@Data
public class CarClueInitMapping implements Serializable {
    private Long id;

    /**
     * apiCode
     */
    private String apiCode;

    /**
     * 品牌名称
     */
    private String brandName;

    /**
     * 车系名称
     */
    private String seriesName;

    /**
     * 固定省份名称
     */
    private String satisfyProvinceName;

    /**
     * 固定城市名称
     */
    private String satisfyCityName;

    /**
     * 排除省份名称
     */
    private String excludeProvinceName;

    /**
     * 排除城市名称
     */
    private String excludeCityName;

    /**
     * 省市类型 0-全国 1-固定 2-排除
     */
    private String provinceType;

    /**
     * 上传日期
     */
    private String appletDate;

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