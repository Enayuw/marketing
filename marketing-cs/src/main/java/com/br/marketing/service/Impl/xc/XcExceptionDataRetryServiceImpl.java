package com.br.marketing.service.Impl.xc;

import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.XieChengCollidingDataLogMapper;
import com.br.marketing.mapper.XieChengCollidingDataLoopCycleMapper;
import com.br.marketing.mapper.XieChengCollidingDataRobMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.TimeUtils;
import com.br.marketing.webhook.dingding.msgtype.DingDingMarkdownMessage;
import com.br.marketing.webhook.dingding.service.DingDingRobotHookService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * @Description 携程异常重试作业实现类
 * @Author hong.chen
 * @CreateTime 2024/03/20
 */
@Service
@Slf4j
public class XcExceptionDataRetryServiceImpl implements XcExceptionDataRetryService {
    @Autowired
    RedisChgService redisChgService;
    @Autowired
    MarketingCommonConfig marketingCommonConfig;
    @Resource
    XieChengCollidingDataLoopCycleMapper loopCycleMapper;

    @Resource
    XieChengCollidingDataRobMapper robMapper;
    @Resource
    XcLoopCycleDataService cycleDataService;

    @Resource
    XieChengCollidingDataLogMapper logMapper;

    @Resource
    XieChengRobDataCollidingService robDataCollidingService;
    @Resource
    private DingDingRobotHookService dingDingRobotHookService;

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
        // todo
        Map<String, BiConsumer<List<?>, AtomicInteger>> map = new HashMap<>();
        map.put("loop_cycle", (list, failNum) -> cycleDataService.pushDataAndHandleResult((List<XieChengCollidingDataLoopCycle>) list, failNum));
        map.put("rob", (list, failNum) -> robDataCollidingService.pushDataAndHandleResult((List<XieChengCollidingDataRob>) list, failNum));
        for (Map.Entry<String, BiConsumer<List<?>, AtomicInteger>> entry : map.entrySet()) {
            process(entry, threadPool, Boolean.FALSE);
        }

        // 再撞重试次数3的数据
        if (isLastTime()) {
            AtomicInteger failNum = new AtomicInteger(0);
            for (Map.Entry<String, BiConsumer<List<?>, AtomicInteger>> entry : map.entrySet()) {
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

    @Override
    public void conditonProcess() {
        // 判断是否需要打开条件开关
        // 查询堆积量级是否超限（10w）
        XieChengCollidingDataLoopCycleExample loopCycleExample = new XieChengCollidingDataLoopCycleExample();
        loopCycleExample.createCriteria().andIsDeleteEqualTo(0).andRetryCountGreaterThan(0);
        int cycleCount = loopCycleMapper.countByExample(loopCycleExample);

        XieChengCollidingDataRobExample robExample = new XieChengCollidingDataRobExample();
        // todo 是否要加push_time
        robExample.createCriteria().andIsDeleteEqualTo(0).andRetryCountGreaterThan(0);
        int robCount = robMapper.countByExample(robExample);

        // 查log表是否存在：create_time=当天且business_code=707
        // todo 广绣提供
        Integer retryThresholdSize = 100000;
        Date today = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        XieChengCollidingDataLogExample logExample = new XieChengCollidingDataLogExample();
        logExample.createCriteria().andIsDeleteEqualTo(0).andBusinessCodeEqualTo(707).andCreateTimeGreaterThanOrEqualTo(today);
        int overCount = logMapper.countByExample(logExample);

        // 查TRUE表release_time=7天后的量级是否超限（500w）
        // todo 广绣提供
        Integer trueDataThresholdSize = 5000000;
        LocalDate start = LocalDate.now().plusDays(7);
        LocalDate end = LocalDate.now().plusDays(8);
        Date releaseDateStart = Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date releaseDateEnd = Date.from(end.atStartOfDay(ZoneId.systemDefault()).toInstant());

        XieChengCollidingDataLoopCycleExample cycleExample = new XieChengCollidingDataLoopCycleExample();
        cycleExample.createCriteria().andIsDeleteEqualTo(0).andReleaseTimeGreaterThanOrEqualTo(releaseDateStart).andReleaseTimeLessThan(releaseDateEnd);
        int trueDataCount = loopCycleMapper.countByExample(cycleExample);
        Boolean conditonSwitch = (cycleCount + robCount) >= retryThresholdSize || overCount > 0 || trueDataCount >= trueDataThresholdSize;

        // 查询条件开启撞库开关（redis）、日志打印开关状态
        // 如开关是开启状态：1.需要关闭，则关闭后发送钉钉告警,return。2.执行重试撞库
        // 如开关是关闭状态：1.需要开启，则开启后执行重试撞库。2.do-nothing
        String redisSwitch = redisChgService.get(RedisKeyConstant.XIECHENG_CONDITIONSWITCH);
        if ("true".equalsIgnoreCase(redisSwitch)) {
            if (conditonSwitch) {
                process();
            } else {
                redisChgService.set(RedisKeyConstant.XIECHENG_CONDITIONSWITCH, "false");
                // 发送钉钉告警
                DingDingMarkdownMessage.Markdown markdown = new DingDingMarkdownMessage.Markdown();
                markdown.setTitle("携程撞库异常量级过大通知");
                markdown.setText("3710058携程重试堆积量级已超" + marketingCommonConfig.getXieChengSmsCollidingRetryWarnCount() + "条。"
                );
                DingDingMarkdownMessage dingDingMarkdownMessage = new DingDingMarkdownMessage();
                dingDingMarkdownMessage.setMarkdown(markdown);
                dingDingRobotHookService.sendMessageGroup(marketingCommonConfig.getXieChengGroupAccessToken(),
                        marketingCommonConfig.getXieChengGroupSecret(), dingDingMarkdownMessage, true);
            }
        } else if (conditonSwitch) {
            redisChgService.set(RedisKeyConstant.XIECHENG_CONDITIONSWITCH, "true");
            process();
        }
    }

    private AtomicInteger process(Map.Entry<String, BiConsumer<List<?>, AtomicInteger>> entry,
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

                // 转化
                if (entry.getKey().equals("rob")) {
                    List<XieChengCollidingDataRob> robList = dataList.stream().map(t -> {
                        XieChengCollidingDataRob rob = new XieChengCollidingDataRob();
                        BeanUtils.copyProperties(t, rob);
                        return rob;
                    }).collect(Collectors.toList());

                    // 分组
                    List<List<XieChengCollidingDataRob>> partitions = Lists.partition(robList, PARTATIONNUM);
                    for (List<XieChengCollidingDataRob> partition : partitions) {
                        threadPool.submit(() -> entry.getValue().accept(partition, failNum));
                    }
                } else {
                    // 分组
                    List<List<XieChengCollidingDataLoopCycle>> partitions = Lists.partition(dataList, PARTATIONNUM);
                    for (List<XieChengCollidingDataLoopCycle> partition : partitions) {
                        threadPool.submit(() -> entry.getValue().accept(partition, failNum));
                    }
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
