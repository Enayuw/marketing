package com.br.marketing.check.controller;

import com.br.marketing.check.task.FtpToSftpCheckTask;
import com.br.marketing.check.task.SignFileCheckTask;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.*;

/**
 * Created by Bairong on 2020/4/7.
 */
@RestController
@RequestMapping("/alarm/")
@Slf4j
public class AlarmController {

    @Resource
    EmailService reportServiceImpl;
    @Resource
    EmailService businessAlarmServiceImpl;
    @Resource
    EmailService validDataAlarmServiceImpl;
    @Resource
    EmailService ppdNoticeServiceImpl;
    @Resource
    EmailService systemExceptionServiceImpl;

    @Value("${otherConfig.warning.ftpHost}")
    private String ftpHost;
    @Value("${otherConfig.warning.ftpPort}")
    private Integer ftpPort;
    @Value("${otherConfig.warning.ftpUsername}")
    private String ftpUsername;
    @Value("${otherConfig.warning.ftpPwd}")
    private String ftpPwd;

    @Resource
    RedisChgService redisChgService;

    @GetMapping("monitoringExpirationAlarm")
    public String monitoringExpirationAlarm(){
        businessAlarmServiceImpl.monitoringExpirationAlarm();
        return "success";
    }

    @GetMapping("closeDateAlarm")
    public String closeDateAlarm(){
        businessAlarmServiceImpl.closeDateAlarm();
        return "success";
    }

    @GetMapping("signFileAlarm")
    public String signFileAlarm(String apiCode){
     /*   Timer t=new Timer();
        t.schedule(signFileCheckTask,1000,1800000);*/
        ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1,
                new BasicThreadFactory.
                        Builder().namingPattern("signFileAlarm-schedule-pool-%d").daemon(true).build());
        SignFileCheckTask signFileCheckTask = new SignFileCheckTask(apiCode,systemExceptionServiceImpl,
                businessAlarmServiceImpl,ftpHost,ftpPort,ftpUsername,ftpPwd,executor);
        executor.scheduleWithFixedDelay(signFileCheckTask,1000,1800000, TimeUnit.MILLISECONDS);
        return "success";
    }

    @GetMapping("dataFileVolumn")
    public String dataFileVolumn(String apiCode,String message){
        validDataAlarmServiceImpl.dataFileVolumn(apiCode,message);
        return "success";
    }
    @GetMapping("upLoadFile")
    public String upLoadFile(String apiCode,String batchNumber){
        validDataAlarmServiceImpl.fileUpload(apiCode,batchNumber);
        return "success";
    }

    @GetMapping("progressReport")
    public String progressReport(){
        log.info("progressReport");
        reportServiceImpl.progressReport();
        return "success";
    }

    @GetMapping("report")
    public String report(){
        log.info("report");
        reportServiceImpl.report();
        return "success";
    }

    @GetMapping("/alarm")
    public String alarm(String apiCode,String type,String message) {
        log.error("alarm  api_code--{},type--{},message--{}",apiCode,type,message);
        return "success";
    }

    @GetMapping("/fileSizeAlarm")
    public String fileSizeAlarm(String apiCode,String fileName) {
        log.error(" fileSizeAlarm apiCode--{},fileName--{}",apiCode,fileName);
        businessAlarmServiceImpl.fileSizeException(apiCode,fileName);
        return "success";
    }

    @GetMapping("/resultVolumeCheck")
    public String resultVolumeCheck(String apiCode) {
        businessAlarmServiceImpl.resultVolumeCheck(apiCode);
        return "success";
    }

    @GetMapping("/resultFileUploadFtp")
    public String resultFileUploadFtp(String apiCode,String message) {
        log.error(" resultFileUploadFtp apiCode--{},message--{}",apiCode,message);
        businessAlarmServiceImpl.fileUploadFtpException(apiCode,message);
        return "success";
    }

    @GetMapping("/ftpToSftpCheck")
    public String ftpToSftpCheck(final String apiCode) {
        String s = redisChgService.get(Constants.FTP_TO_SFTP_CHECK_TIME);
        int i=1;
        if(StringUtils.isNotEmpty(s)){
            i=Integer.parseInt(s);
        }
        final ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1,
                new BasicThreadFactory.
                        Builder().namingPattern("ftpToSftpCheck-schedule-pool-%d").daemon(true).build());
        FtpToSftpCheckTask ftpToSftpCheckTask = new FtpToSftpCheckTask(apiCode, businessAlarmServiceImpl,executorService);
        executorService.schedule(ftpToSftpCheckTask, 3600000*i,  TimeUnit.MILLISECONDS);
        return "success";
    }

    @GetMapping("/ppdFtpToSftpCheck")
    public String ppdFtpToSftpCheck(String apiCode) {
        businessAlarmServiceImpl.ftpToSftpCheck(apiCode);
        return "success";
    }

    @GetMapping("/sendReport")
    public String sendReport(String apiCode) {
        log.info("alarm sendReport apiCode--{},",apiCode);
        ppdNoticeServiceImpl.sendReport(apiCode);
        return "success";
    }

}
