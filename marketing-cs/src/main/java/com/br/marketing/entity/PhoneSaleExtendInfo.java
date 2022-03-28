package com.br.marketing.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * b_phone_sale_extend_info
 * @author  lizhen
 */
@Data
public class PhoneSaleExtendInfo implements Serializable {
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
     * taskId
     */
    private String taskId;

    /**
     * 场景
     */
    private String userType;

    /**
     * 数据上传日期
     */
    private String appletDate;

    /**
     * 数据上传时间
     */
    private String appletTime;

    /**
     * 状态 a,b
     */
    private String status;

    /**
     * 1-未推送；2-推送成功；3-推送失败
     */
    private Integer pStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}