package com.br.marketing.dto;

import lombok.Data;

import java.util.Date;

/**
 * @description: 转化表和上传表字段
 * @author: songjuanjuan
 * @create: 2022-07-13 14:22
 */
@Data
public class MarketingTransferSyncUserAndStatusDTO {
    /**
     *
     */
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;


    /**
     * 客户案件编号
     */
    private String custNum;

    /**
     * 业务保留字段1
     */
    private String reserveField1;

    /**
     * 业务保留字段2
     */
    private String reserveField2;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 案件状态
     */
    private Integer status;
}
