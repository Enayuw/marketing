package com.br.marketing.dto.wuba;

import lombok.Data;

@Data
public class ConversionReponseDTO {

    private Long id;
    private String mobileEncrypt;
    private String lastLoginTime;
    private String financeApplyTime;
    private String financeCreditStatus;
    private String financeCreditFinishTime;
    private String debtTime;
    private String debtPassTime;
    private String loanAmt;
    private String extend;


}

