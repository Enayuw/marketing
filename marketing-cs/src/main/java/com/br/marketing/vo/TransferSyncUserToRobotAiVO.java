package com.br.marketing.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 转化接口推送智能客服标准版
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/11/3 17:09
 */
@Data
public class TransferSyncUserToRobotAiVO implements Serializable {

    private static final long serialVersionUID = -2824594431908515232L;

    /**
     * 数据源
     */
    private String source;

    /**
     * 机构运营场景
     */
    private String userType;

    /**
     * 转化节点
     */
    private String type;

    /**
     * 客群名称
     */
    private String customName;

    /**
     * 是否注册 1是0否
     */
    private String ifRegister;

    /**
     * 注册时间 yyyy-mm-dd hh:mm:ss:SSS
     */
    private String registerTime;

    /**
     * 是否登录 1是0否
     */
    private String ifLogin;

    /**
     * 登录时间yyyy-mm-dd hh:mm:ss:SSS
     */
    private String loginTime;

    /**
     * 是否进件 1是0否
     */
    private String ifApply;

    /**
     * 进件时间yyyy-mm-dd hh:mm:ss:SSS
     */
    private String applyDt;

    /**
     * 审批时间yyyy-mm-dd hh:mm:ss:SSS
     */
    private String applyTime;

    /**
     * 审批结果 1是0否
     */
    private String applyResult;

    /**
     * 拒绝时间yyyy-mm-dd hh:mm:ss:SSS
     */
    private String refuseTime;

    /**
     * 授信时间yyyy-mm-dd hh:mm:ss:SSS
     */
    private String auditTime;

    /**
     * 授信总金额
     */
    private String auditAmount;

    /**
     * 是否提现 1是0否
     */
    private String ifLent;

    /**
     * 提现时间yyyy-mm-dd hh:mm:ss:SSS
     */
    private String lentTime;

    /**
     * 提现金额
     */
    private String lentAmount;

    /**
     * 未提现额度
     */
    private String unlentAmount;

    /**
     * 是否结清 1是0否
     */
    private String ifSettle;

    /**
     * 结清时间yyyy-mm-dd hh:mm:ss:SSS
     */
    private String settleTime;

    /**
     * 活动类型
     */
    private String activity;

    /**
     * 结案状态 0未结案1已结案
     */
    private String caseStatus;

    /**
     * 案件有效性 0无效1有效
     */
    private String caseEffective;

    /**
     * 是否转化 1是0否
     */
    private String ifTransform;

    /**
     * 创建时间yyyy-mm-dd hh:mm:ss:SSS
     */
    private String insertTime;

    /**
     * 业务保留字段1
     */
    private String reserveField1;

    /**
     * 业务保留字段2
     */
    private String reserveField2;

    /**
     * 客户请求日期
     */
    private String requestData;

    /**
     * 客户请求时间
     */
    private String requestTime;

}
