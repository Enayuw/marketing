package com.br.marketing.entity;

import lombok.Data;

import java.util.Date;

@Data
public class XieChengCollidingDataRobPriority extends XieChengCollidingDataRob{
    /**
     * 优先级 1 ，2，,3
     */
    private Integer priority;

    /**
     * 撞库结束时间
     */
    private Date collidingEndTime;
}