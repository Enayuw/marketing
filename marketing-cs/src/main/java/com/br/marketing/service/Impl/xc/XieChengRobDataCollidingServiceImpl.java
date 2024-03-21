package com.br.marketing.service.Impl.xc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.xiecheng.XieChengService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.XieChengCollidingDataRob;
import com.br.marketing.mapper.XieChengCollidingDataLoopCycleMapper;
import com.br.marketing.mapper.XieChengCollidingDataRobMapper;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 携程非周期数据撞库相关Service实现
 *
 * @author senyang.zheng
 * @date 2024/03/19
 */
@Service
@Slf4j
public class XieChengRobDataCollidingServiceImpl implements XieChengRobDataCollidingService {

    @Resource
    private RedisChgService redisChgService;
    @Resource
    private XieChengCollidingDataLoopCycleMapper xieChengCollidingDataLoopCycleMapper;
    @Resource
    private XieChengCollidingDataRobMapper xieChengCollidingDataRobMapper;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private XieChengService xieChengService;
    @Resource
    private RabbitMqProducter rabbitMqProducter;
    @Resource
    private XieChengCollidingResultHandleService handleService;

    @Override
    public void collidingData() {
        Integer perMinuteCounts = getPerMinuteCounts();
        Integer todayTrueTotalCounts = xieChengCollidingDataLoopCycleMapper.selectTodayCycleCount();
        // TODO 从广秀提供方法中获取
        Integer totalThreshold = 5000000;
        Integer limit = Math.min(perMinuteCounts, todayTrueTotalCounts);
        // TODO 从陈宏配置字段取
        Integer decrement = 20000;
        while (limit > 0) {
            ThreadPoolExecutor xiechengRobCollidingThread = BrExecutors.getThreadPool(marketingCommonConfig.getXiechengRobCollidingThread(),
                marketingCommonConfig.getXiechengRobCollidingThread());
            List<XieChengCollidingDataRob> robDataList = xieChengCollidingDataRobMapper.getRobCollidingDataList(limit);
            List<List<XieChengCollidingDataRob>> xieChengCollidingDataListPartition = Lists.partition(robDataList, 50);
            xieChengCollidingDataListPartition.forEach((List<XieChengCollidingDataRob> robData) -> CompletableFuture
                .runAsync(() -> pushRobCollidingData(robData, new AtomicInteger(0)), xiechengRobCollidingThread));
            limit -= decrement;
        }

    }

    /**
     * 推送非周期撞库数据
     *
     * @param robData rob数据
     * @param failNum fail num
     * @author senyang.zheng
     * @date 2024/03/21
     */
    @Override
    public void pushRobCollidingData(List<XieChengCollidingDataRob> robData, AtomicInteger failNum) {
        Map<String, XieChengCollidingDataRob> cellMap = robData.stream()
            .collect(Collectors.toMap(XieChengCollidingDataRob::getCellSha256CodeList, rob -> rob, (existing, replacement) -> replacement));
        List<String> sha256Codes = robData.stream().map(XieChengCollidingDataRob::getCellSha256CodeList).collect(Collectors.toList());
        Result collidingResult = xieChengService.pushXieChengSmsCollidingDataNew(sha256Codes);
        handleService.robDataHandle(collidingResult, cellMap, failNum);
    }

    public Integer getPerMinuteCounts() {
        // TODO 从广秀提供方法获取
        Integer threshold = 100000;
        String today = DateUtil.today();
        Long size = redisChgService.hlen(today);
        if (size.equals(0L)) {
            initializeTodayReleaseTime(today);
        }
        String minute = DateUtil.format(LocalDateTime.now(), DatePattern.NORM_DATETIME_MINUTE_PATTERN);
        return Integer.valueOf(redisChgService.hget(today, minute));
    }

    private void initializeTodayReleaseTime(String today) {
        List<Map<String, String>> perMinuteCounts = xieChengCollidingDataLoopCycleMapper.selectPerMinuteCounts();
        perMinuteCounts.forEach(
            (Map<String, String> perMinuteCount) -> redisChgService.hset(today, perMinuteCount.get("releaseTime"), perMinuteCount.get("counts")));
    }
}
