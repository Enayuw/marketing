package com.br.marketing.check.job;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.constants.rocketmq.MarketingAssistConstants;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.mapper.XieChengDataMapper;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.br.marketing.common.utils.MQConstants.ROUTING_KEY_UNIVERSAL_SFTPTODB_XIECHENGRECEIVE;

/**
 * @author GuangChao.Zhang
 * @version 1.0
 * @date 2023/2/18 16:57
 */
@Component
@Slf4j
public class XieChengCallingRecordJob extends AbstractSimpleElasticJob {

    @Autowired
    RabbitMqProducter producter;
    @Resource
    private XieChengDataMapper xieChengDataMap;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private RocketMqSwitch rocketMqSwitch;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        while (Boolean.FALSE.equals(marketingCommonConfig.getXieChengCallingRecordSwitch())) {
            List<String> localIds = xieChengDataMap.selectLocalIdByNotSend();
            if (localIds.isEmpty()) {
                break;
            }
            localIds.stream().forEach(localId -> {
                JSONObject msg = new JSONObject();
                msg.put("localId", localId);
                msg.put("type", 2);
//                producter.send(ROUTING_KEY_UNIVERSAL_SFTPTODB_XIECHENGRECEIVE
//                        , msg.toJSONString());
                rocketMqSwitch.sendMessage("", MarketingAssistConstants.TOPIC,
                        MarketingAssistConstants.TAG_MARKETING_UNIVERSAL_SFTPTODB_XIECHENGRECEIVE, ROUTING_KEY_UNIVERSAL_SFTPTODB_XIECHENGRECEIVE,
                        msg.toJSONString());
            });
            try {
                Thread.sleep(marketingCommonConfig.getXieChengCallingRecordSleep());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }


    }
}
