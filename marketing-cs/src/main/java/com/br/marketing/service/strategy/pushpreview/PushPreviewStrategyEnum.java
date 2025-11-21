package com.br.marketing.service.strategy.pushpreview;

import lombok.Getter;

/**
 * 推送预览策略类型枚举
 *
 * @author system
 * @date 2025-11-09
 */
@Getter
public enum PushPreviewStrategyEnum {

    /**
     * 上传任务策略
     */
    UPLOAD_TASK("UPLOAD_TASK", "上传任务"),

    /**
     * 携程跑分任务策略
     */
    XIE_CHENG_SCORE("XIE_CHENG_SCORE", "携程跑分任务"),

    /**
     * 合并跑分任务策略
     */
    MERGE_SCORE("MERGE_SCORE", "合并跑分任务"),

    /**
     * 通用跑分任务策略
     */
    COMMON_SCORE("COMMON_SCORE", "通用跑分任务");

    /**
     * 策略代码
     */
    private final String code;

    /**
     * 策略描述
     */
    private final String desc;

    PushPreviewStrategyEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

