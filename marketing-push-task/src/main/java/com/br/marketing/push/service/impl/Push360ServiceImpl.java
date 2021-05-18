package com.br.marketing.push.service.impl;

import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.file.FtpUtil2;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.LoanFileMapper;
import com.br.marketing.mapper.TaskStatusMapper;
import com.br.marketing.push.PushApplication;
import com.br.marketing.push.service.PushService;
import com.br.marketing.push.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;

/**
 * Created by Bairong on 2020/5/11.
 */
@Service
@Slf4j
public class Push360ServiceImpl implements PushService {
    @Resource
    LoanFileMapper loanFileMapper;
    @Value("${otherConfig.warning.path:00}")
    private String path;
    @Value("${otherConfig.warning.ruleList:00}")
    private String rules;
    @Value("${otherConfig.warning.ftpHost:00}")
    private String ftpHost;
    @Value("${otherConfig.warning.ftpPort:00}")
    private Integer ftpPort;
    @Value("${otherConfig.warning.ftpUsername:00}")
    private String ftpUsername;
    @Value("${otherConfig.warning.ftpPwd:00}")
    private String ftpPwd;
    @Resource
    TaskStatusMapper taskStatusMapper;

    @Override
    public void push(List<LoanFile> files) {
        log.warn("start push files --{}",files.size());
        FtpUtil2 ftp=new FtpUtil2();
        if(files ==null||files.isEmpty()){
            return;
        }
        String batchNumber="";
        String apiCode =files.get(0).getApiCode();
        try {
            String today =  DateHelper.getDateAddYyMmDd(0);
            boolean connect = ftp.connect( "/loanwarn/" + apiCode + "/output/" + today + "/", ftpHost, ftpPort, ftpUsername, ftpPwd);
            if(!connect){
                log.error("获取ftp链接出错");
                return;
            }
            for (LoanFile loanFile : files) {
                List<String> fileNames =loanFile.getZipFileNames();
                for(String  name:fileNames){
                    File file = new File(name);
                    if(file.exists()){
                        log.warn("push :{}",name);
                        ftp.upload(file);
                    }
                    batchNumber=loanFile.getBatchNumber();
                    loanFileMapper.updateFile(loanFile);
                    TaskStatus bts = new TaskStatus();
                    bts.setBatchNumber(batchNumber);
                    bts.setFileId(loanFile.getId());
                    taskStatusMapper.updateTaskStatus(bts);
                }
                successUpLoad(apiCode,batchNumber);
            }

        } catch (Exception e) {
            log.error("上传文件到ftp出错",e);
        }finally {
            ftp.closeFtp();
        }
    }

    private void successUpLoad(String apiCode,String batchNumber){
        FtpUtil2 ftp=new FtpUtil2();
        try{
            String destPath=path+"/ftp_data/"+apiCode+"/";
            File writePath = new File(destPath);
            if (!writePath.exists()) {
                writePath.mkdirs();
            }
            String fileaName=destPath+apiCode+"_"+batchNumber+"_"+DateHelper.getDateAddYyMmDd(0)+".complete";
            File successFile=new File(fileaName);
            boolean newFile = successFile.createNewFile();
            log.info("客户批次回传标识文件---{}---{}",fileaName,newFile);
            String today =  DateHelper.getDateAddYyMmDd(0);
            ftp.connect( "/loanwarn/"+apiCode+"/output/"+today+"/", ftpHost, ftpPort, ftpUsername, ftpPwd);
            if(successFile.exists()){
                ftp.upload(successFile);
            }
        }catch (Exception e){
            log.error("上传周期日回传标识文件文件出错---{}",e);
        }finally {
            ftp.closeFtp();
        }

    }

}
