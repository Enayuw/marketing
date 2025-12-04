package com.br.marketing.entity;

import lombok.Data;

/**
 * @ClassName UpdateTask
 * @Author hang.zhou
 * @Date 2025/12/4
 */
@Data
public class UpdateTask {

    Long dataId;
    Integer queryStatus;      // 0-未查询，1-查询中，2-查询失败，3-查询成功（对应响应中的code：0-调用成功，1-系统异常）
    Integer invocationStatus; // 对应响应中的result：1-准入，2-不准入
    Integer status;           // 1-正常,2-非正常（不修改此字段）
    String dataMessage;       // 数据描述

    public UpdateTask(Long dataId, Integer queryStatus, Integer invocationStatus, String dataMessage) {
        this.dataId = dataId;
        this.queryStatus = queryStatus;
        this.invocationStatus = invocationStatus;
        this.dataMessage = dataMessage;
    }
}
