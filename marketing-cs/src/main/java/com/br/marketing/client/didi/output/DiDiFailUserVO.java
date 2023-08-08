package com.br.marketing.client.didi.output;

import lombok.Data;

@Data
public class DiDiFailUserVO {
    private String errorCode;
    private String errorMessage;
    private String data;
}
