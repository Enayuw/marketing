package com.br.marketing.enums;

import lombok.Getter;

/**
 * 线程池调整策略枚举
 * 定义线程池大小调整的不同策略
 *
 * @author kongbx
 * @date 2025-09-19
 */
@Getter
public enum ThreadPoolAdjustmentEnum {
    INCREASE_THREADS("增加线程数"),
    DECREASE_THREADS("减少线程数"),
    NO_CHANGE("无需调整");
    
    private final String description;

    ThreadPoolAdjustmentEnum(String description) {
        this.description = description;
    }

}
