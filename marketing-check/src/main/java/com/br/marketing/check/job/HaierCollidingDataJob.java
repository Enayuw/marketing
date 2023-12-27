package com.br.marketing.check.job;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.br.marketing.entity.HaierCollidingDataLogExample;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.mapper.HaierCollidingDataLogMapper;
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
    @Resource
    private HaierCollidingDataLogMapper haierCollidingDataLogMapper;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        List<LocalFile> localFileList = localFileMapper.getNotPushLocalFileByFileTypeAndFileName("%" + currentDate + "%", HAIER_COLLIDING);
        localFileList.forEach((LocalFile localFile) -> {
            if (localFile.getPushStartTime() == null) {
                localFile.setPushStartTime(new Date());
            }
            // 执行撞库逻辑
            pushDataService.pushHaierCollidingData(localFile.getId());
            // 更新文件表
            HaierCollidingDataLogExample dataLogExample = new HaierCollidingDataLogExample();
            dataLogExample.createCriteria().andLocalIdEqualTo(localFile.getId()).andStatusIn(Arrays.asList(2, 3))
                .andSendDateEqualTo(Integer.valueOf(currentDate));
            // 获取推送数量
            int pushNum = haierCollidingDataLogMapper.countByExample(dataLogExample);
            localFile.setPushNumber(pushNum);
            if (pushNum == localFile.getActualNumber() - localFile.getErrorActualNumber()) {
                localFile.setPushStatus("2");
                localFile.setPushEndTime(new Date());
            }
            localFileMapper.updateByPrimaryKeySelective(localFile);
        });
    }

}
