package com.br.marketing.client.dassservice.input.transfer;

import lombok.Data;

/**
 * @Description : 调电销转化接口入参
 * ---------------------------------
 * @Author : lizhen
 * @Date : Create in 2022/4/21 15:35
 */
@Data
public class DassTransferDataDTO {

    private Long id;
    /**
     * 用户id
     */
    private String uid;

    /**
     * 数据源
     */
    private String source;

    /**
     * 机构运营场景
     */
    private String userType;
    /**
     * 手机号AES加密
     */
    private String phone;
    /**
     * 机器人转化节点类型
     */
    private String type;
    /**
     * 机构名称
     */
    private String orgName;
    /**
     * 是否注册
     */
    private String ifRegister;

    /**
     * 注册时间
     */
    private String registerTime;

    /**
     * 是否登录
     */
    private String ifLogin;

    /**
     * 登录时间
     */
    private String loginTime;

    /**
     * 是否进件
     */
    private String ifApply;

    /**
     * 进件时间
     */
    private String applyDt;

    /**
     * 审批时间
     */
    private String applyTime;

    /**
     * 审批结果
     */
    private String applyResult;
    /**
     * 拒绝时间
     */
    private String refuseTime;

    /**
     * 授信时间
     */
    private String auditTime;

    /**
     * 授信总金额
     */
    private String auditAmount;

    /**
     * 是否提现
     */
    private String ifLent;

    /**
     * 提现时间
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
     * 是否结清
     */
    private String ifSettle;

    /**
     * 结清时间
     */
    private String settleTime;

    /**
     * 0-无活动  1-红包  2-24%利率  3-30%利率
     */
    private String activity;
    /**
     * 结案状态：0未结案 1已结案
     */
    private String caseStatus;
    /**
     * 案件有效性：0无效 1有效
     */
    private String caseEffective;
    /**
     * 是否转化：1是0否
     */
    private String ifTransform;
    /**
     * 转化时间
     */
    private String transformTime;
    /**
     * 案件转化状态（当前状态）
     */
    private String status;
    /**
     * 创建时间（严格时序）
     */
    private String insertTime;
    /**
     * 营销中台编号
     */
    private String apiCode;
    /**
     * 转化规则类型
     */
    private String transformStatus;
}
