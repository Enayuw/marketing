package com.br.marketing.client.dassservice.input.userdata;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DassSingleImportDataDTO {

    @JsonIgnore
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
    @JSONField(name = "recv_data")
    private String recvData = "";

    /**
     * 数组变量list
     */
    @JSONField(name = "recv_vars")
    private List recvVars = new ArrayList<>();

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
    @JSONField(name = "user_type")
    private String userType;


    /**
     * 产品信息
     */
    @JSONField(name = "product_name")
    private String productName;

    /**
     * 乐花卡类型（1 人工结清 2人工未结清）
     */
    @JSONField(name = "flag_type")
    private String flagType;

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
    @JSONField(name = "if_register")
    private String ifRegister;

    /**
     * 注册时间
     */
    @JSONField(name = "register_time")
    private String registerTime;

    /**
     * 是否登录
     */
    @JSONField(name = "if_login")
    private String ifLogin;

    /**
     * 登录时间
     */
    @JSONField(name = "login_time")
    private String loginTime;

    /**
     * 是否进件
     */
    @JSONField(name = "if_apply")
    private String ifApply;

    /**
     * 进件时间
     */
    @JSONField(name = "apply_dt")
    private String applyDt;

    /**
     * 审批时间
     */
    @JSONField(name = "apply_time")
    private String applyTime;

    /**
     * 审批结果
     */
    @JSONField(name = "apply_result")
    private String applyResult;

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
    @JSONField(name = "refuse_time")
    private String refuseTime;

    /**
     * 授信时间
     */
    @JSONField(name = "audit_time")
    private String auditTime;

    /**
     * 授信总金额
     */
    @JSONField(name = "audit_amount")
    private String auditAmount;

    /**
     * 是否提现
     */
    @JSONField(name = "if_lent")
    private String ifLent;

    /**
     * 提现时间
     */
    @JSONField(name = "lent_time")
    private String lentTime;

    /**
     * 提现金额
     */
    @JSONField(name = "lent_amount")
    private String lentAmount;

    /**
     * 未提现额度
     */
    @JSONField(name = "unlent_amount")
    private String unlentAmount;

    /**
     * 是否结清
     */
    @JSONField(name = "if_settle")
    private String ifSettle;

    /**
     * 结清时间
     */
    @JSONField(name = "settle_time")
    private String settleTime;

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
     * 近3天是否申请：1是0否
     */
    @JSONField(name = "yx_flag_3d")
    private String yxFlag3d;

    /**
     * 近7天是否申请：1是0否
     */
    @JSONField(name = "yx_flag_7d")
    private String yxFlag7d;

    /**
     * 近15天是否申请：1是0否
     */
    @JSONField(name = "yx_flag_15d")
    private String yxFlag15d;

    /**
     * 近30天是否申请：1是0否
     */
    @JSONField(name = "yx_flag_1m")
    private String yxFlag1m;

    /**
     * 是否有房：1是0否
     */
    @JSONField(name = "person_flag_house")
    private String personFlagHouse;

    /**
     * 是否有车：1是0否
     */
    @JSONField(name = "person_flag_car")
    private String personFlagCar;

    /**
     * 是否有寿险：1是0否
     */
    @JSONField(name = "person_flag_insur")
    private String personFlagInsur;

    /**
     * 是否命中国网白名单：1是0否
     */
    @JSONField(name = "white_list_gw")
    private String whiteListGw;

    /**
     * 是否命中发票白名单：1是0否
     */
    @JSONField(name = "white_list_fp")
    private String whiteListFp;

    /**
     * 是否命中烟草白名单：1是0否
     */
    @JSONField(name = "white_list_yc")
    private String whiteListYc;
    /**
     * 扩展字段
     */
    private String extend;

    /**
     * 拨打优先级（枚举值：1、2、3）
     */
    private String prioritySymbol;
    /**
     * 筛选项1
     */
    private String filterItem1;

    /**
     * 筛选项2
     */
    private String filterItem2;


}
