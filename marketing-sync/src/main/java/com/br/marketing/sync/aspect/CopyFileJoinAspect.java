package com.br.marketing.sync.aspect;

import com.br.common.validator.DateUtils;
import com.br.marketing.client.BaseFtpClient;
import com.br.marketing.client.FtpClient;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.enums.DataTypeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.file.MyFileUtil;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.LoanFileMapper;
import com.br.marketing.mapper.SyncLogMapper;
import com.br.marketing.mapper.TransferFileTaskMapper;
import com.jcraft.jsch.SftpATTRS;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPFile;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@Slf4j
public class CopyFileJoinAspect {
    @Resource
    SyncLogMapper loanSyncLogMapper;
    @Resource
    LoanFileMapper loanFileMapper;

    @Autowired
    TransferFileTaskMapper transferFileTaskMapper;

    @Pointcut("execution(public * com.br.marketing.sync.service.impl.SyncServiceImpl.copyFile(..))")
    public void copyFile() {
    }

    @Pointcut("execution(public Boolean com.br.marketing.sync.service.impl.SyncServiceImpl.downloadFileToLocalDisk(..))")
    public void downloadFileToLocalDisk() {
    }

    @Pointcut("execution(public Boolean com.br.marketing.sync.service.impl.SyncServiceImpl.sftpFileUploadToMiNio(..))")
    public void sftpFileUploadToMiNio() {
    }

    @Around("com.br.marketing.sync.aspect.CopyFileJoinAspect.copyFile()")
    public Object copyFile(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        SyncConfig loanSyncConfig = new SyncConfig();
        String fileName = "";
        BaseFtpClient srcClient = null;
        BaseFtpClient targetClient = null;
        for (int i = 0; i < args.length; i++) {
            if (0 == i) {
                loanSyncConfig = (SyncConfig) args[i];
            } else if (1 == i) {
                fileName = (String) args[i];
            }else if(2 == i){
                srcClient= (BaseFtpClient) args[i];
            }else if(3 == i){
                targetClient= (BaseFtpClient) args[i];
            }else{
                log.error("args get Object is error ");
                break;
            }
        }
        if (srcClient == null || targetClient == null) {
            log.error("targetClient or srcClient is null");
            return Boolean.FALSE;
        }
        Boolean success = false;
        Object result = Boolean.FALSE;
        try {
            result = joinPoint.proceed(args);
            success = Boolean.TRUE.equals(result);
        } catch (Throwable throwable) {
            log.error("copyFile error", throwable);
        }
        if (success) {
            SyncLog loanSyncLog = setSyncLog(loanSyncConfig, fileName, srcClient);
            if (DataTypeEnum.TRANSFER.getValue().equals(loanSyncConfig.getDataType())) {
                insertSyncLog(loanSyncConfig, fileName, true, loanSyncLog);
                TransferFileTaskExample example = new TransferFileTaskExample();
                example.createCriteria().andApiCodeEqualTo(loanSyncConfig.getApiCode()).andFileNameEqualTo(fileName);
                TransferFileTask task = new TransferFileTask();
                task.setStatus(4);
                transferFileTaskMapper.updateByExampleSelective(task, example);
            } else {
                boolean b = vaildatorFile(loanSyncConfig, fileName, loanSyncLog, targetClient);
                insertSyncLog(loanSyncConfig, fileName, b, loanSyncLog);
                updateFileHisStatus(loanSyncConfig, fileName, b);
            }
        }
        return result;
    }

    @Around("downloadFileToLocalDisk()")
    public Object localDisk(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        SyncConfig loanSyncConfig = null;
        String fileName = "";
        BaseFtpClient srcClient = null;
        for (int i = 0; i < args.length; i++) {
            if (0 == i) {
                loanSyncConfig = (SyncConfig) args[i];
            } else if (1 == i) {
                srcClient = (BaseFtpClient) args[i];
            } else if (2 == i) {
                fileName = (String) args[i];
            } else {
                break;
            }
        }
        if (srcClient == null || loanSyncConfig == null) {
            log.warn("Download File To Local Disk srcClient is null");
            return Boolean.FALSE;
        }
        Object proceed = Boolean.FALSE;
        if (Constants.LOAN_DISK.equals(loanSyncConfig.getTargetType())) {
            SyncLog loanSyncLog = setSyncLog(loanSyncConfig, fileName, srcClient);
            try {
                proceed = joinPoint.proceed(args);
                insertSyncLog(loanSyncConfig, fileName, (Boolean) proceed, loanSyncLog);
            } catch (Throwable throwable) {
                log.error("download File LocalDisk error", throwable);
            }
        }
        return proceed;
    }

