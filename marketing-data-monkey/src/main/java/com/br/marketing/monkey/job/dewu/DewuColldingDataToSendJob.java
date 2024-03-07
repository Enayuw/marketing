package com.br.marketing.monkey.job.dewu;

import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.service.DewuCollidingDataService;
import com.br.marketing.service.Impl.tongcheng.TongChengOperationPushToCustomerService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 得物撞库job
 *
 * @author guangxiu.li
 * @dateTime 2024/01/25 16:13
 */
@Component
@Slf4j
public class DewuColldingDataToSendJob extends AbstractSimpleElasticJob {



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
