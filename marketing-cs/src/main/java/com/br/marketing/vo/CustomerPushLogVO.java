package com.br.marketing.vo;

/**
 * 推送决策日志dto
 */
public class CustomerPushLogVO {
    /**
     * id
     */
    private Long id;

    /**
     * 请求批次号
     */
    private String batch;

    /**
     * 推送状态确认
     */
    private String realStauts;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public String getRealStauts() {
        return realStauts;
    }

    public void setRealStauts(String realStauts) {
        this.realStauts = realStauts;
    }
}
