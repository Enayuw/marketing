package com.br.marketing.api.customer.transfer.service.alien.dto;

import com.br.marketing.common.constants.MarketingErrorInfo;
import com.br.marketing.dto.ResponseCustomDTO;

/**
 * 未知用户响应
 *
 * @author Guo Zeqiang
 * @dateTime 2023-10-25 15:59
 */
public class AlienResponseDTO extends ResponseCustomDTO {

    private static final long serialVersionUID = 3641802481118707321L;
    /**
     * 2023-10-16 17:14 状态码
     */
    private String code;

    /**
     * 2023-10-16 17:14 描述
     */
    private String message;

    public AlienResponseDTO() {
    }

    public AlienResponseDTO(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public AlienResponseDTO(MarketingErrorInfo errorInfo) {
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


    public AlienResponseDTO success() {
        this.code = MarketingErrorInfo.SUCCESS.getErrorCode();
        this.message = MarketingErrorInfo.SUCCESS.getErrorMsg();
        return this;
    }

    public AlienResponseDTO failed(MarketingErrorInfo errorInfo) {
        this.code = errorInfo.getErrorCode();
        this.message = errorInfo.getErrorMsg();
        return this;
    }

    public AlienResponseDTO failed(String message) {
        this.code = MarketingErrorInfo.UNKNOWN_ERROR.getErrorCode();
        this.message = MarketingErrorInfo.UNKNOWN_ERROR.getErrorMsg().concat(message);
        return this;
    }

    public AlienResponseDTO failed() {
        this.code = MarketingErrorInfo.UNKNOWN_ERROR.getErrorCode();
        this.message = MarketingErrorInfo.UNKNOWN_ERROR.getErrorMsg();
        return this;
    }

    @Override
    public String toString() {
        return "AlienResponseDTO{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                "} ";
    }
}
