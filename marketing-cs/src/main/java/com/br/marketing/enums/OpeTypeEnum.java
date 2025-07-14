package com.br.marketing.enums;

import lombok.Getter;

@Getter
public enum OpeTypeEnum {

    OPE_TYPE_INS(1, "新增"),
    OPE_TYPE_UPD(2, "变更"),
    OPE_TYPE_DEL(3, "删除"),
    OPE_TYPE_FOB(4, "禁用"),
    OPE_TYPE_ALLOW(5, "启用"),
    ;

    private Integer type;

    private String desc;

    OpeTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }


}
