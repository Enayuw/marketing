package com.br.marketing.client.intelligentcustomerservice.output;

import lombok.Data;

@Data
public class PolicyResultByTaskIdsDTO {


    /**
     * 请求批次号：b_customer_info_push_main的主键id
     */
    private String verification;


    /**
     * 该批次失败原因
     */
    private String verificationReason;


}



