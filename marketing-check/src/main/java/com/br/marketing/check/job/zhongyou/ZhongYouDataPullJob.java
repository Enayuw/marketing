package com.br.marketing.check.job.zhongyou;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.service.ZhongYouDataService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 描述：： 中邮数据拉取
 * <p>
 * ------------------------------------
 *
 * @program: marketing
 * @ClassName ZhongYouDataPullJob
 * @author: it-yml
 * @create: 2023-08-02 17:03
 * @Version 1.0
 * --------------------------------------
 **/
@Component
@Slf4j
public class ZhongYouDataPullJob extends AbstractSimpleElasticJob {

    @Resource
    private ZhongYouDataService zhongYouDataService;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        Result postResult = zhongYouDataService.saveFileNameList();
        Object data = postResult.getData();


    }

}
