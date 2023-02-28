/**
  * Copyright 2021 bejson.com 
  */
package com.br.marketing.client.robotaiapi.input;

import com.br.marketing.dto.DataDistributeLogBase;
import lombok.Data;

/**
 * 客户转化接口去重DTO
 */
@Data
public class TransferRobotOutboundSoleDTO extends DataDistributeLogBase<ConversionData> {
    private Long transferInfoId;
    private String apiCode;
    private String last;
}