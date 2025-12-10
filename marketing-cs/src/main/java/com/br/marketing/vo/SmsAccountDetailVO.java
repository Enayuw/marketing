package com.br.marketing.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * deatil表聚合出来的分页使用的dto对象
 * b_marketing_sms_account_detail_normal
 */

@Data
public class SmsAccountDetailVO {
   private String  groupId;
   private Long vendorId;
   private String vendorName;
   private String channelsInfo;
   private BigDecimal price;
   private Date effectStartDate;
   private Date effectEndDate;
   private Integer enabled;
   private Date createTime;
   private Date updateTime;
   private Integer isDelete;
}
