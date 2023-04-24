package com.br.marketing.client.didi.output;

import lombok.Data;

@Data
public class DiDiResponseTO {
    private String errorCode;
    private String errorMessage;
    private ResResult data;

    @Data
    class ResResult {
        private boolean result;
    }
}
