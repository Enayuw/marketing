package com.br.marketing.entity;

import java.util.List;

/**
 * @Description WubaFunction
 * @Author hong.chen
 * @CreateTime 2024/08/28
 */
public class WubaFunction {
    private List<String> partition;
    private String apiCode;
    private String batchNo;
    Long taskId;

    public List<String> getPartition() {
        return partition;
    }

    public void setPartition(List<String> partition) {
        this.partition = partition;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }
}
