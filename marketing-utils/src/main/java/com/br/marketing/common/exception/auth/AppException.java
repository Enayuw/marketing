package com.br.marketing.common.exception.auth;

import com.br.marketing.common.constants.auth.CodeEnum;

public class AppException  extends RuntimeException {
    private String code;

    public AppException(String code, String message) {
        super(message);
        this.setCode(code);
    }

    public AppException(CodeEnum codeEnum) {
        super(codeEnum.getMessage());
        this.setCode(codeEnum.getCode());
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
