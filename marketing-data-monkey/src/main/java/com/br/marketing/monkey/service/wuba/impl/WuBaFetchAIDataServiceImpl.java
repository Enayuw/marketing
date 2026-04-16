package com.br.marketing.monkey.service.wuba.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpStatus;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.marketingapi.input.PushTransferDataDetailDTO;
import com.br.marketing.client.wuba.WuBaAIClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.wuba.WuBaAiConversionData;
import com.br.marketing.entity.wuba.WuBaAiFetchTask;
import com.br.marketing.entity.wuba.WuBaAiFetchTaskExample;
import com.br.marketing.enums.TcRecordCleanStatusEnum;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.mapper.wuba.WuBaAiConversionDataMapper;
import com.br.marketing.mapper.wuba.WuBaAiFetchTaskMapper;
import com.br.marketing.monkey.service.wuba.WuBaFetchAIDataService;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.service.clean.common.GeneralDataCleanService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import io.micrometer.core.instrument.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Service
public class WuBaFetchAIDataServiceImpl implements WuBaFetchAIDataService {

    private final static String TITLE = "【58AI】-转化数据拉取";

    public static final String FILENAME = "filename";

    private static final Pattern CSV_SPLIT_PATTERN = Pattern.compile("\\n");

    private static final Pattern FILENAME_PATTERN = Pattern.compile(FILENAME + "=([^\"]*)");

    private static final Random RANDOM = new Random();

    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;

    @Resource
    private GeneralDataCleanService generalDataCleanService;

    @Resource
    private WuBaAiConversionDataMapper conversionDataMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private WuBaAiFetchTaskMapper fetchTaskMapper;

    @Resource
    private SyncConfigService syncConfigService;

    @Resource
    private PushInfoService pushInfoService;

    @Resource
    private WuBaAIClient wuBaAIClient;


    private static final List<String> EXPECTED_HEADERS = Arrays.asList(
            "dwEventTime", "mobileEncrypt", "userType", "lastLoginTime", "debtTime", "debtApplyTime", "debtPassTime"
    );

