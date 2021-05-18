package com.br.marketing.api.aspect.alarm;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import com.br.marketing.api.StrategyEarlyWaringApiApplication;
import com.br.marketing.api.service.AlarmCs;
import lombok.extern.slf4j.Slf4j;


/**
 * The type Alarm appender.
 *
 * @param <E> the type parameter
 */
@Slf4j
public class AlarmAppender<E> extends RollingFileAppender<E>  {
    @Override
    protected void subAppend(E eventObject) {
        super.subAppend(eventObject);
        if(eventObject instanceof LoggingEvent){
            ThrowableProxy throwableProxy = (ThrowableProxy)((LoggingEvent)eventObject).getThrowableProxy();
            if(throwableProxy!=null){
                Throwable throwable = throwableProxy.getThrowable();
                try {
                    AlarmCs alarmCs= StrategyEarlyWaringApiApplication.ac.getBean(AlarmCs.class);
                    alarmCs.handle(throwable,"api","marketing");
                } catch (Exception e) {
                    log.warn("Exception",e);
                }
            }
        }
    }
}
