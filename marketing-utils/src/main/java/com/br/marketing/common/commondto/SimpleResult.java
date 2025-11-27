package com.br.marketing.common.commondto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "返回的数据结构")
public class SimpleResult<T> {
    /**
     * 返回标识 1-成功；500-错误
     */
    @Schema(description = "1-成功；500-错误")
    private Integer code;

    /**
     * 返回信息
     */
    @Schema(description = "返回的信息")
    private String message;

    @Schema(description = "返回的数据")
    private T data;

    public SimpleResult setCode(Integer code) {
        this.code = code;
        return this;
    }

    public SimpleResult setDate(T data) {
        this.data = data;
        return this;
    }

    public SimpleResult setMessage(String message){
        this.message = message;
        return this;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public boolean isSuccess() {
        return ResultCode.SUCCESS.getValue().equals(this.code);
    }

    public SimpleResult success() {
        this.setCode(ResultCode.SUCCESS.getValue());
        return this;
    }

    public SimpleResult failure() {
        this.setCode(ResultCode.FAIL.getValue());
        return this;
    }
}
