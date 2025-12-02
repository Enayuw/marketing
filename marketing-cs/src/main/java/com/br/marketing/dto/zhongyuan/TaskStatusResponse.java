package com.br.marketing.dto.zhongyuan;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 批量外呼任务状态修改响应DTO
 *
 * @author kongbx
 * @date 2025/11/14
 */
@Data
public class TaskStatusResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 失败列表
     */
    private List<FailInfo> failList;

    /**
     * 失败信息
     */
    @Data
    public static class FailInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 任务UID
         */
        private String taskUid;

        /**
         * 失败原因
         */
        private String message;
    }
}

