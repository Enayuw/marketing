package com.br.marketing.monkey.job.didi;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.mapper.DidiCallRecordMapper;
import com.br.marketing.mapper.XieChengDataMapper;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.google.common.base.Splitter;
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
public class DidiCallingRecordJob extends AbstractSimpleElasticJob {

    @Autowired
    RabbitMqProducter producter;
    @Resource
    private XieChengDataMapper xieChengDataMap;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private DidiCallRecordMapper didiCallRecordMapper;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {

        String jobParameter = jobExecutionMultipleShardingContext.getJobParameter();
        if (StringUtils.isNotBlank(jobParameter)) {
            List<String> sendDateLists = Splitter.on(",").splitToList(jobParameter);
        }else {
            //didiCallRecordMapper.
        }





        while (Boolean.FALSE.equals(marketingCommonConfig.getXieChengCallingRecordSwitch())) {
            List<String> localIds = xieChengDataMap.selectLocalIdByNotSend();
            if (localIds.isEmpty()) {
                break;
            }
            localIds.stream().forEach(localId -> {
                JSONObject msg = new JSONObject();
                msg.put("localId", localId);
                msg.put("type", 2);
                producter.send(ROUTING_KEY_UNIVERSAL_SFTPTODB_XIECHENGRECEIVE
                        , msg.toJSONString());
            });
            try {
                Thread.sleep(marketingCommonConfig.getXieChengCallingRecordSleep());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }


    }
}
