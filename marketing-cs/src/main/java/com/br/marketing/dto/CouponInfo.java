package com.br.marketing.dto;

import com.br.marketing.service.Impl.qifu.enums.CouponType;

/**
 * 券信息类
 *
 * @author guangxiu.li
 * @className CouponInfo
 * @date 2025/5/23
 * @description TODO
 */
public class CouponInfo {
    private String originalName;
    private String cleanedName;
    private CouponType type;
    private double value;
    private int originalIndex;

    public CouponInfo(String originalName, String cleanedName, CouponType type, double value, int originalIndex) {
        this.originalName = originalName;
        this.cleanedName = cleanedName;
        this.type = type;
        this.value = value;
        this.originalIndex = originalIndex;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getCleanedName() {
        return cleanedName;
    }

    public CouponType getType() {
        return type;
    }

    public double getValue() {
        return value;
    }

    public int getOriginalIndex() {
        return originalIndex;
    }
}
