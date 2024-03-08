package com.br.marketing.monkey.job.dewu;

import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.service.DewuCollidingDataService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 得物撞库job
 *
 * @author 张广超
 * @dateTime 2024/03/08 16:13
 */
@Component
@Slf4j
public class DewuCollidingDataToSendJob extends AbstractSimpleElasticJob {



    @Resource
    private DewuCollidingDataService dewuCollidingDataService;
    @Resource
    private LocalFileMapper localFileMapper;

    private final static String DEWUCOLLIDINGDATA = "dewucollidingdata";
    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        LocalFileExample localFileExample = new LocalFileExample();
        localFileExample.createCriteria()
                .andFileTypeEqualTo(DEWUCOLLIDINGDATA)
                .andStatusEqualTo("2");
        localFileExample.setOrderByClause("create_time desc");
        List<LocalFile> localFileList = localFileMapper.selectByExample(localFileExample);
        localFileList.forEach((LocalFile lf) ->
            dewuCollidingDataService.collidingDataProcess(lf)
        );
    }
}
