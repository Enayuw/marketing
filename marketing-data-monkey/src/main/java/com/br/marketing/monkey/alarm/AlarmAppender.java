package com.br.marketing.monkey.alarm;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import com.br.marketing.monkey.MarketingDataMonkeyApplication;
import com.br.marketing.service.Impl.SystemExceptionServiceImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AlarmAppender<E> extends RollingFileAppender<E>  {
    @Override
    protected void subAppend(E eventObject) {
        super.subAppend(eventObject);
        if(eventObject instanceof LoggingEvent){
            ThrowableProxy throwableProxy = (ThrowableProxy)((LoggingEvent)eventObject).getThrowableProxy();
            String loggerName = ((LoggingEvent) eventObject).getLoggerName();
            String formattedMessage = ((LoggingEvent) eventObject).getFormattedMessage();
            StringBuilder content=new StringBuilder(formattedMessage);
            if(throwableProxy!=null) {
                Throwable throwable = throwableProxy.getThrowable();
                content.append("-----").append(throwable.getMessage());
            }
            try {
                SystemExceptionServiceImpl emailService= MarketingDataMonkeyApplication.ac.getBean(SystemExceptionServiceImpl.class);
                emailService.sendAlarmPrintStack(loggerName+":</br>"+content.toString(), "MARKETING-DATA-MONKEY",throwableProxy);
            } catch (Exception e) {
                log.warn("Exception",e);
            }
        }
    }
}
