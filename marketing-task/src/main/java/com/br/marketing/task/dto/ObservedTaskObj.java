package com.br.marketing.task.dto;

import com.br.marketing.entity.MarketingTask;
import lombok.Data;

import java.util.concurrent.ExecutorService;

@Data
public class ObservedTaskObj {
    private ExecutorService executorService;
    private MarketingTask marketingTask;
    private Integer interrupt = 0;

    public ObservedTaskObj(ExecutorService executorService,MarketingTask marketingTask){
        this.executorService = executorService;
        this.marketingTask = marketingTask;
    }
}
