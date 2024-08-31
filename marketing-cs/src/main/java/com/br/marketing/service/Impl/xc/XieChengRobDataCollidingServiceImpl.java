package com.br.marketing.service.Impl.xc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.XieChengCollidingDataPackage;
import com.br.marketing.entity.XieChengCollidingDataPackageExample;
import com.br.marketing.entity.XiechengCollidingDataPackageRuleExample;
import com.br.marketing.mapper.XieChengCollidingDataPackageMapper;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.xiecheng.XieChengServiceNew;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.XieChengCollidingDataRob;
import com.br.marketing.entity.XieChengCollidingDataRobExample;
import com.br.marketing.entity.XiechengCollidingDataPackageRule;
import com.br.marketing.mapper.XieChengCollidingDataLoopCycleMapper;
import com.br.marketing.mapper.XieChengCollidingDataRobMapper;
import com.br.marketing.mapper.XiechengCollidingDataPackageRuleMapper;
import com.br.marketing.service.Impl.VariableAllocationServiceImpl;
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
    @Resource
    private VariableAllocationServiceImpl variableAllocationService;
    @Resource
    private XiechengCollidingDataPackageRuleMapper packageRuleMapper;
    @Resource
    private XieChengCollidingDataLoopCycleMapper loopCycleMapper;
    @Resource
    private XcLoopCycleDataService xcLoopCycleDataService;
    @Resource
    private XieChengCollidingDataPackageMapper packageMapper;
    private static final Integer RESETPARTITION = 10000;


    @Override
    public void collidingData() {
        // 周期积压量级
        Date startDate = Date.from(LocalDate.now().minusDays(1).atTime(23, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = new Date();
        if (loopCycleMapper.selectCycleCountOfStack(startDate, endDate) >= 200000) {
            return;
        }

        Integer perMinuteCounts = getPerMinuteCounts();
        Integer todayTrueTotalCounts = xieChengCollidingDataLoopCycleMapper.selectTodayCycleCount();
        Integer totalThreshold = variableAllocationService.getVariableAllocation().getNormalQuantity();
        int limit = Math.min(perMinuteCounts, totalThreshold - todayTrueTotalCounts);
        // 强制开关开启强制撞库，强制开关关闭且条件开关打开开始撞库
        while (limit > 0 && (marketingCommonConfig.getXieChengForceOpenSwitch()
            || Objects.equals("true", redisChgService.get(RedisKeyConstant.XIECHENG_CONDITIONSWITCH)))) {
            XIECHENG_ROB_COLLIDING_THREAD.setCorePoolSize(marketingCommonConfig.getXiechengRobCollidingThread());
            XIECHENG_ROB_COLLIDING_THREAD.setMaximumPoolSize(marketingCommonConfig.getXiechengRobCollidingThread());
            int pageSize = Math.min(marketingCommonConfig.getXiechengCollidingPageSize(), limit);
            // 获取当前待执行规则
            XiechengCollidingDataPackageRule packageRule = getCurrentPackageRule();
            // 没有待执行的规则直接跳出
            if (Objects.isNull(packageRule)) {
                break;
            }
            List<XieChengCollidingDataRob> robDataList = xieChengCollidingDataRobMapper.getRobCollidingDataList(pageSize, packageRule.getId(),
                packageRule.getPackageId(), packageRule.getCollidingTimes());
            if (CollectionUtils.isEmpty(robDataList)) {
                break;
            }
            List<List<XieChengCollidingDataRob>> xieChengCollidingDataListPartition = Lists.partition(robDataList, 50);
            List<CompletableFuture<Void>> futures = Lists.newArrayList();
            xieChengCollidingDataListPartition.forEach((List<XieChengCollidingDataRob> robData) -> {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> pushDataAndHandleResult(robData), XIECHENG_ROB_COLLIDING_THREAD);
                futures.add(future);
            });
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            limit -= robDataList.size();
        }
    }

    private XiechengCollidingDataPackageRule getCurrentPackageRule() {
        List<XiechengCollidingDataPackageRule> collidingDataPackageRules = packageRuleMapper.getCollidingPackageRules();
        for (XiechengCollidingDataPackageRule packageRule : collidingDataPackageRules) {
            if (!checkPackageRule(packageRule)) {
                return packageRule;
            }
        }
        return null;
    }

    private Boolean checkPackageRule(XiechengCollidingDataPackageRule packageRule) {
        Integer collidingBackNumber = packageRule.getCollidingBackNumber();
        // 根据撞库次数获取待撞量级
        int count = xieChengCollidingDataRobMapper.countByCollidingCount(packageRule.getPackageId(), packageRule.getCollidingTimes());
        if (collidingBackNumber == null) {
            // 如果不需要判断撞得量级，则只需判断是否有满足撞库次数的记录
            return count == 0;
        }

        // 查询撞得量级
        XieChengCollidingDataRobExample example = new XieChengCollidingDataRobExample();
        example.createCriteria().andPackageRuleIdEqualTo(packageRule.getId()).andDataSourceTypeEqualTo("F").andIsDeleteEqualTo(1)
            .andPushTimeGreaterThanOrEqualTo(DateUtil.beginOfDay(new Date()));
        int packageTrueCount = xieChengCollidingDataRobMapper.countByExample(example);
        // 判断是否满足撞得量级和撞库次数的条件
        return packageTrueCount >= collidingBackNumber || count == 0;
    }

    /**
     * 推送非周期撞库数据
     *
     * @param robData rob数据
     * @author senyang.zheng
     * @date 2024/03/21
     */
    @Override
    public void pushDataAndHandleResult(List<XieChengCollidingDataRob> robData) {
        Map<String, XieChengCollidingDataRob> cellMap = robData.stream()
            .collect(Collectors.toMap(XieChengCollidingDataRob::getCellSha256CodeList, rob -> rob, (existing, replacement) -> replacement));
        List<String> sha256Codes = robData.stream().map(XieChengCollidingDataRob::getCellSha256CodeList).collect(Collectors.toList());

        List<String> cells = xcLoopCycleDataService.excludeData(sha256Codes,"F");
        if (CollectionUtils.isEmpty(cells)) {
            return;
        }

        try {
            Result collidingResult = xieChengServiceNew.pushXieChengSmsCollidingDataNew(cells);
            handleService.robDataHandle(collidingResult, cellMap);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                    , "携程非周期撞库异常"), e);
        }

    }

    public Integer getPerMinuteCounts() {
        Integer threshold = marketingCommonConfig.getXiechengPerMinuteThreshold();
        String today = DateUtil.today();
        String key = RedisKeyConstant.XIECHENG_RELEASE_TIME + today;
        Long size = redisChgService.hlen(key);
        if (size.equals(0L)) {
            initializeTodayReleaseTime(key);
        }
        String minute = DateUtil.format(LocalDateTime.now(), DatePattern.NORM_DATETIME_MINUTE_PATTERN);
        String perMinuteCounts = redisChgService.hget(key, minute) == null ? "0" : redisChgService.hget(key, minute);
        return threshold - Integer.parseInt(perMinuteCounts);
    }

    public void initializeTodayReleaseTime(String key) {
        List<Map<String, Object>> perMinuteCounts = xieChengCollidingDataLoopCycleMapper.selectPerMinuteCountstiflash_();
        // 初始化剔除当天和昨天的key
        redisChgService.del(key);
        String yesKey = RedisKeyConstant.XIECHENG_RELEASE_TIME + DateUtil.formatDate(DateUtil.yesterday());
        redisChgService.del(yesKey);
        perMinuteCounts.forEach((Map<String, Object> perMinuteCount) -> redisChgService.hset(key, String.valueOf(perMinuteCount.get("releaseTime")),
            String.valueOf(perMinuteCount.get("counts"))));
    }

    @Override
    public void resetCollidingCount() {
        XieChengCollidingDataPackageExample packageExample = new XieChengCollidingDataPackageExample();
        packageExample.createCriteria().andIsDeleteEqualTo(0);
        List<XieChengCollidingDataPackage> packages = packageMapper.selectByExample(packageExample);

        List<XieChengCollidingDataPackage> validPackages = packages.stream().filter(this::checkPackageValid).collect(Collectors.toList());
        // 按轮次分组
        Map<Integer, List<XieChengCollidingDataPackage>> roundPackageMap =
                validPackages.stream().collect(Collectors.groupingBy(XieChengCollidingDataPackage::getRound));

        // 不开启轮次的撞库包
        List<XieChengCollidingDataPackage> nonRoundPackages = roundPackageMap.get(0);

        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(20, 20);
        try {
            resetCollidingCountByPackages(nonRoundPackages, threadPool);

            // 开启轮次的撞库包
            List<XieChengCollidingDataPackage> roundPackages = roundPackageMap.get(1);
            if (CollectionUtils.isEmpty(roundPackages)) {
                return;
            }

            // 有撞库次数=0的数据，不重置撞库次数
            Long zeroCount = xieChengCollidingDataRobMapper.selectCountByRoundPackages(roundPackages);
            if (zeroCount > 0) {
                return;
            }

            resetCollidingCountByPackages(roundPackages, threadPool);
        } finally {
            threadPoolShutDown(threadPool);
        }
    }

    /**
     * 根据撞库包查询非周期数据id，根据id将撞库次数置为0
     * @param packages
     * @param threadPool
     */
    private void resetCollidingCountByPackages(List<XieChengCollidingDataPackage> packages, ThreadPoolExecutor threadPool) {
        if (CollectionUtils.isEmpty(packages)) {
            return;
        }

        Long minId = null;
        while (true) {
            List<Long> list = xieChengCollidingDataRobMapper.selectRobsByNonRoundPackages(minId, packages);
            if (CollectionUtils.isEmpty(list)) {
                break;
            }

            minId = list.get(list.size() - 1);

            List<List<Long>> partition = Lists.partition(list, RESETPARTITION);
            for (List<Long> robList : partition) {
                threadPool.submit(() -> {
                    try {
                        xieChengCollidingDataRobMapper.batchResetCollidingCountByIds(robList);
                    } catch (Exception e) {
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                                , "携程重置撞库次数作业，子线程处理异常"), e);
                    }
                });
            }
        }
    }

    private void threadPoolShutDown(ThreadPoolExecutor threadPool) {
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("携程重置撞库次数作业线程池关闭");
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), ex.getMessage()
                    , "携程重置撞库次数作业，日志保存线程池结束异常"), ex);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 根据撞库规则的撞库开始和结束时间获取有效撞库包
     * @param dataPackage
     * @return
     */
    private boolean checkPackageValid(XieChengCollidingDataPackage dataPackage) {
        XiechengCollidingDataPackageRuleExample packageRuleExample = new XiechengCollidingDataPackageRuleExample();
        packageRuleExample.createCriteria().andIsDeleteEqualTo(0).andPackageIdEqualTo(dataPackage.getId())
                .andCollidingStartTimeLessThanOrEqualTo(new Date()).andCollidingEndTimeGreaterThanOrEqualTo(new Date());
        List<XiechengCollidingDataPackageRule> packageRules = packageRuleMapper.selectByExample(packageRuleExample);
        if (CollectionUtils.isEmpty(packageRules)) {
            return false;
        }

        return true;
    }
}
