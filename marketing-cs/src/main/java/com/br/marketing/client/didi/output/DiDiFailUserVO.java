package com.br.marketing.client.didi.output;

import lombok.Data;

@Data
public class DiDiFailUserVO {
    private Integer errorCode;
    private String errorMessage;
    private String data;
}
