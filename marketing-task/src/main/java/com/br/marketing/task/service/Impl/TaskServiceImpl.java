package com.br.marketing.task.service.Impl;

import com.alibaba.fastjson.JSON;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.ZookeeperPath;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.entity.MarketingCustomerExample;
import com.br.marketing.entity.MarketingTask;
import com.br.marketing.entity.MarketingTaskExample;
import com.br.marketing.entity.MerchantParam;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.entity.StraHisFileExample;
import com.br.marketing.entity.TaskStatus;
import com.br.marketing.entity.TaskStatusExample;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.MarketingTaskExtendMapper;
import com.br.marketing.mapper.MarketingTaskMapper;
import com.br.marketing.mapper.ScoreRuleConfigMapper;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.mapper.TaskBatchnumberPreMapper;
import com.br.marketing.mapper.TaskStatusMapper;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.service.IApiToDbService;
import com.br.marketing.service.ICompatibleService;
import com.br.marketing.service.IDynamicSqlService;
import com.br.marketing.service.IRuleConfigService;
import com.br.marketing.service.Impl.EntityOptServiceImpl;
import com.br.marketing.service.MarketingTaskService;
import com.br.marketing.service.SoleStrategyService;
import com.br.marketing.service.MarketingTaskOptService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.task.service.ITaskService;
import com.br.marketing.vo.CustomerScoreRuleVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Resource
    StraHisFileMapper straHisFileMapper;

    @Autowired
    EntityOptServiceImpl entityOptService;

    @Autowired
    MarketingTaskOptService marketingTaskOptService;

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
            if(marketingCustomers.size()<=0){
                continue;
            }
            MarketingCustomer customer = marketingCustomers.get(0);
            Boolean action = iCompatibleService.isAction(customer.getExtendConfigInfo(),jobNm);
            if(!action){
                continue;
            }
