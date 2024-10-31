package com.br.marketing.api.customer.black.service.alien.dto;

import com.br.marketing.common.constants.MarketingErrorInfo;
import com.br.marketing.dto.ResponseCustomDTO;
import lombok.Getter;
import lombok.Setter;

/**
 * 未知用户响应
 *
 * @author Guo Zeqiang
 * @dateTime 2023-10-25 15:59
 */
@Setter
@Getter
public class AlienBlackResponseDTO extends ResponseCustomDTO {


    /**
     * 2023-10-16 17:14 状态码
     */
    private String code;

    /**
     * 2023-10-16 17:14 描述
     */
    private String message;

    public AlienBlackResponseDTO() {
    }

    /**
     * 外星人上传响应dto
     *
     * @param code    代码
     * @param message 信息
     * @author senyang.zheng
     * @date 2024/08/07
     */
    public AlienBlackResponseDTO(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 外星人上传响应dto
     *
     * @param errorInfo 错误信息
     * @author senyang.zheng
     * @date 2024/08/07
     */
    public AlienBlackResponseDTO(MarketingErrorInfo errorInfo) {
        this.code = errorInfo.getErrorCode();
        this.message = errorInfo.getErrorMsg();
    }


    /**
     * 成功
     *
     * @return {@link AlienBlackResponseDTO }
     * @author senyang.zheng
     * @date 2024/08/07
     */
    public AlienBlackResponseDTO success() {
        this.code = MarketingErrorInfo.SUCCESS.getErrorCode();
        this.message = MarketingErrorInfo.SUCCESS.getErrorMsg();
        return this;
    }

    /**
     * 失败
     *
     * @param errorInfo 错误信息
     * @return {@link AlienBlackResponseDTO }
     * @author senyang.zheng
     * @date 2024/08/07
     */
    public AlienBlackResponseDTO failed(MarketingErrorInfo errorInfo) {
        this.code = errorInfo.getErrorCode();
        this.message = errorInfo.getErrorMsg();
        return this;
    }

    /**
     * 失败
     *
     * @param message 信息
     * @return {@link AlienBlackResponseDTO }
     * @author senyang.zheng
     * @date 2024/08/07
     */
    public AlienBlackResponseDTO failed(String message) {
        this.code = MarketingErrorInfo.UNKNOWN_ERROR.getErrorCode();
        this.message = MarketingErrorInfo.UNKNOWN_ERROR.getErrorMsg().concat(message);
        return this;
    }

    /**
     * 失败
     *
     * @return {@link AlienBlackResponseDTO }
     * @author senyang.zheng
     * @date 2024/08/07
     */
    public AlienBlackResponseDTO failed() {
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
