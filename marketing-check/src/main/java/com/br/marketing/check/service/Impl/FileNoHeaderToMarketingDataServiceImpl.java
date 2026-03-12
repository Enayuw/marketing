package com.br.marketing.check.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.check.CkeckApplication;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.SftpClient;
import com.br.marketing.client.marketingapi.input.PushTransferDataDetailDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.commondto.SimpleResult;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.DataTypeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.mapper.MarketingDataFileConfigNoHeaderMapper;
import com.br.marketing.mapper.SyncConfigMapper;
import com.br.marketing.service.IFileActionService;
import com.br.marketing.service.IFileToMarketingRuleTransferService;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.vo.FileToMarketingDataFieldVO;
import com.br.marketing.vo.FileToMarketingFieldByColumnVO;
import com.marketingkit.tracking.model.indicator.DataFlowDirection;
import com.marketingkit.tracking.service.TrackingService;
import com.marketingkit.tracking.util.TrackingContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.curator.shaded.com.google.common.base.Splitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 文件无表头的转化数据清洗：按列顺序解析、列与配置不一致时告警、推送转化接口
 *
 * @author kongbx
 */
@Slf4j
@Service
public class FileNoHeaderToMarketingDataServiceImpl implements com.br.marketing.check.service.FileNoHeaderToMarketingDataService {

    private static final String TITLE = "【无表头文件转化清洗】";
    private static final String FILE_TYPE_NO_HEADER = "marketingDataNoHeader";
    private static final int PUSH_BATCH_SIZE = 500;

    @Value("${otherConfig.warning.sftpHost:00}")
    private String sftpHost;
    @Value("${otherConfig.warning.sftpPort:22}")
    private Integer sftpPort;
    @Value("${otherConfig.warning.sftpUser:}")
    private String sftpUsername;
    @Value("${otherConfig.warning.sftpPwd:}")
    private String sftpPwd;

    @Resource
    private SyncConfigMapper syncConfigMapper;
    @Autowired
    private SyncConfigService syncConfigService;
    @Autowired
    private IFileActionService iFileActionService;
    @Resource
    private MarketingDataFileConfigNoHeaderMapper marketingDataFileConfigNoHeaderMapper;
    @Resource
    private MarketingCustomerMapper marketingCustomerMapper;
    @Resource
    private LocalFileMapper localFileMapper;
    @Autowired
    private PushInfoService pushInfoService;
    @Resource
    private TrackingService trackingService;
    @Resource
    private AlarmApiClient alarmApiClient;