    @Around("sftpFileUploadToMiNio()")
    public Object uploadToMiNio(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        SyncConfig loanSyncConfig = null;
        String fileName = "";
        BaseFtpClient srcClient = null;
        for (int i = 0; i < args.length; i++) {
            if (0 == i) {
                loanSyncConfig = (SyncConfig) args[i];
            } else if (1 == i) {
                fileName = (String) args[i];
            } else if (2 == i) {
                srcClient = (BaseFtpClient) args[i];
            } else {
                break;
            }
        }
        if (srcClient == null || loanSyncConfig == null) {
            log.warn("Upload To MiNio srcClient or loanSyncConfig is null");
            return Boolean.FALSE;
        }
        Object proceed = Boolean.FALSE;
        SyncLog loanSyncLog = setSyncLog(loanSyncConfig, fileName, srcClient);
        try {
            proceed = joinPoint.proceed(args);
            insertSyncLog(loanSyncConfig, fileName, (Boolean) proceed, loanSyncLog);
        } catch (Throwable throwable) {
            log.error("upload To MiNio error", throwable);
        }
        return proceed;
    }

    /**
     * 回传给客户的结果文件，同步完成之后需要更新stra_his_file表中的status字段
     *
     * @param loanSyncConfig 文件同步配置
     * @param fileName       文件名称
     * @param b              文件同步是否成功
     */
    private void updateFileHisStatus(SyncConfig loanSyncConfig, String fileName, boolean b) {
        if(1==loanSyncConfig.getType()||!b||!fileName.endsWith(".zip")){
            return;
        }
        Map<String,String> param=new HashMap<>();
        param.put("apiCode",loanSyncConfig.getApiCode());
        param.put("fileName",fileName);
        loanFileMapper.updateStatus(param);
        log.warn("updateStatus:{}",param);

    }

