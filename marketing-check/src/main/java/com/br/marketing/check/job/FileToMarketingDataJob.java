package com.br.marketing.check.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.check.CkeckApplication;
import com.br.marketing.check.utils.SftpToDbUtils;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.SftpClient;
import com.br.marketing.client.marketingapi.input.PushTransferDataDetailDTO;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.commondto.SimpleResult;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.DataTypeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.mapper.MarketingDataFileConfigMapper;
import com.br.marketing.mapper.SyncConfigMapper;
import com.br.marketing.service.*;
import com.br.marketing.vo.FileToMarketingDataFieldVO;
import com.br.marketing.vo.FileToMarketingFieldVO;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.marketingkit.tracking.model.indicator.DataFlowDirection;
import com.marketingkit.tracking.service.TrackingService;
import com.marketingkit.tracking.util.TrackingContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.curator.shaded.com.google.common.base.Splitter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
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
 * //				    _ooOoo_
 * //				   o8888888o
 * //				   88" . "88
 * //				   (| -_- |)
 * //				   O\  =  /O
 * //			    ____/`---'\____
 * //			  .'  \\|     |//  `.
 * //		     /  \\|||  :  |||//  \
 * //		    /  _|||||--:--|||||_  \
 * //		    | / | \\\  -  /// | \ |
 * //		    | \_|  ''\-:-/''  |_/ |
 * //		    \  .-\__  `-`  ___/-. /
 * //		  ___`...'  /--.--\  '...`___
 * //	   ."" '< `.___\_<|>_/___.'  >' "".
 * //	   | | : `- \`.;`\ _ /`;.`/ -` : | |
 * //	    \ \ `-.  \_ __\ /__ _/  .-` / /
 * // ======`-.____`-.____\____/.-`____.-`======
 * //				    `=---='
 * //^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
 * //			  Buddha Bless, No Bug !
 *
 * @Author xiaoxin.pang
 * @Date 2021/4/27 15:46
 * @Description:
 **/
@Component
@Slf4j
public class FileToMarketingDataJob extends AbstractSimpleElasticJob {

    @Autowired
    private AlarmApiClient alarmClient;

    @Value("${otherConfig.warning.sftpHost:00}")
    private String sftpHost;
    @Value("${otherConfig.warning.sftpPort:00}")
    private Integer sftpPort;
    @Value("${otherConfig.warning.sftpUser:00}")
    private String sftpUsername;
    @Value("${otherConfig.warning.sftpPwd:00}")
    private String sftpPwd;

    @Resource
    SyncConfigMapper syncConfigMapper;

    @Autowired
    SyncConfigService syncConfigService;

    @Autowired
    IFileActionService iFileActionService;

    @Resource
    MarketingDataFileConfigMapper marketingDataFileConfigMapper;
    @Resource
    private MarketingCustomerMapper marketingCustomerMapper;

    @Resource
    LocalFileMapper localFileMapper;

    @Autowired
    PushInfoService pushInfoService;
    @Resource
    private TrackingService trackingService;

