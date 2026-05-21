package com.br.marketing.task.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.common.log.AlertLog;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.ZookeeperPath;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.constants.rocketmq.MarketingAssistConstants;
import com.br.marketing.common.customizedassert.AssertResult;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.RedisValueTypeEnum;
import com.br.marketing.common.enums.TaskTypeEnum;
import com.br.marketing.common.exception.BusinessException;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.dto.TaskExtendExtendFieldDTO;
import com.br.marketing.entity.*;
import com.br.marketing.enums.DingDingAlarmFunctionEnum;
import com.br.marketing.enums.ScoreStatusEnum;
import com.br.marketing.enums.ScoreThreeKeyEncryptEnum;
import com.br.marketing.enums.ZkScoreStatusEnum;
import com.br.marketing.mapper.*;
import com.br.marketing.monitor.PrometheusMonitorUtils;
import com.br.marketing.service.*;
import com.br.marketing.service.Impl.StrategyCs;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.task.dto.ObservedTaskObj;
import com.br.marketing.task.dto.ScoreTaskBatchDTO;
import com.br.marketing.task.service.ScoreBatchExpirePolicyService;
import com.br.marketing.task.thread.CoreScoreThread;
import com.br.marketing.util.BrMonitorExecutor;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.br.marketing.vo.BaseHead;
import com.br.marketing.vo.BaseHeadConfigVO;
import com.br.marketing.vo.StrategyProductDetailVO;
import com.br.marketing.webhook.dingding.service.DingDingRobotHookService;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.IteratorUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 单任务多片跑分
 */
@Service
@Slf4j
public class TaskScoreServiceImpl {
    @Value("${otherConfig.warning.pageSize:00}")
    private Integer pageSize;
    @Autowired
    SyncConfigService syncConfigService;
    @Value("${otherConfig.mom.appSecretKey:00}")
    private String appSecretKey;

    @Value("${otherConfig.huaXiangInterface.getReport:00}")
    private String url;

    @Resource
    private AlarmApiClient alarmClient;
    @Value("${otherConfig.alarm.outsideSecretKey:00}")
    private String secretKey;
    @Value("${otherConfig.alarm.outsideAppName:00}")
    private String appName;

    @Resource
    MarketingSepService marketingSepService;
    @Resource
    TaskStatusMapper taskStatusMapper;
    @Resource
    StrategyCs strategyCS;
    @Resource
    RedisChgService redisChgService;
    @Resource
    MarketingStrategyProductMapper marketingStrategyProductMapper;
    @Resource
    ApicodeScoreProductMapper apicodeScoreProductMapper;
    @Resource
    MarketingTaskExtendService marketingTaskExtendService;
    @Resource
    ScoreRuleConfigService scoreRuleConfigService;

    @Autowired
    StraHisFileMapper straHisFileMapper;

    @Autowired
    IProductResultSimpleService iProductResultSimpleService;

    private final static String RedisEsOpen = "es:open";
    @Autowired
    ObservedScoreThreadServiceImpl observedScoreThreadService;

    final static Integer allMonitorType = 4;

    final static String RedisCodeProduct = "apicodescore:product:";
    static ConcurrentHashMap<String, Integer> threadContextNum = new ConcurrentHashMap<>();

    private static final int GROUP_MAX_NUM = 1000;

    @Autowired
    private CuratorFramework client;

    @Resource
    MarketingCustomerMapper marketingCustomerMapper;

    @Autowired
    IDynamicSqlService iDynamicSqlService;

    @Autowired
    MarketingTaskService marketingTaskService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Autowired
    private DingDingRobotHookService dingDingRobotHookService;

    @Resource
    private MarketingRetryEsMapper marketingRetryEsMapper;
    @Resource
    MarketingRetryRedisMapper marketingRetryRedisMapper;

    @Resource
    private ScoreBatchExpirePolicyService scoreBatchExpirePolicyService;

    @Resource
    private RocketMqSwitch rocketMqSwitch;

    private static final String TITLE = "【跑分监控】";

