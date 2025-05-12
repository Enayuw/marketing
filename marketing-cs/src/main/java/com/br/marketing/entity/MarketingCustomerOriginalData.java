package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * b_marketing_customer_original_data
 * @author 
 */
@Data
public class MarketingCustomerOriginalData implements Serializable {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * API编码
     */
    private String apiCode;

    /**
     * 请求流水号
     */
    private String requestId;

    /**
     * 数据
     */
    private String jsonData;

    /**
     * 数据类型：0:上传，1:转化
     */
    private Integer dataType;

    /**
     * 接收类型：0:通用，1:定制
     */
    private Integer acceptType;

    /**
     * 数据实际条数
     */
    private Integer actualNum;

    /**
     * 扩展字段
     */
    private String extend;

    /**
     * 状态 0-无效、1-有效
     */
    private Integer status;

    /**
     * 清洗状态：0待清洗、1清洗中、2清洗完成
     */
    private Integer cleanStatus;

    /**
     * 接入日期yyyy-MM-dd
     */
    private String receiveDate;

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