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


}
