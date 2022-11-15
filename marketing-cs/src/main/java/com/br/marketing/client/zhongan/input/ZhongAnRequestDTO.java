package com.br.marketing.client.zhongan.input;

import lombok.Data;

@Data
public class ZhongAnRequestDTO {
    private String apiKey;
    private String reqNo;
    private String reqDate;
    private String gatewayVersion;
    private String bizParam;
    private String sign;
}
