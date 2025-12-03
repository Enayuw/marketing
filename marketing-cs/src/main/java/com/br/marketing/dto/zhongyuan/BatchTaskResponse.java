package com.br.marketing.dto.zhongyuan;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 批量任务上报响应DTO
 *
 * @author kongbx
 * @date 2025/11/14
 */
@Data
public class BatchTaskResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 批次编号
     */
    private String batchNo;

    /**
     * 批次唯一标识（系统生成）
     */
    private String batchUid;

    /**
     * 任务信息列表
     */
    private List<TaskInfo> taskInfoList;

    /**
     * 任务信息
     */
    @Data
    public static class TaskInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 任务唯一标识（系统生成）
         */
        private String taskUid;

        /**
         * 任务编号
         */
        private String taskNo;

        /**
         * 电话号码
         */
        private String telNo;
    }
}

