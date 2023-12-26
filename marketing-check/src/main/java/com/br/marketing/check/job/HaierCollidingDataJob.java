package com.br.marketing.check.job;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.br.marketing.entity.HaierCollidingDataExample;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.mapper.HaierCollidingDataMapper;
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
    private HaierCollidingDataMapper haierCollidingDataMapper;


    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        String formatted = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        List<LocalFile> localFileList = localFileMapper.getNotPushLocalFileByFileTypeAndFileName("%" + formatted + "%",HAIER_COLLIDING);
        localFileList.forEach((LocalFile localFile) -> {

            if(localFile.getPushStartTime() == null){
                localFile.setPushStartTime(new Date());
                localFile.setPushStatus("1");
            }
            //执行撞库逻辑
            pushDataService.pushHaierCollidingData(localFile.getId());
            //更新文件表
            HaierCollidingDataExample dataExample = new HaierCollidingDataExample();
            dataExample.createCriteria()
                .andLocalIdEqualTo(localFile.getId())
                .andPushStatusEqualTo(2)
                .andStatusEqualTo(1);
            //获取推送数量
            int pushNum = haierCollidingDataMapper.countByExample(dataExample);
            localFile.setPushNumber(pushNum);
            int total = localFile.getActualNumber() - localFile.getErrorActualNumber();
            //判断是否推送完成
            if(total == pushNum){
                localFile.setPushEndTime(new Date());
                localFile.setPushStatus("2");
            }
            localFileMapper.updateByPrimaryKeySelective(localFile);
        });
    }

}
