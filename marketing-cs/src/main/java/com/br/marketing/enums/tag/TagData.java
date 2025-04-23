package com.br.marketing.enums.tag;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class TagData {

    @Getter
    @AllArgsConstructor
    public enum TableTypeEnum {
        BASE(1, "base表"),
        MATERIALIZED_VIEW(2, "物化视图");
        private Integer label;
        private String desc;
    }


    @Getter
    @AllArgsConstructor
    public enum TagCalculateStatusEnum {
        READY(0, "待执行"),
        RUNNING(1, "执行中"),
        COMPLETE(2, "执行结束");
        private Integer code;
        private String desc;
    }


}
