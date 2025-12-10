package com.br.marketing.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class SmsAccountDetailDTO {
   private Long groupId;
   private Long vendorId;
   private String channelIds;
   private BigDecimal price;
   private Date effectStartDate;
   private Date effectEndDate;
   private Integer enabled;
   private Date createTime;
   private Date updateTime;
   private Integer isDelete;

}
