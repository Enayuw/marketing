package com.br.marketing.api.customer.transfer.service.hengchang.dto;

import com.br.marketing.common.constants.MarketingErrorInfo;
import com.br.marketing.dto.ResponseCustomDTO;

/**
 * @ClassName HengChangResponseDTO
 * @Author kongbx
 * @Date 2025/1/7 13:51
 */
public class HengChangResponseDTO extends ResponseCustomDTO {

    /**
     * 2023-10-16 17:14 状态码
     */
    private String code;

    /**
     * 2023-10-16 17:14 描述
     */
    private String message;

    public HengChangResponseDTO() {
    }

    public HengChangResponseDTO(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public HengChangResponseDTO(MarketingErrorInfo errorInfo) {
        this.code = errorInfo.getErrorCode();
        this.message = errorInfo.getErrorMsg();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public HengChangResponseDTO success() {
        this.code = MarketingErrorInfo.SUCCESS.getErrorCode();
        this.message = MarketingErrorInfo.SUCCESS.getErrorMsg();
        return this;
    }

    public HengChangResponseDTO failed(MarketingErrorInfo errorInfo) {
        this.code = errorInfo.getErrorCode();
        this.message = errorInfo.getErrorMsg();
        return this;
    }

    public HengChangResponseDTO failed(String message) {
        this.code = MarketingErrorInfo.UNKNOWN_ERROR.getErrorCode();
        this.message = MarketingErrorInfo.UNKNOWN_ERROR.getErrorMsg().concat(message);
        return this;
    }

    public HengChangResponseDTO failed() {
        this.code = MarketingErrorInfo.UNKNOWN_ERROR.getErrorCode();
        this.message = MarketingErrorInfo.UNKNOWN_ERROR.getErrorMsg();
        return this;
    }

    @Override
    public String toString() {
        return "HengChangResponseDTO{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                "} ";
    }

}
