package com.br.marketing.entity;

import lombok.Data;

import java.util.Date;

@Data
public class XiechengCollidingDataPackageRule {
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
     * 规则开启时间
     */
    private Date collidingStartTime;

    /**
     * 规则结束时间
     */
    private Date collidingEndTime;

    /**
     * 撞库开始时间（多个时间以逗号分割，格式HH:mm）
     */
    private String startTimes;

    /**
     * 一天内的撞库次数
     */
    private Integer collidingTimes;

    /**
     * 撞库开关，0 开启 1 关闭 默认 开启
     */
    private Integer collidingSwitch;

    /**
     * 异常信息详情
     */
    private String errorMessage;

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