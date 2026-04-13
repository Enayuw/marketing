package com.br.marketing.service.tccpa.impl;

import com.alibaba.fastjson.JSONArray;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.*;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.config.biz.TcyrCpaConfigManager;
import com.br.marketing.dto.tccpa.*;
import com.br.marketing.dto.tc.TcCpaMagnitudeDistDTO;
import com.br.marketing.dto.tc.TcyrCpaCollidingDataPackageInfo;
import com.br.marketing.dto.tc.TcyrCpaDeleteRuleInfo;
import com.br.marketing.entity.*;
import com.br.marketing.enums.TcCpaCleanStatusEnum;
import com.br.marketing.enums.TcCpaCollidingTaskStatusEnum;
import com.br.marketing.mapper.*;
import com.br.marketing.service.tccpa.TcCpaCollidingRuleService;
import com.br.marketing.service.tccpa.TcCpaCommonService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
public class TcCpaCollidingRuleServiceImpl implements TcCpaCollidingRuleService {

    @Resource
    TcyrCpaCollidingDataPackageMapper tcyrCpaCollidingDataPackageMapper;

    @Resource
    TcyrCpaDeleteRuleMapper tcyrCpaDeleteRuleMapper;

    @Resource
    TcyrCpaLockDataMapper tcyrCpaLockDataMapper;

    @Resource
    TcyrCpaInvalueDataMapper tcyrCpaInvalueDataMapper;

    @Resource
    TcyrCpaCollidingTaskMapper tcyrCpaCollidingTaskMapper;

    @Resource
    private MarketingCustomerMapper marketingCustomerMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TcCpaCommonService tcCpaCommonService;

    @Resource
    TcyrCpaConfigManager tcyrCpaConfigManager;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Result<TcCpaCollidingRuleInfoDTO> info() {
        TcCpaCollidingRuleInfoDTO infoDTO = new TcCpaCollidingRuleInfoDTO();
        //1.查询有效的数据包
        TcyrCpaCollidingDataPackageExample dataPackageExample = new TcyrCpaCollidingDataPackageExample();
        dataPackageExample.createCriteria()
                .andIsDelEqualTo(Constants.DATA_VALID)
                .andEnabledEqualTo(Constants.ENABLED_ACT)
                .andCleanStatusEqualTo(TcCpaCleanStatusEnum.CLEAN_SUCCESS.getValue());
        List<TcyrCpaCollidingDataPackage> dataPackages = tcyrCpaCollidingDataPackageMapper.selectByExample(dataPackageExample);
        if(CollectionUtils.isNotEmpty(dataPackages)){
            List<TcyrCpaCollidingDataPackageInfo> dataPackageInfo = dataPackages.stream()
                    .map(dataPackage -> new TcyrCpaCollidingDataPackageInfo(dataPackage.getId(), dataPackage.getPackageName()))
                    .collect(Collectors.toList());
            infoDTO.setDataPackages(dataPackageInfo);
        }
        //2.查询有效的剔除规则
        TcyrCpaDeleteRuleExample deleteRuleExample = new TcyrCpaDeleteRuleExample();
        deleteRuleExample.createCriteria()
                .andIsDelEqualTo(Constants.DATA_VALID)
                .andEnabledEqualTo(Constants.ENABLED_ACT);
        List<TcyrCpaDeleteRule> deleteRules = tcyrCpaDeleteRuleMapper.selectByExample(deleteRuleExample);
        if(CollectionUtils.isNotEmpty(deleteRules)){
            List<TcyrCpaDeleteRuleInfo> deleteRuleInfo = deleteRules.stream()
                    .map(deleteRule -> new TcyrCpaDeleteRuleInfo(deleteRule.getId(), deleteRule.getRuleName()))
                    .collect(Collectors.toList());
            infoDTO.setDeleteRules(deleteRuleInfo);
        }
        //3.查询提取量级阈值和提取时间
        infoDTO.setExtraTime(marketingCommonConfig.getTcyrCpaPushFileVTConfig().getString("extraTime"));
        return new Result<TcCpaCollidingRuleInfoDTO>()
                .setCode(ResultCode.SUCCESS.getValue())
                .setDate(infoDTO);
    }

