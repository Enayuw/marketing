package com.br.marketing.check.job;

import com.br.marketing.client.xiecheng.XieChengService;
import com.br.marketing.mapper.XieChengDataMapper;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.service.PushRuleService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author guangchao.zhang
 * @Classname CallingToSendJob
 * @Description 回调第三方接口发送不打信息
 * @Date 2022/2/16 10:02 AM
 */
@Component
@Slf4j
public class XiechengToSendJob extends AbstractSimpleElasticJob {

    @Autowired
    RabbitMqProducter producter;
    @Resource
    private XieChengDataMapper xieChengDataMapper;
    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        List<String> strings = xieChengDataMapper.selectLocalIdByNotSend();
        strings.forEach(str->{
            producter.send("Marketing.Universal.SftpToDb.XieChengReceive", str);
        });
    }
}
