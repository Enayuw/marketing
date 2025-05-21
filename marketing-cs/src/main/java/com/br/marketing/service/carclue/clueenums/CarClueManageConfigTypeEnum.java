package com.br.marketing.service.carclue.clueenums;

import lombok.Getter;

/**
 * @ClassName CarClueManageConfigTypeEnum
 * @Author kongbx
 * @Date 2025/5/13 15:53
 */
@Getter
public enum CarClueManageConfigTypeEnum {

    PERFORMED_MANUALLY(0, "手动执行"),
    AUTOMATED(1, "自动执行");

    CarClueManageConfigTypeEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    private Integer value;

    private String desc;

}
