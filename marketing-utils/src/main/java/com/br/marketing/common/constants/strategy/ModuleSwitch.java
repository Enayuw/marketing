package com.br.marketing.common.constants.strategy;

/**
 * 规则集、贷前重审等模块的开启/关闭状态
 */
public enum ModuleSwitch {
    //关闭
    CLOSE(0,"关闭"),
    //打开
    OPEN(1,"打开"),
    //开通模块
    APPLY(1,"开通模块"),
    //未开通模块
    NOT_APPLY(0,"未开通模块");
    private final Integer code;
    private final String message;

    private ModuleSwitch(Integer code, String message){
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
