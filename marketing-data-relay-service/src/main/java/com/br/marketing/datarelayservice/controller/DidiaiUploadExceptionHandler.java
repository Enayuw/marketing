package com.br.marketing.datarelayservice.controller;

import com.br.marketing.datarelayservice.client.DidiaiResponseDTO;
import com.br.marketing.datarelayservice.enums.DidiaiErrorCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 滴滴上传控制器专用异常处理类。
 *
 * <p>通过 RestControllerAdvice 限定仅对 DidiaiUploadController 类型生效，并将多种异常映射为
 * DidiaiResponseDTO，使接口在出现解析错误、缺头、未捕获异常时仍返回与业务成功时相同外层结构的 JSON，
 * 便于对端统一解析。使用最高优先级顺序，避免被更通用的全局异常处理器抢先处理导致响应体格式不一致。
 *
 * @author yueping.bai
 */
@RestControllerAdvice(assignableTypes = DidiaiUploadController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class DidiaiUploadExceptionHandler {

    /**
     * 将时间戳字符串无法转换为 long 的情况转为参数非法类业务响应。
     *
     * <p>典型场景：请求头 timestamp 含有非数字字符或空串在其它层未被拦截时进入解析。
     *
     * @param e 数字格式异常，包含 JVM 提供的简短原因信息
     * @return errorCode 为校验失败枚举对应数值，errorMsg 固定为时间戳非法提示
     */
    @ExceptionHandler(NumberFormatException.class)
    public DidiaiResponseDTO onBadTimestamp(NumberFormatException e) {
        log.warn("[DiDi-AI-API] 接入异常，errorMsg={}", e.getMessage());
        return DidiaiResponseDTO.fail(
                DidiaiErrorCodeEnum.BAD_TIMESTAMP.getCode(),
                DidiaiErrorCodeEnum.BAD_TIMESTAMP.getMessage());
    }

    /**
     * 将使用 Spring RequestHeader 注解等机制声明的必填头缺失异常转为业务响应。
     *
     * <p>说明：若控制器改为手动读头，则本方法可能长期不被触发，保留用于兼容使用 RequestHeader 注解等声明式绑定的场景。
     *
     * @param e 携带缺失头名称等信息的框架异常
     * @return 参数校验失败类响应，文案中包含缺失头名称
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public DidiaiResponseDTO onMissingHeader(MissingRequestHeaderException e) {
        log.warn("[DiDi-AI-API] 接入异常，errorMsg={}", e.getMessage());
        String name = e.getHeaderName();
        if ("appKey".equalsIgnoreCase(name) || "app-key".equalsIgnoreCase(name)) {
            return DidiaiResponseDTO.fail(
                    DidiaiErrorCodeEnum.MISSING_APP_KEY.getCode(),
                    DidiaiErrorCodeEnum.MISSING_APP_KEY.getMessage());
        }
        if ("timestamp".equalsIgnoreCase(name)) {
            return DidiaiResponseDTO.fail(
                    DidiaiErrorCodeEnum.MISSING_TIMESTAMP.getCode(),
                    DidiaiErrorCodeEnum.MISSING_TIMESTAMP.getMessage());
        }
        if ("sign".equalsIgnoreCase(name)) {
            return DidiaiResponseDTO.fail(
                    DidiaiErrorCodeEnum.MISSING_SIGN.getCode(),
                    DidiaiErrorCodeEnum.MISSING_SIGN.getMessage());
        }
        return DidiaiResponseDTO.fail(
                DidiaiErrorCodeEnum.RECORD_FIELD_MISSING.getCode(),
                "缺少请求头: " + name);
    }

    /**
     * 处理请求体无法反序列化为控制器方法参数的情况，例如 JSON 语法错误、类型不匹配等。
     *
     * @param e Spring 封装的消息不可读异常
     * @return JSON 非法类统一错误码与默认提示文案
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public DidiaiResponseDTO onNotReadable(HttpMessageNotReadableException e) {
        log.warn("[DiDi-AI-API] 接入异常，errorMsg={}", e.getMessage());
        return DidiaiResponseDTO.fail(
                DidiaiErrorCodeEnum.JSON_INVALID.getCode(),
                DidiaiErrorCodeEnum.JSON_INVALID.getMessage());
    }

    /**
     * 兜底处理该控制器内未被更具体处理器覆盖的任意异常。
     *
     * <p>返回未知错误码，message 优先使用异常信息，若为空则使用枚举默认说明，避免返回 null 给对端。
     *
     * @param e 任意未处理异常
     * @return 未知错误类业务响应
     */
    @ExceptionHandler(Exception.class)
    public DidiaiResponseDTO onAny(Exception e) {
        log.warn("[DiDi-AI-API] 接入异常，errorMsg={}", e.getMessage(), e);
        return DidiaiResponseDTO.fail(
                DidiaiErrorCodeEnum.UNKNOWN_ERROR.getCode(),
                StringUtils.defaultIfBlank(e.getMessage(), DidiaiErrorCodeEnum.UNKNOWN_ERROR.getMessage()));
    }
}
