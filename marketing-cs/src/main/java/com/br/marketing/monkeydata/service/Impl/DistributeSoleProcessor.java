package com.br.marketing.monkeydata.service.Impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.bo.ZhonganRosterLockingDataBO;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.DistributeSourceTypeEnum;
import com.br.marketing.common.enums.DistributeTypeEnum;
import com.br.marketing.entity.DataDistributeDetailLog;
import com.br.marketing.entity.ZhonganRosterLockingData;
import com.br.marketing.mapper.DataDistributeDetailLogMapper;

import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DistributeSoleProcessor {

    @Resource
    RedisChgService redisChgService;

    @Resource
    DataDistributeDetailLogMapper dataDistributeDetailLogMapper;

    public List<Long> process(List<ZhonganRosterLockingDataBO> pushList, ZhonganRosterLockingData pageParam) {
        List<Long> notPushIds = new ArrayList<>();
        String key = RedisKeyConstant.PUSH_ZHONGAN_DISTRIBUTE_DATA_SLOE_LOCK;
        Integer distributeType = DistributeTypeEnum.ZHONGAN_PUSH_DETAIL.getValue();
        Integer soleDay = 1;
        String userType = pageParam.getUserType();

        Iterator<ZhonganRosterLockingDataBO> iterator = pushList.iterator();
        long startTime = System.currentTimeMillis();
        while (iterator.hasNext()) {
            ZhonganRosterLockingDataBO next = iterator.next();
            String apiCode = next.getApiCode();
            String cell = next.getSyncUser().getCell();
            String tag = next.getTag();
            key = key.concat(String.format(":%d:%d:%s:%s", distributeType, soleDay, apiCode, cell));
            String lockValue = UUID.randomUUID().toString();
            try {
                redisChgService.lock(key, lockValue);
                String distributeDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                List<Long> ids =
                    dataDistributeDetailLogMapper.findZhongAnLockingDataDistributeLog(apiCode, distributeType, distributeDate, cell, userType, tag);
                if (CollectionUtil.isNotEmpty(ids)) {
                    iterator.remove();
                    notPushIds.add(next.getData().getId());
                    redisChgService.unlock(key, lockValue);
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
                    distributeLog.setSourceType(DistributeSourceTypeEnum.ZHONGAN_LOCKING_DATA.getValue());
                    JSONObject extend = new JSONObject();
                    extend.put("userType", userType);
                    extend.put("tag", tag);
                    distributeLog.setExtend(JSONObject.toJSONString(extend));
                    dataDistributeDetailLogMapper.insertSelective(distributeLog);
                }
                redisChgService.unlock(key, lockValue);
            } catch (Exception e) {
                redisChgService.unlock(key, lockValue);
            }
        }
        long endTime = System.currentTimeMillis();
        log.warn("推送众安去重一次的耗时：" + (endTime - startTime));
        return notPushIds;
    }

}
