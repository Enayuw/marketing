package com.br.marketing.push.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.file.FtpUtil2;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.LoanFileMapper;
import com.br.marketing.mapper.TaskStatusMapper;
import com.br.marketing.push.service.PushService;
import com.br.marketing.push.service.ZipFileCheckService;
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
    TaskStatusMapper taskStatusMapper;

    @Override
    public void push(List<LoanFile> files) {
        checkZipFile(files);
        pushToSftp(files);

    }

    private void checkZipFile(List<LoanFile> files){
        String apiCode=files.get(0).getApiCode();
        JSONObject json=new JSONObject();
        json.put("apiCode",apiCode);
        for(LoanFile blf:files){
            json.put("batchNumber",blf.getBatchNumber());
            json.put("file",blf.getZipFileName());
            long l = System.currentTimeMillis();
            zipFileCheckServiceImpl.zipFileCheck(json);
            log.warn("cost time :{}",System.currentTimeMillis()-l);
        }

    }

    public void pushToSftp(List<LoanFile> files){
        String apiCode=files.get(0).getApiCode();
        SftpClient sftpClient = new SftpClient(sftpHost,sftpPort,sftpUsername,sftpPwd);
        try {
            sftpClient.connect();
            String remotePath="/UploadFiles/marketing/"+apiCode+"/output/"+ DateHelper.getDateAddYyMmDd(0);
            for(LoanFile blf:files){
                String zipFileName = blf.getZipFileName();
                String[] split = zipFileName.split("/");
                String name = split[split.length - 1];
                File file = new File(zipFileName);
                if(file.exists()){
                    log.warn("push zip to sftp :{}",zipFileName);
                    boolean flag= sftpClient.uploadFile(remotePath, name, zipFileName);
                    if(flag){
                        String completeFileaName=apiCode+"_"+blf.getBatchNumber()+"_"+DateHelper.getDateAddYyMmDd(0)+".complete";
                        File completeFile=new File(path+"/sftp_data/"+apiCode+"/"+completeFileaName);
                        completeFile.createNewFile();
                        if(completeFile.exists()){
                            log.warn("push complete to sftp :{}",completeFileaName);
                            sftpClient.uploadFile(remotePath, completeFileaName, path+"/sftp_data/"+apiCode+"/"+completeFileaName);
                        }
                    }
                }
                blf.setZipFileName(name);
//                blf.setErrorFile(errorFile);
                loanFileMapper.updateFile(blf);
                TaskStatus bts = new TaskStatus();
                bts.setBatchNumber(blf.getBatchNumber());
                bts.setFileId(blf.getId());
                taskStatusMapper.updateTaskStatus(bts);

            }

        } catch (Exception e) {
            log.error("Exception",e);
        }finally {
            try {
                sftpClient.disconnect();
            } catch (Exception e) {
                log.error("Exception",e);
            }
        }
    }



}
