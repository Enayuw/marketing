package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * b_data_group_task_detail
 * @author 
 */
@Data
public class DataGroupTaskDetail implements Serializable {
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 分组任务id
     */
    private Long groupTaskId;

    /**
     * 场景
     */
    private String userType;

    /**
     * 分组扩展字段
     */
    private String extendField;

    /**
     * 分组扩展字段的value值
     */
    private String extendFieldValue;

    /**
     * 分组字段
     */
    private String groupField;

    /**
     * 分组字段的value值
     */
    private String groupFieldValue;

    /**
     * 分组量级
     */
    private Integer groupNum;

    /**
     * 分组量级最小Id
     */
    private Long groupMinId;

    /**
     * 分组量级最大Id
     */
    private Long groupMaxId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 更新的sql条件
     */
    private String updateCondition;

    private static final long serialVersionUID = 1L;
}