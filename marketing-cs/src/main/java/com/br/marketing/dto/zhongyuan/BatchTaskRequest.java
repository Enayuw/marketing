package com.br.marketing.dto.zhongyuan;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 批量任务上报请求DTO
 *
 * @author kongbx
 * @date 2025/11/14
 */
@Data
public class BatchTaskRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 批次名称
     */
    private String batchName;

    /**
     * 批次编号
     */
    private String batchNo;

    /**
     * 场景代码
     */
    private String sceneCode;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 节假日禁止标识（0-否，1-是）
     */
    private Integer festivalBan;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 上报结束标识
     */
    private String reportEndFlag;

    /**
     * 任务数据列表
     */
    private List<TaskData> taskDataList;

    /**
     * 任务数据
     */
    @Data
    public static class TaskData implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 任务编号
         */
        private String taskNo;

        /**
         * 电话号码
         */
        private String telNo;

        /**
         * 变量列表
         */
        private List<Variable> variableList;
    }

    /**
     * 变量
     */
    @Data
    public static class Variable implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 变量代码
         */
        private String code;

        /**
         * 变量值
         */
        private String value;
    }
}

