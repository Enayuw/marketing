package com.br.marketing.common.exception;

import lombok.Data;

/** 贷中异常 - 该异常为运行时异常
 *
 * 发生该异常时可方便进行异步报警
 * @author Wang Weiwei
 * @since 2018/3/12
 */
@Data
public class LoanRunTimeException extends RuntimeException {

    public LoanRunTimeException(String message) {
        super(message);
    }

    public LoanRunTimeException(String message, Throwable cause) {
        super(message, cause);
    }


    /**
     * 异常时报警方法
     * @param mails 要进行异常发送的邮件
     * */
    public void warning(String[] mails){}

}
