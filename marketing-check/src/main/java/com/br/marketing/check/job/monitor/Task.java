package com.br.marketing.check.job.monitor;

import java.util.concurrent.ScheduledFuture;

public class Task {
    private int taskId;
    private String cronExpression;
    private ScheduledFuture<?> scheduledFuture;
    private Runnable task;

    public Task(int taskId, String cronExpression, ScheduledFuture<?> scheduledFuture, Runnable task) {
        this.taskId = taskId;
        this.cronExpression = cronExpression;
        this.scheduledFuture = scheduledFuture;
        this.task = task;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public ScheduledFuture<?> getScheduledFuture() {
        return scheduledFuture;
    }

    public void setScheduledFuture(ScheduledFuture<?> scheduledFuture) {
        this.scheduledFuture = scheduledFuture;
    }

    public Runnable getTask() {
        return task;
    }

    public void setTask(Runnable task) {
        this.task = task;
    }
}
