package com.br.marketing.service.Impl.xc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.xiecheng.XieChengServiceNew;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.XieChengCollidingDataRob;
import com.br.marketing.mapper.XieChengCollidingDataLoopCycleMapper;
import com.br.marketing.mapper.XieChengCollidingDataRobMapper;
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

    public static final ThreadPoolExecutor XIECHENG_ROB_COLLIDING_THREAD = BrExecutors.getThreadPool(20, 20);

    @Resource
    private RedisChgService redisChgService;
    @Resource
    private XieChengCollidingDataLoopCycleMapper xieChengCollidingDataLoopCycleMapper;
    @Resource
    private XieChengCollidingDataRobMapper xieChengCollidingDataRobMapper;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private XieChengServiceNew xieChengServiceNew;
    @Resource
    private XieChengCollidingResultHandleService handleService;

    @Override
    public void collidingData(List<Long> packageIds) {
        Integer perMinuteCounts = getPerMinuteCounts();
        Integer todayTrueTotalCounts = xieChengCollidingDataLoopCycleMapper.selectTodayCycleCount();
        // TODO 从广秀提供方法中获取
        Integer totalThreshold = 5000000;
        Integer limit = Math.min(perMinuteCounts, totalThreshold - todayTrueTotalCounts);
        Integer pageSize = marketingCommonConfig.getXiechengCollidingPageSize();
        // 强制开关开启强制撞库，强制开关关闭且条件开关打开开始撞库
        while (limit > 0 && (marketingCommonConfig.getXieChengForceOpenSwitch()
            || Objects.equals("true", redisChgService.get(RedisKeyConstant.XIECHENG_CONDITIONSWITCH)))) {
            XIECHENG_ROB_COLLIDING_THREAD.setCorePoolSize(marketingCommonConfig.getXiechengRobCollidingThread());
            XIECHENG_ROB_COLLIDING_THREAD.setMaximumPoolSize(marketingCommonConfig.getXiechengRobCollidingThread());
            List<XieChengCollidingDataRob> robDataList = xieChengCollidingDataRobMapper.getRobCollidingDataList(pageSize, packageIds);
            if (CollectionUtils.isEmpty(robDataList)) {
                break;
            }
            List<List<XieChengCollidingDataRob>> xieChengCollidingDataListPartition = Lists.partition(robDataList, 50);
            List<CompletableFuture<Void>> futures = Lists.newArrayList();
            xieChengCollidingDataListPartition.forEach((List<XieChengCollidingDataRob> robData) -> {
                CompletableFuture<Void> future =
                    CompletableFuture.runAsync(() -> pushDataAndHandleResult(robData), XIECHENG_ROB_COLLIDING_THREAD);
                futures.add(future);
            });
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            limit -= pageSize;
        }

    }

    /**
     * 推送非周期撞库数据
     *
     * @param robData rob数据
     * @param failNum failNum
     * @author senyang.zheng
     * @date 2024/03/21
     */
    @Override
    public void pushDataAndHandleResult(List<XieChengCollidingDataRob> robData) {
        Map<String, XieChengCollidingDataRob> cellMap = robData.stream()
            .collect(Collectors.toMap(XieChengCollidingDataRob::getCellSha256CodeList, rob -> rob, (existing, replacement) -> replacement));
        List<String> sha256Codes = robData.stream().map(XieChengCollidingDataRob::getCellSha256CodeList).collect(Collectors.toList());
        try {
            Result collidingResult = xieChengServiceNew.pushXieChengSmsCollidingDataNew(sha256Codes);
            handleService.robDataHandle(collidingResult, cellMap);
        } catch (Exception e) {
            log.error("携程非周期撞库异常，sha256Codes:{}", sha256Codes, e);
        }

    }

    public Integer getPerMinuteCounts() {
        // TODO 从广秀提供方法获取
        Integer threshold = marketingCommonConfig.getXiechengPerMinuteThreshold();
        String today = DateUtil.today();
        Long size = redisChgService.hlen(today);
        if (size.equals(0L)) {
            initializeTodayReleaseTime(today);
        }
        String minute = DateUtil.format(LocalDateTime.now(), DatePattern.NORM_DATETIME_MINUTE_PATTERN);
        String perMinuteCounts = redisChgService.hget(today, minute) == null ? "0" : redisChgService.hget(today, minute);
        return threshold - Integer.parseInt(perMinuteCounts);
    }

    private void initializeTodayReleaseTime(String today) {
        List<Map<String, String>> perMinuteCounts = xieChengCollidingDataLoopCycleMapper.selectPerMinuteCounts();
        perMinuteCounts.forEach(
            (Map<String, String> perMinuteCount) -> redisChgService.hset(today, perMinuteCount.get("releaseTime"), perMinuteCount.get("counts")));
    }
}
