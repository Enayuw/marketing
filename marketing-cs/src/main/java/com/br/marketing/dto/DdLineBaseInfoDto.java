package com.br.marketing.dto;

import lombok.Data;

@Data
public class DdLineBaseInfoDto {
   private Long gatewayId;
   private String caller;
   private String outboundNumber;
   private String lineSupplier;
   private String projectName;
}
