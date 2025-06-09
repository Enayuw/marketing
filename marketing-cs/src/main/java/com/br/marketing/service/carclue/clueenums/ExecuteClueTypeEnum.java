package com.br.marketing.service.carclue.clueenums;

import lombok.Getter;

/**
 * @ClassName ExecuteClueTypeEnum
 * @Author kongbx
 * @Date 2025/5/6 18:06
 */
@Getter
public enum ExecuteClueTypeEnum {
    CLEAN(0, "清洗"),
    PUSH(1, "推送");


    ExecuteClueTypeEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    private Integer value;

    private String desc;

}
