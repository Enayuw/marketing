package com.br.marketing.entity;

import lombok.Data;


/**
 * Created by Bairong on 2019/8/19.
 */
@Data
public class MarketingUser {
    private Long id;
    private String apiCode;
    private String batchNumber;
    private String cusNum;
    private String idCard;
    private String name;
    private String cell;
    private String linkmanCell;
    private String homeAddr;
    private String telHome;
    private String mail;
    private String createTime;
    private String updateTime;
    private Integer timeRange;
    private String passDate;
    private String approvalResult;
    private String loanMaturityDate;
    private String hitData;
    private String userDate;
    private Integer status;
    private String decodeFailType;
    private String extendJson;
    private String taskId;
    private String userType;
    public MarketingUser() {
    }

    public MarketingUser(String apiCode, String batchNumber, String cusNum, String idCard, String name, String cell) {
        this.apiCode = apiCode;
        this.batchNumber = batchNumber;
        this.cusNum = cusNum;
        this.idCard = idCard;
        this.name = name;
        this.cell = cell;
    }

    public MarketingUser(String apiCode, String batchNumber, String cusNum) {
        this.apiCode = apiCode;
        this.batchNumber = batchNumber;
        this.cusNum = cusNum;
    }

    @Override
    public String toString() {
        return "LoanUser{" +
                "id=" + id +
                ", apiCode='" + apiCode + '\'' +
                ", batchNumber='" + batchNumber + '\'' +
                ", cusNum='" + cusNum + '\'' +
                ", idCard='" + idCard + '\'' +
                ", name='" + name + '\'' +
                ", cell='" + cell + '\'' +
                ", linkmanCell='" + linkmanCell + '\'' +
                ", homeAddr='" + homeAddr + '\'' +
                ", telHome='" + telHome + '\'' +
                ", mail='" + mail + '\'' +
                ", createTime='" + createTime + '\'' +
                ", updateTime='" + updateTime + '\'' +
                ", timeRange='" + timeRange + '\'' +
                ", passDate='" + passDate + '\'' +
                ", approvalResult='" + approvalResult + '\'' +
                ", loanMaturityDate='" + loanMaturityDate + '\'' +
                ", hitData='" + hitData + '\'' +
                ", userDate='" + userDate + '\'' +
                ", status=" + status +
                ", decodeFailType='" + decodeFailType + '\'' +
                ", extendJson='" + extendJson + '\'' +
                ", taskId='" + taskId + '\'' +
                ", userType='" + userType + '\'' +
                '}';
    }
}
