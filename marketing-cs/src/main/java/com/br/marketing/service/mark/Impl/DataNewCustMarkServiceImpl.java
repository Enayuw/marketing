package com.br.marketing.service.mark.Impl;

import cn.hutool.core.collection.CollectionUtil;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.FlagData;
import com.br.marketing.mapper.FlagDataMapper;
import com.br.marketing.service.mark.DataNewCustMarkService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author peng.kang
 * @description: 与榕树求交打标实现类
 * @date 2025/2/20 14:56
 */
@Service
@Slf4j
public class DataNewCustMarkServiceImpl implements DataNewCustMarkService {
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
            Integer threadPoolSize = marketingCommonConfig.getDataMarkThreadNum();
            ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadPoolSize, threadPoolSize);
            String key = RedisKeyConstant.DATA_RONGSHU_MARK.concat(":").concat(apiCode);
            List<Long> ids = new ArrayList<>();
            while (true) {
                String lockValue = UUID.randomUUID().toString();
                try {
                    redisChgService.lock(key, lockValue);
                } catch (Exception e) {
                    continue;
                }
                try {
                    ThreadPoolAdjustmentUtil.adjustThreadPoolSize(threadPool, marketingCommonConfig.getDataMarkThreadNum());
                    Integer pageSize = marketingCommonConfig.getDataMarkPageSize();
                    //打标表数据查询
                    List<FlagData> list = flagDataMapper.queryFlagNewCustComputation(pageSize, apiCode);
                    if (CollectionUtil.isEmpty(list)) {
                        redisChgService.unlock(key, lockValue);
                        threadPoolShutDown(threadPool);
                        break;
                    }
                    //更新状态:flag_new_cust_computation
                    ids = list.stream().map(FlagData::getId).collect(Collectors.toList());
                    flagDataMapper.batchUpdateFlagNewCustComputationByIds(ids, 0);
                    //释放锁
                    redisChgService.unlock(key, lockValue);
                    //打标更新:flag_new_cust
                    updateFlagNewCust(threadPool, list);
                } catch (Exception e) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.PP_MARKING_SERVICEERROR.getCode(), "pp停车与榕树打标抢锁出现异常，" + "errorMessage=" + e.getMessage()), e);
                    if (!CollectionUtils.isEmpty(ids)) {
                        flagDataMapper.batchUpdateFlagNewCustComputationByIds(ids, null);
                    }
                    redisChgService.unlock(key, lockValue);
                    threadPoolShutDown(threadPool);
                }
            }
            threadPoolShutDown(threadPool);
        });
    }

    void updateFlagNewCust(ThreadPoolExecutor threadPool, List<FlagData> ids) {
        List<List<FlagData>> partitions = Lists.partition(ids, PARTATION_SIZE);
        for (List<FlagData> partition : partitions) {
            threadPool.submit(() -> markAndUpdateFlagNewCust(partition));
        }
    }

    void markAndUpdateFlagNewCust(List<FlagData> list) {
        // 提取原始单元格列表
        List<String> originalCells = list.stream()
                .map(FlagData::getCellLog)
                .collect(Collectors.toList());
        try {
            // Doris 求交查询
            List<String> intersectionCells = flagDataMapper.intersectionWithRongshubI_(originalCells);

            // 更新交集数据状态
            if (CollectionUtils.isNotEmpty(intersectionCells)) {
                updateCellStatus(intersectionCells, 0, 1);
            }

            // 计算非交集数据并更新状态
            List<String> unIntersectionCells = calculateUnIntersectionCells(originalCells, intersectionCells);
            if (CollectionUtils.isNotEmpty(unIntersectionCells)) {
                updateCellStatus(unIntersectionCells, 1, 1);
            }
        } catch (Exception e) {
            // 异常处理
            handleException(originalCells, e);
        }
    }

    /**
     * 计算非交集数据
     */
    private List<String> calculateUnIntersectionCells(List<String> originalCells, List<String> intersectionCells) {
        if (CollectionUtils.isEmpty(intersectionCells)) {
            return originalCells;
        }
        Set<String> intersectionSet = new HashSet<>(intersectionCells);
        return originalCells.parallelStream()
                .filter(cell -> !intersectionSet.contains(cell))
                .collect(Collectors.toList());
    }

    /**
     * 更新单元格状态
     */
    private void updateCellStatus(List<String> cells, Integer status, Integer computationFlag) {
        try {
            flagDataMapper.batchUpdateFlagNewCustComputationByCells(cells, status, computationFlag);
        } catch (Exception e) {
            log.warn("更新单元格状态失败，cells: {}, status: {}, computationFlag: {}", cells, status, computationFlag, e);
        }
    }

    /**
     * 异常处理
     */
    private void handleException(List<String> originalCells, Exception e) {
        String errorMessage = "pp停车&榕树求交打标子线程流程中出现异常，errorMessage=" + e.getMessage();
        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.PP_MARKING_SERVICEERROR.getCode(), errorMessage), e);

        try {
            flagDataMapper.batchUpdateFlagNewCustComputationByCells(originalCells, null, null);
        } catch (Exception ex) {
            log.warn("异常处理中更新单元格状态失败，cells: {}", originalCells, ex);
        }
    }

    private void threadPoolShutDown(ThreadPoolExecutor threadPool) {
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("pp停车与榕树求交线程池关闭");
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.PP_MARKING_SERVICEERROR.getCode(), "pp停车与榕树求交线程作业，日志保存线程池结束异常！errorMessage=" + ex.getMessage()), ex);
            Thread.currentThread().interrupt();
        }
    }
}
