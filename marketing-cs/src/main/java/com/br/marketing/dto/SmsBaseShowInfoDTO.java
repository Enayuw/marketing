package com.br.marketing.dto;

import lombok.Data;

import java.util.List;


@Data
public class SmsBaseShowInfoDTO {
   private Long vendorId;
   private String vendorName;
   private List<SmsBaseInfo> channelDTOList;

   @Data
   public static class SmsBaseInfo {
      private Long channelId;
      private String channelName;
   }
}
