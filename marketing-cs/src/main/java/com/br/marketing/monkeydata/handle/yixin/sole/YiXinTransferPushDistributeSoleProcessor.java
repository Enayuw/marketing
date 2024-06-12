package com.br.marketing.monkeydata.handle.yixin.sole;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.DistributeSourceTypeEnum;
import com.br.marketing.common.enums.DistributeTypeEnum;
import com.br.marketing.entity.DataDistributeDetailLog;
import com.br.marketing.entity.DataDistributeDetailLogExample;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.DataDistributeDetailLogMapper;
import com.br.marketing.monkeydata.entity.yixin.YiXinCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class YiXinTransferPushDistributeSoleProcessor {

    @Resource
    private RedisChgService redisChgService;

    @Resource
    private DataDistributeDetailLogMapper dataDistributeDetailLogMapper;

    private final static String TITLE = "【宜信转化过滤推送百应】-转化推送";

    public List<MarketingTransferSyncUser> process(List<MarketingTransferSyncUser> pushList, YiXinCondition condition){
        String key = RedisKeyConstant.YIXIN_TRANSFER_PUSH_BAIYING_DISTRIBUTE_DATA_SLOE_LOCK;
        Integer distributeType = DistributeTypeEnum.YIXIN_TRANSFER_PUSH_BAIYING.getValue();
        Integer soleDay = 1;

        if(CollectionUtils.isEmpty(pushList)){
            return pushList;
        }

        Iterator<MarketingTransferSyncUser> iterator = pushList.iterator();
        int beforePushSize = pushList.size();
        long startTime = System.currentTimeMillis();
        while(iterator.hasNext()){
            MarketingTransferSyncUser next = iterator.next();
            String apiCode = next.getApiCode();
            String custNum = next.getCustNum();
            key = key.concat(String.format(":%d:%d:%s:%s", distributeType, soleDay, apiCode, custNum));
            String lockValue = UUID.randomUUID().toString();
            try {
                redisChgService.lock(key, lockValue);
                String distributeDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                DataDistributeDetailLogExample logExample = new DataDistributeDetailLogExample();
                logExample.setOrderByClause(" id limit 1 ");
                DataDistributeDetailLogExample.Criteria criteria = logExample.createCriteria();
                criteria.andApiCodeEqualTo(apiCode)
                        .andDistributeTypeEqualTo(distributeType)
                        .andDistributeDateEqualTo(distributeDate)
                        .andCustNumEqualTo(custNum)
                        ;
                List<DataDistributeDetailLog> dataDistributeDetailLogs = dataDistributeDetailLogMapper.selectByExample(logExample);
                if (dataDistributeDetailLogs.size() > 0) {
                    iterator.remove();
                    redisChgService.unlock(key, lockValue);
                    continue;
                } else {
                    DataDistributeDetailLog distributeLog = new DataDistributeDetailLog();
                    distributeLog.setApiCode(apiCode);
                    distributeLog.setCustNum(next.getCustNum());
                    distributeLog.setCell("");
                    distributeLog.setStatus("1");
                    distributeLog.setpStatus(2);
                    distributeLog.setDistributeDate(distributeDate);
                    distributeLog.setDistributeType(distributeType);
                    distributeLog.setSuccessDate(distributeDate);
                    distributeLog.setCreateTime(new Date());
                    distributeLog.setSourceId(next.getId());
                    distributeLog.setSourceType(DistributeSourceTypeEnum.TRANSFER.getValue());
                    dataDistributeDetailLogMapper.insertSelective(distributeLog);
                }
                redisChgService.unlock(key, lockValue);
            }catch (Exception e){
                redisChgService.unlock(key, lockValue);
                continue;
            }
        }
        long endTime = System.currentTimeMillis();
        log.warn(TITLE+"去重量级{}，去重耗时{}", beforePushSize, (endTime-startTime));
        return pushList;
    }

}
