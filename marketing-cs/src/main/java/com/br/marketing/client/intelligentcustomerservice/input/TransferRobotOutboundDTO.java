/**
  * Copyright 2021 bejson.com 
  */
package com.br.marketing.client.intelligentcustomerservice.input;
import java.util.List;

/**
 * Auto-generated: 2021-08-04 10:58:58
 */
public class TransferRobotOutboundDTO {

    private List<ConversionData> conversionData;
    private String method;
    public void setConversionData(List<ConversionData> conversionData) {
         this.conversionData = conversionData;
     }
     public List<ConversionData> getConversionData() {
         return conversionData;
     }

    public void setMethod(String method) {
         this.method = method;
     }
     public String getMethod() {
         return method;
     }

}