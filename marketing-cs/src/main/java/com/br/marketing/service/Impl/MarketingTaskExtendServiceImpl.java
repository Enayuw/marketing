package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.enums.TaskTypeEnum;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingJsonNodeParseMapper;
import com.br.marketing.mapper.MarketingSyncReportMapper;
import com.br.marketing.mapper.MarketingTaskExtendMapper;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.service.MarketingTaskExtendService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.BaseHead;
import com.br.marketing.vo.BaseHeadConfigVO;
import com.br.marketing.vo.StrategyProductDetailVO;
import com.br.marketing.vo.TaskInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
public class MarketingTaskExtendServiceImpl implements MarketingTaskExtendService {

    @Resource
    MarketingTaskExtendMapper marketingTaskExtendMapper;

    @Resource
    StraHisFileMapper straHisFileMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private MarketingJsonNodeParseMapper marketingJsonNodeParseMapper;

    @Resource
    private MarketingSyncReportMapper syncReportMapper;

    @Override
    public MarketingTaskExtend getMarketingTaskExtend(Long taskId) {
        MarketingTaskExtendExample extendExample = new MarketingTaskExtendExample();
        extendExample.createCriteria().andIsDelEqualTo(Integer.valueOf(1)).andTaskIdEqualTo(taskId);
        List<MarketingTaskExtend> extendList = marketingTaskExtendMapper.selectByExample(extendExample);
        if(extendList.size()>0){
            return extendList.get(0);
        }
        return null;
    }

    @Override
    public Map<String, Set<String>> getProducts(String ids, Integer taskType) {
        Map<String, Set<String>> map = new HashMap<>();
        if(StringUtils.isEmpty(ids)){
            log.warn("入参缺少id");
            return map;
        }
        Set<String> baseHeadList = new HashSet<>();
        Set<String> fieldsList = new HashSet<>();
        // 数据准备：查询文件信息
        List<Long> taskIds = Arrays.stream(ids.split(",")).map(Long::valueOf).collect(Collectors.toList());

        // 根据 taskType 判断处理逻辑
        if (taskType != null && taskType == 1) {
            // 上传任务处理
            processUploadTask(taskIds, baseHeadList, fieldsList);
        } else {
            // 跑分任务处理
            processScoreTask(taskIds, baseHeadList, fieldsList);
        }
        
        map.put("showBaseHead", baseHeadList);
        map.put("fields", fieldsList);
        return map;
    }

    /**
     * 处理上传任务：根据 apiCode 查询 JSON 结构表获取字段
     * 根据 parentPath 是否包含 .reserveField1 判断字段放入 baseHeadList 还是 fieldsList
     *
     * @param taskIds  上传任务id
     * @param baseHeadList  基础字段集合
     * @param fieldsList    业务字段集合
     */
    private void processUploadTask(List<Long> taskIds, Set<String> baseHeadList, Set<String> fieldsList) {
        log.warn("上传任务处理，查询JSON结构表");

        // 提取 apiCode
        MarketingSyncReport marketingSyncReport = syncReportMapper.selectByPrimaryKey(taskIds.get(0));
        if (marketingSyncReport == null) {
            log.warn("未查询到上传数据记录，fileIds: {}", taskIds.get(0));
            return;
        }

        String apiCode = marketingSyncReport.getApiCode();
        log.warn("上传任务 apiCode: {}", apiCode);
        
        // 根据 apiCode 查询 b_marketing_json_node_parse 表（数据类型：0上传）
        MarketingJsonNodeParseExample jsonNodeParseExample = new MarketingJsonNodeParseExample();
        jsonNodeParseExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andDataTypeEqualTo(0);
        
        List<MarketingJsonNodeParse> jsonNodeParseList = marketingJsonNodeParseMapper.selectByExample(jsonNodeParseExample);

        if (CollectionUtils.isEmpty(jsonNodeParseList)) {
            log.warn("未从JSON结构表查询到字段信息，apiCode: {}", apiCode);
            return;
        }

        // 定义需要排除的字段
        Set<String> excludeFields = new HashSet<>(Arrays.asList("dataItems", "item", "reserveField1", "reserveField2"));
        
        // 提取节点名称作为字段，根据 parentPath 判断放入不同集合
        for (MarketingJsonNodeParse node : jsonNodeParseList) {
            String nodeName = node.getNodeName();
            String parentPath = node.getParentPath();

            if (StringUtils.hasText(nodeName)) {
                // 过滤掉特定的字段
                if (excludeFields.contains(nodeName)) {
                    continue;
                }
                
                // 判断 parentPath 是否包含 .reserveField1
                if (StringUtils.hasText(parentPath) && parentPath.contains(".reserveField1")) {
                    // 包含 .reserveField1，放入 fieldsList
                    fieldsList.add(nodeName);
                } else {
                    // 不包含 .reserveField1，放入 baseHeadList
                    baseHeadList.add(nodeName);
                }
            }
        }

    }

