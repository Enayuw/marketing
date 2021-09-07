package com.br.marketing.innerapi.exception;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.exception.BusinessException;
import com.br.marketing.common.exception.auth.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.br.marketing.common.enums.ServiceResultEnum.FAILED;
import static com.br.marketing.common.enums.ServiceResultEnum.UNKNOWN_ERROR;

/**
 * 全局处理请求异常
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/8/31 19:48
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionAdvice {

    /**
     * 自定义业务异常
     *
     * @author zeqiang.guo@brgroup.com
     * @dateTime 2021/9/1 10:47
     */
    @ExceptionHandler(value = AppException.class)
    @ResponseBody
    public ApiResult<Object> businessException(AppException exception, HttpServletRequest request, HttpServletResponse response) {
        return new ApiResult<>().fail(exception.getCode(), exception.getMessage());
    }


    /**
     * 自定义业务异常
     *
     * @author zeqiang.guo@brgroup.com
     * @dateTime 2021/9/1 10:47
     */
    @ExceptionHandler(value = BusinessException.class)
    @ResponseBody
    public ApiResult<Object> businessException(BusinessException exception, HttpServletRequest request, HttpServletResponse response) {
        Exception e = exception.getException();
        log.warn(request.getRequestURI());
        log.error(exception.getMsg());
        if (!ObjectUtils.isEmpty(e)) {
            log.error(e.getMessage(), e);
        }
        return new ApiResult<>().fail(exception.getCode(), exception.getMsg());
    }

    /**
     * 空指针异常
     *
     * @author zeqiang.guo@brgroup.com
     * @dateTime 2021/9/1 10:47
     */
    @ExceptionHandler(value = {NullPointerException.class, ArrayIndexOutOfBoundsException.class})
    @ResponseBody
    public ApiResult<Object> nullPointerException(Exception exception, HttpServletRequest request, HttpServletResponse response) {
        log.warn(request.getRequestURI());
        log.error(exception.getMessage(), exception);
        return new ApiResult<>().fail(FAILED);
    }

    /**
     * 其他异常
     *
     * @author zeqiang.guo@brgroup.com
     * @dateTime 2021/9/1 10:47
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ApiResult<Object> exception(Exception exception, HttpServletRequest request, HttpServletResponse response) {
        log.warn(request.getRequestURI());
        log.error(exception.getMessage(), exception);
        return new ApiResult<>().fail(UNKNOWN_ERROR);
    }
}
