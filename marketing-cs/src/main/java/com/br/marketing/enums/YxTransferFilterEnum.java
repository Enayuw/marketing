package com.br.marketing.enums;

import lombok.Getter;

import java.util.Set;
import java.util.TreeSet;

/**
 * YxTransferFilterEnum
 */
@Getter
public enum YxTransferFilterEnum {

    YX_TRANSFER_FILTER01("YxTransferFilter01", 1),
    YX_TRANSFER_FILTER02("YxTransferFilter01", 2),
    YX_TRANSFER_FILTER03("YxTransferFilter01", 3),
    YX_TRANSFER_FILTER04("YxTransferFilter01", 4),
    YX_TRANSFER_FILTER05("YxTransferFilter01", 5),
    ;

    private String name;
    private Integer priority;


    YxTransferFilterEnum(String name, Integer priority) {
        this.name = name;
        this.priority = priority;
    }

    public static YxTransferFilterEnum getEnumByCode(Integer name) {
        for (YxTransferFilterEnum e : YxTransferFilterEnum.values()) {
            if (e.getName().equals(name)) {
                return e;
            }
        }
        return null;
    }

    public static Set<String> getFilterSetOrderByPriority () {
        Set set = new TreeSet();
        for (YxTransferFilterEnum e : YxTransferFilterEnum.values()) {
            set.add(e.getName());
        }
        return set;
    }
}
