package com.br.marketing.enums;

import lombok.Getter;

@Getter
public enum DataMarkEnum {

    //交集标记
    MARK_INTERSECTION(0, "INTERSECTION"),
    //客群标记
    MARK_RISKGROUP(1, "RISKGROUPANDINTEREST"),
    //利率标记
    MARK_INTEREST(2, "RISKGROUPANDINTEREST"),
    //高风险标记
    MARK_HIGHRISK(3, "HIGHRISK"),
    //黑名单标记
    MARK_BLACKLIST(4, "BLACKLIST"),
    //白名单标记
    MARK_WHITELIST(5, "WHITELIST");

    //标记类型
    private Integer markType;

    //标记redis key
    private String markRedisKey;

    DataMarkEnum(Integer markType, String markRedisKey) {
        this.markType = markType;
        this.markRedisKey = markRedisKey;
    }
}
