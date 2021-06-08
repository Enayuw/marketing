package com.br.marketing.push.service.impl;

import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.file.FtpUtil2;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.LoanFileMapper;
import com.br.marketing.mapper.TaskStatusMapper;
import com.br.marketing.push.service.PushService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;

/**
 * Created by Bairong on 2019/8/28.
 */
@Service
@Slf4j
public class PushServiceImpl implements PushService {

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
        log.info("start push files api_code:{}--{}",files.size());
        String zipFileName="";
        String errorFile="";
        FtpUtil2 ftp=new FtpUtil2();
        if(files ==null||files.isEmpty()){
            return;
        }
        String apiCode =files.get(0).getApiCode();
        try {
            String today = DateHelper.getDateAddYyMmDd(0);
            boolean connect = ftp.connect( "/loanwarn/" + apiCode + "/output/"+today+"/", ftpHost, ftpPort, ftpUsername, ftpPwd);
            if(!connect){
                log.error("获取ftp链接出错");
                return ;
            }
            for(LoanFile blf:files){
                String fileName = blf.getZipFileName();
                log.info("filename:{}",fileName);
                File file = new File(fileName);
                if(file.exists()){
                    boolean upload = ftp.upload(file);
                    if(upload){
                        successUpLoad(blf,ftp);
                    }else {
                        log.error("上传文件到ftp失败");
                    }

                    String[] split = fileName.split("/");
                    String name = split[split.length - 1];
                    if(name.indexOf("error")>-1){
                        errorFile=name;
                    }else {
                        zipFileName=name;
                    }
                    log.info("zipFile_name:{}",zipFileName);
                }

                blf.setZipFileName(zipFileName);
                blf.setErrorFile(errorFile);
                loanFileMapper.updateFile(blf);
                TaskStatus bts = new TaskStatus();
                bts.setBatchNumber(blf.getBatchNumber());
                bts.setFileId(blf.getId());
                taskStatusMapper.updateTaskStatus(bts);
            }

        } catch (Exception e) {
            log.error("上传文件到ftp出错",e);
        }finally {
            ftp.closeFtp();
        }
    }

    private void successUpLoad(LoanFile blf, FtpUtil2 ftp){
        try{
            String apiCode=blf.getApiCode();
            String batchNumber=blf.getBatchNumber();
            String[] split = blf.getZipFileName().split("/");
            String name = split[split.length - 1];
            String successFileName=name+".success";
            String destPath=path+"/sftp_data/"+apiCode+"/";
            File writePath = new File(destPath);
            if (!writePath.exists()) {
                writePath.mkdirs();
            }
            String fileaName=destPath+apiCode+"_"+batchNumber+"_"+DateHelper.getDateAddYyMmDd(0)+".complete";
            File completeFile=new File(fileaName);
            boolean newFile = completeFile.createNewFile();
            String s = destPath + successFileName;
            File successFile=new File(s);
            boolean newFile1 = successFile.createNewFile();
            log.info("客户批次回传标识文件---success:{}---create{}---complete:{}-----create:{}",successFileName,newFile1,fileaName,newFile);
            if(successFile.exists()){
                ftp.upload(successFile);
            }
            if(completeFile.exists()){
                ftp.upload(completeFile);
            }
        }catch (Exception e){
            log.error("上传周期日回传标识文件文件出错---{}",e);
        }

    }







}
