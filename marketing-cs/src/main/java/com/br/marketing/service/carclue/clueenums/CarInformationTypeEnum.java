package com.br.marketing.service.carclue.clueenums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @ClassName CarInformationTypeEnum
 * @Author kongbx
 * @Date 2025/4/9 13:50
 */
@AllArgsConstructor
@Getter
public enum CarInformationTypeEnum {
    BRAND(0, "品牌"),
    SERIES(1, "车系");

    private Integer value;
    private String desc;
}
