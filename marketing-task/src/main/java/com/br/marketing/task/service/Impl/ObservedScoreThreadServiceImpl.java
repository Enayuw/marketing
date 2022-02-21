package com.br.marketing.task.service.Impl;

import com.br.marketing.entity.MarketingTask;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.error.Mark;

import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;

@Service
public class ObservedScoreThreadServiceImpl {

    private Integer interrupt = 1;

    private ExecutorService executorService;

    public Integer getInterrupt() {
        return interrupt;
    }

    public void setInterrupt(Integer interrupt) {
        this.interrupt = interrupt;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public void addObserver(ExecutorService executorService){
        this.executorService = executorService;
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
        this.interrupt=0;
    }

    public boolean isInterrupt(){
        if(new Integer(0).equals(this.interrupt)){
            return Boolean.TRUE;
        }else{
            return Boolean.FALSE;
        }
    }
}
