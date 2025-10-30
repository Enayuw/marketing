package com.br.marketing.service.rulecenter.impl.push;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailByRuleDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.dto.SyncOperateTypeDTO;
import com.br.marketing.entity.*;
import com.br.marketing.enums.clean.DataProcessEnum;
import com.br.marketing.mapper.MarketingJsonNodeParseMapper;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.MarketingSyncReportMapper;
import com.br.marketing.service.clean.common.DataCleanService;
import com.br.marketing.service.customertagsprocess.CustomerTagsProcessServiceImpl;
import com.br.marketing.service.customertagsprocess.vo.CustomerTagsVO;
import com.br.marketing.service.rulecenter.RuleCenterPushContext;
import com.br.marketing.service.rulecenter.enums.RuleCenterPushTargetEnum;
import com.br.marketing.util.EsConditionTransferSqlUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UploadRePushPolicyStrategy extends AbstractRuleCenterPushStrategy {

    @Autowired
    private MarketingSyncReportMapper syncReportMapper;

    @Autowired
    private MarketingJsonNodeParseMapper marketingJsonNodeParseMapper;


    @Autowired
    private MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Autowired
    private DataCleanService dataCleanService;

    @Resource
    CustomerTagsProcessServiceImpl customerTagsProcessService;

    private static final String TITLE = "[上传重推决策]";


    protected Result<Boolean> preProcess(RuleCenterPushContext context) {
        //数据清洗
        ThreadPoolExecutor cleanPool = context.getEsThreadPool();

        CustomerInfoPushMain pushMain = context.getCustomerInfoPushMain();
        String reportIds = pushMain.getUploadReportIds();
        String apiCode = pushMain.getmApiCode();
        String condition = getUploadDataCondition(pushMain.getmRuleCondition(), pushMain.getmApiCode());
        List<Long> listIds = Arrays.stream(reportIds.split(",")).map(Long::parseLong).collect(Collectors.toList());
        MarketingSyncReportExample syncReportExample = new MarketingSyncReportExample();
        syncReportExample.createCriteria().andIdIn(listIds);
        List<MarketingSyncReport> marketingSyncReports = syncReportMapper.selectByExample(syncReportExample);
        //通用调用,查询清洗规则配置
        Map<String, MarketingDataCleanGeneralRuleConfig> configRule = dataCleanService.getConfigRule(apiCode,
                DataProcessEnum.DataTypeEnum.UPLOAD.getCode(), DataProcessEnum.AcceptTypeEnum.GENERAL.getCode(), DataProcessEnum.RuleStatusEnum.PRE_SUCCESS.getCode());
        marketingSyncReports.forEach(syncreport -> {
            String appletDate = syncreport.getAppletDate();
            String userType = syncreport.getUserType();
            Date createTime = LocalDate.now().toString().equals(appletDate) ? pushMain.getCreateTime() : null;
            Long minId = null;
            while (true) {
                List<MarketingSyncUser> syncUsers = marketingSyncInfoMapper.getMarketingSyncByCondition(apiCode, null, appletDate, userType, createTime, condition, minId);
                if (CollectionUtils.isEmpty(syncUsers)) {
                    break;
                }
                minId = syncUsers.get(syncUsers.size() - 1).getId();
                List<List<MarketingSyncUser>> partitions = ListUtils.partition(syncUsers, 500);
                partitions.forEach(syncUserList -> {
                    cleanPool.submit(() -> cleanUploadData(syncUserList, configRule.values(), apiCode));
                });
            }
        });

        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue());

    }


    private Object cleanUploadData(List<MarketingSyncUser> syncUserList, Collection<MarketingDataCleanGeneralRuleConfig> ruleConfigList, String apiCode) {
        // 过滤并提取mappingField字段
        List<String> mappingFields = ruleConfigList.stream()
                .filter(ruleConfig -> ruleConfig.getIsMapping() && StringUtils.isNotEmpty(ruleConfig.getMappingRule()))
                .map(MarketingDataCleanGeneralRuleConfig::getMappingField)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(mappingFields) || CollectionUtils.isEmpty(syncUserList)) {
            return null;
        }
        // 构建批量更新的字段值映射列表
        List<Map<String, Object>> batchFieldValueMaps = new ArrayList<>();
        List<Long> updateIds = new ArrayList<>();

        for (MarketingSyncUser syncUser : syncUserList) {
            JSONObject jsonObject = (JSONObject) JSONObject.toJSON(syncUser);
            //清洗
            dataCleanService.uploadDetailCleanHandler(jsonObject, ruleConfigList, syncUser);
            Map<String, Object> fieldValueMap = buildFieldValueMap(syncUser, mappingFields);
            batchFieldValueMaps.add(fieldValueMap);
            updateIds.add(syncUser.getId());

        }
        // 如果有数据需要更新，构建批量更新SQL
        if (!batchFieldValueMaps.isEmpty()) {
            String sql = buildBatchUpdateSql(apiCode, updateIds, batchFieldValueMaps, mappingFields);
            log.warn("Generated Batch Update SQL: {}", sql);
            marketingSyncInfoMapper.updateRepeatUserStatus(sql);
        }
        return null;
    }

    /**
     * 构建批量更新SQL - 使用CASE WHEN
     */
    private String buildBatchUpdateSql(String apiCode, List<Long> updateIds,
                                       List<Map<String, Object>> batchFieldValueMaps,
                                       List<String> mappingFields) {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE b_marketing_sync_").append(apiCode).append(" SET ");
        // 为每个字段构建 CASE WHEN 语句
        List<String> setClauses = new ArrayList<>();
        for (String fieldName : mappingFields) {
            StringBuilder caseWhen = new StringBuilder();
            caseWhen.append(fieldName).append(" = CASE id ");
            // 为每条记录构建 WHEN 条件
            for (int i = 0; i < updateIds.size(); i++) {
                Long id = updateIds.get(i);
                Map<String, Object> fieldValueMap = batchFieldValueMaps.get(i);
                Object fieldValue = fieldValueMap.get(fieldName);

                if (fieldValue != null) {
                    caseWhen.append("WHEN ").append(id).append(" THEN ");
                    // 根据类型添加值
                    if (fieldValue instanceof String) {
                        String escapedValue = ((String) fieldValue).replace("'", "''");
                        caseWhen.append("'").append(escapedValue).append("' ");
                    } else {
                        caseWhen.append(fieldValue).append(" ");
                    }
                }
            }

            caseWhen.append("ELSE ").append(fieldName).append(" END");
            setClauses.add(caseWhen.toString());
        }

        sql.append(String.join(", ", setClauses));
        // 添加 WHERE 子句
        sql.append(" WHERE id IN (");
        sql.append(updateIds.stream().map(String::valueOf).collect(Collectors.joining(", ")));
        sql.append(")");
        return sql.toString();
    }

    /**
     * 构建字段值映射
     */
    private Map<String, Object> buildFieldValueMap(MarketingSyncUser syncUser, List<String> mappingFields) {
        Map<String, Object> fieldValueMap = new HashMap<>();
        for (String fieldName : mappingFields) {
            // 根据字段名获取对应的值（硬编码方式，性能更好）
            switch (fieldName.toLowerCase()) {
                case "cell":
                    fieldValueMap.put(fieldName, syncUser.getCell());
                    break;
                case "name":
                    fieldValueMap.put(fieldName, syncUser.getName());
                    break;
                case "id":
                    fieldValueMap.put(fieldName, syncUser.getIdCard());
                    break;
                case "custNum":
                    fieldValueMap.put(fieldName, syncUser.getCustNum());
                    break;
                case "operateType":
                    fieldValueMap.put(fieldName, syncUser.getOperateType());
                    break;
                case "userType":
                    fieldValueMap.put(fieldName, syncUser.getUserType());
                    break;
                default:
                    fieldValueMap.put("reserveField1", syncUser.getReserveField1());
                    break;
            }
        }
        return fieldValueMap;
    }


    protected Result<Boolean> doExecutePush(RuleCenterPushContext context) {
        // 推送决策
        ThreadPoolExecutor pushPool = context.getPushThreadPool();
        CustomerInfoPushMain pushMain = context.getCustomerInfoPushMain();
        String reportIds = pushMain.getUploadReportIds();
        String apiCode = pushMain.getmApiCode();
        String condition = getUploadDataCondition(pushMain.getmRuleCondition(), pushMain.getmApiCode());
        List<Long> listIds = Arrays.stream(reportIds.split(",")).map(Long::parseLong).collect(Collectors.toList());
        MarketingSyncReportExample syncReportExample = new MarketingSyncReportExample();
        syncReportExample.createCriteria().andIdIn(listIds);
        List<MarketingSyncReport> marketingSyncReports = syncReportMapper.selectByExample(syncReportExample);
        List<SyncOperateTypeDTO> operateTypeDTOList = syncReportMapper.selectOperateTypeGroup(apiCode, marketingSyncReports);
        //查询重推次数
        CustomerInfoPushMainExample pushMainExample = new CustomerInfoPushMainExample();
        pushMainExample.createCriteria()
                .andMApiCodeEqualTo(apiCode)
                .andPushTargetEqualTo(RuleCenterPushTargetEnum.UPLOAD_REPUSH_POLICY.getCode())
                .andCreateTimeGreaterThanOrEqualTo(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        Long rePushCount = customerInfoPushMainMapper.countByExample(pushMainExample) + 1;
        //处理重复的数据
        Set<String> custNumSet = handleOperateTypeFiveRepeat(pushPool, marketingSyncReports, condition, pushMain, rePushCount);
        //处理重复的数据
        Set<String> cellSet = handleOperateTypeSixRepeat(pushPool, marketingSyncReports, condition, pushMain, rePushCount);
        operateTypeDTOList.forEach(operateTypeDTO -> {
            String operateType = operateTypeDTO.getOperateType();
            if (StringUtils.isEmpty(operateType)) {
                log.error(TITLE + "存在operateType为空的数据");
                return;
            }
            // 根据不同的operateType添加额外条件
            switch (operateType) {
                case "3":
                    handleOperateTypeThree(pushPool, operateTypeDTO, condition, pushMain, rePushCount);
                    break;
                case "4":
                    handleOperateTypeFour(pushPool, operateTypeDTO, condition, pushMain, rePushCount);
                    break;
                case "5":
                    handleOperateTypeFive(pushPool, operateTypeDTO, condition, pushMain, rePushCount, custNumSet);
                    break;
                case "6":
                    handleOperateTypeSix(pushPool, operateTypeDTO, condition, pushMain, rePushCount, cellSet);
                    break;

                default:
                    log.warn("未知的 operateType: {}", operateType);
                    break;
            }
        });

        return null;
    }

    private Set<String> handleOperateTypeSixRepeat(ThreadPoolExecutor pushPool, List<MarketingSyncReport> marketingSyncReports, String condition, CustomerInfoPushMain pushMain, Long rePushCount) {
        String apiCode = pushMain.getmApiCode();
        List<MarketingSyncUser> repeatList = marketingSyncInfoMapper.getCustNumRepeatUserByCondition(apiCode, marketingSyncReports, condition, pushMain.getCreateTime());
        if (CollectionUtils.isEmpty(repeatList)) {
            return new HashSet<>();
        }
        CustomerTagsVO tags = customerTagsProcessService.getTags(apiCode);
        Integer jc3keyType = tags.getPushJc3keyType();
        // 按 custNum 分组
        Map<String, List<MarketingSyncUser>> groupedByCell = repeatList.stream().collect(Collectors.groupingBy(MarketingSyncUser::getCell));
        List<PushMarketingUserDetailByRuleDTO> pushList = new ArrayList<>();
        buildPushDataWithSequence(groupedByCell, apiCode, "6", pushList, jc3keyType, rePushCount);
        List<List<PushMarketingUserDetailByRuleDTO>> partitions = ListUtils.partition(pushList, 2000);
        partitions.forEach(psuhDetail -> {
            pushPool.submit(() -> uploadPushPolicy(psuhDetail));
        });
        return groupedByCell.keySet();
    }

    private Set<String> handleOperateTypeFiveRepeat(ThreadPoolExecutor pushPool, List<MarketingSyncReport> marketingSyncReports, String condition, CustomerInfoPushMain pushMain, Long rePushCount) {
        String apiCode = pushMain.getmApiCode();
        List<MarketingSyncUser> repeatList = marketingSyncInfoMapper.getCustNumRepeatUserByCondition(apiCode, marketingSyncReports, condition, pushMain.getCreateTime());
        if (CollectionUtils.isEmpty(repeatList)) {
            return new HashSet<>();
        }
        CustomerTagsVO tags = customerTagsProcessService.getTags(apiCode);
        Integer jc3keyType = tags.getPushJc3keyType();
        // 按 custNum 分组
        Map<String, List<MarketingSyncUser>> groupedByCustNum = repeatList.stream().collect(Collectors.groupingBy(MarketingSyncUser::getCustNum));
        List<PushMarketingUserDetailByRuleDTO> pushList = new ArrayList<>();
        buildPushDataWithSequence(groupedByCustNum, apiCode, "5", pushList, jc3keyType, rePushCount);
        List<List<PushMarketingUserDetailByRuleDTO>> partitions = ListUtils.partition(pushList, 2000);
        partitions.forEach(psuhDetail -> {
            pushPool.submit(() -> uploadPushPolicy(psuhDetail));
        });
        return groupedByCustNum.keySet();
    }

    private void handleOperateTypeSix(ThreadPoolExecutor pushPool, SyncOperateTypeDTO operateTypeDTO, String condition, CustomerInfoPushMain pushMain, Long rePushCount, Set<String> cellSet) {
        String apiCode = pushMain.getmApiCode();
        String operateType = operateTypeDTO.getOperateType();
        String appletDate = operateTypeDTO.getAppletDate();
        String userType = operateTypeDTO.getUserType();
        Date createTime = LocalDate.now().toString().equals(appletDate) ? pushMain.getCreateTime() : null;
        String nowDate = LocalDate.now().toString().replace("-", "");
        CustomerTagsVO tags = customerTagsProcessService.getTags(apiCode);
        Integer jc3keyType = tags.getPushJc3keyType();
        String buildBatchNumber = nowDate + "_" + apiCode + "_" + operateType + "_" + userType + "_" + 1 + "_RE_" + rePushCount;
        Long minId = null;
        while (true) {
            List<MarketingSyncUser> syncUsers = marketingSyncInfoMapper.getMarketingSyncByCondition(apiCode, operateType, appletDate, userType, createTime, condition, minId);
            if (CollectionUtils.isEmpty(syncUsers)) {
                break;
            }
            //过滤重复的
            syncUsers = syncUsers.stream().filter(syncUser -> cellSet.contains(syncUser.getCustNum())).collect(Collectors.toList());
            minId = syncUsers.get(syncUsers.size() - 1).getId();
            List<PushMarketingUserDetailByRuleDTO> pushList = new ArrayList<>();
            buildPushParam(pushList, syncUsers, buildBatchNumber, jc3keyType);
            pushPool.submit(() -> uploadPushPolicy(pushList));

        }
    }

    /**
     * 为重复的 custNum 数据构建递增序号的 batchNumber
     */
    private void buildPushDataWithSequence(Map<String, List<MarketingSyncUser>> groupdData,
                                           String apiCode,
                                           String operateType,
                                           List<PushMarketingUserDetailByRuleDTO> pushList, Integer jc3keyType, Long rePushCount) {

        groupdData.forEach((groupField, userList) -> {
            // 为每条记录分配序号
            for (int i = 0; i < userList.size(); i++) {
                MarketingSyncUser syncUser = userList.get(i);
                int sequence = i + 1;
                // 构建推送数据对象
                PushMarketingUserDetailByRuleDTO pushData = new PushMarketingUserDetailByRuleDTO();
                pushData.setInitId(syncUser.getId());
                pushData.setCaseNumber(syncUser.getCustNum());
                pushRuleService.judgeEncryptType(pushData, syncUser, jc3keyType);
                String nowDate = LocalDate.now().toString().replace("-", "");
                String reserveField1 = syncUser.getReserveField1();
                JSONObject jsonObject = JSONObject.parseObject(syncUser.getReserveField1());
                String userType = syncUser.getUserType();
                customizFieldMapping(apiCode, jsonObject);
                if (StringUtils.isNotBlank(reserveField1) && ObjectUtil.isNotEmpty(jsonObject)) {
                    String strategyCodeOriginal = ObjectUtil.isNotEmpty(jsonObject.getString("strategyCode"))
                            ? jsonObject.getString("strategyCode")
                            : "";
                    String strategyCode = strategyCodeOriginal.length() < 12
                            ? strategyCodeOriginal
                            : strategyCodeOriginal.substring(strategyCodeOriginal.length() - 12);
                    jsonObject.put("strategyCode", strategyCode);
                    String batchNumber = nowDate + "_" + apiCode + "_" + operateType + "_" + userType + "_" + sequence + "_RE_" + rePushCount;
                    String batchName = ObjectUtil.isNotEmpty(jsonObject.getString("batchName"))
                            ? jsonObject.getString("batchName")
                            : (nowDate + "_" + apiCode);
                    String strategyName = ObjectUtil.isNotEmpty(jsonObject.getString("strategyName"))
                            ? jsonObject.getString("strategyName")
                            : "";
                    if (StringUtils.isNotEmpty(strategyCode)) {
                        pushData.setStrategyCode(strategyCode);
                    } else {
                        pushData.setStrategyCode("");
                    }
                    if (StringUtils.isNotEmpty(strategyCode)) {
                        jsonObject.put("strategyName", strategyName);
                    } else {
                        jsonObject.put("strategyName", "");
                    }
                    pushData.setBatchNumber(batchNumber);
                    pushData.setBatchName(batchName);
                    jsonObject.put("batchName", batchName);
                    if (StringUtils.isNotEmpty(syncUser.getUserType())) {
                        jsonObject.put("userType", syncUser.getUserType());
                    }
                }
                if (ObjectUtil.isEmpty(jsonObject)) {
                    jsonObject = new JSONObject();
                }
                buildJson(jsonObject, syncUser, jc3keyType);
                pushData.setVariables(jsonObject);
                pushList.add(pushData);
            }
        });
    }

    private void handleOperateTypeFive(ThreadPoolExecutor pushPool, SyncOperateTypeDTO operateTypeDTO, String condition, CustomerInfoPushMain pushMain,
                                       Long rePushCount, Set<String> custNumSet) {
        String apiCode = pushMain.getmApiCode();
        String operateType = operateTypeDTO.getOperateType();
        String appletDate = operateTypeDTO.getAppletDate();
        String userType = operateTypeDTO.getUserType();
        Date createTime = LocalDate.now().toString().equals(appletDate) ? pushMain.getCreateTime() : null;
        String nowDate = LocalDate.now().toString().replace("-", "");
        CustomerTagsVO tags = customerTagsProcessService.getTags(apiCode);
        Integer jc3keyType = tags.getPushJc3keyType();
        String buildBatchNumber = nowDate + "_" + apiCode + "_" + operateType + "_" + userType + "_" + 1 + "_RE_" + rePushCount;
        Long minId = null;
        while (true) {
            List<MarketingSyncUser> syncUsers = marketingSyncInfoMapper.getMarketingSyncByCondition(apiCode, operateType, appletDate, userType, createTime, condition, minId);
            if (CollectionUtils.isEmpty(syncUsers)) {
                break;
            }
            //过滤重复的
            syncUsers = syncUsers.stream().filter(syncUser -> custNumSet.contains(syncUser.getCustNum())).collect(Collectors.toList());
            minId = syncUsers.get(syncUsers.size() - 1).getId();
            List<PushMarketingUserDetailByRuleDTO> pushList = new ArrayList<>();
            buildPushParam(pushList, syncUsers, buildBatchNumber, jc3keyType);
            pushPool.submit(() -> uploadPushPolicy(pushList));
        }

    }

    private void handleOperateTypeFour(ThreadPoolExecutor pushPool, SyncOperateTypeDTO operateTypeDTO, String condition, CustomerInfoPushMain pushMain, Long rePushCount) {
        String apiCode = pushMain.getmApiCode();
        String operateType = operateTypeDTO.getOperateType();
        String appletDate = operateTypeDTO.getAppletDate();
        String userType = operateTypeDTO.getUserType();
        Date createTime = LocalDate.now().toString().equals(appletDate) ? pushMain.getCreateTime() : null;
        CustomerTagsVO tags = customerTagsProcessService.getTags(apiCode);
        Integer jc3keyType = tags.getPushJc3keyType();
        String buildBatchNumber = LocalDate.now().toString().replace("-", "") + "_" + apiCode + "_" + userType + "_RE_" + rePushCount;
        Long minId = null;
        while (true) {
            List<MarketingSyncUser> syncUsers = marketingSyncInfoMapper.getMarketingSyncByCondition(apiCode, operateType, appletDate, userType, createTime, condition, minId);
            if (CollectionUtils.isEmpty(syncUsers)) {
                break;
            }
            minId = syncUsers.get(syncUsers.size() - 1).getId();
            List<PushMarketingUserDetailByRuleDTO> pushList = new ArrayList<>();
            buildPushParam(pushList, syncUsers, buildBatchNumber, jc3keyType);
            pushPool.submit(() -> uploadPushPolicy(pushList));
        }
    }

    private void handleOperateTypeThree(ThreadPoolExecutor pushPool, SyncOperateTypeDTO operateTypeDTO, String condition, CustomerInfoPushMain pushMain
            , Long rePushCount) {
        String apiCode = pushMain.getmApiCode();
        String operateType = operateTypeDTO.getOperateType();
        String appletDate = operateTypeDTO.getAppletDate();
        String userType = operateTypeDTO.getUserType();
        Date createTime = LocalDate.now().toString().equals(appletDate) ? pushMain.getCreateTime() : null;
        CustomerTagsVO tags = customerTagsProcessService.getTags(apiCode);
        Integer jc3keyType = tags.getPushJc3keyType();
        String buildBatchNumber = LocalDate.now().toString().replace("-", "") + "_" + apiCode + "_" + "RE_" + rePushCount;
        Long minId = null;
        while (true) {
            List<MarketingSyncUser> syncUsers = marketingSyncInfoMapper.getMarketingSyncByCondition(apiCode, operateType, appletDate, userType, createTime, condition, minId);
            if (CollectionUtils.isEmpty(syncUsers)) {
                break;
            }
            minId = syncUsers.get(syncUsers.size() - 1).getId();
            List<PushMarketingUserDetailByRuleDTO> pushList = new ArrayList<>();
            buildPushParam(pushList, syncUsers, buildBatchNumber, jc3keyType);
            pushPool.submit(() -> uploadPushPolicy(pushList));
        }
    }

    private void buildPushParam(List<PushMarketingUserDetailByRuleDTO> pushList, List<MarketingSyncUser> syncUsers, String buildBatchNumber, Integer jc3keyType) {
        syncUsers.forEach(syncUser -> {
            String apiCode = syncUser.getApiCode();
            PushMarketingUserDetailByRuleDTO pushData = new PushMarketingUserDetailByRuleDTO();
            pushData.setInitId(syncUser.getId());
            pushData.setCaseNumber(syncUser.getCustNum());
            pushRuleService.judgeEncryptType(pushData, syncUser, jc3keyType);
            String nowDate = LocalDate.now().toString().replace("-", "");
            String reserveField1 = syncUser.getReserveField1();
            JSONObject jsonObject = JSONObject.parseObject(syncUser.getReserveField1());
            customizFieldMapping(apiCode, jsonObject);
            if (StringUtils.isNotBlank(reserveField1) && ObjectUtil.isNotEmpty(jsonObject)) {
                String strategyCodeOriginal = ObjectUtil.isNotEmpty(jsonObject.getString("strategyCode"))
                        ? jsonObject.getString("strategyCode")
                        : "";
                String strategyCode = strategyCodeOriginal.length() < 12
                        ? strategyCodeOriginal
                        : strategyCodeOriginal.substring(strategyCodeOriginal.length() - 12);
                jsonObject.put("strategyCode", strategyCode);
                String batchNumber = ObjectUtil.isNotEmpty(jsonObject.getString("batchNumber"))
                        ? jsonObject.getString("batchNumber")
                        : buildBatchNumber;
                String batchName = ObjectUtil.isNotEmpty(jsonObject.getString("batchName"))
                        ? jsonObject.getString("batchName")
                        : (nowDate + "_" + apiCode);
                String strategyName = ObjectUtil.isNotEmpty(jsonObject.getString("strategyName"))
                        ? jsonObject.getString("strategyName")
                        : "";
                if (StringUtils.isNotEmpty(strategyCode)) {
                    pushData.setStrategyCode(strategyCode);
                } else {
                    pushData.setStrategyCode("");
                }
                if (StringUtils.isNotEmpty(strategyCode)) {
                    jsonObject.put("strategyName", strategyName);
                } else {
                    jsonObject.put("strategyName", "");
                }
                pushData.setBatchNumber(batchNumber);
                pushData.setBatchName(batchName);
                jsonObject.put("batchName", batchName);
                if (StringUtils.isNotEmpty(syncUser.getUserType())) {
                    jsonObject.put("userType", syncUser.getUserType());
                }
            }
            if (ObjectUtil.isEmpty(jsonObject)) {
                jsonObject = new JSONObject();
            }
            buildJson(jsonObject, syncUser, jc3keyType);
            pushData.setVariables(jsonObject);
            pushList.add(pushData);
        });
    }

    private Object uploadPushPolicy(List<PushMarketingUserDetailByRuleDTO> pushList) {

        return null;




    }


    @Override
    protected Callable<List<Future<Result<Integer>>>> createPushTask(RuleCenterPushContext context, Integer partitionIndex) {
        return null;
    }


    @Override
    protected Integer getSuccessStatus(CustomerInfoPushMain customerInfoPushMain) {
        return 0;
    }


    public String getUploadDataCondition(String ruleCondition, String apiCode) {
        //解析ruleCondition
        if (StringUtils.isEmpty(ruleCondition)) {
            return null;
        }
        JSONObject ruleConditionObject = JSON.parseObject(ruleCondition);
        String sqlCondition = EsConditionTransferSqlUtil.jsonTransferSql(ruleConditionObject, "");
        MarketingJsonNodeParseExample jsonNodeParseExample = new MarketingJsonNodeParseExample();
        jsonNodeParseExample.createCriteria().andApiCodeEqualTo(apiCode).andDataTypeEqualTo(DataProcessEnum.UPLOAD_DATA_GENERAL.getDataType()).
                andAcceptTypeEqualTo(DataProcessEnum.UPLOAD_DATA_GENERAL.getAcceptType())
                .andParentPathEqualTo("dataItems.item.reserveField1");
        List<MarketingJsonNodeParse> jsonNodeParseList = marketingJsonNodeParseMapper.selectByExample(jsonNodeParseExample);
        List<String> result = jsonNodeParseList.stream().map(MarketingJsonNodeParse::getNodeName).collect(Collectors.toList());

        // 遍历result中的字段名，将sqlCondition中匹配的字段替换为JSON提取表达式
        for (String nodeName : result) {
            // 使用正则表达式匹配
            String pattern = "\\b" + nodeName + "\\b";
            if (sqlCondition.matches(".*" + pattern + ".*")) {
                // 将字段名替换为 JSON_UNQUOTE(JSON_EXTRACT(reserve_field1, '$.字段名'))
                String jsonExtractExpr = String.format("JSON_UNQUOTE(JSON_EXTRACT(reserve_field1, '$.%s'))", nodeName);
                sqlCondition = sqlCondition.replaceAll(pattern, jsonExtractExpr);
            }
        }

        return sqlCondition;
    }


    private void customizFieldMapping(String apiCode, JSONObject jsonObject) {
        HashMap<String, JSONObject> fieldKeyMapping = marketingCommonConfig.getFieldKeyMapping();
        JSONObject mapping = fieldKeyMapping.get(apiCode);
        if (ObjectUtil.isNotEmpty(mapping)) {
            for (String s : mapping.keySet()) {
                String toKey = mapping.getString(s);
                String oldV = jsonObject.getString(toKey);
                String newV = jsonObject.getString(s);
                if (StringUtils.isBlank(oldV) && StringUtils.isNotBlank(newV)) {
                    jsonObject.put(toKey, newV);
                }
            }
        }
    }

    /**
     * 构建营销同步用户的JSON对象
     *
     * @param jsonObject 目标JSON对象
     * @param syncUser   营销同步用户数据
     * @param jc3keyType 加密类型(null表示未配置)
     * @return 构建好的JSON对象
     */
    private JSONObject buildJson(JSONObject jsonObject, MarketingSyncUser syncUser, Integer jc3keyType) {
        // 添加基础字段
        jsonObject.put("cusBatch", emptyDefault(syncUser.getCusBatch()));
        jsonObject.put("requestBatch", emptyDefault(syncUser.getRequestBatch()));
        jsonObject.put("custNum", emptyDefault(syncUser.getCustNum()));
        jsonObject.put("groupType", emptyDefault(syncUser.getGroupType()));
        jsonObject.put("operateType", emptyDefault(syncUser.getOperateType()));
        jsonObject.put("registerDate", emptyDefault(syncUser.getRegisterDate()));
        jsonObject.put("appletDate", emptyDefault(syncUser.getAppletDate()));
        jsonObject.put("taskId", emptyDefault(syncUser.getCusBatch()));

        // 处理敏感信息(姓名和身份证)
        pushRuleService.processSensitiveInfo(jsonObject, syncUser, jc3keyType);

        // 添加用户姓名
        cusNameOfJo(syncUser.getName(), jsonObject);

        return jsonObject;
    }

    private String emptyDefault(String value) {
        return com.br.common.util.StringUtils.isNotEmpty(value) ? value : "";
    }

    private void cusNameOfJo(String name, JSONObject jo) {
        if (StringUtils.isBlank(name)) {
            return;
        }
        if (ObjectUtil.isEmpty(jo)) {
            return;
        }
        String cusName = jo.getString("cusName");
        if (StringUtils.isBlank(cusName)) {
            jo.put("cusName", BrCipherMaker.getInstance().decode(name));
        }
    }

}
