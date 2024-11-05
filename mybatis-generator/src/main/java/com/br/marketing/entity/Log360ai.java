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
     * 状态 1-处理中，2-处理完成
     */
    private Byte status;

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

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }
}