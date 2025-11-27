package com.br.marketing.common.commondto;


import com.br.marketing.common.enums.ServiceResultEnum;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "接口返回结果")
public class ApiResult<T> {
    @Schema(description = "状态码")
    private String code;
    @Schema(description = "结果数据")
    private T data;
    @Schema(description = "消息")
    private String message;

    public String getCode() {
        return code;
    }

    public ApiResult<T> setCode(String code) {
        this.code = code;
        return this;
    }

    public T getData() {
        return data;
    }

    public ApiResult<T> setData(T data) {
        this.data = data;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public ApiResult<T> setMessage(String message) {
        this.message = message;
        return this;
    }

    public ApiResult<T> fromResult(Result<T> result,Integer inner) {
        if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            if(inner == null){
                this.code= "00";
            }else{
                this.code = "000000";
            }
        } else {
            this.code = "100001";
        }
        this.data = result.getData();
        this.message = result.getMessage();

        return this;
    }

    public ApiResult<T> success() {
        ServiceResultEnum success = ServiceResultEnum.SUCCESS;
        this.code = success.getCode();
        this.message = success.getMessage();
        return this;
    }

    public ApiResult<T> success(T data) {
        ServiceResultEnum success = ServiceResultEnum.SUCCESS;
        this.code = success.getCode();
        this.data = data;
        this.message = success.getMessage();
        return this;
    }

    public ApiResult<T> success(String message) {
        this.code = ServiceResultEnum.SUCCESS.getCode();
        this.message = message;
        return this;
    }

    public ApiResult<T> success(ServiceResultEnum resultEnum) {
        this.code = resultEnum.getCode();
        this.message = resultEnum.getMessage();
        return this;
    }

    public ApiResult<T> success(String code, String message) {
        this.code = code;
        this.message = message;
        return this;
    }

    public ApiResult<T> success(T data, String message) {
        this.code = ServiceResultEnum.SUCCESS.getCode();
        this.data = data;
        this.message = message;
        return this;
    }

    public ApiResult<T> success(T data, ServiceResultEnum resultEnum) {
        this.code = resultEnum.getCode();
        this.data = data;
        this.message = resultEnum.getMessage();
        return this;
    }

    public ApiResult<T> success(T data, String code, String message) {
        this.data = data;
        this.code = code;
        this.message = message;
        return this;
    }

    public ApiResult<T> fail() {
        ServiceResultEnum success = ServiceResultEnum.FAILED;
        this.code = success.getCode();
        this.message = success.getMessage();
        return this;
    }

    public ApiResult<T> fail(String message) {
        this.code = ServiceResultEnum.UNKNOWN_ERROR.getCode();
        this.message = message;
        return this;
    }

    public ApiResult<T> fail(ServiceResultEnum resultEnum) {
        this.code = resultEnum.getCode();
        this.message = resultEnum.getMessage();
        return this;
    }

    public ApiResult<T> fail(T data, String message) {
        this.code = ServiceResultEnum.UNKNOWN_ERROR.getCode();
        this.data = data;
        this.message = message;
        return this;
    }

    public ApiResult<T> fail(T data, ServiceResultEnum resultEnum) {
        this.code = resultEnum.getCode();
        this.data = data;
        this.message = resultEnum.getMessage();
        return this;
    }

    public ApiResult<T> fail(String code, String message) {
        this.code = code;
        this.message = message;
        return this;
    }

    public ApiResult<T> fail(T data, String code, String message) {
        this.data = data;
        this.code = code;
        this.message = message;
        return this;
    }

}
