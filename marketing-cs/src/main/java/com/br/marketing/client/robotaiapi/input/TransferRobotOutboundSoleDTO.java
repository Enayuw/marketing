/**
  * Copyright 2021 bejson.com 
  */
package com.br.marketing.client.robotaiapi.input;

import lombok.Data;

/**
 * 客户转化接口去重DTO
 */
@Data
public class TransferRobotOutboundSoleDTO extends TransferRobotOutboundDTO {
    /**
     * 1-cell去重
     */
    private Integer soleType;
}