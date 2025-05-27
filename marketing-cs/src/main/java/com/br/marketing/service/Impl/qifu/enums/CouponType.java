package com.br.marketing.service.Impl.qifu.enums;

/**
 * 券类型枚举
 * 定义了不同券类型的优先级（数字越小优先级越高）
 */
public enum CouponType {
    
    /**
     * 分期券 - 最高优先级
     */
    INSTALLMENT(1, "分期券"),
    
    /**
     * 大额直减券
     */
    LARGE_REDUCTION(2, "大额直减券"),
    
    /**
     * 周转金
     */
    TURNOVER(3, "周转金"),
    
    /**
     * 折扣券
     */
    DISCOUNT(4, "折扣券"),
    
    /**
     * 小额直减券
     */
    SMALL_REDUCTION(5, "小额直减券"),
    
    /**
     * 普通券 - 最低优先级
     */
    COMMON(6, "普通券");

    private final int priority;
    private final String name;

    CouponType(int priority, String name) {
        this.priority = priority;
        this.name = name;
    }

    /**
     * 获取优先级
     * @return 优先级数值，数字越小优先级越高
     */
    public int getPriority() {
        return priority;
    }

    /**
     * 获取券类型名称
     * @return 券类型中文名称
     */
    public String getName() {
        return name;
    }
} 