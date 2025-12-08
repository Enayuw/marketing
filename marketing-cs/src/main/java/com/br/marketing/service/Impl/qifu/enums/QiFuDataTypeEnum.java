package com.br.marketing.service.Impl.qifu.enums;

import lombok.Getter;

/**
 * 奇富数据类型枚举（是否实时数据）
 */
@Getter
public enum QiFuDataTypeEnum {
    
    /**
     * 非实时数据
     */
    NON_REALTIME(0, "非实时数据"),
    
    /**
     * 实时数据
     */
    REALTIME(1, "实时数据");

    private final Integer code;
    private final String desc;

    QiFuDataTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取枚举
     */
    public static QiFuDataTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (QiFuDataTypeEnum typeEnum : values()) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }

    /**
     * 根据code获取描述
     */
    public static String getDescByCode(Integer code) {
        QiFuDataTypeEnum typeEnum = getByCode(code);
        return typeEnum != null ? typeEnum.getDesc() : null;
    }

    /**
     * 判断是否为实时数据
     */
    public static boolean isRealtime(Integer code) {
        return REALTIME.getCode().equals(code);
    }
}

