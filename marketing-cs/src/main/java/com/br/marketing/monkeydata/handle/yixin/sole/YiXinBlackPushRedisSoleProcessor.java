package com.br.marketing.monkeydata.handle.yixin.sole;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.monkeydata.entity.yixin.YiXinCondition;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class YiXinBlackPushRedisSoleProcessor {

    @Resource
    private RedisChgService redisChgService;

    private final static String TITLE = "【宜信转化过滤推送百应】-黑名单推送-REDIS去重";

    public List<MarketingSyncUser> process(List<MarketingSyncUser> pushList, YiXinCondition condition, Integer distributeType){
        if(CollectionUtils.isEmpty(pushList)){
            return pushList;
        }

        String apiCode = condition.getApiCode();
        String distributeDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Integer soleDay = 1;
        String key = RedisKeyConstant.YIXIN_TRANSFER_PUSH_BAIYING_REDIS_SLOE;
        key = key.concat(String.format(":%d:%d:%s:%s", distributeType, soleDay, apiCode, distributeDate));

        Iterator<MarketingSyncUser> iterator = pushList.iterator();
        int beforePushSize = pushList.size();
        long startTime = System.currentTimeMillis();
        while(iterator.hasNext()){
            MarketingSyncUser next = iterator.next();
            String custNum = next.getCustNum();
            try {
                Long successQuantity = redisChgService.saddMember(key, custNum);
                if(successQuantity == null || successQuantity != 1L) {
                    iterator.remove();
                }
            }catch (Exception e){
                iterator.remove();
                log.error(TITLE+"去重异常"+custNum, e);
            }
        }
        long endTime = System.currentTimeMillis();
        log.warn(TITLE+"去重量级{}，去重耗时{}", beforePushSize, (endTime-startTime));
        return pushList;
    }

}
