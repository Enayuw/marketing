package com.br.marketing.client.didi.input.v5;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DiDiV5BlackDataRequestDTO {
    private String mediaName;
    private String sign;
    private String timestamp;
    private String signature;
}