    @Override
    public void process(String jobParameter) {
        String apiCode = StringUtils.isBlank(jobParameter) ? "" : jobParameter;
        SyncConfigExample example = new SyncConfigExample();
        SyncConfigExample.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(apiCode)) {
            criteria.andApiCodeEqualTo(apiCode);
        }
        criteria.andStatusEqualTo(1)
                .andDataTypeEqualTo(DataTypeEnum.MARKETING_DATA_NO_HEADER.getValue())
                .andTypeEqualTo(1);
        List<SyncConfig> syncConfigs = syncConfigMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(syncConfigs)) {
            log.warn(TITLE + "无 SyncConfig(dataType=原始数据文件(无表头))，跳过");
            return;
        }
        for (SyncConfig syncConfig : syncConfigs) {
            String targetPath = syncConfigService.getPath().concat("initPath/").concat(syncConfig.getApiCode()).concat("/");
            SftpClient sftpClient = new SftpClient(sftpHost, sftpPort, sftpUsername, sftpPwd);
            Result<List<String>> res = iFileActionService.downSyncFileBySftp(sftpClient, syncConfig, targetPath);
            if (!ResultCode.SUCCESS.getValue().equals(res.getCode())) {
                continue;
            }
            List<String> fileNames = res.getData();
            List<MarketingDataFileConfigNoHeader> configs = getNoHeaderConfigs(syncConfig.getApiCode());
            if (CollectionUtils.isEmpty(configs)) {
                log.warn(TITLE + "apiCode={} 无有效无表头配置，跳过", syncConfig.getApiCode());
                continue;
            }
            for (MarketingDataFileConfigNoHeader noHeaderConfig : configs) {
                IFileToMarketingRuleTransferService transferService = getTransferService(noHeaderConfig.getTransferServiceName());
                if (transferService == null) {
                    log.warn(TITLE + "transferServiceName 未找到: {}", noHeaderConfig.getTransferServiceName());
                    continue;
                }
                for (String fileName : fileNames) {
                    Result<Long> action = isAction(syncConfig.getApiCode(), fileName, targetPath, syncConfig.getTargetPath());
                    if (ResultCode.SUCCESS.getValue().equals(action.getCode())) {
                        fileTransferActionNoHeader(syncConfig.getApiCode(), noHeaderConfig, targetPath, fileName, action.getData(), transferService);
                    }
                }
            }
        }
    }

    /** 按 apiCode 查询有效配置，取最新一条（id 倒序） */
    private List<MarketingDataFileConfigNoHeader> getNoHeaderConfigs(String apiCode) {
        MarketingDataFileConfigNoHeaderExample ex = new MarketingDataFileConfigNoHeaderExample();
        ex.setOrderByClause("id desc");
        ex.createCriteria().andApiCodeEqualTo(apiCode).andIsDelEqualTo((byte) 1);
        List<MarketingDataFileConfigNoHeader> list = marketingDataFileConfigNoHeaderMapper.selectByExample(ex);
        return CollectionUtils.isEmpty(list) ? list : Collections.singletonList(list.get(0));
    }

    private IFileToMarketingRuleTransferService getTransferService(String serviceName) {
        if (StringUtils.isBlank(serviceName)) {
            return null;
        }
        Map<String, IFileToMarketingRuleTransferService> beans = CkeckApplication.ac.getBeansOfType(IFileToMarketingRuleTransferService.class);
        return beans.get(serviceName);
    }

    private Result<Long> isAction(String apiCode, String fileName, String targetPath, String srcPath) {
        LocalFileExample ex = new LocalFileExample();
        ex.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andFileNameEqualTo(fileName)
                .andFileTypeEqualTo(FILE_TYPE_NO_HEADER)
                .andLocalPathEqualTo(targetPath);
        if (localFileMapper.countByExample(ex) > 0) {
            log.warn(TITLE + "该文件已处理: {}", targetPath + fileName);
            return new Result<Long>().setCode(ResultCode.FAIL.getValue()).setDate(null);
        }
        LocalFile localFile = new LocalFile();
        if (StringUtils.isNotBlank(apiCode)) {
            List<MarketingCustomer> customers = marketingCustomerMapper.getNameByApiCodeList(apiCode);
            if (!CollectionUtils.isEmpty(customers)) {
                localFile.setCid(customers.get(0).getCid());
            }
        }
        localFile.setApiCode(apiCode);
        localFile.setFileType(FILE_TYPE_NO_HEADER);
        localFile.setSrcPath(srcPath);
        localFile.setFileName(fileName);
        localFile.setLocalPath(targetPath);
        localFile.setStatus("1");
        Date now = new Date();
        localFile.setCreateTime(now);
        localFile.setUpdateTime(now);
        localFileMapper.insertSelective(localFile);
        Result<Long> result = new Result<Long>();
        result.setCode(ResultCode.SUCCESS.getValue());
        result.setDate(localFile.getId());
        return result;
    }

    /**
     * 无表头解析：第 1 行起即为数据行，按 columnIndex 映射；列数与配置不一致时告警
     */
    private void fileTransferActionNoHeader(String apiCode, MarketingDataFileConfigNoHeader noHeaderConfig,
                                            String path, String fileNm, Long localId,
                                            IFileToMarketingRuleTransferService transferService) {
        List<FileToMarketingFieldByColumnVO> columnConfigs =
                JSON.parseArray(noHeaderConfig.getFieldConfigColumn(), FileToMarketingFieldByColumnVO.class);
        if (CollectionUtils.isEmpty(columnConfigs)) {
            log.warn(TITLE + "field_config_column 为空, configId={}", noHeaderConfig.getId());
            return;
        }
        columnConfigs = columnConfigs.stream()
                .sorted(Comparator.comparing(FileToMarketingFieldByColumnVO::getColumnIndex)).collect(Collectors.toList());
        int expectedColumns = columnConfigs.stream().mapToInt(c -> c.getColumnIndex() == null ? 0 : c.getColumnIndex()).max().orElse(0);
        if (expectedColumns <= 0) {
            log.warn(TITLE + "配置列序号无效, configId={}", noHeaderConfig.getId());
            return;
        }

        LocalFile updateFile = new LocalFile();
        updateFile.setId(localId);
        String fileStr = path.concat(fileNm);
        File file = new File(fileStr);
        int lineNum = 0;
        int errorNum = 0;
        int pushSum = 0;
        boolean columnMismatchAlarmed = false;
        ThreadPoolExecutor pushPool = BrExecutors.getThreadPool(5, 5);
        Date startDate = new Date();
        List<TransferDataItemDTO> transferDataDTOS = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String row;
            while ((row = br.readLine()) != null) {
                lineNum++;
                if (StringUtils.isBlank(row)) {
                    continue;
                }
                List<String> columns = Splitter.on(",").splitToList(row);
                if (columns.size() != expectedColumns) {
                    errorNum++;
                    if (!columnMismatchAlarmed) {
                        columnMismatchAlarmed = true;
                        log.error(TITLE + "文件列数与配置列数不一致，需告警。文件名={}，行号={}，当前列数={}，配置要求列数={}", fileNm, lineNum, columns.size(), expectedColumns);
                    }
                    continue;
                }
                List<FileToMarketingDataFieldVO> dataFieldVOS = buildDataFieldVOSByColumn(columns, columnConfigs, fileNm);
                if (dataFieldVOS == null) {
                    errorNum++;
                    continue;
                }
                HashMap<String, FileToMarketingDataFieldVO> dataFieldMap = new HashMap<>();
                for (FileToMarketingDataFieldVO vo : dataFieldVOS) {
                    dataFieldMap.put(vo.getHeadField(), vo);
                }
                SimpleResult<?> valid = transferService.isVaild(dataFieldVOS, dataFieldMap);
                if (!ResultCode.SUCCESS.getValue().equals(valid.getCode())) {
                    errorNum++;
                    log.warn(TITLE + "文件名:{};行数:{};校验失败:{}", fileNm, lineNum, valid.getMessage());
                    continue;
                }
                TransferDataItemDTO make = transferService.make(dataFieldVOS);
                JSONObject jsonObject = JSONObject.parseObject(make.getReserveField1());
                if (jsonObject == null) {
                    jsonObject = new JSONObject();
                }
                make.setReserveField1(jsonObject.toJSONString());
                make.setApiCode(apiCode);
                transferDataDTOS.add(make);

                if (transferDataDTOS.size() >= PUSH_BATCH_SIZE) {
                    pushSum += pushBatchTransfer(apiCode, transferDataDTOS, pushPool);
                    transferDataDTOS.clear();
                }
            }
            if (!transferDataDTOS.isEmpty()) {
                pushSum += pushBatchTransfer(apiCode, transferDataDTOS, pushPool);
            }
        } catch (Exception ex) {
            log.error(TITLE + "文件读取异常, file={}", fileStr, ex);
        }

        pushPool.shutdown();
        try {
            while (!pushPool.awaitTermination(5L, TimeUnit.SECONDS)) {
                log.info("等待线程池结束");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        updateFile.setActualNumber(lineNum);
        updateFile.setPushNumber(pushSum);
        updateFile.setErrorActualNumber(errorNum);
        updateFile.setPushStartTime(startDate);
        updateFile.setPushEndTime(new Date());
        updateFile.setComplete(errorNum > 0 ? "3" : "1");
        localFileMapper.updateByPrimaryKeySelective(updateFile);

        // 埋点
        try {
            trackingService.trackPointLog(DataFlowDirection.OUT
                    , apiCode
                    , TITLE
                    , (long) pushSum
                    , "无表头转化清洗,fileName=" + fileNm
                    , TrackingContext.generateBatchId());
        } catch (Exception ex) {
            log.warn(
                    AlertLog.buildWarnMessage(
                            AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                            , ex.getMessage()
                            , "埋点异常")
                    , ex);
        }


        try {
            StringBuilder content = new StringBuilder();
            content.append("导入文件名称：").append(fileNm).append("\r\n")
                    .append("文件id：").append(updateFile.getId()).append("\r\n")
                    .append("文件类型：").append(FILE_TYPE_NO_HEADER).append("\r\n")
                    .append("导入文件状态：").append(errorNum == 0 ? "正常" : "不正常").append("\r\n")
                    .append("导入数据行数：").append(updateFile.getActualNumber()).append("\r\n")
                    .append("其中有问题行数：").append(errorNum).append("\r\n");
            alarmApiClient.sendAlarm(content.toString(), "无表头文件转化清洗", AlarmSendCodeEnum.SUCCESS_UPLOAD.getCode());
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    /**
     * 按列配置将一行数据组装为 List<FileToMarketingDataFieldVO>；columnIndex 1 对应 columns[0]
     */
    private List<FileToMarketingDataFieldVO> buildDataFieldVOSByColumn(List<String> columns,
                                                                        List<FileToMarketingFieldByColumnVO> columnConfigs,
                                                                        String fileNm) {
        List<FileToMarketingDataFieldVO> result = new ArrayList<>();
        for (FileToMarketingFieldByColumnVO colConf : columnConfigs) {
            int idx = (colConf.getColumnIndex() == null ? 0 : colConf.getColumnIndex()) - 1;
            String value = (idx >= 0 && idx < columns.size()) ? columns.get(idx) : "";
            if (StringUtils.isBlank(value) && StringUtils.isNotBlank(colConf.getDefaultValue())) {
                value = colConf.getDefaultValue();
            }
            if (StringUtils.isNotBlank(colConf.getConversion()) && StringUtils.isNotBlank(value)) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
                    List<Map<String, String>> maps = om.readValue(colConf.getConversion(),
                            new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, String>>>() {});
                    if (!maps.isEmpty()) {
                        String mapped = maps.get(0).get(value);
                        if (mapped != null) {
                            value = mapped;
                        }
                    }
                } catch (IOException e) {
                    log.warn(TITLE + "conversion 解析失败", e);
                }
            }
            if (Boolean.TRUE.equals(colConf.getIsMust()) && StringUtils.isBlank(value)) {
                return null;
            }
            FileToMarketingDataFieldVO vo = new FileToMarketingDataFieldVO();
            vo.setHeadField(colConf.getInterfaceField());
            vo.setInterfaceField(colConf.getInterfaceField());
            vo.setIsMust(colConf.getIsMust());
            vo.setDefaultValue(colConf.getDefaultValue());
            vo.setConversion(colConf.getConversion());
            vo.setIsExtend(colConf.getIsExtend());
            vo.setDataValue(value);
            result.add(vo);
        }
        boolean hasFileName = result.stream().anyMatch(v -> "fileName".equals(v.getInterfaceField()));
        if (!hasFileName) {
            FileToMarketingDataFieldVO fn = new FileToMarketingDataFieldVO();
            fn.setInterfaceField("fileName");
            fn.setDataValue(fileNm);
            fn.setHeadField("fileName");
            result.add(fn);
        }
        return result;
    }

    private int pushBatchTransfer(String apiCode, List<TransferDataItemDTO> items, ThreadPoolExecutor pushPool) {
        TransferDataDTO<TransferDataItemDTO> dto = new TransferDataDTO<>();
        // requestId：apiCode + 时间戳(毫秒) + 五位以上随机数
        dto.setRequestId(apiCode + "_" + System.currentTimeMillis() + "_" + RandomStringUtils.randomNumeric(6));
        dto.setDataItems(new ArrayList<>(items));
        PushTransferDataDetailDTO detail = new PushTransferDataDetailDTO();
        detail.setApiCode(apiCode);
        detail.setJsonData(JSON.toJSONString(dto));
        pushPool.submit(() -> pushInfoService.pushTransferByRetry(detail, null));
        return items.size();
    }
}
