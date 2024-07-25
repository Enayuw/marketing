package com.br.marketing.monkey.job.zhijia;

import com.br.marketing.service.Impl.zhijia.ZhiJiaDataProcessService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @ClassName ZhiJiaGetAreaBrandJob
 * @Description 之家获取区域及品牌人任务
 * @Author zhen.Li1
 * @Date 2024/7/10 16:02
 */
@Component
@Slf4j
public class ZhiJiaGetAreaBrandJob extends AbstractSimpleElasticJob {

    @Autowired
    ZhiJiaDataProcessService zhiJiaDataProcessService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {

        //获取市区县
        zhiJiaDataProcessService.getCityAndCounty();
        //获取品牌和车系
        zhiJiaDataProcessService.getBrandAndseries();

    }
}
