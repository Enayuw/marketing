package com.br.marketing.entity;


public class MarketingTransferSyncUserCell extends MarketingTransferSyncUser{

    private String cell;

    private String taskId;

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
}