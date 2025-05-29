package com.br.marketing.client.dassservice.input.update;

import lombok.Data;

@Data
public class DaasUpdateDataDTO {


    private Long id;

    /**
     * 数据源
     */
    private String source;

    /**
     * ⽤户类型
     */
    private String userType;


    /**
     * 案件编号
     */
    private String uid;

    /**
     * 机构名称
     */
    private String orgname;

    /**
     * 扩展字段
     */
    private String extend;

    /**
     * 请求ID，用于幂等性校验
     */
    private String requestId;

}
