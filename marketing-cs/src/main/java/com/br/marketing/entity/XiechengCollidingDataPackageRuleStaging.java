package com.br.marketing.entity;

import java.util.Date;

import lombok.Data;

@Data
public class XiechengCollidingDataPackageRuleStaging {
    /**
     * 
     */
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 携程撞库包的id
     */
    private Long packageId;

    /**
     * 撞库数据清洗任务id
     */
    private Long collidingDataTaskId;

    /**
     * 撞得量级
     */
    private Integer collidingBackNumber;

    /**
     * 撞库开始时间
     */
    private Date collidingStartTime;

    /**
     * 撞库结束时间
     */
    private Date collidingEndTime;

    /**
     * 一天内的撞库次数
     */
    private Integer collidingTimes;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    /**
     * 0 正常，1删除
     */
    private Integer isDelete;

}