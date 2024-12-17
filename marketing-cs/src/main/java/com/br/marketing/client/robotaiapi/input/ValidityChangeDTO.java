package com.br.marketing.client.robotaiapi.input;

import lombok.Data;

import java.util.List;

@Data
public class ValidityChangeDTO {

    /**
     * Api接口具体方法名
     */
    private String method;

    /**
     * 客户请求流水号
     */
    private String accessNumber;

    /**
     * 有效期生效时间
     */
    private String validStartDate;

    /**
     * 有效期失效时间
     */
    private String validEndDate;

    /**
     * 数据
     */
    private List<CaseNumDTO> data;
}
