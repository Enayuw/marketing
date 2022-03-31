package com.br.marketing.rule.yixin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.impl.YiXinRealTimeRuleCollectDataImpl;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.origin.MqFact;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.ZnkfPushService;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import org.springframework.stereotype.Service;
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
public class YiXinRealTimeDataMessageDelayImpl implements AssembleData<MqFact> {

    @Resource
    private ZnkfPushService znkfPushService;

    private final static String CUSTOMER_NUMBER_IS_FIRST = "customer:realtime:first";
    @Override
    public MqFact assemble(Object transmitFact, ProcessHandlerContext context) {
        MqFact mqFact = context.getMqFact();
        mqFact.setIsDelay(1);
        return mqFact;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        String reserveField1 = transfer.getReserveField1();
        if (StringUtils.hasText(reserveField1)){
            YiXinRealTimeRuleCollectDataImpl.YiXinRealTimeRuleNecessaryData ruleNecessaryData =
                    (YiXinRealTimeRuleCollectDataImpl.YiXinRealTimeRuleNecessaryData) context.getRuleNecessaryData();
            JSONObject json = JSON.parseObject(reserveField1);
            Integer transformType = json.getInteger("transformType");
            Integer liveType = json.getInteger("liveType");
            MqFact mqFact = context.getMqFact();
            String key = CUSTOMER_NUMBER_IS_FIRST.concat(":").concat(transfer.getUserType()).concat(":").concat(transfer.getCustNum());
            Map<String, String> blackList = ruleNecessaryData.getBlackList();
            boolean notBlack = "N".equals(blackList.get(transfer.getId().toString()));
            /*
            满足条件进入延迟队列
                1、不满足客服黑名单
                1、当天该案件编号未被推送
                2、transformType 为1
                3、需要静置的liveType 4,6,8
                4、不是从延迟队列过来的消息
             */
            return notBlack && znkfPushService.cusNumIsFirstToday(key) && 1 == transformType
                    && Arrays.asList(4,6,8).contains(liveType) && 1 != mqFact.getIsDelay();
        }
        return false;
    }

    @Override
    public String label() {
        return "YiXin_RealTimeData_MessageDelay";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.MESSAGE_DELAY.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }
}
