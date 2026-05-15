package com.br.marketing.monkey.service.ningbo.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.DataTypeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.SyncConfig;
import com.br.marketing.entity.ningbo.FileReadConfig;
import com.br.marketing.entity.ningbo.NingBoDataTask;
import com.br.marketing.entity.ningbo.NingBoDataTaskExample;
import com.br.marketing.entity.ningbo.NingBoOriginalData;
import com.br.marketing.mapper.SyncConfigMapper;
import com.br.marketing.mapper.ningbo.FileReadConfigMapper;
import com.br.marketing.mapper.ningbo.NingBoDataTaskMapper;
import com.br.marketing.mapper.ningbo.NingBoOriginalDataMapper;
import com.br.marketing.monkey.enums.ningbo.TaskStatusEnum;
import com.br.marketing.monkey.enums.ningbo.TaskTypeEnum;
import com.br.marketing.monkey.service.ningbo.NingBoBankDataService;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.service.clean.common.GeneralDataCleanService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import com.nbopen.api.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NingBoBankDataServiceImpl implements NingBoBankDataService {

    public static final String TIME_FORMATTER = "yyyy-MM-dd";

    public static final String TIME_FORMATTER2 = "yyyyMMdd";

    private static final Pattern DATE_PATTERN_1 = Pattern.compile(Pattern.quote("yyyy-MM-dd"));

    private static final Pattern DATE_PATTERN_2 = Pattern.compile(Pattern.quote("yyyyMMdd"));

    private final static String TITLE = "【宁波银行】";

    private static final Random RANDOM = new Random();

    @Resource
    private MarketingCommonConfig commonConfig;

    @Resource
    private NingBoDataTaskMapper ningBoDataTaskMapper;

    @Resource
    private NingBoOriginalDataMapper ningBoOriginalDataMapper;

    @Resource
    private FileReadConfigMapper fileReadConfigMapper;

    @Resource
    private PushInfoService pushInfoService;

    @Resource
    private SyncConfigMapper syncConfigMapper;

    @Resource
    private SyncConfigService syncConfigService;

    @Resource
    private GeneralDataCleanService generalDataCleanService;

    @Override
    public void downloadFile(Date collectDate) {
        NingBoDataTaskExample example = new NingBoDataTaskExample();
        example.createCriteria().andTaskTypeEqualTo(TaskTypeEnum.DOWNLOAD.getCode())
                .andTaskDateEqualTo(collectDate)
                .andStatusIn(Arrays.asList(
                        TaskStatusEnum.RUNNING.getCode(),
                        TaskStatusEnum.SUCCESS.getCode()
                ));
        if (ningBoDataTaskMapper.countByExample(example) > 0) {
            return;
        }

        NingBoDataTask currentTask = new NingBoDataTask();
        currentTask.setTaskDate(collectDate);
        currentTask.setStatus(1);
        currentTask.setTaskType(TaskTypeEnum.DOWNLOAD.getCode());
        ningBoDataTaskMapper.insertSelective(currentTask);
        try {
            JSONObject config = commonConfig.getNingboBankConfig();
            String apiCode = config.getString("apiCode");
            boolean mockEnable = config.getBoolean("mockEnable");
            int limit = config.getInteger("limit");

            FileReadConfig fieldConfig = fileReadConfigMapper.getActiveConfigByApiCode(apiCode);
            if (fieldConfig == null) {
                throw new RuntimeException("未找到对应的字段映射配置，apiCode: " + apiCode);
            }

            Map<String, String> fieldMapping = JSON.parseObject(fieldConfig.getFieldMapping(), LinkedHashMap.class);
            String separator = fieldConfig.getFileSeparator();
            String charset = fieldConfig.getFileCharset();

            String fileName = config.getString("fileName");
            fileName = replaceDate(fileName, LocalDate.now());
            String tempDir = syncConfigService.getPath() + apiCode + File.separator;
            String localFilePath = Paths.get(tempDir, fileName).toString();

            if (!mockEnable) {
                downloadFileFromBank(config, localFilePath, fileName);
            }
            log.warn("文件地址：{}", localFilePath);
            File downloadedFile = new File(localFilePath);
            if (!downloadedFile.exists() || downloadedFile.length() == 0) {
                throw new RuntimeException("文件下载失败，本地文件不存在或为空");
            }
            log.warn("文件下载成功，文件大小: {} 字节", downloadedFile.length());

            processFileContentBatched(
                    localFilePath, charset, fieldMapping, currentTask.getId(),
                    apiCode, collectDate, separator, limit, fileName
            );

            // 更新任务状态为成功
            ningBoDataTaskMapper.updateTaskStatus(currentTask.getId(), TaskStatusEnum.SUCCESS.getCode(),
                    "下载并入库成功");
            log.warn("宁波银行数据下载任务执行成功");
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.NINGBO_BANK_SERVICEERROR.getCode(), e.getMessage(), "宁波银行数据下载异常"), e
            );
            ningBoDataTaskMapper.updateTaskStatus(currentTask.getId(), TaskStatusEnum.FAILED.getCode(), e.getMessage());
        }
    }

    /**
     * 流式读取并分批处理文件
     */
    private void processFileContentBatched(String filePath, String charset, Map<String, String> fieldMapping,
                                           Long taskId, String apiCode, Date collectDate,
                                           String separator, int limit, String tempFileName) {
        String escapedSeparator = Pattern.quote(separator);
        AtomicInteger successCount = new AtomicInteger(0);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        TpDynamicExecutor executor = TpDynamicExecutorFactory.getThreadPool(
                ThreadPoolNameEnum.NINGBO_BANK.getName(), 50, 50);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath),
                        StringUtils.isNotBlank(charset) ? charset : StandardCharsets.UTF_8.name()))) {
            List<String> batchLines = new ArrayList<>(limit);
            int currentLineNum = 0;

            String line;
            while ((line = reader.readLine()) != null) {
                JSONObject config = commonConfig.getNingboBankConfig();
                if(config.getBoolean("downloadSwitch")) {
                    break;
                }

                currentLineNum++;
                batchLines.add(line);

                if (batchLines.size() >= limit) {
                    List<String> linesToProcess = new ArrayList<>(batchLines);
                    int finalCurrentLineNum = currentLineNum;
                    CompletableFuture<Void> future = CompletableFuture.runAsync(
                            () -> processBatchLines(linesToProcess, escapedSeparator,
                                    fieldMapping, taskId, apiCode, collectDate,
                                    finalCurrentLineNum - linesToProcess.size() + 1, successCount, tempFileName),
                            executor
                    );
                    futures.add(future);
                    batchLines.clear();
                }
            }

            if (!batchLines.isEmpty()) {
                int finalCurrentLineNum1 = currentLineNum;
                CompletableFuture<Void> future = CompletableFuture.runAsync(
                        () -> processBatchLines(batchLines, escapedSeparator,
                                fieldMapping, taskId, apiCode, collectDate,
                                finalCurrentLineNum1 - batchLines.size() + 1, successCount, tempFileName),
                        executor
                );
                futures.add(future);
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            log.warn("文件解析完成，成功处理{}条数据", successCount.get());
        } catch (IOException e) {
            log.warn(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.NINGBO_BANK_SERVICEERROR.getCode(), e.getMessage(), "文件读取失败"), e
            );
            throw new RuntimeException("读取文件失败", e);
        } finally {
            executor.shutdownAndAwaitTermination();
        }
    }

    /**
     * 处理批次数据
     */
    private void processBatchLines(List<String> batchLines, String escapedSeparator,
                                   Map<String, String> fieldMapping,
                                   Long taskId, String apiCode, Date collectDate,
                                   int startLineNum, AtomicInteger successCount, String tempFileName) {
        List<NingBoOriginalData> batchData = Lists.newArrayList();

        for (int i = 0; i < batchLines.size(); i++) {
            String line = batchLines.get(i);
            if (StringUtils.isBlank(line)) {
                continue;
            }

            int lineNum = startLineNum + i;
            try {
                String[] fields = line.split(escapedSeparator, -1);
                NingBoOriginalData data = new NingBoOriginalData();
                data.setTaskId(taskId);
                data.setTaskDate(new java.sql.Date(collectDate.getTime()));
                data.setApiCode(apiCode);

                JSONObject reserveFields = new JSONObject();

                List<String> dbFieldNames = new ArrayList<>(fieldMapping.values());

                for (int columnIndex = 0; columnIndex < fields.length; columnIndex++) {
                    String value = fields[columnIndex].trim();

                    if (columnIndex < dbFieldNames.size()) {
                        String dbFieldName = dbFieldNames.get(columnIndex);
                        if (StringUtils.isNotBlank(dbFieldName)) {
                            try {
                                BeanUtils.setProperty(data, dbFieldName, value);
                            } catch (Exception e) {
                                reserveFields.put("column_" + columnIndex, value);
                            }
                        } else {
                            if (StringUtils.isNotBlank(value)) {
                                reserveFields.put("column_" + columnIndex, value);
                            }
                        }
                    } else {
                        if (StringUtils.isNotBlank(value)) {
                            reserveFields.put("column_" + columnIndex, value);
                        }
                    }
                }

                if (!reserveFields.isEmpty()) {
                    data.setReserveField1(reserveFields.toJSONString());
                }
                batchData.add(data);
            } catch (Exception e) {
                String message = "第" + lineNum + "行数据解析失败: " + e.getMessage();
                log.warn(AlertLog.buildWarnMessage(message, "数据解析异常"), e);
            }
        }

        if (CollectionUtils.isEmpty(batchData)) {
            return;
        }
        try {
            ningBoOriginalDataMapper.batchSave(batchData);
            successCount.addAndGet(batchData.size());
            log.warn("线程{}成功入库{}条数据", Thread.currentThread().getName(), batchData.size());

            String tempTaskId = tempFileName.split("\\.")[0];
            List<JSONObject> jsonObjectList = batchData.stream()
                    .map(record -> {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("apiCode", record.getApiCode());
                        jsonObject.put("cell", record.getMoPhone());
                        jsonObject.put("custNum", record.getMoPhone());
                        jsonObject.put("operateType", 6);
                        JSONObject reserveField1 = JSONObject.parseObject(JSON.toJSONString(record));
                        reserveField1.put("taskId", tempTaskId);
                        jsonObject.put("reserveField1", reserveField1.toJSONString());
                        return jsonObject;
                    }).collect(Collectors.toList());
            Result uploadResult = generalDataCleanService.uploadClean(jsonObjectList, apiCode);

            if (uploadResult == null || !uploadResult.isSuccess()) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.NINGBO_BANK_SERVICEERROR.getCode(),
                        TITLE + " 数据清洗失败", null));
            } else {
                List<MarketingPreUserDetailDTO> transferDataItemDTOS = (List<MarketingPreUserDetailDTO>) uploadResult.getData();
                UploadDataDTO dto = initUploadData(apiCode, transferDataItemDTOS, tempTaskId);
                Result pushResult = pushInfoService.pushUploadByRetry(dto, null);
                log.warn("{},调用push接口 code:{},isSuccess:{},msg:{}", TITLE,
                        pushResult.getCode(), pushResult.isSuccess(), pushResult.getMessage());
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.NINGBO_BANK_SERVICEERROR.getCode(),
                    TITLE + " 数据清洗异常", null), e);
        }
    }

    private UploadDataDTO initUploadData(String apiCode, List<MarketingPreUserDetailDTO> syncUsers, String taskId) {
        int randomNumber = 10000 + RANDOM.nextInt(90000);
        String requestId = apiCode + "_" + taskId + "_" + System.currentTimeMillis() + randomNumber;
        MarketingPreUserDTO marketingPreUserDTO = new MarketingPreUserDTO();
        marketingPreUserDTO.setRequestId(requestId);
        marketingPreUserDTO.setDataItems(syncUsers);
        marketingPreUserDTO.setTaskId(taskId);
        UploadDataDTO uploadDataDTO = new UploadDataDTO();
        uploadDataDTO.setApiCode(apiCode);
        uploadDataDTO.setJsonData(JSON.toJSONString(marketingPreUserDTO));
        return uploadDataDTO;
    }

    private String replaceDate(String input, LocalDate date) {
        if (input == null || date == null) {
            return input;
        }

        if (input.contains(TIME_FORMATTER)) {
            String newDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
            return DATE_PATTERN_1.matcher(input).replaceAll(newDate);
        }

        if (input.contains(TIME_FORMATTER2)) {
            String newDate = date.format(DateTimeFormatter.BASIC_ISO_DATE);
            return DATE_PATTERN_2.matcher(input).replaceAll(newDate);
        }
        return input;
    }

    /**
     * 从宁波银行下载文件
     */
    private void downloadFileFromBank(JSONObject config, String localFilePath, String fileName) throws Exception {
        NBOpenSDK.setSDKLogLevel(SDKLogLevel.DEBUG);
        String sdkFilePath = config.getString("sdkFilePath");
        log.warn("初始化SDK, SDK配置文件路径: {}", sdkFilePath);
        ClassPathResource resource = new ClassPathResource(sdkFilePath);
        if (!resource.exists()) {
            throw new FileNotFoundException("SDK配置文件不存在: " + sdkFilePath);
        }
        try (InputStream inputStream = resource.getInputStream()) {
            byte[] data = inputStream.readAllBytes();
            try(ByteArrayInputStream initStream = new ByteArrayInputStream(data)) {
                NBOpenSDK.init(initStream);
            }
            try(ByteArrayInputStream updateStream = new ByteArrayInputStream(data)) {
                NBOpenSDK.updateConfig(updateStream);
            }
        }

        SDKRequest request = new SDKRequest();
        RequestHead head = new RequestHead();
        head.setRqsJrnlNo(NBOpenSDK.getRandom());
        request.setHead(head);

        RequestFileData fileData = new RequestFileData();
        fileData.setLocalFilePath(localFilePath);

        String newDate = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        fileData.setRemoteFileName("/" + newDate + "/" + fileName);
        fileData.setTranCode(config.getString("downloadTranCode"));
        fileData.setUid(config.getString("uid"));
        request.setData(fileData);

        log.warn("开始下载宁波银行文件，保存路径: {}", localFilePath);
        SDKResponse response = NBOpenSDK.getFile(request);

        if (response == null || response.getHead() == null) {
            throw new RuntimeException("SDK文件下载失败: " + (response != null ? response.toString() : "响应为空"));
        }
    }

    @Override
    public void uploadFile(Date collectDate) {
        NingBoDataTaskExample example = new NingBoDataTaskExample();
        example.createCriteria().andTaskTypeEqualTo(TaskTypeEnum.UPLOAD.getCode())
                .andTaskDateEqualTo(collectDate)
                .andStatusIn(Arrays.asList(
                        TaskStatusEnum.RUNNING.getCode(),
                        TaskStatusEnum.SUCCESS.getCode()
                ));
        if (ningBoDataTaskMapper.countByExample(example) > 0) {
            return;
        }

        NingBoDataTask currentTask = new NingBoDataTask();
        currentTask.setTaskDate(collectDate);
        currentTask.setStatus(1);
        currentTask.setTaskType(TaskTypeEnum.UPLOAD.getCode());
        ningBoDataTaskMapper.insertSelective(currentTask);

        try {
            JSONObject config = commonConfig.getNingboBankConfig();
            if(config.getBoolean("uploadSwitch")) {
                return;
            }
            SyncConfig syncConfig = new SyncConfig();
            syncConfig.setApiCode(config.getString("apiCode"));
            syncConfig.setDataType(DataTypeEnum.MARKETING_DATA_NO_HEADER.getValue());
            syncConfig.setType(1);
            syncConfig = syncConfigMapper.queryConfigByConditaion(syncConfig);

            String remoteFileName = config.getString("uploadFileName");
            remoteFileName = replaceDate(remoteFileName, LocalDate.now());

            String localFilePath = syncConfig.getTargetPath() + remoteFileName;
            File uploadFile = new File(localFilePath);
            if (!uploadFile.exists() || uploadFile.length() == 0) {
                log.warn("上传文件不存在或为空，文件路径: {}", localFilePath);
                return;
            }

            log.warn("开始上传宁波银行文件，本地路径: {}，远程文件名: {}", localFilePath, remoteFileName);

            SDKResponse response = uploadFileToBank(config, localFilePath, remoteFileName);
            if (response == null || response.getHead() == null) {
                throw new RuntimeException("SDK文件上传失败: " + (response != null ? response.toString() : "响应为空"));
            }

            ningBoDataTaskMapper.updateTaskStatus(currentTask.getId(), TaskStatusEnum.SUCCESS.getCode(),
                    "文件上传成功，文件大小: " + uploadFile.length() + " 字节");
            log.warn("宁波银行文件上传任务执行成功，远程文件名: {}", remoteFileName);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.NINGBO_BANK_SERVICEERROR.getCode(), e.getMessage(), "宁波银行数据上传异常"), e
            );
            ningBoDataTaskMapper.updateTaskStatus(currentTask.getId(), TaskStatusEnum.FAILED.getCode(), e.getMessage());
        }
    }

    /**
     * 上传文件到宁波银行
     */
    private SDKResponse uploadFileToBank(JSONObject config, String localFilePath, String remoteFileName) {
        try {
            String sdkFilePath = config.getString("sdkFilePath");
            log.warn("初始化SDK, SDK配置文件路径: {}", sdkFilePath);
            NBOpenSDK.setSDKLogLevel(SDKLogLevel.DEBUG);
            ClassPathResource resource = new ClassPathResource(sdkFilePath);
            if (!resource.exists()) {
                throw new FileNotFoundException("SDK配置文件不存在: " + sdkFilePath);
            }
            try (InputStream inputStream = resource.getInputStream()) {
                byte[] data = inputStream.readAllBytes();
                try(ByteArrayInputStream initStream = new ByteArrayInputStream(data)) {
                    NBOpenSDK.init(initStream);
                }
                try(ByteArrayInputStream updateStream = new ByteArrayInputStream(data)) {
                    NBOpenSDK.updateConfig(updateStream);
                }
            }

            SDKRequest request = new SDKRequest();
            RequestHead head = new RequestHead();
            head.setRqsJrnlNo(NBOpenSDK.getRandom());
            request.setHead(head);

            byte[] fileBytes = readFileBytes(localFilePath);

            RequestFileData fileData = new RequestFileData();
            fileData.setLocalFileArray(fileBytes);
            fileData.setRemoteFileName(remoteFileName);
            fileData.setTranCode(config.getString("uploadTranCode"));
            fileData.setUid(config.getString("uid"));
            request.setData(fileData);

            return NBOpenSDK.putFile(request);

        } catch (Exception e) {
            throw new RuntimeException("调用宁波银行SDK上传文件失败", e);
        }
    }

    /**
     * 读取文件字节数组
     */
    private byte[] readFileBytes(String filePath) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {
            byte[] buffer = new byte[(int) raf.length()];
            raf.readFully(buffer);
            return buffer;
        }
    }
}