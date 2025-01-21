package com.br.marketing.entity;

import lombok.Data;

import java.util.Date;

@Data
public class XieChengCollidingDataLoopCycle{
    /**
     * 
     */
    private Long id;

    /**
     * 规则包记录id
     */
    private Long packageId;

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
     * 券码信息
     */
    private String marketCouponList;

    /**
     * 券码code
     */
    private String couponCode;

    /**
     * 券码名称
     */
    private String couponDesc;

    /**
     * 客群名称：1-未注册、2-促活
     */
    private Integer customerGroup;

    /**
     * 撞库info
     */
    private String info;

    /**
     * 最近一次撞库时间
     */
    private Date pushTime;

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