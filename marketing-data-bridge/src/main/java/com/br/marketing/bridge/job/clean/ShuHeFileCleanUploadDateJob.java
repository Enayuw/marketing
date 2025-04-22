package com.br.marketing.bridge.job.clean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.encryption.BrCipherMaker;
import com.br.common.log.AlertLog;
import com.br.common.validator.DateUtils;
import com.br.marketing.client.FtpClient;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.DataTypeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.entity.*;
import com.br.marketing.enums.TransferActionFrontActionTypeEnum;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.mapper.SyncConfigMapper;
import com.br.marketing.mapper.TransferActionFrontMapper;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.service.Impl.JobManager;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.TimeUtils;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.jcraft.jsch.SftpATTRS;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName ShuHeFileCleanUploadDateJob
 * @Description 数禾洗库：https://c.100credit.cn/pages/viewpage.action?pageId=201089016
 * @Author kongbx
 * @Date 2025/4/14 10:36
 */
@Component
@Slf4j
public class ShuHeFileCleanUploadDateJob extends AbstractSimpleElasticJob {
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private TransferActionFrontMapper transferActionFrontMapper;
    @Resource
    private JobManager jobManager;
    @Resource
    MarketingSyncUserMapper marketingSyncUserMapper;
    @Resource
    SyncConfigMapper syncConfigMapper;
    public static final String PATH_SEPARATOR = "/";
    public static final String SUFFIX_TXT = ".txt";
    public static final String PATH_SPLIT = "/split/";
    public static final String PATH_SPECIAL = "special";
    private static final String TITLE = "【数禾首借数据自动化匹配洗库】";

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        log.warn("{}开始执行", TITLE);
        long start = System.currentTimeMillis();
        try {
            processApiCodes();
        } catch (Exception e) {
            log.error(TITLE + "处理异常{}", e.getMessage());
        }
        long end = System.currentTimeMillis();
        log.warn("{}执行完成，耗时{}ms", TITLE, end - start);
    }

    private void processApiCodes() {

        // 从配置获取初始数据
        Map<String, Object> configMap = marketingCommonConfig.getShuHeFileCleanUploadDateConfig();
        log.warn(TITLE + "获取speed！" + configMap.toString());
        // 1. 处理apiCodes
        List<String> apiCodes = getApiCodesWithDefaults(configMap);
        // 2. 处理日期列表
        List<String> appletDates = getAppletDatesWithDefaults(configMap);
        // 3. 处理文件名
        String fileName = processFileName(configMap);

        for (String apiCode : apiCodes) {
            //判断今日是否执行过
            TransferActionFrontExample frontExample = new TransferActionFrontExample();
            frontExample.createCriteria()
                    .andApiCodeEqualTo(apiCode)
                    .andActionDataEqualTo(LocalDate.now().toString())
                    .andActionTypeEqualTo(TransferActionFrontActionTypeEnum.ONE.getValue())
                    .andIsDelEqualTo(Constants.DATA_VALID);
            List<TransferActionFront> transferActionFronts = transferActionFrontMapper.selectByExample(frontExample);

            TransferActionFront transferActionFront;
            if (!CollectionUtils.isEmpty(transferActionFronts)) {
                transferActionFront = transferActionFronts.get(0);
                if(transferActionFront.getStatus() == 2){
                    log.warn(TITLE + "今日已执行！");
                    continue;
                }
            }else {
                //新增执行记录 setStatus 任务状态 1-未执行；2-执行结束；3-本地文件已生成
                transferActionFront = jobManager.saveFront(apiCode, LocalDate.now().toString(), TransferActionFrontActionTypeEnum.ONE.getValue());
            }

            SyncConfigExample syncConfigExample = new SyncConfigExample();
            syncConfigExample.createCriteria()
                    .andApiCodeEqualTo(apiCode)
                    .andDataTypeEqualTo(DataTypeEnum.MARKETINGUPLOADDATA.getValue())
                    .andTargetPathLike("%/download/marketing%")
                    .andStatusEqualTo(1)
                    .andTypeEqualTo(1);
            List<SyncConfig> syncConfigs = syncConfigMapper.selectByExample(syncConfigExample);
            if (CollectionUtils.isEmpty(syncConfigs)) {
                log.warn(TITLE + "SFTP配置不存在！");
                continue;
            }
            SyncConfig syncConfig = syncConfigs.get(0);
            // 文件处理逻辑
            processFile(syncConfig, fileName, appletDates, transferActionFront);
        }
    }

    private List<String> getApiCodesWithDefaults(Map<String, Object> configMap) {
        List<String> configuredApiCodes = (List<String>) configMap.get("apiCode");
        if (!CollectionUtils.isEmpty(configuredApiCodes)) {
            return new ArrayList<>(configuredApiCodes);
        }
        return Arrays.asList("3710128", "3710148");
    }

    private List<String> getAppletDatesWithDefaults(Map<String, Object> configMap) {
        List<String> configuredDates = (List<String>) configMap.get("appletDate");
        if (!CollectionUtils.isEmpty(configuredDates)) {
            return new ArrayList<>(configuredDates);
        }
        // 默认取昨天日期
        return Arrays.asList(LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    }

    private String processFileName(Map<String, Object> configMap) {
        String fileName = (String) configMap.get("fileName");
        if (fileName != null && fileName.contains("yyyy-MM-dd")) {
            return fileName.replace("yyyy-MM-dd", TimeUtils.getNowDate(TimeUtils.DATE_FORMAT));
        }
        return fileName;
    }

    private void processFile(SyncConfig syncConfig, String fileName,
                             List<String> appletDates,TransferActionFront transferActionFront) {

        String srcPath = syncConfig.getSrcPath();
        String targetPath = syncConfig.getTargetPath();

        //源文件逻辑处理
        if (srcPath.contains("yyyy-MM-dd")) {
            srcPath = srcPath.replace("yyyy-MM-dd", TimeUtils.getNowDate(TimeUtils.DATE_FORMAT));
        }else if(srcPath.contains("yyyyMMdd")){
            srcPath = srcPath.replace("yyyyMMdd", TimeUtils.getNowDate(TimeUtils.DATE_STRING));
        }

        if (targetPath.contains("yyyy-MM-dd")) {
            targetPath = targetPath.replace("yyyy-MM-dd", TimeUtils.getNowDate(TimeUtils.DATE_FORMAT));
        }else if(targetPath.contains("yyyyMMdd")){
            targetPath = targetPath.replace("yyyyMMdd", TimeUtils.getNowDate(TimeUtils.DATE_STRING));
        }

        syncConfig.setSrcPath(srcPath);
        syncConfig.setTargetPath(targetPath);

        Boolean flag = Boolean.TRUE;
        if(transferActionFront.getStatus() == 1){
            if (Constants.LOAN_WARNING_FTP.equals(syncConfig.getSrcType())) {
                FtpClient ftpClient = new FtpClient(syncConfig, true);
                flag = ftpFileList(ftpClient, syncConfig, fileName, transferActionFront);
            } else if (Constants.LOAN_WARNING_SFTP.equals(syncConfig.getSrcType())) {
                SftpClient sftpClient = new SftpClient(syncConfig, true);
                flag = sftpFileList(sftpClient, syncConfig, fileName, transferActionFront);
            }
        }
        //文件处理
        TransferActionFront front = transferActionFrontMapper.selectByPrimaryKey(transferActionFront.getId());
        if(flag && front.getStatus() == 3){
            workWithFiles(syncConfig, appletDates, transferActionFront.getId());
        }
    }

    private Boolean ftpFileList(FtpClient ftpClient, SyncConfig syncConfig, String fileName,TransferActionFront transferActionFront) {
        String srcPath = syncConfig.getSrcPath();
        String targetPath = syncConfig.getTargetPath();
        try {
            ftpClient.connect();
            boolean fileExists = ftpClient.isExistFile(srcPath.concat(fileName));
            if (!fileExists) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SHUHE_SERVICEERROR.getCode(), TITLE + "文件不存在："+srcPath.concat(fileName)));
                return Boolean.FALSE;
            }
            // 判断文件创建时间是否超过1分钟
            FTPFile ftpFile = ftpClient.getFtpFile(srcPath, fileName);
            Calendar timestamp = ftpFile.getTimestamp();
            String createFileTime = DateUtils.parseDateTimeByDate(timestamp.getTime(), "yyyy-MM-dd HH:mm:ss");
            long minutes = DateHelper.getDistanceMinutes(createFileTime);
            if (minutes < 1) {
                log.warn(TITLE + "文件上传时间距离当前时间小于1分钟，暂时不处理，文件名{},创建时间{}", fileName, createFileTime);
                return Boolean.FALSE;
            }
            //判断.success文件是否存在
            String successName = fileName + ".success";
            boolean successFileExists = ftpClient.isExistFile(srcPath.concat(successName));
            if (!successFileExists) {
                //创建.success文件
                log.warn(TITLE + ".success文件不存在:{}", successName);
                File file = new File(targetPath.concat("success/"));
                if (!file.exists()) {
                    file.mkdirs();
                }
                String targetPathConcat = targetPath.concat("success/").concat(successName);
                File successFile = new File(targetPathConcat);
                if (!successFile.exists()) {
                    successFile.createNewFile();
                }

                log.warn(TITLE + "ftpClient推送success文件本地目录:{},远程目录:{}", targetPathConcat,srcPath+successName);
                ftpClient.uploadFile(Files.newInputStream(Paths.get(targetPathConcat)), srcPath, successName);
                return Boolean.FALSE;
            }
            //判断本地文件是否存在
            File targetFile = new File(targetPath.concat(fileName));
            if (!targetFile.exists()) {
                log.warn(TITLE + "本地文件不存在:{}", targetPath.concat(fileName));
                return Boolean.FALSE;
            }
            //判断本地文件生成时间是否小于2分钟
            Path filePath = targetFile.toPath();
            BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
            FileTime targetCreationTime = attrs.creationTime();
            String targetCreateFileTime = DateUtils.parseDateTimeByDate(new Date(targetCreationTime.toMillis()), "yyyy-MM-dd HH:mm:ss");
            long targetMinutes = DateHelper.getDistanceMinutes(targetCreateFileTime);
            if (targetMinutes < 1) {
                log.warn(TITLE + "本地文件上传时间距离当前时间小于1分钟，暂时不处理，文件名{},创建时间{}", fileName, targetCreateFileTime);
                return Boolean.FALSE;
            }
        } catch (Exception e) {
            log.error(TITLE + "拉取文件异常", e);
            return Boolean.FALSE;
        } finally {
            try {
                ftpClient.disconnect();
            } catch (Exception e) {
                log.error(TITLE + "文件检查任务关闭FTP客户端异常", e);
            }
        }
        jobManager.updateFrontDataStatus(transferActionFront.getId(),3);
        return Boolean.TRUE;
    }

    private Boolean sftpFileList(SftpClient sftpClient, SyncConfig syncConfig, String fileName,TransferActionFront transferActionFront) {
        String srcPath = syncConfig.getSrcPath();
        String targetPath = syncConfig.getTargetPath();
        try {
            sftpClient.connect();
            boolean fileExists = sftpClient.isExistFile(srcPath.concat(fileName));
            if (!fileExists) {
                log.warn(TITLE + "文件不存在:{}", fileName);
                return Boolean.FALSE;
            }
            // 判断文件创建时间是否超过1分钟
            Map<String, SftpATTRS> map = sftpClient.listFiles(srcPath);
            log.warn(TITLE + "SFTP同步路径:{},该路径下文件有:{}个", srcPath, map.keySet().size());
            for (Map.Entry<String, SftpATTRS> entry : map.entrySet()) {
                if (entry.getKey().equals(fileName)) {
                    SftpATTRS attrs = entry.getValue();
                    String createFileTime = DateHelper.timeStamp2Date(attrs.getMTime() + "", "yyyy-MM-dd HH:mm:ss");
                    long minutes = DateHelper.getDistanceMinutes(createFileTime);
                    if (minutes < 1) {
                        log.warn("文件上传时间距离当前时间小于1分钟，暂时不处理，文件名{},创建时间{}", fileName, createFileTime);
                        return Boolean.FALSE;
                    }
                }
            }
            //判断.success文件是否存在
            String successName = fileName + ".success";
            boolean successFileExists = sftpClient.isExistFile(srcPath.concat(successName));
            if (!successFileExists) {
                //创建.success文件
                log.warn(TITLE + ".success文件不存在:{}", successName);
                File file = new File(targetPath.concat("success/"));
                if (!file.exists()) {
                    file.mkdirs();
                }
                File successFile = new File(targetPath.concat("success/").concat(successName));
                if (!successFile.exists()) {
                    successFile.createNewFile();
                }
                sftpClient.uploadFile(srcPath, successName, targetPath.concat("success/").concat(successName));
                return Boolean.FALSE;
            }
            //判断本地文件是否存在
            File targetFile = new File(targetPath.concat(fileName));
            if (!targetFile.exists()) {
                log.warn(TITLE + "本地文件不存在:{}", targetPath.concat(fileName));
                return Boolean.FALSE;
            }
        } catch (Exception e) {
            log.error(TITLE + "拉取文件异常", e);
            return Boolean.FALSE;
        } finally {
            try {
                sftpClient.disconnect();
            } catch (Exception e) {
                log.error(TITLE + "文件检查任务关闭FTP客户端异常", e);
            }
        }
        jobManager.updateFrontDataStatus(transferActionFront.getId(),3);
        return Boolean.TRUE;
    }

    private void workWithFiles(SyncConfig syncConfig, List<String> appletDates, Long jobId) {
        try {
            //本地文件逻辑处理
            ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(10, 10, 20);
            String apiCode = syncConfig.getApiCode();
            //文件切分
            List<String> fileNameList = specialSplitFile(syncConfig, 5000);
            //2、小文件自动化匹配洗库
            if (!CollectionUtils.isEmpty(fileNameList)) {
                CountDownLatch countDownLatch = new CountDownLatch(fileNameList.size());
                for (String name : fileNameList) {
                    threadPool.submit(() -> {
                        syncDataToShThreadOnlyRisk(name, apiCode, appletDates, countDownLatch);
                    });
                }
                try {
                    countDownLatch.await();
                } catch (Exception e) {
                    log.warn(TITLE, e);
                }
            }
            // 关闭线程池
            threadPool.shutdown();
            try {
                while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                    log.warn("等待线程池结束");
                }
            } catch (Exception ex) {
                log.error(TITLE + "线程池关闭异常", ex);
            }
            //更新执行记录
            jobManager.updateFrontDataStatus(jobId, 2);
        } catch (Exception e) {
            log.error(TITLE + "文件处理异常", e);
        }
    }

    /**
     * 切分文件
     *
     * @param syncConfig
     * @param splitNum
     * @return
     */
    public static List<String> specialSplitFile(SyncConfig syncConfig, int splitNum) {
        String targetPath = syncConfig.getTargetPath();

        List<String> fileNameList = new ArrayList<>();
        log.warn(TITLE + "开始切分文件");
        long l = System.currentTimeMillis();
        String uploadDate = TimeUtils.getNowDate(TimeUtils.DATE_STRING);
        // 切分文件路径---apiCode/special/yyyyMMdd
        String requestPath = targetPath + uploadDate;
        // 切分小文件路径---apiCode/special/yyyyMMdd/split/
        String splitPath = requestPath + PATH_SPLIT;
        File tmpFile = new File(targetPath);
        if (!tmpFile.exists()) {
            tmpFile.mkdirs();
        }
        //文件集合
        File[] temFiles = tmpFile.listFiles();
        if (temFiles != null) {
            for (File tFile : temFiles) {
                if (tFile.isDirectory()) {
                    continue;
                }
                String name = tFile.getName();
                List<String> fileNames = splitFile(tFile.getPath(), splitPath, splitNum);
                if (!CollectionUtils.isEmpty(fileNames)) {
                    fileNameList.addAll(fileNames);
                }
                //文件移动
                moveFiles(tFile, requestPath, name);
            }
        }
        log.warn(TITLE + "切分文件结束--{},耗时--{}", fileNameList.size(), System.currentTimeMillis() - l);
        return fileNameList;
    }

    /**
     * 切分文件
     *
     * @param pathName      需要拆分的文件全路径
     * @param splitPath     拆分结果目标路径
     * @param splitNum      单个文件拆分大小
     * @return
     */
    public static List<String> splitFile(String pathName, String splitPath, int splitNum) {
        //创建切割目录
        List<String> fileNameList = new ArrayList<>();
        File writeName = new File(splitPath);
        if (!writeName.exists()) {
            writeName.mkdirs();
        }
        Writer fw = null;
        try (FileReader read = new FileReader(pathName);
             BufferedReader br = new BufferedReader(read)) {
            String row;
            int rownum = 1;
            int fileNo = 1;
            //创建写入文件
            String splitName = splitPath + fileNo + SUFFIX_TXT;
            File file1 = new File(splitName);
            fw = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(Paths.get(file1.getPath())), StandardCharsets.UTF_8));
            fileNameList.add(file1.getPath());
            while ((row = br.readLine()) != null) {
                rownum++;
                fw.append(row + "\r\n");
                if ((rownum / splitNum) > (fileNo - 1)) {
                    fw.close();
                    fileNo++;
                    File fileAdd = new File(splitPath + fileNo + SUFFIX_TXT);
                    fw = new FileWriter(fileAdd);
                    fileNameList.add(fileAdd.getPath());
                }
            }
        } catch (IOException e) {
            log.error(TITLE + "splitFile error", e);
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException ex) {
                    log.error(TITLE + "Writer fw close error", ex);
                }
            }
        }
        return fileNameList;
    }

    /**
     * 移动文件
     *
     * @param tFile
     * @param requestPath
     * @param name
     * @return
     */
    public static void moveFiles(File tFile, String requestPath, String name) {
        long l = System.currentTimeMillis();
        boolean flag = true;
        for (int i = 0; i < 5; i++) {
            try {
                String sourceStr = requestPath + PATH_SEPARATOR + name;
                Files.move(Paths.get(tFile.getPath()), Paths.get(sourceStr), StandardCopyOption.REPLACE_EXISTING);
                flag = false;
            } catch (Exception e) {
                log.warn(TITLE + "File is failed to move！error", e);
            }
            if (!flag) {
                log.warn(TITLE + "File is moved successful！文件名：{} 目标路径：{} costTime:{}", name, requestPath, System.currentTimeMillis() - l);
                break;
            } else {
                log.warn(TITLE + "第{}次移动文件 文件名：{} 起始路径：{} costTime:{}", i, name, requestPath, System.currentTimeMillis() - l);
                //沉睡0.2s
                threadSleep();
            }
        }
    }

    /**
     * 沉睡0.2s
     *
     * @param
     * @return
     */
    private static void threadSleep() {
        try {
            Thread.sleep(200);
        } catch (Exception e) {
            log.error(TITLE + "Thread.sleep error", e);
        }
    }

    /**
     * 创建文件
     *
     * @param syncConfig
     * @param requestTime
     * @return
     */
    private Writer createSpecial(SyncConfig syncConfig, String requestTime) throws IOException {
        String targetPath = syncConfig.getTargetPath();
        File writeName = new File(targetPath);
        if (!writeName.exists()) {
            boolean mkdirs = writeName.mkdirs();
            if (!mkdirs) {
                log.error(TITLE + "writeName mkdirs error:{}", writeName);
            }
        }
        StringBuilder path = new StringBuilder()
                .append(targetPath)
                .append(requestTime).append(PATH_SEPARATOR)
                .append(PATH_SPECIAL).append("_").append(requestTime).append("_").append(System.currentTimeMillis())
                .append(SUFFIX_TXT);
        File file1 = new File(path.toString());
        return new BufferedWriter(
                new OutputStreamWriter(
                        Files.newOutputStream(Paths.get(file1.getPath())), StandardCharsets.UTF_8));
    }

    public void syncDataToShThreadOnlyRisk(String fileName, String apiCode, List<String> appletDates,
                                           CountDownLatch countDownLatch) {
        log.warn("开始自动化匹配洗库处理:{}", fileName);
        try (InputStreamReader fReader = new InputStreamReader(Files.newInputStream(Paths.get(fileName)), StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(fReader)) {
            String params;
            int total = 0;
            List<MarketingSyncUser> batchList = new ArrayList<>();
            Map<Long, String> cellMap = new HashMap<>();
            Map<Long, String> cellLogEncodeMap = new HashMap<>();
            Map<Long, String> riskCreditLimitMap = new HashMap<>();

            while ((params = reader.readLine()) != null) {
                if (StringUtils.isNotBlank(params)) {
                    if (params.contains("mobile_sha256")) {
                        log.warn(TITLE + "过滤表头params:{}", params);
                        continue;
                    }

                    // 解析数据
                    String[] split = params.split(",");
                    String cell = split.length >= 2 ? split[1] : "";
                    String riskCreditLimitHbLv0 = split.length >= 3 ? split[2] : "";

                    if (StringUtils.isNotBlank(cell)) {
                        total = processCellData(cell, riskCreditLimitHbLv0, apiCode, appletDates,
                                batchList, cellMap, cellLogEncodeMap, riskCreditLimitMap, total);
                    } else {
                        log.warn(TITLE + "cell为空:{}", params);
                    }
                }
            }

            // 处理最后一批数据
            if (!batchList.isEmpty()) {
                processBatchUpdate(batchList, apiCode, riskCreditLimitMap, total);
            }

            log.warn(TITLE + "处理完成:{},total:{}", fileName, total);
        } catch (Exception e) {
            log.error(TITLE + "处理出错", e);
        } finally {
            countDownLatch.countDown();
        }
    }

    /**
     * 处理手机号数据
     * @return 更新后的total值
     */
    private int processCellData(String cell, String riskCreditLimitHbLv0, String apiCode,
                                List<String> appletDates, List<MarketingSyncUser> batchList,
                                Map<Long, String> cellMap, Map<Long, String> cellLogEncodeMap,
                                Map<Long, String> riskCreditLimitMap, int total) throws IOException {
        String cellEncode = RpcClientProxy.decode(cell, "cell", "sha", "");
        if (StringUtils.isNotBlank(cellEncode) && !cellEncode.equals(cell)) {
            String cellLogEncode = BrCipherMaker.getInstance().encode(cellEncode);
            List<MarketingSyncUser> list = marketingSyncUserMapper.getUserByCell(apiCode, appletDates, cellLogEncode);

            if (list != null && !list.isEmpty()) {
                if (list.size() > 1) {
                    log.warn(TITLE + "查询：{},{},{},list:{}", apiCode, cellLogEncode, appletDates, JSON.toJSON(list));
                }

                // 保存每条记录对应的cell和加密信息
                for (MarketingSyncUser user : list) {
                    cellMap.put(user.getId(), cell);
                    cellLogEncodeMap.put(user.getId(), cellLogEncode);
                    riskCreditLimitMap.put(user.getId(), riskCreditLimitHbLv0);
                }

                batchList.addAll(list);

                if (batchList.size() >= 500) {
                    total = processBatchUpdate(batchList, apiCode, riskCreditLimitMap, total);
                    batchList.clear();
                    // 清理已处理的数据
                    cellMap.clear();
                    cellLogEncodeMap.clear();
                    riskCreditLimitMap.clear();
                }
            }
        } else {
            log.warn(TITLE + "解密失败:{}", cell);
        }
        return total;
    }

    /**
     * 批量更新数据
     */
    private int processBatchUpdate(List<MarketingSyncUser> batchList, String apiCode,
                                   Map<Long, String> riskCreditLimitMap, int total) {
        StringBuilder updateSql = new StringBuilder();
        updateSql.append("UPDATE b_marketing_sync_").append(apiCode).append(" SET reserve_field1 = CASE id ");

        List<Long> idList = new ArrayList<>();

        for (MarketingSyncUser sync : batchList) {
            JSONObject reserveField1Obj = JSONObject.parseObject(sync.getReserveField1());

            if (reserveField1Obj == null || reserveField1Obj.isEmpty()) {
                reserveField1Obj = new JSONObject();
            }

            String riskCreditLimitHbLv0 = riskCreditLimitMap.get(sync.getId());
            String riskCreditLimitHbLv0His = reserveField1Obj.getString("risk_credit_limit_hb_lv0");

            if (StringUtils.isNotBlank(riskCreditLimitHbLv0) && StringUtils.isBlank(riskCreditLimitHbLv0His)) {
                reserveField1Obj.put("risk_credit_limit_hb_lv0", riskCreditLimitHbLv0);
            }

            updateSql.append("WHEN ").append(sync.getId())
                    .append(" THEN '").append(reserveField1Obj.toJSONString()).append("' ");

            idList.add(sync.getId());
            total++;
        }

        updateSql.append("END WHERE id IN (").append(StringUtils.join(idList, ",")).append(")");

        marketingSyncUserMapper.updateBatchData(updateSql.toString());
        return total;
    }

}
