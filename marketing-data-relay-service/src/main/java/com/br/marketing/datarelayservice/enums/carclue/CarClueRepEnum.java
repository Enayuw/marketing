package com.br.marketing.datarelayservice.enums.carclue;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CarClueRepEnum {

    SUCCESS(1,"成功"),

    FAIL(0,"失败");

    private Integer code;

    private String desc;
}
