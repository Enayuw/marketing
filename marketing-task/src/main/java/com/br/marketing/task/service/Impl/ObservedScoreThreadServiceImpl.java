package com.br.marketing.task.service.Impl;

import com.br.marketing.entity.MarketingTask;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;

@Service
public class ObservedScoreThreadServiceImpl {

    private ExecutorService executorService;

    private List<MarketingTask> taskList;

    public void addObserver(ExecutorService executorService){
        this.executorService = executorService;
    }

    public List<MarketingTask> getTaskList() {
        return taskList;
    }

    public void addTaskList(List<MarketingTask> taskList) {
        this.taskList = taskList;
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
