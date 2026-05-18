package com.br.marketing.task.service.Impl;

import com.alibaba.fastjson.JSON;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.ZookeeperPath;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.MarketingTaskStatusEnum;
import com.br.marketing.common.enums.RedisValueTypeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.score.ProductCatalogValidationResult;
import com.br.marketing.enums.ScoreStatusEnum;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.service.*;
import com.br.marketing.service.Impl.EntityOptServiceImpl;
import com.br.marketing.service.Impl.datagroup.DataGroupHandlerServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.task.dto.ScoreTaskBatchDTO;
import com.br.marketing.task.service.ITaskService;
import com.br.marketing.task.service.ScoreBatchExpirePolicyService;
import com.br.marketing.vo.CustomerScoreRuleVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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

    @Resource
    DataGroupHandlerServiceImpl dataGroupHandlerService;

    @Resource
    MarketingRetryRedisMapper marketingRetryRedisMapper;

    @Resource
    private ScoreBatchExpirePolicyService scoreBatchExpirePolicyService;

    @Resource
    private ProductCatalogValidationService productCatalogValidationService;

    @Resource
    private MarketingTaskModelCheckMapper marketingTaskModelCheckMapper;


    @Override
    public void buildScoreTask(List<Long> scoreRuleIds, String jobNm) {
        Result<List<CustomerScoreRuleVO>> scoreConfigNow = iRuleConfigService.getScoreConfigNow(scoreRuleIds, null);
//        AssertResult.assertResult(scoreConfigNow);
        if (!ResultCode.SUCCESS.getValue().equals(scoreConfigNow.getCode())) {
            return;
        }
        List<CustomerScoreRuleVO> data = scoreConfigNow.getData();
        for (CustomerScoreRuleVO datum : data) {
            MarketingCustomerExample customerExample = new MarketingCustomerExample();
            customerExample.createCriteria().andApiCodeEqualTo(datum.getApiCode()).andStatusEqualTo(new Byte("1"));
            List<MarketingCustomer> marketingCustomers = marketingCustomerMapper.selectByExample(customerExample);
            if (marketingCustomers.size() <= 0) {
                continue;
            }
            MarketingCustomer customer = marketingCustomers.get(0);
            Boolean action = iCompatibleService.isAction(customer.getExtendConfigInfo(), jobNm);
            if (!action) {
                continue;
            }
            String value = UUID.randomUUID().toString();
            try {
                //加锁-跑分配置获取最新
                dataGroupHandlerService.addLockGroupScoreConfig(datum.getApiCode(), value);
                ScoreRuleConfig scoreRuleConfig = scoreRuleConfigMapper.selectByPrimaryKey(datum.getId());
                datum.setBaseInfo(scoreRuleConfig.getBaseInfo());
                marketingTaskService.buildScoreTaskOfAutoBuild(datum);
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "后台生成手动规则以及任务异常"), e);
            } finally {
                dataGroupHandlerService.unlockGroupScoreConfig(datum.getApiCode(), value);
            }
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

        // 先处理异常重试跑分数据
        MarketingRetryRedisExample marketingRetryRedisExample = new MarketingRetryRedisExample();
        marketingRetryRedisExample.createCriteria()
                .andApiCodeEqualTo(task.getApiCode())
                .andBatchNumberEqualTo(task.getBatchNumber())
                .andRetryStatusEqualTo(0);
        List<MarketingRetryRedis> marketingRetryRedis = marketingRetryRedisMapper.selectByExample(marketingRetryRedisExample);
        if (!CollectionUtils.isEmpty(marketingRetryRedis)) {
            int scoreBatchExpireSeconds = scoreBatchExpirePolicyService.resolveAndEnsureExpireDay(customer);
            for (MarketingRetryRedis retryRedis : marketingRetryRedis) {
                String key = retryRedis.getRedisKey();
                boolean success = retrySetRedisOrDisableTask(retryRedis, String.valueOf(task.getFileId()),
                        retryRedis.getPage(), scoreBatchExpireSeconds);
                if (!success) {
                    log.error("重试Redis异常，任务已暂停，后续流程不再执行，fileId={}, page={}", task.getBatchNumber(), retryRedis.getPage());
                    return new Result<>().setCode(ResultCode.FAIL.getValue());
                }
                // 成功则更新状态
                MarketingRetryRedis retryRedis1 = new MarketingRetryRedis();
                retryRedis1.setRetryStatus(1);
                retryRedis1.setId(retryRedis.getId());
                marketingRetryRedisMapper.updateByPrimaryKeySelective(retryRedis1);
            }
        }

        ProductCatalogValidationResult catalogValidation = productCatalogValidationService.validate(task);
        if (!catalogValidation.isPassed()) {
            log.error("跑分任务产管产品目录校验未通过,batchNumber={},taskId={},detail={}",
                    task.getBatchNumber(), task.getId(), JSON.toJSONString(catalogValidation.getFailedItems()));
            MarketingTask taskUpd = new MarketingTask();
            taskUpd.setId(task.getId());
            taskUpd.setStatus(MarketingTaskStatusEnum.DISABLED.getValue());
            marketingTaskMapper.updateByPrimaryKeySelective(taskUpd);
            updateStraHisFileStatusOnCatalogValidationFailure(task);
            persistProductCatalogValidationFailure(task, catalogValidation);
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

    private void updateStraHisFileStatusOnCatalogValidationFailure(MarketingTask task) {
        try {
            StraHisFile straHisFile = null;

            if (StringUtils.isNotBlank(task.getBatchNumber())) {
                StraHisFileExample example = new StraHisFileExample();
                example.createCriteria().andBatchNumberEqualTo(task.getBatchNumber());
                List<StraHisFile> files = straHisFileMapper.selectByExample(example);
                if (!CollectionUtils.isEmpty(files)) {
                    straHisFile = files.get(0);
                }
            }
            if (straHisFile == null) {
                log.warn("产管校验未通过，未找到对应跑分记录stra_his_file,batchNumber={},taskId={}",
                        task.getBatchNumber(), task.getId());
                return;
            }
            StraHisFile updateFile = new StraHisFile();
            updateFile.setId(straHisFile.getId());
            updateFile.setStatus(ScoreStatusEnum.PAUSEED.getValue());
            updateFile.setUpdateTime(new Date());
            straHisFileMapper.updateByPrimaryKeySelective(updateFile);
        } catch (Exception e) {
            log.error("产管校验未通过，更新stra_his_file状态失败,batchNumber={},taskId={}",
                    task.getBatchNumber(), task.getId(), e);
        }
    }

    private void persistProductCatalogValidationFailure(MarketingTask task, ProductCatalogValidationResult catalogValidation) {
        try {
            if (StringUtils.isBlank(task.getBatchNumber())) {
                insertProductCatalogValidationFailureRow(task, catalogValidation);
            } else {
                MarketingTaskModelCheckExample existExample = new MarketingTaskModelCheckExample();
                existExample.createCriteria().andBatchNumberEqualTo(task.getBatchNumber());
                if (marketingTaskModelCheckMapper.countByExample(existExample) == 0) {
                    insertProductCatalogValidationFailureRow(task, catalogValidation);
                }
            }
        } catch (Exception e) {
            log.error("写入产管校验结果表失败,batchNumber={}", task.getBatchNumber(), e);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("任务批次：%s；产管产品/版本与当前许可不一致，任务已标记为禁用(status=%d)。\r\n",
                task.getBatchNumber(), MarketingTaskStatusEnum.DISABLED.getValue()))
                .append("明细：").append(JSON.toJSONString(catalogValidation.getFailedItems()));
        log.warn("{}", sb);
    }

    private void insertProductCatalogValidationFailureRow(MarketingTask task, ProductCatalogValidationResult catalogValidation) {
        MarketingTaskModelCheck record = new MarketingTaskModelCheck();
        record.setApiCode(task.getApiCode());
        record.setBatchNumber(task.getBatchNumber());
        record.setCusBatch(task.getCusBatch());
        fillModelCheckRuleFieldsFromScoreRuleConfig(task, record);
        record.setModelCheckStatus(0);
        record.setFailedModelInfo(JSON.toJSONString(catalogValidation.getFailedItems()));
        record.setIsDel(1);
        record.setCreateTime(new Date());
        marketingTaskModelCheckMapper.insertSelective(record);
    }

    private void fillModelCheckRuleFieldsFromScoreRuleConfig(MarketingTask task, MarketingTaskModelCheck record) {
        MarketingTaskExtendExample extendExample = new MarketingTaskExtendExample();
        extendExample.createCriteria().andIsDelEqualTo(1).andTaskIdEqualTo(task.getId());
        List<MarketingTaskExtend> extendList = marketingTaskExtendMapper.selectByExample(extendExample);
        if (extendList == null || extendList.isEmpty()) {
            log.warn("未找到任务扩展，无法写入产管校验规则名称，taskId={}", task.getId());
            return;
        }
        MarketingTaskExtend extend = extendList.get(0);
        if (extend.getRuleId() == null) {
            log.warn("任务扩展无 ruleId，taskId={}", task.getId());
            return;
        }
        ScoreRuleConfig rule = scoreRuleConfigMapper.selectByPrimaryKey(extend.getRuleId());
        if (rule == null) {
            log.warn("未找到跑分规则配置，ruleId={}", extend.getRuleId());
            return;
        }
        record.setRuleName(rule.getRuleName());
        record.setRuleNameShort(rule.getRuleNameShort());
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
        List<String> parentPaths = Arrays.asList(ZookeeperPath.marketPath);
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

    /**
     * 尝试写入Redis，失败重试3次，失败后暂停任务并跳出外层循环
     */
    private boolean retrySetRedisOrDisableTask(MarketingRetryRedis retryRedis, String fileId,
                                               String page, int scoreBatchExpireSeconds) {
        String key = retryRedis.getRedisKey();
        String redisValueType = retryRedis.getRedisValueType();
        int retryCount = 0;
        while (retryCount < 3) {
            try {
                // 模拟重试redis异常
                checkMockRedisSwitch("retryRedis");

                if(RedisValueTypeEnum.String.getValue().equals(redisValueType)) {
                    redisChgService.set(key, retryRedis.getRedisValue());
                }else if(RedisValueTypeEnum.Set.getValue().equals(redisValueType)) {
                    redisChgService.saddMember(key, retryRedis.getRedisValue());
                }else if(RedisValueTypeEnum.Hash.getValue().equals(redisValueType)) {
                    ScoreTaskBatchDTO scoreTaskBatchDTO = JSON.parseObject(retryRedis.getRedisValue(), ScoreTaskBatchDTO.class);
                    redisChgService.hset(key, String.valueOf(scoreTaskBatchDTO.getPreId()), JSON.toJSONString(scoreTaskBatchDTO));
                } else {
                    redisChgService.set(key, "1");
                }
                redisChgService.expire(key, scoreBatchExpireSeconds);
                // 成功
                return true;
            } catch (Exception e) {
                retryCount++;
                if (retryCount >= 3) {
                    log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SUCCESS_UPLOAD.getCode(),
                            String.format("跑分异常，Redis异常重试写入失败3次，RedisKey=%s, fileId=%s, page=%s", key, fileId, page), e.getMessage()));
                    return false;
                } else {
                    try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                }
            }
        }
        return false;
    }

    private void checkMockRedisSwitch(String key) throws Exception {
        Map<String, Boolean> mockRedisSwitch =  marketingCommonConfig.getMockRedisSwitch();
        if(!CollectionUtils.isEmpty(mockRedisSwitch)){
            if(mockRedisSwitch.get(key)){
                throw new Exception();
            }
        }
    }

}
