package com.br.marketing.client.dassservice.input;

import lombok.Data;

@Data
public class DassImportDataDTO {

    private Long id;

    /**
     * 用户id
     */
    private String uid;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 姓
     */
    private String name;

    /**
     * 用户姓名md5加密
     */
    private String nameSec;

    /**
     * 身份证号
     */
    private String cid;

    /**
     * 性别
     */
    private String sex;

    /**
     * 营销分
     */
    private String score;

    /**
     * 风控分
     */
    private String riskScore;

    /**
     * 机构名称
     */
    private String orgName;

    /**
     * 数据源
     */
    private String source;

    /**
     * 机构运营场景
     */
    private String userType;

    /**
     * 机器人转化节点类型
     */
    private String type;

    /**
     * 客群名称
     */
    private String customName;

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
     * 扩展字段
     */
    private String extraSet;
}
