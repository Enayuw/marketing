package com.br.marketing.service.clean.common.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.dataclean.mq.MqDataJsonParse;
import com.br.marketing.entity.MarketingDataCleanGeneralRuleConfig;
import com.br.marketing.entity.MarketingJsonNodeParse;
import com.br.marketing.entity.MarketingJsonNodeParseExample;
import com.br.marketing.enums.clean.DataProcessEnum;
import com.br.marketing.mapper.MarketingDataCleanGeneralRuleConfigMapper;
import com.br.marketing.mapper.MarketingJsonNodeParseMapper;
import com.br.marketing.service.clean.common.DataCleanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DataCleanServiceImpl implements DataCleanService {

    @Autowired
    private MarketingJsonNodeParseMapper marketingJsonNodeParseMapper;


    @Autowired
    private MarketingDataCleanGeneralRuleConfigMapper marketingDataCleanGeneralRuleConfigMapper;


    @Resource
    private RedisChgService redisChgService;

    @Override
    public Result<Boolean> customerDataJsonParse(String message) {
        Result<Boolean> result = new Result<>().setCode(ResultCode.SUCCESS.getValue());

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
                        "$",
                        jsonObject,
                        0,
                        false
                );

                result.setDate(true);
            } else {
                log.warn("数据ID: {} 的JSON数据为空", mqDataJsonParse.getDataId());
            }
        } catch (Exception e) {
            log.error("客户数据JSON结构解析异常 mq:{} 失败 -- ", message, e);
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
                newParentPath = parentPath + "." + nodeName;
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
                newParentPath = parentPath + "." + nodeName;
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
    }


    @Override
    public Map<String, String> getConfigRule(String apiCode, Integer dataType, Integer acceptType) {
        String redisKey = RedisKeyConstant.DATA_CLEAN_CONFIG_RULE.concat(apiCode).concat(":").concat(dataType.toString()).concat(":").concat(acceptType.toString());
        Map<String, Object> ruleMap = redisChgService.hgetall(redisKey);
        if (!CollectionUtils.isEmpty(ruleMap)) {
            Map<String, String> resultMap = new HashMap<>();
            ruleMap.forEach((key, value) -> {
                resultMap.put(key, value != null ? value.toString() : null);
            });
            return resultMap;
        }
        //查询数据库
        List<MarketingDataCleanGeneralRuleConfig> ruleConfigList = marketingDataCleanGeneralRuleConfigMapper.getRuleConfigList(apiCode, dataType, acceptType);
        if (CollectionUtils.isEmpty(ruleConfigList)) {
            return null;
        }
        Map<String, String> config = new HashMap<>();
        ruleConfigList.forEach(rule -> {
            config.put(rule.getMappingField(), rule.getMappingRule());

        });
        redisChgService.hmset(redisKey, config);
        return config;
    }

}
