package com.br.marketing.dto.zhongyuan;

import lombok.Data;

import java.io.Serializable;

/**
 * 外呼任务场景变量修改响应DTO
 *
 * @author kongbx
 * @date 2025/11/28
 */
@Data
public class ChangeSceneVariableResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    private String taskUid;

    /**
     * 更新结果
     */
    private String ew;
}

