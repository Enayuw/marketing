package com.br.marketing.monkey.job.taikang.util;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author itw_xuzw01
 */
@Data
public class Result<T> implements Serializable {

    private static final String pattern = "yyyyMMddHHmmss";
    private String code;
    private String message;
    private String timestamp;
    private T data;

    public Result() {

    }

    public Result(T data, String code, String message) {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
