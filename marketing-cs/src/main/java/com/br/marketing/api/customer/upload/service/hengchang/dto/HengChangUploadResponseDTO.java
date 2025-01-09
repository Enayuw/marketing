package com.br.marketing.api.customer.upload.service.hengchang.dto;

import com.br.marketing.common.constants.MarketingErrorInfo;
import com.br.marketing.dto.ResponseCustomDTO;
import lombok.*;

/**
 * @ClassName HengChangUploadResponseDTO
 * @Description TODO
 * @Author kongbx
 * @Date 2025/1/3 16:11
 */
@Setter
@Getter
public class HengChangUploadResponseDTO extends ResponseCustomDTO {

    /**
     * 2023-10-16 17:14 状态码
     */
    private String code;

    /**
     * 2023-10-16 17:14 描述
     */
    private String message;

    public HengChangUploadResponseDTO() {
    }

    /**
     * 外星人上传响应dto
     *
     * @param code    代码
     * @param message 信息
     * @author senyang.zheng
     * @date 2024/08/07
     */
    public HengChangUploadResponseDTO(String code, String message) {
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
    public HengChangUploadResponseDTO(MarketingErrorInfo errorInfo) {
        this.code = errorInfo.getErrorCode();
        this.message = errorInfo.getErrorMsg();
    }


    /**
     * 成功
     *
     * @return {@link HengChangUploadResponseDTO }
     * @author senyang.zheng
     * @date 2024/08/07
     */
    public HengChangUploadResponseDTO success() {
        this.code = MarketingErrorInfo.SUCCESS.getErrorCode();
        this.message = MarketingErrorInfo.SUCCESS.getErrorMsg();
        return this;
    }

    /**
     * 失败
     *
     * @param errorInfo 错误信息
     * @return {@link HengChangUploadResponseDTO }
     * @author senyang.zheng
     * @date 2024/08/07
     */
    public HengChangUploadResponseDTO failed(MarketingErrorInfo errorInfo) {
        this.code = errorInfo.getErrorCode();
        this.message = errorInfo.getErrorMsg();
        return this;
    }

    /**
     * 失败
     *
     * @param message 信息
     * @return {@link HengChangUploadResponseDTO }
     * @author senyang.zheng
     * @date 2024/08/07
     */
    public HengChangUploadResponseDTO failed(String message) {
        this.code = MarketingErrorInfo.UNKNOWN_ERROR.getErrorCode();
        this.message = MarketingErrorInfo.UNKNOWN_ERROR.getErrorMsg().concat(message);
        return this;
    }

    /**
     * 失败
     *
     * @return {@link HengChangUploadResponseDTO }
     * @author senyang.zheng
     * @date 2024/08/07
     */
    public HengChangUploadResponseDTO failed() {
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
