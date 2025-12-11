package com.br.marketing.monkey.enums.syj;

import lombok.Getter;

@Getter
public enum QueryStatusEnum {

    NO_QUERIED(0, "未查询"),
    QUERYING(1, "查询中"),
    QUERY_SUCCESS(2, "查询成功"),
    QUERY_FAILED(3, "查询失败"),
    QUERY_PARTIAL_SUCCESS(4, "部分成功");


    private Integer code;

    private String description;

    QueryStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

}
