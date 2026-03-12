package com.br.marketing.dto.xyf;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * XYF 接口解密后的响应体（加密前）
 */
@Data
public class XyfDecryptedResponseDTO {

    @Schema(description = "返回状态 1-成功 0-失败")
    private String status;

    @Schema(description = "返回状态码 E00000000-成功")
    private String error;

    @Schema(description = "返回消息")
    private String msg;

    @Schema(description = "返回报文实体")
    private XyfDecryptedResponseData data;

    @Data
    public static class XyfDecryptedResponseData {
        @Schema(description = "请求方批次唯一标识")
        private String batchId;
        @Schema(description = "供应商批次唯一标识")
        private String respBatchId;
    }
}
