package com.br.marketing.client.robotaiapi.input;

import lombok.Data;

import java.util.List;

@Data
public class TransferJsonDataDTO {
    private String accessNumber;
    private List<ConversionData> conversionData;
    private String method;
    private String platApiCode;
}
