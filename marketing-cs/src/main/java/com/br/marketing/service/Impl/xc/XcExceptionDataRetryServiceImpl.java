package com.br.marketing.service.Impl.xc;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.XieChengCollidingDataLoopCycle;
import com.br.marketing.entity.XieChengCollidingDataRob;
import com.br.marketing.enums.DingDingAlarmFunctionEnum;
import com.br.marketing.mapper.XieChengCollidingDataLoopCycleMapper;
import com.br.marketing.mapper.XieChengCollidingDataRobMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.br.marketing.util.TimeUtils;
import com.br.marketing.webhook.dingding.msgtype.DingDingMarkdownMessage;
import com.br.marketing.webhook.dingding.service.DingDingRobotHookService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
    XieChengRobDataCollidingService robDataCollidingService;
    @Resource
    private DingDingRobotHookService dingDingRobotHookService;

    private final static int PARTATIONNUM = 50;

    @Override
    public void process() {
        // 创建线程池
        ThreadPoolExecutor threadPool =
                BrExecutors.getThreadPool(marketingCommonConfig.getXieChengSmsCollidingRetryThread(),
                        marketingCommonConfig.getXieChengSmsCollidingRetryThread());

        // 分页大小
        Integer pageSize = marketingCommonConfig.getXiechengCollidingPageSize();

        // 先撞重试次数1和2的数据。先撞TRUE 再撞FALSE。
        processByTrue(threadPool, false, pageSize);
        processByFalse(threadPool, false, pageSize);

        // 到时间再撞重试次数3的数据。先撞TRUE 再撞FALSE。
        if (isLastTime()) {
            processByTrue(threadPool, true, pageSize);
            processByFalse(threadPool, true, pageSize);
        }

        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("携程异常重试撞库线程池关闭");
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), ex.getMessage()
                    , "携程异常重试撞库，日志保存线程池结束异常！"), ex);
            Thread.currentThread().interrupt();
        }
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

    /**
     * 修改线程池大小
     * @param pool
     */
    private void modifyThreadPool(ThreadPoolExecutor pool) {
        Integer threadNum = marketingCommonConfig.getXieChengSmsCollidingRetryThread();
        ThreadPoolAdjustmentUtil.adjustThreadPoolSize(pool, threadNum);
    }

    /**
     * 处理TRUE表数据重试
     * @param threadPool
     * @param isLast
     * @param pageSize
     */
    private void processByTrue(ThreadPoolExecutor threadPool, Boolean isLast, Integer pageSize) {
        Long minId = null;
        while (cycleDataService.canStart()) {
            List<XieChengCollidingDataLoopCycle> dataList = loopCycleMapper.selectCycleByRetryCount(minId, isLast, pageSize);
            if (CollectionUtils.isEmpty(dataList)) {
                break;
            }
            minId = dataList.get(dataList.size() - 1).getId();

            // 修改线程池大小
            modifyThreadPool(threadPool);

            // 分组
            List<List<XieChengCollidingDataLoopCycle>> partitions = Lists.partition(dataList, PARTATIONNUM);
            for (List<XieChengCollidingDataLoopCycle> partition : partitions) {
                threadPool.submit(() -> cycleDataService.pushDataAndHandleResult(partition));
            }
        }
    }

    /**
     * 处理FALSE表数据重试
     * @param threadPool
     * @param isLast
     * @param pageSize
     */
    private void processByFalse(ThreadPoolExecutor threadPool, Boolean isLast, Integer pageSize) {
        Long minId = null;
        while (cycleDataService.canStart()) {
            List<XieChengCollidingDataRob> dataList = robMapper.selectRobByRetryCount(minId, isLast, pageSize);
            if (CollectionUtils.isEmpty(dataList)) {
                break;
            }
            minId = dataList.get(dataList.size() - 1).getId();

            // 修改线程池大小
            modifyThreadPool(threadPool);

            // 分组
            List<List<XieChengCollidingDataRob>> partitions = Lists.partition(dataList, PARTATIONNUM);
            for (List<XieChengCollidingDataRob> partition : partitions) {
                threadPool.submit(() -> robDataCollidingService.pushDataAndHandleResult(partition));
            }
        }
    }

    @Override
    public void sendDingDingAlert(String title, String text) {
        DingDingMarkdownMessage.Markdown markdown = new DingDingMarkdownMessage.Markdown();
        markdown.setTitle(title);
        markdown.setText(text);
        DingDingMarkdownMessage dingDingMarkdownMessage = new DingDingMarkdownMessage();
        dingDingMarkdownMessage.setMarkdown(markdown);

        String token = marketingCommonConfig.getXieChengGroupAccessToken();
        String secret = marketingCommonConfig.getXieChengGroupSecret();
        try {
            dingDingRobotHookService.sendMessageGroup(token,
                    secret, dingDingMarkdownMessage, true);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                    , "发送钉钉消息失败"), e);
        }
    }

    @Override
    public void sendDingDingAlertByAtSomeBody(String msg) {
        Map<String, JSONObject> webHookInfo = marketingCommonConfig.getDingDingWebHookInfo();
        Map<String, Object> map = webHookInfo.get(DingDingAlarmFunctionEnum.XIECHENG_TRUE_DELETE_NOTICE.toString());

        dingDingRobotHookService.sendDingDingTextMessage(msg, map);
    }
}