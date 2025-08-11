package com.br.marketing.client.intelligentcustomerservice.input;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class PushMarketingUserDetailDTO implements Serializable {

    public static final long serialVersionUID = 1L;

    /**
     * 案件编号
     */
    private String caseNumber;

    /**
     *手机号码
     */
    private String phone;

    /**
     *手机号码（log加密）
     */
    private String logCell;

    /**
     *变量JSON
     */
    private JSONObject variables;

    private String strategyCode;

}
