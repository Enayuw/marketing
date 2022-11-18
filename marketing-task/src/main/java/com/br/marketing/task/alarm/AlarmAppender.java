package com.br.marketing.task.alarm;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.exception.HxResultRuntimeException;
import com.br.marketing.service.EmailService;
import com.br.marketing.service.Impl.SystemExceptionServiceImpl;
import com.br.marketing.task.Scheduler;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;


@Slf4j
public class AlarmAppender<E> extends RollingFileAppender<E>  {
    private final static Pattern S=Pattern.compile("\\001");
    @Override
    protected void subAppend(E eventObject) {
        super.subAppend(eventObject);

        if(eventObject instanceof LoggingEvent){
            try {
                ThrowableProxy throwableProxy = (ThrowableProxy)((LoggingEvent)eventObject).getThrowableProxy();
                String loggerName = ((LoggingEvent) eventObject).getLoggerName();
                String formattedMessage = ((LoggingEvent) eventObject).getFormattedMessage();
                StringBuilder content=new StringBuilder(formattedMessage);
                SystemExceptionServiceImpl emailService=Scheduler.ac.getBean(SystemExceptionServiceImpl.class);
                if(throwableProxy!=null) {
                    Throwable throwable = throwableProxy.getThrowable();
                    if(throwable instanceof HxResultRuntimeException){
                        String message = throwable.getMessage();
                        String[] split = S.split(message);
                        emailService.hxResultErrorAlarm(split[0],split[1]);
                        return;
                    }
                    content.append("-----").append(throwable.getMessage());
                }
                emailService.sendAlarmPrintStack(loggerName+":</br>"+content.toString(), "LOAN-WARNING-TASK",throwableProxy);
            } catch (Exception e) {
                log.warn("Exception",e);
            }
        }
    }
}
