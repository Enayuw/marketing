package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.MarketingCleanDataFile;
import com.br.marketing.entity.MarketingCleanDataTask;
import com.br.marketing.entity.MarketingCleanDataTaskExample;
import com.br.marketing.entity.MarketingDataFileConfig;
import com.br.marketing.handle.SnowflakeRedisGeneratorHandle;
import com.br.marketing.mapper.MarketingCleanDataFileMapper;
import com.br.marketing.mapper.MarketingCleanDataTaskMapper;
import com.br.marketing.mapper.MarketingDataFileConfigMapper;
import com.br.marketing.service.IDataCleaningGeneralService;
import com.br.marketing.service.IFileToMarketingRuleService;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.vo.FileToMarketingDataFieldVO;
import com.br.marketing.vo.FileToMarketingFieldVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.shaded.com.google.common.base.Splitter;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 数据清洗处理接口
 *
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-05-24
 */
@Service
@Slf4j
public class DataCleaningGeneralServiceImpl implements IDataCleaningGeneralService {

    @Resource
    MarketingCleanDataTaskMapper marketingCleanDataTaskMapper;
    @Resource
    MarketingCleanDataFileMapper marketingCleanDataFileMapper;

    @Resource
    MarketingDataFileConfigMapper marketingDataFileConfigMapper;

    @Resource
    PushInfoService pushInfoService;

    @Resource
    private SnowflakeRedisGeneratorHandle snowflakeRedisGeneratorHandle;

    public static List<String> pattern = Arrays.asList(
            "yyyy-MM-dd",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy/MM/dd",
            "yyyy/MM/dd HH:mm:ss",
            "yyyy-M-dd",
            "yyyy-M-dd HH:mm:ss",
            "yyyy/M/dd",
            "yyyy/M/dd HH:mm:ss",
            "MM-dd-yyyy",
            "MM-dd-yyyy HH:mm:ss",
            "dd-MM-yyyy",
            "dd-MM-yyyy HH:mm:ss"
            // 可以添加更多可能的格式
    );

    @Override
    public MarketingCleanDataTask getAction() {
        // 查询满足处理条件的清洗任务
        MarketingCleanDataTaskExample example = new MarketingCleanDataTaskExample();
        example.createCriteria()
                .andCreateTimeLessThanOrEqualTo(new Date())
                .andCleanStatusEqualTo(0)
                .andAutoCleanWayTypeEqualTo(0)
                .andIsDelEqualTo(1);
        example.setOrderByClause("create_time asc");
        List<MarketingCleanDataTask> marketingCleanDataTasks = marketingCleanDataTaskMapper.selectByExample(example);
        if (marketingCleanDataTasks.size() > 0) {
            MarketingCleanDataTask marketingCleanDataTask = marketingCleanDataTasks.get(0);
            marketingCleanDataTaskMapper.updateMarketingCleanDataTaskById(marketingCleanDataTask.getId(), 1);
            return marketingCleanDataTask;
        } else {
            return null;
        }
    }

