package com.br.marketing.service.carclue.clueenums;

import lombok.Getter;

/**
 * 车线索回调状态枚举
 *
 * @author zhen.li1
 * @date 2025/01/15
 */
@Getter
public enum CarClueCallBackStatusEnum {

    READY(0, "待回调"),
    SUCCESS(1, "回调成功"),
    FAIL(2, "回调失败");


    CarClueCallBackStatusEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    private Integer value;

    private String desc;


}
