/**
  * Copyright 2021 bejson.com 
  */
package com.br.marketing.client.intelligentcustomerservice.output;
import lombok.Data;

import java.util.List;

/**
 * Auto-generated: 2021-08-04 11:38:32
 */
@Data
public class TransferRobotOutboundVO extends RobotParentVO {
    private String swiftNumber;
    private List<SuccessData> successData;
    private List<UnsuccessfulData> unsuccessfulData;

}