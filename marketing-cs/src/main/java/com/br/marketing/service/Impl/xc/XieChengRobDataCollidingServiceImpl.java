package com.br.marketing.service.Impl.xc;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.XieChengCollidingDataRob;
import com.br.marketing.mapper.XieChengCollidingDataLoopCycleMapper;
import com.br.marketing.mapper.XieChengCollidingDataRobMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

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


    @Override
    public void collidingData() {
        Integer perMinuteCounts = getPerMinuteCounts();

        Integer todayTrueTotalCounts = xieChengCollidingDataLoopCycleMapper.selectTodayCycleCount();
        //TODO 从广秀提供方法中获取
        Integer totalThreshold = 5000000;
        Integer limit = Math.min(perMinuteCounts, todayTrueTotalCounts);
        ThreadPoolExecutor xiechengRobCollidingThread =
            BrExecutors.getThreadPool(marketingCommonConfig.getXiechengRobCollidingThread(), marketingCommonConfig.getXiechengRobCollidingThread());
        List<XieChengCollidingDataRob> xieChengCollidingDataRobList = xieChengCollidingDataRobMapper.getRobCollidingDataList(limit);

    }

    public Integer getPerMinuteCounts() {
        //TODO 从广秀提供方法获取
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
        perMinuteCounts.forEach((Map<String, String> perMinuteCount) -> redisChgService.hset(today, perMinuteCount.get("releaseTime"), perMinuteCount.get("counts")));
    }
}
