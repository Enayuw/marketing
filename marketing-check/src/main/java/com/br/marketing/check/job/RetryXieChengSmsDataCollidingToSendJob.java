package com.br.marketing.check.job;

import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.service.PushDataService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author guangchao.zhang
 * @Classname CallingToSendJob
 * @Description 携程异常重试job
 * @Date 2024/2/26 10:02 AM
 */
@Component
@Slf4j
public class RetryXieChengSmsDataCollidingToSendJob extends AbstractSimpleElasticJob {
    private final static String XIECHENGSMSCOLLIDING = "xiechengsmscolliding";
    @Resource
    private PushDataService pushDataService;
    @Resource
    private LocalFileMapper localFileMapper;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        LocalFileExample localFileExample = new LocalFileExample();
        localFileExample.createCriteria()
                .andFileTypeEqualTo(XIECHENGSMSCOLLIDING)
                .andStatusEqualTo("2");
        List<LocalFile> localFileList = localFileMapper.selectByExample(localFileExample);
        localFileList.forEach((lf) ->
                pushDataService.retryPushXieChengSmsCollidingToDbData(lf.getId())
        );
    }
}
