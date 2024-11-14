package com.br.marketing.service.Impl.datagroup;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.dto.datagroup.DataGroupConfgDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.service.SoleStrategyService;
import com.br.marketing.service.datagroup.DataGroupHandlerService;
import com.br.marketing.mapper.datagroup.DataGroupConfigMapper;
import com.br.marketing.mapper.datagroup.DataGroupTaskMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.BaseHead;
import com.br.marketing.vo.BaseHeadConfigVO;
import com.br.marketing.vo.MarketingTaskVO;
import com.br.marketing.vo.datagroup.DataGropRuleVO;
import com.br.marketing.vo.datagroup.DataGroupConfigVO;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description 数据分组service实现
 * @Author zhen.Li1
 * @CreateTime 2024/11/07
 */
@Service
@Slf4j
public class DataGroupHandlerServiceImpl implements DataGroupHandlerService {

    @Autowired
    private DataGroupConfigMapper dataGroupConfigMapper;

    @Resource
    private MarketingCustomerMapper marketingCustomerMapper;

    @Autowired
    private DataGroupTaskMapper dataGroupTaskMapper;

    @Resource
    private RedisChgService redisChgService;

    @Resource
    private MarketingSyncReportMapper syncReportMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;


    @Resource
    private CustomerRuleMapper customerRuleMapper;

    @Resource
    private ScoreRuleConfigMapper scoreRuleConfigMapper;


    @Resource
    MarketingTaskMapper marketingTaskMapper;

    @Autowired
    SoleStrategyService soleStrategyService;


    /**
     * 获取分组配置
     *
     * @param ids 上传记录id集合
     * @return List<DataGroupConfigVO>
     */
    @Override
    public List<DataGroupConfigVO> configList(String ids, String apiCode) {
        List<String> idList = Arrays.asList(ids.split(","));
        Collections.sort(idList);
        List<DataGroupConfigVO> dataGroupConfigList;
        if (idList.size() > 1) {

            dataGroupConfigList = dataGroupConfigMapper.selectGroupConfigList(String.join(",", idList), "1", apiCode);
        } else {
            dataGroupConfigList = dataGroupConfigMapper.selectGroupConfigList(ids, "2", apiCode);

        }
        return dataGroupConfigList;
    }

