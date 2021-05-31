package com.br.marketing.client.intelligentcustomerservice.input;

import lombok.Data;

import java.io.Serializable;

@Data
public class PushMarketingUserDetailDTO implements Serializable {

    /**
     * 案件编号
     */
    private String caseNumber;

    /**
     *手机号码
     */
    private String phone;

    /**
     *变量JSON
     */
    private String variables;



}
