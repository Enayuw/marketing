package com.br.marketing.common.exception.strategy;


import com.br.marketing.common.exception.LoanRunTimeException;

/** 贷中策略任务异常
 * 当子任务发生不可解决异常时，会被转化为该异常
 * @author Wang Weiwei
 * @since 2018/3/16
 */
public class LoanTaskException extends LoanRunTimeException {
    public LoanTaskException(String message) {
        super(message);
    }

    public LoanTaskException(String message, Throwable cause) {
        super(message, cause);
    }
}
