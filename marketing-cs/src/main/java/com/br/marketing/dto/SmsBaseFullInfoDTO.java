package com.br.marketing.dto;

import lombok.Data;

/**
 * sms基础信息(全字段)
 */

@Data
public class SmsBaseFullInfoDTO {
   private  Long id;
   private Long channelId;
   private String channelName;
   private Long vendorId;
   private String vendorName;
   private Long vendorPrimaryId;

}
