package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * b_score_statistics_detail
 * @author 
 */
@Data
public class ScoreStatisticsDetail implements Serializable {
    private Long id;

    /**
     * 跑分统计记录表id
     */
    private Long statisticsId;

    /**
     * x区间范围
     */
    private String fieldXValue;

    /**
     * y区间范围
     */
    private String fieldYValue;

    /**
     * 量级
     */
    private Integer fieldNum;

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