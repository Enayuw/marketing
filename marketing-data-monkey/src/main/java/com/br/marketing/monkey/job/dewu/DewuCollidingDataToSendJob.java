package com.br.marketing.monkey.job.dewu;

import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.service.DewuCollidingDataService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        String uuid = UUID.randomUUID().toString();
        log.warn("DewuCollidingDataToSendJob-start-{}",uuid);
        String jobParameter = context.getJobParameter();
        List<String> localFileIds = new ArrayList<>();
        if (StringUtils.isNotBlank(jobParameter)) {
            localFileIds = Splitter.on(",").splitToList(jobParameter);
        }
        if(localFileIds.size()>0){
            localFileIds.forEach((String localFileId)->{
                dewuCollidingDataService.collidingDataProcess(Long.valueOf(localFileId));
            });
        }else {
            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            LocalFileExample localFileExample = new LocalFileExample();
            LocalFileExample.Criteria criteria = localFileExample.createCriteria();
            criteria
                    .andFileTypeEqualTo(DEWUCOLLIDINGDATA)
                    .andFileNameLike("%"+currentDate+"%")
                    .andStatusEqualTo("2");
            localFileExample.setOrderByClause("create_time desc");
            List<LocalFile> localFileList = localFileMapper.selectByExample(localFileExample);
            localFileList.forEach((LocalFile lf) ->
                    dewuCollidingDataService.collidingDataProcess(lf.getId())
            );
        }
        log.warn("DewuCollidingDataToSendJob-end-{}",uuid);
    }
}
