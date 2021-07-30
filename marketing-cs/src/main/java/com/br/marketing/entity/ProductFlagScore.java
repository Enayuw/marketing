package com.br.marketing.entity;

import java.util.Date;

public class ProductFlagScore {
    /**
     * 
     */
    private Integer id;

    /**
     * 
     */
    private String flagScoreProduct;

    /**
     * 1-有效；9-无效；
     */
    private Integer isDel;

    /**
     * 
     */
    private Date createTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFlagScoreProduct() {
        return flagScoreProduct;
    }

    public void setFlagScoreProduct(String flagScoreProduct) {
        this.flagScoreProduct = flagScoreProduct == null ? null : flagScoreProduct.trim();
    }

    public Integer getIsDel() {
        return isDel;
    }

    public void setIsDel(Integer isDel) {
        this.isDel = isDel;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}