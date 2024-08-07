package com.br.marketing.api.customer.transfer.service.guomei.dto;

import com.br.marketing.dto.ResponseCustomDTO;

/**
 * 国美定制响应
 *
 * @author Guo Zeqiang
 * @dateTime 2023-10-16 17:14
 */
public class ResponseGuMeDTO extends ResponseCustomDTO {

    private static final long serialVersionUID = -7690813151831346825L;
    /**
     * 2023-10-16 17:14 状态码
     */
    private int code;

    /**
     * 2023-10-16 17:14 描述
     */
    private String message;

    public ResponseGuMeDTO() {
    }

    public ResponseGuMeDTO(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public ResponseGuMeDTO(ResponseGuMeDTO.ResultEnum resultEnum) {
        this.code = resultEnum.getCode();
        this.message = resultEnum.getDesc();
    }

    public ResponseGuMeDTO(ResponseGuMeDTO.ResultEnum resultEnum, String msg) {
        this.code = resultEnum.getCode();
        this.message = resultEnum.getDesc().concat(msg);
    }

    public ResponseGuMeDTO success() {
        this.code = ResponseGuMeDTO.ResultEnum.SUCCESS.getCode();
        this.message = ResponseGuMeDTO.ResultEnum.SUCCESS.getDesc();
        return this;
    }

    public ResponseGuMeDTO failed(String message) {
        this.code = ResponseGuMeDTO.ResultEnum.FAILED.getCode();
        this.message = ResponseGuMeDTO.ResultEnum.FAILED.getDesc().concat(message);
        return this;
    }

    public ResponseGuMeDTO failed() {
        this.code = ResponseGuMeDTO.ResultEnum.FAILED.getCode();
        this.message = ResponseGuMeDTO.ResultEnum.FAILED.getDesc();
        return this;
    }

    public ResponseGuMeDTO failed(ResponseGuMeDTO.ResultEnum resultEnum) {
        this.code = resultEnum.getCode();
        this.message = resultEnum.getDesc();
        return this;
    }

    public ResponseGuMeDTO failed(ResponseGuMeDTO.ResultEnum resultEnum, String msg) {
        this.code = resultEnum.getCode();
        this.message = resultEnum.getDesc().concat(msg);
        return this;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ResponseGuMeDTO{" +
                "code=" + code +
                ", message='" + message + '\'' +
                '}';
    }

    /**
     * 状态码枚举
     *
     * @author Guo Zeqiang
     * @dateTime 2023-10-16 17:14
     */
    public enum ResultEnum {

        /**
         * 2023-10-16 17:22
         * 成功
         */
        SUCCESS(200, "SUCCESS"),
        /**
         * 2023-10-16 17:22
         * 失败
         */
        FAILED(5000, "失败"),
        ;

        private int code;
        private String desc;

        ResultEnum() {
        }

        ResultEnum(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }
    }
}
