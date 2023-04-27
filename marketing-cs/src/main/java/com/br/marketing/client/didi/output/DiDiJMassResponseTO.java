package com.br.marketing.client.didi.output;

import lombok.Data;

@Data
public class DiDiJMassResponseTO {
    private String errorCode;
    private String errorMessage;
    private String data;
}
