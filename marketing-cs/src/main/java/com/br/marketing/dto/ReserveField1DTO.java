package com.br.marketing.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class ReserveField1DTO implements Serializable {
    @ApiModelProperty(value = "机构名称")
    private String orgName;
    @ApiModelProperty(value = "用户姓（明文）")
    private String firstName;
    @ApiModelProperty(value = "数据源")
    private String source;
    @ApiModelProperty(value = "机构运营场景,替代groupType")
    private String userType;
    @ApiModelProperty(value = "转化节点")
    private String type;
    @ApiModelProperty(value = "用户性别", allowableValues="0,1", example = "0:女 1：男")
    private String gender;
    @ApiModelProperty(value = "客群名称")
    private String customName;
    @ApiModelProperty(value = "是否注册", allowableValues = "0,1", example = "1是0否")
    private String ifRegister;
    @ApiModelProperty(value = "注册时间", example = "yyyy-mm-dd hh:mm:ss")
    private String registerTime;
    @ApiModelProperty(value = "是否登录", allowableValues = "0,1", example = "1是0否")
    private String ifLogin;
    @ApiModelProperty(value = "登录时间", example = "yyyy-mm-dd hh:mm:ss")
    private String loginTime;
    @ApiModelProperty(value = "是否进件", allowableValues = "0,1", example = "1是0否")
    private String ifApply;
    @ApiModelProperty(value = "进件时间", example = "yyyy-mm-dd hh:mm:ss")
    private String applyDt;
    @ApiModelProperty(value = "审批时间", example = "yyyy-mm-dd hh:mm:ss")
    private String applyTime;
    @ApiModelProperty(value = "审批结果", allowableValues = "0,1", example = "1是0否")
    private String applyResult;
    @ApiModelProperty(value = "拒绝时间", example = "yyyy-mm-dd hh:mm:ss")
    private String refuseTime;
    @ApiModelProperty(value = "授信时间", example = "yyyy-mm-dd hh:mm:ss")
    private String auditTime;
    @ApiModelProperty(value = "授信总金额")
    private String auditAmount;
    @ApiModelProperty(value = "是否提现", allowableValues = "0,1", example = "1是0否")
    private String ifLent;
    @ApiModelProperty(value = "提现时间", example = "yyyy-mm-dd hh:mm:ss")
    private String lentTime;
    @ApiModelProperty(value = "提现金额")
    private String lentAmount;
    @ApiModelProperty(value = "未提现额度")
    private String unlentAmount;
    @ApiModelProperty(value = "是否结清", allowableValues = "0,1", example = "1是0否")
    private String ifSettle;
    @ApiModelProperty(value = "结清时间", example = "yyyy-mm-dd hh:mm:ss")
    private String settleTime;
    @ApiModelProperty(value = "紧急扩展字段", example = "客户传输的任意值")
    private String extStr;
    @ApiModelProperty(value = "拍拍贷扩展字段", example = "90d,180d,360d,720d")
    private String desleep;
}
