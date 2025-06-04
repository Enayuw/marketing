package com.br.marketing.chain.xiecheng;

import com.br.marketing.context.XieChengReportContext;
import com.br.marketing.enums.XieChengBizMarkEnum;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 携程上报责任链构建器
 */
@Component
public class XieChengReportHandlerChain implements ApplicationContextAware {

    private List<AbstractXieChengReportHandler> cpaHandlers;

    private List<AbstractXieChengReportHandler> cpsHandlers;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, AbstractXieChengReportHandler> handlerMap = applicationContext.getBeansOfType(AbstractXieChengReportHandler.class);
        List<AbstractXieChengReportHandler> handlers = handlerMap.values()
                .stream()
                .sorted(Comparator.comparing(AbstractXieChengReportHandler::getOrder))
                .collect(Collectors.toList());
        this.cpaHandlers = handlers.stream()
                .filter(handler -> !XieChengBizMarkEnum.CPS.name().equals(handler.getBizMark())).collect(Collectors.toList());
        this.cpsHandlers = handlers.stream()
                .filter(handler -> !XieChengBizMarkEnum.CPA.name().equals(handler.getBizMark())).collect(Collectors.toList());
        buildChain(cpaHandlers);
        buildChain(cpsHandlers);
    }

    private void buildChain(List<AbstractXieChengReportHandler> handlers) {
        for (int i = 0; i < handlers.size() - 1; i++) {
            handlers.get(i).setNext(handlers.get(i + 1));
        }
    }

    public void handle(XieChengReportContext context) {
        if ("1".equals(context.getPushConfig().getConditionKey())) {
            if (!cpaHandlers.isEmpty()) {
                cpaHandlers.get(0).handle(context);
            }
        } else {
            if (!cpsHandlers.isEmpty()) {
                cpsHandlers.get(0).handle(context);
            }
        }

    }

}
