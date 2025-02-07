package com.br.marketing.client.dassservice.input.csos;

import lombok.Data;

/**
 * @Description : 调电销（金融AI）财富接口入参
 * ---------------------------------
 * @Author : lizhen
 * @Date : Create in 2025/02/06 15:35
 */
@Data
public class DaasCsosDataDTO {


    private Long id;

    /**
     * 机构名称
     */
    private String orgName;

    /**
     * 数据源
     */
    private String source;

    /**
     * ⽤户类型
     */
    private String userType;

    /**
     * 客户姓名 兜底默认为1
     */
    private String name;

    /**
     * 性别
     */
    private String gender;

    /**
     * 手机号AES加密
     */
    private String phone;

    /**
     * 用户id
     */
    private String uid;


    /**
     * 户籍
     */
    private String householdRegistration;

    /**
     * 年龄
     */
    private String age ;

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
     * 可⽤余额
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
     * 基⾦持仓
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
     * ⻛险等级
     */
    private String riskLevel;

    /**
     * 三⽅存管
     */
    private String thirdPartyCustody;


    /**
     * 扩展字段
     */
    private String extend;

}