    @Override
    public void fetchAndProcessData(Date collectDate) {
        JSONObject wuBaAIConfig = marketingCommonConfig.getWuBaAIConfig();
        String orgCode = wuBaAIConfig.getString("orgCode");
        String password = wuBaAIConfig.getString("password");
        String apiCode = wuBaAIConfig.getString("apiCode");
        Integer limit = wuBaAIConfig.getInteger("limit");
        Integer userTypeTruncate  = wuBaAIConfig.getInteger("userTypeTruncate");
        String baseFilePath = syncConfigService.getPath();

        String dateStr = DateUtil.format(collectDate, "yyyyMMdd");
        log.warn("{} 开始处理数据拉取任务，机构: {}, 日期: {}", TITLE, orgCode, dateStr);

        WuBaAiFetchTaskExample example = new WuBaAiFetchTaskExample();
        example.createCriteria()
                .andCollectDateEqualTo(collectDate)
                .andStatusIn(Arrays.asList(1, 2));
        if (fetchTaskMapper.countByExample(example) > 0) {
            log.warn("{} 已存在处理中或成功任务(collectDate={})，跳过本次执行", TITLE, collectDate);
            return;
        }

        WuBaAiFetchTask task = new WuBaAiFetchTask();
        task.setCollectDate(collectDate);
        task.setStatus(0);
        fetchTaskMapper.insertSelective(task);
        Long taskId = task.getId();

        try {
            log.warn("{} 开始调用58接口，taskId: {}", TITLE, taskId);
            HttpResponse response = wuBaAIClient.downloadConversionFile(orgCode, password);
            if (response == null) {
                String errorMsg = AlertLog.buildErrorMessage(AlarmSendCodeEnum.WUBA_AI_SERVICEERROR.getCode(),
                        TITLE + " 接口调用失败，未获取到响应，taskId:" + taskId, null);
                log.error(errorMsg);
                return;
            }

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.HTTP_OK) {
                String errorBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                String errorMsg = AlertLog.buildErrorMessage(AlarmSendCodeEnum.WUBA_AI_SERVICEERROR.getCode(),
                        TITLE + " 接口请求失败，taskId:" + taskId + ", 状态码:" + statusCode + ", 返回:" + errorBody, null);
                log.error(errorMsg);
                EntityUtils.consumeQuietly(response.getEntity());
                return;
            }
            Header contentTypeHeader = response.getFirstHeader("Content-Type");
            String contentType = contentTypeHeader != null ? contentTypeHeader.getValue() : null;
            if (contentType != null && contentType.contains("application/json")) {
                String jsonBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                EntityUtils.consumeQuietly(response.getEntity());
                JSONObject jsonResponse = JSON.parseObject(jsonBody);
                String errorMsg = AlertLog.buildErrorMessage(AlarmSendCodeEnum.WUBA_AI_SERVICEERROR.getCode(),
                        TITLE + " 接口返回业务错误，taskId:" + taskId + ", code=" + jsonResponse.getString("code") +
                                ", message=" + jsonResponse.getString("message"), null);
                log.error(errorMsg);
            } else if (contentType != null && contentType.contains("application/octet-stream")) {
                byte[] fileBytes = EntityUtils.toByteArray(response.getEntity());
                try {
                    saveFileToTempPath(response, fileBytes, task, baseFilePath, apiCode, dateStr);
                } catch (Exception saveEx) {
                    log.error(AlertLog.buildErrorMessage(AlarmSendCodeEnum.WUBA_AI_SERVICEERROR.getCode(),
                            TITLE + " 保存临时文件失败，taskId:" + taskId), saveEx);
                }
                processDataFromBytes(fileBytes, task, limit, apiCode, userTypeTruncate);
                task.setStatus(2);
                fetchTaskMapper.updateByPrimaryKeySelective(task);
                log.warn("{} 任务处理完成，taskId: {}, 处理{}条数据", TITLE, taskId, task.getSuccessCount());
            } else {
                EntityUtils.consumeQuietly(response.getEntity());
                String errorMsg = AlertLog.buildErrorMessage(AlarmSendCodeEnum.WUBA_AI_SERVICEERROR.getCode(),
                        TITLE + " 未知的响应类型: " + contentType + ", taskId:" + taskId, null);
                log.error(errorMsg);
            }
        } catch (Exception e) {
            String errorMsg = AlertLog.buildErrorMessage(AlarmSendCodeEnum.WUBA_AI_SERVICEERROR.getCode(),
                    TITLE + " 数据处理过程发生异常，taskId:" + taskId);
            log.error(errorMsg, e);
            task.setStatus(3);
            task.setErrorMessage(StrUtil.subPre(e.getMessage(), 2000));
            fetchTaskMapper.updateByPrimaryKeySelective(task);
        }
    }

    /**
     * 保存文件到临时路径
     * 此方法独立，即使失败也不应影响主流程
     */
    private File saveFileToTempPath(HttpResponse response, byte[] fileBytes, WuBaAiFetchTask task, String baseFilePath,
                                    String apiCode, String dateStr) {
        String fileName = "download.zip";
        Header dispositionHeader = response.getFirstHeader("Content-Disposition");
        if (dispositionHeader != null && dispositionHeader.getValue() != null) {
            String contentDisposition = dispositionHeader.getValue();
            Matcher matcher = FILENAME_PATTERN.matcher(contentDisposition);
            if (matcher.find()) {
                fileName = matcher.group(1);
            }
        }
        String saveDirPath = baseFilePath + apiCode + File.separator + dateStr + File.separator;
        File saveDir = new File(saveDirPath);
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }
        File tempFile = new File(saveDirPath + fileName);
        FileUtil.writeBytes(fileBytes, tempFile);
        task.setFileName(fileName);
        task.setFilePath(tempFile.getAbsolutePath());
        fetchTaskMapper.updateByPrimaryKeySelective(task);
        return tempFile;
    }

    private void processDataFromBytes(byte[] zipFileBytes, WuBaAiFetchTask task, Integer limit, String apiCode,
                                      Integer userTypeTruncate) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(zipFileBytes);
             ZipInputStream zis = new ZipInputStream(bais, StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    String csvContent = IOUtils.toString(zis, StandardCharsets.UTF_8).replaceAll("\uFEFF", "");
                    processCsvContent(csvContent, task, limit, apiCode, userTypeTruncate);
                    break;
                }
            }
        } catch (Exception e) {
            String errorMsg = AlertLog.buildErrorMessage(AlarmSendCodeEnum.WUBA_AI_SERVICEERROR.getCode(),
                    TITLE + " 从字节流解析ZIP/CSV数据失败，taskId:" + task.getId());
            log.error(errorMsg, e);
            throw new IOException(errorMsg, e);
        }
    }

    private void processCsvContent(String csvContent, WuBaAiFetchTask task, Integer limit, String apiCode, Integer userTypeTruncate) {
        String[] lines = CSV_SPLIT_PATTERN.split(csvContent);
        if (lines.length == 0) {
            log.warn("{} CSV内容无有效行，taskId: {}", TITLE, task.getId());
            task.setTotalCount(0);
            task.setSuccessCount(0);
            return;
        }

        String headerLine = lines[0];
        List<String> actualHeaders = Arrays.stream(headerLine.split(",")).map(String::trim).toList();
        List<WuBaAiConversionData> dataList = new ArrayList<>();
        int successCount = 0;
        int totalCount = lines.length - 1;
        List<String> extraHeaders = new ArrayList<>();
        Map<String, Integer> headerIndexMap = new HashMap<>();

        for (int i = 0; i < actualHeaders.size(); i++) {
            String header = actualHeaders.get(i).trim();
            headerIndexMap.put(header, i);
            if (!EXPECTED_HEADERS.contains(header)) {
                extraHeaders.add(header);
            }
        }

        if (!extraHeaders.isEmpty()) {
            log.warn("{} 检测到CSV中包含未定义的字段: {}，taskId: {}", TITLE, extraHeaders, task.getId());
        }

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (StrUtil.isBlank(line)) {
                continue;
            }
            String[] fields = line.split(",", -1);
            try {
                WuBaAiConversionData data = convertCsvLineToEntity(fields, headerIndexMap, extraHeaders, task.getId());
                dataList.add(data);
                successCount++;
                if (dataList.size() >= limit) {
                    saveBatchData(dataList, task);
                    pushTransferData(apiCode, dataList, userTypeTruncate);
                    dataList.clear();
                }
            } catch (Exception e) {
                log.error(AlertLog.buildErrorMessage(AlarmSendCodeEnum.WUBA_AI_SERVICEERROR.getCode(),
                        TITLE + " 解析CSV第" + (i + 1) + "行数据失败，taskId:" + task.getId()), e);
            }
        }

        if (CollectionUtils.isNotEmpty(dataList)) {
            pushTransferData(apiCode, dataList, userTypeTruncate);
            saveBatchData(dataList, task);
        }

        task.setTotalCount(totalCount);
        task.setSuccessCount(successCount);
        fetchTaskMapper.updateByPrimaryKeySelective(task);
        log.warn("{} CSV解析完成，共{}行，成功解析{}行，taskId: {}", TITLE, totalCount, successCount, task.getId());
    }

    private void pushTransferData(String apiCode, List<WuBaAiConversionData> dataList, Integer userTypeTruncate) {
        List<String> cellMD5List = dataList.stream().map(WuBaAiConversionData::getMobileEncrypt).toList();
        Map<String, MarketingSyncUser> syncUserMap = marketingSyncUserMapper.getSyncUserByMD5(apiCode, cellMD5List)
                .stream().collect(Collectors.toMap(MarketingSyncUser::getCellMd5, Function.identity()));

        List<JSONObject> jsonObjectList = dataList.stream()
                .map(record -> {
                    MarketingSyncUser syncUser = syncUserMap.get(record.getMobileEncrypt());
                    if (Objects.isNull(syncUser)) {
                        return null;
                    }
                    JSONObject reserveField1 = syncUser.getReserveField1() != null
                            ? JSONObject.parseObject(syncUser.getReserveField1())
                            : new JSONObject();
                    JSONObject recordJson = (JSONObject) JSONObject.toJSON(record);
                    reserveField1.putAll(recordJson);
                    EXPECTED_HEADERS.forEach(header -> {
                        String value = recordJson.getString(header);
                        reserveField1.put(header, Objects.nonNull(value) ? value : "");
                    });
                    String userType = keepFromRight13(record.getUserType(), userTypeTruncate);
                    reserveField1.put("userType", userType);
                    syncUser.setReserveField1(reserveField1.toJSONString());
                    syncUser.setUserType(userType);
                    return (JSONObject) JSONObject.toJSON(syncUser);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Result transferResult = generalDataCleanService.transferClean(jsonObjectList, apiCode);
        if (transferResult == null || !transferResult.isSuccess()) {
            log.error(AlertLog.buildErrorMessage(AlarmSendCodeEnum.WUBA_AI_SERVICEERROR.getCode(),
                    TITLE + " 数据清洗失败", null));
        } else {
            List<TransferDataItemDTO> transferDataItemDTOS = (List<TransferDataItemDTO>) transferResult.getData();
            PushTransferDataDetailDTO dto = initTransferData(apiCode, transferDataItemDTOS);
            Result pushResult = pushInfoService.pushTransferByRetry(dto, null);
            log.warn("{},调用push接口 code:{},isSuccess:{},msg:{}", TITLE,
                    pushResult.getCode(), pushResult.isSuccess(), pushResult.getMessage());
            if (pushResult.isSuccess()) {
                dataList.forEach(data -> data.setCleanStatus(TcRecordCleanStatusEnum.CLEAN_COMPLETED.getValue()));
            } else {
                dataList.forEach(data -> data.setCleanStatus(TcRecordCleanStatusEnum.CLEAN_PUSH.getValue()));
            }
        }
    }

    private static String keepFromRight13(String str, Integer userTypeTruncate) {
        if (str == null) {
            return null;
        }
        int length = str.length();
        if (length < userTypeTruncate) {
            return str;
        }
        int startIndex = 0;
        int endIndex = length - userTypeTruncate;
        return str.substring(startIndex, endIndex + 1);
    }

    private PushTransferDataDetailDTO initTransferData(String apiCode, List<TransferDataItemDTO> transferDataItems) {
        PushTransferDataDetailDTO dto = new PushTransferDataDetailDTO();
        TransferDataDTO transferDataDTO = new TransferDataDTO();
        transferDataDTO.setDataItems(transferDataItems);
        int randomNumber = 10000 + RANDOM.nextInt(90000);
        String requestId = apiCode + "_" + System.currentTimeMillis() + "_" + randomNumber;
        transferDataDTO.setRequestId(requestId);
        dto.setApiCode(apiCode);
        dto.setJsonData(JSON.toJSONString(transferDataDTO));
        return dto;
    }

    private WuBaAiConversionData convertCsvLineToEntity(String[] fields,
                                                        Map<String, Integer> headerIndexMap,
                                                        List<String> extraHeaders,
                                                        Long taskId) {
        WuBaAiConversionData data = new WuBaAiConversionData();
        data.setTaskId(taskId);

        data.setDwEventTime(getFieldValue("dwEventTime", fields, headerIndexMap));
        data.setMobileEncrypt(getFieldValue("mobileEncrypt", fields, headerIndexMap));
        data.setUserType(getFieldValue("userType", fields, headerIndexMap));
        data.setLastLoginTime(getFieldValue("lastLoginTime", fields, headerIndexMap));
        data.setDebtTime(getFieldValue("debtTime", fields, headerIndexMap));
        data.setDebtApplyTime(getFieldValue("debtApplyTime", fields, headerIndexMap));
        data.setDebtPassTime(getFieldValue("debtPassTime", fields, headerIndexMap));

        if (data.getDwEventTime() != null) {
            Date date = DateUtil.parse(data.getDwEventTime());
            Date expireDate = DateUtil.offsetDay(date, 7);
            String expireDateStr = DateUtil.formatDateTime(expireDate);
            data.setExpireDate(expireDateStr);
        }
        data.setInversionStatus(StringUtils.isNotEmpty(data.getDebtPassTime()) ? "0" : "1");
        data.setPushDecisionStatus("0");

        if (!extraHeaders.isEmpty()) {
            Map<String, String> extraFieldMap = new LinkedHashMap<>();
            for (String extraHeader : extraHeaders) {
                String value = getFieldValue(extraHeader, fields, headerIndexMap);
                if (StrUtil.isNotBlank(value)) {
                    extraFieldMap.put(extraHeader, value);
                }
            }
            if (!extraFieldMap.isEmpty()) {
                data.setReserveField(JSON.toJSONString(extraFieldMap));
            }
        }
        return data;
    }

    private void saveBatchData(List<WuBaAiConversionData> dataList, WuBaAiFetchTask task) {
        if (!dataList.isEmpty()) {
            dataList.forEach(conversionDataMapper::insertSelective);
            log.warn("{} 批量插入{}条数据成功，taskId: {}", TITLE, dataList.size(), task.getId());
        }
    }


    private String getFieldValue(String header, String[] fields, Map<String, Integer> headerIndexMap) {
        Integer index = headerIndexMap.get(header);
        if (index != null && index < fields.length) {
            String value = fields[index].trim();
            return value.isEmpty() ? null : value;
        }
        return null;
    }
}