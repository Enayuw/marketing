/**
  * Copyright 2021 bejson.com 
  */
package com.br.marketing.client.robotaiapi.output;

import lombok.Data;

/**
 * Auto-generated: 2021-08-04 11:38:32
 */
@Data
public class TransferRobotOutboundVO<T> extends RobotParentVO {
    private String accessNumber;
    private T data;
}