package com.br.marketing.client.dassservice.input;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class IbuReqDTO {
    private String data;
    private String accessKey;
    private String sign;
    private long ts;


    @Data
    public static class Datum {
        @JsonIgnore
        private Long id;
        private String uid;
        private String userType;
        private String purpose;
        private String userCode;
        private String userName;
        private String gender;
        private String phone;
        private String signInTimeStr;
        private String clickProductName;
        private String clickTimeStr;
        private List<String> recommendList;
        private List<String> recommendH5List;
        private String basicInfo;
        private String realName;
        private String supplement;
        private String contract;
        private String operator;
        private String loanProductName;
        private String loanTimeStr;
        private String createTimeStr;
        private String diffAmount;
        private String faceRecognition;
        private String firstApproveResult;
        private String firstApproveTimeStr;
        private String hasBindCard;
        private String hasEverBorrow;
        private String hasWithdraw;
        private String insteadCommitFlag;
        private String insteadCommitPname;
        private String isTimely;
        private String loanFailedTimeStr;
        private String loanSuccessTimeStr;
        private String loanWillingness;
        private String aCardScore;
        private String bucketName;
        private String overdueDays;
        private String prepayAmount;
        private String prepayPname;
        private String prepayTimeStr;
        private String repayPname;
        private String repayAmount;
        private String repayTimeStr;
        private String secondApproveResult;
        private String secondApproveTimeStr;
        private String applyAmount;
        private String approveAmount;
        private String source;
        private String prodType;
        private String score;
        private String callTimes;
        private Integer callAccessScore;
        private String remark;
        private String grade;
        private String totalAmount;
        private String surplusAmount;
        private Integer pid;
        private String pchannel;
        private String channelName;
        private String marketPurpose;
        private String riskControlLabel;
        private String firstLoginTimeStr;
        private Integer planId;
        private String goalsApp;
        private String flowSideName;
        private String flowSidePath;
        private String cusTag;
        private String abgroupPushOffsetStr;
        private String extra1;
        private String extra2;
        private String extra3;
        private String reserveField1;
        private String creditTimeStr;
        private String creditChannel;
        private String amountStatus;
        private Integer connectTimes;
        private Boolean zyApplyFlag;
        private Boolean zyApplySuccessFlag;
        private String zyAmountStatus;
        private BigDecimal zyTotalUsableAmount;
        private String isIdnumber;
        private String isTaobao;
        private String isNuclearapproval;
        private String callaccessscore;
        private String marketingScore;
        private String noWithdrawOrders;
        private String planData;
        private String priorityScore;
        private String callType;
    }
}
