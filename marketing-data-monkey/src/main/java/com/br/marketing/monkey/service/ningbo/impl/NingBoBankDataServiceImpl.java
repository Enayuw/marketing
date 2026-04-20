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
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NingBoBankDataServiceImpl implements NingBoBankDataService {

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
                .andStatusGreaterThan(TaskStatusEnum.WAITING.getCode());
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
            String filePrefix = config.getString("filePrefix");
            boolean mockEnable = config.getBoolean("mockEnable");
            int limit = config.getInteger("limit");

            FileReadConfig fieldConfig = fileReadConfigMapper.getActiveConfigByApiCode(apiCode);
            if (fieldConfig == null) {
                throw new RuntimeException("未找到对应的字段映射配置，apiCode: " + apiCode);
            }

            Map<String, String> fieldMapping = JSON.parseObject(fieldConfig.getFieldMapping(), LinkedHashMap.class);
            String separator = fieldConfig.getFileSeparator();
            String charset = fieldConfig.getFileCharset();

            String timestamp = String.valueOf(System.currentTimeMillis());
            String tempFileName = filePrefix + timestamp + ".txt";
            String tempDir = syncConfigService.getPath() + apiCode + File.separator;
            String localFilePath = Paths.get(tempDir, tempFileName).toString();
//            String localFilePath = "D:\\Program Files\\stocks\\baostock_download\\orginal_bank2br_20260419.txt";

            if (!mockEnable) {
                downloadFileFromBank(collectDate, config, localFilePath, filePrefix);
            }

            File downloadedFile = new File(filePrefix + ".txt");
            if (!downloadedFile.exists() || downloadedFile.length() == 0) {
                throw new RuntimeException("文件下载失败，本地文件不存在或为空");
            }
            log.warn("文件下载成功，文件大小: {} 字节", downloadedFile.length());

            processFileContentBatched(
                    localFilePath, charset, fieldMapping, currentTask.getId(),
                    apiCode, collectDate, separator, limit
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
            throw new RuntimeException("宁波银行数据下载任务执行失败", e);
        }
    }

    /**
     * 流式读取并分批处理文件
     */
    private void processFileContentBatched(String filePath, String charset, Map<String, String> fieldMapping,
                                           Long taskId, String apiCode, Date collectDate,
                                           String separator, int limit) {
        String escapedSeparator = Pattern.quote(separator);
        Map<String, Integer> headerIndexMap;
        AtomicInteger successCount = new AtomicInteger(0);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        TpDynamicExecutor executor = TpDynamicExecutorFactory.getThreadPool(
                ThreadPoolNameEnum.NINGBO_BANK.getName(), 50, 50);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath),
                        StringUtils.isNotBlank(charset) ? charset : StandardCharsets.UTF_8.name()))) {

            String headerLine = reader.readLine();
            if (StringUtils.isBlank(headerLine)) {
                log.warn("文件内容为空，无数据可解析");
                return;
            }

            String[] headers = headerLine.split(escapedSeparator, -1);
            headerIndexMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                String header = headers[i].trim();
                if (StringUtils.isNotBlank(header)) {
                    headerIndexMap.put(header, i);
                }
            }

            List<String> batchLines = new ArrayList<>(limit);
            int currentLineNum = 1;
            String line;
            while ((line = reader.readLine()) != null) {
                currentLineNum++;
                batchLines.add(line);

                if (batchLines.size() >= limit) {
                    List<String> linesToProcess = new ArrayList<>(batchLines);
                    int finalCurrentLineNum = currentLineNum;
                    CompletableFuture<Void> future = CompletableFuture.runAsync(
                            () -> processBatchLines(linesToProcess, escapedSeparator, headerIndexMap,
                                    fieldMapping, taskId, apiCode, collectDate,
                                    finalCurrentLineNum - linesToProcess.size() + 1, successCount),
                            executor
                    );
                    futures.add(future);
                    batchLines.clear();
                }
            }

            if (!batchLines.isEmpty()) {
                int finalCurrentLineNum1 = currentLineNum;
                CompletableFuture<Void> future = CompletableFuture.runAsync(
                        () -> processBatchLines(batchLines, escapedSeparator, headerIndexMap,
                                fieldMapping, taskId, apiCode, collectDate,
                                finalCurrentLineNum1 - batchLines.size() + 1, successCount),
                        executor
                );
                futures.add(future);
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            log.warn("文件解析完成，成功处理{}条数据", successCount.get());
        } catch (IOException e) {
            log.error("读取文件失败: {}", e.getMessage(), e);
            throw new RuntimeException("读取文件失败", e);
        } finally {
            executor.shutdownAndAwaitTermination();
        }
    }

    /**
     * 处理批次数据
     */
    private void processBatchLines(List<String> batchLines, String escapedSeparator,
                                   Map<String, Integer> headerIndexMap, Map<String, String> fieldMapping,
                                   Long taskId, String apiCode, Date collectDate,
                                   int startLineNum, AtomicInteger successCount) {
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
                for (Map.Entry<String, Integer> headerEntry : headerIndexMap.entrySet()) {
                    String fileFieldName = headerEntry.getKey();
                    int columnIndex = headerEntry.getValue();
                    String value = (columnIndex < fields.length) ? fields[columnIndex].trim() : null;
                    String dbFieldName = fieldMapping.get(fileFieldName);

                    if (StringUtils.isNotBlank(dbFieldName)) {
                        try {
                            BeanUtils.setProperty(data, dbFieldName, value);
                        } catch (Exception e) {
                            reserveFields.put(fileFieldName, value);
                        }
                    } else {
                        if (StringUtils.isNotBlank(value)) {
                            reserveFields.put(fileFieldName, value);
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

            List<JSONObject> jsonObjectList = batchData.stream()
                    .map(record -> {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("apiCode", record.getApiCode());
                        jsonObject.put("cell", record.getMoPhone());
                        jsonObject.put("custNum", record.getMoPhone());
                        jsonObject.put("operateType", 6);
                        jsonObject.put("reserveField1", JSON.toJSONString(record));
                        return jsonObject;
                    }).collect(Collectors.toList());
            Result uploadResult = generalDataCleanService.uploadClean(jsonObjectList, apiCode);

            if (uploadResult == null || !uploadResult.isSuccess()) {
                log.error(AlertLog.buildErrorMessage(AlarmSendCodeEnum.NINGBO_BANK_SERVICEERROR.getCode(),
                        TITLE + " 数据清洗失败", null));
            } else {
                List<MarketingPreUserDetailDTO> transferDataItemDTOS = (List<MarketingPreUserDetailDTO>) uploadResult.getData();
                UploadDataDTO dto = initUploadData(apiCode, transferDataItemDTOS);
                Result pushResult = pushInfoService.pushUploadByRetry(dto, null);
                log.warn("{},调用push接口 code:{},isSuccess:{},msg:{}", TITLE,
                        pushResult.getCode(), pushResult.isSuccess(), pushResult.getMessage());
            }
        } catch (Exception e) {
            log.error(AlertLog.buildErrorMessage(AlarmSendCodeEnum.NINGBO_BANK_SERVICEERROR.getCode(),
                    TITLE + " 数据清洗异常", null), e);
        }
    }

    private UploadDataDTO initUploadData(String apiCode, List<MarketingPreUserDetailDTO> syncUsers) {
        int randomNumber = 10000 + RANDOM.nextInt(90000);
        String requestId = apiCode + "_" + System.currentTimeMillis() + "_" + randomNumber;
        MarketingPreUserDTO marketingPreUserDTO = new MarketingPreUserDTO();
        marketingPreUserDTO.setRequestId(requestId);
        marketingPreUserDTO.setDataItems(syncUsers);
        marketingPreUserDTO.setTaskId(apiCode + "_" + LocalDate.now());
        UploadDataDTO uploadDataDTO = new UploadDataDTO();
        uploadDataDTO.setApiCode(apiCode);
        uploadDataDTO.setJsonData(JSON.toJSONString(marketingPreUserDTO));
        return uploadDataDTO;
    }

    /**
     * 从宁波银行下载文件
     */
    private void downloadFileFromBank(Date collectDate, JSONObject config, String localFilePath, String filePrefix) {
        try {
            SDKRequest request = new SDKRequest();
            RequestHead head = new RequestHead();
            head.setRqsJrnlNo(NBOpenSDK.getRandom());
            request.setHead(head);

            RequestFileData fileData = new RequestFileData();
            fileData.setLocalFilePath(localFilePath);
            fileData.setRemoteFileName("orginal_bank2br_" + DateUtil.format(collectDate, "yyyyMMdd") + ".txt");
            fileData.setTranCode(config.getString("tranCode"));
            fileData.setUid(config.getString("uid"));
            request.setData(fileData);

            log.warn("开始下载宁波银行文件，保存路径: {}", localFilePath);
            SDKResponse response = NBOpenSDK.getFile(request);

            if (response == null || response.getHead() == null || !"SUCCESS".equals(response.getHead().getRspCode())) {
                throw new RuntimeException("SDK文件下载失败: " + (response != null ? response.toString() : "响应为空"));
            }

        } catch (Exception e) {
            throw new RuntimeException("调用宁波银行SDK下载文件失败", e);
        }
    }

    @Override
    public void uploadFile(Date collectDate) {
        NingBoDataTask currentTask = ningBoDataTaskMapper.createOrUpdateRunningTask(collectDate, TaskTypeEnum.UPLOAD.getCode());
        if (currentTask == null || Objects.equals(currentTask.getStatus(), TaskStatusEnum.SUCCESS.getCode())) {
            return;
        }

        try {
            JSONObject config = commonConfig.getNingboBankConfig();

            SyncConfig syncConfig = new SyncConfig();
            syncConfig.setApiCode(config.getString("apiCode"));
            syncConfig.setDataType(DataTypeEnum.TRANSFER.getValue());
            syncConfig = syncConfigMapper.queryConfigByConditaion(syncConfig);

            String localFilePath = syncConfig.getSrcPath();
            String filePrefix = config.getString("filePrefix");
            File uploadFile = new File(localFilePath);
            if (!uploadFile.exists() || uploadFile.length() == 0) {
                log.warn("上传文件不存在或为空，文件路径: {}", localFilePath);
                return;
            }
            String remoteFileName = filePrefix + DateUtil.format(collectDate, "yyyyMMdd") + ".txt";
            log.warn("开始上传宁波银行文件，本地路径: {}，远程文件名: {}", localFilePath, remoteFileName);

            SDKResponse response = uploadFileToBank(config, localFilePath, remoteFileName);
            if (response == null || response.getHead() == null || !"SUCCESS".equals(response.getHead().getRspCode())) {
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
            throw new RuntimeException("宁波银行数据上传任务执行失败", e);
        }
    }

    /**
     * 上传文件到宁波银行
     */
    private SDKResponse uploadFileToBank(JSONObject config, String localFilePath, String remoteFileName) {
        try {
            SDKRequest request = new SDKRequest();
            RequestHead head = new RequestHead();
            head.setRqsJrnlNo(NBOpenSDK.getRandom());
            request.setHead(head);

            byte[] fileBytes = readFileBytes(localFilePath);

            RequestFileData fileData = new RequestFileData();
            fileData.setLocalFileArray(fileBytes);
            fileData.setRemoteFileName(remoteFileName);
            fileData.setTranCode(config.getString("tranCode"));
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