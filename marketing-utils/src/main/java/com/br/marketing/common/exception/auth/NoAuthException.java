package com.br.marketing.common.exception.auth;

/**
 * ${tags}
 *
 * @version V1.0
 * @ClassName: ${type_name}
 * @author: xiaowen.wang
 * @company BaiRong
 * @date ${date} ${time}
 * ${tags}
 * @Motified by:
 */
public class NoAuthException extends LoanAuthException {
    public NoAuthException(String message) {
        super(message);
    }

    public NoAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
