package com.br.marketing.monkeydata.service.Impl;

import com.br.marketing.bo.ZhonganRosterLockingDataBO;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.entity.DataDistributeDetailLog;
import com.br.marketing.entity.DataDistributeDetailLogExample;
import com.br.marketing.mapper.DataDistributeDetailLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class DistributeSoleProcessor {

    @Resource
    RedisChgService redisChgService;

    @Resource
    DataDistributeDetailLogMapper dataDistributeDetailLogMapper;

    public List<Long> process(List<ZhonganRosterLockingDataBO> pushList){
        List<Long> notPushIds = new ArrayList<>();
        String key = RedisKeyConstant.dributeDataSloeLock;
        Integer distributeType = 2;
        Integer soleDay = 1;

        Iterator<ZhonganRosterLockingDataBO> iterator = pushList.iterator();
        long startTime = System.currentTimeMillis();
        while(iterator.hasNext()){
            ZhonganRosterLockingDataBO next = iterator.next();
            String apiCode = next.getApiCode();
            String cell = next.getSyncUser().getCell();
            key = key.concat(String.format(":%d:%d:%s:%s", distributeType
                    , soleDay, apiCode, cell));
            UUID uuid = UUID.randomUUID();
            try {
                redisChgService.lock(key, uuid.toString());
                DataDistributeDetailLogExample logExample = new DataDistributeDetailLogExample();
                logExample.setOrderByClause(" id limit 1 ");
                DataDistributeDetailLogExample.Criteria criteria = logExample.createCriteria().andApiCodeEqualTo(apiCode)
                        .andDistributeTypeEqualTo(distributeType);
                        String distributeDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        criteria.andDistributeDateEqualTo(distributeDate);
                    criteria.andCellEqualTo(cell);
                List<DataDistributeDetailLog> dataDistributeDetailLogs = dataDistributeDetailLogMapper.selectByExample(logExample);
                if (dataDistributeDetailLogs.size() > 0) {
                    iterator.remove();
                    notPushIds.add(next.getData().getId());
                    redisChgService.unlock(key, uuid.toString());
                    continue;
                } else {
                    DataDistributeDetailLog distributeLog = new DataDistributeDetailLog();
                    distributeLog.setApiCode(apiCode);
                    distributeLog.setCustNum(next.getSyncUser().getCustNum());
                    distributeLog.setCell(next.getSyncUser().getCell());
                    distributeLog.setStatus("1");
                    distributeLog.setpStatus(2);
                    distributeLog.setDistributeDate(distributeDate);
                    distributeLog.setDistributeType(distributeType);
                    distributeLog.setSuccessDate(distributeDate);
                    distributeLog.setCreateTime(new Date());
                    distributeLog.setSourceId(next.getSyncUser().getId());
                    distributeLog.setSourceType("1");
                    // distributeLog.setExtend("");
                    dataDistributeDetailLogMapper.insertSelective(distributeLog);
                }
                redisChgService.unlock(key, uuid.toString());
            }catch (Exception e){
                redisChgService.unlock(key, uuid.toString());
                continue;
            }
        }
        long endTime = System.currentTimeMillis();
        log.warn("推送众安去重一次的耗时："+(endTime-startTime));
        return notPushIds;
    }

}