//            if (datum.getParentId() <= 0) {
                marketingTaskService.buildScoreTaskOfAutoBuild(datum);
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
        int resource = 0;

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

            Result<TaskStatus> taskStatusResult = canScore(scoreTask,jobNm);
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

    @Override
    public void JumpQueuehandle() {
        LocalDate nowDate = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        String nowTimeStr = now.format(DateTimeFormatter.ofPattern("HH:mm"));
        MarketingTaskExample marketingTaskExample = new MarketingTaskExample();
        marketingTaskExample.createCriteria().andStatusEqualTo(1).andPriorityEqualTo(0)
                .andStartDateEqualTo(nowDate.toString()).andStartTimeLessThanOrEqualTo(nowTimeStr);
        List<MarketingTask> marketingTasks = marketingTaskMapper.selectByExample(marketingTaskExample);

        List<MarketingTask> highPriorityTasks = marketingTasks.stream().filter((MarketingTask task) -> {
            if (task.getMonitorType() >= 1 && task.getMonitorType() <= 4) {
                TaskStatusExample statusExample = new TaskStatusExample();
                statusExample.createCriteria().andBatchNumberEqualTo(task.getBatchNumber());
                List<TaskStatus> bts = taskStatusMapper.selectByExample(statusExample);
                if (bts.size() > 0 && ((Objects.equals(bts.get(0).getOnceStatus(),3)) || (Objects.equals(bts.get(0).getAllStatus(), 3)))) {
                    return true;
                }
                return bts.size() <= 0;
            }
            return false;
        }).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(highPriorityTasks)) {
            log.warn("暂停优先级非0任务，没有查询到0优先级任务");
            return;
        }

        StraHisFileExample straHisFileExample = new StraHisFileExample();
        straHisFileExample.createCriteria().andStatusEqualTo(3);
        List<StraHisFile> straHisFiles = straHisFileMapper.selectByExample(straHisFileExample);

        if (CollectionUtils.isEmpty(straHisFiles)) {
            log.warn("暂停优先级非0任务，没有查询到正在进行中任务");
            return;
        }

        Integer numberOfScoreTaskNodes = marketingCommonConfig.getNumberOfScoreTaskNodes();
        numberOfScoreTaskNodes = numberOfScoreTaskNodes == null ? 4 : numberOfScoreTaskNodes;

        // 判断跑分中任务数量和分片数是否相等
        if (straHisFiles.size() != numberOfScoreTaskNodes) {
            log.warn("暂停优先级非0任务，跑分中任务数量和分片数不相等");
            return;
        }

        // 获取正在跑分中的优先级非0的任务.条件：优先级非0，且任务类型非一次性验证，且is_online为在线跑分
        List<String> batchNumbers = straHisFiles.stream().map(StraHisFile::getBatchNumber).collect(Collectors.toList());
        MarketingTaskExample runningTaskExample = new MarketingTaskExample();
        runningTaskExample.createCriteria().andBatchNumberIn(batchNumbers).andPriorityNotEqualTo(0)
                .andMonitorTypeNotEqualTo(2).andIsOnlineEqualTo(1);
        runningTaskExample.setOrderByClause("priority desc, start_date desc");

        List<MarketingTask> runningTasks = marketingTaskMapper.selectByExample(runningTaskExample);
        if (CollectionUtils.isEmpty(runningTasks)) {
            log.warn("暂停优先级非0任务，没有查询到正在跑分中的非0任务");
            return;
        }

        if (highPriorityTasks.size() >= runningTasks.size()) {
            pauseTasks(runningTasks, straHisFiles);
        } else {
            List<MarketingTask> pauseTasks = runningTasks.stream().limit(highPriorityTasks.size()).collect(Collectors.toList());
            pauseTasks(pauseTasks, straHisFiles);
        }
    }

    /**
     * 暂停正在跑分中的非0优先级任务
     * @param runningTasks
     * @param straHisFiles
     */
    private void pauseTasks(List<MarketingTask> runningTasks, List<StraHisFile> straHisFiles) {
        for (MarketingTask runningTask : runningTasks) {
            Optional<StraHisFile> first =
                    straHisFiles.stream().filter((StraHisFile straHisFile) -> straHisFile.getBatchNumber()
                            .equals(runningTask.getBatchNumber())).findFirst();

            if (!first.isPresent()) {
                continue;
            }
            pauseTask(first.get());
        }
    }

    private void pauseTask(StraHisFile straHisFileNeedPause) {
        TaskStatusExample statusExample = new TaskStatusExample();
        statusExample.createCriteria().andFileIdEqualTo(straHisFileNeedPause.getId().intValue());
        List<TaskStatus> taskStatuses = taskStatusMapper.selectByExample(statusExample);
        if (CollectionUtils.isEmpty(taskStatuses)) {
            log.warn("跑分执行状态表中未找到该跑分任务。跑分编号：{}",straHisFileNeedPause.getBatchNumber());
            return;
        }
        TaskStatus taskStatus = taskStatuses.get(0);

        Result result = marketingTaskOptService.pauseTaskByStraHisFile(2, straHisFileNeedPause, taskStatus);
        if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            log.warn(result.getMessage() + "。跑分编号：{}", straHisFileNeedPause.getBatchNumber());
        }
    }

    /**
     * 2-暂停；3-恢复跑分；
     *
     * @param task
     * @return
     */
    private Result<TaskStatus> canScore(MarketingTask task, String jobNm) {

        MarketingCustomerExample customerExample = new MarketingCustomerExample();
        customerExample.createCriteria().andApiCodeEqualTo(task.getApiCode()).andStatusEqualTo(new Byte("1"));
        List<MarketingCustomer> marketingCustomers = marketingCustomerMapper.selectByExample(customerExample);
        if(marketingCustomers.size()<=0){
            log.warn("跑分任务执行，marketingCustomers为空，{}", JSON.toJSONString(task));
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }
        MarketingCustomer customer = marketingCustomers.get(0);
        Boolean action = iCompatibleService.isAction(customer.getExtendConfigInfo(),jobNm);
        if(!action){
            log.warn("跑分任务执行，!action，{}", JSON.toJSONString(task));
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }

        // 根据跑分状态表判断任务是否已经跑过
        // 一次行全量、一次性验证判断onceStatus;每个任务的周期、每日定时判断allStatus
        if (task.getMonitorType() >= 1 && task.getMonitorType() <= 4) {
            TaskStatusExample statusExample = new TaskStatusExample();
            statusExample.createCriteria().andBatchNumberEqualTo(task.getBatchNumber());
            List<TaskStatus> bts = taskStatusMapper.selectByExample(statusExample);
            if (bts.size() > 0 && ((Objects.equals(3,bts.get(0).getOnceStatus())) || (Objects.equals(3,bts.get(0).getAllStatus())))) {
                return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(bts.get(0));
            }
            if (bts.size() > 0) {
                log.warn("跑分任务执行，task_status已存在，{}", JSON.toJSONString(task));
                return new Result<>().setCode(ResultCode.FAIL.getValue());
            }
            return new Result<>().setCode(ResultCode.SUCCESS.getValue());
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

    private int getResource() throws Exception {
        int hasResource = 0;
        int maxNum = marketingCommonConfig.getTaskResourceMaxNum() == null ? 300 : marketingCommonConfig.getTaskResourceMaxNum();
        List<String> parentPaths = Arrays.asList(ZookeeperPath.loanPath, ZookeeperPath.marketPath);
        for (String parentPath : parentPaths) {
            if (client.checkExists().forPath(parentPath) != null) {
                List<String> loanPaths = client.getChildren().forPath(parentPath);
                for (String path : loanPaths) {
                    String concatPath = parentPath.concat("/").concat(path);
                    hasResource += client.getData().forPath(concatPath) == null ?
                            0 : Integer.parseInt(new String(client.getData().forPath(concatPath)));
                }
            }
        }

        if (hasResource >= maxNum) {
            return 0;
        }

        return maxNum - hasResource;
    }
}
