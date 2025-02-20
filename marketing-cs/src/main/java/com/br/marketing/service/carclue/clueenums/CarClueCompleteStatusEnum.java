package com.br.marketing.service.carclue.clueenums;

import lombok.Getter;

/**
 * 车线索补全状态枚举
 *
 * @author zhen.li1
 * @date 2025/01/15
 */
@Getter
public enum CarClueCompleteStatusEnum {

    NORMAL_COMPLETE(0, "未补全"),
    SYSTEM_COMPLETE(1, "系统补全"),
    AETIFICAL_LACK_COMPLETE(2, "缺失线索手动补全"),
    AETIFICAL_ABNORMAL_COMPLETE(3, "异常线索手动补全");


    CarClueCompleteStatusEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    private Integer value;

    private String desc;

}
