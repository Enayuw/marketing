package com.br.marketing.task.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.ZookeeperPath;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.service.*;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.task.service.ITaskService;
import com.br.marketing.vo.ConfigByApiCodeVO;
import com.br.marketing.vo.CustomerScoreRuleVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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

    @Autowired
    MarketingTaskService marketingTaskService;

    @Autowired
    MarketingCustomerMapper marketingCustomerMapper;

    @Value("${spring.profiles.active}")
    String env;

    @Value("${cluster.flag}")
    private String clusterConfig;

    @Autowired
    ICompatibleService iCompatibleService;

    @Override
    public void buildScoreTask(List<Long> scoreRuleIds,String jobNm) {
        Result<List<CustomerScoreRuleVO>> scoreConfigNow = iRuleConfigService.getScoreConfigNow(scoreRuleIds);
//        AssertResult.assertResult(scoreConfigNow);
        if(!ResultCode.SUCCESS.getValue().equals(scoreConfigNow.getCode())){
            return;
        }
        List<CustomerScoreRuleVO> data = scoreConfigNow.getData();
        for (CustomerScoreRuleVO datum : data) {
            MarketingCustomerExample customerExample = new MarketingCustomerExample();
            customerExample.createCriteria().andApiCodeEqualTo(datum.getApiCode()).andStatusEqualTo(new Byte("1"));
            List<MarketingCustomer> marketingCustomers = marketingCustomerMapper.selectByExample(customerExample);
            if(marketingCustomers.size()<0){
                continue;
            }
            MarketingCustomer customer = marketingCustomers.get(0);
            Boolean action = iCompatibleService.isAction(customer.getExtendConfigInfo(),jobNm);
            if(!action){
                continue;
            }
//            if (datum.getParentId() <= 0) {
                marketingTaskService.buildScoreTaskOfAuto(datum);
//            } else {
//                buildScoreTaskOfSelect(datum);
//            }
        }
    }

    /**
     *
     * @param nowDay
     * @param taskId 任务id
     * @param isTimeLimit 是否筛选运行时间（HH:mm）小于等于当前时间的任务 null-不筛选；有值则筛选；
     * @return
     *
     * 1、从zk中获取当前所有正在运行的线程数量
     * 2、给获取到的任务 加锁
     *  2.1、判断 跑分记录表中的状态 是否未跑过，存在的话是否是中断状态
     *  2.2、移除锁状态
     */
    @Override
    public Result<MarketingTask> getScoreTask(String nowDay, Long taskId,Integer isTimeLimit,String jobNm) {
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
        String hm= isTimeLimit==null?LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")):null;
        List<MarketingTask> scoreTasks = marketingTaskMapper.getScoreTasks(nowDay, taskId,hm);

        for (MarketingTask scoreTask : scoreTasks) {
            MerchantParam merchantParam = RpcClientProxy.getMerchantParam(scoreTask.getApiCode());
            if (merchantParam == null) {
                log.error("用户中心结果为空"+scoreTask.getApiCode());
                continue;
            }

            String s = UUID.randomUUID().toString();
            boolean taskLock = getTaskLock(scoreTask, s);
            if (!taskLock) {
                continue;
            }

            Result<TaskStatus> taskStatusResult = canScore(scoreTask, nowDay,jobNm);
            if (!ResultCode.SUCCESS.getValue().equals(taskStatusResult.getCode())) {
                removeTaskLock(scoreTask, s);
                continue;
            }
            TaskStatus statusData = taskStatusResult.getData();
            if (statusData != null) {
                TaskStatus updateStatus = new TaskStatus();
                updateStatus.setId(statusData.getId());
                if (scoreTask.getMonitorType().equals(1)||scoreTask.getMonitorType().equals(2)) {
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
                if (scoreTask.getMonitorType().equals(1)||scoreTask.getMonitorType().equals(2)) {
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

        return new Result<>().setCode(ResultCode.FAIL.getValue());
    }

    /**
     * 2-暂停；3-恢复跑分；
     *
     * @param task
     * @return
     */
    private Result<TaskStatus> canScore(MarketingTask task, String nowDay,String jobNm) {

        MarketingCustomerExample customerExample = new MarketingCustomerExample();
        customerExample.createCriteria().andApiCodeEqualTo(task.getApiCode()).andStatusEqualTo(new Byte("1"));
        List<MarketingCustomer> marketingCustomers = marketingCustomerMapper.selectByExample(customerExample);
        if(marketingCustomers.size()<=0){
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }
        MarketingCustomer customer = marketingCustomers.get(0);
        Boolean action = iCompatibleService.isAction(customer.getExtendConfigInfo(),jobNm);
        if(!action){
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }
        //一次行跑分、规则验证、离线跑批 都判断状态表种的 oncestatus状态来判定任务是否已经跑过
        if (1 == task.getMonitorType()||2==task.getMonitorType()) {
            //region 一次性跑分
            TaskStatusExample statusExample = new TaskStatusExample();
            statusExample.createCriteria().andBatchNumberEqualTo(task.getBatchNumber());
            List<TaskStatus> bts = taskStatusMapper.selectByExample(statusExample);
            if (bts.size() > 0 && bts.get(0).getOnceStatus().equals(3)) {
                return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(bts.get(0));
            }
            if (bts.size() > 0) {
                return new Result<>().setCode(ResultCode.FAIL.getValue());
            }
            return new Result<>().setCode(ResultCode.SUCCESS.getValue());
            //endregion
        } else if (4 == task.getMonitorType()||3 == task.getMonitorType()) {
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
        return redisChgService.setnx(key, lockValue, 10);
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
                    hasResource += client.getData().forPath(concatPath) == null ? 0 : Integer.parseInt(new String(client.getData().forPath(concatPath)));
                }
            }
        }

        if (hasResource >= maxNum) {
            return 0;
        }

        return maxNum - hasResource;
    }
}
