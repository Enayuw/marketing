package com.br.marketing.chain.xiecheng;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.XieChengReportContext;
import com.br.marketing.entity.XieChengReportHandlerConfig;
import com.br.marketing.enums.HandlerStageEnum;
import com.br.marketing.enums.XieChengBizMarkEnum;
import com.br.marketing.mapper.XieChengReportHandlerConfigMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.thread.TaggedFuture;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.api.client.util.Lists;
import com.google.common.base.Splitter;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 携程上报责任链构建器
 */
@Component
@Slf4j
public class XieChengReportHandlerChain {

    @Autowired
    private List<AbstractXieChengReportHandler> xieChengReportHandlers;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private XieChengReportHandlerConfigMapper xieChengReportHandlerConfigMapper;

    private LoadingCache<String, List<AbstractXieChengReportHandler>> xieChengReportHandlerCache = null;

    @PostConstruct
    private void init() {
        xieChengReportHandlerCache = Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build(key -> fetchXieChengReportHandlerChain(key));
    }

    private List<AbstractXieChengReportHandler> fetchXieChengReportHandlerChain(String bizForm) {
        List<String> handlerNameList = xieChengReportHandlerConfigMapper.selectHandlerNameByBizForm(bizForm);
        if (CollectionUtils.isEmpty(handlerNameList)) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                    "携程上报：bizForm = " + bizForm + "，未查询到handlerNameList！"));
            return null;
        }
        List<String> handlerNames = Splitter.on(",").splitToList(handlerNameList.get(0).trim());
        return xieChengReportHandlers.stream()
                .filter(handler -> handlerNames.contains(handler.getName()))
                .collect(Collectors.toList());
    }

    public void handle(XieChengReportContext context) {
        TpDynamicExecutor threadPool = TpDynamicExecutorFactory.getThreadPool(ThreadPoolNameEnum.XIECHENG_CALL_SMS_REPORT.getName(), 50, 50);
        //1.根据context中的type和conditionKey获取对应的handlerChain
        String bizForm = context.getType() + "-" + context.getPushConfig().getConditionKey();
        try {
            List<AbstractXieChengReportHandler> handlers = xieChengReportHandlerCache.get(bizForm);
            //2.先执行pre阶段的handler(去重)，目前只有一个handler，不需要排序，后续若有多个，可在handler中添加order来排序
            List<AbstractXieChengReportHandler> preHandlers = handlers.stream()
                    .filter(handler -> HandlerStageEnum.PRE.name().equals(handler.getStage())).collect(Collectors.toList());
            for (AbstractXieChengReportHandler preHandler : preHandlers) {
                String preMessage = preHandler.process(context);
                if (StringUtils.isNotBlank(preMessage)) {
                    context.setError(preMessage);
                    return;
                }
            }
            //3.执行thread阶段，该阶段handler可以同时处理，为了提高效率，放在线程池中处理
            List<Callable<String>> tasks = new ArrayList<>();
            List<AbstractXieChengReportHandler> threadHandlers = handlers.stream()
                    .filter(handler -> HandlerStageEnum.THREAD.name().equals(handler.getStage())).collect(Collectors.toList());
            // 线程池开启开关
            if (marketingCommonConfig.getXcMqReportHandlerSwitch()) {
                for (AbstractXieChengReportHandler threadHandler : threadHandlers) {
                    String threadMessage = threadHandler.process(context);
                    if (StringUtils.isNotBlank(threadMessage)) {
                        context.setError(threadMessage);
                        return;
                    }
                }
            } else {
                for (AbstractXieChengReportHandler handler : threadHandlers) {
                    tasks.add(() -> handler.process(context));
                }
                List<TaggedFuture<String>> futures = new ArrayList<>();
                try {
                    List<Future<String>> orgFutures = threadPool.invokeAll(tasks, 60, TimeUnit.SECONDS);
                    for (int i = 0; i < threadHandlers.size(); i++) {
                        futures.add(new TaggedFuture(threadHandlers.get(i).getName(), orgFutures.get(i)));
                    }
                } catch (InterruptedException e) {
                    for (Callable<String> task : tasks) {
                        if (task instanceof Future) {
                            ((Future) task).cancel(true);
                        }
                    }
                    throw new RuntimeException("携程上报handler执行被中断", e);
                }
                List<String> messages = new ArrayList<>();
                for (TaggedFuture<String> future : futures) {
                    try {
                        String message = future.getFuture().get(60, TimeUnit.SECONDS);
                        messages.add(message);
                    } catch (InterruptedException e) {
                        for (TaggedFuture<String> f : futures) {
                            if (!f.getFuture().isDone()) {
                                f.getFuture().cancel(true);
                            }
                        }
                        throw new RuntimeException("携程上报handler获取结果时被中断", e);
                    } catch (ExecutionException | TimeoutException e) {
                        messages.add(future.getTag() + ":" + e.getMessage());
                        context.setExceptionFlag(true);
                    }
                }
                messages = messages.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                if (!messages.isEmpty()) {
                    context.setError(String.join(";", messages));
                }
            }
        } finally {
            threadPool.shutdownAndAwaitTermination();
        }
    }

}
