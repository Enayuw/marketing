package com.br.marketing.service.clean.common.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.JsonParseUtils;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.dto.dataclean.mq.MqDataJsonParse;
import com.br.marketing.entity.*;
import com.br.marketing.enums.clean.DataCleanStatusEnum;
import com.br.marketing.enums.clean.DataProcessEnum;
import com.br.marketing.enums.clean.DataSourceTypeEnum;
import com.br.marketing.mapper.MarketingDataCleanGeneralRuleConfigMapper;
import com.br.marketing.mapper.MarketingJsonNodeParseMapper;
import com.br.marketing.mapper.rulecleaning.MarketingCustomerOriginalDataMapper;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.service.clean.common.DataCleanService;
import com.br.marketing.service.ruleCleaning.RuleCleaningService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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

    private static final String TITLE = "【定制上传数据清洗】";

    @Override
    public Result<Boolean> customerDataJsonParse(String message) {
        Result<Boolean> result = new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(false);

        try {
            MqDataJsonParse mqDataJsonParse = JSON.parseObject(message, MqDataJsonParse.class);
            //获取表名
            String tableName = DataProcessEnum.getByTypes(mqDataJsonParse.getDataType(), mqDataJsonParse.getAcceptType()).getTableName();

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
                        mqDataJsonParse.getDataType(),
                        mqDataJsonParse.getAcceptType(),
                        "",
                        "",
                        jsonObject,
                        0,
                        false
                );

            } else {
                log.warn("数据ID: {} 的JSON数据为空", mqDataJsonParse.getDataId());
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DATACLEANING_SERVICEERROR.getCode(),
                    "客户数据JSON结构解析异常mq=" + message), e);
        }
        return result;
    }


    /**
     * 递归处理JSON节点并存入数据库
     *
     * @param apiCode     API编码
     * @param dataType    数据类型
     * @param acceptType  接收类型
     * @param nodeName    节点名称
     * @param parentPath  父节点路径
     * @param nodeValue   节点值
     * @param level       节点层级
     * @param isArrayItem 是否为数组元素
     */
    private void processJsonNode(String apiCode, Integer dataType, Integer acceptType,
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
            saveNodeData(apiCode, dataType, acceptType, nodeName, level, parentPath, nodeType, isArrayItem, nodeValueStr);

            // 构建新的父路径
            String newParentPath = parentPath;
            if (!nodeName.isEmpty()) {
                newParentPath = parentPath.isEmpty() ? nodeName : parentPath + "." + nodeName;
            }

            // 递归处理对象的每个字段
            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                processJsonNode(apiCode, dataType, acceptType, entry.getKey(), newParentPath, entry.getValue(), level + 1, false);
            }
        } else if (nodeValue instanceof JSONArray) {
            // 数组类型
            JSONArray jsonArray = (JSONArray) nodeValue;
            nodeType = "array";

            // 数组值直接转为字符串
            nodeValueStr = jsonArray.toString();

            // 保存当前数组节点，包含节点值
            saveNodeData(apiCode, dataType, acceptType, nodeName, level, parentPath, nodeType, isArrayItem, nodeValueStr);

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
                            processJsonNode(apiCode, dataType, acceptType, arrayItemName, newParentPath, element, level + 1, true);
                        }
                    }
                } else {
                    // 如果数组元素是基本类型(primitive)，只记录数组节点本身，不再继续遍历数组元素
                    log.debug("数组元素是基本类型，不再继续遍历: {}", newParentPath);
                }
            }
        } else {
            // 原始类型 (字符串、数字、布尔值等)
            nodeType = "primitive";

            // 原始类型值直接转为字符串
            nodeValueStr = nodeValue.toString();

            // 保存原始类型节点，包含节点值
            saveNodeData(apiCode, dataType, acceptType, nodeName, level, parentPath, nodeType, isArrayItem, nodeValueStr);
        }
    }

    /**
     * 保存节点数据到数据库
     */
    private void saveNodeData(String apiCode, Integer dataType, Integer acceptType, String nodeName,
                              Integer level, String parentPath, String nodeType,
                              boolean isArrayItem, String nodeValue) {
        String redisKey = RedisKeyConstant.ORIGINAL_DATA_JSON_PARSE.concat(apiCode).concat(":").concat(dataType.toString()).concat(":").concat(acceptType.toString())
                .concat(":").concat(level.toString());
        if (redisChgService.sismember(redisKey, nodeName)) {
            return;
        }
        //再查库
        MarketingJsonNodeParseExample jsonNodeParseExample = new MarketingJsonNodeParseExample();
        jsonNodeParseExample.createCriteria().andApiCodeEqualTo(apiCode).andDataTypeEqualTo(dataType).andAcceptTypeEqualTo(acceptType)
                .andParentPathEqualTo(parentPath).andNodeNameEqualTo(nodeName);
        List<MarketingJsonNodeParse> jsonNodeParseList = marketingJsonNodeParseMapper.selectByExample(jsonNodeParseExample);
        try {
            if (CollectionUtils.isEmpty(jsonNodeParseList)) {
                MarketingJsonNodeParse jsonNodeParse = new MarketingJsonNodeParse();
                jsonNodeParse.setApiCode(apiCode);
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
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DATACLEANING_SERVICEERROR.getCode(),
                    "数据清洗json解析入库异常" + e.getMessage()), e);

        }
    }


    @Override
    public Map<String, MarketingDataCleanGeneralRuleConfig> getConfigRule(String apiCode, Integer dataType, Integer acceptType) {
        String redisKey = RedisKeyConstant.DATA_CLEAN_CONFIG_RULE.concat(apiCode).concat(":").concat(dataType.toString()).concat(":").concat(acceptType.toString());
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
        List<MarketingDataCleanGeneralRuleConfig> ruleConfigList = marketingDataCleanGeneralRuleConfigMapper.getRuleConfigList(apiCode, dataType, acceptType);
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


    public Long delConfigRule(String apiCode, Integer dataType, Integer acceptType) {

        String redisKey = RedisKeyConstant.DATA_CLEAN_CONFIG_RULE.concat(apiCode).concat(":").concat(dataType.toString()).concat(":").concat(acceptType.toString());
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
        log.warn(TITLE + "apiCode={}清洗结束,耗时：{}ms", config.getApiCode(), System.currentTimeMillis() - start);
    }


    private void processData(MarketingCustomerOriginalData originalData, List<MarketingDataCleanGeneralRuleConfig> ruleConfigList) {
        try {
            JSONObject jsonData = JSON.parseObject(originalData.getJsonData());
            String apiCode = originalData.getApiCode();
            //层级字段处理
            String levelField = null;
            List<MarketingDataCleanGeneralRuleConfig> dataItemList = ruleConfigList.stream().filter(ruleConfig -> ruleConfig.getMappingField().equals("dataItems")).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(dataItemList)) {
                levelField = dataItemList.get(0).getCleanFields();
                ruleConfigList.removeIf(config -> config.getMappingField().equals("dataItems"));
            }
            List<JSONObject> jsonObjectList = new ArrayList<>();
            if (StringUtils.isNotEmpty(levelField)) {
                jsonObjectList = JsonParseUtils.parseJsonArrayByName(jsonData, levelField);
            } else {
                jsonObjectList.add(jsonData);
            }
            MarketingPreUserDTO marketingPreUserDTO = new MarketingPreUserDTO();
            //taskId清洗
            List<MarketingDataCleanGeneralRuleConfig> taskConfigList = ruleConfigList.stream().filter(ruleConfig -> ruleConfig.getMappingField().equals("taskId")).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(taskConfigList)) {
                marketingPreUserDTO.setTaskId(originalData.getApiCode().concat("_").concat(LocalDate.now().toString()));
            } else {
                marketingPreUserDTO.setTaskId((String) ruleCleaningService.executeCleaningRule(jsonData, taskConfigList.get(0)));
            }
            //requestId清洗
            List<MarketingDataCleanGeneralRuleConfig> requestIdConfigList = ruleConfigList.stream().filter(ruleConfig -> ruleConfig.getMappingField().equals("requestId")).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(requestIdConfigList)) {
                marketingPreUserDTO.setRequestId(originalData.getApiCode().concat("_").concat(LocalDate.now().toString()).concat(UUID.randomUUID().toString()));
            } else {
                marketingPreUserDTO.setRequestId((String) ruleCleaningService.executeCleaningRule(jsonData, requestIdConfigList.get(0)));
            }
            //剔除taskId，requestId
            ruleConfigList.removeIf(config -> config.getMappingField().equals("requestId") || config.getMappingField().equals("taskId"));
            //根据规则进行清洗处理
            List<MarketingPreUserDetailDTO> syncUsers = new ArrayList<>();
            jsonObjectList.forEach(jsonObject -> {
                MarketingPreUserDetailDTO marketingPreUserDetailDTO = new MarketingPreUserDetailDTO();
                //数据清洗
                dataCleanHandler(jsonObject, ruleConfigList, marketingPreUserDetailDTO);
                syncUsers.add(marketingPreUserDetailDTO);
            });
            //写入到info表
            marketingPreUserDTO.setDataItems(syncUsers);
            marketingPreUserDTO.setDataSourceType(DataSourceTypeEnum.ORIGINAL_INTERFACE.getCode());
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
            update.setId(originalData.getId());
            marketingCustomerOriginalDataMapper.updateByPrimaryKeySelective(update);
        } catch (Exception e) {
            log.error(TITLE + "清洗处理异常", e);
        }
    }

    @Override
    public void dataCleanHandler(JSONObject jsonObject, Collection<MarketingDataCleanGeneralRuleConfig> ruleConfigList, MarketingPreUserDetailDTO marketingPreUserDetailDTO) {
        ruleConfigList.forEach(ruleConfig -> {
            //数据清洗
            Object result = ruleCleaningService.executeCleaningRule(jsonObject, ruleConfig);
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
            pool.setCorePoolSize(threadNum);
            pool.setMaximumPoolSize(threadNum);
        }
        log.warn(TITLE + "处理线程数core={}，max={}", pool.getCorePoolSize(), pool.getMaximumPoolSize());
    }
}



