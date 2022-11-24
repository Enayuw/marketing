package com.br.marketing.client.zhongan.output;

import lombok.Data;

@Data
public class ZkReponseVO {
    private Boolean access;
    private String respMsg;
    private String respNo;
    private String respCode;
    private String status;
    private String failReason;
}
