package com.br.marketing.dto;

import lombok.Data;

import java.util.List;

/**
 *
 CREATE TABLE `b_marketing_line_base_info_normal` (
 `id` bigint(20) NOT NULL AUTO_INCREMENT,
 `gateway_id` bigint(20) DEFAULT NULL COMMENT '线路id',
 `caller` varchar(255) DEFAULT NULL COMMENT '呼叫号码',
 `line_supplier_id` bigint(20) DEFAULT NULL COMMENT '供应商组合id',
 `project_name` varchar(255) DEFAULT NULL COMMENT '项目名称',
 `outbound_number` varchar(255) DEFAULT NULL COMMENT '外显号码',
 `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 `update_time` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
 `ope_status` int(11) DEFAULT '0' COMMENT '状态 0-没修改 1-已修改 2-已删除',
 `ope_time` datetime DEFAULT NULL COMMENT '操作修改时间',
 `is_delete` int(11) DEFAULT '0' COMMENT '状态 0-正常 1-删除',
 PRIMARY KEY (`id`) /*T![clustered_index] CLUSTERED
 */

@Data
public class LineBaseShowInfoDTO {
   private String lineSupplier;
   private List<LineBaseInfo> channelDTOList;

   @Data
   public static class LineBaseInfo {
      private Long gatewayId;
      private String caller;
      private String projectName;
      private String outboundNumber;
      private String lineSupplier;
      private String callerFullName;
   }
}
