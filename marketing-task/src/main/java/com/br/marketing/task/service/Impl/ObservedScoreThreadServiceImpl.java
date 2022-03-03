package com.br.marketing.task.service.Impl;


import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Service
public class ObservedScoreThreadServiceImpl {

    private Integer interrupt = 1;

    private List<ExecutorService> executorService = new ArrayList<>();

    public Integer getInterrupt() {
        return interrupt;
    }

    public void setInterrupt(Integer interrupt) {
        this.interrupt = interrupt;
    }


    public void addObserver(ExecutorService executorService){
        this.executorService.add(executorService);
    }

    public void removeThread(ExecutorService executorService){
        if(this.executorService != null){
            this.executorService.remove(executorService);
        }
    }

    public void stopThread(){
        for (ExecutorService service : executorService) {
            if(service != null&&!service.isTerminated()){
                service.shutdownNow();
            }
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
