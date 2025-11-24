package com.br.marketing.entity;

import lombok.Data;

import java.util.Date;

/**
 * 奇富原始上传数据明细表
 *
 * @author Generated
 */
@Data
public class BQifuUploadDataOriginal {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * DRS 来源数据ID
     */
    private Long drsId;

    /**
     * 批次号 batchNo
     */
    private String batchNo;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 场景标识
     */
    private String userType;

    /**
     * 拨打时间范围 callTimeRange
     */
    private String callTimeRange;

    /**
     * 呼叫方式 callType
     */
    private String callType;

    /**
     * 性别
     */
    private String gender;

    /**
     * 手机号MD5
     */
    private String phoneNoMd5;

    /**
     * 用户唯一编号
     */
    private String serialNo;

    /**
     * 姓氏
     */
    private String surname;

    /**
     * 选择状态：0-待查询、1-查询中、2-直询成功、3-重试（接口异常）、4-重试（无卷信息）
     */
    private Integer selectStatus;

    /**
     * 处理状态：0-未处理，1-处理中，2-处理完成
     */
    private Integer status;

    /**
     * 流水号
     */
    private String flowNo;

    /**
     * 运营场景
     */
    private String operateScene;

    /**
     * 模板编号
     */
    private String templateNo;

    /**
     * 事件类型：REALTIME_LIMIT_INCREASE-实时提额, REALTIME_PRICE_DROP-实时降价, REALTIME_LOAN-实时借款, REALTIME_LOGIN-实时登录
     */
    private String eventType;

    /**
     * 是否是实时数据：0-否,1-是
     */
    private Integer isReal;

    /**
     * 是否发送短信，Y-是，N-否
     */
    private String sendMsg;

    /**
     * 是否重拨，Y-是，N-否
     */
    private String retryCall;

    /**
     * 重拨范围
     */
    private String retryRange;

    /**
     * 重拨次数
     */
    private String retryNums;

    /**
     * 重拨间隔
     */
    private String retryInterval;

    /**
     * 重拨策略
     */
    private String retryCallStrategy;

    /**
     * 扩展字段
     */
    private String extend;

    /**
     * 接入日期 yyyy-MM-dd
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
}
