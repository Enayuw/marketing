package com.br.marketing.monkey.job.carclue;

import com.br.marketing.entity.CarClueInfoExample;
import com.br.marketing.entity.TransferFileTask;
import com.br.marketing.entity.TransferFileTaskExample;
import com.br.marketing.enums.carclue.CarClueDataStatusEnum;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 车线索数据清洗作业job
 *
 * @author zhen.Li1
 * @dateTime 2025/01/05 14:13
 */
@Component
@Slf4j
public class CarClueDataCleanJob extends AbstractSimpleElasticJob {
    @Override
    public void process(JobExecutionMultipleShardingContext context) {


        String apiCode = "3710012";
        CarClueInfoExample carClueInfoExample = new CarClueInfoExample();
        carClueInfoExample.createCriteria().andApiCodeEqualTo(apiCode).andClueDataStatusEqualTo(CarClueDataStatusEnum.READY.getValue());



    }
}
