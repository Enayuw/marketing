package com.br.marketing.enums;

import lombok.Getter;

@Getter
public enum XcDeleteEnum {

    DELETE_GENERAL(0, "xcCollidingDelete"),
    DELETE_DYNAFALSE(1, "xcCollidingDeleteForDyna"),
    DELETE_BLACKLIST(2, "xcCollidingDeleteForBlack");

    private Integer type;

    private String key;

    XcDeleteEnum(Integer type, String key) {
        this.type = type;
        this.key = key;
    }


}
