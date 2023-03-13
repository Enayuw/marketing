package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * b_data_distribute_detail_log
 * @author :zhenLi
 * @updateTime: 2023-03-10
 */
@Data
public class DataDistributeDetailLog implements Serializable {
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 案件编号
     */
    private String custNum;

    /**
     * 手机号
     */
    private String cell;

    /**
     * 情况类型
     */
    private String status;

    /**
     * 推送状态1-待推送；2-成功；
     */
    private Integer pStatus;

    /**
     * 分发日期
     */
    private String distributeDate;

    /**
     * 分发流向 1-客服转化;
     */
    private Integer distributeType;

    /**
     * 分发成功日期
     */
    private String successDate;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 数据源id
     */
    private Long sourceId;

    /**
     * 数据源表
     */
    private String sourceType;

    private static final long serialVersionUID = 1L;
}