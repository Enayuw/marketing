package com.br.marketing.client.dassservice.input;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

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
     * 性别
     */
    private String gender;

    /**
     * 变量key-value
     */
    private String recv_data = "";

    /**
     * 数组变量list
     */
    private List recv_vars = new ArrayList<>();

    /**
     * 姓
     */
    private String name;

    /**
     * 营销分
     */
    private String marketscore;

    /**
     * 风控分
     */
    private String riskscore;

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
    private String user_type;


    /**
     * 产品信息
     */
    private String product_name;

    /**
     * 乐花卡类型（1 人工结清 2人工未结清）
     */
    private String flag_type;

    /**
     * 机器人转化节点类型
     */
    private String type;

    /**
     * 意向等级
     */
    private String level;

    /**
     * 是否注册
     */
    private String if_register;

    /**
     * 注册时间
     */
    private String register_time;

    /**
     * 是否登录
     */
    private String if_login;

    /**
     * 登录时间
     */
    private String login_time;

    /**
     * 是否进件
     */
    private String if_apply;

    /**
     * 进件时间
     */
    private String apply_dt;

    /**
     * 审批时间
     */
    private String apply_time;

    /**
     * 审批结果
     */
    private String apply_result;

    /**
     * 页面节点
     */
    private String pagenode;

    /**
     * 1人工 2机器人
     */
    private String optype;

    /**
     * 拒绝时间
     */
    private String refuse_time;

    /**
     * 授信时间
     */
    private String audit_time;

    /**
     * 授信总金额
     */
    private String audit_amount;

    /**
     * 是否提现
     */
    private String if_lent;

    /**
     * 提现时间
     */
    private String lent_time;

    /**
     * 提现金额
     */
    private String lent_amount;

    /**
     * 未提现额度
     */
    private String unlent_amount;

    /**
     * 是否结清
     */
    private String if_settle;

    /**
     * 结清时间
     */
    private String settle_time;

    /**
     * 0-无活动  1-红包  2-24%利率  3-30%利率
     */
    private String activity;

    /**
     * 推荐产品
     */
    private String production;

    /**
     * 经营地区
     */
    private String region;

    /**
     * 扩展字段
     */
    private String extend;
}
