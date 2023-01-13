package com.br.marketing.check.job;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.entity.XieChengSmsCollidingData;
import com.br.marketing.entity.XieChengSmsCollidingDataExample;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.XieChengDataMapper;
import com.br.marketing.mapper.XieChengSmsCollidingDataMapper;
import com.br.marketing.mapper.XiechengSmsQuitDataMapper;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.service.PushDataService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
                .andStatusEqualTo("1");
        List<LocalFile> localFileList = localFileMapper.selectByExample(localFileExample);


        localFileList.stream().forEach((localFile) -> {
            JSONObject msg = new JSONObject();
            msg.put("localId", localFile.getId());
            msg.put("type", 2);
            producter.send("Marketing.Universal.SftpToDb.XieChengSmsCollidingReceive", msg.toJSONString());
        });
    }

}
