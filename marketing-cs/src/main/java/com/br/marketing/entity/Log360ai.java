package com.br.marketing.entity;

public class Log360ai {
    /**
     * 
     */
    private Long id;

    /**
     * 数据id
     */
    private Long dataId;

    /**
     * 状态 1-处理中，2-处理完成，3-接口推送失败，4-数据处理失败
     */
    private Integer status;

    /**
     * 失败原因
     */
    private String errorMsg;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDataId() {
        return dataId;
    }

    public void setDataId(Long dataId) {
        this.dataId = dataId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg == null ? null : errorMsg.trim();
    }
}