package com.br.marketing.dto.sanliuling.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;

import java.io.Serializable;

@Data
public class SanLiuLingResponseDTO implements Serializable {
    private static final long serialVersionUID = -1;

    @ApiModelProperty("响应码")
    private Integer code;
    @ApiModelProperty("响应消息")
    private String msg;

    public SanLiuLingResponseDTO success() {
        this.code = SanLiuLingResponseDTO.ResultEnum.SUCCESS.getCode();
        this.msg = SanLiuLingResponseDTO.ResultEnum.SUCCESS.getDesc();
        return this;
    }

    public SanLiuLingResponseDTO failed(SanLiuLingResponseDTO.ResultEnum resultEnum) {
        this.code = resultEnum.getCode();
        this.msg = resultEnum.getDesc();
        return this;
    }

    public SanLiuLingResponseDTO failed(SanLiuLingResponseDTO.ResultEnum resultEnum, String msg) {
        this.code = resultEnum.getCode();
        this.msg = resultEnum.getDesc().concat(msg);
        return this;
    }


    @Getter
    public enum ResultEnum {

        SUCCESS(0, "成功"),
        FAILED_PARAM_ERROR(400, "失败,参数校验错误"),
        FAILED_SYSTEM_ERROR(500, "失败,系统异常"),
        FAILED_BIZ_ERROR(501, "失败，业务异常"),
        ;

        private int code;
        private String desc;

        ResultEnum(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

    }
}
