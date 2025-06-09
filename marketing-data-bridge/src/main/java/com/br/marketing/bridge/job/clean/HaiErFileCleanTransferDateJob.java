package com.br.marketing.bridge.job.clean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.common.validator.DateUtils;
import com.br.marketing.client.FtpClient;
import com.br.marketing.client.marketingapi.input.PushTransferDataDetailDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.DataTypeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.entity.SyncConfig;
import com.br.marketing.entity.SyncConfigExample;
import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.entity.TransferActionFrontExample;
import com.br.marketing.enums.TransferActionFrontActionTypeEnum;
import com.br.marketing.mapper.SyncConfigMapper;
import com.br.marketing.mapper.TransferActionFrontMapper;
import com.br.marketing.service.Impl.JobManager;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.util.TimeUtils;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.opencsv.CSVReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @ClassName HaiErFileCleanTransferDateJob
 * @Description 海尔转化数据每日清洗(sftp - > api)-3710018 https://c.100credit.cn/pages/viewpage.action?pageId=204927045
 * @Author kongbx
 * @Date 2025/5/19 16:12
 */
@Component
@Slf4j
public class HaiErFileCleanTransferDateJob extends AbstractSimpleElasticJob {

    @Resource
    private TransferActionFrontMapper transferActionFrontMapper;
    @Resource
    private JobManager jobManager;
    @Resource
    SyncConfigMapper syncConfigMapper;
    @Resource
    private PushInfoService pushInfoService;

    private static final String TITLE = "【海尔转化数据每日清洗】";

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        log.warn("{}开始执行", TITLE);
        long start = System.currentTimeMillis();

        String apiCode = context.getJobParameter();
        if (StringUtils.isEmpty(apiCode)) {
            apiCode = "3710018";
        }
        clean(apiCode);

