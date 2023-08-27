package com.br.marketing.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class PhoneSaleExtendInfo implements Serializable {
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
     * 手机号
     */
    private String cell;

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

    /**
     * 冗余信息
     */
    private String redundancyField;

    /**
     * null或者1——人工批量和人工单条，2——ibu定制人工批量接口
     */
    private Integer interfaceType;

    /**
     * 电销业务线使用的场景
     */
    private String dxUserType;

    /**
     * 组号，默认组号为0
     */
    private Integer groupNo;
}