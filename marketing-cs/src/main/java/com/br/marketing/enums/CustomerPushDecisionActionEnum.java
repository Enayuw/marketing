package com.br.marketing.enums;

/**
 * 客户推送决策
 * 排序规则: 位置越靠前，优先级越高
 *
 * @author Guo Zeqiang
 * @dateTime 2023-04-11 10:50
 */
public enum CustomerPushDecisionActionEnum {

    /**
     * 2023-04-11 16:22
     * 众安客户推送
     */
    ZHONG_AN,
    ZHONG_AN2,
    PPD,
    YILIAN,
    RONG_SHU,
    ZHONGAN_AUTOTASK
    ;

    CustomerPushDecisionActionEnum() {
    }


}
