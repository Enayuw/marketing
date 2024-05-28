package com.br.marketing.enums;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * YxTransferFilterEnum
 */
@Getter
public enum YxTransferFilterEnum {

    YX_TRANSFER_FILTER01("YxTransferFilter01", 1),
    YX_TRANSFER_FILTER02("YxTransferFilter02", 2),
    YX_TRANSFER_FILTER03("YxTransferFilter03", 3),
    YX_TRANSFER_FILTER04("YxTransferFilter04", 4),
    ;

    private String name;
    private Integer priority;


    YxTransferFilterEnum(String name, Integer priority) {
        this.name = name;
        this.priority = priority;
    }

    public static YxTransferFilterEnum getEnumByName(String name) {
        for (YxTransferFilterEnum e : YxTransferFilterEnum.values()) {
            if (e.getName().equals(name)) {
                return e;
            }
        }
        return null;
    }

    public static List<String> getFilterListOrderByPriority () {
        List<String> list = new ArrayList();
        for (YxTransferFilterEnum e : YxTransferFilterEnum.values()) {
            list.add(e.getName());
        }
        list.sort(((name1, name2) -> {
            Integer priority1 = YxTransferFilterEnum.getEnumByName(name1).getPriority();
            Integer priority2 = YxTransferFilterEnum.getEnumByName(name2).getPriority();
            //从小到大
            return priority1 - priority2;
        }));
        return list;
    }
}
