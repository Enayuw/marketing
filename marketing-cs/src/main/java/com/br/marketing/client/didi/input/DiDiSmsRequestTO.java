package com.br.marketing.client.didi.input;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Description DiDiRequestTO
 * @Author hong.chen
 * @CreateTime 2023/04/23
 */
@Data
@Accessors(chain = true)
public class DiDiSmsRequestTO {
    private String sign;
    private String timestamp;
    private String signature;
    private String scas;
    private String channelId;
    private String mediaName;
}
