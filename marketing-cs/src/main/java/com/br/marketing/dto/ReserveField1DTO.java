package com.br.marketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
public class ReserveField1DTO implements Serializable {
    @Schema(description = "机构名称")
    private String orgName;
    @Schema(description = "用户姓（明文）")
    private String firstName;
    @Schema(description = "数据源")
    private String source;
    @Schema(description = "机构运营场景,替代groupType")
    private String userType;
    @Schema(description = "转化节点")
    private String type;
    @Schema(description = "用户性别", allowableValues="0,1", example = "0:女 1：男")
    private String gender;
    @Schema(description = "客群名称")
    private String customName;
    @Schema(description = "是否注册", allowableValues = "0,1", example = "1是0否")
    private String ifRegister;
    @Schema(description = "注册时间", example = "yyyy-mm-dd hh:mm:ss")
    private String registerTime;
    @Schema(description = "是否登录", allowableValues = "0,1", example = "1是0否")
    private String ifLogin;
    @Schema(description = "登录时间", example = "yyyy-mm-dd hh:mm:ss")
    private String loginTime;
    @Schema(description = "是否进件", allowableValues = "0,1", example = "1是0否")
    private String ifApply;
    @Schema(description = "进件时间", example = "yyyy-mm-dd hh:mm:ss")
    private String applyDt;
    @Schema(description = "审批时间", example = "yyyy-mm-dd hh:mm:ss")
    private String applyTime;
    @Schema(description = "审批结果", allowableValues = "0,1", example = "1是0否")
    private String applyResult;
    @Schema(description = "拒绝时间", example = "yyyy-mm-dd hh:mm:ss")
    private String refuseTime;
    @Schema(description = "授信时间", example = "yyyy-mm-dd hh:mm:ss")
    private String auditTime;
    @Schema(description = "授信总金额")
    private String auditAmount;
    @Schema(description = "是否提现", allowableValues = "0,1", example = "1是0否")
    private String ifLent;
    @Schema(description = "提现时间", example = "yyyy-mm-dd hh:mm:ss")
    private String lentTime;
    @Schema(description = "提现金额")
    private String lentAmount;
    @Schema(description = "未提现额度")
    private String unlentAmount;
    @Schema(description = "是否结清", allowableValues = "0,1", example = "1是0否")
    private String ifSettle;
    @Schema(description = "结清时间", example = "yyyy-mm-dd hh:mm:ss")
    private String settleTime;
    @Schema(description = "紧急扩展字段", example = "客户传输的任意值")
    private String extStr;
    @Schema(description = "拍拍贷扩展字段", example = "90d,180d,360d,720d")
    private String desleep;
}
