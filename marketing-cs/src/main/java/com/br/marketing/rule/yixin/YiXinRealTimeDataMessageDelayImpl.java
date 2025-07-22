package com.br.marketing.rule.yixin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.YiXinRuleCollectDataImpl;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.origin.MqFact;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Map;

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
 * @Description : 宜信实时数据满足特定条件进入延迟队列
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/3/28 15:54
 */

@Service
@Slf4j
public class YiXinRealTimeDataMessageDelayImpl implements AssembleData<MqFact> {

    @Resource
    private RedisChgService redisChgService;

    private final static String CUSTOMER_NUMBER_IS_FIRST = "customer:realtime:first";
    @Override
    public MqFact assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        MqFact mqFact = new MqFact();
        mqFact.setSourceId(transfer.getId());
        return mqFact;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        String reserveField1 = transfer.getReserveField1();
        if (StringUtils.hasText(reserveField1)){
            YiXinRuleCollectDataImpl.YiXinRuleNecessaryData ruleNecessaryData =
                    (YiXinRuleCollectDataImpl.YiXinRuleNecessaryData) context.getRuleNecessaryData();
            JSONObject json = JSON.parseObject(reserveField1);
            boolean transformType = "1".equals(json.getString("transformType"));
            Integer liveType = json.getInteger("liveType");
            MqFact mqFact = context.getMqFact();
            String key = CUSTOMER_NUMBER_IS_FIRST.concat(":").concat(transfer.getCustNum());
            Map<String, String> blackList = ruleNecessaryData.getBlackList();
            boolean notBlack = true;
            if (!CollectionUtils.isEmpty(blackList)){
                notBlack = "N".equals(blackList.get(transfer.getId().toString()));
            }
            /*
            满足条件进入延迟队列
                1、不满足客服黑名单
                1、当天该案件编号未被推送
                2、transformType 为1
                3、需要静置的liveType 4,6,8
                4、不是从延迟队列过来的消息
             */
            if (!notBlack){
                log.warn("宜信实时推决策id:{} cust_num:{}不满足黑名单条件", transfer.getId(), transfer.getCustNum());
                return false;
            }
            if (redisChgService.exists(key)){
                log.warn("宜信实时推决策id:{} key:{}不满足当天该案件编号未被推送", transfer.getId(), key);
                return false;
            }
            /* 2023-03-24 liveType是4,6的数据不进入静置队列 */
            boolean flag = transformType && Arrays.asList(8,9).contains(liveType) && mqFact.getIsDelay() == null;
            if (!flag){
                log.warn("宜信实时推决策id:{} cust_num:{}不满足进入延迟队列", transfer.getId(), transfer.getCustNum());
                return false;
            }

            return  true;
        }
        return false;
    }

    @Override
    public String label() {
        return "YiXin_RealTimeData_MessageDelayToPolicyRule";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.BATCH_MESSAGE_DELAY.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.YI_XIN_DATA_COLLECTION.getCode();
    }
}
