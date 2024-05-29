package com.br.marketing.bridge.common.enums;

/**
 * ErrorFileTypeEnum
 */
public enum ErrorFileTypeEnum {
    ERROR_FILE("errorFile"),
    ERROR_DATA("errorData"),
    ERROR_CONFIG("errorConfig");
    private String type;
    ErrorFileTypeEnum(String type){
        this.type = type;
    }
}
