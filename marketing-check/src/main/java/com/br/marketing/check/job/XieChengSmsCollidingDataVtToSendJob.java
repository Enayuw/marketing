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
 * 描述：： 携程新版短信撞库 job
 * <p>
 * ------------------------------------
 *
 * @program: marketing
 * @ClassName XieChengSmsCollidingDataVtToSendJob
 * @author: it-yml
 * @create: 2023-07-11 19:31
 * @Version 1.0
 * --------------------------------------
 **/
@Component
@Slf4j
public class XieChengSmsCollidingDataVtToSendJob extends AbstractSimpleElasticJob {
    private static final String XIECHENGSMSCOLLIDINGVT = "xiechengsmscollidingvt";


    /**
     * 文件
     */
    @Resource
    private LocalFileMapper localFileMapper;

    /**
     * 推送实现
     */
    @Resource
    private PushDataService pushDataService;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        final LocalFileExample localFileExample = new LocalFileExample();
        localFileExample.createCriteria()
                .andFileTypeEqualTo(XIECHENGSMSCOLLIDINGVT)
                .andStatusEqualTo("1");
        List<LocalFile> localFileList = localFileMapper.selectByExample(localFileExample);
        localFileList.forEach((LocalFile lf) ->
                pushDataService.pushXieChengSmsCollidingToDbDataVt(lf.getId()));
    }
}
