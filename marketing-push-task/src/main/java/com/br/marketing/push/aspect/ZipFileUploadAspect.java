package com.br.marketing.push.aspect;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.entity.LoanFile;
import com.br.marketing.push.service.ZipFileCheckService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;

/**
 * @Author: Bairong
 * @Time: 2020/11/20 10:36
 * @Company：百融
 * @Description: 文件上传到ftp时，发送文件全路径信息到mq
 */
@Aspect
@Component
@Slf4j
public class ZipFileUploadAspect {
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

    @Pointcut("execution(public * com.br.marketing.push.service.impl.Push*Impl.push(..))")
    public void push(){}


    @After("com.br.marketing.push.aspect.ZipFileUploadAspect.push()")
    public void push(JoinPoint joinPoint){
        Object[] args = joinPoint.getArgs();
        checkZipFile(args);
        pushToSftp(args);
    }

    private void checkZipFile(Object[] args){
        List<LoanFile> files= (List<LoanFile>) args[0];
        String apiCode=files.get(0).getApiCode();
        JSONObject json=new JSONObject();
        json.put("apiCode",apiCode);
        if((apiCode.equals(Constants.APICODE_360)||apiCode.equals(Constants.APICODE_360_QA))){
            for(LoanFile blf:files){
                List<String> fileNames=blf.getZipFileNames();
                json.put("batchNumber",blf.getBatchNumber());
                json.put("files",fileNames);
                long l = System.currentTimeMillis();
                zipFileCheckServiceImpl.zipFileCheck(json);
                log.warn("cost time :{}",System.currentTimeMillis()-l);
            }
        }else {
            for(LoanFile blf:files){
                json.put("batchNumber",blf.getBatchNumber());
                JSONArray array = new JSONArray();
                array.add(blf.getFilePath()+"/"+blf.getZipFileName());
                json.put("files",array);
                long l = System.currentTimeMillis();
                zipFileCheckServiceImpl.zipFileCheck(json);
                log.warn("cost time :{}",System.currentTimeMillis()-l);
            }
        }
    }


    public void pushToSftp(Object[] args){
        List<LoanFile> files= (List<LoanFile>) args[0];
        String apiCode=files.get(0).getApiCode();
        SftpClient sftpClient = new SftpClient(sftpHost,sftpPort,sftpUsername,sftpPwd);
        try {
            sftpClient.connect();
            String remotePath="/UploadFiles/loanwarn/"+apiCode+"/output/"+ DateHelper.getDateAddYyMmDd(0);
            for(LoanFile blf:files){
                String zipFileName = blf.getZipFileName();
                String filePath = blf.getFilePath();
                File file = new File(filePath+"/"+zipFileName);
                if(file.exists()){
                    log.warn("push zip to sftp :{}",filePath+"/"+zipFileName);
                    boolean flag= sftpClient.uploadFile(remotePath, zipFileName, filePath+"/"+zipFileName);
                    if(flag){
                        String completeFileaName=apiCode+"_"+blf.getBatchNumber()+"_"+DateHelper.getDateAddYyMmDd(0)+".complete";
                        File completeFile=new File(path+"/ftp_data/"+apiCode+"/"+completeFileaName);
                        if(completeFile.exists()){
                            log.warn("push complete to sftp :{}",completeFileaName);
                            sftpClient.uploadFile(remotePath, completeFileaName, path+"/ftp_data/"+apiCode+"/"+completeFileaName);
                        }
                    }
                }
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
