package com.br.marketing.api.customer.upload.service.xiecheng.dto;

import com.br.marketing.dto.ResponseCustomDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * XieChengActivateDataResponseDTO
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class XieChengActivateDataResponseDTO extends ResponseCustomDTO {
    private static final long serialVersionUID = -8297831932682360616L;

    /**
     * 2023-10-16 17:14 状态码
     */
    private String code;

    /**
     * 2023-10-16 17:14 描述
     */
    private String msg;

    public XieChengActivateDataResponseDTO success() {
        this.code = ResultEnum.SUCCESS.getCode();
        this.msg = ResultEnum.SUCCESS.getDesc();
        return this;
    }

    public XieChengActivateDataResponseDTO failed(String message) {
        this.code = ResultEnum.FAILED.getCode();
        this.msg = ResultEnum.FAILED.getDesc().concat(message);
        return this;
    }

    public XieChengActivateDataResponseDTO failed() {
        this.code = ResultEnum.FAILED.getCode();
        this.msg = ResultEnum.FAILED.getDesc();
        return this;
    }

    public XieChengActivateDataResponseDTO failed(ResultEnum resultEnum) {
        this.code = resultEnum.getCode();
        this.msg = resultEnum.getDesc();
        return this;
    }

    public XieChengActivateDataResponseDTO failed(ResultEnum resultEnum, String msg) {
        this.code = resultEnum.getCode();
        this.msg = resultEnum.getDesc().concat(msg);
        return this;
    }

    @Override
    public String toString() {
        return "ResponseGuMeDTO{" + "code=" + code + ", msg='" + msg + '\'' + '}';
    }


    /**
     * 状态码枚举
     *
     * @author senyang.zheng
     * @date 2024/08/07
     */
    @Getter
    public enum ResultEnum {

        /**
         * 2023-10-16 17:22 成功
         */
        SUCCESS("00", "成功"),
        /**
         * 2023-10-16 17:22 失败
         */
        FAILED("5000", "失败,未知异常"),
        FAILED_JSON_ERROR("5001", "失败,jsonData解析失败"),
        FAILED_JSON_ARRAY_ERROR("5002", "失败,jsonData非数组格式"),
        ;

        private String code;
        private String desc;

        ResultEnum() {}

        ResultEnum(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

    }
}