    @Override
    public void action(MarketingCleanDataTask task, IFileToMarketingRuleService iFileToMarketingRuleService
            , MarketingDataFileConfig marketingDataFileConfig) {
        String fileIds = task.getFileId();
        String fileStr = "";
        Long taskId = task.getId();
        if (StringUtils.isNotBlank(fileIds)) {
            String[] split = fileIds.split(",");
            for (int j = 0; j < split.length; j++) {
                MarketingCleanDataFile marketingCleanDataFile = marketingCleanDataFileMapper.selectByPrimaryKey(Long.parseLong(split[j]));
//                fileStr = "D:\\test\\";
//                String fileName = "bairong_formal_act_01.txt";
                fileStr = marketingCleanDataFile.getLocalPath();
                String fileName = marketingCleanDataFile.getFileName();
                String apiCode = task.getApiCode();
                String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                String tasId = apiCode.concat("_").concat(yyyyMMdd);
                String requestIdPrefix = apiCode.concat("_").concat(fileName).concat("_");
                String fileConfigString = marketingDataFileConfig.getFieldConfig();
                // 解析配置的清洗规则
                List<FileToMarketingFieldVO> fieldVos = JSON.parseArray(fileConfigString, FileToMarketingFieldVO.class);
                // 根据 headField 字段分组
                Map<String, List<FileToMarketingFieldVO>> fieldVosMap = fieldVos.stream()
                        .collect(Collectors.groupingBy(FileToMarketingFieldVO::getHeadField));
                // 提取规则配置中的 必填字段，根据 headField 字段分组
                List<String> mustHeads = fieldVos.stream().filter(t -> t.getIsMust()).map(t -> t.getHeadField()).collect(Collectors.toList());
                // 读取文件并逐行处理

                // 定义一个map<表头名字:字段值>
                Map<String, String> tableMap = new HashMap<>();

                File file = new File(fileStr + fileName);
                Integer lineNum = 0;
                Integer errorSum = 0;
                Integer pushSum = 0;
                ThreadPoolExecutor pushPool = BrExecutors.getThreadPool(5, 5);
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String row = "";
                    Integer pushNum = 500;
                    Integer pushBatchNumber = 1;
                    HashMap<Integer, String> address = new HashMap<>();
                    HashSet<String> extra = new HashSet<>();
                    List<MarketingPreUserDetailDTO> syncUsers = new ArrayList<>();
                    Integer headSum = 0;
                    Boolean isNotFinal = Boolean.TRUE;
                    List<String> headList = null;
                    while (isNotFinal) {
                        row = br.readLine();
                        if (row == null) {
                            isNotFinal = Boolean.FALSE;
                        } else {
                            if(StringUtils.isBlank(row)){
                                continue;
                            }
                            lineNum++;
                        }
                        if (lineNum == 0 && !isNotFinal) {
                            continue;
                        }
                        if (isNotFinal) {
                            if (lineNum == 1) {
                                //region 文件头处理
                                headList = com.google.common.base.Splitter.on(",").splitToList(row);
                                // 存储表头信息
                                for (String header : headList) {
                                    tableMap.put(header, null);
                                }
                                headSum = headList.size();
                                Result result = statisticsHeadByCommon(headList, address, extra, mustHeads, fieldVosMap);
                                if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                                    log.warn("清洗任务表头校验异常-id:{}-规则id:{}-文件名:{}-异常原因:{}",
                                            taskId, task.getConfigId(), fileName, result.getMessage());
                                    marketingCleanDataTaskMapper.updateMarketingCleanDataTaskById(taskId, 3);
                                    return;
                                }
                            } else {
                                //region文件数据处理
                                List<String> datas = Splitter.on(",").splitToList(row);
                                if (!headSum.equals(datas.size())) {
                                    ++errorSum;
                                    log.warn("文件名:{};行数:{};错误:{};", fileName, lineNum, "该行与表头列数不一致");
                                    continue;
                                }
                                // 确保表头和数据数量一致
                                if (headSum == datas.size()) {
                                    // 使用索引来按顺序添加数据到对应的表头中
                                    for (int i = 0; i < headSum; i++) {
                                        // 更新map中对应键的值
                                        tableMap.put(headList.get(i), datas.get(i));
                                    }
                                }
                                StringBuilder errorMsg = new StringBuilder();
                                List<FileToMarketingDataFieldVO> dataFieldVOS = new ArrayList<>();
                                HashMap<String, List<FileToMarketingDataFieldVO>> dataFieldMap = new HashMap<>();
                                HashSet hasSet = new HashSet();
                                ArrayList<String> list = Lists.newArrayList();
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
                                        for (int k = 0; k < fileToMarketingFieldVOS.size(); k++) {
                                            fieldVO = fileToMarketingFieldVOS.get(k);
                                            rowFieldHandle(fieldVO, list, value, tableMap, fileName, lineNum
                                                    , errorMsg, headNm, hasSet, dataFieldVOS, dataFieldMap);
                                        }
                                    } else {
                                        log.warn("[{}]文件[{}]行[{}]字段没配置，舍弃", fileName, lineNum, headNm);
                                        // do nothing
                                    }
                                }
                                List<String> finalHeadList = headList;
                                for (Map.Entry<String, List<FileToMarketingFieldVO>> entry : fieldVosMap.entrySet()) {
                                    List<FileToMarketingFieldVO> valueList = entry.getValue();
                                    List<FileToMarketingDataFieldVO> collect = valueList.stream().filter(
                                            (FileToMarketingFieldVO vo) -> !finalHeadList.contains(vo.getHeadField())
                                    ).map((FileToMarketingFieldVO ftmf) -> {
                                        FileToMarketingDataFieldVO vo = new FileToMarketingDataFieldVO();
                                        vo.setDataValue(ftmf.getDefaultValue());
                                        BeanUtils.copyProperties(ftmf, vo);
                                        return vo;
                                    }).collect(Collectors.toList());
                                    dataFieldVOS.addAll(collect);
                                }
                                // 选填字段处理：例如身份证号和性别选填二选一
                                if (!list.isEmpty()) {
                                    Boolean b = false;
                                    for (String s : list) {
                                        if (StringUtils.isNotBlank(tableMap.get(s))) {
                                            b = true;
                                        }
                                    }
                                    if (!b) {
                                        ++errorSum;
                                        log.warn("文件名:{};行数:{};错误:{};", fileName, lineNum, "选填字段未赋值:" + list);
                                        continue;
                                    }
                                }
                                if (StringUtils.isNotBlank(errorMsg.toString())) {
                                    ++errorSum;
                                    log.warn("文件名:{};行数:{};错误:{};", fileName, lineNum, errorMsg.toString());
                                    continue;
                                }
                                // 行数据拼装
                                MarketingPreUserDetailDTO make = iFileToMarketingRuleService.make(dataFieldVOS);
                                syncUsers.add(make);
                            }
                        }
                        //region 调用营销上传接口处理
                        if (syncUsers.size() == pushNum || (!isNotFinal && syncUsers.size() > 0)) {
                            pushSum += syncUsers.size();
                            // TODO 不同类型不同的参数拼装和接口调用
                            asyncUploadData(apiCode, tasId, requestIdPrefix, pushPool, pushBatchNumber, syncUsers);
                            syncUsers = new ArrayList<>();
                            pushBatchNumber++;
                        }
                    }
                    pushPool.shutdown();
                    while (!pushPool.awaitTermination(5L, TimeUnit.SECONDS)) {
                        // do nothing
                    }
                } catch (InterruptedException ie) {
                    log.error("线程池终止[{}]文件[{}]-行数[{}]-成功数[{}]-失败数[{}]--", apiCode, fileName, lineNum, pushSum, errorSum, ie);
                    Thread.currentThread().interrupt();
                } catch (Exception ex) {
                    log.error("[{}]清洗失败[{}]-行数[{}]-推送成功数[{}]-推送失败数[{}]--", apiCode, fileName, lineNum, pushSum, errorSum, ex);
                    marketingCleanDataTaskMapper.updateMarketingCleanDataTaskById(taskId, 3);
                    return;
                }
                // 打印处理正确和不正确的条数以及所在行
                log.warn("[{}]数据清洗文件[{}]-文件行数[{}]-推送成功数[{}]-推送失败数[{}]", apiCode, fileName, lineNum, pushSum, errorSum);
            }
            marketingCleanDataTaskMapper.updateMarketingCleanDataTaskById(taskId, 2);
        } else {
            log.warn("清洗任务未配置文件名");
        }
    }

    /**
     * 异步调用上传数据接口
     * @param apiCode         apiCode
     * @param tasId           tasId
     * @param requestIdPrefix requestIdPrefix
     * @param pushPool        上传使用线程池
     * @param pushBatchNumber pushBatchNumber
     * @param syncUsers       具体数据对象
     */
    private void asyncUploadData(String apiCode, String tasId, String requestIdPrefix
            , ThreadPoolExecutor pushPool, Integer pushBatchNumber, List<MarketingPreUserDetailDTO> syncUsers) {
        MarketingPreUserDTO marketingPreUserDTO = new MarketingPreUserDTO();
        marketingPreUserDTO.setTaskId(tasId);
        marketingPreUserDTO.setRequestId(requestIdPrefix.concat(pushBatchNumber.toString()));
        marketingPreUserDTO.setDataItems(syncUsers);
        UploadDataDTO uploadDataDTO = new UploadDataDTO();
        uploadDataDTO.setApiCode(apiCode);
        uploadDataDTO.setJsonData(JSON.toJSONString(marketingPreUserDTO));
        pushPool.submit(() -> {
            pushInfoService.pushUploadByRetry(uploadDataDTO, null);
        });
    }

    /**
     * 处理每一行数据字段的方法
     * @param fieldVO      列对应的处理规则（列名可以重复）
     * @param list
     * @param value        列名对应的值
     * @param tableMap     map<表头名字:字段值>
     * @param fileName     待处理文件名
     * @param lineNum      待处理文件行号
     * @param errorMsg     错误信息
     * @param headNm       列名
     * @param hasSet
     * @param dataFieldVOS
     * @param dataFieldMap <表头,表头对应规则list>
     */
    public void rowFieldHandle(FileToMarketingFieldVO fieldVO, ArrayList<String> list, String value
            , Map<String, String> tableMap, String fileName, Integer lineNum
            , StringBuilder errorMsg, String headNm, HashSet hasSet
            , List<FileToMarketingDataFieldVO> dataFieldVOS, HashMap<String, List<FileToMarketingDataFieldVO>> dataFieldMap) {
        //endregion
        // 选填字段集合
        if (StringUtils.isNotBlank(fieldVO.getGroupOptional())) {
            list.add(fieldVO.getHeadField());
        }

        //region 根据配置信息进行处理
        // 表里没有初始值，需要动态赋值或取默认值（初始数据 > 动态赋值 > 默认值）
        if (StringUtils.isBlank(value)) {
            // 根据动态配置赋值
            if (StringUtils.isNotBlank(fieldVO.getDynamicData())) {
                value = tableMap.get(fieldVO.getDynamicData());
            } else if (StringUtils.isNotBlank(fieldVO.getDefaultValue())) {
                value = fieldVO.getDefaultValue();
            }
        }
        // 字典项不为空 则进行字典项映射
        if (StringUtils.isNotEmpty(value) && StringUtils.isNotBlank(fieldVO.getConversion())) {
            String conversion = fieldVO.getConversion();
            // 创建ObjectMapper实例
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                // 将JSON字符串转换为List<Map<String, String>>
                List<Map<String, String>> genderMappings = objectMapper.readValue(conversion, List.class);
                if (!genderMappings.isEmpty()) {
                    Map<String, String> genderMapping = genderMappings.get(0);
                    value = genderMapping.get(value);
                }
            } catch (IOException ex) {
                log.error(ex.getMessage(), ex);
            }
        }
        // 时间格式转换
        if (StringUtils.isNotEmpty(value) && null != fieldVO.getIsDateTransform() && fieldVO.getIsDateTransform()) {
            value = getFormatterValue(value, fileName, lineNum);
        }
        // 必填字段没值 则报错
        if (fieldVO.getIsMust() && StringUtils.isBlank(value)) {
            errorMsg.append(String.format("字段名:%s 未赋值;", fieldVO.getHeadField()));
            return;
        }
        FileToMarketingDataFieldVO vo = new FileToMarketingDataFieldVO();
        BeanUtils.copyProperties(fieldVO, vo);
        vo.setDataValue(value);
        hasSet.add(vo.getInterfaceField());
        dataFieldVOS.add(vo);
        String headField = vo.getHeadField();
        List<FileToMarketingDataFieldVO> fileToMarketingDataFieldVOS = dataFieldMap.get(headField);
        if (null != fileToMarketingDataFieldVOS) {
            fileToMarketingDataFieldVOS.add(vo);
        } else {
            List<FileToMarketingDataFieldVO> fileToMarketingDataFieldVOS1 = new ArrayList<>();
            fileToMarketingDataFieldVOS1.add(vo);
            dataFieldMap.put(headField, fileToMarketingDataFieldVOS1);
        }
    }

    /**
     * 处理时间格式的方法
     * @param value    待处理的时间类型的值
     * @param fileName 待处理文件名
     * @param lineNum  待处理文件对应行数
     * @return String 格式化后的时间值（yyyy-MM-dd HH:mm:ss）
     */
    private String getFormatterValue(String value, String fileName, Integer lineNum) {
        Date date = null;
        for (String parser : pattern) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(parser);
                date = sdf.parse(value);
                // 如果解析成功，则跳出循环
                break;
            } catch (ParseException e) {
                // 忽略异常，并尝试下一个解析器
                if (log.isInfoEnabled()) {
                    log.info("无法解析日期-文件名:{};行数:{};格式:{};原值:{}", fileName, lineNum, parser, value);
                }
            }
        }
        if (date != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            value = formatter.format(date);
        } else {
            log.warn("无法解析日期:{}-文件名:{}-行数:{}", value, fileName, lineNum);
        }
        return value;
    }

    /**
     * @param heads       文件表头字段集合
     * @param address     空值, <位置,表头字段名>
     * @param extra       空值, 扩展子段包含的表头字段名
     * @param mustHeads   必填字段
     * @param fieldVosMap <表头字段,处理规则配置list>
     * @return Result
     * @Author yu.xia@brgroup.com
     * @Date 2024/5/21 17:47
     */
    public static Result statisticsHeadByCommon(List<String> heads, HashMap<Integer, String> address, HashSet extra,
                                                List<String> mustHeads, Map<String, List<FileToMarketingFieldVO>> fieldVosMap) {
        if (heads.size() <= 0) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("head信息不存在");
        }
        if (!heads.containsAll(mustHeads)) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("表头缺少必填字段");
        }
        for (int i = 0; i < heads.size(); i++) {
            String s = heads.get(i);
            if (StringUtils.isBlank(s)) {
                return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("head信息不能有空字段");
            }
            FileToMarketingFieldVO fieldVO = null;
            //根据当前表头名获取配置信息
            List<FileToMarketingFieldVO> fileToMarketingFieldVOS = fieldVosMap.get(s);
            if (fileToMarketingFieldVOS != null && fileToMarketingFieldVOS.size() > 0) {
                for (int j = 0; j < fileToMarketingFieldVOS.size(); j++) {
                    fieldVO = fileToMarketingFieldVOS.get(j);
                    if (fieldVO.getIsExtend()) {
                        // 该列字段数据需要写到扩展字段
                        extra.add(s);
                    }
                }
            } else {
                // 该列字段数据需要写到扩展字段
                extra.add(s);
            }
            address.put(i, s);
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public MarketingPreUserDTO pilotAction(Long id) {
        List<MarketingPreUserDetailDTO> syncUsers = new ArrayList<>();
        // 查询ID对应的清洗任务
        MarketingCleanDataTask task = marketingCleanDataTaskMapper.selectByPrimaryKey(id);
        String tasId = task.getApiCode().concat("_").concat(LocalDate.now().toString()).concat("test");
        Long fieldId = Long.valueOf(Arrays.asList(task.getFileId().split(",")).get(0));
        MarketingCleanDataFile marketingCleanDataFile = marketingCleanDataFileMapper.selectByPrimaryKey(fieldId);
        List<String> head = Splitter.on(",").splitToList(marketingCleanDataFile.getFileHeader());
        // 解析配置的清洗规则
        MarketingDataFileConfig marketingDataFileConfig = marketingDataFileConfigMapper.selectByPrimaryKey(Long.valueOf(task.getConfigId()));
        String ruleConfig = marketingDataFileConfig.getFieldConfig();
        List<String> datas = Splitter.on(",").splitToList(marketingCleanDataFile.getFileData());
        // 定义一个map<表名:字段值>
        Map<String, String> tableMap = new HashMap<>();
        // 确保表头和数据数量一致
        if (head.size() == datas.size()) {
            // 使用索引来按顺序添加数据到对应的表头中
            for (int i = 0; i < head.size(); i++) {
                // 更新map中对应键的值
                tableMap.put(head.get(i), datas.get(i));
            }
        }
        // 解析配置的清洗规则
        List<FileToMarketingFieldVO> fieldVos = JSON.parseArray(ruleConfig, FileToMarketingFieldVO.class);
        // 根据 headField 字段分组
        Map<String, List<FileToMarketingFieldVO>> fieldVosMap = fieldVos.stream().
                collect(Collectors.groupingBy(FileToMarketingFieldVO::getHeadField));
        List<FileToMarketingDataFieldVO> dataFieldVOS = new ArrayList<>();
        //region 每列的字段处理逻辑
        for (int i = 0; i < datas.size(); i++) {
            // 列字段值
            String value = datas.get(i);
            String headNm = head.get(i);
            //根据当前表头名获取配置信息
            List<FileToMarketingFieldVO> fileToMarketingFieldVOS = fieldVosMap.get(headNm);
            //未获取配置
            if (CollectionUtils.isEmpty(fileToMarketingFieldVOS)) {
                continue;
            }
            Iterator<FileToMarketingFieldVO> itr = fileToMarketingFieldVOS.iterator();
            //遍历配置项进行组装
            while (itr.hasNext()) {
                FileToMarketingFieldVO fieldVO = itr.next();
                // 表里没有初始值，需要动态赋值或取默认值（初始数据 > 动态赋值 > 默认值）
                if (StringUtils.isBlank(value)) {
                    // 根据动态配置赋值
                    if (StringUtils.isNotBlank(fieldVO.getDynamicData())) {
                        value = tableMap.get(fieldVO.getDynamicData());
                    } else if (StringUtils.isNotBlank(fieldVO.getDefaultValue())) {
                        value = fieldVO.getDefaultValue();
                    }
                }
                // 字典项不为空 则进行字典项映射
                if (StringUtils.isNotEmpty(value) && StringUtils.isNotBlank(fieldVO.getConversion())) {
                    String conversion = fieldVO.getConversion();
                    // 创建ObjectMapper实例
                    ObjectMapper objectMapper = new ObjectMapper();
                    String mappStr = "";
                    try {
                        // 将JSON字符串转换为List<Map<String, String>>
                        List<Map<String, String>> genderMappings = objectMapper.readValue(conversion, List.class);
                        if (!genderMappings.isEmpty()) {
                            Map<String, String> genderMapping = genderMappings.get(0);
                            mappStr = genderMapping.get(value);
                        }
                    } catch (IOException ex) {
                        log.error(ex.getMessage(), ex);
                    }
                    if (StringUtils.isNotEmpty(mappStr)) {
                        value = mappStr;
                    }
                }
                if (StringUtils.isNotEmpty(value) && null != fieldVO.getIsDateTransform() && fieldVO.getIsDateTransform()) {
                    value = getFormatterValue(value, marketingCleanDataFile.getFileName(), 1);
                }
                FileToMarketingDataFieldVO vo = new FileToMarketingDataFieldVO();
                BeanUtils.copyProperties(fieldVO, vo);
                vo.setDataValue(value);
                dataFieldVOS.add(vo);
            }
        }
        Map<String, List<FileToMarketingFieldVO>> defaultConfig = fieldVosMap.entrySet().stream().filter(map -> !head.contains(map.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        defaultConfig.forEach((String fileHeader, List<FileToMarketingFieldVO> configList) -> {
            FileToMarketingDataFieldVO vo = new FileToMarketingDataFieldVO();
            vo.setDataValue(configList.get(0).getDefaultValue());
            vo.setInterfaceField(configList.get(0).getHeadField());
            dataFieldVOS.add(vo);
        });
        MarketingPreUserDetailDTO make = make(dataFieldVOS);
        make.setFingerprint(snowflakeRedisGeneratorHandle.nextId());
        //endregion
        syncUsers.add(make);
        MarketingPreUserDTO marketingPreUserDTO = new MarketingPreUserDTO();
        marketingPreUserDTO.setTaskId(tasId);
        marketingPreUserDTO.setRequestId(task.getApiCode().concat("_").concat(LocalDate.now().toString()).concat("_")
                .concat(UUID.randomUUID().toString()));
        marketingPreUserDTO.setDataItems(syncUsers);
        return marketingPreUserDTO;
    }

    MarketingPreUserDetailDTO make(List<FileToMarketingDataFieldVO> vos) {
        MarketingPreUserDetailDTO dto = new MarketingPreUserDetailDTO();
        JSONObject reserveFieldJo = new JSONObject();
        for (FileToMarketingDataFieldVO vo : vos) {
            switch (vo.getInterfaceField()) {
                case "custNum":
                    dto.setCustNum(vo.getDataValue());
                    break;
                case "cell":
                    dto.setCell(vo.getDataValue());
                    break;
                case "id":
                    if (com.br.marketing.common.utils.StringUtils.isNotBlank(vo.getDataValue())) {
                        dto.setId(vo.getDataValue());
                    }
                    break;
                case "name":
                    if (com.br.marketing.common.utils.StringUtils.isNotBlank(vo.getDataValue())) {
                        dto.setName(vo.getDataValue());
                    }
                    break;
                case "groupType":
                    if (com.br.marketing.common.utils.StringUtils.isNotBlank(vo.getDataValue())) {
                        dto.setGroupType(vo.getDataValue());
                    }
                    break;
                case "userType":
                    reserveFieldJo.put("userType", vo.getDataValue());
                    break;
                case "operateType":
                    reserveFieldJo.put("operateType", vo.getDataValue());
                    break;
                case "fileName":
                    reserveFieldJo.put("fileName", vo.getDataValue());
                    break;
                default:
                    break;
            }
            if (vo.getIsExtend() != null && vo.getIsExtend()) {
                reserveFieldJo.put(com.br.marketing.common.utils.StringUtils.isBlank(vo.getInterfaceField()) ?
                        vo.getHeadField() : vo.getInterfaceField(), vo.getDataValue());
            }
        }
        if (reserveFieldJo.keySet().size() > 0) {
            dto.setReserveField1(JSON.toJSONString(reserveFieldJo));
        }
        return dto;
    }
}
