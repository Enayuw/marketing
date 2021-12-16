package com.br.marketing.task.service.Impl;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;

@Service
public class ObservedScoreThreadServiceImpl {

    private ExecutorService executorService;

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
    }
}
