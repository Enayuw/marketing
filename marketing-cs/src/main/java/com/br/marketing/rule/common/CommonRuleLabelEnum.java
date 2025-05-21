package com.br.marketing.rule.common;




public enum CommonRuleLabelEnum {
    TO_POLICY_COMMON("To_Policy_Common"),
    TO_POLICY_GENERAL("To_Policy_General"),
    AI_To_Policy_PatLoan_OperaType_Four("AI_To_Policy_PatLoan_OperaType_Four");


    CommonRuleLabelEnum(String code) {
        this.code = code;
    }

    private final String code;

    public String getCode() {
        return this.code;
    }
}
