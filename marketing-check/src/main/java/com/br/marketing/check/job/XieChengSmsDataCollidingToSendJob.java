package com.br.marketing.check.job;

import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.XieChengDataMapper;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author guangchao.zhang
 * @Classname CallingToSendJob
 * @Description 回调第三方接口发送不打信息
 * @Date 2022/2/16 10:02 AM
 */
@Component
@Slf4j
public class XieChengSmsDataCollidingToSendJob extends AbstractSimpleElasticJob {

    private final static String XIECHENGSMSCOLLIDING = "xiechengsmscolliding";

    @Autowired
    RabbitMqProducter producter;
    @Resource
    private LocalFileMapper localFileMapper;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        LocalFileExample localFileExample = new LocalFileExample();
        localFileExample.createCriteria()
                .andFileTypeEqualTo(XIECHENGSMSCOLLIDING)
                .andStatusEqualTo("1")
                .andPushStatusNotEqualTo("1");
        List<LocalFile> localFileList = localFileMapper.selectByExample(localFileExample);


        localFileList.stream().forEach((localFile) -> {
            Date createTime = localFile.getCreateTime();
            if (differentDaysByMillisecond(createTime, new Date(), 15 * 24)) {
                producter.send("Marketing.Universal.SftpToDb.XieChengSmsCollidingReceive", String.valueOf(localFile.getId()));
            }
        });
    }

    /**
     * 通过时间秒毫秒数判断两个时间的间隔
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean differentDaysByMillisecond(Date date1, Date date2, int hours) {
        int days = ((int) ((date2.getTime() - date1.getTime()) / (1000 * 3600)));
        return days / hours > 0 && days % hours == 0;
    }

}
