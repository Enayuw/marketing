package com.br.marketing.entity;

import lombok.Data;

import java.util.Date;

@Data
public class DiDiV5CollidingDataLog {
    /**
     * 主键id
     */
    private Long id;

    /**
     * 准入数据主键id
     */
    private Long dataId;

    /**
     * 本地文件记录id
     */
    private Long localId;

    /**
     * apiCode
     */
    private String apiCode;

    /**
     * 投保人手机号
     */
    private String cell;

    /**
     * 撞库结果 true:可以使用；false:不可使用
     */
    private String result;

    /**
     * 失败原因 1：频控限制；2：非滴滴用户；3：其他
     */
    private String failReason;

    /**
     * 用户群体 1：基础群体；2：消息补充型客群；3：黑名单客群
     */
    private String userGroup;

    /**
     * 下次营销时间
     */
    private String nextTime;

    /**
     * 请求码
     */
    private String httpCode;

    /**
     * 业务异常码
     */
    private String errorCode;

    /**
     * 业务异常码
     */
    private String errorMessage;

    /**
     * 接口返回内容
     */
    private String returnContent;

    /**
     * 扩展字段
     */
    private String extend;

    /**
     * 状态 0-正常1 删除
     */
    private Integer isDelete;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 券类型
     */
    private String couponType;

    /**
     * 数据源类型
     */
    private String sourceType;
}