    /**
     * 跑分服务
     *
     * @param task 执行的任务
     * @param day  执行的日期
     */
    public void process(MarketingTask task, String day) {
        String apiCode = task.getApiCode();
        Boolean isOffline = task.getIsOnline().equals(2);
        MarketingCustomerExample customerExample = new MarketingCustomerExample();
        customerExample.createCriteria().andApiCodeEqualTo(apiCode).andStatusEqualTo(new Byte("1"));
        List<MarketingCustomer> marketingCustomers = marketingCustomerMapper.selectByExample(customerExample);

        if (marketingCustomers.size() <= 0) {
            return;
        }
        MarketingCustomer customer = marketingCustomers.get(0);
        if (customer.getThreadNum() == null) {
            customer.setThreadNum(20);
        }
        int scoreBatchExpireSeconds = scoreBatchExpirePolicyService.resolveAndEnsureExpireDay(customer);
        //线程池调用使用线程池监控调用类
        ThreadPoolExecutor warrningExecutor = BrMonitorExecutor.getThreadPool(customer.getThreadNum(), customer.getThreadNum(),
                PrometheusMonitorUtils.COUNT_CORE_SCORE_API_THREAD_METRIC_NAME,apiCode,task.getBatchNumber());

        //线程监听
        ObservedTaskObj observedTaskObj = new ObservedTaskObj(warrningExecutor, task);

        //线程池注册
        observedScoreThreadService.addObserver(observedTaskObj);

        threadNumListen(warrningExecutor, customer, task);

        try {

            //线程池运行情况报告
            Thread thread = threadReport(warrningExecutor, customer);
            log.warn(TITLE + "跑分任务generateTask，本次调度任务id：{}",task.getBatchNumber());

            // 2. 只有全部重试都成功，才执行generateTask
            this.generateTask(observedTaskObj, customer, day, scoreBatchExpireSeconds);
            /**
             * 等待所有任务都执行完成
             **/
            log.warn(TITLE + "所有任务已加入队列，等待结束-----"+task.getBatchNumber());
            warrningExecutor.shutdown();
            while (true) {
                if (warrningExecutor.isTerminated()) {
                    observedScoreThreadService.removeThread(observedTaskObj);
                    log.warn(TITLE + "所有线程都执行结束");
                    break;
                }
                try {
                    Thread.sleep(6000);
                } catch (Exception e) {
                }
            }
            //endregion

            if (task.getFileId() == null || task.getFileId() <= 1) {
                return;
            }

            //region 重试
            try {
                String hkey = Constants.HXRESULTERROR_RETRY_KEY + ":" + task.getFileId();
                List<String> hkeys = redisChgService.hkeys(hkey);
                if (!hkeys.isEmpty() && hkeys.size() > 0) {
                    warrningExecutor = BrMonitorExecutor.getThreadPool(20, 20,
                            PrometheusMonitorUtils.COUNT_RETRY_SCORE_API_THREAD_METRIC_NAME,apiCode,task.getBatchNumber());

                    int i = 1;
                    for (String errorFile : hkeys) {
                        if (task != null) {
                            this.retry(task, errorFile, warrningExecutor, i, customer, scoreBatchExpireSeconds);
                            i++;
                        }
                    }
                    log.warn(TITLE + "所有重试任务已加入队列，等待结束-----" +task.getBatchNumber());
                    warrningExecutor.shutdown();
                    while (true) {
                        if (warrningExecutor.isTerminated()) {
                            log.warn(TITLE + "重试任务所有线程都执行结束");
                            break;
                        }
                        try {
                            Thread.sleep(6000);
                        } catch (Exception e) {
                        }
                    }
                    redisChgService.del(hkey);
                }
            } catch (Exception e) {
                log.error("重新处理异常数据出错", e);
            }
            //endregion
            //画像返回异常数据统计进行钉钉告警
            String errorResultKey = RedisKeyConstant.TASKSCORE_HXRESULTERROR.concat(":").concat(apiCode).concat(":").concat(task.getId().toString());
            List<String> errorkeys = redisChgService.hkeys(errorResultKey);
            if (!CollectionUtils.isEmpty(errorkeys)) {
                Map<String, JSONObject> webHookInfo = marketingCommonConfig.getDingDingWebHookInfo();
                Map<String, Object> map = webHookInfo.get(DingDingAlarmFunctionEnum.TASKSCORE_HXRESULT_ERROR_MESSAGE.toString());
                if (CollectionUtils.isEmpty(map)) {
                    log.error(TITLE + "跑分结果异常告警统计，钉钉配置未配置，请检查");
                }
                String contentHeld = apiCode + "_" + LocalDate.now().toString()+"_任务编号="+task.getBatchNumber()+"_" + "跑分结果异常统计\n";
                String content = "跑分总量级:" + task.getTaskNumber() + "\n";
                Map<String, Object> resultMap = redisChgService.hgetall(errorResultKey);
                for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
                    content = content.concat(entry.getKey()).concat(": ").concat(entry.getValue().toString()).concat("条 \n");
                }
                dingDingRobotHookService.sendDingDingTextMessage(contentHeld + content, map);
            }
            //region 任务状态表和任务记录表的更新
            TaskStatus updateStatus = new TaskStatus();
            updateStatus.setId(task.getStatusId());
            if (task.getMonitorType().equals(1) || task.getMonitorType().equals(2)) {
                updateStatus.setOnceStatus(observedTaskObj.getInterrupt().equals(1) ? 4 : 2);
            } else {
                updateStatus.setAllStatus(observedTaskObj.getInterrupt().equals(1) ? 4 : 2);
            }
            taskStatusMapper.updateByPrimaryKeySelective(updateStatus);
            StraHisFile updateFile = new StraHisFile();
            updateFile.setId(task.getFileId());
            if (observedTaskObj.getInterrupt().equals(0)) {
                // 离线
                if (isOffline) {
                    updateFile.setStatus(ScoreStatusEnum.OFFLINEMERGE.getValue());
                    updateFile.setIndexNum(marketingTaskService.getPartNum(task.getTaskNumber()));
                    straHisFileMapper.updateByPrimaryKeySelective(updateFile);
//                    producter.send(MQConstants.ROUTING_KEY_PUSHTASK_FILE_MERGE, task.getFileId().toString());
                    rocketMqSwitch.sendMessage(apiCode, MarketingAssistConstants.TOPIC
                            , MarketingAssistConstants.TAG_MARKETING_PUSHTASK_FILE_MERGE
                            , task.getFileId().toString(), MQConstants.ROUTING_KEY_PUSHTASK_FILE_MERGE);
                } else {
                    MarketingRetryEsExample marketingRetryEsExample = new MarketingRetryEsExample();
                    marketingRetryEsExample.createCriteria()
                            .andApiCodeEqualTo(apiCode)
                            .andRetryStatusEqualTo(0)
                            .andFileIdEqualTo(task.getFileId());
                    int i = marketingRetryEsMapper.countByExample(marketingRetryEsExample);
                    if(i == 0){
                        updateFile.setRunningEndTime(new Date());
                        updateFile.setStatus(task.getMonitorType().equals(2) ? ScoreStatusEnum.FINISH.getValue() : ScoreStatusEnum.MERGE.getValue());
                        updateFile.setIndexNum(marketingTaskService.getPartNum(task.getTaskNumber()));
                        straHisFileMapper.updateByPrimaryKeySelective(updateFile);
//                        producter.send(MQConstants.ROUTING_KEY_PUSHTASK_FILE_INITMERGE, task.getFileId().toString());
                        rocketMqSwitch.sendMessage(apiCode, MarketingAssistConstants.TOPIC
                                , MarketingAssistConstants.TAG_MARKETING_PUSHTASK_FILE_INITMERGE
                                , task.getFileId().toString(), MQConstants.ROUTING_KEY_PUSHTASK_FILE_INITMERGE);
                    }else {
                        // 存在异常数据，更新跑分记录状态为 异常待重试
                        updateFile.setStatus(ScoreStatusEnum.WAIT_RETRY.getValue());
                        straHisFileMapper.updateByPrimaryKeySelective(updateFile);
                    }
                }
            } else {
                // 当b_task_status.pause_type为2（插队暂停）时，将状态置为待恢复
                TaskStatus taskStatus = taskStatusMapper.selectByPrimaryKey(task.getStatusId());
                if (Objects.equals(taskStatus.getPauseType(), 2)) {
                    setTaskStatusToRecovered(task, taskStatus);
                }

                updateFile.setStatus(ScoreStatusEnum.PAUSEED.getValue());
                straHisFileMapper.updateByPrimaryKeySelective(updateFile);
                String content = String.format("任务编号：【%s】；\r\n 跑分记录id：【%s】；\r\n 已经暂停跑分"
                        , task.getBatchNumber(), task.getFileId().toString());
                sendContent(content, "跑分暂停", AlarmSendCodeEnum.SUCCESS_UPLOAD.getCode());
            }
            //endregion
            //删除告警统计的redis-key
            redisChgService.del(errorResultKey);
            thread.interrupt();
            removeZk(task);
        } catch (Exception e) {
            log.error("预警调度出错", e);
        }
        return;
    }

    private void setTaskStatusToRecovered(MarketingTask task, TaskStatus taskStatus) {
        if (task.getMonitorType().equals(1)) {
            log.warn(TITLE + "暂停优先级非0任务，一次性全量类型任务状态置为待恢复，跑分编号：{}", task.getBatchNumber());
            taskStatus.setOnceStatus(3);
        } else {
            log.warn(TITLE + "暂停优先级非0任务，任务状态置为待恢复，跑分编号：{}", task.getBatchNumber());
            taskStatus.setAllStatus(3);
        }
        taskStatusMapper.updateByPrimaryKeySelective(taskStatus);
    }

    /**
     * 重新处理异常数据
     *
     * @param errorFile        异常数据记录文件
     * @param warrningExecutor 线程池
     * @param num              文件编号
     */
    private void retry(MarketingTask marketingTask, String errorFile, ExecutorService warrningExecutor
            , Integer num, MarketingCustomer customer, int scoreBatchExpireSeconds) {
        String noflagproduct = redisChgService.get(RedisKeyConstant.noFlagProduct);
        List<String> noflagproductlist = new ArrayList<>();
        if (StringUtils.isNotBlank(noflagproduct)) {
            noflagproductlist = Splitter.on(",").splitToList(noflagproduct);
        } else {
            noflagproductlist.add("mappingcust");
            noflagproductlist.add("mappingcust1");
            noflagproductlist.add("mappingcust2");
            noflagproductlist.add("mappingcust3");
        }
        List<String> flagproductlist = new ArrayList<>();
        Result<List<String>> flagProduct = iProductResultSimpleService.getFlagProduct();
        if (flagProduct.getCode().equals(ResultCode.SUCCESS.getValue())) {
            flagproductlist = flagProduct.getData();
        }
        String separator = marketingSepService.querySepByApiCode(marketingTask.getApiCode());
        MarketingTaskExtend marketingTaskExtend = marketingTaskExtendService.getMarketingTaskExtend(marketingTask.getId());
        BaseHeadConfigVO baseHeadConfigVO = baseHeadHandle(marketingTaskExtend, marketingTask);
        StrategyProductDetailVO fieldInfo = JSON.parseObject(marketingTaskExtend.getStrategyProductJson(), new TypeReference<StrategyProductDetailVO>() {
        }.getType());
        StraHisFile file = straHisFileMapper.selectByPrimaryKey(Long.valueOf(marketingTask.getFileId()));
        String day = new SimpleDateFormat("yyyy-MM-dd").format(file.getCreateTime());
        String productJson = "";
        if (marketingTask.getTaskType().compareTo(new Integer(0)) == 0) {
            productJson = strategyCS.strategyIdCheck(marketingTask.getApiCode(), marketingTask.getStrategyId());
        } else if (marketingTask.getTaskType().compareTo(new Integer(1)) == 0) {
            return;
        } else if (marketingTask.getTaskType().compareTo(new Integer(2)) == 0) {
            productJson = marketingTask.getProductInfo();
        }
        if (StringUtils.isEmpty(productJson)) {
            log.error("贷中策略不可用:fileId:{} task_number：{}", marketingTask.getFileId().toString(), marketingTask.getBatchNumber());
            return;
        }
        String dateAddYyMmDd = DateHelper.getDateAddYyMmDd(0);
        String s = dateAddYyMmDd + num.toString();
        String row = null;
        Long currentPage = Long.parseLong(s);
        try (FileReader read = new FileReader(errorFile);
             BufferedReader br = new BufferedReader(read);) {
            List<MarketingSyncUser> list = new ArrayList<>();
            while ((row = br.readLine()) != null) {
                MarketingSyncUser syncUser = JSON.parseObject(row, MarketingSyncUser.class);
                list.add(syncUser);
            }
            String descPath = file.getFilePath();
            log.info("{},list:{}", errorFile, list.size());
            Map<String, String> param = new HashMap<>();
            param.put("apiCode", marketingTask.getApiCode());
            param.put("strategyId", marketingTask.getStrategyId());
            param.put("path", descPath);
            param.put("strategyStr", productJson);
            param.put("sep", separator);
            param.put("batchNumber", marketingTask.getBatchNumber());
            param.put("cusBatchNumber", marketingTask.getFileName());
            param.put("url", url);
            param.put("appSecretKey", appSecretKey);
            param.put("isRepair", marketingTask.getIsRepair());
            param.put("fileId", marketingTask.getFileId().toString());
            param.put("part", marketingTaskService.getPart(num).toString());
            param.put("straHisFileCreateTimeMillis",
                    file.getCreateTime() == null ? "" : String.valueOf(file.getCreateTime().getTime()));
            warrningExecutor.submit(new CoreScoreThread(
                    list, param, currentPage, true, customer
                    , marketingTask, noflagproductlist
                    , flagproductlist, marketingTaskExtend
                    , baseHeadConfigVO, fieldInfo, true
                    , marketingRetryEsMapper, marketingCommonConfig, marketingRetryRedisMapper, null, scoreBatchExpireSeconds));
        } catch (Exception e) {
            log.error("重新处理画像异常数据出错:{},{}", errorFile, row, e);
        }
    }

    private void generateTask(ObservedTaskObj taskObj, MarketingCustomer customer, String day, int scoreBatchExpireSeconds) {
        ExecutorService warrningExecutor = taskObj.getExecutorService();
        MarketingTask blt = taskObj.getMarketingTask();
        String productJson = "";
        if (blt.getTaskType().compareTo(TaskTypeEnum.STRATYGYDATA.getValue()) == 0) {
            productJson = strategyCS.strategyIdCheck(blt.getApiCode(), blt.getStrategyId());
        } else if (blt.getTaskType().compareTo(TaskTypeEnum.PRODUCTDATA.getValue()) == 0) {
            productJson = blt.getProductInfo();
        }
        if (!blt.getTaskType().equals(TaskTypeEnum.DIRECTDATA.getValue()) && StringUtils.isEmpty(productJson)) {
            log.error("贷中策略不可用:apiCode:{} Strategy_id：{}", blt.getApiCode(), blt.getStrategyId());
            return;
        }

        String descPath = syncConfigService.getPath().concat(Constants.monitorTypeMap.get(String.valueOf(blt.getMonitorType()))).concat("/").concat(blt.getApiCode()).concat("/")
                .concat(blt.getBatchNumber()).concat("/").concat(day);
        // 是否是恢复跑分
        boolean isRestoreFlag = false;
        //region 写入或者获取跑分记录以及状态
        if (blt.getFileId() != null && blt.getFileId() > 1) {
            isRestoreFlag = true;
            StraHisFile file = straHisFileMapper.selectByPrimaryKey(blt.getFileId());
            descPath = file.getFilePath();
            // 待恢复任务，跑分状态置为进行中
            file.setStatus(ScoreStatusEnum.RUNNING.getValue());
            straHisFileMapper.updateByPrimaryKeySelective(file);
        } else {
            StraHisFile file = new StraHisFile();
            file.setApiCode(blt.getApiCode());
            file.setBatchNumber(blt.getBatchNumber());
            file.setFilePath(descPath);
            file.setCreateTime(new Date());
            file.setUpdateTime(new Date());
            file.setExpectedNum(blt.getTaskNumber());
            file.setStatus(ScoreStatusEnum.RUNNING.getValue());
            if (1 == blt.getMonitorType() || 2 == blt.getMonitorType()) {
                file.setType(2);
            } else if (4 == blt.getMonitorType() || 3 == blt.getMonitorType()) {
                file.setType(1);
            }
//            file.setShowTitle(createShowTitle(blt));
            straHisFileMapper.insertSelective(file);
            blt.setFileId(file.getId());

            TaskStatus updateStatus = new TaskStatus();
            updateStatus.setId(blt.getStatusId());
            updateStatus.setFileId(file.getId());
            taskStatusMapper.updateByPrimaryKeySelective(updateStatus);
            log.warn(TITLE + "跑分任务TaskStatus写入完成，本次调度任务id：{}",blt.getId());

            //region 记录跑分产品
            JSONArray pList = JSONArray.parseArray(productJson);
            if (pList != null) {
                for (int i = 0; i < pList.size(); i++) {
                    JSONObject jsonObject = pList.getJSONObject(i);
                    String code = jsonObject.getString("code");
                    MarketingStrategyProduct marketingStrategyProduct = new MarketingStrategyProduct();
                    marketingStrategyProduct.setApiCode(blt.getApiCode());
                    marketingStrategyProduct.setBatchNumber(blt.getBatchNumber());
                    marketingStrategyProduct.setCreateTime(new Date());
                    marketingStrategyProduct.setCusBatchNumber(blt.getFileName());
                    marketingStrategyProduct.setProductName(code);
                    marketingStrategyProduct.setProductVersion(jsonObject.getString("version"));
                    marketingStrategyProduct.setStrategyId(blt.getStrategyId());
                    marketingStrategyProduct.setFileId(blt.getFileId());
                    marketingStrategyProductMapper.insertSelective(marketingStrategyProduct);
                    String scorekey = RedisCodeProduct.concat(blt.getApiCode());
                    String s = redisChgService.get(scorekey);
                    List<String> products = s == null
                            ? new ArrayList<>()
                            : IteratorUtils.toList(Splitter.on(",").split(s).iterator());
                    if (products.size() <= 0 || !products.contains(code)) {
                        ApicodeScoreProduct scoreProduct = new ApicodeScoreProduct();
                        scoreProduct.setApiCode(blt.getApiCode());
                        scoreProduct.setProduct(code);
                        scoreProduct.setCreateTime(new Date());
                        try {
                            apicodeScoreProductMapper.insertSelective(scoreProduct);
                            products.add(code);
                            String join = Joiner.on(",").join(products);
                            redisChgService.set(scorekey, join);
                            redisChgService.expire(scorekey, 60 * 60);
                        } catch (DuplicateKeyException keyException) {

                        } catch (Exception ex) {
                            log.error(ex.getMessage(), ex);
                        }
                    }

                }
            }
            //endregion
        }
        //endregion
        StringBuilder addTaskContent = new StringBuilder();
        addTaskContent.append(String.format("任务批次号:%s,分片:%d 加入队列", blt.getBatchNumber(), blt.getIndex()).concat("\r\n"));
        sendContent(addTaskContent.toString(), "任务开始", AlarmSendCodeEnum.SUCCESS_UPLOAD.getCode());
        scoreStatusListen(taskObj);
        core(blt, descPath, true, productJson, warrningExecutor, blt.getFileId().toString(), customer, isRestoreFlag, scoreBatchExpireSeconds);
    }

    private void sendContent(String msg, String title, String code) {
        alarmClient.sendAlarm(msg, title, code);
    }

    private BaseHeadConfigVO baseHeadHandle(MarketingTaskExtend marketingTaskExtend, MarketingTask blt) {
        BaseHeadConfigVO baseHeadConfigVO = StringUtils.isBlank(marketingTaskExtend.getExtendShowTitle())
                ? new BaseHeadConfigVO(new ArrayList<String>(), new ArrayList<BaseHead>())
                : JSON.parseObject(marketingTaskExtend.getExtendShowTitle(), new TypeReference<BaseHeadConfigVO>() {
        }.getType());
        /**
         * 离线跑批处理基础表头字段
         *  1、先判断没有配置三要素的情况 也要析出该字段信息到文件
         *  2、对于配置过三要素的情况 统一把字段的加密类型改为原值析出
         * 非离线跑批
         *  1、把相应的加密类型赋值到三要素上
         */
        if (new Integer(2).equals(blt.getIsOnline())) {
            List<String> showBaseHead = baseHeadConfigVO.getShowBaseHead();
            List<BaseHead> baseHead = baseHeadConfigVO.getBaseHead();
            iProductResultSimpleService.offLineHeadComplete(showBaseHead, baseHead);
            baseHeadConfigVO.getBaseHead().forEach(t -> {
                String key = t.getName().toLowerCase();
                if (key.equals("name") || key.equals("id") || key.equals("idcard") || key.equals("cell")) {
                    t.setThreekEncryptType(ScoreThreeKeyEncryptEnum.init.getValue());
                }
            });
        } else {
            String extendConfigInfo = marketingTaskExtend.getExtendConfigInfo();
            if (StringUtils.isNotBlank(extendConfigInfo)) {
                TaskExtendExtendFieldDTO taskExtendExtendFieldDTO1 = JSONObject.parseObject(extendConfigInfo, TaskExtendExtendFieldDTO.class);
                baseHeadConfigVO.getBaseHead().forEach(t -> {
                    String key = t.getName().toLowerCase();
                    if (key.equals("name") || key.equals("id") || key.equals("idcard") || key.equals("cell")) {
                        t.setThreekEncryptType(taskExtendExtendFieldDTO1 == null || taskExtendExtendFieldDTO1.getThreekEncryptType() == null
                                ? ScoreThreeKeyEncryptEnum.md5.getValue()
                                : taskExtendExtendFieldDTO1.getThreekEncryptType());
                    }
                });
            }
        }
        return baseHeadConfigVO;
    }

    /**
     * 提交任务
     *
     * @param blt
     * @param descPath
     */
    private void core(MarketingTask blt, String descPath, boolean firstTime, String strategyStr, ExecutorService warrningExecutor,
                      String fileId, MarketingCustomer customer, Boolean isRestoreFlag, int scoreBatchExpireSeconds) {
        try {
            String noflagproduct = redisChgService.get(RedisKeyConstant.noFlagProduct);
            List<String> noflagproductlist = new ArrayList<>();
            if (StringUtils.isNotBlank(noflagproduct)) {
                noflagproductlist = Splitter.on(",").splitToList(noflagproduct);
            } else {
                noflagproductlist.add("mappingcust");
                noflagproductlist.add("mappingcust1");
                noflagproductlist.add("mappingcust2");
                noflagproductlist.add("mappingcust3");
            }
            List<String> flagproductlist = new ArrayList<>();
            Result<List<String>> flagProduct = iProductResultSimpleService.getFlagProduct();
            if (flagProduct.getCode().equals(ResultCode.SUCCESS.getValue())) {
                flagproductlist = flagProduct.getData();
            }
            //使用任务表中的分隔符
            String separator = blt.getScoreSeparator();
            String redisOpen = redisChgService.get(RedisEsOpen);
            String esOpenMark = StringUtils.isNotBlank(redisOpen) ? redisOpen : "1";
            MarketingTaskExtend marketingTaskExtend = marketingTaskExtendService.getMarketingTaskExtend(blt.getId());
            //获取扩展表中labelName字段
            String labelName = marketingTaskExtend.getLabelName();
            //析出客户上传字段
            BaseHeadConfigVO baseHeadConfigVO = baseHeadHandle(marketingTaskExtend, blt);
            //析出画像字段
            StrategyProductDetailVO fieldInfo = JSON.parseObject(marketingTaskExtend.getStrategyProductJson(), new TypeReference<StrategyProductDetailVO>() {
            }.getType());
            StraHisFile file = straHisFileMapper.selectByPrimaryKey(Long.valueOf(fileId));
            String day = new SimpleDateFormat("yyyy-MM-dd").format(file.getCreateTime());
            //获取跑分数据筛选的条件
            Result<List<String>> dataCondition = scoreRuleConfigService.getDataCondition(marketingTaskExtend, blt, day);
            AssertResult.assertResult(dataCondition);
            List<String> conditionDatas = dataCondition.getData();
            Long currentPage = 1L;
            Integer sumNum = 0;
            long startTime = System.currentTimeMillis();
            //是否是预览跑分
            boolean isVerScore = 2 == blt.getMonitorType();
            TaskExtendExtendFieldDTO taskExtendExtendFieldDTO = JSON.parseObject(marketingTaskExtend.getExtendConfigInfo(), TaskExtendExtendFieldDTO.class);
            //预览跑分限制的条数
            Integer verNum = taskExtendExtendFieldDTO != null && taskExtendExtendFieldDTO.getDataLimit() != null && taskExtendExtendFieldDTO.getDataLimit() > 0
                    ? taskExtendExtendFieldDTO.getDataLimit() : 500;
            Boolean isHead = Boolean.TRUE;

            // 跑分条件序号
            Integer conditionIndex = 0;
            for (String conditionData : conditionDatas) {
                conditionIndex++;
                if (isVerScore && verNum <= 0) {
                    continue;
                }
                // 取分级和自适应的较小值，避免单批过大
                int totalCount = iDynamicSqlService.countByRuleScoreWithDate(blt.getApiCode(), conditionData, labelName);
                int totalPages = (int) Math.ceil((double) totalCount / 100);
                // 限定范围 1000-5000
                totalPages = Math.max(1000, Math.min(totalPages, 5000));
                Long minId = iDynamicSqlService.minIdRuleScoreWithDate(blt.getApiCode(), conditionData, labelName);
                log.warn(TITLE + "每页最小id--{},页码--{},总量级--{},每页量级--{}", minId, currentPage,totalCount, isVerScore ? verNum : totalPages);
                if (minId != null && minId > 0L) {
                    // 分组id
                    int groupId = 1;
                    // 分组中元素数量
                    int groupNum = 1;
                    String groupSetKey = RedisKeyConstant.scoreBatch.concat(":").concat(String.valueOf(blt.getFileId())).concat(":").concat(String.valueOf(conditionIndex)).concat(":group");
                    log.warn("------groupSetKey----:{}", groupSetKey);
                    Set<String> groupIdSet = new HashSet<>();
                    Map<String, ScoreTaskBatchDTO> scoreTaskBatchDTOMap = new HashMap<>();
                    if(isRestoreFlag) {
                        // 恢复跑分，初始化跑批进度
                        initScoreBatchProgressData(groupSetKey, groupIdSet, scoreTaskBatchDTOMap, fileId,
                                conditionIndex, blt, scoreBatchExpireSeconds);
                    }

                    Integer actNum = 0;
                    Long begin = 0L;
                    Boolean threadpoolStatus = Boolean.TRUE;
                    while (threadpoolStatus) {
                        if (isVerScore && verNum <= 0) {
                            threadpoolStatus = Boolean.FALSE;
                            continue;
                        }
                        // 跑分任务批次DTO
                        ScoreTaskBatchDTO scoreTaskBatchDTO = null;
                        Long minUnCompleteId = null;
                        Long maxId = null;
                        if(isRestoreFlag) {
                            // 跑分恢复时，已完成的批次跳过
                            scoreTaskBatchDTO = scoreTaskBatchDTOMap.get(String.valueOf(begin));
                            while (scoreTaskBatchDTO != null) {
                                sumNum += scoreTaskBatchDTO.getCompleteNum();
                                // 处理当时暂停时，未完全完成的批次按最小待处理记录ID和最大记录ID查询数据
                                if(scoreTaskBatchDTO.getMinUnCompleteId() != null) {
                                    currentPage = scoreTaskBatchDTO.getCurrentPage();
                                    minUnCompleteId = scoreTaskBatchDTO.getMinUnCompleteId();
                                    maxId = scoreTaskBatchDTO.getMaxId();
                                    break;
                                }
                                currentPage++;
                                // 当前分组中元素数量等于分组最大数量，要开启下一个分组，即分组ID+1，分组中元素数量重置为1
                                if(GROUP_MAX_NUM == groupNum) {
                                    groupId++;
                                    groupNum = 1;
                                }else {
                                    groupNum++;
                                }
                                begin = scoreTaskBatchDTO.getMaxId();
                                scoreTaskBatchDTOMap.remove(String.valueOf(scoreTaskBatchDTO.getPreId()));
                                scoreTaskBatchDTO = scoreTaskBatchDTOMap.get(String.valueOf(begin));

                            }
                        }
                        //获取跑分数据 预览跑分则筛选限制的剩余条数
                        List<MarketingSyncUser> list = new ArrayList<>();
                        while (true) {
                            try {
                                list = iDynamicSqlService.
                                        selectDataRuleScoreWithDate(blt.getApiCode()
                                                , conditionData
                                                , begin
                                                , isVerScore ? verNum : totalPages
                                                , labelName,minUnCompleteId,maxId);
                                break;
                            } catch (Exception ex) {
                                log.error(String.format(TITLE + "该跑分任务捞取数据异常：%s;错误信息：%s", blt.getBatchNumber(), ex.getMessage()), ex);
                            }
                        }
                        if (list.size() <= 0) {
                            threadpoolStatus = Boolean.FALSE;
                            continue;
                        }
                        sumNum += list.size();
                        //region 如果是预览跑分并且第一次进入循环 插入表头数据
                        if (isVerScore && isHead) {
                            StringBuilder verHead = new StringBuilder();
                            iProductResultSimpleService.initHead(verHead, separator, blt);
                            MarketingTaskResultPreview preview = new MarketingTaskResultPreview();
                            preview.setApiCode(blt.getApiCode());
                            preview.setTaskId(blt.getId());
                            preview.setFileId(blt.getFileId());
                            preview.setBatchNumber(blt.getBatchNumber());
                            preview.setIsTitle(1);
                            preview.setContent(verHead.toString());
                            marketingTaskService.saveScoreResult(preview);
                            isHead = Boolean.FALSE;
                        }
                        //endregion

                        //预览跑分 每次都要计算剩余跑分的条数
                        if (isVerScore) {
                            verNum = verNum - list.size();
                        }
                        // 当前分组中元素数量为1，即当前分组的数据要开始执行了，存储redis
                        if(groupNum == 1) {
                            saveGroupId(groupSetKey, groupIdSet, String.valueOf(groupId), fileId, blt, scoreBatchExpireSeconds);
                        }
                        // 构建跑分任务批次DTO,未开始执行的批次，进行跑分任务批次DTO初始化
                        scoreTaskBatchDTO = assemblyScoreTaskBatchDTO(scoreTaskBatchDTO, list, begin, currentPage, groupId, conditionIndex);
                        if (!getCoreDataStatus(blt, fileId, currentPage)) {
                            Map<String, String> param = new HashMap<>();
                            param.put("apiCode", blt.getApiCode());
                            param.put("strategyId", blt.getStrategyId());
                            param.put("path", descPath);
                            param.put("strategyStr", strategyStr);
                            param.put("sep", separator);
                            param.put("batchNumber", blt.getBatchNumber());
                            param.put("cusBatchNumber", blt.getFileName());
                            param.put("url", url);
                            param.put("appSecretKey", appSecretKey);
                            param.put("isRepair", blt.getIsRepair());
                            param.put("fileId", fileId);
                            param.put("noflagproduct", noflagproduct);
                            param.put("part", marketingTaskService.getPart(sumNum, currentPage).toString());
                            param.put("straHisFileCreateTimeMillis",
                                    file.getCreateTime() == null ? "" : String.valueOf(file.getCreateTime().getTime()));
                            warrningExecutor.submit(new CoreScoreThread(
                                    list, param, currentPage
                                    , firstTime, customer, blt
                                    , noflagproductlist, flagproductlist, marketingTaskExtend
                                    , baseHeadConfigVO, fieldInfo, false
                                    , marketingRetryEsMapper, marketingCommonConfig, marketingRetryRedisMapper,
                                    scoreTaskBatchDTO, scoreBatchExpireSeconds));
                            if (warrningExecutor.isTerminated()) {
                                threadpoolStatus = Boolean.FALSE;
                            }
                            Thread.sleep(100);
                        }
                        // 当前分组中元素数量等于分组最大数量，要开启下一个分组，即分组ID+1，分组中元素数量重置为1
                        if(GROUP_MAX_NUM == groupNum) {
                            groupId++;
                            groupNum = 1;
                        }else {
                            groupNum++;
                        }
                        // 获取到下一个批次的开始，即下一次批次的开始ID大于begin
                        begin = list.get(list.size() - 1).getId();
                        currentPage++;
                    }
                } else {
                    log.warn(String.format(TITLE + "无符合条件的数据--apiCode:%s,batchNumber:%s", blt.getApiCode(), blt.getBatchNumber()));
                }
            }
            long endtime = System.currentTimeMillis();
            if (log.isWarnEnabled()) {
                log.warn(TITLE + "apicode:".concat(blt.getBatchNumber()).concat("~~查询总耗时："
                        .concat(String.valueOf(endtime - startTime)).concat("~~轮询总次数：")
                        .concat(String.valueOf(currentPage).concat("~~esOpen:").concat(esOpenMark))));
            }


        } catch (Exception e) {
            log.error(TITLE + "执行任务失败", e);
        }
    }

    /**
     * 获取跑数状态
     *
     * @param fileId 跑分记录id
     * @param page   页码
     * @return false-为暂未跑完；true-已经跑完；
     */
    boolean getCoreDataStatus(MarketingTask task, String fileId, Long page) {
        // 用于兼容历史数据，之前redis的key为scoreStatus，当前时间在10天内，还会继续走下面查redis的数据
        LocalDate now = LocalDate.now();
        String scoreStatusFinalDate = marketingCommonConfig.getScoreStatusFinalDate();
        if(StringUtils.isNotEmpty(scoreStatusFinalDate)) {
            LocalDate start = LocalDate.parse(scoreStatusFinalDate);
            long days = Math.abs(ChronoUnit.DAYS.between(start, now));
            if(days > 10) {
                return false;
            }
        }
        String key = RedisKeyConstant.scoreStatus.concat(fileId).concat(":").concat(page.toString());
        int retryCount = 0;
        while (retryCount < 3) {
            try {
                // 模拟读redis异常
                checkMockRedisSwitch("readRedis");

                String s = redisChgService.get(key);
                if (StringUtils.isBlank(s)) {
                    return false;
                }
                if (s.equals("1")) {
                    return true;
                }
                return false;
            } catch (Exception e) {
                retryCount++;
                if (retryCount >= 3) {
                    // 3次都失败，报警
                    log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SUCCESS_UPLOAD.getCode(),
                            String.format(TITLE + "跑分异常，Redis查询失败3次，RedisKey=%s, fileId=%s, page=%s", key, fileId, page),e.getMessage()));
                    // 禁用跑分任务
                    marketingTaskService.disableTask(task);
                    return true;
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

    private String createShowTitle(MarketingTask task) {
        SimpleDateFormat yyyy_MM_dd = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd");
        MarketingTaskExtendExample extendExample = new MarketingTaskExtendExample();
        extendExample.createCriteria()
                .andTaskIdEqualTo(Long.valueOf(task.getId()))
                .andIsDelEqualTo(1);
        MarketingTaskExtend extend = marketingTaskExtendService.getMarketingTaskExtend(task.getId());
        if (extend != null) {
            String groupStr = "";
            ScoreRuleConfig scoreRule = scoreRuleConfigService.getScoreRule(extend.getRuleId());
            if (scoreRule != null) {
                groupStr = scoreRule.getRuleNameShort().concat("_");
            }
            Date parse = null;
            try {
                parse = yyyy_MM_dd.parse(extend.getUploadTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String showTitle = task.getApiCode().concat("_")
                    .concat(extend.getCusTaskId()).concat("_")
                    .concat(groupStr)
                    .concat(yyyyMMdd.format(parse)).concat("_")
                    .concat(yyyyMMdd.format(new Date()));
            return showTitle;

        }
        if (allMonitorType.equals(task.getMonitorType())) {
            return task.getCusBatch().concat("_").concat(yyyyMMdd.format(new Date()));
        }
        return task.getCusBatch();
    }


    public static String getLocalIp() {
        String ip = "";
        if (System.getProperty("os.name").toLowerCase().indexOf("windows") > -1) {
            try {
                ip = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                log.error("UnknownHostException {}", e);
            }
        } else {
            try {
                for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                    NetworkInterface interf = en.nextElement();
                    String name = interf.getName();
                    if (!name.contains("docker") && !name.contains("lo")) {
                        for (Enumeration<InetAddress> enumeAddress = interf.getInetAddresses(); enumeAddress.hasMoreElements(); ) {
                            InetAddress address = enumeAddress.nextElement();
                            if (!address.isLoopbackAddress()) {
                                String ipAddress = address.getHostAddress().toString();
                                if (!ipAddress.contains("::") && !ipAddress.contains("0:0") && !ipAddress.contains("fe80")) {
                                    ip = ipAddress;
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("get Linux local ip error {}", e);
            }
        }
        return ip;
    }

    /**
     * 线程监听
     *
     * @param executor
     * @param customer
     * @param task
     */
    private void threadNumListen(ThreadPoolExecutor executor, MarketingCustomer customer, MarketingTask task) {
        String zkpath = ZookeeperPath.marketPath.concat("/").concat(getLocalIp().concat("_")).concat(task.getBatchNumber());
        try {
            if (client.checkExists().forPath(zkpath) == null) {
                client.create().forPath(zkpath, customer.getThreadNum().toString().getBytes(StandardCharsets.UTF_8));
            } else {
                client.setData().forPath(zkpath, customer.getThreadNum().toString().getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        NodeCache nodeCache = new NodeCache(client, zkpath);
        nodeCache.getListenable().addListener(() -> {
            if (nodeCache.getCurrentData() != null) {
                int threadNum = Integer.parseInt(new String(nodeCache.getCurrentData().getData(), StandardCharsets.UTF_8));
                threadContextNum.put(customer.getApiCode(), threadNum);
                // 使用带重试机制的线程池调整工具类
                ThreadPoolAdjustmentUtil.adjustThreadPoolSize(executor, threadNum);
            }
        });
        try {
            nodeCache.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 跑分监听
     *
     * @param taskObj
     */
    private void scoreStatusListen(ObservedTaskObj taskObj) {
        MarketingTask task = taskObj.getMarketingTask();
        String zkStatusPath = ZookeeperPath.marketStatusPath.concat("/").concat(task.getFileId().toString());
        try {
            String parentPaht = "";
            String[] parentArrays = zkStatusPath.split("\\/");
            for (int i = 0; i < parentArrays.length; i++) {
                String pathNode = parentArrays[i];
                if (StringUtils.isBlank(pathNode)) {
                    continue;
                }
                if (i == parentArrays.length - 1) {
                    continue;
                }
                parentPaht += "/" + pathNode;
                if (client.checkExists().forPath(parentPaht) == null) {
                    client.create().forPath(parentPaht);
                }
            }
            if (client.checkExists().forPath(zkStatusPath) == null) {
                client.create().forPath(zkStatusPath, ZkScoreStatusEnum.RUNNING.getValue().getBytes(StandardCharsets.UTF_8));
            } else {
                client.setData().forPath(zkStatusPath, ZkScoreStatusEnum.RUNNING.getValue().getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        NodeCache nodeStatus = new NodeCache(client, zkStatusPath);
        nodeStatus.getListenable().addListener(() -> {
            if (nodeStatus.getCurrentData() != null) {
                String currentStatus = new String(nodeStatus.getCurrentData().getData());
                if (taskObj.getInterrupt().equals(0) && currentStatus.equals(ZkScoreStatusEnum.PAUSE.getValue())) {
                    observedScoreThreadService.stopThread(taskObj);
                    log.warn(TITLE + "已执行完暂停，batchNumber--{}", task.getBatchNumber());
                }
            }
        });
        try {
            nodeStatus.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Thread threadReport(ThreadPoolExecutor executor, MarketingCustomer customer) {
        Thread thread1 = new Thread(() -> {
            try {
                Boolean isListion = Boolean.TRUE;
                Integer times = 0;
                int sleeptime_unit = 10000;
                int sleeptime = 10000;
                while (isListion) {
                    if (sleeptime <= 1000 * 60 * 10) {
                        times++;
                        sleeptime = sleeptime_unit * times;
                    }
                    Thread.sleep(sleeptime);
                    int activeCount = executor.getActiveCount();
                    log.warn(String.format(TITLE + "跑分线程线程状态(客户：%s,活动线程：%d,核心线程数：%d,变动线程数：%d)"
                            , customer.getApiCode(), activeCount, executor.getCorePoolSize()
                            , threadContextNum.get(customer.getApiCode()) == null ? 0 : threadContextNum.get(customer.getApiCode())));
                    if (activeCount <= 0) {
                        threadContextNum.remove(customer.getApiCode());
                        isListion = Boolean.FALSE;
                    }
                }
            } catch (InterruptedException e) {
                if (log.isInfoEnabled()) {
                    log.info("终止运行");
                }
            }
        });
        thread1.start();
        return thread1;
    }

    private void removeZk(MarketingTask task) {
        String zkpath = ZookeeperPath.marketPath.concat("/").concat(getLocalIp().concat("_")).concat(task.getBatchNumber());
        String zkStatusPath = ZookeeperPath.marketStatusPath.concat("/").concat(task.getFileId().toString());
        try {
            client.delete().guaranteed().forPath(zkpath);
            client.delete().guaranteed().forPath(zkStatusPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 保存分组ID
     * @param groupSetKey  分组集合key
     * @param groupIdSet   分组ID集合
     * @param groupId      分组ID
     * @param fileId       执行记录ID
     */
    private void saveGroupId(String groupSetKey, Set<String> groupIdSet, String groupId, String fileId,
                             MarketingTask task, int scoreBatchExpireSeconds) {
        if(groupIdSet.contains(groupId)) {
            return;
        }
        int retryCount = 0;
        while (retryCount < 3) {
            try {
                // 模拟写redis异常
                checkMockRedisSwitch("writeRedis");

                groupIdSet.add(groupId);
                redisChgService.saddMember(groupSetKey,groupId);
                redisChgService.expire(groupSetKey, scoreBatchExpireSeconds);
                return;
            } catch (Exception e) {
                retryCount++;
                if (retryCount >= 3) {
                    log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SUCCESS_UPLOAD.getCode(),
                            String.format("跑分异常，Redis写入分组ID失败3次，RedisKey=%s, fileId=%s, groupId=%s", groupSetKey, fileId, groupId),e.getMessage()));
                    insertRetryRedis(groupSetKey,groupId, RedisValueTypeEnum.Set.getValue(), task);
                } else {
                    try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                }
            }
        }
    }

    private ScoreTaskBatchDTO assemblyScoreTaskBatchDTO(ScoreTaskBatchDTO scoreTaskBatchDTO,List<MarketingSyncUser> list, Long preId, Long currentPage,Integer groupId, Integer conditionIndex ) {
        if(scoreTaskBatchDTO == null) {
            scoreTaskBatchDTO = new ScoreTaskBatchDTO();
            scoreTaskBatchDTO.setPreId(preId);
            scoreTaskBatchDTO.setConditionIndex(conditionIndex);
            scoreTaskBatchDTO.setGroupId(groupId);
            scoreTaskBatchDTO.setMinId(list.get(0).getId());
            scoreTaskBatchDTO.setMaxId(list.get(list.size() - 1).getId());
            scoreTaskBatchDTO.setCurrentPage(currentPage);
            scoreTaskBatchDTO.setCompleteNum(0);
        }
        return scoreTaskBatchDTO;
    }

    /**
     * redis重试异常记录至异常表
     * @param key
     */
    private void insertRetryRedis(String key, String value, String valueType,MarketingTask marketingTask) {
        MarketingRetryRedis marketingRetryRedis = new MarketingRetryRedis();
        marketingRetryRedis.setApiCode(marketingTask.getApiCode());
        marketingRetryRedis.setBatchNumber(marketingTask.getBatchNumber());
        marketingRetryRedis.setPage("0");
        marketingRetryRedis.setRedisKey(key);
        marketingRetryRedis.setRedisValue(value);
        marketingRetryRedis.setRedisValueType(valueType);
        marketingRetryRedis.setRetryStatus(0);
        marketingRetryRedis.setAppletDate(LocalDate.now().toString());
        marketingRetryRedis.setCreateTime(new Date());
        marketingRetryRedis.setUpdateTime(new Date());
        marketingRetryRedisMapper.insertSelective(marketingRetryRedis);
    }


    /**
     * 初始化跑分批次进度
     * @param groupSetKey
     * @param groupIdSet
     * @param scoreTaskBatchDTOMap
     * @param fileId
     * @param conditionIndex
     * @param task
     */
    public void initScoreBatchProgressData(String groupSetKey, Set<String> groupIdSet,
                                           Map<String, ScoreTaskBatchDTO> scoreTaskBatchDTOMap, String fileId,
                                           Integer conditionIndex, MarketingTask task,
                                           int scoreBatchExpireSeconds) {
        int retryCount = 0;
        while (retryCount < 3) {
            try {
                // 模拟读redis异常
                checkMockRedisSwitch("readRedis");

                // 初始化分组ID集合
                initGroupIdSetData(groupIdSet, groupSetKey, scoreBatchExpireSeconds);
                // 初始化分组下的跑分任务批次DTO
                initScoreTaskBatchDTOMapData(scoreTaskBatchDTOMap, groupIdSet, fileId, conditionIndex, scoreBatchExpireSeconds);

                return;
            } catch (Exception e) {
                retryCount++;
                if (retryCount >= 3) {
                    // 3次都失败，报警
                    log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SUCCESS_UPLOAD.getCode(),
                            String.format(TITLE + "跑分异常，Redis查询失败3次，fileId=%s", fileId),e.getMessage()));
                    // 禁用跑分任务
                    marketingTaskService.disableTask(task);
                    throw new BusinessException("跑分异常，Redis查询失败3次");
                } else {
                    try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                }
            }
        }

    }

    /**
     * 初始化分组ID集合
     * @param groupSetKey 分组key
     * @param groupIdSet 分组集合
     */
    private void initGroupIdSetData(Set<String> groupIdSet, String groupSetKey, int scoreBatchExpireSeconds) {
        if(StringUtils.isEmpty(groupSetKey)) {
            return;
        }
        Set<String> smembers = redisChgService.smembers(groupSetKey);
        if(!CollectionUtils.isEmpty(smembers)) {
            redisChgService.expire(groupSetKey, scoreBatchExpireSeconds);
            groupIdSet.addAll(smembers);
        }

    }

    /**
     * 初始化分组下的跑分任务批次DTO
     * @param scoreTaskBatchDTOMap 跑分批次DTOmap
     * @param groupIdSet 分组ID集合
     * @param fileId 执行记录ID
     * @param conditionIndex 跑分条件序号
     */
    private void initScoreTaskBatchDTOMapData(Map<String, ScoreTaskBatchDTO> scoreTaskBatchDTOMap,
                                              Set<String> groupIdSet,
                                              String fileId, Integer conditionIndex,
                                              int scoreBatchExpireSeconds) {
        if(CollectionUtils.isEmpty(groupIdSet)) {
            return;
        }
        for(String groupId : groupIdSet) {
            String groupBatchKey = RedisKeyConstant.scoreBatch.concat(":").concat(fileId).concat(":").concat(String.valueOf(conditionIndex)).concat(":").concat(groupId);
            Map<String, Object> batchDTOMap = redisChgService.hgetall(groupBatchKey);
            if(!CollectionUtils.isEmpty(batchDTOMap)) {
                redisChgService.expire(groupBatchKey, scoreBatchExpireSeconds);
                log.warn("groupBatchKey:{}", groupBatchKey);
                for (Map.Entry<String, Object> entry : batchDTOMap.entrySet()) {
                    scoreTaskBatchDTOMap.put(entry.getKey(),JSON.parseObject(entry.getValue().toString(),ScoreTaskBatchDTO.class));
                }
            }
        }

    }

}
