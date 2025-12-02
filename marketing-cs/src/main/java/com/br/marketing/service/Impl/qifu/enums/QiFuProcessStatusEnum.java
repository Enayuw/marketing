package com.br.marketing.service.Impl.qifu.enums;

import lombok.Getter;

/**
 * 奇富处理状态枚举
 */
@Getter
public enum QiFuProcessStatusEnum {
    
    /**
     * 未处理
     */
    UNPROCESSED(0, "未处理"),
    
    /**
     * 处理中
     */
    PROCESSING(1, "处理中"),
    
    /**
     * 处理完成
     */
    COMPLETED(2, "处理完成");

    private final Integer code;
    private final String desc;

    QiFuProcessStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取枚举
     */
    public static QiFuProcessStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (QiFuProcessStatusEnum statusEnum : values()) {
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
        QiFuProcessStatusEnum statusEnum = getByCode(code);
        return statusEnum != null ? statusEnum.getDesc() : null;
    }
}

