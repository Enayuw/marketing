package com.br.marketing.client.zhongan.input;

import lombok.Data;

@Data
public class ZhongAnRequestDTO<T> {
    private String apiKey;
    private String reqNo;
    private String reqDate;
    private String gatewayVersion;
    private T bizParam;
    private String extend;
    private String sign;
}
