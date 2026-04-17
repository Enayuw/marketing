package com.br.marketing.sync.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.br.common.util.AESAlgorithmUtil;
import com.br.common.validator.DateUtils;
import com.br.marketing.client.BaseFtpClient;
import com.br.marketing.client.FtpClient;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.enums.DataTypeEnum;
import com.br.marketing.common.enums.ExecuteTimeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.common.utils.file.ZipUtils;
import com.br.marketing.entity.MarketingCleanDataFile;
import com.br.marketing.entity.SyncConfig;
import com.br.marketing.entity.SyncLog;
import com.br.marketing.enums.sync.SyncConfigIsUnzipEnum;
import com.br.marketing.enums.sync.UnzipFilenameCharsetEnum;
import com.br.marketing.enums.SyncConfigCustomizedTypeEnum;
import com.br.marketing.enums.file.FileServerType;
import com.br.marketing.mapper.MarketingCleanDataFileMapper;
import com.br.marketing.mapper.SyncConfigMapper;
import com.br.marketing.mapper.SyncLogMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.sync.SyncApplication;
import com.br.marketing.sync.service.ShuHeCustomizedSyncService;
import com.br.marketing.sync.service.SyncService;
import com.jcraft.jsch.SftpATTRS;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.beans.BeanUtils;
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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    private ShuHeCustomizedSyncService shuHeCustomizedSyncService;

    @Resource
    private MarketingCleanDataFileMapper marketingCleanDataFileMapper;

    @Resource
    MarketingCommonConfig marketingCommonConfig;

    @Resource
    MinioFileService minioFileService;

    private static final String TITLE = "【文件同步】";

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
        for(SyncConfig loanSyncConfig:loanSyncConfigs){
            try {
                log.warn(TITLE + "开始处理配置 - apiCode:{}, id:{}", loanSyncConfig.getApiCode(), loanSyncConfig.getId());

                Set<String> dateSet = new TreeSet<>();
                Integer dataType = loanSyncConfig.getDataType();

                if(Objects.equals(dataType, DataTypeEnum.SYNC_FILES.getValue())){
                    String executeTime = loanSyncConfig.getExecuteTime();
                    if(executeTime != null){
                        JSONObject jsonObject = JSONObject.parseObject(executeTime);
                        String day = jsonObject.getString("day");
                        String time = jsonObject.getString("time");

                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                        String currentTime = sdf.format(new Date());

                        // 判断当前时间是否大于executeTime执行时间，如果小于则跳过执行
                        if(currentTime.compareTo(time) < 0){
                            log.warn(TITLE + "当前时间{}小于执行时间{}，跳过同步任务，apiCode:{}", currentTime, time, loanSyncConfig.getApiCode());
                            continue;
                        }
                        log.warn(TITLE + "当前时间{}大于等于执行时间{}，继续执行同步任务，apiCode:{}", currentTime, time, loanSyncConfig.getApiCode());

                        // 根据day值决定要拉取的文件日期
                        if(Objects.equals(day, ExecuteTimeEnum.YESTERDAY.getValue())){
                            // day=0: T-1，拉取昨天的文件
                            LocalDate yesterday = LocalDate.now().minusDays(1);
                            String format = yesterday.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                            dateSet.add(format);
                            log.warn(TITLE + "day=0，拉取昨天的文件，日期：{}", format);
                        } else {
                            // 默认逻辑：当前时间减1小时，目的在于防止跨天情况，导致文件无法同步问题；
                            dateSet.add(DateHelper.getDateByMinute(-60));
                            dateSet.add(DateHelper.getDateAddYyMmDd(0));
                        }
                    } else {
                        log.warn(TITLE + "未配置执行时间,loanSyncConfigID：{}",loanSyncConfig.getId());
                       continue;
                    }
                } else {
                    // 非同步文件类型，使用原有逻辑
                    //当前时间减1小时，目的在于防止跨天情况，导致文件无法同步问题；
                    dateSet.add(DateHelper.getDateByMinute(-60));
                    dateSet.add(DateHelper.getDateAddYyMmDd(0));
                }
                String srcPath = loanSyncConfig.getSrcPath();
                String targetPath = loanSyncConfig.getTargetPath();
                for (String date : dateSet) {
                    // 根据路径格式转换日期格式
                    String formattedDate;
                    if (srcPath.contains("yyyy-MM-dd")) {
                        // 将yyyyMMdd格式转换为yyyy-MM-dd格式
                        formattedDate = formatDate(date, "yyyyMMdd", "yyyy-MM-dd");
                        loanSyncConfig.setSrcPath(srcPath.replace("yyyy-MM-dd", formattedDate));
                    } else {
                        loanSyncConfig.setSrcPath(srcPath.replace("yyyyMMdd", date));
                    }
                    if (targetPath.contains("yyyy-MM-dd")) {
                        // 将yyyyMMdd格式转换为yyyy-MM-dd格式
                        formattedDate = formatDate(date, "yyyyMMdd", "yyyy-MM-dd");
                        loanSyncConfig.setTargetPath(targetPath.replace("yyyy-MM-dd", formattedDate));
                    } else {
                        loanSyncConfig.setTargetPath(targetPath.replace("yyyyMMdd", date));
                    }

                    Map<String, List<String>> stringListMap = listFile(loanSyncConfig);
                    syncFile(loanSyncConfig,stringListMap,date);
                }
                log.warn(TITLE + "配置处理完成 - apiCode:{}, id:{}", loanSyncConfig.getApiCode(), loanSyncConfig.getId());
            } catch (Exception configException) {
                log.error(TITLE + "处理配置时出现异常 - apiCode:{}, id:{}, 错误:{}, 继续处理下一个配置",
                        loanSyncConfig.getApiCode(), loanSyncConfig.getId(), configException.getMessage(), configException);
                // 继续处理下一个配置
            }
        }
    }

    /**
     * 日期格式转换
     * @param date 原始日期字符串
     * @param sourceFormat 源格式
     * @param targetFormat 目标格式
     * @return 转换后的日期字符串
     */
    private String formatDate(String date, String sourceFormat, String targetFormat) {
        try {
            SimpleDateFormat sourceFormatter = new SimpleDateFormat(sourceFormat);
            SimpleDateFormat targetFormatter = new SimpleDateFormat(targetFormat);
            return targetFormatter.format(sourceFormatter.parse(date));
        } catch (Exception e) {
            log.error("日期格式转换失败, date: {}, sourceFormat: {}, targetFormat: {}",
                    date, sourceFormat, targetFormat, e);
            return date;
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
    private void syncFile(SyncConfig loanSyncConfig, Map<String, List<String>> stringListMap,String date) throws Exception {
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

        switch (SyncConfigCustomizedTypeEnum.getEnumByCode(loanSyncConfig.getCustomizedType())) {
            case DEFAULT:
                defaultSync(loanSyncConfig, stringListMap, date, diskBoll, srcClient, targetClient);
                break;
            case SHUHE_AUTO_MATCH_DATA:
                shuHeCustomizedSyncService.syncFile(loanSyncConfig, srcClient, targetClient);
                break;
            default:
                break;
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

    private void defaultSync(SyncConfig loanSyncConfig, Map<String, List<String>> stringListMap, String date, boolean diskBoll,
                             BaseFtpClient srcClient, BaseFtpClient targetClient) throws Exception {
        String suffixStr = loanSyncConfig.getSuffix();
        List<String> successList = stringListMap.get("success");
        List<String> finishList = stringListMap.get("finish");
        SyncServiceImpl bean = SyncApplication.ac.getBean(SyncServiceImpl.class);
        if(suffixStr.contains(".txt")){
            log.info("--------------开始同步txt文件---------------");
            List<String> txtList = stringListMap.get("txt");
            if(txtList!=null){
                for(String fileName:txtList){
                    if(checkFinishSuccess(loanSyncConfig,fileName,successList,finishList, date)){
                        if (diskBoll && bean.downloadFileToLocalDisk(loanSyncConfig, srcClient, fileName)) {
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
                    if(checkFinishSuccess(loanSyncConfig,fileName,successList,finishList, date)) {
                        if (diskBoll && bean.downloadFileToLocalDisk(loanSyncConfig, srcClient, fileName)) {
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
                    if(checkFinishSuccess(loanSyncConfig,fileName,successList,finishList, date)) {
                        if (diskBoll && bean.downloadFileToLocalDisk(loanSyncConfig, srcClient, fileName)) {
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
                    if (diskBoll && bean.downloadFileToLocalDisk(loanSyncConfig, srcClient, fileName)) {
                        continue;
                    }
                    bean.copyFile(loanSyncConfig, fileName, srcClient, targetClient);
                }
            }
        }

        // 处理no_suffix类型：同步所有已过滤的文件
        if("no_suffix".equals(suffixStr)){
            log.info("--------------开始同步no_suffix类型（所有文件）---------------");

            Map<String, Boolean> sftpMockAbnormal = marketingCommonConfig.getSftpMockAbnormal();
            Boolean b = sftpMockAbnormal.get(loanSyncConfig.getApiCode());
            if(b != null && b){
                log.warn(TITLE+ "Mock异常: {}", JSONObject.toJSONString(sftpMockAbnormal));
                throw new Exception();
            }

            List<String> noSuffixList = stringListMap.get("no_suffix");
            if(noSuffixList!=null){
                for(String pathAndFileName:noSuffixList){
                    // 解析完整路径和文件名，格式为 "路径|文件名"
                    String[] parts = pathAndFileName.split("\\|");
                    if(parts.length != 2){
                        log.warn(TITLE+ "no_suffix类型-路径格式错误，跳过: {}", pathAndFileName);
                        continue;
                    }
                    
                    String filePath = parts[0];
                    String fileName = parts[1];

                    SyncConfig syncConfig = new SyncConfig();
                    BeanUtils.copyProperties(loanSyncConfig, syncConfig);
                    // 计算目标路径，保持与源路径相同的目录结构
                    String targetPath = calculateTargetPath(loanSyncConfig.getSrcPath(), loanSyncConfig.getTargetPath(), filePath);

                    syncConfig.setSrcPath(filePath);
                    syncConfig.setTargetPath(targetPath);
                    log.warn(TITLE+ "no_suffix类型-开始同步文件: {} 从路径: {} 到路径: {}", fileName, filePath, targetPath);
                    
                    // no_suffix类型不需要checkFinishSuccess检查，直接同步所有文件
                    if (diskBoll && bean.downloadFileToLocalDisk(syncConfig, srcClient, fileName)) {
                        continue;
                    }
                    //minio的上传
                    if (FileServerType.MINIO.getServerType().equals(syncConfig.getTargetType())) {
                        sftpFileUploadToMiNio(syncConfig, fileName, srcClient);
                    } else {
                        bean.copyFile(syncConfig, fileName, srcClient, targetClient);
                    }
                    log.warn(TITLE+ "no_suffix类型-文件同步完成: {}", fileName);
                }
            }
        }
    }

    public Boolean sftpFileUploadToMiNio(SyncConfig loanSyncConfig, String fileName, BaseFtpClient srcClient) {
        InputStream inputStream = null;
        String srcPath = loanSyncConfig.getSrcPath().endsWith("/") ? loanSyncConfig.getSrcPath() : loanSyncConfig.getSrcPath() + "/";
        String targetPath = loanSyncConfig.getTargetPath().endsWith("/") ? loanSyncConfig.getTargetPath() : loanSyncConfig.getTargetPath() + "/";

        try {
            inputStream = srcClient.getInputStream(srcPath, fileName);
            minioFileService.uploadFile(inputStream, targetPath.concat(fileName));
            return Boolean.TRUE;
        } catch (Exception e) {
            log.error("拷贝文件出错", e);
            return Boolean.FALSE;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                log.error("关闭流出错", e);
            }
        }
    }

    /**
     * 计算目标路径，保持与源路径相同的目录结构
     * @param originalSrcPath 原始源路径
     * @param originalTargetPath 原始目标路径
     * @param currentFilePath 当前文件的实际路径
     * @return 计算后的目标路径
     */
    private String calculateTargetPath(String originalSrcPath, String originalTargetPath, String currentFilePath) {
        try {
            // 确保路径以/结尾，便于计算相对路径
            String baseSrcPath = originalSrcPath.endsWith("/") ? originalSrcPath : originalSrcPath + "/";
            String baseTargetPath = originalTargetPath.endsWith("/") ? originalTargetPath : originalTargetPath + "/";
            
            // 如果当前文件路径就是原始源路径，直接返回原始目标路径
            if (currentFilePath.equals(originalSrcPath)) {
                return originalTargetPath;
            }
            
            // 计算相对路径
            if (currentFilePath.startsWith(baseSrcPath)) {
                String relativePath = currentFilePath.substring(baseSrcPath.length());
                String targetPath = baseTargetPath + relativePath;
                log.warn(TITLE+ "计算目标路径: {} -> {}", currentFilePath, targetPath);
                return targetPath;
            } else {
                log.warn(TITLE+ "文件路径{}不在源路径{}下，使用原始目标路径", currentFilePath, originalSrcPath);
                return originalTargetPath;
            }
        } catch (Exception e) {
            log.error(TITLE+ "计算目标路径失败，使用原始目标路径. originalSrcPath: {}, currentFilePath: {}", originalSrcPath, currentFilePath, e);
            return originalTargetPath;
        }
    }

    /**
     * 获取sftp链接
     * @param loanSyncConfig sftp配置信息
     * @param isSrc 是否为源地址账号
     * @return SftpClient
     */
    public BaseFtpClient getClient(SyncConfig loanSyncConfig, boolean isSrc){
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
     * @return 拷贝是否成功
     */
    public boolean copyFile(SyncConfig loanSyncConfig, String fileName, BaseFtpClient srcClient, BaseFtpClient targetClient){
        String srcPath = loanSyncConfig.getSrcPath().endsWith("/") ? loanSyncConfig.getSrcPath() : loanSyncConfig.getSrcPath() + "/";
        String targetPath = loanSyncConfig.getTargetPath().endsWith("/") ? loanSyncConfig.getTargetPath() : loanSyncConfig.getTargetPath() + "/";
        InputStream inputStream = null;
        try {
            targetClient.mkdir(targetPath);
            inputStream = srcClient.getInputStream(srcPath, fileName);
            targetClient.uploadFile(inputStream, targetPath, fileName);
            return true;
        } catch (Exception e) {
            log.error("拷贝文件出错", e);
            return false;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                log.error("关闭流出错", e);
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

    public void ftpFileList(Map<String, List<String>> resultMap, SyncConfig loanSyncConfig, FtpClient client, String apiCode) {
        try {
            String srcPath = loanSyncConfig.getSrcPath();
            String suffixStr = loanSyncConfig.getSuffix();
            
            if("no_suffix".equals(suffixStr)){
                // no_suffix类型需要递归遍历所有子目录
                log.warn(TITLE+ "no_suffix类型，开始递归遍历FTP目录: {}", srcPath);
                ftpFileListRecursively(resultMap, loanSyncConfig, client, apiCode, srcPath);
            } else {
                // 其他类型只遍历当前目录
                FTPFile[] ftpFiles = client.listFiles(srcPath);
                if (ftpFiles == null || ftpFiles.length == 0) {
                    log.warn(TITLE+ "FTP目录为空或不存在，跳过遍历：{}", srcPath);
                    return;
                }
                log.warn(TITLE+ "FTP同步路径:{},该路径下文件有:{}个",srcPath,ftpFiles.length);
                for(FTPFile file:ftpFiles){
                    String fileName = file.getName();
                    Calendar timestamp = file.getTimestamp();
                    String createFileTime = DateUtils.parseDateTimeByDate( timestamp.getTime(), "yyyy-MM-dd HH:mm:ss");
                    log.info(TITLE+ "fileName:{},size:{},time:{}",fileName,file.getSize(),createFileTime);
                    if(vaildExclusionTime(createFileTime,loanSyncConfig)){
                        log.info(TITLE+ "历史文件，不处理{},{}",fileName,createFileTime);
                        continue;
                    }
                    validateIsSync(createFileTime,fileName,apiCode,resultMap,loanSyncConfig);
                }
            }
        } catch (Exception e) {
            log.error("遍历ftp文件出错",e);
        }
    }

    /**
     * 递归遍历FTP目录，收集所有文件（用于no_suffix类型）
     */
    private void ftpFileListRecursively(Map<String,List<String>> resultMap, SyncConfig loanSyncConfig, FtpClient client, String apiCode, String currentPath) {
        try {
            FTPFile[] ftpFiles = client.listFiles(currentPath);
            
            // 检查返回结果，如果是空数组说明目录不存在或无文件
            if (ftpFiles == null || ftpFiles.length == 0) {
                log.warn(TITLE+ "FTP目录为空或不存在，跳过遍历：{}", currentPath);
                return;
            }
            
            log.warn(TITLE+ "递归遍历FTP路径:{},该路径下文件有:{}个", currentPath, ftpFiles.length);
            
            for(FTPFile file : ftpFiles){
                String fileName = file.getName();

                // 跳过. 和 .. 目录
                if (".".equals(fileName) || "..".equals(fileName)) {
                    continue;
                }

                String fullPath = currentPath.endsWith("/") ? currentPath + fileName : currentPath + "/" + fileName;
                
                if (file.isDirectory()) {
                    // 如果是目录，递归遍历
                    log.warn(TITLE+ "发现子目录：{}，开始递归遍历", fullPath);
                    ftpFileListRecursively(resultMap, loanSyncConfig, client, apiCode, fullPath);
                } else {
                    // 如果是文件，检查是否需要同步
                    Calendar timestamp = file.getTimestamp();
                    String createFileTime = DateUtils.parseDateTimeByDate(timestamp.getTime(), "yyyy-MM-dd HH:mm:ss");
                    log.warn("fileName:{},size:{},time:{}",fileName,file.getSize(),createFileTime);
                    if(vaildExclusionTime(createFileTime, loanSyncConfig)){
                        log.warn(TITLE+ "历史文件，不处理{},{}", fileName, createFileTime);
                        continue;
                    }
                    validateIsSyncWithPath(createFileTime, fileName, apiCode, resultMap, loanSyncConfig, currentPath);
                }
            }
        } catch (Exception e) {
            log.error(TITLE+ "递归遍历FTP目录出错，路径：{}", currentPath, e);
        }
    }


    public void sftpFileList(Map<String,List<String>> resultMap, SyncConfig loanSyncConfig, SftpClient client, String apiCode){
            try {
                String srcPath = loanSyncConfig.getSrcPath();
                String suffixStr = loanSyncConfig.getSuffix();
                
                if("no_suffix".equals(suffixStr)){
                    // no_suffix类型需要递归遍历所有子目录
                    log.warn(TITLE+ "no_suffix类型，开始递归遍历SFTP目录: {}", srcPath);
                    sftpFileListRecursively(resultMap, loanSyncConfig, client, apiCode, srcPath);
                } else {
                    // 其他类型只遍历当前目录
                    Map<String, SftpATTRS> map = client.listFiles(srcPath);
                    log.warn(TITLE+ "SFTP同步路径:{},该路径下文件有:{}个",srcPath,map.keySet().size());
                    for(Map.Entry<String, SftpATTRS> entry : map.entrySet()){
                        String fileName = entry.getKey();
                        SftpATTRS attrs = entry.getValue();
                        String createFileTime = DateHelper.timeStamp2Date(attrs.getMTime() + "", "yyyy-MM-dd HH:mm:ss");
                        if(vaildExclusionTime(createFileTime,loanSyncConfig)){
                            log.warn(TITLE+ "历史文件，不处理{},{}",fileName,createFileTime);
                            continue;
                        }
                        validateIsSync(createFileTime,fileName,apiCode,resultMap,loanSyncConfig);
                    }
                }
            } catch (Exception e) {
                log.error("遍历sftp文件出错",e);
            }
    }

    /**
     * 递归遍历SFTP目录，收集所有文件（用于no_suffix类型）
     */
    private void sftpFileListRecursively(Map<String,List<String>> resultMap, SyncConfig loanSyncConfig, SftpClient client, String apiCode, String currentPath) {
        try {
            Map<String, SftpATTRS> map = client.listFiles(currentPath);
            log.warn("递归遍历SFTP路径:{},该路径下文件有:{}个", currentPath, map.keySet().size());
            
            for(Map.Entry<String, SftpATTRS> entry : map.entrySet()){
                String fileName = entry.getKey();
                SftpATTRS attrs = entry.getValue();

                // 跳过. 和 .. 目录
                if (".".equals(fileName) || "..".equals(fileName)) {
                    continue;
                }

                String fullPath = currentPath.endsWith("/") ? currentPath + fileName : currentPath + "/" + fileName;
                
                if (attrs.isDir()) {
                    // 如果是目录，递归遍历
                    log.warn(TITLE+ "发现子目录：{}，开始递归遍历", fullPath);
                    sftpFileListRecursively(resultMap, loanSyncConfig, client, apiCode, fullPath);
                } else {
                    // 如果是文件，检查是否需要同步
                    String createFileTime = DateHelper.timeStamp2Date(attrs.getMTime() + "", "yyyy-MM-dd HH:mm:ss");
                    if(vaildExclusionTime(createFileTime, loanSyncConfig)){
                        log.warn(TITLE+ "历史文件，不处理{},{}", fileName, createFileTime);
                        continue;
                    }
                    validateIsSyncWithPath(createFileTime, fileName, apiCode, resultMap, loanSyncConfig, currentPath);
                }
            }
        } catch (Exception e) {
            log.error(TITLE+ "递归遍历SFTP目录出错，路径：{}", currentPath, e);
        }
    }

    /**
     * 校验文件是否需要同步，如果需要，检查是否已经同步过，然后放到map中（带路径信息，用于no_suffix类型）
     * @param createFileTime 文件创建时间
     * @param fileName 文件名
     * @param apiCode apiCode
     * @param resultMap  文件数据集合
     * @param syncConfig 同步配置
     * @param currentPath 当前路径
     */
    private void validateIsSyncWithPath(String createFileTime, String fileName, String apiCode, Map<String, List<String>> resultMap, SyncConfig syncConfig, String currentPath) {
        long minutes = DateHelper.getDistanceMinutes(createFileTime);
        if(minutes<1){
            log.warn(TITLE+ "文件上传时间距离当前时间小于1分钟，暂时不处理{},{}",fileName,createFileTime);
            return;
        }
        Map<String,String> params=new HashMap<>();
        params.put("apiCode",apiCode);
        params.put("fileName",fileName);
        params.put("srcPath",syncConfig.getSrcSftpHost().concat(":").concat(currentPath));
        List<SyncLog> syncLogs=  loanSyncLogMapper.querySyncLog(params);
        if(syncLogs==null||syncLogs.size()<=0){
            String suffixStr = syncConfig.getSuffix();
            
            if("no_suffix".equals(suffixStr)){
                // no_suffix类型：存储完整路径+文件名，格式为 "路径|文件名"
                List<String> list = resultMap.get("no_suffix");
                if(list==null){
                    list=new ArrayList<>();
                    resultMap.put("no_suffix",list);
                }
                String fullPathAndName = currentPath + "|" + fileName;
                list.add(fullPathAndName);
                log.warn(TITLE+ "no_suffix类型-文件未同步过，加入同步队列: {}",fullPathAndName);
            } else {
                // 其他类型：按后缀分类，只存储文件名
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
                    log.warn(TITLE+ "error fileName :{}",fileName);
                }
            }
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
            log.warn(TITLE+ "文件上传时间距离当前时间小于1分钟，暂时不处理{},{}",fileName,createFileTime);
            return;
        }
        Map<String,String> params=new HashMap<>();
        params.put("apiCode",apiCode);
        params.put("fileName",fileName);
//        params.put("createFileTime",createFileTime);
        params.put("srcPath",syncConfig.getSrcSftpHost().concat(":").concat(syncConfig.getSrcPath()));
        List<SyncLog> syncLogs=  loanSyncLogMapper.querySyncLog(params);
        if(syncLogs==null||syncLogs.size()<=0){
            String suffixStr = syncConfig.getSuffix();
            
            if("no_suffix".equals(suffixStr)){
                // no_suffix类型：所有文件都加入no_suffix分类，不区分后缀
                List<String> list = resultMap.get("no_suffix");
                if(list==null){
                    list=new ArrayList<>();
                    resultMap.put("no_suffix",list);
                }
                list.add(fileName);
                log.warn(TITLE+ "no_suffix类型-文件未同步过，加入同步队列: {}",fileName);
            } else {
                // 其他类型：按后缀分类
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
                    log.warn(TITLE+ "error fileName :{}",fileName);
                }
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
                // 清洗落库：非 zip / zip 但不解压 → 整文件一条；zip 且 is_unzip=1 → 解压后按文件各一条
                String suffixStr = loanSyncConfig.getSuffix();
                boolean zipSuffix = suffixStr != null && suffixStr.toLowerCase().contains("zip");
                boolean needUnzip = zipSuffix && SyncConfigIsUnzipEnum.needUnzip(loanSyncConfig.getIsUnzip());
                if (!zipSuffix || !needUnzip) {
                    return saveDataFileInfo(fileName, loanSyncConfig, targetPath, srcPath, md5Value);
                }
                return unzipAndSaveExtractedFiles(fileName, loanSyncConfig, targetPath, srcPath, file);
            } catch (Exception e) {
                log.warn("文件下载错误文件出错！srcPath:{},fileName:{},targetPath{},syncConfigId:{}"
                        , srcPath, fileName, targetPath, loanSyncConfig.getId(), e);
            } finally {
                byteBuffer.clear();
                if (writableByteChannel != null) {
                    try {
                        writableByteChannel.close();
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
                if (inputStream != null) {
                    try {
                        inputStream.close();
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

    /**
     * 解压 zip 到同级目录，并为每个解压出的文件插入一条 b_marketing_clean_data_file（zip_name 为压缩包名）
     */
    private Boolean unzipAndSaveExtractedFiles(String zipFileName, SyncConfig loanSyncConfig,
                                                String targetPath, String srcPath, File zipFile) {
        try {
            String encoding = UnzipFilenameCharsetEnum.defaultIfBlank(loanSyncConfig.getUnzipFilenameCharset());
            List<String> extractedPaths = ZipUtils.unZipAndReturnExtractedPaths(zipFile, targetPath, "", encoding);
            if (extractedPaths == null || extractedPaths.isEmpty()) {
                log.warn("压缩包内无文件或解压未得到文件列表，zipFileName:{}, syncConfigId:{}", zipFileName, loanSyncConfig.getId());
                return Boolean.TRUE;
            }
            Date now = new Date();
            for (String relPath : extractedPaths) {
                File f = new File(targetPath, relPath);
                String parent = f.getParent();
                String localPath;
                if (parent == null) {
                    localPath = targetPath;
                } else if (parent.endsWith(File.separator)) {
                    localPath = parent;
                } else {
                    localPath = parent + File.separator;
                }
                String extractedFileName = f.getName();
                MarketingCleanDataFile dataFile = new MarketingCleanDataFile();
                dataFile.setFileName(extractedFileName);
                dataFile.setZipName(zipFileName);
                dataFile.setApiCode(loanSyncConfig.getApiCode());
                dataFile.setCreateTime(now);
                dataFile.setUpdateTime(now);
                dataFile.setLocalPath(localPath);
                dataFile.setTargetSftpPath(srcPath);
                dataFile.setSyncConfigId(loanSyncConfig.getId());
                int i = marketingCleanDataFileMapper.insertSelective(dataFile);
                if (i < 1) {
                    log.warn("解压文件落库失败！zipFileName:{}, extractedFile:{}, syncConfigId:{}",
                            zipFileName, extractedFileName, loanSyncConfig.getId());
                }
            }
            log.warn("压缩包解压并落库完成，zipFileName:{}, 解压文件数:{}, syncConfigId:{}",
                    zipFileName, extractedPaths.size(), loanSyncConfig.getId());
            return Boolean.TRUE;
        } catch (Exception e) {
            log.warn("压缩包解压或落库异常，zipFileName:{}, syncConfigId:{}, error:{}",
                    zipFileName, loanSyncConfig.getId(), e.getMessage(), e);
            return Boolean.FALSE;
        }
    }

}
