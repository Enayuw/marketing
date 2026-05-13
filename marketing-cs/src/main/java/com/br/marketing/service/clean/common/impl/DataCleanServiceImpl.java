package com.br.marketing.service.clean.common.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.common.log.AlertLog;
import com.br.marketing.aspect.MqIdempotent;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.client.rulecleaning.DataCleanDTO;
import com.br.marketing.client.rulecleaning.RuleCleaningResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.auth.CodeEnum;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.JsonParseUtils;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.MqIdempotentContext;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.dto.dataclean.mq.CommonMqDataJsonParse;
import com.br.marketing.dto.dataclean.mq.MqDataJsonParse;
import com.br.marketing.entity.*;
import com.br.marketing.enums.clean.DataCleanStatusEnum;
import com.br.marketing.enums.clean.DataProcessEnum;
import com.br.marketing.enums.clean.DataSourceTypeEnum;
import com.br.marketing.handle.SnowflakeRedisGeneratorHandle;
import com.br.marketing.mapper.*;
import com.br.marketing.mapper.rulecleaning.MarketingCustomerOriginalDataMapper;
import com.br.marketing.mapper.rulecleaning.MarketingDataCleanGeneralConfigMapper;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.service.clean.common.DataCleanService;
import com.br.marketing.service.ruleCleaning.RuleCleaningService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.DataCleanDelimiterUtils;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.br.marketing.vo.dataclean.CommonCleanResponseVO;
import com.marketingkit.tracking.model.indicator.DataFlowDirection;
import com.marketingkit.tracking.service.TrackingService;
import com.marketingkit.tracking.util.TrackingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DataCleanServiceImpl implements DataCleanService {

    @Autowired
    private MarketingJsonNodeParseMapper marketingJsonNodeParseMapper;


    @Autowired
    private MarketingDataCleanGeneralRuleConfigMapper marketingDataCleanGeneralRuleConfigMapper;


    @Resource
    private RedisChgService redisChgService;


    @Resource
    MarketingCustomerOriginalDataMapper marketingCustomerOriginalDataMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;


    @Resource
    PushInfoService pushInfoService;


    @Resource
    private RuleCleaningService ruleCleaningService;

    @Resource
    MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Resource
    private MarketingUserMapper marketingUserMapper;

    @Autowired
    private PushRuleService pushRuleService;

    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;

    @Resource
    private SnowflakeRedisGeneratorHandle snowflakeRedisGeneratorHandle;
    @Resource
    private TrackingService trackingService;

    @Resource
    private DataCleanService dataCleanService;


    private static final String TITLE = "【定制上传数据清洗】";
    @Autowired
    private MarketingDataCleanGeneralConfigMapper marketingDataCleanGeneralConfigMapper;

    @Override
    public Result<Boolean> customerDataJsonParse(String message) {
        Result<Boolean> result = new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(false);

        try {
            MqDataJsonParse mqDataJsonParse = JSON.parseObject(message, MqDataJsonParse.class);

            Integer systemType = DataProcessEnum.SystemTypeEnum.MARKETING.getCode();
            if (mqDataJsonParse.getSystemType() != null){
                systemType = mqDataJsonParse.getSystemType();
            }

            //获取表名
            String tableName = DataProcessEnum.getByTypes(systemType, mqDataJsonParse.getDataType()
                    , mqDataJsonParse.getAcceptType()).getTableName();

            Map<String, Object> originalData = marketingJsonNodeParseMapper.getOriginalData(mqDataJsonParse.getDataId(), tableName);
            String jsonData = (String) originalData.get("json_data");
            String apiCode = (String) originalData.get("api_code");
            // 解析JSON
            if (StringUtils.isNotEmpty(jsonData)) {
                // 将JSON字符串转换为JSONObject或JSONArray
                Object jsonObject = JSON.parse(jsonData);
                // 记录节点路径并递归遍历JSON结构
                processJsonNode(
                        apiCode,
                        systemType,
                        mqDataJsonParse.getDataType(),
                        mqDataJsonParse.getAcceptType(),
                        "",
                        "",
                        jsonObject,
                        0,
                        false
                );

                MqIdempotentContext.setApiCode(apiCode);
            } else {
                log.warn("数据ID: {} 的JSON数据为空", mqDataJsonParse.getDataId());
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DATACLEANING_SERVICEERROR.getCode(),
                    "客户数据JSON结构解析异常mq=" + message), e);
        }
        return result;
    }

    @Override
    public Result<Boolean> commonDataJsonParse(String message) {
        Result<Boolean> result = new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(false);

        try {
            CommonMqDataJsonParse commonMqDataJsonParse = JSONObject.parseObject(message, CommonMqDataJsonParse.class);

            String apiCode = commonMqDataJsonParse.getApiCode();
            Integer systemType = commonMqDataJsonParse.getSystemType();
            Integer dataType = commonMqDataJsonParse.getDataType();
            Integer acceptType = commonMqDataJsonParse.getAcceptType();
            String jsonData = commonMqDataJsonParse.getJsonData();
            if (StringUtils.isNotEmpty(jsonData)) {
                // 将JSON字符串转换为JSONObject或JSONArray
                Object jsonObject = JSON.parse(jsonData);
                // 记录节点路径并递归遍历JSON结构
                processJsonNode(
                        apiCode,
                        systemType,
                        dataType,
                        acceptType,
                        "",
                        "",
                        jsonObject,
                        0,
                        false
                );

            }else {
                log.warn("通用清洗接口上传数据为空，apiCode:{}，systemType:{}，dataType:{}，acceptType:{}", apiCode, systemType, dataType, acceptType);
            }
        }catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DATACLEANING_SERVICEERROR.getCode(),
                    "通用清洗接口上传数据JSON结构解析异常mq=" + message), e);
        }
        return result;
    }


    /**
     * 递归处理JSON节点并存入数据库
     *
     * @param apiCode     API编码
     * @param systemType    数据来源
     * @param dataType    数据类型
     * @param acceptType  接收类型
     * @param nodeName    节点名称
     * @param parentPath  父节点路径
     * @param nodeValue   节点值
     * @param level       节点层级
     * @param isArrayItem 是否为数组元素
     */
    private void processJsonNode(String apiCode, Integer systemType, Integer dataType, Integer acceptType,
                                 String nodeName, String parentPath, Object nodeValue, int level, boolean isArrayItem) {
        String nodeType;
        String nodeValueStr = null;

        if (nodeValue == null) {
            //遍历结束，退出
            return;
        }

        if (nodeValue instanceof JSONObject) {
            // 对象类型
            JSONObject jsonObject = (JSONObject) nodeValue;
            nodeType = "object";

            // 对象值直接转为字符串
            nodeValueStr = jsonObject.toString();

            // 保存当前对象节点，包含节点值
            saveNodeData(apiCode, systemType, dataType, acceptType, nodeName, level, parentPath, nodeType, isArrayItem, nodeValueStr);

            // 构建新的父路径
            String newParentPath = parentPath;
            if (!nodeName.isEmpty()) {
                newParentPath = parentPath.isEmpty() ? nodeName : parentPath + "." + nodeName;
            }

            // 递归处理对象的每个字段
            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                processJsonNode(apiCode, systemType, dataType, acceptType, entry.getKey(), newParentPath, entry.getValue(), level + 1, false);
            }
        } else if (nodeValue instanceof JSONArray) {
            // 数组类型
            JSONArray jsonArray = (JSONArray) nodeValue;
            nodeType = "array";

            // 数组值直接转为字符串
            nodeValueStr = jsonArray.toString();

            // 保存当前数组节点，包含节点值
            saveNodeData(apiCode, systemType, dataType, acceptType, nodeName, level, parentPath, nodeType, isArrayItem, nodeValueStr);

            // 构建新的父路径
            String newParentPath = parentPath;
            if (!nodeName.isEmpty()) {
                newParentPath = parentPath.isEmpty() ? nodeName : parentPath + "." + nodeName;
            }

            // 检查数组是否为空
            if (jsonArray.size() > 0) {
                // 获取第一个元素，用于判断数组内容类型
                Object firstElement = jsonArray.get(0);

                // 如果数组元素是对象类型，则继续遍历
                if (firstElement instanceof JSONObject) {
                    // 对数组中的所有对象元素使用统一的节点名称 "item"
                    String arrayItemName = "item";

                    for (int i = 0; i < jsonArray.size(); i++) {
                        // 只处理对象类型的数组元素
                        Object element = jsonArray.get(i);
                        if (element instanceof JSONObject) {
                            // 使用统一的节点名称 "item" 而不是索引
                            processJsonNode(apiCode, systemType, dataType, acceptType, arrayItemName, newParentPath, element, level + 1, true);
                        }
                    }
                } else {
                    // 如果数组元素是基本类型(primitive)，只记录数组节点本身，不再继续遍历数组元素
                    log.debug("数组元素是基本类型，不再继续遍历: {}", newParentPath);
                }
            }
        } else {
            // 原始类型处理 - 新增String类型JSON解析支持
            if (nodeValue instanceof String) {
                String stringValue = (String) nodeValue;

                // 尝试判断字符串是否为JSON格式并解析
                Object parsedValue = tryParseJsonString(stringValue);

                if (parsedValue != null) {
                    // 如果解析成功，递归处理解析后的对象
                    log.debug("字符串解析为JSON成功，继续递归处理: {}", nodeName);
                    processJsonNode(apiCode, systemType, dataType, acceptType, nodeName, parentPath, parsedValue, level, isArrayItem);
                    return;
                }
            }

            // 原始类型 (字符串、数字、布尔值等)
            nodeType = "primitive";

            // 原始类型值直接转为字符串
            nodeValueStr = nodeValue.toString();

            // 保存原始类型节点，包含节点值
            saveNodeData(apiCode, systemType, dataType, acceptType, nodeName, level, parentPath, nodeType, isArrayItem, nodeValueStr);
        }
    }

    /**
     * 尝试解析字符串为JSON对象或数组
     *
     * @param jsonString 待解析的JSON字符串
     * @return 解析成功返回JSONObject或JSONArray，失败返回null
     */
    private Object tryParseJsonString(String jsonString) {
        if (StringUtils.isEmpty(jsonString)) {
            return null;
        }

        String trimmed = jsonString.trim();

        // 判断是否可能为JSON格式 - 优化后的逻辑
        boolean isJsonObject = trimmed.startsWith("{") && trimmed.endsWith("}");
        boolean isJsonArray = trimmed.startsWith("[") && trimmed.endsWith("]");

        if (!(isJsonObject || isJsonArray)) {
            return null;
        }

        try {
            // 尝试解析为JSON对象或数组
            Object parsed = JSON.parse(trimmed);

            // 只有解析结果是JSONObject或JSONArray才继续处理
            if (parsed instanceof JSONObject || parsed instanceof JSONArray) {
                return parsed;
            }
        } catch (Exception e) {
            // 解析失败，记录调试日志但不抛出异常
            log.error("字符串JSON解析失败，按原始字符串处理: {}", e.getMessage());
        }

        return null;
    }

    /**
     * 保存节点数据到数据库
     */
    private void saveNodeData(String apiCode, Integer systemType, Integer dataType, Integer acceptType, String nodeName,
                              Integer level, String parentPath, String nodeType,
                              boolean isArrayItem, String nodeValue) {
        String redisKey = RedisKeyConstant.ORIGINAL_DATA_JSON_PARSE.concat(apiCode).concat(":")
                .concat(systemType.toString()).concat(":")
                .concat(dataType.toString()).concat(":")
                .concat(acceptType.toString()).concat(":")
                .concat(level.toString());
        if (redisChgService.sismember(redisKey, nodeName)) {
            return;
        }
        //再查库
        MarketingJsonNodeParseExample jsonNodeParseExample = new MarketingJsonNodeParseExample();
        jsonNodeParseExample.createCriteria().andApiCodeEqualTo(apiCode)
                .andSystemTypeEqualTo(systemType)
                .andDataTypeEqualTo(dataType)
                .andAcceptTypeEqualTo(acceptType)
                .andParentPathEqualTo(parentPath)
                .andNodeNameEqualTo(nodeName);
        List<MarketingJsonNodeParse> jsonNodeParseList = marketingJsonNodeParseMapper.selectByExample(jsonNodeParseExample);
        try {
            if (CollectionUtils.isEmpty(jsonNodeParseList)) {
                MarketingJsonNodeParse jsonNodeParse = new MarketingJsonNodeParse();
                jsonNodeParse.setApiCode(apiCode);
                jsonNodeParse.setSystemType(systemType);
                jsonNodeParse.setDataType(dataType);
                jsonNodeParse.setAcceptType(acceptType);
                jsonNodeParse.setParentPath(parentPath);
                jsonNodeParse.setNodeName(nodeName);
                jsonNodeParse.setNodeType(nodeType);
                jsonNodeParse.setIsArrayItem(isArrayItem);
                jsonNodeParse.setLevel(level);
                jsonNodeParse.setNodeValue(nodeValue);
                jsonNodeParse.setCreateTime(new Date());
                jsonNodeParse.setUpdateTime(new Date());
                marketingJsonNodeParseMapper.insertSelective(jsonNodeParse);
            }
            //写入缓存
            redisChgService.saddMember(redisKey, nodeName);
        } catch (DuplicateKeyException keyException) {
            log.warn("数据清洗json解析入库已存在,api_code={}", apiCode);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DATACLEANING_SERVICEERROR.getCode(),
                    "数据清洗json解析入库异常" + e.getMessage()), e);
        }
    }


    @Override
    public Map<String, MarketingDataCleanGeneralRuleConfig> getConfigRule(String apiCode, Integer systemType,
                                                                          Integer dataType, Integer acceptType,Integer status) {
        String redisKey = RedisKeyConstant.DATA_CLEAN_CONFIG_RULE.concat(apiCode)
                .concat(":").concat(systemType.toString())
                .concat(":").concat(dataType.toString())
                .concat(":").concat(acceptType.toString());
        Map<String, Object> ruleMap = redisChgService.hgetall(redisKey);
        if (!CollectionUtils.isEmpty(ruleMap)) {
            Map<String, MarketingDataCleanGeneralRuleConfig> resultMap = new HashMap<>();
            ruleMap.forEach((key, value) -> {
                MarketingDataCleanGeneralRuleConfig ruleConfig = JSONObject.parseObject((String) value
                        , new TypeReference<MarketingDataCleanGeneralRuleConfig>() {
                        });
                resultMap.put(key, ruleConfig);
            });
            return resultMap;
        }
        //查询数据库
        List<MarketingDataCleanGeneralRuleConfig> ruleConfigList =
                marketingDataCleanGeneralRuleConfigMapper.getRuleConfigList(apiCode, systemType, dataType, acceptType,status);
        if (CollectionUtils.isEmpty(ruleConfigList)) {
            return null;
        }
        Map<String, String> redisConfig = new HashMap<>();
        Map<String, MarketingDataCleanGeneralRuleConfig> ruleConfig = new HashMap<>();
        ruleConfigList.forEach(rule -> {
            redisConfig.put(rule.getMappingField(), JSON.toJSONString(rule));
            ruleConfig.put(rule.getMappingField(), rule);
        });
        redisChgService.hmset(redisKey, redisConfig);
        //规则缓存24小时
        redisChgService.expire(redisKey, 60 * 60 * 24);
        return ruleConfig;
    }


    public Long delConfigRule(Integer systemType, String apiCode,
                              Integer dataType, Integer acceptType) {

        String redisKey = RedisKeyConstant.DATA_CLEAN_CONFIG_RULE.concat(apiCode)
                .concat(":").concat(systemType.toString())
                .concat(":").concat(dataType.toString())
                .concat(":").concat(acceptType.toString());
        if (redisChgService.exists(redisKey)) {
            return redisChgService.del(redisKey);
        }
        return null;
    }


    /**
     * 获取数据清洗结果
     */
    @Override
    public Object getCleanResult(JSONObject jsonObject, MarketingDataCleanGeneralRuleConfig ruleConfig) {
        return ruleCleaningService.executeCleaningRule(jsonObject, ruleConfig);
    }


    @Override
    public void customUploadDataClean(MarketingDataCleanGeneralConfig config, List<String> appletDateList) {
        log.warn(TITLE + "apiCode={} 开始清洗", config.getApiCode());
        Long start = System.currentTimeMillis();
        String apiCode = config.getApiCode();
        //查询规则
        MarketingDataCleanGeneralRuleConfigExample ruleConfigExample = new MarketingDataCleanGeneralRuleConfigExample();
        ruleConfigExample.createCriteria().andCleanConfigIdEqualTo(config.getId()).andIsDelEqualTo(1);
        List<MarketingDataCleanGeneralRuleConfig> ruleConfigList = marketingDataCleanGeneralRuleConfigMapper.selectByExample(ruleConfigExample);
        // Pool
        ThreadPoolExecutor pool = BrExecutors.getThreadPool(5, 5, 50);
        AtomicLong total = new AtomicLong(0L);
        appletDateList.forEach(appletDate -> {
            Long indexId = null;
            while (true) {
                // 循环获取条件数据，每次pageSize条
                List<MarketingCustomerOriginalData> pageList = marketingCustomerOriginalDataMapper.getCustomUploadData(
                        apiCode, appletDate, indexId);
                if (CollectionUtils.isEmpty(pageList)) {
                    break;
                }
                indexId = pageList.get(pageList.size() - 1).getId();
                modifyCorePoolSize(pool);
                pageList.forEach(originalData -> {
                            List<MarketingDataCleanGeneralRuleConfig> ruleList = new ArrayList<>();
                            ruleList.addAll(ruleConfigList);
                            pool.submit(() -> processData(originalData, ruleList));
                            try {
                                total.addAndGet(originalData.getActualNum());
                            } catch (Exception ex) {
                                log.warn(
                                        AlertLog.buildWarnMessage(
                                                AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                                                , ex.getMessage()
                                                , "埋点异常")
                                        , ex);
                            }
                        }
                );
            }
        });
        // 关闭线程池
        pool.shutdown();
        try {
            while (!pool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("等待线程池结束");
            }
        } catch (InterruptedException ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.SERVICEERROR_UNKNOWN.getCode(), "定制上传数据清洗线程池停止异常！"), ex);
            Thread.currentThread().interrupt();
        }

        try {
            String remark = String.format("定制上传数据清洗job,任务id：%s"
                    , config.getId());
            trackingService.trackPointLog(DataFlowDirection.IN
                    , apiCode
                    , "定制上传数据清洗job"
                    , total.get()
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

        log.warn(TITLE + "apiCode={}清洗结束,耗时：{}ms", config.getApiCode(), System.currentTimeMillis() - start);
    }


    public void processData(MarketingCustomerOriginalData originalData, List<MarketingDataCleanGeneralRuleConfig> ruleConfigList) {
        try {
            String apiCode = originalData.getApiCode();
            Long id = originalData.getId();
            MarketingPreUserDTO marketingPreUserDTO = dataClean(originalData,ruleConfigList);
            insertInfo(apiCode,marketingPreUserDTO,id,Boolean.FALSE);
        } catch (Exception e) {
            log.error(TITLE + "清洗处理异常", e);
        }
    }

    /**
     * 定制上传清洗
     * @param originalData  原始数据
     * @param ruleConfigList    清洗规则
     * @return 清洗后结果
     */
    public MarketingPreUserDTO dataClean(MarketingCustomerOriginalData originalData, List<MarketingDataCleanGeneralRuleConfig> ruleConfigList){
        JSONObject jsonData = JSON.parseObject(originalData.getJsonData());
        List<String> tieDataApiCode = marketingCommonConfig.getTieDataApiCode();
        //层级字段处理
        String levelField = null;
        List<MarketingDataCleanGeneralRuleConfig> ruleConfigListTmp = new ArrayList<>(ruleConfigList);
        List<MarketingDataCleanGeneralRuleConfig> dataItemList = ruleConfigListTmp.stream().filter(ruleConfig -> ruleConfig.getMappingField().equals("dataItems")).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(dataItemList)) {
            levelField = dataItemList.get(0).getCleanFields();
            ruleConfigListTmp.removeIf(config -> config.getMappingField().equals("dataItems"));
        }
        List<JSONObject> jsonObjectList = new ArrayList<>();
        if (StringUtils.isNotEmpty(levelField)) {
            jsonObjectList = JsonParseUtils.parseJsonArrayByName(jsonData, levelField);
        } else {
            jsonObjectList.add(jsonData);
        }
        MarketingPreUserDTO marketingPreUserDTO = new MarketingPreUserDTO();
        //taskId清洗
        List<MarketingDataCleanGeneralRuleConfig> taskConfigList = ruleConfigListTmp.stream().filter(ruleConfig -> ruleConfig.getMappingField().equals("taskId")).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(taskConfigList)) {
            marketingPreUserDTO.setTaskId(originalData.getApiCode().concat("_").concat(LocalDate.now().toString()));
        } else {
            marketingPreUserDTO.setTaskId((String) ruleCleaningService.executeCleaningRule(jsonData, taskConfigList.get(0)));
        }
        //requestId清洗
        List<MarketingDataCleanGeneralRuleConfig> requestIdConfigList = ruleConfigListTmp.stream().filter(ruleConfig -> ruleConfig.getMappingField().equals("requestId")).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(requestIdConfigList)) {
            marketingPreUserDTO.setRequestId(originalData.getApiCode().concat("_").concat(LocalDate.now().toString()).concat(UUID.randomUUID().toString()));
        } else {
            marketingPreUserDTO.setRequestId((String) ruleCleaningService.executeCleaningRule(jsonData, requestIdConfigList.get(0)));
        }
        //剔除taskId，requestId
        ruleConfigListTmp.removeIf(config -> config.getMappingField().equals("requestId") || config.getMappingField().equals("taskId"));
        //根据规则进行清洗处理
        List<MarketingPreUserDetailDTO> syncUsers = new ArrayList<>();
        List<Long> ids = snowflakeRedisGeneratorHandle.nextIds(jsonObjectList.size());
        AtomicInteger index = new AtomicInteger(0);
        jsonObjectList.forEach(jsonObject -> {
            MarketingPreUserDetailDTO marketingPreUserDetailDTO = new MarketingPreUserDetailDTO();
            //数据清洗
            dataCleanHandler(jsonObject, ruleConfigListTmp, marketingPreUserDetailDTO);
            dataCleanNotConfigHandler(jsonObject, ruleConfigListTmp, marketingPreUserDetailDTO);
            //打平数据结构
            if(tieDataApiCode.contains(originalData.getApiCode())){
                flattenReserveField1NestedObjects(marketingPreUserDetailDTO);
            }
            marketingPreUserDetailDTO.setFingerprint(ids.get(index.getAndIncrement()));
            syncUsers.add(marketingPreUserDetailDTO);
        });
        marketingPreUserDTO.setDataItems(syncUsers);
        marketingPreUserDTO.setDataSourceType(DataSourceTypeEnum.ORIGINAL_INTERFACE.getCode());
        return marketingPreUserDTO;
    }

    public void dataCleanNotConfigHandler(JSONObject jsonObject,
                                          Collection<MarketingDataCleanGeneralRuleConfig> ruleConfigList,
                                          MarketingPreUserDetailDTO marketingPreUserDetailDTO) {
        // 收集ruleConfigList中所有的mappingField，用于快速判断字段是否已配置
        Set<String> configuredFields = new HashSet<>();
        if (!CollectionUtils.isEmpty(ruleConfigList)) {
            ruleConfigList.forEach(ruleConfig -> {
                if (ruleConfig.getMappingField() != null) {
                    configuredFields.add(ruleConfig.getMappingField());
                }
            });
        }

        // 获取dataItems对象，判断是否存在
        Object dataItemsObj = jsonObject.get("dataItems");
        JSONObject targetObject;

        if (dataItemsObj instanceof JSONObject) {
            // 如果有dataItems，则遍历dataItems下的字段
            targetObject = (JSONObject) dataItemsObj;

            // 处理顶层的requestId和taskId字段（即使有dataItems，这两个字段也应该从顶层获取）
            if (!configuredFields.contains("requestId") && jsonObject.containsKey("requestId")) {
                Object requestIdValue = jsonObject.get("requestId");
                marketingPreUserDetailDTO.setRequestId(requestIdValue != null ? requestIdValue.toString() : null);
            }
            if (!configuredFields.contains("taskId") && jsonObject.containsKey("taskId")) {
                Object taskIdValue = jsonObject.get("taskId");
                marketingPreUserDetailDTO.setTaskId(taskIdValue != null ? taskIdValue.toString() : null);
            }
        } else {
            // 如果没有dataItems，则遍历最外层字段
            targetObject = jsonObject;
        }

        // 遍历目标对象中的所有字段
        for (Map.Entry<String, Object> entry : targetObject.entrySet()) {
            String fieldName = entry.getKey();
            Object fieldValue = entry.getValue();

            // 如果字段在ruleConfigList中存在，则跳过
            if (configuredFields.contains(fieldName)) {
                continue;
            }

            // 处理reserveField1和reserveField2字段，如果是JSONObject则打平
            if ("reserveField1".equals(fieldName) && fieldValue instanceof JSONObject) {
                // 将reserveField1中的字段打平，直接放到reserveField1中
                JSONObject reserveField1Obj = (JSONObject) fieldValue;
                for (Map.Entry<String, Object> reserveEntry : reserveField1Obj.entrySet()) {
                    String reserveFieldName = reserveEntry.getKey();
                    Object reserveFieldValue = reserveEntry.getValue();
                    // 如果嵌套字段不在配置中，则添加到reserveField1
                    if (!configuredFields.contains(reserveFieldName)) {
                        marketingPreUserDetailDTO.setReserveField1(
                                setExtendField(marketingPreUserDetailDTO.getReserveField1(), reserveFieldName, reserveFieldValue));
                    }
                }
                continue;
            }

            // 如果字段不在配置中，判断是否是固定字段
            switch (fieldName) {
                case "name":
                    marketingPreUserDetailDTO.setName(fieldValue != null ? fieldValue.toString() : null);
                    break;
                case "cell":
                    marketingPreUserDetailDTO.setCell(fieldValue != null ? fieldValue.toString() : null);
                    break;
                case "id":
                    marketingPreUserDetailDTO.setId(fieldValue != null ? fieldValue.toString() : null);
                    break;
                case "custNum":
                    marketingPreUserDetailDTO.setCustNum(fieldValue != null ? fieldValue.toString() : null);
                    break;
                case "operateType":
                    marketingPreUserDetailDTO.setOperateType(fieldValue != null ? fieldValue.toString() : null);
                    break;
                case "requestId":
                    marketingPreUserDetailDTO.setRequestId(fieldValue != null ? fieldValue.toString() : null);
                    break;
                case "taskId":
                    marketingPreUserDetailDTO.setTaskId(fieldValue != null ? fieldValue.toString() : null);
                    break;
                default:
                    // 其他字段都放在reserveField1中
                    marketingPreUserDetailDTO.setReserveField1(setExtendField(marketingPreUserDetailDTO.getReserveField1(), fieldName, fieldValue));
                    break;
            }
        }
    }


    public List<JSONObject> dataCleanByRules(List<JSONObject> jsonObjectList,
                                             List<MarketingDataCleanGeneralRuleConfig> ruleConfigList) {
        if (CollectionUtils.isEmpty(jsonObjectList) || CollectionUtils.isEmpty(ruleConfigList)) {
            return jsonObjectList;
        }

        jsonObjectList.forEach(jsonObject ->
                ruleConfigList.forEach(ruleConfig -> applyRuleAndWriteBack(jsonObject, ruleConfig))
        );
        return jsonObjectList;
    }

    /**
     * 对单个 JSON 对象应用一条清洗规则，并将结果写回原字段位置
     */
    private void applyRuleAndWriteBack(JSONObject jsonObject, MarketingDataCleanGeneralRuleConfig ruleConfig) {

        try {
            // 执行数据清洗，得到目标字段的新值
            Object result = ruleCleaningService.executeCleaningRule(jsonObject, ruleConfig);
            String cleanedValue = result != null ? result.toString() : null;

            String cleanFields = ruleConfig.getCleanFields();
            String mappingField = ruleConfig.getMappingField();
            String parentPath = ruleConfig.getParentPath();

            if (StringUtils.isEmpty(cleanFields)) {
                log.warn("清洗字段为空，跳过回填");
                return;
            }

            // 根据父路径信息，递归回填清洗后的值
            // 1）如果配置了父路径/层级，则按路径精确匹配回填
            // 2）如果未配置父路径，则回填到第一个匹配字段
            String expectedPath = JsonParseUtils.processNodePaths(parentPath);
            
            // 确定目标字段名：如果 mappingField 不为空且与 cleanFields 不同，则使用 mappingField，否则使用 cleanFields
            String targetKey = (StringUtils.isNotEmpty(mappingField) && !mappingField.equals(cleanFields))
                    ? mappingField : cleanFields;
            boolean needRename = StringUtils.isNotEmpty(mappingField) && !mappingField.equals(cleanFields);
            
            boolean updated = updateJsonValueByPath(jsonObject, cleanFields, targetKey, expectedPath, "", cleanedValue, needRename);
            if (!updated) {
                log.warn("未找到可回填的字段位置: cleanFields={}, mappingField={}, parentPath={}", cleanFields, mappingField, parentPath);
            }
        } catch (Exception e) {
            log.error("字段清洗异常", e);
        }
    }

    /**
     * 根据字段名与父路径，在 JSON 结构中递归回填清洗后的值
     * 支持字段名替换：如果 oldKey 和 newKey 不同，会删除 oldKey 并添加 newKey
     *
     * @param node         当前遍历的节点（JSONObject 或 JSONArray）
     * @param oldKey       原始字段名（用于查找）
     * @param newKey       目标字段名（用于回填，如果与 oldKey 不同则替换字段名）
     * @param expectedPath 期望的父路径（已做 dataItems/item 等归一化处理）
     * @param currentPath  当前遍历到的父路径
     * @param newValue     清洗后的新值
     * @param needRename   是否需要替换字段名（oldKey != newKey）
     * @return 是否成功回填
     */
    private boolean updateJsonValueByPath(Object node, String oldKey, String newKey, String expectedPath,
                                          String currentPath, String newValue, boolean needRename) {
        // 对当前路径做归一化（处理 dataItems / item 等特殊层级）
        currentPath = JsonParseUtils.processNodePaths(currentPath);

        if (node instanceof JSONObject) {
            JSONObject jsonObj = (JSONObject) node;

            // expectedPath 为空表示不限制父路径，直接命中第一个
            Boolean isParentNode = StringUtils.isEmpty(expectedPath) || currentPath.equals(expectedPath);
            // 当前对象本身是否是待回填字段的父节点
            if (jsonObj.containsKey(oldKey) && isParentNode) {
                if (needRename) {
                    // 需要替换字段名：删除原字段，添加新字段
                    jsonObj.remove(oldKey);
                    jsonObj.put(newKey, newValue);
                } else {
                    // 只更新值
                    jsonObj.put(oldKey, newValue);
                }
                return true;
            }

            // 继续向下递归遍历
            for (Map.Entry<String, Object> entry : jsonObj.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                String nextPath = StringUtils.isEmpty(currentPath) ? key : (currentPath + "." + key);

                // 处理嵌套在字符串中的 JSON 结构
                if (value instanceof String) {
                    String strVal = (String) value;
                    if (JsonParseUtils.isJsonObject(strVal)) {
                        try {
                            JSONObject nestedJson = JSON.parseObject(strVal);
                            if (updateJsonValueByPath(nestedJson, oldKey, newKey, expectedPath, nextPath, newValue, needRename)) {
                                // 回写嵌套 JSON 字符串
                                jsonObj.put(key, nestedJson.toString());
                                return true;
                            }
                        } catch (Exception ex) {
                            log.error("字符串JSON解析失败, 按原始字符串处理: {}", ex.getMessage());
                        }
                    }
                } else if (value instanceof JSONObject || value instanceof JSONArray) {
                    return updateJsonValueByPath(value, oldKey, newKey, expectedPath, nextPath, newValue, needRename);
                }
            }
        } else if (node instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) node;
            // 数组元素不改变路径语义，沿用当前路径
            for (int i = 0; i < jsonArray.size(); i++) {
                Object item = jsonArray.get(i);

                if (item instanceof String) {
                    String strVal = (String) item;
                    if (JsonParseUtils.isJsonObject(strVal)) {
                        try {
                            JSONObject nestedJson = JSON.parseObject(strVal);
                            if (updateJsonValueByPath(nestedJson, oldKey, newKey, expectedPath, currentPath, newValue, needRename)) {
                                jsonArray.set(i, nestedJson.toString());
                                return true;
                            }
                        } catch (Exception ex) {
                            log.error("数组元素JSON字符串解析失败, 按原始字符串处理: {}", ex.getMessage());
                        }
                    }
                } else if (item instanceof JSONObject || item instanceof JSONArray) {
                    return updateJsonValueByPath(item, oldKey, newKey, expectedPath, currentPath, newValue, needRename);
                }
            }
        }

        return false;
    }

    /**
     * 插入清洗后的数据信息
     *
     * @param apiCode API编码，用于标识数据来源
     * @param marketingPreUserDTO 营销预处理用户数据DTO，包含清洗后的用户信息
     * @param id 原始数据的ID，用于更新清洗状态
     * @param isTest 是否为测试模式
     *               true：试跑模式，数据插入到测试表中，使用测试API码
     */
    public void insertInfo(String apiCode, MarketingPreUserDTO marketingPreUserDTO, Long id,Boolean isTest){
        //试跑
        if(isTest){
            //插入上传info表
            MarketingSyncInfo syncInfo = new MarketingSyncInfo();
            try {
                syncInfo.setApiCode(marketingCommonConfig.getDatacleanTestRunApiCode());
                syncInfo.setCusBatch(marketingPreUserDTO.getTaskId());
                syncInfo.setRequestBatch(marketingPreUserDTO.getRequestId());
                syncInfo.setCreateTime(new Date());
                syncInfo.setJsonData(JSON.toJSONString(marketingPreUserDTO));
                syncInfo.setActualNum(marketingPreUserDTO.getDataItems().size());
                marketingUserMapper.insertMarketingPreUserByText(syncInfo);
            } catch (DuplicateKeyException keyException) {
                log.error("数据清洗上传数据request_batch重复，requestBatch = {}", marketingPreUserDTO.getRequestId());
            } catch (Exception ex) {
                log.error("数据清洗上传数据插入异常", ex.getMessage());
            }
            //插入上传明细表
            pushRuleService.insertMarketingPreUserSync(syncInfo.getId());
            return;
        }
        //写入到info表
        UploadDataDTO uploadDataDTO = new UploadDataDTO();
        uploadDataDTO.setApiCode(apiCode);
        uploadDataDTO.setJsonData(JSON.toJSONString(marketingPreUserDTO));
        pushInfoService.pushUploadByRetry(uploadDataDTO, null);
        MarketingCustomerOriginalData update = new MarketingCustomerOriginalData();
        //回写requestId
        if (StringUtils.isEmpty(update.getRequestId())) {
            update.setRequestId(marketingPreUserDTO.getRequestId());
        }
        update.setCleanStatus(DataCleanStatusEnum.COMPLETE.getCode());
        update.setId(id);
        marketingCustomerOriginalDataMapper.updateByPrimaryKeySelective(update);
    }

    @Override
    public void dataCleanHandler(JSONObject jsonObject, Collection<MarketingDataCleanGeneralRuleConfig> ruleConfigList,
                                MarketingPreUserDetailDTO marketingPreUserDetailDTO) {
        ruleConfigList.forEach(ruleConfig -> {
            Object result = ruleCleaningService.executeCleaningRule(jsonObject, ruleConfig);
            if (StringUtils.isEmpty(result)) {
                return;
            }
            switch (ruleConfig.getMappingField()) {
                case "name":
                    marketingPreUserDetailDTO.setName((String) result);
                    break;
                case "cell":
                    marketingPreUserDetailDTO.setCell((String) result);
                    break;
                case "id":
                    marketingPreUserDetailDTO.setId((String) result);
                    break;
                case "custNum":
                    marketingPreUserDetailDTO.setCustNum((String) result);
                    break;
                case "operateType":
                    marketingPreUserDetailDTO.setOperateType((String) result);
                    break;
                default:
                    marketingPreUserDetailDTO.setReserveField1(setExtendField(marketingPreUserDetailDTO.getReserveField1(), ruleConfig.getMappingField(), result));

            }
        });
    }

    /**
     * 解析 b_marketing_clean_data_file.virtual_headers 的 JSON 为 Map。
     * 读完全部真实 header 与行数据后，将虚拟 header 的 key 作为列、value 追加到每行，当作一般列参与清洗。
     */
    private Map<String, String> parseVirtualHeaders(String virtualHeadersJson) {
        if (StringUtils.isBlank(virtualHeadersJson)) {
            return Collections.emptyMap();
        }
        try {
            JSONObject jo = JSON.parseObject(virtualHeadersJson);
            if (jo == null) {
                return Collections.emptyMap();
            }
            Map<String, String> map = new HashMap<>();
            for (String key : jo.keySet()) {
                Object v = jo.get(key);
                map.put(key, v != null ? v.toString() : "");
            }
            return map;
        } catch (Exception e) {
            log.warn("解析 virtual_headers 失败: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    @Override
    public void fileUploadDataClean(MarketingCleanDataFile cleanFile, MarketingDataCleanGeneralConfig config) {
        log.warn(TITLE + "文件上传数据清洗开始，文件：{}", cleanFile.getFileName());
        Long start = System.currentTimeMillis();

        String filePath = cleanFile.getLocalPath();
        String fileName = cleanFile.getFileName();
        String apiCode = config.getApiCode();

        //查询规则
        MarketingDataCleanGeneralRuleConfigExample ruleConfigExample = new MarketingDataCleanGeneralRuleConfigExample();
        ruleConfigExample.createCriteria().andCleanConfigIdEqualTo(config.getId()).andIsDelEqualTo(1);
        List<MarketingDataCleanGeneralRuleConfig> ruleConfigList = marketingDataCleanGeneralRuleConfigMapper.selectByExample(ruleConfigExample);

        if (CollectionUtils.isEmpty(ruleConfigList)) {
            log.warn("未找到清洗规则配置，apiCode: {}", apiCode);
            return;
        }
        Map<String, String> virtualHeadersMap = parseVirtualHeaders(cleanFile.getVirtualHeaders());
        String fieldDelimiter = DataCleanDelimiterUtils.resolveDelimiter(config.getSftpFileSeparator());
        try {
            // 批量读取并处理文件
            processByBatch(filePath, fileName, apiCode, ruleConfigList, virtualHeadersMap, fieldDelimiter);
        } catch (Exception e) {
            log.error(TITLE + "文件读取异常，文件路径: " + filePath, e);
        }
        log.warn(TITLE + "文件上传数据清洗结束，文件：{}，耗时：{}ms", fileName, System.currentTimeMillis() - start);
    }

    /**
     * 是否为 Excel 文件（.xlsx / .xls）
     */
    private static boolean isExcelFile(String fileName) {
        if (fileName == null) {
            return false;
        }
        String lower = fileName.toLowerCase();
        return lower.endsWith(".xlsx") || lower.endsWith(".xls");
    }

    /**
     * 从 Excel 第一 sheet 读取：第一行为表头，其余行为数据；每行转为逗号分隔字符串，与 CSV 下游一致。
     */
    private ExcelReadResult readExcelToHeadersAndLines(Workbook workbook) {
        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null) {
            log.error("Excel 第一个 sheet 为空");
            return null;
        }
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            log.error("Excel 表头行为空");
            return null;
        }
        DataFormatter formatter = new DataFormatter();
        String[] headers = rowToCellStrings(headerRow, formatter);
        if (headers.length == 0) {
            log.error("Excel 表头解析失败");
            return null;
        }
        List<String> lines = new ArrayList<>();
        for (int r = 1; r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null) {
                continue;
            }
            String line = rowToCommaSeparated(row, formatter, headers.length);
            if (line != null && !line.isBlank()) {
                lines.add(line);
            }
        }
        ExcelReadResult result = new ExcelReadResult();
        result.headers = headers;
        result.lines = lines;
        return result;
    }

    private static String[] rowToCellStrings(Row row, DataFormatter formatter) {
        int lastCellNum = row.getLastCellNum();
        if (lastCellNum <= 0) {
            return new String[0];
        }
        String[] arr = new String[lastCellNum];
        for (int c = 0; c < lastCellNum; c++) {
            arr[c] = formatter.formatCellValue(row.getCell(c));
        }
        return arr;
    }

    /** 输出固定 {@code maxCells} 列，尾部空 Excel 单元格补空串，与表头列数一致。 */
    private static String rowToCommaSeparated(Row row, DataFormatter formatter, int maxCells) {
        if (maxCells <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int c = 0; c < maxCells; c++) {
            if (c > 0) {
                sb.append(',');
            }
            Cell cell = row.getCell(c);
            sb.append(cell == null ? "" : formatter.formatCellValue(cell));
        }
        return sb.toString();
    }

    private static class ExcelReadResult {
        String[] headers;
        List<String> lines;
    }

    /**
     * 批量处理文件数据 - 每500行为一批，同步处理；支持 CSV/文本 与 xlsx/xls
     */
    private void processByBatch(String filePath, String fileName, String apiCode,
                               List<MarketingDataCleanGeneralRuleConfig> ruleConfigList,
                               Map<String, String> virtualHeadersMap, String fieldDelimiter) {
        final int BATCH_SIZE = 500;
        String[] headers = null;
        List<String> batchLines = new ArrayList<>();
        int totalProcessed = 0;
        File file = new File(filePath + fileName);
        ThreadPoolExecutor pool = BrExecutors.getThreadPool(5, 5, 50);

        if (isExcelFile(fileName)) {
            try (FileInputStream fis = new FileInputStream(file);
                 Workbook workbook = WorkbookFactory.create(fis)) {
                ExcelReadResult result = readExcelToHeadersAndLines(workbook);
                if (result == null) {
                    return;
                }
                headers = result.headers;
                List<String> allLines = result.lines;
                for (int start = 0; start < allLines.size(); start += BATCH_SIZE) {
                    int end = Math.min(start + BATCH_SIZE, allLines.size());
                    List<String> batch = new ArrayList<>(allLines.subList(start, end));
                    modifyFilePoolSize(pool);
                    String[] finalHeaders = headers;
                    int finalStart = start;
                    Map<String, String> finalVirtualHeaders = virtualHeadersMap;
                    String finalDelim = fieldDelimiter;
                    pool.submit(() -> processBatchDataSync(batch, finalHeaders, ruleConfigList, apiCode, fileName,
                            finalStart, finalVirtualHeaders, finalDelim));
                    totalProcessed += batch.size();
                }
            } catch (Exception e) {
                log.error("读取 Excel 失败，文件路径: " + filePath + fileName, e);
            }
        } else {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                boolean isFirstLine = true;
                while ((line = reader.readLine()) != null) {
                    String lineData = line.trim();
                    if (isFirstLine) {
                        headers = DataCleanDelimiterUtils.splitLine(lineData, fieldDelimiter);
                        if (headers == null || headers.length == 0) {
                            log.error("文件表头解析失败，文件路径: {}", filePath);
                            return;
                        }
                        isFirstLine = false;
                        continue;
                    }
                    if (StringUtils.isEmpty(lineData)) {
                        continue;
                    }
                    batchLines.add(lineData);
                    if (batchLines.size() >= BATCH_SIZE) {
                        modifyFilePoolSize(pool);
                        String[] finalHeaders = headers;
                        int finalTotalProcessed = totalProcessed;
                        List<String> dataList = new ArrayList<>(batchLines);
                        Map<String, String> finalVirtualHeaders = virtualHeadersMap;
                        String finalDelimCsv = fieldDelimiter;
                        pool.submit(() -> {
                            processBatchDataSync(dataList, finalHeaders, ruleConfigList, apiCode, fileName,
                                    finalTotalProcessed, finalVirtualHeaders, finalDelimCsv);
                        });
                        totalProcessed += batchLines.size();
                        batchLines.clear();
                    }
                }
                if (!batchLines.isEmpty()) {
                    processBatchDataSync(batchLines, headers, ruleConfigList, apiCode, fileName, totalProcessed,
                            virtualHeadersMap, fieldDelimiter);
                    totalProcessed += batchLines.size();
                }
            } catch (IOException e) {
                log.error("读取文件失败，文件路径: " + filePath, e);
            }
        }

        try {
            String remark = String.format("清洗系统-文件清洗作业,清洗文件：%s", filePath + fileName);
            trackingService.trackPointLog(DataFlowDirection.OUT
                    , apiCode
                    , "清洗系统-文件清洗作业"
                    , Long.valueOf(totalProcessed)
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
        log.warn("文件处理完成，总共处理数据行数: {}", totalProcessed);

        pool.shutdown();
        try {
            while (!pool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("等待线程池结束");
            }
        } catch (InterruptedException ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.SERVICEERROR_UNKNOWN.getCode(), "文件上传数据清洗线程池停止异常！"), ex);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 功能说明：
     * 将一批原始文件行 清洗值上传表
     *
     * @param batchLines    待处理的文件行数据列表（不包含表头）
     * @param headers       文件表头字段数组
     * @param ruleConfigList 数据清洗规则配置列表，定义了字段映射和清洗逻辑
     * @param apiCode       API编码
     * @param fileName      文件名，用于日志记录和标识
     * @param startIndex    当前批次在整个文件中的起始索引，用于计算实际行号
     */
    @Override
    public void processBatchDataSync(List<String> batchLines, String[] headers,
                                     List<MarketingDataCleanGeneralRuleConfig> ruleConfigList,
                                     String apiCode, String fileName, int startIndex,
                                     Map<String, String> virtualHeadersMap, String fieldDelimiter) {
        try {
            List<JSONObject> fileJsonData = fileDataAssemble(batchLines, headers, fileName, startIndex,
                    virtualHeadersMap, fieldDelimiter);
            cleanData(fileJsonData, ruleConfigList, apiCode, fileName, Boolean.FALSE, virtualHeadersMap);
        } catch (Exception e) {
            log.error("批次数据处理异常", e);
        }
    }

    public MarketingPreUserDTO cleanData(List<JSONObject> jsonObjectList,
                                         List<MarketingDataCleanGeneralRuleConfig> ruleConfigList,
                                         String apiCode, String fileName, Boolean isTest,
                                         Map<String, String> virtualHeadersMap) {
        List<MarketingPreUserDetailDTO> syncUsers = new ArrayList<>();
        // 处理数据清洗
        MarketingPreUserDTO marketingPreUserDTO = new MarketingPreUserDTO();
        AtomicInteger index = new AtomicInteger(0);
        List<Long> ids = snowflakeRedisGeneratorHandle.nextIds(jsonObjectList.size());
        if (virtualHeadersMap != null && !virtualHeadersMap.isEmpty()) {
            jsonObjectList.forEach(jo -> jo.putAll(virtualHeadersMap));
        }
        jsonObjectList.forEach(jsonData -> {
            MarketingPreUserDetailDTO marketingPreUserDetailDTO = new MarketingPreUserDetailDTO();
            dataCleanHandler(jsonData, ruleConfigList, marketingPreUserDetailDTO);
            marketingPreUserDetailDTO.setFingerprint(ids.get(index.getAndIncrement()));
            syncUsers.add(marketingPreUserDetailDTO);

        });
        marketingPreUserDTO.setTaskId(apiCode + "_" + LocalDate.now()+"_"+fileName);
        marketingPreUserDTO.setRequestId(apiCode + "_" + LocalDate.now() + "_" + UUID.randomUUID());
        // 组装最终数据
        marketingPreUserDTO.setDataItems(syncUsers);
        marketingPreUserDTO.setDataSourceType(DataSourceTypeEnum.ORIGINAL_INTERFACE.getCode());
        // 推送清洗后的数据
        UploadDataDTO uploadDataDTO = new UploadDataDTO();
        uploadDataDTO.setApiCode(apiCode);
        uploadDataDTO.setJsonData(JSON.toJSONString(marketingPreUserDTO));
        if(isTest){
            //插入上传info表
            MarketingSyncInfo syncInfo = new MarketingSyncInfo();
            try {
                syncInfo.setApiCode(marketingCommonConfig.getDatacleanTestRunApiCode());
                syncInfo.setCusBatch(marketingPreUserDTO.getTaskId());
                syncInfo.setRequestBatch(marketingPreUserDTO.getRequestId());
                syncInfo.setCreateTime(new Date());
                syncInfo.setJsonData(uploadDataDTO.getJsonData());
                syncInfo.setActualNum(marketingPreUserDTO.getDataItems().size());
                marketingUserMapper.insertMarketingPreUserByText(syncInfo);
            } catch (DuplicateKeyException keyException) {
                log.error("数据清洗上传数据request_batch重复，requestBatch = {}", marketingPreUserDTO.getRequestId());
            } catch (Exception ex) {
                log.error("数据清洗上传数据插入异常", ex.getMessage());
            }
            //插入上传明细表
            pushRuleService.insertMarketingPreUserSync(syncInfo.getId());
        }else {
            pushInfoService.pushUploadByRetry(uploadDataDTO, null);
        }
        return marketingPreUserDTO;
    }

    @Override
    public List<JSONObject> fileDataAssemble(List<String> batchLines, String[] headers, String fileName, int startIndex,
                                            Map<String, String> virtualHeadersMap, String fieldDelimiter) {
        List<JSONObject> jsonArray = new ArrayList<>();
        for (int i = 0; i < batchLines.size(); i++) {
            String line = batchLines.get(i);
            int actualRowIndex = startIndex + i + 2;
            JSONObject jsonData = buildJsonFromLineData(line, headers, actualRowIndex, fieldDelimiter);
            if (jsonData == null) {
                log.error("文件{},第{}行数据解析失败，跳过处理: {}", fileName, actualRowIndex, line);
                continue;
            }
            if (virtualHeadersMap != null && !virtualHeadersMap.isEmpty()) {
                jsonData.putAll(virtualHeadersMap);
            }
            jsonArray.add(jsonData);
        }
        return jsonArray;
    }

    @Override
    public Result commonClean(DataCleanDTO dto) {

        try {
            //参数空值校验
            String errorMsg = paramsValid(dto);
            if (!StringUtils.isEmpty(errorMsg)) {
                log.warn("数据清洗通用接口参数错误,params={}", dto);
                return new Result().failure().setDate(new CommonCleanResponseVO(CodeEnum.PARAM_ERROR,null));
            }

            pushRuleService.sendJsonParseMq(dto.getApiCode(), 0, dto.getSystemType()
                    , dto.getDataType(), dto.getAcceptType(), String.valueOf(dto.getJsonData()));

            Map<String, MarketingDataCleanGeneralRuleConfig> configRule = dataCleanService.getConfigRule(
                    dto.getApiCode(),
                    dto.getSystemType(),
                    dto.getDataType(),
                    dto.getAcceptType(),
                    DataProcessEnum.RuleStatusEnum.PRE_SUCCESS.getCode());

            if (configRule == null || configRule.isEmpty()) {
                return new Result().failure().setDate(new CommonCleanResponseVO(CodeEnum.NOT_FOUND_CLEAN_RULE_CONFIG,dto.getJsonData()));
            }
            List<MarketingDataCleanGeneralRuleConfig> ruleConfigListTmp = new ArrayList<>(configRule.values());

            //数据清洗
            String jsonData = dto.getJsonData();

            //层级字段处理
            String levelField = null;

            List<MarketingDataCleanGeneralRuleConfig> dataItemList = ruleConfigListTmp.stream().filter(
                    ruleConfig -> ruleConfig.getMappingField().equals("dataItems")
            ).toList();
            if (!CollectionUtils.isEmpty(dataItemList)) {
                levelField = dataItemList.get(0).getCleanFields();
                ruleConfigListTmp.removeIf(config -> config.getMappingField().equals("dataItems"));
            }

            // 解析JSON对象
            JSONObject jsonObject = JSON.parseObject(jsonData);

            // 如果没有配置层级字段，检查JSON对象中是否有值为数组的key
            if (StringUtils.isEmpty(levelField)) {
                for (String key : jsonObject.keySet()) {
                    Object value = jsonObject.get(key);
                    if (value instanceof JSONArray) {
                        log.warn("未配置层级字段，但JSON中存在值为数组的字段[{}]，直接返回原值，apiCode:{}", key, dto.getApiCode());
                        return new Result().failure().setDate(new CommonCleanResponseVO(CodeEnum.NOT_FOUND_DATA_ITEMS_CONFIG,dto.getJsonData()));
                    }
                }
            }

            if (StringUtils.isNotEmpty(levelField)) {
                // 如果有层级字段，提取该字段对应的数组进行清洗
                List<JSONObject> jsonObjectLists = JsonParseUtils.parseJsonArrayByName(jsonObject, levelField);
                // 清洗数组中的数据
                dataCleanByRules(jsonObjectLists, ruleConfigListTmp);
                // 将清洗后的数组转换为JSONArray并直接替换原JSON对象中的字段
                JSONArray cleanedArray = new JSONArray();

                for (JSONObject cleanedJson : jsonObjectLists) {
                    JSONObject object = cleanedJson.getJSONObject(levelField);
                    cleanedArray.add(object);
                }

                jsonObject.put(levelField, cleanedArray);
                return new Result().success().setDate(new CommonCleanResponseVO(CodeEnum.SUCC,jsonObject.toJSONString()));
            } else {
                // 没有层级字段，直接清洗整个对象
                List<JSONObject> jsonObjectLists = new ArrayList<>();
                jsonObjectLists.add(jsonObject);
                dataCleanByRules(jsonObjectLists, ruleConfigListTmp);
                // 返回清洗后的对象
                return new Result().success().setDate(new CommonCleanResponseVO(CodeEnum.SUCC,jsonObject.toJSONString()));
            }
        } catch (Exception e) {
            log.error("通用数据清洗异常，message:{}", e.getMessage());
            return new Result().failure().setMessage("通用数据清洗异常，返回原值").setDate(new CommonCleanResponseVO(CodeEnum.ERROR,dto.getJsonData()));
        }
    }

    public String paramsValid(DataCleanDTO dto) {
        StringBuilder errorMsg = new StringBuilder();
        if (StringUtils.isEmpty(dto.getApiCode())) {
            errorMsg.append("apiCode为空");
        }
        if (StringUtils.isEmpty(dto.getSystemType())){
            errorMsg.append("，systemType为空");
        }
        if (StringUtils.isEmpty(dto.getDataType())){
            errorMsg.append("，dataType为空");
        }
        if (StringUtils.isEmpty(dto.getAcceptType())){
            errorMsg.append("，acceptType为空");
        }
        if (StringUtils.isEmpty(dto.getJsonData())){
            errorMsg.append("，jsonData为空");
        }
        return errorMsg.toString();
    }

    @Override
    public void fileUploadCleanPre(List<List<RuleCleaningResult>> resultList, List<MarketingDataCleanGeneralRuleConfig> ruleList,
                                   MarketingCleanDataFile cleanDataFile, Integer actualNum) {
        List<JSONObject> jsonObjects = JSON.parseObject(cleanDataFile.getTestRunData(),
                new TypeReference<List<JSONObject>>() {});
        Map<String, String> virtualHeadersMap = parseVirtualHeaders(cleanDataFile.getVirtualHeaders());
        MarketingPreUserDTO marketingPreUserDTO = cleanData(jsonObjects, ruleList, cleanDataFile.getApiCode(),
                cleanDataFile.getFileName(), Boolean.TRUE, virtualHeadersMap);
        //查询明细表
        List<MarketingSyncUser> syncUserList = marketingSyncUserMapper.getSyncUserByRequestBatch(marketingCommonConfig.getDatacleanTestRunApiCode()
                ,marketingPreUserDTO.getRequestId());
        if(CollectionUtils.isEmpty(syncUserList)){
            return;
        }
        Map<String,String> ruleMap =  ruleList.stream().collect(Collectors.toMap(MarketingDataCleanGeneralRuleConfig::getMappingField,
                MarketingDataCleanGeneralRuleConfig::getCleanFields, (existing, replacement) -> existing));
        //组装数据
        int size = actualNum > jsonObjects.size() ? jsonObjects.size() : actualNum;
        for (int i = 0; i < size; i++) {
            List<RuleCleaningResult> cleaningResultItems = new ArrayList<>();
            JSONObject item = jsonObjects.get(i);
            String custNum = (String) JsonParseUtils.findFirstValueByKey(item, ruleMap.get("custNum"));
            MarketingSyncUser result = syncUserList.stream().filter(marketingSyncUser -> marketingSyncUser.getCustNum().equals(custNum)).findFirst().orElse(null);
            ruleMap.forEach((mappingField,cleanField)->{
                RuleCleaningResult ruleCleaningResult = new RuleCleaningResult();
                ruleCleaningResult.setCleanFields(cleanField);
                ruleCleaningResult.setCleanValue(JsonParseUtils.findFirstValueByKey(item, cleanField));
                ruleCleaningResult.setMappingField(mappingField);
                ruleCleaningResult.setMappingValue(JsonParseUtils.findFirstValueByKey(JSON.toJSON(result), mappingField));
                cleaningResultItems.add(ruleCleaningResult);
            });
            resultList.add(cleaningResultItems);
        }
    }

    /**
     * 根据表头和行数据构建JSON对象 - 简化版本
     */
    private JSONObject buildJsonFromLineData(String lineData, String[] headers, int rowIndex, String fieldDelimiter) {
        try {
            String[] values = DataCleanDelimiterUtils.splitLine(lineData, fieldDelimiter);

            if (values.length == 0) {
                log.warn("第{}行数据为空", rowIndex);
                return null;
            }

            // 构建JSON对象
            JSONObject jsonObject = new JSONObject();

            // 按表头字段数量处理，多余的数据忽略，缺失的数据设为空字符串
            for (int i = 0; i < headers.length; i++) {
                String fieldName = headers[i];
                String fieldValue = i < values.length ? values[i] : "";
                // 去除值的前后空格和引号，但不解析具体类型
                fieldValue = fieldValue.trim().replaceAll("^\"|\"$", "");
                jsonObject.put(fieldName, fieldValue);
            }
            return jsonObject;
        } catch (Exception e) {
            log.error("第{}行JSON构建失败: {}", rowIndex, e.getMessage());
            return null;
        }
    }

    /**
     * 定制上传：将 reserveField1 中任意层级的 JSON 对象打平到同一层，去掉中间节点（如 details、ivrParam）。
     * 嵌套对象与「值为 JSON 对象的字符串」先展开；当前层的叶子字段后写入，同名键以当前层为准（保证清洗/映射后的取值覆盖子层原始值）。
     */
    private void flattenReserveField1NestedObjects(MarketingPreUserDetailDTO detailDTO) {
        String reserveField1 = detailDTO.getReserveField1();
        if (StringUtils.isEmpty(reserveField1)) {
            return;
        }
        try {
            JSONObject root = JSONObject.parseObject(reserveField1);
            if (root == null || root.isEmpty()) {
                return;
            }
            JSONObject flat = new JSONObject(true);
            flattenJsonObjectIntoFlat(root, flat);
            detailDTO.setReserveField1(flat.toJSONString());
        } catch (Exception e) {
            log.warn(TITLE + "reserveField1 打平失败，保留原值, apiCode相关明细 fingerprint 待查日志", e);
        }
    }

    private void flattenJsonObjectIntoFlat(JSONObject src, JSONObject acc) {
        if (src == null || src.isEmpty()) {
            return;
        }
        // 1. 先展开子对象 / 可解析为对象的字符串，深度优先合并
        for (String key : new ArrayList<>(src.keySet())) {
            Object val = src.get(key);
            if (val instanceof JSONObject) {
                flattenJsonObjectIntoFlat((JSONObject) val, acc);
            } else if (val instanceof String) {
                JSONObject parsed = tryParseNestedJsonObject((String) val);
                if (parsed != null) {
                    flattenJsonObjectIntoFlat(parsed, acc);
                }
            }
        }
        // 2. 再写入当前层的叶子，覆盖同名键（外层清洗结果优先）
        for (String key : new ArrayList<>(src.keySet())) {
            Object val = src.get(key);
            if (val instanceof JSONObject) {
                continue;
            }
            if (val instanceof JSONArray) {
                acc.put(key, ((JSONArray) val).toJSONString());
                continue;
            }
            if (val instanceof String) {
                if (tryParseNestedJsonObject((String) val) != null) {
                    continue;
                }
            }
            acc.put(key, val);
        }
    }

    private JSONObject tryParseNestedJsonObject(String raw) {
        if (StringUtils.isEmpty(raw)) {
            return null;
        }
        String t = raw.trim();
        if (t.length() < 2 || t.charAt(0) != '{' || t.charAt(t.length() - 1) != '}') {
            return null;
        }
        try {
            return JSON.parseObject(raw);
        } catch (Exception e) {
            return null;
        }
    }

    private String setExtendField(String reserveField1, String field, Object result) {
        JSONObject jsonObject;
        if (StringUtils.isNotEmpty(reserveField1)) {
            jsonObject = JSONObject.parseObject(reserveField1);
        } else {
            jsonObject = new JSONObject();
        }
        jsonObject.put(field, result);
        return jsonObject.toString();
    }


    private void modifyCorePoolSize(ThreadPoolExecutor pool) {
        Integer threadNum =
                marketingCommonConfig.getCustomUploadCleanThreadNum();
        if (!Objects.isNull(threadNum)) {
            ThreadPoolAdjustmentUtil.adjustThreadPoolSize(pool, threadNum);

        }
        log.warn(TITLE + "处理线程数core={}，max={}", pool.getCorePoolSize(), pool.getMaximumPoolSize());
    }


    private void modifyFilePoolSize(ThreadPoolExecutor pool) {
        Integer threadNum =
                marketingCommonConfig.getUploadFileCleanThreadNum();
        if (!Objects.isNull(threadNum)) {
            ThreadPoolAdjustmentUtil.adjustThreadPoolSize(pool, threadNum);
        }
        log.warn( "文件清洗处理线程数core={}，max={}", pool.getCorePoolSize(), pool.getMaximumPoolSize());
    }

    @Override
    public void uploadDetailCleanHandler(JSONObject jsonObject, Collection<MarketingDataCleanGeneralRuleConfig> ruleConfigList,
                                         MarketingSyncUser marketingSyncUser) {
        ruleConfigList.forEach(ruleConfig -> {
            //数据清洗
            Object result = ruleCleaningService.executeCleaningRule(jsonObject, ruleConfig);
            switch (ruleConfig.getMappingField()) {
                case "name":
                    marketingSyncUser.setName((String) result);
                    break;
                case "cell":
                    marketingSyncUser.setCell((String) result);
                    break;
                case "id":
                    marketingSyncUser.setIdCard((String) result);
                    break;
                case "custNum":
                    marketingSyncUser.setCustNum((String) result);
                    break;
                case "operateType":
                    marketingSyncUser.setOperateType((String) result);
                    break;
                default:
                    marketingSyncUser.setReserveField1(setExtendField(marketingSyncUser.getReserveField1(), ruleConfig.getMappingField(), result));

            }
        });

    }



}



