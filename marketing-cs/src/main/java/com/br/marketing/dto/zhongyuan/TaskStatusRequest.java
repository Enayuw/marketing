package com.br.marketing.dto.zhongyuan;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 批量外呼任务状态修改请求DTO
 *
 * @author kongbx
 * @date 2025/11/14
 */
@Data
public class TaskStatusRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 批次UID
     */
    private String batchUid;

    /**
     * 任务UID列表
     */
    private List<String> taskUidList;

    /**
     * 操作类型（cancel：取消/剔除）
     */
    private String operation;
}

