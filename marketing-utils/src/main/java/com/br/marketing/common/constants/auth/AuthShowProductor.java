package com.br.marketing.common.constants.auth;

/**
 * 策略结果是否展示数据产品枚举
 *
 * @author Wang Weiwei
 * @since 2018/3/24
 */
public enum AuthShowProductor {
    //不展示数据产品
    HIDE(0, "否"),
    //展示数据产品
    SHOW(1, "是"),
    NORMAL(1, "正常"),
    NO_NORMAL(2, "删除");


    private Integer code;
    private String message;

    private AuthShowProductor(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static AuthShowProductor codeToEnum(Integer code) {
        for (AuthShowProductor showProductor : values()) {
            if (showProductor.getCode().equals(code)) {
                return showProductor;
            }
        }
        return null;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
