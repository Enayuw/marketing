package com.br.marketing.sync.service.impl;

import com.br.common.util.AESAlgorithmUtil;
import com.br.common.validator.DateUtils;
import com.br.marketing.client.BaseFtpClient;
import com.br.marketing.client.FtpClient;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingCleanDataFile;
import com.br.marketing.entity.SyncConfig;
import com.br.marketing.entity.SyncLog;
import com.br.marketing.mapper.MarketingCleanDataFileMapper;
import com.br.marketing.mapper.SyncConfigMapper;
import com.br.marketing.mapper.SyncLogMapper;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.sync.SyncApplication;
import com.br.marketing.sync.service.SyncService;
import com.jcraft.jsch.SftpATTRS;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.security.MessageDigest;
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

    @Resource
    private SyncConfigService syncConfigService;

    @Resource
    private MarketingCleanDataFileMapper marketingCleanDataFileMapper;

    @Override
    public void getFromSftp() {
        List<SyncConfig> loanSyncConfigs = loanSyncConfigMapper.queryConfigByTypeAndTargetType("1"
                , Arrays.asList(Constants.LOAN_WARNING_FTP, Constants.LOAN_WARNING_SFTP));
        sync(loanSyncConfigs);
        List<SyncConfig> syncConfigs = loanSyncConfigMapper.queryConfigByTypeAndTargetType("1", Arrays.asList(Constants.LOAN_DISK));
        sync(syncConfigs);
    }

    @Override
    public void putToSftp() {
        List<SyncConfig> loanSyncConfigs = loanSyncConfigMapper.queryConfig("2");
        sync(loanSyncConfigs);
    }

    @Override
    public void insertConfig(SyncConfig loanSyncConfig) {
        String srcSftpPwd = loanSyncConfig.getSrcSftpPwd();
        String targetSftpPwd = loanSyncConfig.getTargetSftpPwd();
        String encryptSrcSftpPwd = AESAlgorithmUtil.encrypt(srcSftpPwd, Constants.SFTP_P_SECRET_KEY);
        String encryptTargetSftpPwd = AESAlgorithmUtil.encrypt(targetSftpPwd, Constants.SFTP_P_SECRET_KEY);
        loanSyncConfig.setSrcSftpPwd(encryptSrcSftpPwd);
        loanSyncConfig.setTargetSftpPwd(encryptTargetSftpPwd);
        log.warn("loanSyncConfig:{}",loanSyncConfig);
        loanSyncConfigMapper.insertConfig(loanSyncConfig);
    }

    public void sync(List<SyncConfig> loanSyncConfigs){
        //当前时间减1小时，目的在于防止跨天情况，导致文件无法同步问题；
        Set<String> dateSet =new TreeSet<>();
        dateSet.add(DateHelper.getDateByMinute(-60));
        dateSet.add(DateHelper.getDateAddYyMmDd(0));
        for(SyncConfig loanSyncConfig:loanSyncConfigs){
            log.info("LoanSyncConfig:{}",loanSyncConfig);
            String srcPath = loanSyncConfig.getSrcPath();
            String targetPath = loanSyncConfig.getTargetPath();
            for (String date : dateSet) {
                loanSyncConfig.setSrcPath(srcPath.replace("yyyyMMdd", date));
                loanSyncConfig.setTargetPath(targetPath.replace("yyyyMMdd", date));
                Map<String, List<String>> stringListMap = listFile(loanSyncConfig);
                syncFile(loanSyncConfig,stringListMap,date);
            }
        }
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
    private void syncFile(SyncConfig loanSyncConfig, Map<String, List<String>> stringListMap,String date) {
        BaseFtpClient srcClient = getClient(loanSyncConfig, true);
        BaseFtpClient targetClient = getClient(loanSyncConfig, false);
        boolean diskBoll = Constants.LOAN_DISK.equals(loanSyncConfig.getTargetType());
        if (srcClient == null || (!diskBoll && targetClient == null)) {
            try {
                if (srcClient != null) {
                    srcClient.disconnect();
                }
                if (targetClient != null) {
                    targetClient.disconnect();
                }
            } catch (Exception ex) {
                log.error("targetClient or srcClient disconnect" + ex.getMessage(), ex);
            }
            log.error("targetClient or srcClient is null");
            return;
        }
        if (!srcClient.isConnected() || (!diskBoll && !targetClient.isConnected())) {
            log.error("连接不可用 srcSftpClient.isConnected():{},targetSftpClient.isConnected():{}", srcClient.isConnected(), targetClient.isConnected());
            return;
        }
        String suffixStr = loanSyncConfig.getSuffix();
        List<String> successList = stringListMap.get("success");
        List<String> finishList = stringListMap.get("finish");
        SyncServiceImpl bean = SyncApplication.ac.getBean(SyncServiceImpl.class);
        if(suffixStr.contains(".txt")){
            log.info("--------------开始同步txt文件---------------");
            List<String> txtList = stringListMap.get("txt");
            if(txtList!=null){
                for(String fileName:txtList){
                    if(checkFinishSuccess(loanSyncConfig,fileName,successList,finishList,date)){
                        if (bean.downloadFileToLocalDisk(loanSyncConfig, srcClient, fileName)) {
                            continue;
                        }
                        bean.copyFile(loanSyncConfig, fileName, srcClient, targetClient);
                        if (suffixStr.contains(".success")) {
                            log.info("--------------开始同步success文件---------------");
                            String successFile = fileName + ".success";

                            bean.copyFile(loanSyncConfig, successFile, srcClient, targetClient);
                        }
                    }
                }
            }
        }

        if(suffixStr.contains(".csv")){
            log.info("--------------开始同步csv文件---------------");
            List<String> txtList = stringListMap.get("csv");
            if(txtList!=null){
                for(String fileName:txtList){
                    if(checkFinishSuccess(loanSyncConfig,fileName,successList,finishList,date)) {
                        if (bean.downloadFileToLocalDisk(loanSyncConfig, srcClient, fileName)) {
                            continue;
                        }
                        bean.copyFile(loanSyncConfig, fileName, srcClient, targetClient);
                        if (suffixStr.contains(".success")) {
                            log.info("--------------开始同步success文件---------------");
                            String successFile = fileName + ".success";
                            bean.copyFile(loanSyncConfig, successFile, srcClient, targetClient);
                        }
                    }
                }
            }
        }

        boolean flag=false;
        if(suffixStr.contains(".zip")){
            log.info("--------------开始同步zip文件---------------");
            List<String> zipList = stringListMap.get("zip");
            if(zipList!=null){
                for(String fileName:zipList){
                    if(checkFinishSuccess(loanSyncConfig,fileName,successList,finishList,date)) {
                        if (bean.downloadFileToLocalDisk(loanSyncConfig, srcClient, fileName)) {
                            continue;
                        }
                        bean.copyFile(loanSyncConfig, fileName, srcClient, targetClient);
                        if (suffixStr.contains(".success")) {
                            log.info("--------------开始同步success文件---------------");
                            String successFile = fileName + ".success";
                            bean.copyFile(loanSyncConfig, successFile, srcClient, targetClient);
                        }
                        flag = true;
                    }
                }
            }
        }

        if(suffixStr.contains(".finish")&&flag){
            log.info("--------------开始同步finish文件---------------");
            if(finishList!=null){
                for(String fileName:finishList) {
                    if (bean.downloadFileToLocalDisk(loanSyncConfig, srcClient, fileName)) {
                        continue;
                    }
                    bean.copyFile(loanSyncConfig, fileName, srcClient, targetClient);
                }
            }
        }
        try {
            srcClient.disconnect();
            if (diskBoll) {
                return;
            }
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

        String srcPath = loanSyncConfig.getSrcPath();
        String targetPath = loanSyncConfig.getTargetPath();
        InputStream inputStream=null;
        try{
            targetClient.mkdir(targetPath);
            inputStream = srcClient.getInputStream(srcPath, fileName);
            targetClient.uploadFile(inputStream,targetPath,fileName);
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
    private boolean checkFinishSuccess(SyncConfig loanSyncConfig, String fileName, List<String> successList, List<String> finishList,String date){
        boolean flag=true;
        if(loanSyncConfig.getCheckFinish()==1) {
            if(finishList==null){
                return false;
            }
            String apiCode = loanSyncConfig.getApiCode();

            String[] s = fileName.split("\\.");
            if(s.length<2){
                return false;
            }
            String finishName="";
            String[] names = s[0].split("_");
            log.warn("checkFinishSuccess fileName:{}",fileName);
            if(1==loanSyncConfig.getType()){
                if(names.length<3){
                    log.warn("checkFinishSuccess fileName:{}",fileName);
                    return false;
                }
                finishName=names[0]+"_ReturnCompleted_"+names[2]+".finish";
            }else if(2==loanSyncConfig.getType()){
                finishName=apiCode + "_ReturnCompleted_" + date + ".finish";
            }
            if(!finishList.contains(finishName)){
                log.warn("finishFilename:{} finishList:{}",finishName,finishList);
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
            FTPFile[] ftpFiles = client.listFiles(srcPath);
            log.warn("FTP同步路径:{},该路径下文件有:{}个",srcPath,ftpFiles.length);
            for(FTPFile file:ftpFiles){
                String fileName = file.getName();
                Calendar timestamp = file.getTimestamp();
                String createFileTime = DateUtils.parseDateTimeByDate( timestamp.getTime(), "yyyy-MM-dd HH:mm:ss");
                log.info("fileName:{},size:{},time:{}",fileName,file.getSize(),createFileTime);
                if(vaildExclusionTime(createFileTime,loanSyncConfig)){
                    log.info("历史文件，不处理{},{}",fileName,createFileTime);
                    continue;
                }
                validateIsSync(createFileTime,fileName,apiCode,resultMap,loanSyncConfig);
            }
        } catch (Exception e) {
            log.error("遍历ftp文件出错",e);
        }
    }


    private void sftpFileList(Map<String,List<String>> resultMap, SyncConfig loanSyncConfig, SftpClient client, String apiCode){
            try {
                String srcPath = loanSyncConfig.getSrcPath();
                Map<String, SftpATTRS> map = client.listFiles(srcPath);
                log.warn("SFTP同步路径:{},该路径下文件有:{}个",srcPath,map.keySet().size());
                for(Map.Entry<String, SftpATTRS> entry : map.entrySet()){
                    String fileName = entry.getKey();
                    SftpATTRS attrs = entry.getValue();
                    String createFileTime = DateHelper.timeStamp2Date(attrs.getMTime() + "", "yyyy-MM-dd HH:mm:ss");
                    if(vaildExclusionTime(createFileTime,loanSyncConfig)){
                        log.warn("历史文件，不处理{},{}",fileName,createFileTime);
                        continue;
                    }
                    validateIsSync(createFileTime,fileName,apiCode,resultMap,loanSyncConfig);
                }
            } catch (Exception e) {
                log.error("遍历sftp文件出错",e);
            }
    }

    /**
     * 校验文件是否需要同步，如果需要，检查是否已经同步过，然后放到map中
     * @param createFileTime 文件创建时间
     * @param fileName 文件名
     * @param apiCode apiCode
     * @param resultMap  文件数据集合
     */
    private void validateIsSync(String createFileTime,String fileName,String apiCode,Map<String, List<String>> resultMap
            ,SyncConfig syncConfig){
        long minutes = DateHelper.getDistanceMinutes(createFileTime);
        if(minutes<1){
            log.warn("文件上传时间距离当前时间小于1分钟，暂时不处理{},{}",fileName,createFileTime);
            return;
        }
        Map<String,String> params=new HashMap<>();
        params.put("apiCode",apiCode);
        params.put("fileName",fileName);
//        params.put("createFileTime",createFileTime);
        params.put("srcPath",syncConfig.getSrcSftpHost().concat(":").concat(syncConfig.getSrcPath()));
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
            if (distanceDays > 0) {
                return true;
            }
        } catch (Exception e) {
            log.warn("Exception", e);
        }
        return false;
    }

    /**
     * 2024-08-08 22:28
     * 下载远程文件到本地
     *
     * @param loanSyncConfig 远程sftp配置
     * @param srcClient      远程客户端
     * @param fileName       文件名称
     * @return true 下载到本地
     */
    public Boolean downloadFileToLocalDisk(SyncConfig loanSyncConfig, BaseFtpClient srcClient, String fileName) {
        String targetType = loanSyncConfig.getTargetType();
        String targetSftpHost = loanSyncConfig.getTargetSftpHost();
        String targetSftpPwd = loanSyncConfig.getTargetSftpPwd();
        String targetSftpUser = loanSyncConfig.getTargetSftpUser();
        Integer targetSftpPort = loanSyncConfig.getTargetSftpPort();
        // 未配置目标资源信息及目标类型为“localDisk”默认本地下载
        boolean bool = StringUtils.isBlank(targetSftpHost)
                || StringUtils.isBlank(targetSftpPwd)
                || StringUtils.isBlank(targetSftpUser)
                || targetSftpPort == null
                || targetSftpPort < 1
                || Constants.LOAN_DISK.equals(targetType);
        String targetPath;
        // 判断本地路径是否正常
        if (bool) {
            targetPath = loanSyncConfig.getTargetPath();
            if (StringUtils.isBlank(targetPath)) {
                log.warn("远程文件下载到本地，本地目录不存在，目录：{}", targetPath);
                return Boolean.TRUE;
            }
            String srcPath = loanSyncConfig.getSrcPath();
            InputStream inputStream = null;
            ReadableByteChannel readableByteChannel = null;
            WritableByteChannel writableByteChannel = null;
            // jvm堆外内存
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024 << 1);
            try {
                File dir = new File(targetPath);
                // 判断路径
                if (!dir.exists()) {
                    if (!dir.mkdirs()) {
                        log.warn("下载远程客户文件路径创建失败：{}", dir.getAbsolutePath());
                    }
                }
                String fileNamePath = targetPath + File.separator + fileName;
                File file = new File(fileNamePath);
                if (file.exists()) {
                    boolean b = file.renameTo(new File(fileName.concat(".bak" + System.currentTimeMillis())));
                    if (!b) {
                        log.warn("{}文件重命名失败！", fileNamePath);
                    }
                }
                inputStream = srcClient.getInputStream(srcPath, fileName);
                readableByteChannel = Channels.newChannel(inputStream);
                writableByteChannel = Channels.newChannel(new FileOutputStream(file));
                MessageDigest md = MessageDigest.getInstance("MD5");
                // 文件内容读取
                while (readableByteChannel.read(byteBuffer) != -1) {
                    byteBuffer.flip();
                    ByteBuffer duplicate = byteBuffer.duplicate();
                    writableByteChannel.write(byteBuffer);
                    md.update(duplicate);
                    byteBuffer.clear();
                }
                // 获取MD5值生成
                String md5Value = DatatypeConverter.printHexBinary(md.digest());
                // 保存文件信息
                return saveDataFileInfo(fileName, loanSyncConfig, targetPath, srcPath, md5Value);
            } catch (Exception e) {
                log.warn("文件下载错误文件出错！srcPath:{},fileName:{},targetPath{},syncConfigId:{}"
                        , srcPath, fileName, targetPath, loanSyncConfig.getId(), e);
            } finally {
                byteBuffer.clear();
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        log.warn(e.getMessage(), e);
                    }
                }
                if (readableByteChannel != null) {
                    try {
                        readableByteChannel.close();
                    } catch (IOException e) {
                        log.warn(e.getMessage(), e);
                    }
                }
                if (writableByteChannel != null) {
                    try {
                        writableByteChannel.close();
                    } catch (IOException e) {
                        log.warn(e.getMessage(), e);
                    }
                }
            }
        } else {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * 2024-08-08 22:35
     * 保存文件信息
     *
     * @param fileName       文件名
     * @param loanSyncConfig sftp配置信息
     * @param targetPath     目标目录
     * @param srcPath        源目录
     * @param md5Value       md5
     */
    private Boolean saveDataFileInfo(String fileName, SyncConfig loanSyncConfig
            , String targetPath, String srcPath, String md5Value) {
        MarketingCleanDataFile dataFile = new MarketingCleanDataFile();
        dataFile.setFileName(fileName);
        dataFile.setApiCode(loanSyncConfig.getApiCode());
        dataFile.setCreateTime(new Date());
        dataFile.setLocalPath(targetPath);
        dataFile.setUpdateTime(new Date());
        dataFile.setTargetSftpPath(srcPath);
        dataFile.setMd5Value(md5Value);
        dataFile.setSyncConfigId(loanSyncConfig.getId());
        int i = marketingCleanDataFileMapper.insertSelective(dataFile);
        if (i < 1) {
            log.warn("清洗文件新增下载失败！fileName:{},targetPath:{},srcPath:{},md5Value:{},syncConfigId:{}"
                    , fileName, targetPath, srcPath, md5Value, loanSyncConfig.getId());
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

}
