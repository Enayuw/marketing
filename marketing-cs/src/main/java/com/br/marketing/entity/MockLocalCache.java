package com.br.marketing.entity;

public class MockLocalCache {

    /**
     * 是否启用 0-启动 1-关闭
     */
    private Integer enabled;

    /**
     * 版本号
     */
    private String version;

    /**
     * 更新时间
     */
    private String updateTime;

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}
