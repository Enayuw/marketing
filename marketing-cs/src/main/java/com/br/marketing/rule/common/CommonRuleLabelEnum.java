package com.br.marketing.rule.common;




public enum CommonRuleLabelEnum {
    TO_POLICY_COMMON("To_Policy_Common");


    CommonRuleLabelEnum(String code) {
        this.code = code;
    }

    private final String code;

    public String getCode() {
        return this.code;
    }
}
