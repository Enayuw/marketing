package com.br.marketing.check.service.Impl;

import com.br.marketing.check.service.PushFinishService;
import com.br.marketing.check.task.FtpToSftpCheckTask;
import com.br.marketing.check.task.SftpSignFileCheckTask;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.LoanFile;
import com.br.marketing.entity.SyncConfig;
import com.br.marketing.mapper.LoanFileMapper;
import com.br.marketing.mapper.SyncConfigMapper;
import com.br.marketing.service.EmailService;
import com.br.marketing.service.SyncConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class PushFinishServiceImpl implements PushFinishService {
    @Resource
    LoanFileMapper loanFileMapper;
    @Autowired
    SyncConfigService syncConfigService;
    @Value("${otherConfig.warning.sftpHost:00}")
    private String sftpHost;
    @Value("${otherConfig.warning.sftpPort:00}")
    private Integer sftpPort;
    @Value("${otherConfig.warning.sftpUser:00}")
    private String sftpUsername;
    @Value("${otherConfig.warning.sftpPwd:00}")
    private String sftpPwd;
    @Resource
    EmailService businessAlarmServiceImpl;
    @Resource
    RedisChgService redisChgService;
    @Resource
    EmailService systemExceptionServiceImpl;
    @Resource
    SyncConfigMapper loanSyncConfigMapper;

    @Override
    public void pushFinish(String apiCode) {
        Integer num = loanFileMapper.queryBlfBySignStatus(apiCode);

        SyncConfig config = new SyncConfig();
        config.setApiCode(apiCode);
        config.setType(2);
        config.setDataType(1);
        config = loanSyncConfigMapper.queryConfigByConditaion(config);
        SftpClient sftpClient = new SftpClient(sftpHost, sftpPort, sftpUsername, sftpPwd);
        if (num != null && num > 0) {
            signFileAlarm(apiCode);
        } else {
            if (config.getCheckSuccess() == 1) {
                pushSuccess(apiCode, sftpClient);
            } else if (config.getCheckFinish() == 1) {
                pushFinish(apiCode, sftpClient);
            }
        }
        try {
            sftpClient.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 结果文件2个小时还未回传到客户sftp，触发报警
     *
     * @param apiCode 客户编号
     */
    private void ftpToSftpCheck(String apiCode) {
        String s = redisChgService.get(Constants.FTP_TO_SFTP_CHECK_TIME);
        int i = 1;
        if (StringUtils.isNotEmpty(s)) {
            i = Integer.parseInt(s);
        }
        final ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1,
                new BasicThreadFactory.
                        Builder().namingPattern("ftpToSftpCheck-schedule-pool-%d").daemon(true).build());
        FtpToSftpCheckTask ftpToSftpCheckTask = new FtpToSftpCheckTask(apiCode, businessAlarmServiceImpl, executorService);
        executorService.schedule(ftpToSftpCheckTask, 3600000 * i, TimeUnit.MILLISECONDS);
    }

    /**
     * finish标识文件未上传报警
     *
     * @param apiCode 客户编号
     */
    private void signFileAlarm(String apiCode) {
        ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1,
                new BasicThreadFactory.
                        Builder().namingPattern("signFileAlarm-schedule-pool-%d").daemon(true).build());
        SftpSignFileCheckTask signFileCheckTask = new SftpSignFileCheckTask(apiCode, systemExceptionServiceImpl,
                businessAlarmServiceImpl, sftpHost, sftpPort, sftpUsername, sftpPwd, executor);
        executor.scheduleWithFixedDelay(signFileCheckTask, 1000, 1800000, TimeUnit.MILLISECONDS);
    }

    private void pushFinish(String apiCode, SftpClient sftpClient) {
        String dateAddYyMmDd = DateHelper.getDateAddYyMmDd(0);
        String finishPath = syncConfigService.getPath() + "result/" + apiCode + "/";
        File dir = new File(finishPath);
        if (!dir.exists()) {
            boolean mkdirs = dir.mkdirs();
            if (!mkdirs) {
                log.error("创建目录失败{}", finishPath);
            }
        }
        String finishFileName = apiCode + "_ReturnCompleted_" + dateAddYyMmDd + ".finish";
        File finishFile = new File(finishPath + finishFileName);
        log.warn("exists:{},path:{}", finishFile.exists(), finishFile.getAbsolutePath());

        try {
            boolean newFile = true;
            if (!finishFile.exists()) {
                newFile = finishFile.createNewFile();
            }
            log.warn("exists:{},path:{}", finishFile.exists(), finishFile.getAbsolutePath());
            if (newFile) {
                sftpClient.connect();
                sftpClient.uploadFile("/UploadFiles/marketing/" + apiCode + "/output/" + dateAddYyMmDd + "/", finishFileName, finishPath + finishFileName);
                boolean existFile = sftpClient.isExistFile("/UploadFiles/marketing/" + apiCode + "/output/" + dateAddYyMmDd + "/" + finishFileName);
                if (existFile) {
                    ftpToSftpCheck(apiCode);
                } else {
                    signFileAlarm(apiCode);
                }
            } else {
                log.error("创建finish文件失败");
            }
        } catch (Exception e) {
            log.error("创建finish文件失败", e);
        } finally {
            try {
                sftpClient.disconnect();
            } catch (Exception e) {
                log.error("Exception", e);
            }
        }
    }

    private void pushSuccess(String apiCode, SftpClient sftpClient) {
        List<LoanFile> loanFiles = loanFileMapper.queryUploadFile(apiCode);
        String remotePath = "/UploadFiles/marketing/" + apiCode + "/output/" + DateHelper.getDateAddYyMmDd(0) + "/";
        try {
            sftpClient.connect();
            for (LoanFile loanFile : loanFiles) {
                String zipFileName = loanFile.getZipFileName();
                String filePath = loanFile.getFilePath();
                File file = new File(filePath + "/" + zipFileName);
                if (file.exists()) {
                    String successFileName = zipFileName + ".success";
                    File successFile = new File(syncConfigService.getPath() + "sftp_data/" + apiCode + "/" + successFileName);
                    boolean newFile = true;
                    if (!successFile.exists()) {
                        newFile = successFile.createNewFile();
                    }
                    if (newFile) {
                        log.warn("push success to sftp :{}", successFileName);
                        sftpClient.uploadFile(remotePath, successFileName, syncConfigService.getPath() + "sftp_data/" + apiCode + "/" + successFileName);
                    }
                }
            }
        } catch (Exception e) {
            log.error("push success to sftp 异常，{}", e);
        }

    }
}
