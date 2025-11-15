package com.br.marketing.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * deatil表聚合出来的分页使用的dto对象
 * b_marketing_line_account_detail_normal
 *
 * group_id,
 *  line_supplier_id,
 *  GROUP_CONCAT(DISTINCT gateway_id ORDER BY gateway_id SEPARATOR ',') as gateway_ids,
 *  price,
 *  effect_start_date,
 *  effect_end_date,
 *  create_time,
 *  update_time,
 *  enabled,
 *  is_delete
 */

@Data
public class LineAccountDetailDTO {
   private  Long groupId;
   private Long lineSupplierId;
   private String gatewayIds;
   private BigDecimal price;
   private Date effectStartDate;
   private Date effectEndDate;
   private Integer enabled;
   private Date createTime;
   private Date updateTime;
   private Integer isDelete;

}
