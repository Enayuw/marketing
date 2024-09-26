package com.br.marketing.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DistributeTypeEnum {
    CUSTOMERTRANSFER(1, "客服转化"),
    POLICYDATA(2, "决策数据"),
    DAAS_REAL_TIME_USER_ONE(3, "人工实时推送用户名单(单条)"),
    DAAS_TRANSFER(4, "人工转化"),
    ZHONGAN_PUSH_DETAIL(5, "众安明细推送"),
    YIXIN_TRANSFER_PUSH_BAIYING(6, "宜信转化推送百应"),
    YIXIN_REALTIME_POLICYDATA(7, "宜信实时推决策数据"),
    WUBA_SUBMIT_CONVERSION(5, "58新客提交营销名单"),
    YIXIN_TRANSFER_PUSH_BIOCLOO(9, "宜信转化推送百可录"),
    ;

    private Integer value;
    private String desc;
}
