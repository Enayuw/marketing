package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * b_csos_phone_sale
 * @author 
 */
@Data
public class CsosPhoneSale implements Serializable {
    private Long id;

    private String apiCode;

    /**
     * 本地文件记录id
     */
    private String localId;

    /**
     * 状态 1-正常2-非正常
     */
    private Integer status;

    /**
     * 数据描述
     */
    private String dataMessage;

    /**
     * 用户id
     */
    private String uid;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 手机号aes加密
     */
    private String phoneAes;

    /**
     * 姓名
     */
    private String name;

    /**
     * 姓名aes加密
     */
    private String nameAes;

    /**
     * 性别
     */
    private String gender;

    /**
     * 机构名称
     */
    private String orgname;

    /**
     * 数据源
     */
    private String source;

    /**
     * 机构运营场景
     */
    private String userType;

    /**
     * 户籍
     */
    private String householdRegistration;

    /**
     * 年龄
     */
    private String age;

    /**
     * 开户时间（yyyy-MM-dd hh:mm:ss）
     */
    private String accountOpenDate;

    /**
     * 开户渠道
     */
    private String accountChannel;

    /**
     * 最近登录的APP
     */
    private String lastLoginApp;

    /**
     * 可投资等级
     */
    private String investLevel;

    /**
     * 购买意向分
     */
    private String purchaseIntentScore;

    /**
     * 营销接受度
     */
    private String marketingAcceptanceLevel;

    /**
     * 多头次数
     */
    private String multiPositionCount;

    /**
     * 可用余额
     */
    private String availableBalance;

    /**
     * 账户资产
     */
    private String accountAssets;

    /**
     * 存款持仓
     */
    private String depositPosition;

    /**
     * 理财持仓
     */
    private String wealthPosition;

    /**
     * 基金持仓
     */
    private String fundPosition;

    /**
     * 保险持仓
     */
    private String insurancePosition;

    /**
     * 购买及赎回记录
     */
    private String buyAndRedeemRecord;

    /**
     * 是否贷款
     */
    private String hasLoan;

    /**
     * 是否加微
     */
    private String hasWechat;

    /**
     * 风险等级
     */
    private String riskLevel;

    /**
     * 三方存管
     */
    private String thirdPartyCustody;

    /**
     * 扩展字段
     */
    private String extend;

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