package com.br.marketing.entity;


public class MarketingTransferSyncUserCell extends MarketingTransferSyncUser{

    private String cell;

    private String taskId;

    private MarketingSyncUser marketingSyncUser;

    private MarketingTransferSyncUser marketingTransferSyncUser;

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public MarketingSyncUser getMarketingSyncUser() {
        return marketingSyncUser;
    }

    public void setMarketingSyncUser(MarketingSyncUser marketingSyncUser) {
        this.marketingSyncUser = marketingSyncUser;
    }

    public MarketingTransferSyncUser getMarketingTransferSyncUser() {
        return marketingTransferSyncUser;
    }

    public void setMarketingTransferSyncUser(MarketingTransferSyncUser marketingTransferSyncUser) {
        this.marketingTransferSyncUser = marketingTransferSyncUser;
    }
}