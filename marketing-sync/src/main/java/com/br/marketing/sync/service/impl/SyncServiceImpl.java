package com.br.marketing.sync.service.impl;

import com.br.common.util.AESAlgorithmUtil;
import com.br.common.validator.DateUtils;
import com.br.marketing.client.BaseFtpClient;
import com.br.marketing.client.FtpClient;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.SyncConfig;
import com.br.marketing.entity.SyncLog;
import com.br.marketing.mapper.SyncConfigMapper;
import com.br.marketing.mapper.SyncLogMapper;
import com.br.marketing.sync.SyncApplication;
import com.br.marketing.sync.service.SyncService;
import com.jcraft.jsch.SftpATTRS;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.*;

/**
 * The type Sync service.
 */
@Service
@Slf4j
public class SyncServiceImpl implements SyncService {
    /**
     * The Loan sync bean mapper.
     */
    @Resource
    SyncConfigMapper loanSyncConfigMapper;
    /**
     * The Loan sync log mapper.
     */
    @Resource
    SyncLogMapper loanSyncLogMapper;

    @Override
    public void getFromSftp() {
        List<SyncConfig> loanSyncConfigs = loanSyncConfigMapper.queryConfig("1");
        for(SyncConfig loanSyncConfig:loanSyncConfigs){
            log.info("LoanSyncConfig:{}",loanSyncConfig);
            Map<String, List<String>> stringListMap = listFile(loanSyncConfig);
            syncFile(loanSyncConfig,stringListMap);
        }
    }

    @Override
    public void putToSftp() {
        List<SyncConfig> loanSyncConfigs = loanSyncConfigMapper.queryConfig("2");
        for(SyncConfig loanSyncConfig:loanSyncConfigs){
            log.info("LoanSyncConfig:{}",loanSyncConfig);
            Map<String, List<String>> stringListMap = listFile(loanSyncConfig);
            syncFile(loanSyncConfig,stringListMap);
        }
    }

    @Override
    public void insertConfig(SyncConfig loanSyncConfig) {
        String srcSftpPwd = loanSyncConfig.getSrcSftpPwd();
        String targetSftpPwd = loanSyncConfig.getTargetSftpPwd();
        String encryptSrcSftpPwd = AESAlgorithmUtil.encrypt(srcSftpPwd, Constants.SFTP_PWD_SECRET_KEY);
        String encryptTargetSftpPwd = AESAlgorithmUtil.encrypt(targetSftpPwd, Constants.SFTP_PWD_SECRET_KEY);
        loanSyncConfig.setSrcSftpPwd(encryptSrcSftpPwd);
        loanSyncConfig.setTargetSftpPwd(encryptTargetSftpPwd);
        log.warn("loanSyncConfig:{}",loanSyncConfig);
        loanSyncConfigMapper.insertConfig(loanSyncConfig);
    }

    /**
     * 同步文件
     * 根据文件类型同步文件
     * 1.同步时需要根据配置校验标识文件。
     * 下面步骤使用切面完成：
     * 2.同步完成后需要校验源目录与目的目录中文件大小是否一致。
     * 3.同步时需要记录同步日志。
     * @param loanSyncConfig 文件同步配置
     * @param stringListMap 文件名称和文件属性
     */
    private void syncFile(SyncConfig loanSyncConfig, Map<String, List<String>> stringListMap) {
        BaseFtpClient srcClient = getClient(loanSyncConfig,true);
        BaseFtpClient targetClient = getClient(loanSyncConfig,false);
        if(srcClient==null||targetClient==null){
            log.error("targetClient or srcClient is null");
            return;
        }
        if(!srcClient.isConnected()||!targetClient.isConnected()){
            log.error("连接不可用 srcSftpClient.isConnected():{},targetSftpClient.isConnected():{}",srcClient.isConnected(),targetClient.isConnected());
            return;
        }
        String suffixStr = loanSyncConfig.getSuffix();
        List<String> successList = stringListMap.get("success");
        List<String> finishList = stringListMap.get("finish");
        SyncServiceImpl bean = SyncApplication.ac.getBean(SyncServiceImpl.class);
        if(suffixStr.contains(".txt")){
            log.debug("--------------开始同步txt文件---------------");
            List<String> txtList = stringListMap.get("txt");
            if(txtList!=null){
                for(String fileName:txtList){
                    if(checkFinishSuccess(loanSyncConfig,fileName,successList,finishList)){
                        bean.copyFile(loanSyncConfig,fileName,srcClient,targetClient);
                        if(suffixStr.contains(".success")){
                            log.debug("--------------开始同步success文件---------------");
                            String successFile=fileName+".success";
                            bean.copyFile(loanSyncConfig,successFile,srcClient,targetClient);
                        }
                    }
                }
            }
        }

        boolean flag=false;
        if(suffixStr.contains(".zip")){
            log.debug("--------------开始同步zip文件---------------");
            List<String> zipList = stringListMap.get("zip");
            if(zipList!=null){
                for(String fileName:zipList){
                    if(checkFinishSuccess(loanSyncConfig,fileName,successList,finishList)){
                        bean.copyFile(loanSyncConfig,fileName,srcClient,targetClient);
                        if(suffixStr.contains(".success")){
                            log.debug("--------------开始同步success文件---------------");
                            String successFile=fileName+".success";
                            bean.copyFile(loanSyncConfig,successFile,srcClient,targetClient);
                        }
                        flag=true;
                    }
                }
            }
        }

        if(suffixStr.contains(".finish")&&flag){
            log.debug("--------------开始同步finish文件---------------");
            if(finishList!=null){
                for(String fileName:finishList){
                    bean.copyFile(loanSyncConfig,fileName,srcClient,targetClient);
                }
            }
        }
        try {
            srcClient.disconnect();
            targetClient.disconnect();
        } catch (Exception e) {
            log.error("关闭sftp链接出错",e);
        }

    }

