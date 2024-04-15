package com.br.marketing.check.job.monitor;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

public class TaskSchedulerManager {
    private ThreadPoolTaskScheduler scheduler;
    private Map<Integer, Task> tasks;

    public TaskSchedulerManager() {
        this.scheduler = new ThreadPoolTaskScheduler();
        this.scheduler.initialize();
        this.tasks = new HashMap<>();
    }

    public void scheduleTask(int taskId, String cronExpression, Runnable task) {
        ScheduledFuture<?> scheduledFuture = this.scheduler.schedule(task, new CronTrigger(cronExpression));
        tasks.put(taskId, new Task(taskId, cronExpression, scheduledFuture, task));
        persistTaskInfo(taskId, cronExpression); // 持久化任务信息到数据库
    }

    public void rescheduleTask(int taskId, String newCronExpression) {
        if (tasks.containsKey(taskId)) {
            Task task = tasks.get(taskId);
            task.getScheduledFuture().cancel(true);
            ScheduledFuture<?> scheduledFuture = this.scheduler.schedule(task.getTask(), new CronTrigger(newCronExpression));
            task.setCronExpression(newCronExpression);
            task.setScheduledFuture(scheduledFuture);
            persistUpdatedSchedule(taskId, newCronExpression); // 更新调度策略并持久化到数据库
        } else {
            System.out.println("Task with ID " + taskId + " does not exist.");
        }
    }

    public void deleteTask(int taskId) {
        if (tasks.containsKey(taskId)) {
            Task task = tasks.get(taskId);
            task.getScheduledFuture().cancel(true);
            tasks.remove(taskId);
            // 实现从数据库中删除任务的操作
            System.out.println("Task with ID " + taskId + " has been deleted.");
        } else {
            System.out.println("Task with ID " + taskId + " does not exist.");
        }
    }

    public void compareTasksWithDatabase() {
        // 实现定时比对数据库中的任务和程序中的任务调度策略是否相同的操作
        System.out.println("Comparing tasks with the database...");
    }

    private void persistTaskInfo(int taskId, String cronExpression) {
        System.out.println("Persisting task information to the database...");
        // 实现将任务信息存储到数据库的操作
    }

    private void persistUpdatedSchedule(int taskId, String newCronExpression) {
        System.out.println("Persisting updated schedule to the database...");
        // 实现将更新后的调度策略存储到数据库的操作
    }

    public static void main(String[] args) {
        TaskSchedulerManager taskSchedulerManager = new TaskSchedulerManager();

        // 创建调度任务
        taskSchedulerManager.scheduleTask(1, "0/5 * * * * ?", () -> System.out.println("Executing scheduled task 1...")); // 每5秒执行一次任务
        taskSchedulerManager.scheduleTask(2, "0/10 * * * * ?", () -> System.out.println("Executing scheduled task 2...")); // 每10秒执行一次任务

        // 根据任务ID修改调度任务的Cron表达式
//        taskSchedulerManager.rescheduleTask(1, "0/7 * * * * ?"); // 每7秒执行一次任务

        // 根据任务ID删除调度任务
//        taskSchedulerManager.deleteTask(2);

        // 定时比对数据库中的任务和程序中的任务调度策略
//        taskSchedulerManager.compareTasksWithDatabase();
    }
}
