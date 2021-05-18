package com.br.marketing.entity;

public class MarketingSep {
    /**
     * 
     */
    private Integer id;

    /**
     * 
     */
    private String apiCode;

    /**
     * 
     */
    private Integer sep;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode == null ? null : apiCode.trim();
    }

    public Integer getSep() {
        return sep;
    }

    public void setSep(Integer sep) {
        this.sep = sep;
    }
}