    @Override
    public ApiResult updateConfig(DataGroupConfgDTO dto) {
        List<String> idList = Arrays.asList(dto.getIds().split(","));
        Collections.sort(idList);
        List<DataGropRuleVO> gropRuleVOList = JSON.parseObject(dto.getGroupRules(), new TypeReference<List<DataGropRuleVO>>() {
        }.getType());
        Map<String, List<DataGropRuleVO>> gropRuleMap = gropRuleVOList.stream().collect(Collectors.groupingBy(DataGropRuleVO::getGroupField));
        String redisKey = RedisKeyConstant.DATA_GROUP_TASK_LOCK.concat(dto.getId().toString());
        String s = UUID.randomUUID().toString();
        try {
            //处理与定时任务执行时的并发操作
            redisChgService.lock(redisKey, s);
            DataGroupTaskExample dataGroupTaskExample = new DataGroupTaskExample();
            dataGroupTaskExample.createCriteria().andApiCodeEqualTo(dto.getApiCode()).andConfigIdEqualTo(dto.getId()).andOperTypeEqualTo(0);
            List<DataGroupTask> groupTaskList = dataGroupTaskMapper.selectByExample(dataGroupTaskExample);
            List<DataGroupTask> runingTask = groupTaskList.stream().filter(task -> task.getStatus() != 0).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(runingTask)) {
                return new ApiResult().fail("分组任务已开始执行，无法进行编辑");
            }
            groupTaskList.forEach((DataGroupTask groupTask) -> {
                groupTask.setGroupRule(JSON.toJSONString(gropRuleMap.get(groupTask.getGroupFiled()).get(0)));
                dataGroupTaskMapper.updateByPrimaryKeySelective(groupTask);
            });
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "分组任务编辑异常"), e);
        } finally {
            redisChgService.unlock(redisKey, s);
        }
        //更新配置
        DataGroupConfig config = dataGroupConfigMapper.selectByPrimaryKey(dto.getId());
        DataGropRuleVO update = gropRuleVOList.get(0);
        List<DataGropRuleVO> groupRule = JSON.parseObject(config.getGroupRules(), new TypeReference<List<DataGropRuleVO>>() {
        }.getType());
        groupRule.removeIf((DataGropRuleVO rule) -> rule.getGroupField().equals(update.getGroupField()));
        groupRule.add(update);
        config.setGroupRules(JSON.toJSONString(groupRule));
        dataGroupConfigMapper.updateByPrimaryKeySelective(config);
        return new ApiResult<Long>().success(dto.getId());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult addOrDeleteConfig(DataGroupConfgDTO dto) {
        List<String> idList = Arrays.asList(dto.getIds().split(","));
        Collections.sort(idList);
        MarketingSyncReportExample reportExample = new MarketingSyncReportExample();
        reportExample.createCriteria().andIdIn(Arrays.stream(dto.getIds().split(",")).map(Long::parseLong).collect(Collectors.toList()));
        List<MarketingSyncReport> reportList = syncReportMapper.selectByExample(reportExample);
        String reportId = String.join(",", idList);
        String ruleJson = dto.getGroupRules();
        String operType = dto.getOperType();
        List<DataGropRuleVO> gropRuleVOList = JSON.parseObject(ruleJson, new TypeReference<List<DataGropRuleVO>>() {
        }.getType());
        Map<String, List<DataGropRuleVO>> gropRuleMap = gropRuleVOList.stream().collect(Collectors.groupingBy(DataGropRuleVO::getGroupField));
        DataGroupConfigExample dataGroupConfigExample = new DataGroupConfigExample();
        DataGroupConfigExample.Criteria criteria = dataGroupConfigExample.createCriteria();
        criteria.andApiCodeEqualTo(dto.getApiCode()).andUploadReportIdEqualTo(reportId).andIsDelEqualTo(1);
        List<DataGroupConfig> groupConfigList = dataGroupConfigMapper.selectByExample(dataGroupConfigExample);
        Long configId;
        if (CollectionUtils.isEmpty(groupConfigList)) {
            DataGroupConfig dataGroupConfig = new DataGroupConfig();
            dataGroupConfig.setApiCode(dto.getApiCode());
            dataGroupConfig.setGroupRules(JSON.toJSONString(gropRuleVOList));
            dataGroupConfig.setUploadReportId(reportId);
            dataGroupConfig.setCreateTime(new Date());
            dataGroupConfig.setUpdateTime(new Date());
            dataGroupConfigMapper.insertSelective(dataGroupConfig);
            configId = dataGroupConfig.getId();
        } else {
            DataGroupConfig update = groupConfigList.get(0);
            configId = update.getId();
            List<DataGropRuleVO> updateGroupRules = JSON.parseObject(update.getGroupRules(), new TypeReference<List<DataGropRuleVO>>() {
            }.getType());
            if (operType.equals("0")) {
                updateGroupRules.addAll(gropRuleVOList);
            } else {
                updateGroupRules.removeIf(rule -> gropRuleMap.keySet().contains(rule.getGroupField()));
                //更新跑分配置 && 判断正在进行中的跑分不生成删除任务
                String value = UUID.randomUUID().toString();
                try {
                    //加锁
                    addLockGroupScoreConfig(dto.getApiCode(),value);
                    List<MarketingTaskVO> marketingTaskVOS = marketingTaskMapper.queryNoFinishStatus(dto.getApiCode(), LocalDate.now().minusDays(7).toString(),
                            LocalDate.now().plusDays(1).toString());
                    if (!CollectionUtils.isEmpty(marketingTaskVOS)) {
                        return new ApiResult().fail("有正在进行中的跑分");
                    }
                    updateScoreConfigField(dto.getApiCode(), gropRuleVOList.get(0).getGroupField(), "1");
                } catch (Exception e) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "分组任务编辑异常"), e);
                } finally {
                    unlockGroupScoreConfig(dto.getApiCode(),value);
                }
            }
            if (CollectionUtils.isEmpty(updateGroupRules)) {
                update.setGroupRules(null);
                update.setIsDel(9);
            } else {
                update.setGroupRules(JSON.toJSONString(updateGroupRules));
            }
            dataGroupConfigMapper.updateByPrimaryKey(update);
        }
        // 插入 任务表
        gropRuleMap.forEach((String field, List<DataGropRuleVO> groupRules) -> {
            DataGroupTask dataGroupTask = new DataGroupTask();
            dataGroupTask.setApiCode(dto.getApiCode());
            dataGroupTask.setConfigId(configId);
            dataGroupTask.setGroupFiled(field);
            dataGroupTask.setGroupRule(JSON.toJSONString(groupRules.get(0)));
            dataGroupTask.setOperType(Integer.valueOf(operType));
            dataGroupTask.setStatus(0);
            dataGroupTask.setCreateTime(new Date());
            dataGroupTask.setUpdateTime(new Date());
            dataGroupTaskMapper.insertSelective(dataGroupTask);

        });
        return new ApiResult<Long>().success(configId);
    }


    public void addLockGroupScoreConfig(String apiCode,String value) {
        String redisKey = RedisKeyConstant.DATA_GROUP_SCORE_CONFIG_LOCK.concat(apiCode);
        //加锁
        redisChgService.lockLoop(redisKey, value,30000L,300000L);
    }


    public void unlockGroupScoreConfig(String apiCode,String value) {
        String redisKey = RedisKeyConstant.DATA_GROUP_SCORE_CONFIG_LOCK.concat(apiCode);
        //解锁
        redisChgService.unlock(redisKey, value);
    }


    @Override
    public void dataGroupHandler(DataGroupTask dataGroupTask) {

        DataGroupConfig config = dataGroupConfigMapper.selectByPrimaryKey(dataGroupTask.getConfigId());
        if (dataGroupTask.getOperType().equals(0)) {
            addFieldHandler(config.getUploadReportId(), dataGroupTask);
        } else {
            delFieldHandler(config.getUploadReportId(), dataGroupTask);
        }
    }

    @Override
    public List<String> extendField(String ids, String apiCode) {
        List<String> fieldList = Lists.newArrayList("apiCode", "custNum", "idCard", "name", "cell", "userType");
        MarketingSyncReportExample reportExample = new MarketingSyncReportExample();
        reportExample.createCriteria().andIdIn(Arrays.stream(ids.split(",")).map(Long::parseLong).collect(Collectors.toList()));
        List<MarketingSyncReport> reportList = syncReportMapper.selectByExample(reportExample);
        reportList.forEach((MarketingSyncReport report) -> {
            if (StringUtils.isNotEmpty(report.getReserveField1Key())) {
                fieldList.addAll(Arrays.asList(report.getReserveField1Key().split(",")));
            }
        });
        List<String> result = fieldList.stream().distinct().collect(Collectors.toList());
        return result;
    }

    @Override
    public HashMap getGroupFieldPercent(String field, Long id) {
        DataGroupConfig config = dataGroupConfigMapper.selectByPrimaryKey(id);
        HashMap<String, Long> percentMap = new HashMap<>();
        List<DataGropRuleVO> dataGropRuleVOList = JSON.parseObject(config.getGroupRules(), new TypeReference<List<DataGropRuleVO>>() {
        }.getType());
        DataGropRuleVO rule = dataGropRuleVOList.stream().filter((DataGropRuleVO ruleVO) -> ruleVO.getGroupField().equals(field))
                .collect(Collectors.toList()).get(0);
        JSONObject ruleJson = rule.getGroupNum();
        if (rule.getGroupType().equals("0")) {
            DataGroupTaskExample dataGroupTaskExample = new DataGroupTaskExample();
            dataGroupTaskExample.createCriteria().andApiCodeEqualTo(config.getApiCode()).andConfigIdEqualTo(config.getId()).andGroupFiledEqualTo(field);
            List<DataGroupTask> groupTaskList = dataGroupTaskMapper.selectByExample(dataGroupTaskExample);
            if (!CollectionUtils.isEmpty(groupTaskList) && groupTaskList.get(0).getStatus().equals(2)) {
                ruleJson.forEach((Object k, Object v) -> {
                    percentMap.put((String) k, 100L);
                });

            } else {
                ruleJson.forEach((Object k, Object v) -> {
                    percentMap.put((String) k, 0L);
                });
            }
        } else {
            MarketingSyncReportExample reportExample = new MarketingSyncReportExample();
            reportExample.createCriteria().andIdIn(Arrays.stream(config.getUploadReportId().split(",")).map(Long::parseLong).collect(Collectors.toList()));
            List<MarketingSyncReport> reportList = syncReportMapper.selectByExample(reportExample);
            List<Map<String, Object>> groupNum = syncReportMapper.selectGroupUploadNumtikv_(config.getApiCode(), reportList, "reserve_field1->'$.\"".concat(field).concat("\"'"),
                    StringUtils.isEmpty(rule.getExtendField()) ? "" : "reserve_field1->'$.\"".concat(rule.getExtendField()).concat("\"'"));
            rule.setGroupRange("0");
            List<Map<String, Object>> groupNumTotal = dataGroupNumTransfer(rule, reportList, Boolean.FALSE);
            JSONObject totalJson = (JSONObject) groupNumTotal.get(0).get("rule");
            groupNum.forEach((Map<String, Object> map) -> {
                String groupField = ((String) map.get("field"));
                if (StringUtils.isBlank(groupField)) {
                    return;
                }
                String fieldTrim = groupField.replace("\"", "");
                Long num = (Long) map.get("num");
                percentMap.put(fieldTrim, num * 100 / totalJson.getInteger(fieldTrim));
            });
        }
        return percentMap;
    }

    private void delFieldHandler(String uploadReportId, DataGroupTask dataGroupTask) {
        String apiCode = dataGroupTask.getApiCode();
        String groupField = dataGroupTask.getGroupFiled();
        ThreadPoolExecutor pool = BrExecutors.getThreadPool(10, 10, 100);
        MarketingSyncReportExample reportExample = new MarketingSyncReportExample();
        reportExample.createCriteria().andIdIn(Arrays.stream(uploadReportId.split(",")).map(Long::parseLong).collect(Collectors.toList()));
        List<MarketingSyncReport> reportList = syncReportMapper.selectByExample(reportExample);
        reportList.forEach((MarketingSyncReport report) -> {
            Long indexId = null;
            Integer pageSize = 2000;
            while (true) {
                List<MarketingSyncUser> marketingSyncUserList = syncReportMapper.selectGroupData(apiCode, Lists.newArrayList(report.getAppletDate()),
                        report.getUserType(), null, indexId, pageSize);
                if (CollectionUtils.isEmpty(marketingSyncUserList)) {
                    break;
                }
                indexId = marketingSyncUserList.get(marketingSyncUserList.size() - 1).getId();
                modifyCorePoolSize(pool);
                pool.submit(() -> delGroupFieldData(marketingSyncUserList, groupField));
            }
        });

        // 关闭线程池
        pool.shutdown();
        try {
            while (!pool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("等待线程池结束");
            }
        } catch (InterruptedException ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "数据分组处理线程池停止异常！"), ex);
            Thread.currentThread().interrupt();
        }
    }

    private void delGroupFieldData(List<MarketingSyncUser> marketingSyncUserList, String extendField) {

        if (marketingSyncUserList.size() <= 0) {
            return;
        }
        try {
            String apiCode = marketingSyncUserList.get(0).getApiCode();
            StringBuilder update = new StringBuilder(String.format("UPDATE b_marketing_sync_%s SET reserve_field1 = CASE id ", apiCode));
            List<Long> ids = new ArrayList<>();
            for (MarketingSyncUser sync : marketingSyncUserList) {
                JSONObject jsonObject = JSON.parseObject(sync.getReserveField1());
                if (jsonObject.containsKey(extendField)) {
                    jsonObject.remove(extendField);
                }
                String jsonString = jsonObject.toJSONString();
                update.append("WHEN ").append(sync.getId()).append(" THEN '").append(jsonString).append("' ");
                ids.add(sync.getId());
            }
            if (ids.size() <= 0) {
                return;
            }
            update.append("END WHERE id IN (");
            update.append(StringUtils.join(ids, ","));
            update.append(");");
            syncReportMapper.updateBatchGroupData(update.toString());
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    private void addFieldHandler(String uploadReportId, DataGroupTask dataGroupTask) {
        String groupField = dataGroupTask.getGroupFiled();
        String apiCode = dataGroupTask.getApiCode();
        DataGropRuleVO rule = JSON.parseObject(dataGroupTask.getGroupRule(), new TypeReference<DataGropRuleVO>() {
        }.getType());
        MarketingSyncReportExample reportExample = new MarketingSyncReportExample();
        reportExample.createCriteria().andIdIn(Arrays.stream(uploadReportId.split(",")).map(Long::parseLong).collect(Collectors.toList()));
        List<MarketingSyncReport> reportList = syncReportMapper.selectByExample(reportExample);
        List<String> appletDates = reportList.stream().map(MarketingSyncReport::getAppletDate).distinct().collect(Collectors.toList());
        List<Map<String, Object>> groupTask = dataGroupNumTransfer(rule, reportList, Boolean.TRUE);
        ThreadPoolExecutor pool = BrExecutors.getThreadPool(10, 10, 100);
        groupTask.forEach((Map<String, Object> taskMap) -> {
            JSONObject jsonRule = (JSONObject) taskMap.get("rule");
            String userType = (String) taskMap.get("userType");
            String extendVaule = (String) taskMap.get(rule.getExtendField());
            StringBuilder extend = new StringBuilder();
            if (StringUtils.isNotEmpty(rule.getExtendField())) {
                extend.append("reserve_field1->'$.").append(rule.getExtendField()).append("'='").append(extendVaule).append("'");
            }
            Long indexId = null;
            Set<String> keySet = jsonRule.keySet();
            //单个规则分组清洗，对应同一批数据
            for (String field : keySet) {
                Integer pageSize;
                Integer total = (int) jsonRule.get(field);
                Integer sum = 0;
                //差值超过2000，每页赋值2000，小于2000，取小值
                if (total - sum >= 2000) {
                    pageSize = 2000;
                } else {
                    pageSize = total - sum;
                }
                List<MarketingSyncUser> marketingSyncUserList;
                while (true) {
                    //没有场景和扩展字段分组
                    if ((StringUtils.isEmpty(userType)) && StringUtils.isEmpty(extendVaule)) {
                        marketingSyncUserList = syncReportMapper.selectGroupDataByReport(apiCode, reportList, indexId, pageSize);
                    } else {
                        marketingSyncUserList = syncReportMapper.selectGroupData(apiCode, appletDates, userType,
                                extend.toString(), indexId, pageSize);
                    }
                    indexId = marketingSyncUserList.get(marketingSyncUserList.size() - 1).getId();
                    modifyCorePoolSize(pool);
                    /*pool.submit(() ->*/
                    updateGroupData(marketingSyncUserList, groupField, field)/*)*/;
                    sum += marketingSyncUserList.size();
                    //达到量级
                    if (sum.equals(total)) {
                        break;
                    }
                }
            }
        });
        // 关闭线程池
        pool.shutdown();
        try {
            while (!pool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("等待线程池结束");
            }
        } catch (InterruptedException ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "数据分组处理线程池停止异常！"), ex);
            Thread.currentThread().interrupt();
        }
        updateScoreConfigField(apiCode, groupField, "0");
    }

    private void updateScoreConfigField(String apiCode, String field, String type) {

        MarketingCustomerExample example = new MarketingCustomerExample();
        example.createCriteria().andApiCodeEqualTo(apiCode);
        // 校验客户信息是否正确
        List<MarketingCustomer> customerList = marketingCustomerMapper.selectByExample(example);
        CustomerRuleExample crExample = new CustomerRuleExample();
        crExample.createCriteria()
                .andCustomerIdIn(customerList.stream().map(MarketingCustomer::getId).collect(Collectors.toList()))
                .andIsDelEqualTo(Constants.DATA_VALID);
        // 根据客户主键获取客户下的跑分规则集合
        List<CustomerRule> customerRules = customerRuleMapper.selectByExample(crExample);
        customerRules.forEach((CustomerRule customerRule) -> {
            ScoreRuleConfig config = scoreRuleConfigMapper.selectByPrimaryKey(customerRule.getRuleId());
            //更新规则
            BaseHeadConfigVO baseHeadConfigVO = JSON.parseObject(config.getBaseInfo(), new TypeReference<BaseHeadConfigVO>() {
            }.getType());
            if (Objects.isNull(baseHeadConfigVO)) {
                return;
            }
            List<BaseHead> baseHeads = baseHeadConfigVO.getBaseHead();
            if (!CollectionUtils.isEmpty(baseHeads)) {
                if (type.equals("0")) {
                    BaseHead baseHead = new BaseHead();
                    baseHead.setName(field);
                    baseHead.setType(2);
                    baseHeads.add(baseHead);
                } else {
                    baseHeads.removeIf(head -> head.getName().equals(field));
                }
            }
            List<String> headConfig = baseHeadConfigVO.getShowBaseHead();
            if (!CollectionUtils.isEmpty(headConfig)) {
                if (type.equals("0")) {
                    headConfig.add(field);
                } else {
                    headConfig.removeIf(head -> head.equals(field));
                }
            }
            config.setBaseInfo(JSON.toJSONString(baseHeadConfigVO));
            scoreRuleConfigMapper.updateByPrimaryKeySelective(config);
        });

    }

    private void updateGroupData(List<MarketingSyncUser> marketingSyncUserList, String extendField, String extendVaule) {
        if (marketingSyncUserList.size() <= 0) {
            return;
        }
        try {
            String apiCode = marketingSyncUserList.get(0).getApiCode();
            StringBuilder update = new StringBuilder(String.format("UPDATE b_marketing_sync_%s SET reserve_field1 = CASE id ", apiCode));
            List<Long> ids = new ArrayList<>();
            for (MarketingSyncUser sync : marketingSyncUserList) {
                JSONObject jsonObject = JSON.parseObject(sync.getReserveField1());
                jsonObject.put(extendField, extendVaule);
                String jsonString = jsonObject.toJSONString();
                update.append("WHEN ").append(sync.getId()).append(" THEN '").append(jsonString).append("' ");
                ids.add(sync.getId());
            }
            if (ids.size() <= 0) {
                return;
            }
            update.append("END WHERE id IN (");
            update.append(StringUtils.join(ids, ","));
            update.append(");");
            syncReportMapper.updateBatchGroupData(update.toString());
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    private void modifyCorePoolSize(ThreadPoolExecutor pool) {

        Integer threadNum =
                marketingCommonConfig.getSuShangPushThreadNum();
        pool.setCorePoolSize(threadNum);
        pool.setMaximumPoolSize(threadNum);
        log.warn("数据分组处理线程数core={}，max={}", pool.getCorePoolSize(), pool.getMaximumPoolSize());
    }

    //数据分组量级转化
    private List<Map<String, Object>> dataGroupNumTransfer(DataGropRuleVO rule, List<MarketingSyncReport> reportList, Boolean extendGroup) {
        StringBuilder field = new StringBuilder();
        StringBuilder whereStr = new StringBuilder();
        StringBuilder groupStr = new StringBuilder();
        field.append("select count(1) as num");
        whereStr.append(" from b_marketing_sync_").append(reportList.get(0).getApiCode()).append(" where status =1 ");
        if (rule.getGroupRange().equals("1")) {
            field.append(",user_type as userType");
            groupStr.append(" group by  user_type");
        }
        if (StringUtil.isNotBlank(rule.getExtendField())) {
            field.append(",reserve_field1->>'$.").append(rule.getExtendField()).append("'  as ").append(rule.getExtendField());
            whereStr.append("and ").append("reserve_field1->'$.").append(rule.getExtendField()).append("' is not null ");
            if (extendGroup) {
                if (StringUtil.isBlank(groupStr)) {
                    groupStr.append(" group by reserve_field1->'$.").append(rule.getExtendField()).append("'");
                } else {
                    groupStr.append(",reserve_field1->'$.").append(rule.getExtendField()).append("'");
                }
            }
        }
        whereStr.append("and (");
        reportList.forEach((MarketingSyncReport report) -> {
            whereStr.append("(applet_date = '").append(report.getAppletDate()).append("' and user_type='").append(report.getUserType()).append("') or");
        });
        whereStr.replace(whereStr.length() - 3, whereStr.length(), "");
        whereStr.append(")");
        List<Map<String, Object>> groupNumList = syncReportMapper.selectGroupCount(field.append(whereStr).append(groupStr).toString());
        JSONObject ruleJson = rule.getGroupNum();
        groupNumList.forEach(map -> {
            int count = Integer.valueOf(map.get("num").toString());
            if (rule.getGroupType().equals("0")) {
                int groupNum = ruleJson.values().stream().filter(num -> ((!num.equals("remain")))).map(obj -> (Integer) obj)
                        .collect(Collectors.toList()).stream().mapToInt(Integer::intValue).sum();
                //int groupNum = ListNum.stream().mapToInt(Integer::intValue).sum();
                ruleJson.forEach((k, v) -> {
                    if (v.equals("remain")) {
                        ruleJson.put(k, count - groupNum);
                    }
                });
                map.put("rule", ruleJson);
            } else {
                //百分比转化处理
                Iterator<Map.Entry<String, Object>> iterator = ruleJson.entrySet().iterator();
                int lastIndex = ruleJson.size() - 1;
                int currentIndex = 0;
                int sum = 0;
                while (iterator.hasNext()) {
                    Map.Entry<String, Object> entry = iterator.next();
                    if (currentIndex == lastIndex) {
                        // 当前元素是最后一个元素
                        entry.setValue(count - sum);
                    } else {
                        // 当前元素不是最后一个元素
                        double num = Double.parseDouble(entry.getValue().toString().replace("%", "")) / 100 * count;
                        int intNum = (int) Math.round(num);
                        entry.setValue(intNum);
                        sum += intNum;
                    }
                    currentIndex++;
                }
                map.put("rule", ruleJson);
            }
        });

        return groupNumList;
    }
}
