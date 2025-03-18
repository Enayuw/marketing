package com.br.marketing.service.mark.Impl;

import cn.hutool.core.collection.CollectionUtil;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.FlagData;
import com.br.marketing.mapper.FlagDataMapper;
import com.br.marketing.service.mark.DataBlackListMarkService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author peng.kang
 * @description: pp停车-与外呼黑名单打标
 * @date 2025/2/21 10:16
 */
@Service
@Slf4j
public class DataBlackListMarkServiceImpl implements DataBlackListMarkService {
    private final static int PARTATION_SIZE = 2000;
    @Resource
    FlagDataMapper flagDataMapper;
    @Autowired
    RedisChgService redisChgService;
    @Resource
    MarketingCommonConfig marketingCommonConfig;

    @Override
    public void process() {
        marketingCommonConfig.getDataMarkApiCodes().forEach((String apiCode) -> {
            Map<String, Integer> blackListOutput = marketingCommonConfig.getPpCarBlackList();
            Integer pageSize = marketingCommonConfig.getDataMarkPageSize();
            Integer threadPoolSize = marketingCommonConfig.getDataMarkThreadNum();
            ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadPoolSize, threadPoolSize);
            String key = RedisKeyConstant.DATA_BLACKLIST_MARK.concat(":").concat(apiCode);
            List<Long> ids = new ArrayList<>();
            while (true) {
                String lockValue = UUID.randomUUID().toString();
                try {
                    redisChgService.lock(key, lockValue);
                } catch (Exception e) {
                    continue;
                }
                try {
                    //打标表数据查询
                    List<FlagData> list = flagDataMapper.queryFlagBlackListComputation(pageSize, apiCode);
                    if (CollectionUtil.isEmpty(list)) {
                        redisChgService.unlock(key, lockValue);
                        threadPoolShutDown(threadPool);
                        break;
                    }
                    //更新状态:flag_blacklist_computation
                    ids = list.stream().map(FlagData::getId).collect(Collectors.toList());
                    flagDataMapper.batchUpdateFlagBlackListComputationByIds(ids, 0);
                    //释放锁
                    redisChgService.unlock(key, lockValue);

                    //打标更新:flag_intellaudio_blacklist
                    updateFlagIntellaudioBlacklist(threadPool, list, blackListOutput);
                } catch (Exception e) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.PP_MARKING_SERVICEERROR.getCode(),
                            "pp停车与黑名单打标抢锁出现异常，" + "errorMessage=" + e.getMessage()), e);
                    if (!CollectionUtils.isEmpty(ids)) {
                        flagDataMapper.batchUpdateFlagBlackListComputationByIds(ids, null);
                    }
                    redisChgService.unlock(key, lockValue);
                    threadPoolShutDown(threadPool);
                }
            }
            threadPoolShutDown(threadPool);
        });
    }

    void updateFlagIntellaudioBlacklist(ThreadPoolExecutor threadPool, List<FlagData> ids, Map<String, Integer> blackListOutput) {
        List<List<FlagData>> partitions = Lists.partition(ids, PARTATION_SIZE);
        for (List<FlagData> partition : partitions) {
            threadPool.submit(() -> markAndUpdateBlacklist(partition, blackListOutput));
        }
    }

    void markAndUpdateBlacklist(List<FlagData> list, Map<String, Integer> blackListOutput) {
        List<String> originalCells = list.stream().map(FlagData::getCellMd5).collect(Collectors.toList());
        List<String> unIntersectionCells;
        try {
            //求交查询(外呼黑名单)
            List<String> intersectionCells = flagDataMapper.intersectionWithBlackList(originalCells, blackListOutput.get("type"));
            if (CollectionUtil.isNotEmpty(intersectionCells)) {
                flagDataMapper.batchUpdateFlagBlackListComputationByCells(intersectionCells, blackListOutput.get("flagIntellaudioBlacklist"), 1);
            }
            unIntersectionCells = originalCells.stream().filter(a -> !intersectionCells.contains(a)).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(unIntersectionCells)) {
                flagDataMapper.batchUpdateFlagBlackListComputationByCells(unIntersectionCells, 0, 1);
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.PP_MARKING_SERVICEERROR.getCode(),
                    "pp停车&黑名单打标子线程流程中出现异常，" + "errorMessage=" + e.getMessage()), e);
            flagDataMapper.batchUpdateFlagBlackListComputationByCells(originalCells, null, null);
        }


    }

    private void threadPoolShutDown(ThreadPoolExecutor threadPool) {
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("pp停车与外呼黑名单打标线程池关闭");
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.PP_MARKING_SERVICEERROR.getCode(),
                    "pp停车与黑名单打标线程作业，日志保存线程池结束异常！errorMessage=" + ex.getMessage()), ex);
            Thread.currentThread().interrupt();
        }
    }
}