    /**
     * 获取sftp链接
     * @param loanSyncConfig sftp配置信息
     * @param isSrc 是否为源地址账号
     * @return SftpClient
     */
    private BaseFtpClient getClient(SyncConfig loanSyncConfig, boolean isSrc){
        BaseFtpClient client= null;
        if(isSrc){
            if(Constants.LOAN_WARNING_FTP.equals(loanSyncConfig.getSrcType())){
                client = new FtpClient(loanSyncConfig, isSrc);
            }else if(Constants.LOAN_WARNING_SFTP.equals(loanSyncConfig.getSrcType())){
                client= new SftpClient(loanSyncConfig,isSrc);
            }
        }else {
            if(Constants.LOAN_WARNING_FTP.equals(loanSyncConfig.getTargetType())){
                client = new FtpClient(loanSyncConfig, isSrc);
            }else if(Constants.LOAN_WARNING_SFTP.equals(loanSyncConfig.getTargetType())){
                client= new SftpClient(loanSyncConfig,isSrc);
            }
        }

        try {
            if(client!=null){
                boolean connect = client.connect();
                if (!connect) {
                    log.error("登录sftp失败 src loanSyncConfig ：{}", loanSyncConfig);
                    return client;
                }
            }
        }catch (Exception e){
            log.error("Exception",e);
        }
        return client;
    }

    /**
     * 拷贝文件。从源目录将指定文件拷贝到目的目录
     * @param loanSyncConfig 同步配置
     * @param fileName 文件名称
     */
    public void copyFile(SyncConfig loanSyncConfig, String fileName, BaseFtpClient srcClient, BaseFtpClient targetClient){
        String dateAddYyMmDd = DateHelper.getDateAddYyMmDd(0);
        String srcPath = loanSyncConfig.getSrcPath();
        String realSrcPath = srcPath.replace("yyyyMMdd", dateAddYyMmDd);
        String targetPath = loanSyncConfig.getTargetPath();
        String realTargetPath = targetPath.replace("yyyyMMdd", dateAddYyMmDd);
        InputStream inputStream=null;
        try{
            targetClient.mkdir(realTargetPath);
            inputStream = srcClient.getInputStream(realSrcPath, fileName);
            targetClient.uploadFile(inputStream,realTargetPath,fileName);
        }catch (Exception e){
            log.error("拷贝文件出错",e);
        }finally {
            try {
                if(inputStream!=null){
                    inputStream.close();
                }
            } catch (Exception e) {
                log.error("关闭流出错",e);
            }
        }
    }

