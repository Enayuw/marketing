package com.br.marketing.dto.customer;

import lombok.Data;

@Data
public class SmsCallBackBO {

    private Long id;

    private String apiCode;

    private String userType;

    private Integer smsSendStatus;

    private String caseNum;

    private String createDate;

    private String reserveField1;

}
