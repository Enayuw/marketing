package com.br.marketing.api.customer.upload.service.guomei.dto;

import com.br.marketing.dto.ResponseCustomDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 国美上传响应dto
 *
 * @author senyang.zheng
 * @date 2024/08/06
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GuMeUploadResponseDTO extends ResponseCustomDTO {

    private static final long serialVersionUID = -7690813151831346825L;
    /**
     * 2023-10-16 17:14 状态码
     */
    private Integer code;

    /**
     * 2023-10-16 17:14 描述
     */
    private String msg;

    public GuMeUploadResponseDTO success() {
        this.code = ResultEnum.SUCCESS.getCode();
        this.msg = ResultEnum.SUCCESS.getDesc();
        return this;
    }

    public GuMeUploadResponseDTO failed(String message) {
        this.code = ResultEnum.FAILED.getCode();
        this.msg = ResultEnum.FAILED.getDesc().concat(message);
        return this;
    }

    public GuMeUploadResponseDTO failed() {
        this.code = ResultEnum.FAILED.getCode();
        this.msg = ResultEnum.FAILED.getDesc();
        return this;
    }

    public GuMeUploadResponseDTO failed(ResultEnum resultEnum) {
        this.code = resultEnum.getCode();
        this.msg = resultEnum.getDesc();
        return this;
    }

    public GuMeUploadResponseDTO failed(ResultEnum resultEnum, String msg) {
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
        SUCCESS(200, "成功"),
        /**
         * 2023-10-16 17:22 失败
         */
        FAILED(5000, "失败,未知异常"),
        FAILED_JSON_ERROR(5001, "失败,jsonData解析失败"),
        FAILED_FIELD_CHECK_ERROR(5002, "失败，必填字段缺失"),
        ;

        private int code;
        private String desc;

        ResultEnum() {}

        ResultEnum(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

    }
}
