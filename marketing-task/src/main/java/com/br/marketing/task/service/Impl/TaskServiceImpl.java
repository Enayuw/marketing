package com.br.marketing.task.service.Impl;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.ZookeeperPath;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.customizedassert.AssertResult;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.service.IApiToDbService;
import com.br.marketing.service.IDynamicSqlService;
import com.br.marketing.service.IRuleConfigService;
import com.br.marketing.service.SoleStrategyService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.task.service.ITaskService;
import com.br.marketing.vo.CustomerScoreRuleVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class TaskServiceImpl implements ITaskService {

    final static DateTimeFormatter ymdhms = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    final static DateTimeFormatter ymd = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    IRuleConfigService iRuleConfigService;

    @Autowired
    SoleStrategyService soleStrategyService;

    @Resource
    MarketingSyncInfoMapper syncInfoMapper;

    @Autowired
    IApiToDbService iApiToDbService;

    @Resource
    MarketingTaskMapper marketingTaskMapper;

    @Resource
    MarketingTaskExtendMapper marketingTaskExtendMapper;

    @Resource
    TaskBatchnumberPreMapper taskBatchnumberPreMapper;

    @Resource
    ScoreRuleConfigMapper scoreRuleConfigMapper;

    static String warnTemp = "apiCode：%s,数据id：%s,错误信息：%s";

    @Autowired
    IDynamicSqlService iDynamicSqlService;

    @Resource
    TaskStatusMapper taskStatusMapper;

    @Autowired
    RedisChgService redisChgService;

    @Autowired
    private CuratorFramework client;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Override
    public void buildScoreTask(List<Long> scoreRuleIds) {
        Result<List<CustomerScoreRuleVO>> scoreConfigNow = iRuleConfigService.getScoreConfigNow(scoreRuleIds);
        AssertResult.assertResult(scoreConfigNow);
        List<CustomerScoreRuleVO> data = scoreConfigNow.getData();
        for (CustomerScoreRuleVO datum : data) {
            if (datum.getParentId() <= 0) {
                buildScoreTaskOfAuto(datum);
            } else {
                buildScoreTaskOfSelect(datum);
            }
        }
    }

    private void buildScoreTaskOfAuto(CustomerScoreRuleVO vo) {
        String apiCode = vo.getApiCode();

        //region 时间处理
        String startTime = vo.getStartTime();
        LocalDateTime nowTime = LocalDateTime.now();
        LocalDate nowData = LocalDate.now();
        String validTimeStr = nowData.format(ymd).concat(" " + startTime + ":00");
        LocalDateTime validTime = LocalDateTime.parse(validTimeStr, ymdhms);

        //筛选数据范围时间
        String sTimeStr = "", eTimeStr = "";
        Date sTime = null, eTime = null;
        //任务的开始时间和结束时间
        String taskStart = "", taskEnd = "";
        if (nowTime.compareTo(validTime) > 0) {
            if ("00:00".equals(startTime)) {
                sTimeStr = nowData.minusDays(1L).format(ymd).concat(" 00:00:00");
                eTimeStr = validTime.format(ymdhms);
            } else {
                sTimeStr = nowData.format(ymd).concat(" 00:00:00");
                eTimeStr = validTime.format(ymdhms);
            }
        } else {
            sTimeStr = nowData.minusDays(1L).format(ymd).concat(" 00:00:00");
            eTimeStr = validTime.minusDays(1L).format(ymdhms);

        }
        taskStart = LocalDate.now().minusDays(1L).format(ymd);
        taskEnd = LocalDate.now().format(ymd);
        String sDate = LocalDateTime.parse(sTimeStr,ymdhms).format(ymd);
        try {
            sTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(sTimeStr);
            eTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(eTimeStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Date ruleOpenTime = vo.getUpdateTime();
        if (ruleOpenTime == null) {
            ruleOpenTime = vo.getCreateTime();
        }
        String ruleOpenDay = new SimpleDateFormat("yyyy-MM-dd").format(ruleOpenTime);
        String nowDay = LocalDate.now().format(ymd);
        // 规则启用日期和生成任务日期相同 需要比较 生效时间是小于等于规则开启时间 认为历史的任务不予生成
        if (ruleOpenDay.equals(nowDay) && eTime.compareTo(ruleOpenTime) <= 0) {
            return;
        }
        //endregion

        //region 条件解析
        Result<String> conditionRes = soleStrategyService.analysisCondition(vo.getConditionInfo());
        if (!ResultCode.SUCCESS.getValue().equals(conditionRes.getCode())) {
            log.warn(String.format("自动规则生成任务 数据范围解析有误;" + warnTemp, vo.getApiCode(), vo.getId(), conditionRes.getMessage()));
            return;
        }

        MarketingSyncInfoExample syncInfoIngExample = new MarketingSyncInfoExample();
        syncInfoIngExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andCreateTimeGreaterThanOrEqualTo(sTime)
                .andCreateTimeLessThan(eTime)
                .andStatusEqualTo(1)
                .andIsUploadEqualTo(1);
        int isUploadCount = syncInfoMapper.countByExample(syncInfoIngExample);
        if (isUploadCount > 0) {
            log.warn(String.format("自动规则生成任务 上传数据还未解析完成" + warnTemp, vo.getApiCode(), vo.getId()));
            return;
        }
        String number = "";

        Long minId = syncInfoMapper
                .getMinIdByRuleScoreWithDate(apiCode, sDate, eTimeStr, conditionRes.getData());
        if (minId != null && minId > 0) {
            String time = LocalDateTime.parse(eTimeStr, ymdhms).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            Result<String> batchNumberRes = iApiToDbService.buildBatchNumber(apiCode
                    , vo.getId().toString(), vo.getRuleNameShort()
                    , time, null);
            if (!ResultCode.SUCCESS.getValue().equals(batchNumberRes.getCode())) {
                log.warn(String.format("自动规则生成任务 批次号生成错误" + warnTemp, vo.getApiCode(), vo.getId()));
                return;
            }
            number = batchNumberRes.getData();
        } else {
            return;
        }
        //endregion

        String whereSql = soleStrategyService.analysisSimpleConditionPlus(conditionRes.getData(),sDate, eTimeStr);

        Integer integer = iDynamicSqlService.countByRuleScoreWithDate(apiCode, whereSql);

        saveTask(apiCode, number, vo, taskStart, integer);

    }

    private void buildScoreTaskOfSelect(CustomerScoreRuleVO vo) {

        String apiCode = vo.getApiCode();
        Result<List<String>> listResult = soleStrategyService.analysisConditions(vo.getConditionInfo());
        if (!ResultCode.SUCCESS.getValue().equals(listResult.getCode())) {
            return;
        }

        List<String> data = listResult.getData();

        Integer count = 0;

        for (String datum : data) {
            Integer integer = iDynamicSqlService.countByRuleScoreWithDate(apiCode, datum);
            count += integer;
        }

        String number = "";
        if (count > 0) {
            String concatTime = vo.getStartDate().concat(" ").concat(vo.getStartTime() + ":00");
            String time = LocalDateTime.parse(concatTime, ymdhms).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            Result<String> batchNumberRes = iApiToDbService.buildBatchNumber(apiCode
                    , vo.getId().toString(), vo.getRuleNameShort()
                    , time, null);
            if (!ResultCode.SUCCESS.getValue().equals(batchNumberRes.getCode())) {
                log.warn(String.format("自动规则生成任务 批次号生成错误" + warnTemp, vo.getApiCode(), vo.getId()));
                return;
            }
            number = batchNumberRes.getData();
        }
        Result result = saveTask(apiCode, number, vo, vo.getStartDate(), count);

        if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            return;
        }
        ScoreRuleConfig ruleConfig = new ScoreRuleConfig();
        ruleConfig.setId(vo.getId());
        ruleConfig.setStatus(2);
        scoreRuleConfigMapper.updateByPrimaryKeySelective(ruleConfig);

    }

    private Result saveTask(String apiCode, String batchNumber, CustomerScoreRuleVO ruleVO, String taskStart, Integer preNum) {

        MarketingTask hasTask = marketingTaskMapper.getByBatchNumber(batchNumber);
        if (hasTask != null) {
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }

        //region 处理task
        MarketingTask task = new MarketingTask();
        task.setApiCode(apiCode);
        task.setBatchNumber(batchNumber);
        task.setMonitorStatus(1);
        task.setStatus(1);
        task.setTaskNumber(preNum);
        task.setStartTime(ruleVO.getStartTime());
        task.setTaskType(ruleVO.getTaskType());
        task.setStrategyId(ruleVO.getStrategyId());
        task.setProductInfo(ruleVO.getProductInfo());
        task.setFileName(String.format("%s_%s", ruleVO.getId().toString(), ruleVO.getRuleNameShort()));
        task.setCusBatch(ruleVO.getId().toString());
        task.setMonitorType(ruleVO.getExecType());
        if (Integer.valueOf(4).equals(ruleVO.getExecType())) {
            MarketingTask task1 = marketingTaskMapper.selectCycleTopByApiCode(apiCode);
            if (task1 != null) {
                task.setStartDate(task1.getStartDate());
                task.setCloseDate(task1.getCloseDate());
            } else {
                task.setStartDate(taskStart);
                task.setCloseDate(ruleVO.getCycleEndDay());
            }
            task.setCycleDay(ruleVO.getCycleDay().toString());
        } else if (Integer.valueOf(3).equals(ruleVO.getExecType())) {
            task.setMonitorType(4);
            task.setStartDate(taskStart);
            task.setCloseDate(ruleVO.getCycleEndDay());
            task.setCycleDay(ruleVO.getCycleDay().toString());
        } else {
            String taskEnd = LocalDate.parse(taskStart, ymd)
                    .plusDays(1L).format(ymd);
            task.setStartDate(taskStart);
            task.setCloseDate(taskEnd);
        }
        task.setCreateTime(LocalDateTime.now().format(ymdhms));
        task.setContextId(iApiToDbService.getTaskContextId());
        marketingTaskMapper.insertSelective(task);
        //endregion

        //region跑分扩展表
        MarketingTaskExtend taskExtend = new MarketingTaskExtend();
        taskExtend.setApiCode(apiCode);
        taskExtend.setTaskId(Long.valueOf(task.getId()));
        taskExtend.setCreateTime(new Date());
        taskExtend.setExtendShowTitle(ruleVO.getBaseInfo());
        taskExtend.setRuleId(ruleVO.getId());
        taskExtend.setStrategyProductJson(ruleVO.getStrategyProductJson());
        taskExtend.setDataCondition(ruleVO.getConditionInfo());
        marketingTaskExtendMapper.insertSelective(taskExtend);
        //endregion

        //region 跑分编号表
        TaskBatchnumberPreExample updateBatchExample = new TaskBatchnumberPreExample();
        updateBatchExample.createCriteria().andBatchNumberEqualTo(batchNumber);
        TaskBatchnumberPre updateBatchnumber = new TaskBatchnumberPre();
        updateBatchnumber.setStatus(2);
        taskBatchnumberPreMapper.updateByExampleSelective(updateBatchnumber, updateBatchExample);
        //endregion

        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public Result<MarketingTask> getScoreTask(String date, Long taskId) {
        Integer resource = 0;

        try {
            resource = getResource();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage(String.format("获取跑分资源情况报错:%s", e.getMessage()));
        }
        if (resource <= 0) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("目前跑分资源已经占满");
        }
        String nowDay = LocalDate.now().format(ymd);
        if (StringUtils.isNotBlank(date)) {
            nowDay = date;
        }
        List<MarketingTask> scoreTasks = marketingTaskMapper.getScoreTasks(nowDay, taskId);

        for (MarketingTask scoreTask : scoreTasks) {
            Result<TaskStatus> taskStatusResult = canScore(scoreTask, nowDay);
            if (!ResultCode.SUCCESS.getValue().equals(taskStatusResult.getCode())) {
                continue;
            }
            TaskStatus statusData = taskStatusResult.getData();
            String s = UUID.randomUUID().toString();
            boolean taskLock = getTaskLock(scoreTask, s);
            if (!taskLock) {
                continue;
            }
            if (statusData != null) {
                TaskStatus updateStatus = new TaskStatus();
                updateStatus.setId(statusData.getId());
                if (scoreTask.getMonitorType().equals(1)) {
                    updateStatus.setOnceStatus(1);
                } else {
                    updateStatus.setAllStatus(1);
                }
                taskStatusMapper.updateByPrimaryKeySelective(updateStatus);
                scoreTask.setFileId(statusData.getFileId());
                scoreTask.setStatusId(statusData.getId());
            } else {
                String time = LocalDateTime.now().format(ymdhms);
                TaskStatus entityStatus = new TaskStatus();
                entityStatus.setApiCode(scoreTask.getApiCode());
                entityStatus.setBatchNumber(scoreTask.getBatchNumber());
                entityStatus.setCreateTime(time);
                entityStatus.setUpdateTime(time);
                if (scoreTask.getMonitorType().equals(1)) {
                    entityStatus.setOnceStatus(1);
                } else {
                    entityStatus.setAllStatus(1);
                }
                taskStatusMapper.insertSelective(entityStatus);
                scoreTask.setStatusId(entityStatus.getId());
            }

            removeTaskLock(scoreTask, s);
            return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(scoreTask);
        }

        return new Result<>().setCode(ResultCode.SUCCESS.getValue());
    }

    /**
     * 2-暂停；3-恢复跑分；
     *
     * @param task
     * @return
     */
    private Result<TaskStatus> canScore(MarketingTask task, String nowDay) {

        if (1 == task.getMonitorType()) {
            //region 一次性跑分
            List<TaskStatus> bts = taskStatusMapper.queryOnceBts(task.getBatchNumber());
            if (bts.size() > 0 && bts.get(0).getOnceStatus().equals(3)) {
                return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(bts.get(0));
            }
            if (bts.size() > 0) {
                return new Result<>().setCode(ResultCode.FAIL.getValue());
            }
            return new Result<>().setCode(ResultCode.SUCCESS.getValue());
            //endregion
        } else if (4 == task.getMonitorType()) {
            //region 周期性跑分
            long days = 0;
            try {
                days = DateHelper.getDistanceDays(nowDay, task.getStartDate());
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Integer cycleDay;
            if (StringUtils.isNotBlank(task.getCycleDay())) {
                cycleDay = Integer.valueOf(task.getCycleDay());
            } else {
                cycleDay = Constants.frequencyMap.get(task.getFrequency());
            }
            if (cycleDay == null || cycleDay == 0) {
                log.error(String.format("该周期性任务 没有配置周期时间 任务id：%d", task.getId()));
                return new Result<>().setCode(ResultCode.FAIL.getValue());
            }
            if (days % cycleDay == 0) {
                TaskStatusExample statusExample = new TaskStatusExample();
                statusExample.createCriteria()
                        .andBatchNumberEqualTo(task.getBatchNumber())
                        .andCreateTimeGreaterThanOrEqualTo(nowDay)
                        .andCreateTimeLessThan(LocalDate.parse(nowDay, ymd).plusDays(1).format(ymd));
                List<TaskStatus> taskStatuses = taskStatusMapper.selectByExample(statusExample);
                if (taskStatuses.size() > 0 && taskStatuses.get(0).getAllStatus().equals(3)) {
                    return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(taskStatuses.get(0));
                }
                if (taskStatuses.size() > 0) {
                    return new Result<>().setCode(ResultCode.FAIL.getValue());
                }
                return new Result<>().setCode(ResultCode.SUCCESS.getValue());
            }
            return new Result<>().setCode(ResultCode.FAIL.getValue());
            //endregion
        }
        return new Result<>().setCode(ResultCode.FAIL.getValue());
    }

    private boolean getTaskLock(MarketingTask task, String lockValue) {
        String key = RedisKeyConstant.taskGetLock.concat(":").concat(task.getId().toString());
        Long setnx = redisChgService.setnx(key, lockValue, 10);
        if (setnx.equals(0L)) {
            return false;
        }
        return true;
    }

    private void removeTaskLock(MarketingTask task, String lockValue) {
        String key = RedisKeyConstant.taskGetLock.concat(":").concat(task.getId().toString());
        String s = redisChgService.get(key);
        if (lockValue.equals(s)) {
            redisChgService.del(key);
        }
    }

    private Integer getResource() throws Exception {
        Integer hasResource = 0;
        int maxNum = marketingCommonConfig.getTaskResourceMaxNum() == null ? 300 : marketingCommonConfig.getTaskResourceMaxNum();
        List<String> parentPaths = Arrays.asList(ZookeeperPath.loanPath, ZookeeperPath.marketPath);
        for (String parentPath : parentPaths) {
            if (client.checkExists().forPath(parentPath) != null) {
                List<String> loanPaths = client.getChildren().forPath(parentPath);
                for (String path : loanPaths) {
                    String concatPath = parentPath.concat("/").concat(path);
                    hasResource += client.getData().forPath(concatPath) == null ? 0 : Integer.valueOf(new String(client.getData().forPath(concatPath)));
                }
            }
        }

        if (hasResource >= maxNum) {
            return 0;
        }

        return maxNum - hasResource;
    }
}
