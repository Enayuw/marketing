package com.br.marketing.dto.shuhe;

import com.br.marketing.dto.ResponseCustomDTO;

/**
 * 数禾订制接口响应
 *
 * @author Guo Zeqiang
 * @dateTime 2022/2/10 14:14
 */
public class ResponseShuheDTO extends ResponseCustomDTO {
    /**
     * 2022/2/10 14:16 状态码
     */
    private int code;

    /**
     * 2022/2/10 14:16 描述
     */
    private String desc;

    public ResponseShuheDTO() {
    }

    public ResponseShuheDTO(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public ResponseShuheDTO(ResultEnum resultEnum) {
        this.code = resultEnum.getCode();
        this.desc = resultEnum.getDesc();
    }

    public ResponseShuheDTO(ResultEnum resultEnum, String msg) {
        this.code = resultEnum.getCode();
        this.desc = resultEnum.getDesc().concat(msg);
    }

    public ResponseShuheDTO success() {
        this.code = ResultEnum.SUCCESS.getCode();
        this.desc = ResultEnum.SUCCESS.getDesc();
        return this;
    }

    public ResponseShuheDTO failed(String desc) {
        this.code = ResultEnum.FAILED.getCode();
        this.desc = ResultEnum.FAILED.getDesc().concat(desc);
        return this;
    }

    public ResponseShuheDTO failed() {
        this.code = ResultEnum.FAILED.getCode();
        this.desc = ResultEnum.FAILED.getDesc();
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
        return "ResponseShuheDTO{" +
                "code=" + code +
                ", desc='" + desc + '\'' +
                '}';
    }

    /**
     * 状态码枚举
     *
     * @author Guo Zeqiang
     * @dateTime 2022/2/11 17:10
     */
    public enum ResultEnum {
        SUCCESS(200, "调用接口成功"),
        FAILED(201, "服务异常!");

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
