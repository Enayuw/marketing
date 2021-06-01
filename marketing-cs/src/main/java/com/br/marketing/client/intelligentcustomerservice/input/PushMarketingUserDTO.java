package com.br.marketing.client.intelligentcustomerservice.input;

import lombok.Data;

import java.io.Serializable;

@Data
public class PushMarketingUserDTO<T>  implements Serializable {

    /**
     *客户的apicode
     */
    private String apiCode;

    /**
     *内部系统公共apicode
     */
    private String platApiCode;

    /**
     * 请求数据
     */
    private T jsonData;
}
