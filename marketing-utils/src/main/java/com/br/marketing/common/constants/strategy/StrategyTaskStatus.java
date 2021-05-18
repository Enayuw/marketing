package com.br.marketing.common.constants.strategy;

/**
 * 策略任务状态
 * 策略状态 1 已完成计算 2 正在计算中 3 计算失败
 *
 * @author Wang Weiwei
 * @since 2018/3/20
 */
public enum StrategyTaskStatus {
    //已完成
    COMPLETION("1","已完成"),
    COMPUTING("2","正在计算中"),
    FAILTRUE("3","失败");
    private final String code;
    private final String message;

    private StrategyTaskStatus(String code, String message){
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }


}
