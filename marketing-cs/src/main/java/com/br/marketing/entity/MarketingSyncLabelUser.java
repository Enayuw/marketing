package com.br.marketing.entity;

/**
 * @ClassName MarketingSyncLabelUser
 * @Author hang.zhou
 * @Date 2025/8/4
 */
public class MarketingSyncLabelUser extends MarketingSyncUser{

    /**
     * 标签任务表Id
     */
    private String labelId;

    public String getLabelId() {
        return labelId;
    }

    public void setLabelId(String labelId) {
        this.labelId = labelId;
    }
}
