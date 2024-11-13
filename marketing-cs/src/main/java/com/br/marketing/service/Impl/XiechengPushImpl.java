package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.constants.rocketmq.MarketingAssistConstants;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.entity.XieChengData;
import com.br.marketing.mapper.XieChengDataMapper;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.rocketmq.rocketmq.template.RocketMqTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class XiechengPushImpl {


    @Resource
    XieChengDataMapper xieChengDataMapper;

    @Resource
    private RabbitMqProducter producter;
    @Resource
    private RocketMqSwitch rocketMqSwitch;
    @Resource
    private RocketMqTemplate template;

    public void pushXieCheng(String date){
        Long id = null;
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(10, 10);
        Boolean status = Boolean.TRUE;
        if (StringUtils.isBlank(date)) {
            date = "2024-03-26";
        }
        while (status){
            List<XieChengData> xieChengData = xieChengDataMapper.selectXieChengCall(date, id);
            if(xieChengData.size()<=0){
                status = Boolean.FALSE;
                continue;
            }
            threadPool.submit(()->{
                for (XieChengData xieChengDatum : xieChengData) {
                    JSONObject msg = new JSONObject();
                    msg.put("localId", xieChengDatum.getLocalId());
                    msg.put("type", 2);
                    if(rocketMqSwitch.rocketMQSwitchFlag(xieChengDatum.getApiCode()
                            , MarketingAssistConstants.TAG_MARKETING_UNIVERSAL_SFTPTODB_XIECHENGRECEIVE)){
                        rocketMqSwitch.syncSend(MarketingAssistConstants.TOPIC
                                , MarketingAssistConstants.TAG_MARKETING_UNIVERSAL_SFTPTODB_XIECHENGRECEIVE, msg.toJSONString());
                    }else{
                        producter.send("Marketing.Universal.SftpToDb.XieChengReceive" , msg.toJSONString());
                    }
                }
            });
            id = xieChengData.get(xieChengData.size()-1).getId();
        }
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(5L, TimeUnit.SECONDS)) {

            }
        }catch (Exception ex){
            log.error(ex.getMessage(), ex);
        }
    }
}
