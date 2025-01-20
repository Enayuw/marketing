package com.br.marketing.service.carclue.clueenums;

import lombok.Getter;

/**
 * @ClassName ProvincesTypeEnum
 * @Description 渠道商
 * @Author kongbx
 * @Date 2025/1/20 21:37
 */
@Getter
public enum ProvincesTypeEnum {

    STARTZJ(0, "海星之家"),
    YCKA(1, "易车KA"),
    YCMEMBER(2, "易车会员");

    ProvincesTypeEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    private Integer value;

    private String desc;

}