    @Override
    public Result<List<TcyrFailMsgSupplyGroupDTO>> magnitudeDist(String releaseTimes, Long taskId) {
        List<String> releaseTimeList = Arrays.asList(releaseTimes.split(","));
        if(CollectionUtils.isEmpty(releaseTimeList)){
            return null;
        }
        //1.读取需要补充的failMsg配置
        JSONArray supplyFailMsgs = marketingCommonConfig.getTcyrCpaPushFileVTConfig().getJSONArray("supplyFailMsgs");
        List<Integer> supplyFailMsgList = IntStream.range(0, supplyFailMsgs.size())
                .mapToObj(supplyFailMsgs::getJSONObject)
                .filter(obj -> obj != null && obj.containsKey("failMsg"))
                .map(obj -> obj.getInteger("failMsg"))
                .filter(value -> value != null)
                .collect(Collectors.toList());
        //2.查询量级
        List<TcyrCpaMagnitude> magnitudeList = getTcyrCpaMagnitudes(releaseTimeList, supplyFailMsgList);
        //3.补充量级为0的数据
        List<TcyrCpaMagnitude> allMagnitudeList = fillMissingData(magnitudeList, releaseTimeList, supplyFailMsgList);
        //4.查询上次勾选的格子
        List<String> isSupplyList = isSupplyData(taskId);
        //5.按releaseTime分组
        List<TcyrFailMsgSupplyGroupDTO> result = groupByDate(allMagnitudeList, isSupplyList);
        return new Result<List<TcCpaMagnitudeDistDTO>>()
                .setCode(ResultCode.SUCCESS.getValue())
                .setDate(result);
    }

