package com.br.marketing.client.zhongan.output;

import lombok.Data;

@Data
public class ZhongAnResponseVO {
    private Boolean success;
    private String resultCode;
    private String resultMsg;
    private String reqNo;
    private String respNo;
    private String respDate;
    private String bizData;
    private String sign;
}