    /**
     * 校验标识文件
     * @param loanSyncConfig 同步配置
     * @param fileName 同步的文件名称
     * @param successList success标识文件列表
     * @param finishList finish标识文件列表
     * @return 校验是否通过
     */
    private boolean checkFinishSuccess(SyncConfig loanSyncConfig, String fileName, List<String> successList, List<String> finishList){
        boolean flag=true;
        boolean deleteMonitor = fileName.indexOf("DeleteMonitor") >= 0;
        if(loanSyncConfig.getCheckFinish()==1) {
            if(finishList==null){
                return false;
            }
            String apiCode = loanSyncConfig.getApiCode();
            String finishFilename="";
            String[] s = fileName.split("\\.");
            if(s.length<2){
                return false;
            }
            String[] names = s[0].split("_");
            log.warn("checkFinishSuccess fileName:{}",fileName);
            String dateAdd = DateHelper.getDateAddYyMmDd(0);
            if(deleteMonitor){
                if(apiCode.equals(Constants.APICODE_360)||apiCode.equals(Constants.APICODE_360_QA)){
                    if(names.length<5){
                        log.warn("checkFinishSuccess fileName:{}",fileName);
                        return false;
                    }
                    finishFilename=names[0]+"_"+names[1]+"_"+names[3]+"_"+names[4]+".finish";
                }
            }else{
                if(apiCode.equals(Constants.APICODE_360)||apiCode.equals(Constants.APICODE_360_QA)){
                    if(1==loanSyncConfig.getType()){
                        if(names.length<4){
                            log.warn("checkFinishSuccess fileName:{}",fileName);
                            return false;
                        }
                        finishFilename=names[0]+"_"+names[1]+"_"+names[3]+".finish";
                    }else if(2==loanSyncConfig.getType()){
                        finishFilename=apiCode+"_UploadCustomFileName"+dateAdd+"_"+dateAdd+".finish";
                    }
                }else{
                    if(1==loanSyncConfig.getType()){
                        if(names.length<3){
                            log.warn("checkFinishSuccess fileName:{}",fileName);
                            return false;
                        }
                        finishFilename=names[0]+"_ReturnCompleted_"+names[2]+".finish";
                    }else if(2==loanSyncConfig.getType()){
                        finishFilename=apiCode+"_ReturnCompleted_"+dateAdd+".finish";
                    }
                }
            }
            if(!finishList.contains(finishFilename)){
                log.warn("finishFilename:{} finishList:{}",finishFilename,finishList);
                flag= false;
            }
        }
        if(loanSyncConfig.getCheckSuccess()==1){
            if(successList==null){
                return false;
            }
            String successFileName=fileName+".success";
            if(!successList.contains(successFileName)){
                log.warn("successFileName:{} successList:{}",successFileName,successList);
                flag= false;
            }
        }
        log.info("fileName:{} checkFinishSuccess result:{}",fileName,flag);
        return flag;
    }





    /**
     * 遍历sftp源目录上需要同步的文件名称
     * @param loanSyncConfig sftp配置信息
     * @return 文件名称列表，按文件类型区分
     */
    private Map<String,List<String>> listFile(SyncConfig loanSyncConfig) {
        Map<String,List<String>> resultMap=new HashMap<>();
        String apiCode = loanSyncConfig.getApiCode();
        BaseFtpClient client = getClient(loanSyncConfig,true);
        if(client==null){
            log.error("config is null");
            return resultMap;
        }
        if(!client.isConnected()){
            log.error("连接不可用 srcSftpClient.isConnected():{}",client.isConnected());
            return resultMap;
        }
        if(Constants.LOAN_WARNING_SFTP.equals(loanSyncConfig.getSrcType())){
            sftpFileList(resultMap,loanSyncConfig, (SftpClient) client,apiCode);
        }else if(Constants.LOAN_WARNING_FTP.equals(loanSyncConfig.getSrcType())){
            ftpFileList(resultMap,loanSyncConfig, (FtpClient) client,apiCode);
        }

        try {
            client.disconnect();
        } catch (Exception e) {
            log.error("断开链接出错",e);
        }
        log.warn("resultMap :{}",resultMap);
        return resultMap;
    }

    private void ftpFileList(Map<String, List<String>> resultMap, SyncConfig loanSyncConfig, FtpClient client, String apiCode) {
        try {
            String srcPath = loanSyncConfig.getSrcPath();
            String realSrcPath = srcPath.replace("yyyyMMdd", DateHelper.getDateAddYyMmDd(0));
            FTPFile[] ftpFiles = client.listFiles(realSrcPath);
            log.warn("realSrcPath{},ftpFiles {}",realSrcPath,ftpFiles.length);
        for(FTPFile file:ftpFiles){
            String fileName = file.getName();
            Calendar timestamp = file.getTimestamp();
            String createFileTime = DateUtils.parseDateTimeByDate( timestamp.getTime(), "yyyy-MM-dd HH:mm:ss");
            log.info("fileName:{},size:{},time:{}",fileName,file.getSize(),createFileTime);
            if(vaildExclusionTime(createFileTime,loanSyncConfig)){
                log.info("历史文件，不处理{},{}",fileName,createFileTime);
                continue;
            }
            String s1 = DateUtils.parseDateTimeByDate(new Date(), "yyyy-MM-dd HH:mm:ss");
            long[] distanceTimes = DateHelper.getDistanceTimes(createFileTime, s1);
            long day = 0;
            long hour = 0;
            long min = 0;
            for(int i=0;i<distanceTimes.length;i++){
                if(i==0){
                    day = distanceTimes[i];
                }
                if(i==1){
                    hour = distanceTimes[i];
                }
                if(i==2){
                    min = distanceTimes[i];
                }
            }
            if(day==0&&hour==0&&min<1){
                log.warn("文件上传时间距离当前时间小于1分钟，暂时不处理{},{}",fileName,createFileTime);
                continue;
            }
            Map<String,String> params=new HashMap<>();
            params.put("apiCode",apiCode);
            params.put("fileName",fileName);
            params.put("createFileTime",createFileTime);
            List<SyncLog> syncLogs=  loanSyncLogMapper.querySyncLog(params);
            if(syncLogs==null||syncLogs.size()<=0){
                String[] split = fileName.split("\\.");
                if(split.length>1){
                    String suf = split[split.length-1];
                    List<String> list = resultMap.get(suf);
                    if(list==null){
                        list=new ArrayList<>();
                        resultMap.put(suf,list);
                    }
                    list.add(fileName);
                }else {
                    log.warn("error fileName :{}",fileName);
                }
            }
        }
        } catch (Exception e) {
            log.error("遍历ftp文件出错",e);
        }
    }


