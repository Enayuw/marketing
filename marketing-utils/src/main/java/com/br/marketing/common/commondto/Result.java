package com.br.marketing.common.commondto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "返回的数据结构")
public class Result<T> {
    /**
     * 返回标识 1-成功；500-错误
     */
    @ApiModelProperty(value = "1-成功；500-错误")
    private Integer code;

    /**
     * 返回信息
     */
    @ApiModelProperty(value = "返回的信息")
    private String message;

    @ApiModelProperty(value = "返回的数据")
    private T data;

    public Result setCode(Integer code) {
        this.code = code;
        return this;
    }

    public Result setDate(T data) {
        this.data = data;
        return this;
    }

    public Result setMessage(String message){
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
}
