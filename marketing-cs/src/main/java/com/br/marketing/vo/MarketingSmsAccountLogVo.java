package com.br.marketing.vo;

import lombok.Data;

import java.util.Date;

@Data
public class MarketingSmsAccountLogVo {
    /**
     * 
     */
    private Long id;

    /**
     * 配置id
     */
    private String configId;

    /**
     * 供应商id
     */
    private Long vendorId;

    /**
     * 供应商名称
     */
    private String vendorName;

    /**
     * 日志信息
     */
    private String detail;

    /**
     * 操作人id
     */
    private Long userId;

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