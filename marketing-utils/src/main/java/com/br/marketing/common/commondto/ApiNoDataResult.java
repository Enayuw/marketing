package com.br.marketing.common.commondto;


import com.br.marketing.common.utils.StringUtils;

public class ApiNoDataResult<T> {
    private String code;

    private String message;

    public String getCode() {
        return code;
    }

    public ApiNoDataResult<T> setCode(String code) {
        this.code = code;
        return this;
    }


    public String getMessage() {
        return message;
    }

    public ApiNoDataResult<T> setMessage(String message) {
        this.message = message;
        return this;
    }

    public ApiNoDataResult<T> fromResult(Result<T> result){
        if(ResultCode.SUCCESS.getValue().equals(result.getCode())){
            this.code = "00";
            if(StringUtils.isBlank(result.getMessage())){
                this.message="成功";
            }else{
                this.message= result.getMessage();
            }
        }else{
            this.code ="100001";
            this.message = result.getMessage();
        }
        return this;
    }
}
