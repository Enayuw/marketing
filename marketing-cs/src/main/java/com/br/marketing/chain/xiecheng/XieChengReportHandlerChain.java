package com.br.marketing.chain.xiecheng;

import com.br.marketing.context.XieChengReportContext;
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

    private List<AbstractXieChengReportHandler> handlers;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, AbstractXieChengReportHandler> handlerMap = applicationContext.getBeansOfType(AbstractXieChengReportHandler.class);
        handlers = handlerMap.values()
                .stream()
                .sorted(Comparator.comparing(AbstractXieChengReportHandler::getOrder))
                .collect(Collectors.toList());
        buildChain();
    }

    private void buildChain() {
        for (int i = 0; i < handlers.size() - 1; i++) {
            handlers.get(i).setNext(handlers.get(i + 1));
        }
    }

    public void handle(XieChengReportContext context) {
        if (!handlers.isEmpty()) {
            handlers.get(0).handle(context);
        }
    }

}
