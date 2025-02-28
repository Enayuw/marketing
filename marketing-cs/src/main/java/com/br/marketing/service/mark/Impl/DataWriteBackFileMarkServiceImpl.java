package com.br.marketing.service.mark.Impl;

import cn.hutool.core.collection.CollectionUtil;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.file.ZipUtil;
import com.br.marketing.dto.mark.FlagDataWriteBackFileMark;
import com.br.marketing.entity.FlagData;
import com.br.marketing.entity.FlagDataExample;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.enums.EsSyncStatusEnum;
import com.br.marketing.mapper.FlagDataMapper;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.service.mark.DataMarkCommonService;
import com.br.marketing.service.mark.DataWriteBackFileMarkService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @ClassName DataWriteBackFileMarkServiceImpl
 * @Description pp停车文件数据回写跑分文件与Doris实现
 * @Author kongbx
 * @Date 2025/2/19 19:12
 */
@Service
@Slf4j
public class DataWriteBackFileMarkServiceImpl implements DataWriteBackFileMarkService {

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
    @Autowired
    RedisChgService redisChgService;
    @Resource
    DataMarkCommonService dataMarkCommonService;
    @Resource
    MarketingCommonConfig marketingCommonConfig;
    @Resource
    FlagDataMapper flagDataMapper;
    private static final String TITLE = "【pp停车文件数据回写】";
    @Override
    public void process(String scoreDate) {
        marketingCommonConfig.getDataMarkApiCodes().forEach((String apiCode) -> {
            if (checkEsStatus(apiCode)) {
                StraHisFile straHisFile = dataMarkCommonService.getStraHisFile(apiCode, scoreDate);;
                if (null == straHisFile) {
                    log.warn(TITLE + "查询跑分文件未空，apiCode:"+apiCode,"日期："+scoreDate);
                    return;
                }
                String batchNumber = straHisFile.getBatchNumber();
                String syncDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
                String descPath = syncConfigService.getPath().concat("ppMarkToFile/").concat(apiCode).concat("/").concat(syncDate).concat("/");
                String fileName = "pp_" + apiCode + "_" + syncDate + ".txt";

                // 同步数据写入doris
                syncData(apiCode, descPath, fileName, batchNumber);
                // 推送文件至SFTP
                pushFileSftp(apiCode, descPath, fileName);
            }
        });

    }

    /**
     * 判断es数据是否补充完毕
     *
     * @return
     */
    private boolean checkEsStatus(String apiCode) {
        Boolean aFalse = Boolean.TRUE;
        FlagDataExample flagDataExample = new FlagDataExample();
        flagDataExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andAppletDateEqualTo(LocalDate.now().toString())
                .andEsSyncStatusNotEqualTo(EsSyncStatusEnum.COMPLETE.getValue())
                .andIsDeleteEqualTo(0);
        int i = flagDataMapper.countByExample(flagDataExample);
        if (i > 0) {
            log.warn(TITLE + "es数据未补充完毕");
            aFalse = Boolean.FALSE;
        }
        return aFalse;
    }

