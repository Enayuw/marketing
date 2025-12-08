package com.br.marketing.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * deatil表聚合出来的分页使用的dto对象
 * b_marketing_line_account_detail_normal
 */

@Data
public class LineAccountDetailVO {
   private String groupId;
   private String lineSupplier;
   private String linesInfo;
   private BigDecimal price;
   private Date effectStartDate;
   private Date effectEndDate;
   private Integer enabled;
   private Date createTime;
   private Date updateTime;
   private Integer isDelete;
}
