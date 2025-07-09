package com.br.marketing.service.Impl.xc;

import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.entity.XieChengCpsCollidingDataLoopCycle;
import com.br.marketing.entity.XieChengCpsCollidingDataRob;
import com.br.marketing.mapper.XieChengCpsCollidingDataLoopCycleMapper;
import com.br.marketing.mapper.XieChengCpsCollidingDataRobMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 携程CPS异常数据重试服务实现类
 * @Author chenh
 * @Date 2025-06-26
 */
@Service
@Slf4j
public class XieChengCpsExceptionDataRetryServiceImpl implements XieChengCpsExceptionDataRetryService {

    @Autowired
    RedisChgService redisChgService;
    @Autowired
    MarketingCommonConfig marketingCommonConfig;
    @Resource
    XieChengCpsCollidingDataLoopCycleMapper loopCycleMapper;
    @Resource
    XieChengCpsCollidingDataRobMapper robMapper;
    @Resource
    XieChengCpsLoopCycleDataService cycleDataService;
    @Resource
    XieChengCpsRobDataCollidingService robDataCollidingService;
    private final static int PARTITION_SIZE = 50;
    private TpDynamicExecutor threadPool;

    private TpDynamicExecutor getThreadPool(){
        if (threadPool == null) {
            threadPool = TpDynamicExecutorFactory.getThreadPool(ThreadPoolNameEnum.XIECHENG_CPS_RETRY_3710090.getName(), 5, 10);
        }
        return threadPool;
    }

    @Override
    public void process() {
        try {
            // 先撞周期表 再撞非周期表
            processByCycle(getThreadPool());
            processByRob(getThreadPool());
        } catch (Exception e) {
            String subject = "携程CPS异常重试作业异常";
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage(), subject), e);
        }
    }

    /**
     * 处理周期表重试数据
     * @param threadPool 线程池
     */
    private void processByCycle(TpDynamicExecutor threadPool) {
        Long minId = null;
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        while (true) {
            // 分页大小
            Integer pageSize = marketingCommonConfig.getXieChengSmsCollidingDataVtPageSize();

            List<XieChengCpsCollidingDataLoopCycle> list = loopCycleMapper.selectCycleByRetryCount(minId, pageSize);
            if (CollectionUtils.isEmpty(list)) {
                break;
            }

            minId = list.get(list.size() - 1).getId();

            // 异步处理
            List<List<XieChengCpsCollidingDataLoopCycle>> lists = Lists.partition(list, PARTITION_SIZE);
            for (List<XieChengCpsCollidingDataLoopCycle> loopCycles : lists) {
                futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        cycleDataService.pushDataAndHandleResult(loopCycles);
                    } catch (Exception e) {
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                                , "携程CPS周期数据重试撞库，处理异常"), e);
                    }
                }, threadPool));
            }
        }

        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    /**
     * 处理非周期表重试数据
     * @param threadPool 线程池
     */
    private void processByRob(TpDynamicExecutor threadPool) {
        Long minId = null;
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        while (true) {
            // 分页大小
            Integer pageSize = marketingCommonConfig.getXieChengSmsCollidingDataVtPageSize();

            List<XieChengCpsCollidingDataRob> list = robMapper.selectRobByRetryCount(minId, pageSize);
            if (CollectionUtils.isEmpty(list)) {
                break;
            }

            minId = list.get(list.size() - 1).getId();

            // 异步处理
            List<List<XieChengCpsCollidingDataRob>> lists = Lists.partition(list, PARTITION_SIZE);
            for (List<XieChengCpsCollidingDataRob> robs : lists) {
                futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        robDataCollidingService.pushDataAndHandleResult(robs);
                    } catch (Exception e) {
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                                , "携程CPS非周期数据重试撞库，处理异常"), e);
                    }
                }, threadPool));
            }
        }

        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }
}