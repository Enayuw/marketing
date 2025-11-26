package com.br.marketing.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * deatil表聚合出来的分页使用的dto对象
 */

@Data
public class LineAccountLogNormalVO {
   /**
    * 主键id
    */
   private Long id;

   /**
    * 配置id
    */
   private String  groupId;

   /**
    * 供应商名称
    */
   private String lineSupplier;

   /**
    * 日志信息
    */
   private String detail;

   /**
    * 操作人id
    */
   private String userId;

   /**
    * 操作人userName
    */
   private String userName;

   /**
    * 操作人realName
    */
   private String realName;

   /**
    * 业务删除 1-新增 2-变更 3-删除 4-禁用
    */
   private Integer opeType;

   /**
    * 创建时间
    */
   private Date createTime;

   /**
    * 修改时间
    */
   private Date updateTime;

   /**
    * 状态 0-正常 1-删除
    */
   private Integer isDelete;
}
