package com.br.marketing.client.robotaiapi.input;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class TransferJsonDataDTO {
    private String accessNumber;
    private List<ConversionData> conversionData;
    private String method;
    private String platApiCode;
    private String last;

    public TransferJsonDataDTO() {
    }

    public TransferJsonDataDTO(List<ConversionData> conversionData) {
        this.accessNumber = UUID.randomUUID().toString();
        this.conversionData = conversionData;
        this.method = "conversionData";
    }

    public TransferJsonDataDTO(List<ConversionData> conversionData, String last) {
        this.accessNumber = UUID.randomUUID().toString();
        this.conversionData = conversionData;
        this.method = "conversionData";
        this.last = last;
    }
}
