package com.br.marketing.entity;

import java.util.Date;

import lombok.Data;

@Data
public class XiechengCollidingDataProcessTask {
    /**
     * 
     */
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 跑分任务编号
     */
    private String batchNumber;

    /**
     * 任务状态 0：任务待代行，1 任务执行中，2 任务执行完成
     */
    private Integer taskStatus;

    /**
     * 预估数据量级
     */
    private Integer discreetNumber;

    /**
     * 实际数据数量
     */
    private Integer actualNumber;

    /**
     * 任务执行开始时间
     */
    private Date taskStartTime;

    /**
     * 任务执行结束时间
     */
    private Date taskEndTime;

    /**
     * 任务类型 0 非周期数据清洗任务, 1 周期数据清洗任务 true，2 推决策任务
     */
    private Integer taskType;

    /**
     * 预估量级执行条件
     */
    private String taskExecutionConditions;

    /**
     * 预估量级执行sql
     */
    private String taskExecutionSql;

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