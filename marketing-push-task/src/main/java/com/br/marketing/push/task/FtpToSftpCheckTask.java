package com.br.marketing.push.task;

import com.br.marketing.service.EmailService;
import lombok.extern.slf4j.Slf4j;

import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
public class FtpToSftpCheckTask extends TimerTask {
    private String apiCode;
    private EmailService businessAlarmServiceImpl;
    ScheduledExecutorService executorService;
    public FtpToSftpCheckTask(String apiCode, EmailService businessAlarmServiceImpl, ScheduledExecutorService executorService) {
        this.apiCode = apiCode;
        this.businessAlarmServiceImpl = businessAlarmServiceImpl;
        this.executorService=executorService;
    }

    @Override
    public void run() {
        businessAlarmServiceImpl.ftpToSftpCheck(apiCode);
        executorService.shutdown();
    }
}
