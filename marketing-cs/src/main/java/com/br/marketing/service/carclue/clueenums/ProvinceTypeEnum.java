package com.br.marketing.service.carclue.clueenums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @ClassName ProvinceTypeEnum
 * @Description 省市枚举 0-全国 1-固定 2-排除
 * @Author kongbx
 * @Date 2025/1/21 10:57
 */
@AllArgsConstructor
@Getter
public enum ProvinceTypeEnum {
    NATIONWIDE(0, "全国"),
    FIXED(1, "固定"),
    EXCLUDE(2, "排除");

    private Integer value;
    private String desc;
}
