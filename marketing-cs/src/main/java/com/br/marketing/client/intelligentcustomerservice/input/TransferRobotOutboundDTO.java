/**
  * Copyright 2021 bejson.com 
  */
package com.br.marketing.client.intelligentcustomerservice.input;
import lombok.Data;

import java.util.List;

/**
 * Auto-generated: 2021-08-04 10:58:58
 */
@Data
public class TransferRobotOutboundDTO {
    private String accessNumber;
    private List<ConversionData> conversionData;
    private String method;
}