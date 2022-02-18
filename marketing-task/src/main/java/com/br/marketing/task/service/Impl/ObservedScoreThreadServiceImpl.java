package com.br.marketing.task.service.Impl;

import com.br.marketing.entity.MarketingTask;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.error.Mark;

import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;

@Service
public class ObservedScoreThreadServiceImpl {

    private ExecutorService executorService;

    private CopyOnWriteArraySet<MarketingTask> taskList = new CopyOnWriteArraySet<>();

    public void addObserver(ExecutorService executorService){
        this.executorService = executorService;
    }

    public void addTaskList(MarketingTask task){
        taskList.add(task);
    }

    public void removeTaskList(MarketingTask task){
        taskList.remove(task);
    }

    public CopyOnWriteArraySet<MarketingTask> getTaskList(){
        return taskList;
    }

    public void removeThread(){
        if(this.executorService != null){
            this.executorService =null;
        }
    }

    public void stopThread(){
        if(this.executorService != null){
            this.executorService.shutdownNow();
        }
    }
}
