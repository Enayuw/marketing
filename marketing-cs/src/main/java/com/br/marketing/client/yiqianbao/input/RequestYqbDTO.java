package com.br.marketing.client.yiqianbao.input;

import lombok.Data;

@Data
public class RequestYqbDTO {

    /**
     * 正文json格式，用RSA算法加密
     */
    private String bizContent;

    /**
     * 请求流水号
     */
    private String reqSeqNo;

    /**
     * 签名
     */
    private String sign;

    /**
     * 签名
     */
    private String channelSource = "BAIRONG";





}
