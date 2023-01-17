package com.br.marketing.entity;

import lombok.Data;

import java.util.List;

/**
 * @author GuangChao.Zhang
 * @version 1.0
 * @date 2023/1/9 17:37
 */
@Data
public class XieChengSmsCollidingReq {

    private String appId;

    private List<String> sha256CodeList;

    private String codeType;

    private String marketType;

    private Boolean marketFinanceUser;

    public XieChengSmsCollidingReq(String appId,List<String> sha256CodeList,String codeType,String marketType,Boolean marketFinanceUser){
        this.appId = appId;
        this.sha256CodeList = sha256CodeList;
        this.codeType = codeType;
        this.marketType = marketType;
        this.marketFinanceUser = marketFinanceUser;
    }
}
