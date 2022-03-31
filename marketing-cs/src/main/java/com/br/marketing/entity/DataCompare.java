package com.br.marketing.entity;

import lombok.Data;

import java.util.Date;

@Data
public class DataCompare {
    /**
     * 
     */
    private Long id;

    /**
     * 三方接口 1:人工黑名单,2:人工转化,3:客服黑名单,4:客服转化,5:人工拨打推送
     */
    private Integer externalInterface;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 
     */
    private Long transferInfoId;

    /**
     * 数据id，逗号拼接
     */
    private String arguments;

    public DataCompare(){}

    public DataCompare(String ids, Integer enumFlag, Long infoId) {
        this.arguments = ids;
        this.externalInterface = enumFlag;
        this.createTime = new Date();
        this.transferInfoId = infoId;
    }
}