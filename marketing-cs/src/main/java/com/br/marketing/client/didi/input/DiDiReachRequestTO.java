package com.br.marketing.client.didi.input;

import lombok.Data;

/**
 * @Description DiDiRequestTO
 * @Author hong.chen
 * @CreateTime 2023/04/23
 */
@Data
public class DiDiReachRequestTO {
    private String sign;
    private String timestamp;
    private String signature;
    private String scas;
    private String channelId;
}
