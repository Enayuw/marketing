package com.br.marketing.service.Impl.xc;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
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

    @Override
    public void collidingData() {
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
            if (checkPackageRule(packageRule)) {
                continue;
            }
            return packageRule;
        }
        return null;
    }

    private Boolean checkPackageRule(XiechengCollidingDataPackageRule packageRule) {
        Integer collidingBackNumber = packageRule.getCollidingBackNumber();
        // 查询不够撞库次数的量级
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
        try {
            Result collidingResult = xieChengServiceNew.pushXieChengSmsCollidingDataNew(sha256Codes);
            handleService.robDataHandle(collidingResult, cellMap);
        } catch (Exception e) {
            log.error("携程非周期撞库异常，sha256Codes:{}", sha256Codes, e);
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
        xieChengCollidingDataRobMapper.batchRsetCollidingCount();
    }
}
