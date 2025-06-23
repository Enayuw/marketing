package com.br.marketing.chain.xiecheng;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.XieChengReportContext;
import com.br.marketing.enums.HandlerStageEnum;
import com.br.marketing.enums.XieChengBizMarkEnum;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.thread.TaggedFuture;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 携程上报责任链构建器
 */
@Component
public class XieChengReportHandlerChain implements ApplicationContextAware {

    private List<AbstractXieChengReportHandler> cpaHandlers;

    private List<AbstractXieChengReportHandler> cpsHandlers;

    @Resource
    @Qualifier("xieChengReportThreadPool")
    ThreadPoolExecutor threadPool;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, AbstractXieChengReportHandler> handlerMap = applicationContext.getBeansOfType(AbstractXieChengReportHandler.class);
        List<AbstractXieChengReportHandler> handlers = handlerMap.values()
                .stream()
                .collect(Collectors.toList());
        cpaHandlers = handlers.stream()
                .filter(handler -> !XieChengBizMarkEnum.CPS.name().equals(handler.getBizMark())).collect(Collectors.toList());
        cpsHandlers = handlers.stream()
                .filter(handler -> !XieChengBizMarkEnum.CPA.name().equals(handler.getBizMark())).collect(Collectors.toList());
    }

    public void handle(XieChengReportContext context) {
        threadPool.setCorePoolSize(marketingCommonConfig.getXcMqReportHandlerThreadNum());
        threadPool.setMaximumPoolSize(marketingCommonConfig.getXcMqReportHandlerThreadNum());
        //1.判断cpa还是cps
        List<AbstractXieChengReportHandler> handlers;
        if ("1".equals(context.getPushConfig().getConditionKey())) {
            handlers = cpaHandlers;
        } else {
            handlers = cpsHandlers;
        }
        //2.先执行pre阶段的handler(去重)，目前只有一个handler，不需要排序，后续若有多个，可在handler中添加order来排序
        List<AbstractXieChengReportHandler> preHandlers = handlers.stream()
                .filter(handler -> HandlerStageEnum.PRE.name().equals(handler.getStage())).collect(Collectors.toList());
        for (AbstractXieChengReportHandler preHandler : preHandlers) {
            String preMessage = preHandler.process(context);
            if (StringUtils.isNotBlank(preMessage)) context.setError(preMessage);
        }
        //3.执行thread阶段，该阶段handler可以同时处理，为了提高效率，放在线程池中处理
        List<Callable<String>> tasks = new ArrayList<>();
        List<AbstractXieChengReportHandler> threadHandlers = handlers.stream()
                .filter(handler -> HandlerStageEnum.THREAD.name().equals(handler.getStage())).collect(Collectors.toList());
        // 线程池开启开关
        if(marketingCommonConfig.getXcMqReportHandlerSwitch()){
            for (AbstractXieChengReportHandler threadHandler : threadHandlers) {
                String threadMessage = threadHandler.process(context);
                if (StringUtils.isNotBlank(threadMessage)) context.setError(threadMessage);
            }
        }else {
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
                } catch (ExecutionException e) {
                    messages.add(future.getTag() + ":" + e.getMessage());
                    context.setExceptionFlag(true);
                } catch (TimeoutException e) {
                    messages.add(future.getTag() + ":" + e.getMessage());
                    context.setExceptionFlag(true);
                }
            }
            messages = messages.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (messages.size() > 0) context.setError(String.join(";", messages));
        }
    }

}
