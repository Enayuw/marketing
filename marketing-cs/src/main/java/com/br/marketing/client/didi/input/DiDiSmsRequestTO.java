package com.br.marketing.client.didi.input;

import lombok.Data;

/**
 * @Description DiDiRequestTO
 * @Author hong.chen
 * @CreateTime 2023/04/23
 */
@Data
public class DiDiSmsRequestTO {
    private String sign;
    private String timestamp;
    private String signature;
}
