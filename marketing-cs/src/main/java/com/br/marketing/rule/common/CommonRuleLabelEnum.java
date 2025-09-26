package com.br.marketing.rule.common;




public enum CommonRuleLabelEnum {
    TO_POLICY_COMMON("To_Policy_Common"),
    TO_POLICY_GENERAL("To_Policy_General"),
    AI_TO_POLICY("AI_To_Policy"),
    AI_TO_POLICY_PATLOAN_OPERATYPE_FOUR("AI_To_Policy_PatLoan_OperaType_Four"),
    AI_TO_POLICY_PATLOAN_OPERATYPE_FIVE("AI_To_Policy_PatLoan_OperaType_Five"),
    AI_TO_POLICY_PATLOAN_OPERATYPE_SIX("AI_To_Policy_PatLoan_OperaType_Six");


    CommonRuleLabelEnum(String code) {
        this.code = code;
    }

    private final String code;

    public String getCode() {
        return this.code;
    }
}
