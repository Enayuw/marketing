package com.br.marketing.datarelayservice.client;

import lombok.Data;

import java.io.Serializable;

@Data
public class QiFuAiResDTO implements Serializable {
    private static final long serialVersionUID = -2584470985635978248L;
    private String code;
    private String msg;
    private String flag;
    private DataResult data;

    @Data
    public static class DataResult {
        private String appId;
        private String bizData;
        private String encryptIV;
        private String encryptKey;
        private String sign;
        private String timestamp;
    }
}
