package com.br.marketing.entity;

import lombok.Data;

import java.util.Date;

@Data
public class WubaSubmitConversionData {
    /**
     * 
     */
    private Long id;

    /**
     * 
     */
    private String apiCode;

    /**
     * 本地文件记录id
     */
    private Long localId;

    /**
     * md5手机号
     */
    private String cell;

    /**
     * 营销时间
     */
    private String marketingTime;

    /**
     * 推送状态 0-待推送, 1-推送中，2-推送成功，3-推送失败
     */
    private Integer pushStatus;

    /**
     * 数据状态 1-正常数据 2-异常数据 3-重复数据
     */
    private Integer status;

    /**
     * 使用场景 1-非金融 2-金融
     */
    private String userType;

    /**
     * 数据描述
     */
    private String dataMessage;

    /**
     * 扩展字段
     */
    private String extend;

    /**
     * 日期
     */
    private Integer createDate;

    /**
     * 最近一次上报时间
     */
    private Date pushTime;

    /**
     * 删除标识 0-正常，1-删除
     */
    private Integer isDeleted;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}