    /**
     * 设置文件同步日志
     * @param loanSyncConfig 文件同步配置
     * @param fileName 文件名称
     * @return 文件同步日志
     */
    private SyncLog setSyncLog(SyncConfig loanSyncConfig, String fileName, BaseFtpClient srcClient){
        log.info("CopyFileJoinAspect saveSyncLog loanSyncConfig：{}，fileName：{}",loanSyncConfig,fileName);
        SyncLog lsl=new SyncLog();
        String srcPath = loanSyncConfig.getSrcPath();
        String targetPath = loanSyncConfig.getTargetPath();
        String size="";
        String createFileTime="";
        try {
            if(Constants.LOAN_WARNING_SFTP.equals(loanSyncConfig.getSrcType())){
                SftpClient sftpClient = (SftpClient) srcClient;
                SftpATTRS value = sftpClient.stats(srcPath + "/" + fileName);
                size=value.getSize()+"";
                createFileTime=DateHelper.timeStamp2Date(value.getMTime() + "", "yyyy-MM-dd HH:mm:ss");
            }else if(Constants.LOAN_WARNING_FTP.equals(loanSyncConfig.getSrcType())){
                FtpClient ftpClient = (FtpClient) srcClient;
                log.info("realTargetPath:{},fileName:{}",srcPath,fileName);
                FTPFile ftpFile = ftpClient.getFtpFile(srcPath + "/" , fileName);
                Calendar timestamp = ftpFile.getTimestamp();
                createFileTime = DateUtils.parseDateTimeByDate( timestamp.getTime(), "yyyy-MM-dd HH:mm:ss");
                size= ftpFile.getSize()+"";
            }

            lsl.setApiCode(loanSyncConfig.getApiCode());
            lsl.setFileName(fileName);
            lsl.setSrcPath(loanSyncConfig.getSrcSftpHost()+":"+srcPath);
            lsl.setTargetPath(loanSyncConfig.getTargetSftpHost()+":"+targetPath);
            lsl.setFileSize(size);
            lsl.setCreateFileTime(createFileTime);
            lsl.setStartTime(DateUtils.parseDateTimeByDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
            if(loanSyncConfig.getType()==1){
                lsl.setCusBatchNumber(Constants.MYREGEX.split(fileName)[0]);
            }
        } catch (Exception e) {
            log.error("记录文件同步日志出错",e);
        }
        return lsl;
    }

    /**
     * 判断文件同步是否成功
     * @param loanSyncConfig 文件同步配置
     * @param fileName 文件名称
     * @param lsl 文件同步日志
     * @return 文件同步是否成功
     */
    private boolean vaildatorFile(SyncConfig loanSyncConfig, String fileName, SyncLog lsl, BaseFtpClient targetClient){
        log.debug("CopyFileJoinAspect vaildatorFile loanSyncConfig：{}，fileName：{}",loanSyncConfig,fileName);
        String targetPath = loanSyncConfig.getTargetPath();
        InputStream inputStream=null;
        String size="";
        try{
            if(Constants.LOAN_WARNING_SFTP.equals(loanSyncConfig.getTargetType())){
                SftpClient sftpClient = (SftpClient) targetClient;
                SftpATTRS value = sftpClient.stats(targetPath + "/" + fileName);
                size=value.getSize()+"";
            }else if(Constants.LOAN_WARNING_FTP.equals(loanSyncConfig.getTargetType())){
                FtpClient ftpClient = (FtpClient) targetClient;
                log.info("realTargetPath:{},fileName:{}",targetPath,fileName);
                FTPFile ftpFile = ftpClient.getFtpFile(targetPath + "/" , fileName);
                size= ftpFile.getSize()+"";
            }
            if(!size.equals(lsl.getFileSize())){
                log.error("{}文件同步前后大小不一致。前：{}，后：{}",fileName,lsl.getFileSize(), size);
                return false;
            }else if(2==loanSyncConfig.getType()&&Long.parseLong(size)>1){
                inputStream = targetClient.getInputStream(targetPath, fileName);
                String md5 = MyFileUtil.getMd5(inputStream);
                Map<String,String> param=new HashMap<>();
                param.put("apiCode",loanSyncConfig.getApiCode());
                param.put("fileName",fileName);
                LoanFile loanFile = loanFileMapper.queryFilePath(param);
                if(loanFile!=null){
                    if(!md5.equals(loanFile.getMd5())){
                        log.error("{}文件MD5校验失败。前：{}，后：{}",fileName,loanFile.getMd5(), md5);
                        return false;
                    }else {
                        log.warn("{}文件MD5校验成功。前：{}，后：{}",fileName,loanFile.getMd5(), md5);
                    }
                }
            }
        }catch (Exception e){
            log.error("获取同步后目的目录文件信息失败",e);
            return false;
        }finally {
            try {
                if(inputStream!=null){
                    inputStream.close();
                }
                if(Constants.LOAN_WARNING_FTP.equals(loanSyncConfig.getTargetType())){
                    File file=new File(Constants.TMP_FILE_PATH+fileName);
                    if(file.exists()){
                        file.delete();
                    }
                }
            } catch (IOException e) {
                log.error("IOException",e);
            }
        }
        return true;
    }

    /**
     * 设置文件同步状态，文件同步结束时间
     * 写入数据库日志表
     * @param loanSyncConfig 文件同步配置
     * @param fileName 文件名称
     * @param b 文件是否同步成功
     * @param lsl 文件同步日志
     */
    private void insertSyncLog(SyncConfig loanSyncConfig, String fileName, boolean b, SyncLog lsl){
        log.debug("CopyFileJoinAspect updateSyncLog loanSyncConfig：{}，fileName：{}",loanSyncConfig,fileName);
        if(b){
            lsl.setStatus(1);
        }else {
            lsl.setStatus(2);
        }
        lsl.setEndTime(DateUtils.parseDateTimeByDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
        loanSyncLogMapper.insertSynLog(lsl);
    }
}
