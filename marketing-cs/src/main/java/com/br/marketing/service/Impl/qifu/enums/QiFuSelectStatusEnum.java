package com.br.marketing.service.Impl.qifu.enums;

import lombok.Getter;

/**
 * 奇富选择状态枚举
 */
@Getter
public enum QiFuSelectStatusEnum {
    
    /**
     * 待查询
     */
    WAIT_QUERY(0, "待查询"),
    
    /**
     * 查询中
     */
    QUERYING(1, "查询中"),
    
    /**
     * 查询成功
     */
    QUERY_SUCCESS(2, "查询成功"),
    
    /**
     * 重试（接口异常）
     */
    RETRY_INTERFACE_ERROR(3, "重试（接口异常）"),
    
    /**
     * 重试（无卷信息）
     */
    RETRY_NO_COUPON(4, "重试（无卷信息）");

    private final Integer code;
    private final String desc;

    QiFuSelectStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取枚举
     */
    public static QiFuSelectStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (QiFuSelectStatusEnum statusEnum : values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }

    /**
     * 根据code获取描述
     */
    public static String getDescByCode(Integer code) {
        QiFuSelectStatusEnum statusEnum = getByCode(code);
        return statusEnum != null ? statusEnum.getDesc() : null;
    }
}

