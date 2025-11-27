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
import com.br.marketing.enums.clean.DataProcessEnum;
import com.br.marketing.mapper.*;
import com.br.marketing.mapper.datagroup.DataGroupTaskDetailMapper;
import com.br.marketing.service.SoleStrategyService;
import com.br.marketing.service.datagroup.DataGroupHandlerService;
import com.br.marketing.mapper.datagroup.DataGroupConfigMapper;
import com.br.marketing.mapper.datagroup.DataGroupTaskMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
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

    @Autowired
    private DataGroupTaskDetailMapper dataGroupTaskDetailMapper;

    @Autowired
    private MarketingJsonNodeParseMapper marketingJsonNodeParseMapper;


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
        DataGropRuleVO update = gropRuleVOList.get(0);
        String redisKey = RedisKeyConstant.DATA_GROUP_TASK_LOCK.concat(dto.getId().toString());
        String s = UUID.randomUUID().toString();
        try {
            //处理与定时任务执行时的并发操作
            redisChgService.lock(redisKey, s);
            DataGroupTaskExample dataGroupTaskExample = new DataGroupTaskExample();
            dataGroupTaskExample.createCriteria().andApiCodeEqualTo(dto.getApiCode()).andConfigIdEqualTo(dto.getId()).andOperTypeEqualTo(0)
                    .andGroupFiledEqualTo(update.getGroupField());
            dataGroupTaskExample.setOrderByClause("create_time desc limit 1");
            List<DataGroupTask> groupTaskList = dataGroupTaskMapper.selectByExample(dataGroupTaskExample);
            List<DataGroupTask> runingTask = groupTaskList.stream().filter(task -> task.getStatus() != 0).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(runingTask)) {
                return new ApiResult().fail("分组任务已开始执行，无法进行编辑");
            }
            groupTaskList.forEach((DataGroupTask groupTask) -> {
                groupTask.setGroupRule(JSON.toJSONString(update));
                dataGroupTaskMapper.updateByPrimaryKeySelective(groupTask);
            });
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "分组任务编辑异常"), e);
        } finally {
            redisChgService.unlock(redisKey, s);
        }
        //更新配置
        DataGroupConfig config = dataGroupConfigMapper.selectByPrimaryKey(dto.getId());
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
                List<DataGropRuleVO> repeatRule = updateGroupRules.stream().filter(rule -> rule.getGroupField().equals(gropRuleVOList.get(0).
                        getGroupField())).collect(Collectors.toList());
                if(!CollectionUtils.isEmpty(repeatRule)){
                    return new ApiResult().fail("打标字段已存在");
                }
                updateGroupRules.addAll(gropRuleVOList);
            } else {
                updateGroupRules.removeIf(rule -> gropRuleMap.keySet().contains(rule.getGroupField()));
                //更新跑分配置 && 判断正在进行中的跑分不生成删除任务
                String value = UUID.randomUUID().toString();
                try {
                    //加锁
                    addLockGroupScoreConfig(dto.getApiCode(), value);
                    List<MarketingTaskVO> marketingTaskVOS = marketingTaskMapper.queryNoFinishStatus(dto.getApiCode(), LocalDate.now().minusDays(7).toString(),
                            LocalDate.now().plusDays(1).toString());
                    if (!CollectionUtils.isEmpty(marketingTaskVOS)) {
                        return new ApiResult().fail("有正在进行中的跑分");
                    }
                    updateScoreConfigField(dto.getApiCode(), gropRuleVOList.get(0).getGroupField(), "1");
                } catch (Exception e) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "分组任务编辑异常"), e);
                } finally {
                    unlockGroupScoreConfig(dto.getApiCode(), value);
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


    public void addLockGroupScoreConfig(String apiCode, String value) {
        String redisKey = RedisKeyConstant.DATA_GROUP_SCORE_CONFIG_LOCK.concat(apiCode);
        //加锁
        redisChgService.lockLoop(redisKey, value, 30000L, 300000L);
    }


    public void unlockGroupScoreConfig(String apiCode, String value) {
        String redisKey = RedisKeyConstant.DATA_GROUP_SCORE_CONFIG_LOCK.concat(apiCode);
        //解锁
        redisChgService.unlock(redisKey, value);
    }


    @Override
    public void dataGroupHandler(DataGroupTask dataGroupTask) {
        Long start = System.currentTimeMillis();
        DataGroupConfig config = dataGroupConfigMapper.selectByPrimaryKey(dataGroupTask.getConfigId());
        if (dataGroupTask.getOperType().equals(0)) {
            addFieldHandler(config.getUploadReportId(), dataGroupTask);
        } else {
            delFieldHandler(config.getUploadReportId(), dataGroupTask);
        }
        log.warn("数据分组分组任务执行完成，耗时{}s", (System.currentTimeMillis() - start) / 1000);
    }

    @Override
    public List<String> extendField(String ids, String apiCode) {
        List<String> fieldList = Lists.newArrayList("apiCode", "custNum", "idCard", "name", "cell", "userType");
        //数禾上传需改造
        MarketingJsonNodeParseExample jsonNodeParseExample = new MarketingJsonNodeParseExample();
        jsonNodeParseExample.createCriteria().andApiCodeEqualTo(apiCode).andDataTypeEqualTo(DataProcessEnum.UPLOAD_DATA_GENERAL.getDataType()).
                andAcceptTypeEqualTo(DataProcessEnum.UPLOAD_DATA_GENERAL.getAcceptType())
                .andParentPathEqualTo("dataItems.item.reserveField1");
        List<MarketingJsonNodeParse> jsonNodeParseList = marketingJsonNodeParseMapper.selectByExample(jsonNodeParseExample);
        List<String> result = jsonNodeParseList.stream().map(MarketingJsonNodeParse::getNodeName).collect(Collectors.toList());
        result.addAll(fieldList);
        return result.stream().distinct().collect(Collectors.toList());
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
        DataGroupTaskExample dataGroupTaskExample = new DataGroupTaskExample();
        dataGroupTaskExample.createCriteria().andApiCodeEqualTo(config.getApiCode()).andConfigIdEqualTo(config.getId()).andGroupFiledEqualTo(field);
        List<DataGroupTask> groupTaskList = dataGroupTaskMapper.selectByExample(dataGroupTaskExample);
        String groupResultNumKey = RedisKeyConstant.DATA_GROUP_RESULT_NUM.concat(":").concat(config.getApiCode()).concat(":")
                .concat(groupTaskList.get(0).getId().toString());
        Map<String, Object> runMap = redisChgService.hgetall(groupResultNumKey);
        //查询任务明细表
        DataGroupTaskDetailExample taskDetailExample = new DataGroupTaskDetailExample();
        taskDetailExample.createCriteria().andGroupTaskIdEqualTo(groupTaskList.get(0).getId());
        List<DataGroupTaskDetail> taskDetailList = dataGroupTaskDetailMapper.selectByExample(taskDetailExample);
        if (CollectionUtils.isEmpty(taskDetailList) || CollectionUtils.isEmpty(runMap)) {
            ruleJson.forEach((Object k, Object v) -> {
                percentMap.put((String) k, 0L);
            });

        } else {
            Map<String, Long> taskDetailMap = taskDetailList.stream().collect(Collectors.groupingBy(DataGroupTaskDetail::getGroupFieldValue,
                    Collectors.summingLong(DataGroupTaskDetail::getGroupNum)));
            ruleJson.forEach((Object k, Object v) -> {
                String fieldVaule = (String) k;
                Long runNum = Long.parseLong((String) runMap.get(fieldVaule));
                percentMap.put(fieldVaule, runNum * 100 / taskDetailMap.get(fieldVaule));
            });

        }

        return percentMap;
    }

    private void delFieldHandler(String uploadReportId, DataGroupTask dataGroupTask) {
        String apiCode = dataGroupTask.getApiCode();
        String groupField = dataGroupTask.getGroupFiled();
        ThreadPoolExecutor pool = BrExecutors.getThreadPool(10, 10, 50);
        MarketingSyncReportExample reportExample = new MarketingSyncReportExample();
        reportExample.createCriteria().andIdIn(Arrays.stream(uploadReportId.split(",")).map(Long::parseLong).collect(Collectors.toList()));
        List<MarketingSyncReport> reportList = syncReportMapper.selectByExample(reportExample);
        reportList.forEach((MarketingSyncReport report) -> {
            Long indexId = null;
            Integer pageSize = marketingCommonConfig.getDataGroupPageSize().get("delFieldPageSize");
            while (true) {
                List<MarketingSyncUser> marketingSyncUserList = syncReportMapper.selectGroupDataByReport(apiCode, Lists.newArrayList(report),
                        null, indexId, pageSize);
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
        Long taskId = dataGroupTask.getId();
        DataGropRuleVO rule = JSON.parseObject(dataGroupTask.getGroupRule(), new TypeReference<DataGropRuleVO>() {
        }.getType());
        MarketingSyncReportExample reportExample = new MarketingSyncReportExample();
        reportExample.createCriteria().andIdIn(Arrays.stream(uploadReportId.split(",")).map(Long::parseLong).collect(Collectors.toList()));
        List<MarketingSyncReport> reportList = syncReportMapper.selectByExample(reportExample);
        dataGroupNumTransfer(rule, reportList, taskId);
        ThreadPoolExecutor pool = BrExecutors.getThreadPool(10, 10, 20);
        //查询任务明细表
        DataGroupTaskDetailExample taskDetailExample = new DataGroupTaskDetailExample();
        taskDetailExample.createCriteria().andGroupTaskIdEqualTo(taskId);
        List<DataGroupTaskDetail> taskDetailList = dataGroupTaskDetailMapper.selectByExample(taskDetailExample);
        taskDetailList.forEach(taskDetail ->{
            Long minId = taskDetail.getGroupMinId();
            Long maxId = taskDetail.getGroupMaxId();
            Boolean continueFlag = Boolean.TRUE;
            while(continueFlag){
                Integer pageSize =  marketingCommonConfig.getDataGroupPageSize().get("addFieldPageSize");
                modifyCorePoolSize(pool);
                Long middleId = minId+pageSize;
                if(middleId>=maxId){
                    middleId = maxId+1;
                    continueFlag = Boolean.FALSE;
                }
                Long finalMinId = minId;
                Long finalMiddleId = middleId;
                pool.submit(() -> {
                    addGroupFieldData(taskDetail, finalMinId, finalMiddleId);
                });
                minId = finalMiddleId;

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

    private void addGroupFieldData(DataGroupTaskDetail taskDetail, Long minId, Long endId) {
        try {
            Long start = System.currentTimeMillis();
            String apiCode = taskDetail.getApiCode();
            StringBuilder update = new StringBuilder(
                    String.format("UPDATE b_marketing_sync_%s SET reserve_field1 = CONCAT(SUBSTRING(reserve_field1, 1, (character_length(reserve_field1)" +
                            " - 1)),',\"%s\":\"%s\"}') where ", apiCode, taskDetail.getGroupField(), taskDetail.getGroupFieldValue()));
            update.append(taskDetail.getUpdateCondition()).append(" and id>=").append(minId).append(" and id<").append(endId);
            int updateNum = syncReportMapper.updateBatchGroupData(update.toString());
            String groupResultNumKey = RedisKeyConstant.DATA_GROUP_RESULT_NUM.concat(":").concat(taskDetail.getApiCode()).concat(":")
                    .concat(taskDetail.getGroupTaskId().toString());
            try {
                redisChgService.hincrby(groupResultNumKey, taskDetail.getGroupFieldValue(), updateNum);
            } catch (Exception ex) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "数据分组结果存入redis异常！"), ex);
            }
            log.warn("数据分组更新字段minId={}-endId={}，量级={}，耗时{}s", minId, endId, updateNum, (System.currentTimeMillis() - start) / 1000);
        } catch (Exception ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "数据分组添加字段更新异常！"), ex);
        }
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
            if (Objects.isNull(config)) {
                return;
            }
            //更新规则
            BaseHeadConfigVO baseHeadConfigVO = JSON.parseObject(config.getBaseInfo(), new TypeReference<BaseHeadConfigVO>() {
            }.getType());
            if (Objects.isNull(baseHeadConfigVO)) {
                return;
            }
            List<BaseHead> baseHeads = baseHeadConfigVO.getBaseHead();
            if (!CollectionUtils.isEmpty(baseHeads)) {
                List<BaseHead> exist =baseHeads.stream().filter(baseHead -> field.equals(baseHead.getName())).collect(Collectors.toList());
                    if (type.equals("0")) {
                        //不存在该字段再添加
                        if(CollectionUtils.isEmpty(exist)) {
                            BaseHead baseHead = new BaseHead();
                            baseHead.setName(field);
                            baseHead.setType(2);
                            baseHeads.add(baseHead);
                        }
                    } else {
                        baseHeads.removeIf(head -> head.getName().equals(field));
                    }
            }
            List<String> headConfig = baseHeadConfigVO.getShowBaseHead();
            if (!CollectionUtils.isEmpty(headConfig)) {
                if (type.equals("0")) {
                    if(!headConfig.contains(field)) {
                        headConfig.add(field);
                    }
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
                marketingCommonConfig.getDataGroupThreadNum();
        if (!Objects.isNull(threadNum)) {
            ThreadPoolAdjustmentUtil.adjustThreadPoolSize(pool, threadNum);
        }
        log.warn("数据分组处理线程数core={}，max={}", pool.getCorePoolSize(), pool.getMaximumPoolSize());
    }

    //数据分组量级转化
    private void  dataGroupNumTransfer(DataGropRuleVO rule, List<MarketingSyncReport> reportList, Long taskId) {
        StringBuilder field = new StringBuilder();
        StringBuilder whereStr = new StringBuilder();
        StringBuilder groupStr = new StringBuilder();
        StringBuilder reportStr = new StringBuilder();
        String apiCode = reportList.get(0).getApiCode();
        field.append("select count(1) as num");
        whereStr.append(" from b_marketing_sync_").append(apiCode).append(" where status =1 and is_repeat in (1, 2) ");
        if (rule.getGroupRange().equals("1")) {
            field.append(",user_type as userType");
            groupStr.append(" group by  user_type");
        }
        if (StringUtil.isNotBlank(rule.getExtendField())) {
            field.append(",reserve_field1->'$.\"").append(rule.getExtendField()).append("\"'  as ").append(rule.getExtendField());
            whereStr.append("and ").append("reserve_field1->'$.\"").append(rule.getExtendField()).append("\"' is not null ");
            if (StringUtil.isBlank(groupStr)) {
                groupStr.append(" group by reserve_field1->'$.\"").append(rule.getExtendField()).append("\"'");
            } else {
                groupStr.append(",reserve_field1->'$.\"").append(rule.getExtendField()).append("\"'");
            }
        }
        reportStr.append("and (");
        reportList.forEach((MarketingSyncReport report) -> {
            reportStr.append("(applet_date = '").append(report.getAppletDate()).append("' and user_type='").append(report.getUserType()).append("') or");
        });
        reportStr.replace(reportStr.length() - 3, reportStr.length(), "");
        reportStr.append(")");
        List<Map<String, Object>> groupNumList = syncReportMapper.selectGroupCounttikv_(field.append(whereStr).append(reportStr).append(groupStr).toString());
        JSONObject ruleJson = rule.getGroupNum();
        Map<String, Set<String>> appletDateMap = reportList.stream().collect(Collectors.groupingBy(
                MarketingSyncReport::getUserType, Collectors.mapping(MarketingSyncReport::getAppletDate, Collectors.toSet())));
        groupNumList.forEach(map -> {
            int count = Integer.valueOf(map.get("num").toString());
            JSONObject ruleNew = new JSONObject();
            if (rule.getGroupType().equals("0")) {
                int groupNum = ruleJson.values().stream().filter(num -> ((!num.equals("remain")))).map(obj -> (Integer) obj)
                        .collect(Collectors.toList()).stream().mapToInt(Integer::intValue).sum();
                ruleJson.forEach((k, v) -> {
                    if (v.equals("remain")) {
                        ruleNew.put(k, count - groupNum);
                    } else {
                        ruleNew.put(k, v);
                    }
                });
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
                        ruleNew.put(entry.getKey(), count - sum);
                    } else {
                        // 当前元素不是最后一个元素
                        double num = Double.parseDouble(entry.getValue().toString().replace("%", "")) / 100 * count;
                        int intNum = (int) Math.round(num);
                        ruleNew.put(entry.getKey(), intNum);
                        sum += intNum;
                    }
                    currentIndex++;
                }
            }
            // 解析任务插入任务详情表
            Long indexId = null;
            for (Map.Entry<String, Object> entry : ruleNew.entrySet()) {
                String groupFieldValue = entry.getKey();
                int groupNum = (int) entry.getValue();
                if (groupNum == 0) {
                    continue;
                }
                StringBuilder sqlWhere = new StringBuilder();
                StringBuilder appletDateWhere = new StringBuilder();
                sqlWhere.append("status =1 and is_repeat in (1, 2) ");
                appletDateWhere.append(" and applet_date");
                String userType = (String) map.get("userType");
                //有user_type，获取user_type对应的appletDate
                if (StringUtils.isNotEmpty(userType)) {
                    List<String> appletDates = Lists.newArrayList(appletDateMap.get(userType));
                    if (appletDates.size() == 1) {
                        appletDateWhere.append(" = '").append(appletDates.get(0)).append("'");
                    } else {
                        appletDateWhere.append(" in (").append(appletDates.stream().map(item -> "'" + item + "'").collect(Collectors.joining(",")
                        )).append(")");
                    }
                    sqlWhere.append(" and user_type =").append(userType).append(appletDateWhere);
                } else {
                    sqlWhere.append(reportStr);
                }
                String extendField = rule.getExtendField();
                String extendVaule = ((String) map.get(extendField));
                if (StringUtils.isNotEmpty(extendField)) {
                    sqlWhere.append(" and  reserve_field1->'$.").append(rule.getExtendField()).append("'='").append(extendVaule.replace("\"",
                            "")).append("'");
                }
                Long start = System.currentTimeMillis();
                Map<String, Long> maxMinId = syncReportMapper.selectGroupMaxMinId(apiCode, indexId, groupNum, sqlWhere.toString());
                Long minId = maxMinId.get("minId");
                Long maxId = maxMinId.get("maxId");
                indexId = maxId;
                DataGroupTaskDetail taskDetail = new DataGroupTaskDetail();
                taskDetail.setApiCode(apiCode);
                taskDetail.setGroupField(rule.getGroupField());
                taskDetail.setGroupTaskId(taskId);
                taskDetail.setGroupFieldValue(groupFieldValue);
                taskDetail.setExtendField(extendField);
                taskDetail.setExtendFieldValue(StringUtils.isNotEmpty(extendVaule) ? extendVaule.replace("\"", "") : null);
                taskDetail.setGroupNum(groupNum);
                taskDetail.setGroupMaxId(maxId);
                taskDetail.setGroupMinId(minId);
                taskDetail.setUserType(userType);
                taskDetail.setUpdateCondition(sqlWhere.toString());
                taskDetail.setCreateTime(new Date());
                taskDetail.setUpdateTime(new Date());
                dataGroupTaskDetailMapper.insertSelective(taskDetail);
                log.warn("数据分组任务解析，耗时{}s", (System.currentTimeMillis() - start) / 1000);  
            }
        });
    }
}
