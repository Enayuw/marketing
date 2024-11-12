package com.br.marketing.datarelayservice.client;

import lombok.Data;

import java.io.Serializable;

@Data
public class QiFuAiReqDTO implements Serializable {

    private static final long serialVersionUID = 7848647589427142061L;
    private String appId;
    private String bizData;
    private String encryptIV;
    private String encryptKey;
    private String sign;
    private String timestamp;
}
