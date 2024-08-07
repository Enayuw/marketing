package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * b_xiecheng_colliding_data_package
 * @author :zhen.Li1
 * @updateTime: 2024-04-25
 */
@Data
public class XieChengCollidingDataPackage implements Serializable {
    private Long id;

    /**
     * 包名称
     */
    private String packageName;

    /**
     * 优先级 1 ，2，,3
     */
    private Integer priority;

    /**
     * 是否开启轮次 0-否 1-是
     */
    private Integer round;

    /**
     * 开始撞库时间
     */
    private Date collidingTime;

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
     * 撞库包实际数据数量
     */
    private Integer actualNumber;

    /**
     * 撞库包预估数据量级
     */
    private Integer discreetNumber;

    /**
     * 撞库数据清洗任务id
     */
    private Long collidingDataTaskId;

    private static final long serialVersionUID = 1L;
}