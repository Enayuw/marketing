package com.br.marketing.dto.autocheck;

import lombok.Data;

import java.util.Date;

@Data
public class CheckTransferSyncDataDto {

    /**
     * id
     */
    private Long id;

    /**
     * cid
     */
    private String cid;

    /**
     * 分表cid
     */
    private String tCid;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 机构名称
     */
    private String orgName;

    /**
     * 客户案件编号
     */
    private String custNum;

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
     * 是否登录 1是0否
     */
    private String ifLogin;

    /**
     * 是否进件 1是0否
     */
    private String ifApply;

    /**
     * 审批结果 1是0否
     */
    private String applyResult;

    /**
     * 授信总金额
     */
    private String auditAmount;

    /**
     * 是否提现 1是0否
     */
    private String ifLent;

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
     * 业务保留字段1
     */
    private String reserveField1;

    /**
     * 业务保留字段2
     */
    private String reserveField2;

    /**
     * 数据指纹，数据唯一标识
     */
    private Long fingerprint;

    /**
     * 快照时间：前一天08:00快照/最新快照的生成时间
     */
    private String snapTime;
}