        long end = System.currentTimeMillis();
        log.warn("{}执行完成，耗时{}ms", TITLE, end - start);
    }

    private void clean(String apiCode) {

        //判断今日是否执行过
        TransferActionFrontExample frontExample = new TransferActionFrontExample();
        frontExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andActionDataEqualTo(LocalDate.now().toString())
                .andActionTypeEqualTo(TransferActionFrontActionTypeEnum.ONE.getValue())
                .andIsDelEqualTo(Constants.DATA_VALID);
        List<TransferActionFront> transferActionFronts = transferActionFrontMapper.selectByExample(frontExample);

        TransferActionFront transferActionFront = new TransferActionFront();

        if (CollectionUtils.isEmpty(transferActionFronts)) {
            // 新增执行记录 任务状态 1-未执行；2-执行结束；3-本地文件已生成
            transferActionFront = jobManager.saveFront(apiCode, LocalDate.now().toString(), TransferActionFrontActionTypeEnum.ONE.getValue());
        } else {
            transferActionFront = transferActionFronts.get(0);
            if (transferActionFront.getStatus() == 2) {
                log.warn(TITLE + "今日已执行！");
                return;
            }
        }

        // 获取SFTP配置
        SyncConfigExample syncConfigExample = new SyncConfigExample();
        syncConfigExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andDataTypeEqualTo(DataTypeEnum.MARKETINGTRANSFERDATA.getValue())
                .andStatusEqualTo(1)
                .andTypeEqualTo(1);

        List<SyncConfig> syncConfigs = syncConfigMapper.selectByExample(syncConfigExample);
        if (CollectionUtils.isEmpty(syncConfigs)) {
            log.warn(TITLE + "SFTP配置不存在！");
            return;
        }
        SyncConfig syncConfig = syncConfigs.get(0);
        // 文件处理逻辑
        processFile(apiCode, syncConfig, transferActionFront);
    }

    private void processFile(String apiCode, SyncConfig syncConfig, TransferActionFront transferActionFront) {

        // 1. 准备日期相关路径
        // yyyy-MM-dd
        String formattedDate = TimeUtils.getNowDate(TimeUtils.DATE_FORMAT);
        // yyyyMMdd
        String compactDate = TimeUtils.getNowDate(TimeUtils.DATE_STRING);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(TimeUtils.DATE_STRING);
        // 获取当前日期并减1天
        LocalDate yesterday = LocalDate.now().minusDays(1);
        // 格式化为字符串
        String yesterdayStr = yesterday.format(formatter);

        // 2. 处理源路径和目标路径中的日期占位符
        String srcPath = replaceDatePlaceholders(syncConfig.getSrcPath(), formattedDate, compactDate);
        String targetPath = replaceDatePlaceholders(syncConfig.getTargetPath(), formattedDate, compactDate);

        String fileName = "BR202501_result_" + yesterdayStr + ".csv";
        syncConfig.setSrcPath(srcPath);
        syncConfig.setTargetPath(targetPath);

        Boolean flag = Boolean.TRUE;
        if (transferActionFront.getStatus() == 1) {
            FtpClient ftpClient = new FtpClient(syncConfig, true);
            flag = ftpFileList(ftpClient, syncConfig, fileName, transferActionFront);
        }
        //文件处理
        TransferActionFront front = transferActionFrontMapper.selectByPrimaryKey(transferActionFront.getId());
        if (flag && front.getStatus() == 3) {
            workWithFiles(apiCode, syncConfig, fileName, transferActionFront.getId());
        }
    }

    /**
     * 替换路径中的日期占位符
     */
    private String replaceDatePlaceholders(String path, String formattedDate, String compactDate) {
        if (path == null) return path;

        if (path.contains("yyyy-MM-dd")) {
            return path.replace("yyyy-MM-dd", formattedDate);
        } else if (path.contains("yyyyMMdd")) {
            return path.replace("yyyyMMdd", compactDate);
        }
        return path;
    }

    private Boolean ftpFileList(FtpClient ftpClient, SyncConfig syncConfig, String fileName, TransferActionFront transferActionFront) {
        String srcPath = syncConfig.getSrcPath();
        String targetPath = syncConfig.getTargetPath();
        try {
            ftpClient.connect();
            boolean fileExists = ftpClient.isExistFile(srcPath.concat(fileName));
            if (!fileExists) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.HAIER_SERVICEERROR.getCode(), TITLE + "文件不存在：" + srcPath.concat(fileName)));
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

                log.warn(TITLE + "ftpClient推送success文件本地目录:{},远程目录:{}", targetPathConcat, srcPath + successName);
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
        jobManager.updateFrontDataStatus(transferActionFront.getId(), 3);
        return Boolean.TRUE;
    }

    private void workWithFiles(String apiCode, SyncConfig syncConfig, String fileName, Long jobId) {

        // 构建完整文件路径
        String path = Paths.get(syncConfig.getTargetPath(), fileName).toString();

        // 验证文件是否存在且可读
        File dataFile = new File(path);
        if (!dataFile.exists() || !dataFile.isFile()) {
            dataFile.mkdirs();
        }

        // 读取并处理文件内容
        try (InputStreamReader fReader = new InputStreamReader(Files.newInputStream(dataFile.toPath()), StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(fReader)) {

            List<TransferDataItemDTO> transferDataItems = new ArrayList<>();
            String line;
            int lineNumber = 0;
            int skippedLines = 0;
            // 每批处理1000条数据
            int batchSize = 1000;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // 跳过空行和标题行
                if (StringUtils.isBlank(line) || line.contains("UUID")) {
                    if (line.contains("UUID")) {
                        log.warn("{} 跳过表头 (第{}行): {}", TITLE, lineNumber, line);
                    }
                    skippedLines++;
                    continue;
                }
                // 解析CSV行数据
                String[] fields;
                try (CSVReader csvReader = new CSVReader(new StringReader(line))) {
                    fields = csvReader.readNext();
                } catch (Exception e) {
                    log.warn("{} 解析CSV行数据失败 (第{}行): {}. 错误: {}", TITLE, lineNumber, line, e.getMessage());
                    skippedLines++;
                    continue;
                }

                // 数据验证
                if (fields == null || fields.length < 12) {
                    log.warn("{} 数据列不足12列 (第{}行): {}", TITLE, lineNumber, line);
                    skippedLines++;
                    continue;
                }
                try {
                    TransferDataItemDTO item = buildTransferDataItem(apiCode, fields);
                    transferDataItems.add(item);

                    // 每积累1000条数据就推送一次
                    if (transferDataItems.size() >= batchSize) {
                        PushTransferDataDetailDTO dto = buildTransferDataDTO(apiCode, transferDataItems);
                        Result pushResult = pushInfoService.pushTransferByRetry(dto, null);

                        log.warn("{} 批量推送结果 - 状态码: {}, 成功: {}, 消息: {}",
                                TITLE, pushResult.getCode(), pushResult.isSuccess(), pushResult.getMessage());

                        if (!ResultCode.SUCCESS.getValue().equals(pushResult.getCode())) {
                            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.HAIER_SERVICEERROR.getCode(),
                                    TITLE + "推送失败结果：" + pushResult.getMessage()));
                        }

                        transferDataItems.clear(); // 清空当前批次
                    }
                } catch (Exception e) {
                    log.warn("{} 解析数据失败 (第{}行): {}. 错误: {}", TITLE, lineNumber, line, e.getMessage());
                    skippedLines++;
                }
            }

            // 处理最后不足1000条的剩余数据
            if (!transferDataItems.isEmpty()) {
                PushTransferDataDetailDTO dto = buildTransferDataDTO(apiCode, transferDataItems);
                Result pushResult = pushInfoService.pushTransferByRetry(dto, null);

                log.warn("{} 最后批次推送结果 - 状态码: {}, 成功: {}, 消息: {}",
                        TITLE, pushResult.getCode(), pushResult.isSuccess(), pushResult.getMessage());

                if (!ResultCode.SUCCESS.getValue().equals(pushResult.getCode())) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.HAIER_SERVICEERROR.getCode(),
                            TITLE + "推送失败结果：" + pushResult.getMessage()));
                }
            }

            // 处理结果统计
            int processedCount = lineNumber - skippedLines;
            log.warn("{} 处理完成 - 总行数: {}, 成功处理: {}, 跳过: {}",
                    TITLE, lineNumber, processedCount, skippedLines);
            // 更新任务状态
            jobManager.updateFrontDataStatus(jobId, 2);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.HAIER_SERVICEERROR.getCode(),
                    TITLE + "文件解析异常：" + e.getMessage()));
        }
    }

    /**
     * 构建传输数据项DTO
     */
    private TransferDataItemDTO buildTransferDataItem(String apiCode, String[] fields) {
        TransferDataItemDTO item = new TransferDataItemDTO();

        // 设置基本字段
        item.setApiCode(apiCode);
        item.setCustNum(StringUtils.trimToNull(fields[0]));
        item.setLoginTime(StringUtils.trimToNull(fields[1]));
        item.setAuditTime(StringUtils.trimToNull(fields[2]));
        item.setIfApply(validateBooleanField(fields[3]));
        item.setUserType(StringUtils.trimToNull(fields[4]));
        item.setApplyDt(StringUtils.trimToNull(fields[5]));
        item.setAuditAmount(StringUtils.trimToNull(fields[6]));
        item.setApplyResult(validateBooleanField(fields[7]));
        item.setIfLent(validateBooleanField(fields[10]));

        // 设置保留字段(JSON格式)
        JSONObject reserveData = new JSONObject();
        reserveData.put("applyLoan", validateBooleanField(fields[8]));
        reserveData.put("applyLoanTime", StringUtils.trimToNull(fields[9]));
        reserveData.put("applyLoanAmount", StringUtils.trimToNull(fields[11]));
        item.setReserveField1(reserveData.toJSONString());

        return item;
    }

    /**
     * 构建传输数据DTO
     */
    private PushTransferDataDetailDTO buildTransferDataDTO(String apiCode, List<TransferDataItemDTO> transferDataItems) {
        TransferDataDTO transferDataDTO = new TransferDataDTO();
        transferDataDTO.setDataItems(transferDataItems);
        Random random = new Random();
        int randomNumber = 10000 + random.nextInt(90000);
        String requestId = apiCode + "_" + System.currentTimeMillis() + "_" + randomNumber;
        transferDataDTO.setRequestId(requestId);

        PushTransferDataDetailDTO dto = new PushTransferDataDetailDTO();
        dto.setApiCode(apiCode);
        dto.setJsonData(JSON.toJSONString(transferDataDTO));
        return dto;
    }

    /**
     * 验证布尔类型字段(1/0)
     */
    private String validateBooleanField(String value) {
        value = StringUtils.trimToNull(value);
        if (value == null) {
            return value;
        }
        return "是".equals(value) ? "1" : "0";
    }

}
