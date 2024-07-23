package com.br.marketing.entity;

import java.util.Date;

import lombok.Data;

@Data
public class XieChengCollidingDataRob {
    /**
     * 主键id
     */
    private Long id;

    /**
     * 包主键id
     */
    private Long packageId;

    /**
     * 规则主键id
     */
    private Long packageRuleId;

    /**
     * 数据来源类型：T -周期，F-代表非周期
     */
    private String dataSourceType;

    /**
     * 手机号
     */
    private String cellSha256CodeList;

    /**
     * 数据释放时间，下次撞库时间
     */
    private Date releaseTime;

    /**
     * 释放日期
     */
    private Date releaseDate;

    /**
     * 最近一次撞库时间
     */
    private Date pushTime;

    /**
     * 撞库次数
     */
    private Integer collidingCount;

    /**
     * 状态 0-正常 1-删除
     */
    private Integer isDelete;

    /**
     * 扩展字段
     */
    private String extend;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 重试次数
     */
    private Integer retryCount;

}