    private static final String TITLE = "【通用文件清洗】";

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {

        String jobParameter = jobExecutionMultipleShardingContext.getJobParameter();
        String apiCode = StringUtils.isBlank(jobParameter) ? "" : jobParameter;
        SyncConfigExample syncConfigExample = new SyncConfigExample();
        SyncConfigExample.Criteria criteria = syncConfigExample.createCriteria();
        if(StringUtils.isNotBlank(apiCode)){
            criteria.andApiCodeEqualTo(apiCode);
        }
        criteria.andStatusEqualTo(1)
                .andDataTypeEqualTo(DataTypeEnum.MARKETINGDATA.getValue())
                .andTypeEqualTo(1);
        List<SyncConfig> syncConfigs = syncConfigMapper.selectByExample(syncConfigExample);
        for (SyncConfig syncConfig : syncConfigs) {
            // 组装本地下载路径
            String targetPath = syncConfigService.getPath().concat("initPath/").concat(syncConfig.getApiCode()).concat("/");
            SftpClient sftpClient = new SftpClient(sftpHost, sftpPort, sftpUsername, sftpPwd);
            Result<List<String>> res = iFileActionService.downSyncFileBySftp(sftpClient, syncConfig, targetPath);
            if (ResultCode.SUCCESS.getValue().equals(res.getCode())) {
                List<String> fileNames = res.getData();
                List<MarketingDataFileConfig> configs = getDataFileConfig(syncConfig.getApiCode());

                for (MarketingDataFileConfig dataFileConfig : configs){
                    if(dataFileConfig.getCleanType().equals(0)){
                        IFileToMarketingRuleService fileToMarketingRuleService = getFileToMarketingRuleService(dataFileConfig);
                        for (String fileName : fileNames) {

                            // 校验表名称
                            Boolean checklistName = fileToMarketingRuleService.isChecklistName(dataFileConfig, fileName);
                            if(!checklistName){
                                log.warn(TITLE + "文件名:{};校验规则:{};错误:{};", fileName, dataFileConfig.getValidationRules(), "文件名称校验失败");
                                continue;
                            }

                            Result<Long> action = isAction(syncConfig.getApiCode(), fileName, targetPath, syncConfig.getTargetPath());
                            if (ResultCode.SUCCESS.getValue().equals(action.getCode())) {
                                fileUploadAction(syncConfig.getApiCode(), dataFileConfig, targetPath, fileName, action.getData(), fileToMarketingRuleService);
                            }
                        }
                    }else {
                        IFileToMarketingRuleTransferService fileToMarketingRuleTransferService = getFileToMarketingRuleTransferService(dataFileConfig);
                        for (String fileName : fileNames) {

                            // 校验表名称
                            Boolean checklistName = fileToMarketingRuleTransferService.isChecklistName(dataFileConfig, fileName);
                            if(!checklistName){
                                log.warn(TITLE + "文件名:{};校验规则:{};错误:{};", fileName, dataFileConfig.getValidationRules(), "文件名称校验失败");
                                continue;
                            }

                            Result<Long> action = isAction(syncConfig.getApiCode(), fileName, targetPath, syncConfig.getTargetPath());
                            if (ResultCode.SUCCESS.getValue().equals(action.getCode())) {
                                fileTransferAction(syncConfig.getApiCode(), dataFileConfig, targetPath, fileName, action.getData(), fileToMarketingRuleTransferService);
                            }
                        }
                    }
                }

            }
        }

    }

