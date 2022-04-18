package com.br.marketing.entity;

import lombok.Data;

import java.util.Date;

@Data
public class PhoneSaleExtendInfo {
    /**
     * 
     */
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

    /**
     * 客户传输节点
     */
    private String type;

    /**
     * 电销节点
     */
    private String dxType;

    /**
     * 1-实时推送;0-非实时推送
     */
    private String transformType;

    /**
     * 源数据id
     */
    private Long sourceId;

    /**
     * 推送电销时间
     */
    private Date pushDxTime;
}