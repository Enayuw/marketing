package com.br.marketing.service.sftp.impl;

import com.br.marketing.common.enums.DataTypeEnum;
import com.br.marketing.entity.LoanFile;
import com.br.marketing.mapper.LoanFileMapper;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.service.ftp.SftpUploadHandlerService;
import com.br.marketing.service.sftp.PushService;
import com.br.marketing.service.sftp.ZipFileCheckService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by Bairong on 2019/8/28.
 */
@Service
@Slf4j
public class PushServiceImpl implements PushService {

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
    ZipFileCheckService zipFileCheckServiceImpl;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    SftpUploadHandlerService sftpUploadHandlerService;

    @Override
    public void push(List<LoanFile> files) throws Exception {
        checkZipFile(files);
        pushToSftp(files);
    }
    private void checkZipFile(List<LoanFile> files){
        for(LoanFile blf:files){
            long l = System.currentTimeMillis();
            zipFileCheckServiceImpl.zipFileCheck(blf);
            log.warn("cost time :{}",System.currentTimeMillis()-l);
        }

    }

    private void pushToSftp(List<LoanFile> files) throws Exception {
        for(LoanFile blf:files){
            sftpUploadHandlerService.insertSftpUploadTask(blf.getApiCode(), blf.getFilePath().concat("/"), blf.getZipFileName(),
                    DataTypeEnum.SCORE.getValue(),
                    "UPDATE stra_his_file SET status=2, update_time=now() where api_code ='" + blf.getApiCode() + "' AND zipFile_name = '"
                            + blf.getZipFileName() + "'");
        }
        /*String apiCode=files.get(0).getApiCode();
        SftpClient sftpClient = new SftpClient(sftpHost,sftpPort,sftpUsername,sftpPwd);
        try {
            sftpClient.connect();
            String remotePath="/UploadFiles/marketing/"+apiCode+"/output/"+ DateHelper.getDateAddYyMmDd(0);
            for(LoanFile blf:files){
                String zipFilePathAndName=blf.getFilePath().concat("/").concat(blf.getZipFileName());
                File file = new File(zipFilePathAndName);
                if(file.exists()){
                    log.warn("push zip to sftp :{}",blf.getZipFileName());
                    checkMockSwitch();
                    boolean flag= sftpClient.uploadFile(remotePath, blf.getZipFileName(), zipFilePathAndName);
                    if(flag){
                        String completeFileaName=apiCode+"_"+blf.getBatchNumber()+"_"+DateHelper.getDateAddYyMmDd(0)+".complete";
                        File completeFile=new File(syncConfigService.getPath()+"sftp_data/"+apiCode+"/"+completeFileaName);
                        if(!completeFile.getParentFile().exists()){
                            completeFile.getParentFile().mkdirs();
                        }
                        completeFile.createNewFile();
                        if(completeFile.exists()){
                            log.warn("push complete to sftp :{}",completeFileaName);
                            sftpClient.uploadFile(remotePath, completeFileaName, syncConfigService.getPath()+"sftp_data/"+apiCode+"/"+completeFileaName);
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.PUSH_TO_SFTP.getCode(),
                    "跑分文件推送SFTP异常，apiCode："+apiCode), e);
            throw e;
        }finally {
            try {
                sftpClient.disconnect();
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.PUSH_TO_SFTP.getCode(),
                        "跑分文件推送SFTP异常，apiCode："+apiCode), e);
            }
        }*/
    }

    public void checkMockSwitch() throws Exception {
        Boolean uploadFileSftp = marketingCommonConfig.getUploadFileSftp();
        if(uploadFileSftp){
            throw new Exception();
        }
    }

}
