package com.br.marketing.monkey.enums.syj;

import lombok.Getter;

@Getter
public enum QueryStatusEnum {

    NO_QUERIED(0, "未查询"),
    QUERYING(1, "查询中"),
    QUERY_FAILED(2, "查询失败"),
    QUERY_SUCCESS(3, "查询成功");


    private Integer code;

    private String description;

    QueryStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

}
