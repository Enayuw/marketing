package com.br.marketing.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.constants.rocketmq.MarketingDelayedConstants;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.handle.SnowflakeRedisGeneratorHandle;
import com.br.marketing.origin.DataLoadingHandlerService;
import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.TransferSource;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.rocketmq.rocketmq.template.RocketMqTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * code is far away from bug with the animal protecting
 * ┏┓　　　┏┓
 * ┏┛┻━━━┛┻┓
 * ┃　　　　　　　┃
 * ┃　　　━　　　┃
 * ┃　┳┛　┗┳　┃
 * ┃　　　　　　　┃
 * ┃　　　┻　　　┃
 * ┃　　　　　　　┃
 * ┗━┓　　　┏━┛
 * 　　┃　　　┃神兽保佑
 * 　　┃　　　┃代码无BUG！
 * 　　┃　　　┗━━━┓
 * 　　┃　　　　　　　┣┓
 * 　　┃　　　　　　　┏┛
 * 　　┗┓┓┏━┳┓┏┛
 * 　　　┃┫┫　┃┫┫
 * 　　　┗┻┛　┗┻┛
 *
 * @Description : 消息延迟处理类
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/3/14 10:56
 */

@Service
public class BatchMessageDelayHandler extends AbstractExternalInterfaceHandler<MqFact>{

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    //消息过期时间 1h
    private static final String EXPIRE_TIME = "3600000";

    @Resource
    private RabbitMqProducter producer;
    @Resource
    private RocketMqSwitch rocketMqSwitch;
    @Resource
    private RocketMqTemplate template;

    @Resource
    private DataLoadingHandlerService handlerService;
    @Resource
    private SnowflakeRedisGeneratorHandle snowflakeRedisGeneratorHandle;

    @Override
    JSONObject call(List<MqFact> mqFacts, ProcessHandlerContext context) {
        String expireTime = StringUtils.hasText(marketingCommonConfig.getMessageQueueExpireTime())
                ?marketingCommonConfig.getMessageQueueExpireTime():EXPIRE_TIME;
        String apiCode = context.getApiCode();
        /**
         * eg:{"last": 0,"tcId": 772,"ids": [607772,607771,607770,607769,607768],"apiCode": "7410430"}
         */

        boolean b = mqFacts.get(0).getIncludeRules() != null && mqFacts.get(0).getIncludeRules().size() > 0;
        Set<String> set = new HashSet<>();
        if(b){
            set = mqFacts.get(0).getIncludeRules();
        }else{
            HashMap<String, List<String>> ppdCustomerType = marketingCommonConfig.getPpdCustomerType();
            // 拍拍贷处理规则
            if (ppdCustomerType.get("transform").contains(apiCode)){
                set.add("PPD_TransferData_ArtificialBatch");
            }else{
                //宜信处理规则
                set.add("YiXin_RealTimeData_ArtificialToPolicyRule");
            }
        }
        StringUtils.isEmpty(mqFacts.get(0).getSource());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("apiCode", apiCode);
        jsonObject.put("ids",mqFacts.stream().map(MqFact::getSourceId).collect(Collectors.toSet()));
        jsonObject.put("tcId",handlerService.getTcIdFromRedis(apiCode));

        MqFact mqFact = new MqFact();
        mqFact.setSourceId(context.getTransferInfoId());
        mqFact.setIsDelay(1);
        mqFact.setIncludeRules(set);
        mqFact.setMessage(jsonObject.toJSONString());
        mqFact.setSource(TransferSource.TRANSFER_DATA_SET_PROCESS.getCode());
        mqFact.setIdempotentKey(snowflakeRedisGeneratorHandle.nextId());

        String message = JSON.toJSONString(mqFact);
        if(rocketMqSwitch.rocketMQSwitchFlag(apiCode, MarketingDelayedConstants.TAG_MARKETING_UNIVERSAL_TRANSFER_RECEIVE_DELAY_HALFHOUR)){
            rocketMqSwitch.syncSendDelaySecond(MarketingDelayedConstants.TOPIC
                    , MarketingDelayedConstants.TAG_MARKETING_UNIVERSAL_TRANSFER_RECEIVE_DELAY_HALFHOUR, message
                    , Integer.parseInt(expireTime)/1000);
        }else{
            producer.sendByExpiration(MQConstants.ROUTING_KEY_UNIVERSAL_TRANSFER_RECEIVE_DELAY,message,expireTime);
        }
        return null;
    }

    @Override
    InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.BATCH_MESSAGE_DELAY;
    }
}
