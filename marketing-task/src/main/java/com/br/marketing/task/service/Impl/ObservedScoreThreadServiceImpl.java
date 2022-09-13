package com.br.marketing.task.service.Impl;


import com.br.marketing.entity.MarketingTask;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.enums.ScoreStatusEnum;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.task.dto.ObservedTaskObj;
import lombok.Data;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Service
public class ObservedScoreThreadServiceImpl {

    private Integer interrupt = 1;

    @Resource
    StraHisFileMapper straHisFileMapper;

    private List<ObservedTaskObj> ObservedTaskObjs = new ArrayList<>();

    public Integer getInterrupt() {
        return interrupt;
    }

    public void setInterrupt(Integer interrupt) {
        this.interrupt = interrupt;
    }


    public void addObserver(ObservedTaskObj ObservedTaskObj) {
        this.ObservedTaskObjs.add(ObservedTaskObj);
    }

    public void removeThread(ObservedTaskObj ObservedTaskObj) {
        if (this.ObservedTaskObjs != null) {
            this.ObservedTaskObjs.remove(ObservedTaskObj);
        }
    }

    public void stopThreadAll() {
        for (ObservedTaskObj obj : ObservedTaskObjs) {
            stopThread(obj);
        }
        this.interrupt = 0;
    }

    public void stopThread(ObservedTaskObj ObservedTaskObj) {
        ExecutorService executorService = ObservedTaskObj.getExecutorService();
        MarketingTask marketingTask = ObservedTaskObj.getMarketingTask();
        if(!executorService.isTerminated()&& ObservedTaskObj.getInterrupt().equals(0)){
            ObservedTaskObj.setInterrupt(1);
            StraHisFile file = new StraHisFile();
            file.setStatus(ScoreStatusEnum.PAUSE.getValue());
            file.setId(marketingTask.getFileId());
            straHisFileMapper.updateByPrimaryKeySelective(file);
            executorService.shutdownNow();
        }
    }

    public boolean isInterrupt() {
        if (new Integer(0).equals(this.interrupt)) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

}
