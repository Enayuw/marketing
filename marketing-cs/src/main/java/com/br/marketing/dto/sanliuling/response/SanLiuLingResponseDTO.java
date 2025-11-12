package com.br.marketing.dto.sanliuling.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;

import java.io.Serializable;

@Data
public class SanLiuLingResponseDTO implements Serializable {

    @Schema(description = "响应码")
    private String code;
    @Schema(description = "响应消息")
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
        this.msg = msg;
        return this;
    }


    @Getter
    public enum ResultEnum {

        SUCCESS("S", "成功"),
        FAILED_PARAM_ERROR("F", "失败")
        ;

        private String code;
        private String desc;

        ResultEnum(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

    }
}
