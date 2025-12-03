package com.br.marketing.service.Impl.qifu.enums;

import lombok.Getter;

@Getter
public enum QiFuSyncStatusEnum {

    /**
     * 未同步
     */
    UN_SYNC(0, "未同步"),

    /**
     * 已同步
     */
    SYNC(1, "已同步");

    private final Integer code;
    private final String desc;

    QiFuSyncStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取枚举
     */
    public static QiFuSyncStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (QiFuSyncStatusEnum statusEnum : values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }

}
