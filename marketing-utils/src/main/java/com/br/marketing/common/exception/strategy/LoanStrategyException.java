package com.br.marketing.common.exception.strategy;


import com.br.marketing.common.exception.LoanRunTimeException;

/** 策略异常
 * @author Wang Weiwei
 * @since 2018/3/15
 */
public class LoanStrategyException extends LoanRunTimeException {

    public LoanStrategyException(String message) {
        super(message);
    }

    public LoanStrategyException(String message, Throwable cause) {
        super(message, cause);
    }
}
