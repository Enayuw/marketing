package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * b_car_clue_series_information
 * @author 
 */
@Data
public class CarClueSeriesInformation implements Serializable {
    private Long id;

    /**
     * 车型类型，0-易车，1-海星之家
     */
    private String seriesType;

    /**
     * 品牌id
     */
    private Integer brandId;

    /**
     * 品牌名称
     */
    private String brandName;

    /**
     * 子品牌id
     */
    private Integer subBrandId;

    /**
     * 子品牌名称
     */
    private String subBrandName;

    /**
     * 车系id
     */
    private Integer seriesId;

    /**
     * 车系名称
     */
    private String seriesName;

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