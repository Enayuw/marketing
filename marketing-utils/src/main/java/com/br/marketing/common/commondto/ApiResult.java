package com.br.marketing.common.commondto;

import lombok.Data;


public class ApiResult<T> {
    private String code;

    private T data;

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

    public ApiResult<T> fromResult(Result<T> result){
        if(ResultCode.SUCCESS.getValue().equals(result.getCode())){
            this.code = "00";
        }else{
            this.code ="100001";
        }
        this.data = result.getData();
        this.message = result.getMessage();

        return this;
    }
}
