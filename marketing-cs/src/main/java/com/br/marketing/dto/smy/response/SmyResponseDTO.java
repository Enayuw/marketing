package com.br.marketing.dto.smy.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Data;
import lombok.Getter;

@Data
public class SmyResponseDTO implements Serializable {
    private static final long serialVersionUID = 8376361895732046747L;

    @Schema(description = "响应码")
    private Integer code;
    @Schema(description = "响应消息")
    private String message;

    public SmyResponseDTO success() {
        this.code = SmyResponseDTO.ResultEnum.SUCCESS.getCode();
        this.message = SmyResponseDTO.ResultEnum.SUCCESS.getDesc();
        return this;
    }

    public SmyResponseDTO failed(SmyResponseDTO.ResultEnum resultEnum) {
        this.code = resultEnum.getCode();
        this.message = resultEnum.getDesc();
        return this;
    }

    public SmyResponseDTO failed(SmyResponseDTO.ResultEnum resultEnum, String msg) {
        this.code = resultEnum.getCode();
        this.message = resultEnum.getDesc().concat(msg);
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

        ResultEnum() {
        }

        ResultEnum(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

    }
}
