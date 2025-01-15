package com.br.marketing.enums.carclue;

import lombok.Getter;

/**
 * 车线索状态枚举
 *
 * @author zhen.li1
 * @date 2025/01/15
 */
@Getter
public enum CarClueDataStatusEnum {


    READY(0, "待清洗"),
    NORMAL_CLUE(1, "有效线索"),
    ABNORMAL_CLUE(2, "异常线索"),
    LACK_CLUE(3, "缺失线索"),
    INVALID_CLUE(4, "无效线索");

    CarClueDataStatusEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    private Integer value;

    private String desc;


}
