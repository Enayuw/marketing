package com.br.marketing.common.exception.auth;


import com.br.marketing.common.exception.LoanRunTimeException;

/** 贷中用户异常，该异常主要发生在权限校验时
 * @author Wang Weiwei
 * @since 2018/3/12
 */
public class LoanAuthException extends LoanRunTimeException {
    public LoanAuthException(String message) {
        super(message);
    }

    public LoanAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