    @Override
    public Result rule(TcCpaCollidingRuleDTO ruleDTO) {
        //1.创建基础任务，设置共性的属性
        List<String> packageIds = ruleDTO.getPackageIds();
        List<Long> packageIdStrs = packageIds.stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());
        String packageNames = tcyrCpaCollidingDataPackageMapper.queryPackageNamesByIds(packageIdStrs);
        TcyrCpaCollidingTask basicTask = new TcyrCpaCollidingTask();
        basicTask.setApiCode(marketingCommonConfig.getTcyrCpaApiCode());
        basicTask.setPackageIds(String.join(",", packageIds));
        basicTask.setPackageNames(packageNames);
        basicTask.setDeleteRuleIds(String.join(",", ruleDTO.getDeleteRuleIds()));
        basicTask.setCollidingNum(ruleDTO.getCollidingNum());
        //1.1创建补包信息
        if (CollectionUtils.isNotEmpty(ruleDTO.getFailMsgSupplyGroups())) {
            //过滤掉isSupply=false的数据
            List<TcyrFailMsgSupplyGroupDTO> supplyGroupDTOS = filterByIsSupply(ruleDTO.getFailMsgSupplyGroups());
            if (CollectionUtils.isNotEmpty(supplyGroupDTOS)) {
                JSONArray supplyFailMsgs = marketingCommonConfig.getTcyrCpaPushFileVTConfig().getJSONArray("supplyFailMsgs");
                Map<Integer, Integer> failMsgToPriority = parseWithStream(supplyFailMsgs);
                //构造数据
                List<TcyrSupplyRuleInfo> supplyRuleInfos = generateGroupedSupplyRules(supplyGroupDTOS, failMsgToPriority);
                basicTask.setSupplyRuleInfo(JsonParseUtils.toJson(supplyRuleInfos));
            }
        }
        //2.遍历撞库日期，插入【b_tcyr_cpa_colliding_task】
        for (String collidingDate : ruleDTO.getCollidingDates()) {
            TcyrCpaCollidingTask task = new TcyrCpaCollidingTask();
            BeanUtils.copyProperties(basicTask, task);
            task.setCollidingDate(DateHelper.parseDate(collidingDate));
            task.setCollidingTime(DateHelper.parseDate(collidingDate + " " + ruleDTO.getCollidingTime()));
            tcyrCpaCollidingTaskMapper.insertSelective(task);
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public PageResultReturn list(TcCpaCollidingRuleQueryDTO dto) {
        PageHelper.startPage(dto.getCurrent(), dto.getSize());
        List<TcCpaCollidingTaskDTO> dtos = new ArrayList<>();
        //1.数据查询
        List<TcyrCpaCollidingTask> taskList = tcyrCpaCollidingTaskMapper.queryTaskListbyPage(
                dto.getPackageName(),
                dto.getEnabled(),
                dto.getCollidingDateBegin(),
                dto.getCollidingDateEnd());
        if (CollectionUtils.isEmpty(taskList)) {
            return PageResultReturn.setPageResult(dtos, dto.getCurrent(), dto.getSize());
        }
        //2.获取MarketingCustomer
        Map<String, MarketingCustomer> customers = getCustomer(taskList);
        //3.封装数据
        for (TcyrCpaCollidingTask task : taskList) {
            TcCpaCollidingTaskDTO taskDTO = new TcCpaCollidingTaskDTO();
            dtos.add(taskDTO);
            //基础字段
            BeanUtils.copyProperties(task, taskDTO);
            //转换字段
            MarketingCustomer customer = customers.get(task.getApiCode());
            taskDTO.setCid(customer.getCid());
            taskDTO.setCustomerName(customer.getShortName());
            List<String> packageIds = Arrays.stream(task.getPackageIds().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            taskDTO.setPackageIds(packageIds);
            taskDTO.setCollidingDate(DateHelper.formatDate(task.getCollidingDate()));
            taskDTO.setCollidingTime(DateHelper.formatHMS(task.getCollidingTime()));
            List<String> deleteRuleIds = Arrays.stream(task.getDeleteRuleIds().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            taskDTO.setDeleteRuleIds(deleteRuleIds);
            if (StringUtils.isNotEmpty(task.getSupplyRuleInfo())) {
                List<TcyrSupplyRuleInfo> supplyRuleInfos;
                Set<String> releaseTimeSet = new HashSet<>();
                try {
                    supplyRuleInfos = objectMapper.readValue(task.getSupplyRuleInfo(),
                            new TypeReference<List<TcyrSupplyRuleInfo>>() {
                            });
                } catch (IOException e) {
                    log.error("同程cpa-supplyRuleInfos转化异常," + e.getMessage());
                    throw new RuntimeException(e);
                }
                for (TcyrSupplyRuleInfo supplyRuleInfo : supplyRuleInfos) {
                    releaseTimeSet.addAll(supplyRuleInfo.getReleaseTimes());
                }
                taskDTO.setReleaseTimes(String.join(",", releaseTimeSet));
            }
        }
        PageInfo<TcyrCpaCollidingTask> pageInfo = new PageInfo<>(taskList);
        return PageResultReturn.setPageResult(dtos, dto.getCurrent(), dto.getSize(), pageInfo.getTotal());
    }

    @Override
    public Result update(TcCpaCollidingRuleDTO ruleDTO) {
        //1.查询原数据并校验
        TcyrCpaCollidingTask task = tcyrCpaCollidingTaskMapper.selectByPrimaryKey(ruleDTO.getTaskId());
        if(task == null){
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("未查到对应撞库任务，请联系开发人员！");
        }
        if (task.getEnabled() == Constants.ENABLED_ACT) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("启用状态的撞库任务不可修改！");
        }
        if (task.getStatus() > TcCpaCollidingTaskStatusEnum.STATUS_STA_COMPLETED.getValue()) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("该撞库任务已进入推送流程，不可修改！");
        }
        //2.赋值基础字段
        List<String> packageIds = ruleDTO.getPackageIds();
        List<Long> packageIdStrs = packageIds.stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());
        String packageNames = tcyrCpaCollidingDataPackageMapper.queryPackageNamesByIds(packageIdStrs);
        String collidingDate = DateHelper.formatDate(task.getCollidingDate());
        task.setPackageIds(String.join(",", packageIds));
        task.setPackageNames(packageNames);
        task.setDeleteRuleIds(String.join(",", ruleDTO.getDeleteRuleIds()));
        task.setCollidingTime(DateHelper.parseDate(collidingDate + " " + ruleDTO.getCollidingTime()));
        task.setStatus(TcCpaCollidingTaskStatusEnum.STATUS_WAIT_STA.getValue());
        task.setEstNum(null);
        task.setSupplyNum(null);
        task.setDeleteNum(null);
        task.setCollidingNum(ruleDTO.getCollidingNum());
        //todo 更新时需要更新量级不
        //3.赋值补包字段
        if (CollectionUtils.isEmpty(ruleDTO.getFailMsgSupplyGroups())) {
            task.setSupplyRuleInfo(null);
        } else {
            //过滤掉isSupply=false的数据
            List<TcyrFailMsgSupplyGroupDTO> supplyGroupDTOS = filterByIsSupply(ruleDTO.getFailMsgSupplyGroups());
            if (CollectionUtils.isNotEmpty(supplyGroupDTOS)) {
                JSONArray supplyFailMsgs = marketingCommonConfig.getTcyrCpaPushFileVTConfig().getJSONArray("supplyFailMsgs");
                Map<Integer, Integer> failMsgToPriority = parseWithStream(supplyFailMsgs);
                //构造数据
                List<TcyrSupplyRuleInfo> supplyRuleInfos = generateGroupedSupplyRules(supplyGroupDTOS, failMsgToPriority);
                task.setSupplyRuleInfo(JsonParseUtils.toJson(supplyRuleInfos));
            }
        }
        tcyrCpaCollidingTaskMapper.updateByPrimaryKeySelective(task);
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public Result enable(Long taskId, Integer enabled) {
        TcyrCpaCollidingTask task = new TcyrCpaCollidingTask();
        task.setId(taskId);
        task.setEnabled(enabled);
        tcyrCpaCollidingTaskMapper.updateByPrimaryKeySelective(task);
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    /**
     * 查询上次勾选的格子
     * @param taskId
     */
    private List<String> isSupplyData(Long taskId) {
        if (taskId == null) {
            return null;
        }
        String supplyRuleInfo = tcyrCpaCollidingTaskMapper.querysupplyRuleInfo(taskId);
        if (StringUtils.isEmpty(supplyRuleInfo)) {
            return null;
        }
        List<TcyrSupplyRuleInfo> supplyRuleInfos;
        List<String> isSupplyList = new ArrayList<>();
        try {
            supplyRuleInfos = objectMapper.readValue(supplyRuleInfo,
                    new TypeReference<List<TcyrSupplyRuleInfo>>() {
                    });
        } catch (IOException e) {
            log.error("同程cpa-supplyRuleInfos转化异常," + e.getMessage());
            throw new RuntimeException(e);
        }
        for (TcyrSupplyRuleInfo ruleInfo : supplyRuleInfos) {
            for (String releaseTime : ruleInfo.getReleaseTimes()) {
                isSupplyList.add(releaseTime + "-" + ruleInfo.getFailMsg());
            }
        }
        return isSupplyList;

    }

    private Map<String, MarketingCustomer> getCustomer(List<TcyrCpaCollidingTask> taskList) {
        List<String> apiCodes = taskList.stream().map(TcyrCpaCollidingTask::getApiCode).collect(Collectors.toList());
        MarketingCustomerExample customerExample = new MarketingCustomerExample();
        customerExample.createCriteria().andApiCodeIn(apiCodes);
        Map<String, MarketingCustomer> customers = marketingCustomerMapper.selectByExample(customerExample)
                .stream().collect(Collectors.toMap(MarketingCustomer::getApiCode, customer -> customer));
        return customers;
    }

    /**
     * @description 查询量级
     * @param releaseTimeList
     * @param supplyFailMsgList
     * @return java.util.List<com.br.marketing.entity.TcyrCpaMagnitude>
     * @author hedongshuo
     * @date 2025/12/3 20:34
     **/
    private List<TcyrCpaMagnitude> getTcyrCpaMagnitudes(List<String> releaseTimeList, List<Integer> supplyFailMsgList) {
        List<TcyrCpaMagnitude> magnitudeList = new ArrayList<>();
        //failMsg与lockBelong的映射Map
        Map<Integer, Integer> failMsgToLbMap = tcyrCpaConfigManager.getFailMsgToBlMap();
        for (Integer failMsg : supplyFailMsgList) {
            Integer lockBelong = failMsgToLbMap.get(failMsg);
            List<TcyrCpaMagnitude> magnitudeInnerList;
            if (lockBelong != null) {
                //查询【b_tcyr_cpa_lock_data】
                magnitudeInnerList = tcyrCpaLockDataMapper
                        .queryMagnitudeWithBelong(releaseTimeList, failMsg, lockBelong);
            } else {
                //查询【b_tcyr_cpa_invalue_data】
                magnitudeInnerList = tcyrCpaInvalueDataMapper
                        .queryMagnitudeWithFailMsg(releaseTimeList, failMsg.toString());
            }
            magnitudeList.addAll(magnitudeInnerList);
        }
        return magnitudeList;
    }

    /**
     * 补全量级为0的数据
     * @param existingData
     * @param releaseTimeList
     * @param supplyFailMsgList
     */
    private List<TcyrCpaMagnitude> fillMissingData(List<TcyrCpaMagnitude> existingData,
                                 List<String> releaseTimeList,
                                 List<Integer> supplyFailMsgList) {
        if (CollectionUtils.isEmpty(existingData)) {
            existingData = new ArrayList<>();
        }
        Map<String, TcyrCpaMagnitude> existingDataMap = new HashMap<>();
        //1.创建存在数据的Map<releaseTime-failMsg, TcyrCpaMagnitude>
        for (TcyrCpaMagnitude item : existingData) {
            String key = item.getReleaseTime() + "-" + item.getFailMsg();
            existingDataMap.put(key, item);
        }
        //2.创建结果列表
        List<TcyrCpaMagnitude> result = new ArrayList<>();
        for (String releaseTime : releaseTimeList) {
            for (Integer failMsg : supplyFailMsgList) {
                String key = releaseTime + "-" + failMsg;
                if (existingDataMap.containsKey(key)) {
                    result.add(existingDataMap.get(key));
                } else {
                    TcyrCpaMagnitude missingItem = new TcyrCpaMagnitude();
                    missingItem.setReleaseTime(releaseTime);
                    missingItem.setFailMsg(failMsg);
                    missingItem.setMagnitude(0L);
                    result.add(missingItem);
                }
            }
        }
        //3.按日期和failMsg排序
        result.sort(Comparator
                .comparing(TcyrCpaMagnitude::getReleaseTime)
                .thenComparing(TcyrCpaMagnitude::getFailMsg));
        return result;
    }

    /**
     * 只保留isSupply=true的数据
     * @param failMsgSupplyGroups
     */
    private List<TcyrFailMsgSupplyGroupDTO> filterByIsSupply(List<TcyrFailMsgSupplyGroupDTO> failMsgSupplyGroups) {
        return failMsgSupplyGroups.stream()
                .filter(group -> group != null && StringUtils.isNotBlank(group.getReleaseTime()))
                .map(group -> {
                    TcyrFailMsgSupplyGroupDTO filteredGroup = new TcyrFailMsgSupplyGroupDTO();
                    filteredGroup.setReleaseTime(group.getReleaseTime());
                    //过滤supplyInfo，只保留isSupply为true的数据
                    List<TcyrFailMsgSupplyDTO> filteredSupplyInfo =
                            Optional.ofNullable(group.getSupplyInfo())
                                    .orElse(Collections.emptyList())
                                    .stream()
                                    .filter(supply -> supply != null && supply.isSupply())  // 注意：isSupply()方法
                                    .collect(Collectors.toList());
                    filteredGroup.setSupplyInfo(filteredSupplyInfo);
                    return filteredGroup;
                })
                .filter(group -> CollectionUtils.isNotEmpty(group.getSupplyInfo()))  // 过滤掉supplyInfo为空的组
                .collect(Collectors.toList());
    }

    /**
     * 将List<TcyrCpaMagnitude>转换为按日期分组的结果
     */
    public List<TcyrFailMsgSupplyGroupDTO> groupByDate(List<TcyrCpaMagnitude> magnitudeList, List<String> isSupplyList) {
        if (CollectionUtils.isEmpty(magnitudeList)) {
            return Collections.emptyList();
        }
        //1.按日期分组
        Map<String, List<TcyrCpaMagnitude>> groupByDate = magnitudeList.stream()
                .filter(item -> item.getReleaseTime() != null)
                .collect(Collectors.groupingBy(
                        TcyrCpaMagnitude::getReleaseTime,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
        //2.转换为目标结构
        return groupByDate.entrySet().stream()
                .map(entry -> {
                    TcyrFailMsgSupplyGroupDTO result = new TcyrFailMsgSupplyGroupDTO();
                    result.setReleaseTime(entry.getKey());
                    // 将每个日期下的数据转换为TcyrFailMsgSupplyDTO
                    List<TcyrFailMsgSupplyDTO> supplyInfoList = entry.getValue().stream()
                            .filter(item -> item.getFailMsg() != null)
                            .map(item -> new TcyrFailMsgSupplyDTO(
                                    item.getFailMsg(),
                                    item.getMagnitude() != null ? item.getMagnitude() : 0L,
                                    isSupply(isSupplyList, entry.getKey() + "-" + item.getFailMsg())
                            ))
                            .sorted(Comparator.comparing(TcyrFailMsgSupplyDTO::getFailMsg))
                            .collect(Collectors.toList());
                    result.setSupplyInfo(supplyInfoList);
                    return result;
                })
                .sorted(Comparator.comparing(TcyrFailMsgSupplyGroupDTO::getReleaseTime))
                .collect(Collectors.toList());
    }

    private boolean isSupply(List<String> isSupplyList, String key) {
        if(CollectionUtils.isEmpty(isSupplyList)) {
            return false;
        }
        return isSupplyList.contains(key);
    }

    /**
     * 将配置supplyFailMsgs转成Map<failMsg,priority>
     * @param supplyFailMsgs
     * @return
     */
    public Map<Integer, Integer> parseWithStream(JSONArray supplyFailMsgs) {
        return IntStream.range(0, supplyFailMsgs.size())
                .mapToObj(supplyFailMsgs::getJSONObject)
                .filter(obj -> obj != null
                        && obj.getInteger("failMsg") != null
                        && obj.getInteger("priority") != null)
                .collect(Collectors.toMap(
                        obj -> obj.getInteger("failMsg"),
                        obj -> obj.getInteger("priority"),
                        (priority1, priority2) -> {
                            return priority1;
                        }
                ));
    }

    /**
     * 将数据按failMsg分组，合并日期
     */
    public List<TcyrSupplyRuleInfo> generateGroupedSupplyRules(
            List<TcyrFailMsgSupplyGroupDTO> supplyGroupDTOS,
            Map<Integer, Integer> failMsgToPriority) {
        //1.按failMsg分组收集数据到groupByFailMsg
        Map<Integer, SupplyGroupData> groupByFailMsg = new HashMap<>();
        for (TcyrFailMsgSupplyGroupDTO group : supplyGroupDTOS) {
            if (group == null || StringUtils.isBlank(group.getReleaseTime())
                    || CollectionUtils.isEmpty(group.getSupplyInfo())) {
                continue;
            }
            String releaseTime = group.getReleaseTime();
            for (TcyrFailMsgSupplyDTO supply : group.getSupplyInfo()) {
                if (supply == null || supply.getFailMsg() == null || supply.getMagnitude() == null) {
                    continue;
                }
                int failMsg = supply.getFailMsg();
                SupplyGroupData groupData = groupByFailMsg.computeIfAbsent(failMsg,
                        k -> new SupplyGroupData(failMsg));
                // 添加日期和数量
                groupData.addDate(releaseTime);
            }
        }
        //failMsg与lockBelong的映射Map
        Map<Integer, Integer> failMsgToLbMap = tcyrCpaConfigManager.getFailMsgToBlMap();
        //2.将数据转换为TcyrSupplyRuleInfo
        List<TcyrSupplyRuleInfo> result = new ArrayList<>();
        for (Map.Entry<Integer, SupplyGroupData> entry : groupByFailMsg.entrySet()) {
            SupplyGroupData groupData = entry.getValue();
            Integer priority = failMsgToPriority.get(groupData.getFailMsg());
            TcyrSupplyRuleInfo ruleInfo = new TcyrSupplyRuleInfo();
            ruleInfo.setPriority(priority != null ? (100 + priority) : 99);
            ruleInfo.setReleaseTimes(groupData.getDates());
            ruleInfo.setFailMsg(groupData.getFailMsg());
            String supplyScript = generateDynamicSql(
                    groupData.getFailMsg(), failMsgToLbMap.get(groupData.getFailMsg()), groupData.getDates());
            ruleInfo.setSupplyScript(supplyScript);
            result.add(ruleInfo);
        }
        //3.按priority排序
        result.sort(Comparator.comparing(TcyrSupplyRuleInfo::getPriority));
        return result;
    }

    /**
     * 生成动态SQL
     */
    private String generateDynamicSql(Integer failMsg, Integer lockBelong, List<String> dates) {
        //1.日期
        String dateConditions = dates.stream()
                .map(date -> "'" + date + "'")
                .collect(Collectors.joining(","));
        if (lockBelong == null) {
            return String.format(
                    "select user_key from b_tcyr_cpa_invalue_data " +
                            "where fail_msg = %d " +
                            "and date(release_time) in (%s)",
                    failMsg, dateConditions
            );
        } else {
            return String.format(
                    "select user_key from b_tcyr_cpa_lock_data " +
                            "where lock_belong = %d " +
                            "and date(release_time) in (%s)",
                    lockBelong, dateConditions
            );
        }
    }

    /**
     * 分组数据内部类
     */
    @Data
    private static class SupplyGroupData {
        private Integer failMsg;
        private List<String> dates = new ArrayList<>();

        public SupplyGroupData(Integer failMsg) {
            this.failMsg = failMsg;
        }

        public void addDate(String date) {
            if (!dates.contains(date)) {
                dates.add(date);
            }
        }
    }
}
