package com.br.marketing.service.carclue.clueenums;

import lombok.Getter;

/**
 * 车线索匹配类型枚举
 *
 * @author zhen.li1
 * @date 2025/01/20
 */
@Getter
public enum CarClueMatchTypeEnum {

    COMPLETE_MATCH(1, "精确匹配"),
    FUZZY_MATCH(2, "模糊匹配");

    CarClueMatchTypeEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    private Integer value;

    private String desc;




}
