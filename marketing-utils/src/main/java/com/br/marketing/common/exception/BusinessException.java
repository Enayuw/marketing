package com.br.marketing.common.exception;

import com.br.marketing.common.enums.ServiceResultEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 自定义业务异常
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/8/31 19:53
 */
@Setter
@Getter
@ToString
public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = -4667804982239017435L;
    /**
     * 2021/9/1 11:46 自定义状态码
     */
    private String code;
    /**
     * 2021/9/1 11:46 自定义消息
     */
    private String msg;
    /**
     * 2021/9/1 11:45 异常信息
     */
    private String exceptionMessage;
    /**
     * 2021/9/1 11:46 异常
     */
    private Exception exception;

    public BusinessException() {
        super();
        ServiceResultEnum failed = ServiceResultEnum.SUCCESS_5;
        this.code = failed.getCode();
        this.msg = failed.getMessage();
    }

    public BusinessException(String msg) {
        super();
        this.code = ServiceResultEnum.SUCCESS_5.getCode();
        this.msg = msg;
    }

    public BusinessException(ServiceResultEnum resultEnum) {
        super();
        this.code = resultEnum.getCode();
        this.msg = resultEnum.getMessage();
    }

    public BusinessException(String code, String msg) {
        super();
        this.code = code;
        this.msg = msg;
    }

    public BusinessException(String msg, Exception exception) {
        super();
        this.code = ServiceResultEnum.SUCCESS_5.getCode();
        this.msg = msg;
        this.exceptionMessage = exception.getMessage() == null ? msg : exception.getMessage();
        this.exception = exception;
    }

    public BusinessException(String code, String msg, Exception exception) {
        super();
        this.code = code;
        this.msg = msg;
        this.exceptionMessage = exception.getMessage() == null ? msg : exception.getMessage();
        this.exception = exception;
    }

    public BusinessException(String code, String msg, String exceptionMessage, Exception exception) {
        super();
        this.code = code;
        this.msg = msg;
        this.exceptionMessage = exceptionMessage == null ? msg : exceptionMessage;
        this.exception = exception;
    }

    public BusinessException(String message, String code, String msg, String exceptionMessage, Exception exception) {
        super(message);
        this.code = code;
        this.msg = msg;
        this.exceptionMessage = exceptionMessage == null ? msg : exceptionMessage;
        this.exception = exception;
    }

    public BusinessException(String message, Throwable cause, String code, String msg, String exceptionMessage, Exception exception) {
        super(message, cause);
        this.code = code;
        this.msg = msg;
        this.exceptionMessage = exceptionMessage == null ? msg : exceptionMessage;
        this.exception = exception;
    }

    public BusinessException(Throwable cause, String code, String msg, String exceptionMessage, Exception exception) {
        super(cause);
        this.code = code;
        this.msg = msg;
        this.exceptionMessage = exceptionMessage == null ? msg : exceptionMessage;
        this.exception = exception;
    }

    public BusinessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, String code, String msg, String exceptionMessage, Exception exception) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.code = code;
        this.msg = msg;
        this.exceptionMessage = exceptionMessage == null ? msg : exceptionMessage;
        this.exception = exception;
    }
}