    /**
     * 同步数据写入doris
     */
    private void syncData(String apiCode, String descPath, String fileName, String batchNumber) {
        Integer threadPoolSize = marketingCommonConfig.getDataMarkThreadNum();
        int dataDorisMarkPageSize = marketingCommonConfig.getDataDorisMarkPageSize() == null ? 2000 : marketingCommonConfig.getDataMarkPageSize();
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadPoolSize, threadPoolSize);
        try {
            Long minId = null;
            boolean isContiue = Boolean.TRUE;
            while (isContiue) {
                // 分页查询打标数据
                FlagDataExample flagDataExample = new FlagDataExample();
                flagDataExample.setOrderByClause("id limit " + dataDorisMarkPageSize);

                FlagDataExample.Criteria criteria = flagDataExample.createCriteria()
                        .andApiCodeEqualTo(apiCode)
                        .andAppletDateEqualTo(LocalDate.now().toString())
                        .andEsSyncStatusEqualTo(EsSyncStatusEnum.COMPLETE.getValue());
                if (minId != null) {
                    criteria.andIdGreaterThan(minId);
                }
                List<FlagData> flagDataList = flagDataMapper.selectByExample(flagDataExample);
                if (CollectionUtil.isEmpty(flagDataList)) {
                    isContiue = Boolean.FALSE;
                    continue;
                }
                minId = flagDataList.get(flagDataList.size() - 1).getId();
                threadPool.submit(() -> writeBackFileMark(flagDataList, batchNumber, descPath, fileName));
            }
            threadPool.shutdown();
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.warn(TITLE + "线程池关闭");
            }
        } catch (Exception ex) {
            threadPool.shutdownNow();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ES_RETRY_DATAERROR.getCode(), TITLE + "线程池关闭！异常"), ex);
            Thread.currentThread().interrupt();
        }
    }

    private void writeBackFileMark(List<FlagData> flagDataList, String batchNumber,
                                   String descPath, String fileName) {
        // 写入doris
        List<FlagDataWriteBackFileMark> dataList = insertMarkData(flagDataList, batchNumber);
        // 写入文件
        writeDataToFile(dataList, descPath, fileName);
    }

    private List<FlagDataWriteBackFileMark> insertMarkData(List<FlagData> flagDataList, String batchNumber) {
        List<FlagDataWriteBackFileMark> flagDataResult = new ArrayList<>();
        try {
            List<String> cellMd5List = flagDataList.stream().map(FlagData::getCellMd5).collect(Collectors.toList());

            Map<String, FlagData> groupByCellMd5 = flagDataList.stream()
                    .collect(Collectors.toMap(FlagData::getCellMd5, data -> data, (oldValue, newValue) -> newValue));

            // 根据cells查询doris数据
            String cell = cellMd5List.stream()
                    .map(md5 -> "'" + md5 + "'")
                    .collect(Collectors.joining(","));
            String scoreSql = "select * from b_score_".concat(batchNumber).concat(" where cell in(").concat(cell).concat(")");
            flagDataResult = flagDataMapper.queryDataByCellbI_(scoreSql);

            flagDataResult.forEach((FlagDataWriteBackFileMark fileMark) -> {
                FlagData flagData = groupByCellMd5.get(fileMark.getCell());
                if(flagData.getFlagNewCust() != null){
                    String format = new SimpleDateFormat("yyyy-MM-dd").format(flagData.getDtWhitelist());
                    fileMark.setDtWhitelist(format);
                }else {
                    fileMark.setDtWhitelist(null);
                }
                fileMark.setFlagNewCust(flagData.getFlagNewCust());
                fileMark.setFlagRiskgroup(flagData.getFlagRiskgroup());
                fileMark.setFlagInterest(flagData.getFlagInterest());
                fileMark.setFlagAge(flagData.getFlagAge());
                fileMark.setFlagProvince(flagData.getFlagProvince());
                fileMark.setFlagSpecialSmall(flagData.getFlagSpecialSmall());
                fileMark.setFlagSpecialrisklevelRule(flagData.getFlagSpecialrisklevelRule());
                fileMark.setFlagApplyloan(flagData.getFlagApplyloan());
                fileMark.setFlagScoreysbase(flagData.getFlagScoreysbase());
                fileMark.setFlagScorefxsbbaseb(flagData.getFlagScorefxsbbaseb());
                fileMark.setFlagScorescashonregisternologin(flagData.getFlagScorescashonregisternologin());
                fileMark.setFlagScorescashonyxxy(flagData.getFlagScorescashonyxxy());
                fileMark.setFlagScorencashonzawswyyym(flagData.getFlagScorencashonzawswyyym());
                fileMark.setFlagIntellaudioBlacklist(flagData.getFlagIntellaudioBlacklist());
                fileMark.setFlagWithoutWillingness(flagData.getFlagWithoutWillingness());
                fileMark.setFlagWhitelist(flagData.getFlagWhitelist());

            });
            flagDataMapper.batchInsertFlagDatabI_(flagDataResult);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                    TITLE + "出现异常，" + "errorMessage=" + e.getMessage()), e);
        }
        return flagDataResult;
    }

    private void writeDataToFile(List<FlagDataWriteBackFileMark> dataList, String descPath, String fileName) {
        if (CollectionUtil.isEmpty(dataList)) {
            return;
        }

        String fileAllPath = descPath.concat(fileName);
        File file = new File(descPath);
        if (!file.exists()) {
            file.mkdirs();
        }

        File decodeFile = new File(fileAllPath);
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(decodeFile, true), StandardCharsets.UTF_8))) {
            // 动态生成表头
            String fileHeader = generateHeader();
            // 检查文件是否为空，如果为空则写入表头
            checkHeaderExists(fileAllPath, writer, fileHeader);

            // 写入数据行
            for (FlagDataWriteBackFileMark data : dataList) {
                List<String> values = new ArrayList<>();
                for (Field field : getOrderedFields()) {
                    try {
                        // 获取字段值
                        field.setAccessible(true); // 确保可以访问私有字段
                        Object value = field.get(data);
                        values.add(value != null ? value.toString() : "");
                    } catch (Exception e) {
                        values.add("");
                    }
                }
                String row = String.join(",", values);
                writer.append(row).append(System.lineSeparator());
            }

        } catch (IOException e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.PP_MARKING_SERVICEERROR.getCode(),
                    TITLE + "写入文件时发生异常"), e);
        }
    }

    /**
     * 根据 FlagDataWriteBackFileMark 类的字段顺序动态生成表头。
     * 过滤掉 Jacoco 注入的字段。
     */
    private String generateHeader() {
        List<String> headers = new ArrayList<>();
        for (Field field : getOrderedFields()) {
            // 将字段名转换为下划线命名（如 requestTime -> request_time）
            String fieldName = field.getName();
            fieldName = fieldName.replaceAll("([A-Z])", "_$1").toLowerCase();
            headers.add(fieldName);
        }
        return String.join(",", headers);
    }

    /**
     * 获取 FlagDataWriteBackFileMark 类中所有字段，并按定义顺序返回。
     * 过滤掉 Jacoco 注入的字段。
     */
    private List<Field> getOrderedFields() {
        // 获取类中的所有字段
        Field[] fields = FlagDataWriteBackFileMark.class.getDeclaredFields();
        // 过滤掉 Jacoco 注入的字段
        return Arrays.stream(fields)
                .filter(field -> !field.getName().startsWith("$")) // 过滤掉以 $ 开头的字段
                .collect(Collectors.toList());
    }

    /**
     * 检查文件是否为空，如果为空则写入表头。
     */
    private void checkHeaderExists(String filePath, Writer writer, String fileHeader) throws IOException {
        File file = new File(filePath);
        if (file.length() == 0) {
            log.warn(TITLE + "表头行数：" + fileHeader.split(",").length);
            writer.append(fileHeader).append(System.lineSeparator());
        }
    }

    /**
     * 推送文件至SFTP
     */
    private void pushFileSftp(String apiCode, String descPath, String fileName) {
        SftpClient sftpClient = new SftpClient(sftpHost, sftpPort, sftpUsername, sftpPwd);
        String syncDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String remotePath = "/UploadFiles/marketing/" + apiCode + "/output/" + syncDate;

        String zipFileName = fileName.replace(".txt", ".zip");
        String localFileName = descPath.concat(fileName);
        String localFileNameZip = descPath.concat(zipFileName);
        log.warn(TITLE + "原文件路径：" + localFileNameZip + " | 推送文件路径:" + remotePath);
        try {
            ZipUtil.compress(localFileName, localFileNameZip);
            sftpClient.connect();
            boolean upload = sftpClient.uploadFile(remotePath, zipFileName, localFileNameZip);
            if (upload) {
                File successFile = new File(localFileNameZip + ".success");
                successFile.createNewFile();
                if (successFile.exists()) {
                    sftpClient.uploadFile(remotePath, zipFileName + ".success", localFileNameZip + ".success");
                }
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.PUSH_TO_SFTP.getCode(),
                    TITLE + "文件推送SFTP异常，apiCode：" + apiCode), e);
        } finally {
            try {
                sftpClient.disconnect();
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.PUSH_TO_SFTP.getCode(),
                        TITLE + "文件推送SFTP关闭连接异常，apiCode：" + apiCode), e);
            }
        }
    }

}
