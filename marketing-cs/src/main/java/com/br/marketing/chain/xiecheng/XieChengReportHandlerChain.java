package com.br.marketing.chain.xiecheng;

import com.br.marketing.context.XieChengReportContext;
import com.br.marketing.enums.XieChengBizMarkEnum;
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
        //1.确定cpa还是cps
        List<AbstractXieChengReportHandler> handlers;
        if ("1".equals(context.getPushConfig().getConditionKey())) {
            handlers = cpaHandlers;
        } else {
            handlers = cpsHandlers;
        }
        //4.执行check阶段，该阶段handler可以同时处理，为了提高效率，放在线程池中处理
        List<Callable<String>> tasks = new ArrayList<>();
        for (AbstractXieChengReportHandler handler : handlers) {
            tasks.add(() -> handler.process(context));
        }
        List<Future<String>> futures;
        try {
            futures = threadPool.invokeAll(tasks, 60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            for (Callable<String> task : tasks) {
                if (task instanceof Future) {
                    ((Future) task).cancel(true);
                }
            }
            throw new RuntimeException("任务执行被中断", e);
        }
        List<String> messages = new ArrayList<>();
        List<Exception> exceptions = new ArrayList<>();
        for (Future<String> future : futures) {
            try {
                String message = future.get(10, TimeUnit.SECONDS);
                messages.add(message);
            } catch (InterruptedException e) {
                for (Future<String> f : futures) {
                    if (!f.isDone()) {
                        f.cancel(true);
                    }
                }
                throw new RuntimeException("获取结果时被中断", e);
            } catch (ExecutionException e) {
                exceptions.add(e);
            } catch (TimeoutException e) {
                exceptions.add(e);
            }
        }
        if (messages.size() != handlers.size() || !exceptions.isEmpty()) {
            throw new RuntimeException("任务执行失败");
        }
        messages = messages.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (messages.size() > 0) context.setError(String.join("; ", messages));
    }

}