    /**
     * 处理上传文件
     *
     * @param apiCode
     * @param fileConfig
     * @param path
     * @param fileNm
     * @param localId
     * @param iFileToMarketingRuleService
     */
    private void fileUploadAction(String apiCode, MarketingDataFileConfig fileConfig, String path, String fileNm, Long localId, IFileToMarketingRuleService iFileToMarketingRuleService) {
        LocalFile updateFile = new LocalFile();
        updateFile.setId(localId);
        String taskId = iFileToMarketingRuleService.getTaskId(apiCode,fileNm);
        String requestIdPrefix = apiCode.concat("_").concat(fileNm).concat("_");
        String fileStr = path.concat(fileNm);
        // json转化为字段属性list
        List<FileToMarketingFieldVO> fieldVos = JSON.parseArray(fileConfig.getFieldConfig(), FileToMarketingFieldVO.class);
        // 根据 headField 字段分组
        Map<String, List<FileToMarketingFieldVO>> fieldVosMap = fieldVos.stream().collect(Collectors.groupingBy(FileToMarketingFieldVO::getHeadField));
        // 筛选出 必须的字段，根据 headField 字段分组
        List<String> mustHeads = fieldVos.stream().filter(t -> t.getIsMust()).map(t -> t.getHeadField()).collect(Collectors.toList());
        // 定义一个map<表名:字段值>
        Map<String, String> tableMap = new HashMap<>();

        File file = new File(fileStr);
        Integer line = 0;
        Integer errorNum = 0;
        Integer pushSum = 0;
        ThreadPoolExecutor pushPool = BrExecutors.getThreadPool(5, 5);
        Date startDate = new Date();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String row = "";
            Integer pushNum = 500;
            Integer pushBatchNumber = 1;
            HashMap<Integer, String> address = new HashMap<>();
            HashSet<String> extra = new HashSet<>();
            List<MarketingPreUserDetailDTO> syncUsers = new ArrayList<>();
            Integer headSum = 0;
            Boolean isNotFinal = Boolean.TRUE;
            String[] headers = new String[0];
            while (isNotFinal) {
                row = br.readLine();
                if (row == null) {
                    isNotFinal = Boolean.FALSE;
                } else {
                    line++;
                }
                if (line == 0 && !isNotFinal) {
                    continue;
                }
                if (isNotFinal) {
                    if (line == 1) {
                        //region 文件头处理
                        headers = row.split(",", -1);
                        // 存储表头信息
                        for (String header : headers) {
                            tableMap.put(header, null);
                        }
                        headSum = headers.length;
                        Result result = SftpToDbUtils.statisticsHeadByCommon(row, address, extra, mustHeads,fieldVosMap);
                        if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                            updateFile.setComplete("2");
                            localFileMapper.updateByPrimaryKeySelective(updateFile);
                            log.error(String.format("%s 文件：%s", fileNm, result.getMessage()));
                            return;
                        }
                    } else {
                        //region文件数据处理
                        List<String> datas = Splitter.on(",").splitToList(row);
                        if (!headSum.equals(datas.size())) {
                            errorNum++;
                            log.warn(TITLE + "文件名:{};行数:{};错误:{};", fileNm, line, "该行与表头列数不一致");
                            continue;
                        }
                        // 确保表头和数据数量一致
                        if (headers.length == datas.size()) {
                            // 使用索引来按顺序添加数据到对应的表头中
                            for (int i = 0; i < headers.length; i++) {
                                // 更新map中对应键的值
                                tableMap.put(headers[i], datas.get(i));
                            }
                        }
                        StringBuilder errorMsg = new StringBuilder();
                        List<FileToMarketingDataFieldVO> dataFieldVOS = new ArrayList<>();
                        HashMap<String, FileToMarketingDataFieldVO> dataFieldMap = new HashMap<>();
                        HashSet hasSet = new HashSet();
                        ArrayList<String> list = Lists.newArrayList();
                        String cell = "";

                        //region 每列的字段处理逻辑
                        for (int i = 0; i < datas.size(); i++) {
                            // 列字段值
                            String value = datas.get(i);
                            // 列名
                            String headNm = address.get(i);
                            FileToMarketingFieldVO fieldVO = null;
                            //根据当前表头名获取配置信息
                            List<FileToMarketingFieldVO> fileToMarketingFieldVOS = fieldVosMap.get(headNm);
                            if (fileToMarketingFieldVOS != null && fileToMarketingFieldVOS.size() > 0) {
                                fieldVO = fileToMarketingFieldVOS.get(0);
                            }

                            //region 未获取到配置信息的处理
                            if (fieldVO == null) {
                                if (!extra.contains(headNm)) {
                                    continue;
                                } else {
                                    fieldVO = new FileToMarketingFieldVO();
                                    fieldVO.setHeadField(headNm);
                                    fieldVO.setInterfaceField(headNm);
                                    fieldVO.setIsMust(Boolean.FALSE);
                                    fieldVO.setIsExtend(true);
                                }
                            }
                            // 选填字段集合
                            if(StringUtils.isNotBlank(fieldVO.getGroupOptional())){
                                list.add(fieldVO.getHeadField());
                            }

                            //region 根据配置信息进行处理
                            // 表里没有初始值，需要动态赋值或取默认值（初始数据 > 动态赋值 > 默认值）
                            if(StringUtils.isBlank(value)){
                                // 根据动态配置赋值
                                if(StringUtils.isNotBlank(fieldVO.getDynamicData())){
                                    value = tableMap.get(fieldVO.getDynamicData());
                                }else if(StringUtils.isNotBlank(fieldVO.getDefaultValue())) {
                                    value = fieldVO.getDefaultValue();
                                }
                            }
                            // 字典项不为空 则进行字典项映射
                            if(StringUtils.isNotEmpty(value) && StringUtils.isNotBlank(fieldVO.getConversion())){
                                String conversion = fieldVO.getConversion();
                                // 创建ObjectMapper实例
                                ObjectMapper objectMapper = new ObjectMapper();
                                try {
                                    // 将JSON字符串转换为List<Map<String, String>>
                                    List<Map<String, String>> genderMappings = objectMapper.readValue(conversion, List.class);
                                    if (!genderMappings.isEmpty()) {
                                        Map<String, String> genderMapping = genderMappings.get(0);
                                        value = genderMapping.get(value) == null? value : genderMapping.get(value);
                                    }
                                } catch (IOException ex) {
                                    log.error(ex.getMessage(), ex);
                                }
                            }
                            // 必填字段没值 则报错
                            if (fieldVO.getIsMust() && StringUtils.isBlank(value)) {
                                errorMsg.append(String.format("字段名:%s 未赋值;", fieldVO.getHeadField()));
                                continue;
                            }
                            FileToMarketingDataFieldVO vo = new FileToMarketingDataFieldVO();
                            if (fieldVO != null) {
                                BeanUtils.copyProperties(fieldVO, vo);
                            } else {
                                vo.setHeadField(headNm);
                                vo.setInterfaceField(headNm);
                            }
                            vo.setDataValue(value);
                            hasSet.add(vo.getInterfaceField());
                            dataFieldVOS.add(vo);
                            dataFieldMap.put(vo.getHeadField(), vo);
                        }
                        // 增加fileName值
                        if(StringUtils.isBlank(tableMap.get("fileName"))){
                            FileToMarketingDataFieldVO vo = new FileToMarketingDataFieldVO();
                            vo.setInterfaceField("fileName");
                            vo.setDataValue(fileNm);
                            dataFieldVOS.add(vo);
                        }
                        // 选填字段处理：例如身份证号和性别选填二选一
                        if(!list.isEmpty()){
                            Boolean b = false;
                            for (String s : list) {
                                if(StringUtils.isNotBlank(tableMap.get(s))){
                                    b = true;
                                }
                            }
                            if(!b){
                                errorNum++;
                                log.warn(TITLE + "文件名:{};行数:{};错误:{};", fileNm, line, "选填字段未赋值:"+list);
                                continue;
                            }
                        }
                        if (StringUtils.isNotBlank(errorMsg.toString())) {
                            errorNum++;
                            log.warn(TITLE + "文件名:{};行数:{};错误:{};", fileNm, line, errorMsg.toString());
                            continue;
                        }

                        //region 抽象的剔除方法和组装逻辑的调用,如未实现走默认的service
                        SimpleResult vaild = iFileToMarketingRuleService.isVaild(dataFieldVOS, dataFieldMap);
                        if (!ResultCode.SUCCESS.getValue().equals(vaild.getCode())) {
                            errorNum++;
                            log.warn(TITLE + "文件名:{};行数:{};错误:{};", fileNm, line, vaild.getMessage());
                            continue;
                        }
                        MarketingPreUserDetailDTO make = iFileToMarketingRuleService.make(dataFieldVOS);
                        syncUsers.add(make);
                    }
                }
                //region 调用营销上传接口处理
                if (syncUsers.size() == pushNum || (!isNotFinal && syncUsers.size() > 0)) {
                    pushSum += syncUsers.size();
                    MarketingPreUserDTO marketingPreUserDTO = new MarketingPreUserDTO();
                    marketingPreUserDTO.setTaskId(taskId);
                    marketingPreUserDTO.setRequestId(requestIdPrefix.concat(pushBatchNumber.toString()));
                    marketingPreUserDTO.setDataItems(syncUsers);
                    UploadDataDTO uploadDataDTO = new UploadDataDTO();
                    uploadDataDTO.setApiCode(apiCode);
                    uploadDataDTO.setJsonData(JSON.toJSONString(marketingPreUserDTO));
                    pushPool.submit(() -> {
                        pushInfoService.pushUploadByRetry(uploadDataDTO, null);
                    });
                    syncUsers = new ArrayList<>();
                    pushBatchNumber++;
                }

            }
            pushPool.shutdown();
            while (!pushPool.awaitTermination(5L, TimeUnit.SECONDS)) {

            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

        Date end = new Date();
        updateFile.setActualNumber(line > 0 ? line - 1 : line);
        updateFile.setPushNumber(pushSum);
        updateFile.setErrorActualNumber(errorNum);
        updateFile.setPushStartTime(startDate);
        updateFile.setPushEndTime(end);
        updateFile.setComplete(errorNum > 0 ? "3" : "1");
        localFileMapper.updateByPrimaryKeySelective(updateFile);
        // 埋点
        try {
            String remark = String.format("通用文件清洗job,文件类型：%s"
                    , "上传");
            trackingService.trackPointLog(DataFlowDirection.OUT
                    , apiCode
                    , "通用文件清洗JOB"
                    , Long.valueOf(pushSum)
                    , remark
                    , TrackingContext.generateBatchId());
        } catch (Exception ex) {
            log.warn(
                    AlertLog.buildWarnMessage(
                            AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                            , ex.getMessage()
                            , "埋点异常")
                    , ex);
        }

        //region 提示
        try {
            StringBuilder content = new StringBuilder();
            content.append("导入文件名称：".concat(fileNm).concat("\r\n"))
                    .append("文件id：".concat(updateFile.getId().toString()).concat("\r\n"))
                    .append("文件类型：".concat("marketingData").concat("\r\n"))
                    .append("导入文件状态：".concat(errorNum == 0 ? "正常" : "不正常").concat("\r\n"))
                    .append("导入数据行数：".concat(updateFile.getActualNumber().toString()).concat("\r\n"))
                    .append("其中有问题行数：".concat(String.valueOf(errorNum)).concat("\r\n"));
            alarmClient.sendAlarm(content.toString(), "sftp数据上传", AlarmSendCodeEnum.SUCCESS_UPLOAD.getCode());
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

    }

    /**
     * 处理转化文件
     *
     * @param apiCode
     * @param fileConfig
     * @param path
     * @param fileNm
     * @param localId
     * @param iFileToMarketingRuleService
     */
    private void fileTransferAction(String apiCode, MarketingDataFileConfig fileConfig, String path, String fileNm, Long localId, IFileToMarketingRuleTransferService iFileToMarketingRuleService) {
        String TITLE1 = TITLE + "清洗";
        LocalFile updateFile = new LocalFile();
        updateFile.setId(localId);
        String taskId = iFileToMarketingRuleService.getTaskId(apiCode,fileNm);
        String fileStr = path.concat(fileNm);
        // json转化为字段属性list
        List<FileToMarketingFieldVO> fieldVos = JSON.parseArray(fileConfig.getFieldConfig(), FileToMarketingFieldVO.class);
        // 根据 headField 字段分组
        Map<String, List<FileToMarketingFieldVO>> fieldVosMap = fieldVos.stream().collect(Collectors.groupingBy(FileToMarketingFieldVO::getHeadField));
        // 筛选出 必须的字段，根据 headField 字段分组
        List<String> mustHeads = fieldVos.stream().filter(t -> t.getIsMust()).map(t -> t.getHeadField()).collect(Collectors.toList());
        // 定义一个map<表名:字段值>
        Map<String, String> tableMap = new HashMap<>();

        File file = new File(fileStr);
        Integer line = 0;
        Integer errorNum = 0;
        Integer pushSum = 0;
        ThreadPoolExecutor pushPool = BrExecutors.getThreadPool(5, 5);
        Date startDate = new Date();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String row = "";
            Integer pushNum = 500;
            HashMap<Integer, String> address = new HashMap<>();
            HashSet<String> extra = new HashSet<>();
            List<TransferDataItemDTO> transferDataDTOS = new ArrayList<>();
            Integer headSum = 0;
            Boolean isNotFinal = Boolean.TRUE;
            String[] headers = new String[0];
            while (isNotFinal) {
                row = br.readLine();
                if (row == null) {
                    isNotFinal = Boolean.FALSE;
                } else {
                    line++;
                }
                if (line == 0 && !isNotFinal) {
                    continue;
                }
                if (isNotFinal) {
                    if (line == 1) {
                        //region 文件头处理
                        headers = row.split(",", -1);
                        // 存储表头信息
                        for (String header : headers) {
                            tableMap.put(header, null);
                        }
                        headSum = headers.length;
                        Result result = SftpToDbUtils.statisticsHeadByCommon(row, address, extra, mustHeads,fieldVosMap);
                        if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                            updateFile.setComplete("2");
                            localFileMapper.updateByPrimaryKeySelective(updateFile);
                            log.error(String.format("%s 文件：%s", fileNm, result.getMessage()));
                            return;
                        }
                    } else {
                        //region文件数据处理
                        List<String> datas = Splitter.on(",").splitToList(row);
                        if (!headSum.equals(datas.size())) {
                            errorNum++;
                            log.warn(TITLE1 + "文件名:{};行数:{};错误:{};", fileNm, line, "该行与表头列数不一致");
                            continue;
                        }
                        // 确保表头和数据数量一致
                        if (headers.length == datas.size()) {
                            // 使用索引来按顺序添加数据到对应的表头中
                            for (int i = 0; i < headers.length; i++) {
                                // 更新map中对应键的值
                                tableMap.put(headers[i], datas.get(i));
                            }
                        }
                        StringBuilder errorMsg = new StringBuilder();
                        List<FileToMarketingDataFieldVO> dataFieldVOS = new ArrayList<>();
                        HashMap<String, FileToMarketingDataFieldVO> dataFieldMap = new HashMap<>();
                        HashSet hasSet = new HashSet();
                        ArrayList<String> list = Lists.newArrayList();
                        String cell = "";

                        //region 每列的字段处理逻辑
                        for (int i = 0; i < datas.size(); i++) {
                            // 列字段值
                            String value = datas.get(i);
                            // 列名
                            String headNm = address.get(i);
                            FileToMarketingFieldVO fieldVO = null;
                            //根据当前表头名获取配置信息
                            List<FileToMarketingFieldVO> fileToMarketingFieldVOS = fieldVosMap.get(headNm);
                            if (fileToMarketingFieldVOS != null && fileToMarketingFieldVOS.size() > 0) {
                                fieldVO = fileToMarketingFieldVOS.get(0);
                            }

                            //region 未获取到配置信息的处理
                            if (fieldVO == null) {
                                if (!extra.contains(headNm)) {
                                    continue;
                                } else {
                                    fieldVO = new FileToMarketingFieldVO();
                                    fieldVO.setHeadField(headNm);
                                    fieldVO.setInterfaceField(headNm);
                                    fieldVO.setIsMust(Boolean.FALSE);
                                    fieldVO.setIsExtend(true);
                                }
                            }
                            // 选填字段集合
                            if(StringUtils.isNotBlank(fieldVO.getGroupOptional())){
                                list.add(fieldVO.getHeadField());
                            }

                            //region 根据配置信息进行处理
                            // 表里没有初始值，需要动态赋值或取默认值（初始数据 > 动态赋值 > 默认值）
                            if(StringUtils.isBlank(value)){
                                // 根据动态配置赋值
                                if(StringUtils.isNotBlank(fieldVO.getDynamicData())){
                                    value = tableMap.get(fieldVO.getDynamicData());
                                }else if(StringUtils.isNotBlank(fieldVO.getDefaultValue())) {
                                    value = fieldVO.getDefaultValue();
                                }
                            }
                            // 字典项不为空 则进行字典项映射
                            if(StringUtils.isNotEmpty(value) && StringUtils.isNotBlank(fieldVO.getConversion())){
                                String conversion = fieldVO.getConversion();
                                // 创建ObjectMapper实例
                                ObjectMapper objectMapper = new ObjectMapper();
                                try {
                                    // 将JSON字符串转换为List<Map<String, String>>
                                    List<Map<String, String>> genderMappings = objectMapper.readValue(conversion, List.class);
                                    if (!genderMappings.isEmpty()) {
                                        Map<String, String> genderMapping = genderMappings.get(0);
                                        value = genderMapping.get(value) == null? value : genderMapping.get(value);
                                    }
                                } catch (IOException ex) {
                                    log.error(ex.getMessage(), ex);
                                }
                            }
                            // 必填字段没值 则报错
                            if (fieldVO.getIsMust() && StringUtils.isBlank(value)) {
                                errorMsg.append(String.format("字段名:%s 未赋值;", fieldVO.getHeadField()));
                                continue;
                            }
                            FileToMarketingDataFieldVO vo = new FileToMarketingDataFieldVO();
                            if (fieldVO != null) {
                                BeanUtils.copyProperties(fieldVO, vo);
                            } else {
                                vo.setHeadField(headNm);
                                vo.setInterfaceField(headNm);
                            }
                            vo.setDataValue(value);
                            hasSet.add(vo.getInterfaceField());
                            dataFieldVOS.add(vo);
                            dataFieldMap.put(vo.getHeadField(), vo);
                        }

                        // 增加fileName值
                        if(StringUtils.isBlank(tableMap.get("fileName"))){
                            FileToMarketingDataFieldVO vo = new FileToMarketingDataFieldVO();
                            vo.setInterfaceField("fileName");
                            vo.setDataValue(fileNm);
                            dataFieldVOS.add(vo);
                        }
                        // 选填字段处理：例如身份证号和性别选填二选一
                        if(!list.isEmpty()){
                            Boolean b = false;
                            for (String s : list) {
                                if(StringUtils.isNotBlank(tableMap.get(s))){
                                    b = true;
                                }
                            }
                            if(!b){
                                errorNum++;
                                log.warn(TITLE1 + "文件名:{};行数:{};错误:{};", fileNm, line, "选填字段未赋值:"+list);
                                continue;
                            }
                        }
                        if (StringUtils.isNotBlank(errorMsg.toString())) {
                            errorNum++;
                            log.warn(TITLE1 + "文件名:{};行数:{};错误:{};", fileNm, line, errorMsg.toString());
                            continue;
                        }

                        //region 抽象的剔除方法和组装逻辑的调用,如未实现走默认的service
                        SimpleResult vaild = iFileToMarketingRuleService.isVaild(dataFieldVOS, dataFieldMap);
                        if (!ResultCode.SUCCESS.getValue().equals(vaild.getCode())) {
                            errorNum++;
                            log.warn(TITLE1 + "文件名:{};行数:{};错误:{};", fileNm, line, vaild.getMessage());
                            continue;
                        }
                        TransferDataItemDTO make = iFileToMarketingRuleService.make(dataFieldVOS);
                        JSONObject jsonObject = JSONObject.parseObject(make.getReserveField1());
                        jsonObject.put("taskId",taskId);
                        make.setReserveField1(jsonObject.toJSONString());
                        make.setApiCode(apiCode);
                        transferDataDTOS.add(make);
                    }
                }
                // 调用营销转化接口处理
                if (transferDataDTOS.size() == pushNum || (!isNotFinal && transferDataDTOS.size() > 0)) {
                    pushSum += transferDataDTOS.size();
                    TransferDataDTO transferDataDTO = new TransferDataDTO();
                    transferDataDTO.setRequestId(apiCode.concat("_").concat(RandomStringUtils.randomNumeric(5)).concat("_").concat(String.valueOf(System.currentTimeMillis())));
                    transferDataDTO.setDataItems(transferDataDTOS);
                    PushTransferDataDetailDTO dto = new PushTransferDataDetailDTO();
                    dto.setApiCode(apiCode);
                    dto.setJsonData(JSON.toJSONString(transferDataDTO));
                    pushPool.submit(() -> {
                        pushInfoService.pushTransferByRetry(dto, null);
                    });
                    transferDataDTOS = new ArrayList<>();
                }
            }
            pushPool.shutdown();
            while (!pushPool.awaitTermination(5L, TimeUnit.SECONDS)) {

            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

        // 埋点
        try {
            String remark = String.format("【通用文件清洗】,文件类型：%s"
                    , "转化");
            trackingService.trackPointLog(DataFlowDirection.OUT
                    , apiCode
                    , "【通用文件清洗JOB】"
                    , Long.valueOf(pushSum)
                    , remark
                    , TrackingContext.generateBatchId());
        } catch (Exception ex) {
            log.warn(
                    AlertLog.buildWarnMessage(
                            AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                            , ex.getMessage()
                            , "埋点异常")
                    , ex);
        }

        Date end = new Date();
        updateFile.setActualNumber(line > 0 ? line - 1 : line);
        updateFile.setPushNumber(pushSum);
        updateFile.setErrorActualNumber(errorNum);
        updateFile.setPushStartTime(startDate);
        updateFile.setPushEndTime(end);
        updateFile.setComplete(errorNum > 0 ? "3" : "1");
        localFileMapper.updateByPrimaryKeySelective(updateFile);
        //region 提示
        try {
            StringBuilder content = new StringBuilder();
            content.append("导入文件名称：".concat(fileNm).concat("\r\n"))
                    .append("文件id：".concat(updateFile.getId().toString()).concat("\r\n"))
                    .append("文件类型：".concat("marketingData").concat("\r\n"))
                    .append("导入文件状态：".concat(errorNum == 0 ? "正常" : "不正常").concat("\r\n"))
                    .append("导入数据行数：".concat(updateFile.getActualNumber().toString()).concat("\r\n"))
                    .append("其中有问题行数：".concat(String.valueOf(errorNum)).concat("\r\n"));
            alarmClient.sendAlarm(content.toString(), "sftp数据上传", AlarmSendCodeEnum.SUCCESS_UPLOAD.getCode());
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

    }

    /**
     * 判断当前文件是否处理
     *
     * @param apiCode
     * @param fileName
     * @param targetPath
     * @param srcPath
     * @return
     */
    private Result<Long> isAction(String apiCode, String fileName, String targetPath, String srcPath) {
        String fileStr = targetPath.concat(fileName);
        LocalFileExample localFileExample = new LocalFileExample();
        localFileExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andFileNameEqualTo(fileName)
                .andFileTypeEqualTo("marketingData")
                .andLocalPathEqualTo(targetPath);
        List<LocalFile> localFiles = localFileMapper.selectByExample(localFileExample);
        if (localFiles.size() > 0) {
            log.warn(TITLE + "该文件已经读取过：{}", fileStr);
            return new Result().setCode(ResultCode.FAIL.getValue());
        } else {
            Date date = new Date();
            LocalFile localFile = new LocalFile();
            if(StringUtils.isNotBlank(apiCode)){
                List<MarketingCustomer> customers = marketingCustomerMapper.getNameByApiCodeList(apiCode);
                if (!CollectionUtils.isEmpty(customers)) {
                    MarketingCustomer customer = customers.get(0);
                    localFile.setCid(customer.getCid());
                }
            }
            localFile.setApiCode(apiCode);
            localFile.setFileType("marketingData");
            localFile.setSrcPath(srcPath);
            localFile.setFileName(fileName);
            localFile.setLocalPath(targetPath);
            localFile.setStatus("1");
            localFile.setCreateTime(date);
            localFile.setUpdateTime(date);
            localFileMapper.insertSelective(localFile);
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(localFile.getId());
        }
    }

    /**
     * 获取配置文件
     * 如未配置获取默认配置
     *
     * @param apiCode
     * @return
     */
    private List<MarketingDataFileConfig> getDataFileConfig(String apiCode) {
        MarketingDataFileConfigExample fileConfigExample = new MarketingDataFileConfigExample();
        fileConfigExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andIsDelEqualTo(Constants.DATA_VALID);
        List<MarketingDataFileConfig> marketingDataFileConfigs = marketingDataFileConfigMapper.selectByExample(fileConfigExample);
        if (marketingDataFileConfigs.isEmpty()) {
            MarketingDataFileConfig defaultConfig = marketingDataFileConfigMapper.selectByPrimaryKey(1L);
            marketingDataFileConfigs.add(defaultConfig);
        }
        return marketingDataFileConfigs;
    }

    /**
     * 获取上传执行的方法
     *
     * @param config
     * @return
     */
    private IFileToMarketingRuleService getFileToMarketingRuleService(MarketingDataFileConfig config) {
        Map<String, IFileToMarketingRuleService> beansOfRule = CkeckApplication.ac.getBeansOfType(IFileToMarketingRuleService.class);
        IFileToMarketingRuleService iFileToMarketingRuleService = beansOfRule.get(config.getServiceName());
        return iFileToMarketingRuleService;
    }

    /**
     * 获取转化执行的方法
     *
     * @param config
     * @return
     */
    private IFileToMarketingRuleTransferService getFileToMarketingRuleTransferService(MarketingDataFileConfig config) {
        Map<String, IFileToMarketingRuleTransferService> beansOfRule = CkeckApplication.ac.getBeansOfType(IFileToMarketingRuleTransferService.class);
        IFileToMarketingRuleTransferService iFileToMarketingRuleTransferService = beansOfRule.get(config.getTransferServiceName());
        return iFileToMarketingRuleTransferService;
    }


}
