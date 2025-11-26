package com.br.marketing.dto;

import lombok.Data;

/**
 * line基础信息(全字段)
 */

@Data
public class LineBaseFullInfoDTO {
   private  Long id;
   private Long gatewayId;
   private String caller;
   private String projectName;
   private String outboundNumber;
   private Long lineSupplierId;
   private String lineSupplier;

}
