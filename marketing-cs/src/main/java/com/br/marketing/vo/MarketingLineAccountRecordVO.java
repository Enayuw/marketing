package com.br.marketing.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class MarketingLineAccountRecordVO {
    /**
     * 
     */
    private Long id;

    /**
     * 配置id
     */
    private String configId;

    /**
     * 供应商名称
     */
    private String lineSupplier;

    /**
     * 主叫号码集合
     */
    private String linesInfo;

    /**
     * 短信单价，元/条
     */
    private BigDecimal price;

    /**
     * 生效开始日期
     */
    private Date effectStartDate;

    /**
     * 生效结束日期
     */
    private Date effectEndDate;

    /**
     * 禁用标志 0-禁用 1-启用
     */
    private Integer enabled;

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