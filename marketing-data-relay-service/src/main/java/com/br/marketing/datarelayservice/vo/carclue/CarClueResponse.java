package com.br.marketing.datarelayservice.vo.carclue;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.datarelayservice.enums.carclue.CarClueRepEnum;


public class CarClueResponse {
    private Integer code;

    private String message;



    public CarClueResponse setCode(Integer resultCode) {
        this.code = resultCode;
        return this;
    }

    public CarClueResponse setMessage(String message) {
        this.message = message;
        return this;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static CarClueResponse fromResult(Result result) {
        if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            return new CarClueResponse().setCode(CarClueRepEnum.SUCCESS.getCode()).setMessage("成功");
        }
        return new CarClueResponse().setCode(CarClueRepEnum.FAIL.getCode()).setMessage(result.getMessage());
    }
}
