package com.br.marketing.dto;

import org.springframework.util.Assert;

/**
 * 定制化客户响应模板
 *
 * @author Guo Zeqiang
 * @dateTime 2023-10-24 19:49
 */
public class CustomerResponseDTO {

    /**
     * 2023-10-25 22:46
     * 客户响应,必填
     */
    private ResponseCustomDTO responseCustomDTO;

    /**
     * 2023-10-24 19:57
     * 数据状态,必填
     */
    private StatusEnum statusEnum;

    /**
     * 2023-10-24 19:57
     * 响应码,必填
     */
    private Object responseCode;

    public CustomerResponseDTO(ResponseCustomDTO responseCustomDTO, StatusEnum statusEnum, Object responseCode) {
        Assert.notNull(responseCustomDTO, "客户定制化响应不能空");
        this.responseCustomDTO = responseCustomDTO;
        Assert.notNull(statusEnum, "数据状态不能空");
        this.statusEnum = statusEnum;
        Assert.notNull(responseCode, "客户定制化响应码不能为空");
        this.responseCode = responseCode;
    }

    public ResponseCustomDTO getResponseCustomDTO() {
        return responseCustomDTO;
    }

    public final void setResponseCustomDTO(ResponseCustomDTO responseCustomDTO) {
        Assert.notNull(responseCustomDTO, "客户定制化响应不能空");
        this.responseCustomDTO = responseCustomDTO;
    }

    public StatusEnum getStatusEnum() {
        return statusEnum;
    }

    public final void setStatusEnum(StatusEnum statusEnum) {
        Assert.notNull(statusEnum, "数据状态不能空");
        this.statusEnum = statusEnum;
    }

    public Object getResponseCode() {
        return responseCode;
    }

    public final void setResponseCode(Object responseCode) {
        Assert.notNull(responseCode, "客户定制化响应码不能为空");
        this.responseCode = responseCode;
    }

    public enum StatusEnum {
        /**
         * 2023-10-24 19:53
         * 无效
         */
        INVALID(0),
        /**
         * 2023-10-24 19:53
         * 有效
         */
        VALID(1);

        private int value;

        StatusEnum(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    @Override
    public String toString() {
        return "CustomerResponseDTO{" +
                "responseCustomDTO=" + responseCustomDTO +
                ", statusEnum=" + statusEnum +
                ", responseCode=" + responseCode +
                '}';
    }
}
