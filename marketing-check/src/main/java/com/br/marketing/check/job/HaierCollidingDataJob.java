package com.br.marketing.check.job;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.service.PushDataService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class HaierCollidingDataJob extends AbstractSimpleElasticJob {

    private final static String HAIER_COLLIDING = "haierColliding";

    @Resource
    private PushDataService pushDataService;
    @Resource
    private LocalFileMapper localFileMapper;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        String formatted = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        final LocalFileExample localFileExample = new LocalFileExample();
        localFileExample.createCriteria().andFileTypeEqualTo(HAIER_COLLIDING).andFileNameLike("%" + formatted + "%").andStatusEqualTo("2");
        localFileExample.setOrderByClause("id desc");
        List<LocalFile> localFileList = localFileMapper.selectByExample(localFileExample);
        localFileList.forEach((LocalFile lf) -> pushDataService.pushHaierCollidingData(lf.getId()));
    }

}