    private void sftpFileList(Map<String,List<String>> resultMap, SyncConfig loanSyncConfig, SftpClient client, String apiCode){
        try {
            String srcPath = loanSyncConfig.getSrcPath();
            String realSrcPath = srcPath.replace("yyyyMMdd", DateHelper.getDateAddYyMmDd(0));
            Map<String, SftpATTRS> map = client.listFiles(realSrcPath);
            log.warn("realSrcPath:{} map key:{}",realSrcPath,map.keySet());
            for(Map.Entry<String, SftpATTRS> entry : map.entrySet()){
                String fileName = entry.getKey();
                SftpATTRS attrs = entry.getValue();

                log.info("sftp filename：{} Atime:{},size:{},atTime:{},Extended:{},Flags:{},gid:{},mTime:{},MtimeString:{}," +
                                "Permissions:{},PermissionsString:{},uid:{}"
                        ,fileName,attrs.getAtimeString(),attrs.getSize(),attrs.getATime()
                        ,attrs.getExtended(),attrs.getFlags(),attrs.getGId(),attrs.getMTime()
                        ,attrs.getMtimeString(),attrs.getPermissions(),attrs.getPermissionsString(),attrs.getUId());

                String createFileTime = DateHelper.timeStamp2Date(attrs.getMTime() + "", "yyyy-MM-dd HH:mm:ss");
                if(vaildExclusionTime(createFileTime,loanSyncConfig)){
                    log.warn("历史文件，不处理{},{}",fileName,createFileTime);
                    continue;
                }
                String s1 = DateUtils.parseDateTimeByDate(new Date(), "yyyy-MM-dd HH:mm:ss");
                long[] distanceTimes = DateHelper.getDistanceTimes(createFileTime, s1);
                long day = 0;
                long hour = 0;
                long min = 0;
                for(int i=0;i<distanceTimes.length;i++){
                    if(i==0){
                        day = distanceTimes[i];
                    }
                    if(i==1){
                        hour = distanceTimes[i];
                    }
                    if(i==2){
                        min = distanceTimes[i];
                    }
                }
                if(day==0&&hour==0&&min<1){
                    log.warn("文件上传时间距离当前时间小于1分钟，暂时不处理{},{}",fileName,createFileTime);
                    continue;
                }

                Map<String,String> params=new HashMap<>();
                params.put("apiCode",apiCode);
                params.put("fileName",fileName);
                params.put("createFileTime",createFileTime);
                List<SyncLog> syncLogs=  loanSyncLogMapper.querySyncLog(params);
                //log.warn("params:{},syncLogs:{}",params,syncLogs.size());
                if(syncLogs==null||syncLogs.size()<=0){
                    String[] split = fileName.split("\\.");
                    if(split.length>1){
                        String suf = split[split.length-1];
                        List<String> list = resultMap.get(suf);
                        if(list==null){
                            list=new ArrayList<>();
                            resultMap.put(suf,list);
                        }
                        list.add(fileName);
                    }else {
                        log.warn("error fileName :{}",fileName);
                    }
                }
            }
        } catch (Exception e) {
            log.error("遍历sftp文件出错",e);
        }
    }

    /**
     * 校验排除日期
     * 主要是钱以后历史的文件不需要同步
     * @param createFileTime 文件生成日期
     * @param loanSyncConfig 任务配置
     * @return
     */
    private boolean vaildExclusionTime(String createFileTime, SyncConfig loanSyncConfig){
        if(StringUtils.isEmpty(loanSyncConfig.getExclusionTime())){
            return false;
        }
        try {
            long distanceDays = DateHelper.getDistanceDays(createFileTime, loanSyncConfig.getExclusionTime());
            if(distanceDays>0){
                return true;
            }
        } catch (Exception e) {
           log.warn("Exception",e);
        }
        return false;
    }
    public static void main(String[] args) {
      BaseFtpClient  client = new FtpClient(new SyncConfig(), false);
        System.out.println(client);
    }
}
