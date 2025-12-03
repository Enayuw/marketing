    package com.br.marketing.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @ClassName ZhongYuanCleanStatusEnum
 * @Description 中原消金清洗状态枚举
 * @Author kongbx
 * @Date 2025/11/24 15:42
 */
@Getter
@AllArgsConstructor
public enum ZhongYuanCleanStatusEnum {
    
    PENDING(0, "待清洗"),
    PROCESSING(1, "清洗中"),
    COMPLETED(2, "清洗完成");

    private Integer value;
    private String desc;
}