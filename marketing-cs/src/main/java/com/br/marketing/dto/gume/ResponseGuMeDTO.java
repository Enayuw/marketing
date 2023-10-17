package com.br.marketing.dto.gume;

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
    private String desc;

    public ResponseGuMeDTO() {
    }

    public ResponseGuMeDTO(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public ResponseGuMeDTO(ResponseGuMeDTO.ResultEnum resultEnum) {
        this.code = resultEnum.getCode();
        this.desc = resultEnum.getDesc();
    }

    public ResponseGuMeDTO(ResponseGuMeDTO.ResultEnum resultEnum, String msg) {
        this.code = resultEnum.getCode();
        this.desc = resultEnum.getDesc().concat(msg);
    }

    public ResponseGuMeDTO success() {
        this.code = ResponseGuMeDTO.ResultEnum.SUCCESS.getCode();
        this.desc = ResponseGuMeDTO.ResultEnum.SUCCESS.getDesc();
        return this;
    }

    public ResponseGuMeDTO failed(String desc) {
        this.code = ResponseGuMeDTO.ResultEnum.FAILED.getCode();
        this.desc = ResponseGuMeDTO.ResultEnum.FAILED.getDesc().concat(desc);
        return this;
    }

    public ResponseGuMeDTO failed() {
        this.code = ResponseGuMeDTO.ResultEnum.FAILED.getCode();
        this.desc = ResponseGuMeDTO.ResultEnum.FAILED.getDesc();
        return this;
    }

    public ResponseGuMeDTO failed(ResponseGuMeDTO.ResultEnum resultEnum) {
        this.code = resultEnum.getCode();
        this.desc = resultEnum.getDesc();
        return this;
    }

    public ResponseGuMeDTO failed(ResponseGuMeDTO.ResultEnum resultEnum, String msg) {
        this.code = resultEnum.getCode();
        this.desc = resultEnum.getDesc().concat(msg);
        return this;
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

    @Override
    public String toString() {
        return "ResponseGuMeDTO{" +
                "code=" + code +
                ", desc='" + desc + '\'' +
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
        FAILED(5000, "服务异常"),
        /**
         * 2023-10-17 13:32
         * 参数不能为空
         */
        FAILED_PARAM_NULL(5001, "参数不能为空"),

        /**
         * 2023-10-17 13:32
         * 参数不合法
         */
        FAILED_PARAM_LEGAL(5002, "参数不合法，"),
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
