package com.br.marketing.client.robotaiapi.input;

import lombok.Getter;

/**
 * @author lizhen
 * @date 2022/03/25 16:55
 * @Description 手机号加密类型枚举
 */
@Getter
public enum PhoneEncryptTypeEnum {

    MD5_TYPE(1,"MD5"),
    LOG_TYPE(2,"LOG");


    private String encryptType;
    private int encryptCode;

    PhoneEncryptTypeEnum(int encryptCode, String encryptType) {
        this.encryptCode = encryptCode;
        this.encryptType = encryptType;
    }


}
