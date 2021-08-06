package com.br.marketing.entity;

import java.util.Date;

public class DataTest {
    /**
     * 
     */
    private Long id;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private String data;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data == null ? null : data.trim();
    }
}