package com.br.marketing.service.Impl.xc;

import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.XieChengCollidingDataLoopCycle;
import com.br.marketing.mapper.XieChengCollidingDataLoopCycleMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.TimeUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

/**
 * @Description 携程异常重试作业实现类
 * @Author hong.chen
 * @CreateTime 2024/03/20
 */
@Service
@Slf4j
public class XcExceptionDataRetryServiceImpl implements XcExceptionDataRetryService {
    @Autowired
    MarketingCommonConfig marketingCommonConfig;
    @Resource
    XieChengCollidingDataLoopCycleMapper loopCycleMapper;
    @Resource
    XcLoopCycleDataService cycleDataService;

    @Resource
    XieChengRobDataCollidingService robDataCollidingService;

    @Resource
    private AlarmApiClient alarmClient;

    private final static int PARTATIONNUM = 50;

    @Override
    public void process() {
        // 创建线程池
        ThreadPoolExecutor threadPool =
                BrExecutors.getThreadPool(marketingCommonConfig.getXieChengSmsCollidingRetryThread(),
                        marketingCommonConfig.getXieChengSmsCollidingRetryThread());

        // 先撞重试次数1和2的数据
        // 先撞TRUE数据表
        // 再撞FALSE数据表
        Map<String, BiConsumer<List<XieChengCollidingDataLoopCycle>, AtomicInteger>> map = new HashMap<>();
        map.put("loop_cycle", cycleDataService::pushDataAndHandleResult);
        // todo
//        map.put("rob", robDataCollidingService::pushRobCollidingData);

        for (Map.Entry<String, BiConsumer<List<XieChengCollidingDataLoopCycle>, AtomicInteger>> entry : map.entrySet()) {
            process(entry, threadPool, Boolean.FALSE);
        }

        // 再撞重试次数3的数据
        if (isLastTime()) {
            AtomicInteger failNum = new AtomicInteger(0);
            for (Map.Entry<String, BiConsumer<List<XieChengCollidingDataLoopCycle>, AtomicInteger>> entry : map.entrySet()) {
                failNum = process(entry, threadPool, Boolean.TRUE);
            }

            // 统计撞库异常数据发系统告警
            sendAlarm(failNum, "携程短信撞库接口重试推送异常，需要关注！！！");
        }

        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("携程撞库线程池关闭");
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            log.error("日志保存线程池结束异常！", ex);
            Thread.currentThread().interrupt();
        }
    }

    private AtomicInteger process(Map.Entry<String, BiConsumer<List<XieChengCollidingDataLoopCycle>, AtomicInteger>> entry,
                                  ThreadPoolExecutor threadPool, Boolean isLast) {
        AtomicInteger failNum = new AtomicInteger(0);
        Long minId = null;
        while (true) {
            // 判断强制开启撞库开关
            Boolean forceOpenSwitch = marketingCommonConfig.getXieChengForceOpenSwitch();
            // todo 广绣提供
            Boolean conditionSwitch = Boolean.TRUE;

            if (forceOpenSwitch || conditionSwitch) {
                List<XieChengCollidingDataLoopCycle> dataList;
                if (isLast) {
                    dataList = loopCycleMapper.selectByRetryCountOfThreeTimes(minId, entry.getKey());
                } else {
                    dataList = loopCycleMapper.selectByRetryCountOfOnceAndTwice(minId, entry.getKey());
                }

                if (CollectionUtils.isEmpty(dataList)) {
                    break;
                }
                minId = dataList.get(dataList.size() - 1).getId();

                // 分组
                List<List<XieChengCollidingDataLoopCycle>> partitions = Lists.partition(dataList, PARTATIONNUM);
                for (List<XieChengCollidingDataLoopCycle> partition : partitions) {
                    threadPool.submit(() -> entry.getValue().accept(partition, failNum));
                }
            }
        }

        return failNum;
    }

    /**
     * 当前时间在23:30:00~23:59:59或02:00:00~02:30:00
     * @return
     */
    private boolean isLastTime() {
        return TimeUtils.timeCompare(
                marketingCommonConfig.getXieChengSmsCollidingRetryWarnAllTime().get(0),
                marketingCommonConfig.getXieChengSmsCollidingRetryWarnAllTime().get(1)
        )
                ||
                TimeUtils.timeCompare(
                        marketingCommonConfig.getXieChengSmsCollidingRetryWarnAllTime().get(2),
                        marketingCommonConfig.getXieChengSmsCollidingRetryWarnAllTime().get(3));
    }

    private void sendAlarm(AtomicInteger failNum, String title) {
        if (failNum.get() > 0) {
            try {
                alarmClient.sendAlarm("推送失败条数=" + failNum.get(), title, AlarmSendCodeEnum.EXCEPTION_URGENT.getCode());
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }
    }
}