    /**
     * 处理跑分任务：查询任务扩展表获取字段
     *
     * @param taskIds  跑分记录id
     * @param baseHeadList  基础字段集合
     * @param fieldsList    业务字段集合
     */
    private void processScoreTask(List<Long> taskIds, Set<String> baseHeadList, Set<String> fieldsList) {
        log.warn("跑分任务处理，查询任务扩展表");

        StraHisFileExample straHisFileExample = new StraHisFileExample();
        straHisFileExample.createCriteria().andIdIn(taskIds);
        List<StraHisFile> straHisFiles = straHisFileMapper.selectByExample(straHisFileExample);
        Assert.notEmpty(straHisFiles, "没有匹配到文件信息");

        // 提取批次号和 apiCode
        List<String> batchNumbers = straHisFiles.stream()
                .map(StraHisFile::getBatchNumber)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
        Assert.notEmpty(batchNumbers, "没有匹配到批次号");
        
        List<String> apiCodes = straHisFiles.stream()
                .map(StraHisFile::getApiCode)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
        
        // 添加携程撞库基础字段
        if (!CollectionUtils.isEmpty(apiCodes)) {
            List<String> xieChengApiCodes = marketingCommonConfig.getXieChengCollidingDataProcessApiCodes();
            if (xieChengApiCodes.contains(apiCodes.get(0))) {
                baseHeadList.add("result");
                baseHeadList.add("release_time");
                baseHeadList.add("clean_time");
                baseHeadList.add("info");
                baseHeadList.add("blacklist_delete");
                baseHeadList.add("coupon_code");
                baseHeadList.add("coupon_desc");
                baseHeadList.add("customer_group");
            }
        }
        
        // 查询任务扩展信息
        List<TaskInfoVO> products = marketingTaskExtendMapper.getProducts(batchNumbers);
        
        // 判断是否为跑分任务，添加跑分基础字段
        boolean isScore = products.stream().anyMatch(t ->
                TaskTypeEnum.STRATYGYDATA.getValue().equals(t.getTaskType())
                        || TaskTypeEnum.PRODUCTDATA.getValue().equals(t.getTaskType()));
        
        if (isScore) {
            baseHeadList.add("request_time");
            baseHeadList.add("strategy_id");
            baseHeadList.add("cus_num");
        }
        
        // 解析产品配置，提取字段信息
        for (TaskInfoVO product : products) {
            String extendShowTitle = product.getExtendShowTitle();
            String strategyProductJson = product.getStrategyProductJson();
            
            // 解析策略产品 JSON，提取业务字段
            if (StringUtils.hasText(strategyProductJson)) {
                StrategyProductDetailVO strategyProductDetailVO = JSONObject.parseObject(strategyProductJson, StrategyProductDetailVO.class);
                List<String> fields = strategyProductDetailVO.getFields();
                if (!CollectionUtils.isEmpty(fields)) {
                    fieldsList.addAll(fields);
                }
            }
            
            // 解析扩展展示标题，提取基础字段
            if (StringUtils.hasText(extendShowTitle)) {
                BaseHeadConfigVO baseHeadConfigVO = JSONObject.parseObject(extendShowTitle, BaseHeadConfigVO.class);
                List<BaseHead> baseHead = baseHeadConfigVO.getBaseHead();
                if (!CollectionUtils.isEmpty(baseHead)) {
                    for (BaseHead single : baseHead) {
                        String convert = ifConvert(single.getName());
                        baseHeadList.add(convert);
                    }
                }
            }
        }
    }

    public String ifConvert(String s){
        String lowerCase = s.toLowerCase();
        //usertype->user_type,idcard->id_card,strategyId->strategy_id，taskid->task_id
        switch (lowerCase){
            case "usertype": return "user_type";
            case "idcard": return "id_card";
            case "id": return "id_card";
            case "strategyId": return "strategy_id";
            case "taskid": return "task_id";
            case "custnum": return "cus_num";
            default:return s;
        }
    }
}
