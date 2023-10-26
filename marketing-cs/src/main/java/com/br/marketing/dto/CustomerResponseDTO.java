package com.br.marketing.dto;

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
        this.responseCustomDTO = responseCustomDTO;
        this.statusEnum = statusEnum;
        this.responseCode = responseCode;
    }

    public ResponseCustomDTO getResponseCustomDTO() {
        return responseCustomDTO;
    }

    public void setResponseCustomDTO(ResponseCustomDTO responseCustomDTO) {
        this.responseCustomDTO = responseCustomDTO;
    }

    public StatusEnum getStatusEnum() {
        return statusEnum;
    }

    public void setStatusEnum(StatusEnum statusEnum) {
        this.statusEnum = statusEnum;
    }

    public Object getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(Object responseCode) {
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
