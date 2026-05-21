package com.br.marketing.task.thread;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.cloud.counter.BrCounter;
import com.br.common.encryption.BrCipherMaker;
import com.br.common.log.AlertLog;
import com.br.common.util.StringUtils;
import com.br.marketing.client.ProFieldsClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.constants.rediskey.RedisKeyExpireConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.RedisValueTypeEnum;
import com.br.marketing.common.enums.TaskTypeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingRetryEsMapper;
import com.br.marketing.mapper.MarketingRetryRedisMapper;
import com.br.marketing.monitor.PrometheusMonitorUtils;
import com.br.marketing.service.MarketingTaskService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.task.Scheduler;
import com.br.marketing.task.dto.ScoreTaskBatchDTO;
import com.br.marketing.task.utils.HxUtil;
import com.br.marketing.task.utils.ResultUtil;
import com.br.marketing.vo.BaseHeadConfigVO;
import com.br.marketing.vo.StrategyProductDetailVO;
import org.mybatis.spring.MyBatisSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.lang.reflect.UndeclaredThrowableException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Callable;


public class CoreScoreThread implements Callable<String> {
    private static final Logger log = LoggerFactory.getLogger(CoreScoreThread.class);
    private List<MarketingSyncUser> list;
    private String apiCode;
    private String strategyId;
    private Long currentPage;
    private String path;
    private String strategyStr;
    private String message;
    private boolean firstTime;
    private JSONObject meal = new JSONObject();
    private String url;
    private Map<String, String> proFieldMap = new HashMap<>();
    private String sep;
    private List<MarketingSyncUser> errorList = new ArrayList<>();
    private RedisChgService redisChgService;
    private String batchNumber;
    private String cusBatchNumber;
    private String isRepair;
    private String fileId;
    private MarketingCustomer customer;
    private BaseHeadConfigVO baseHeadConfigVO;
    private StrategyProductDetailVO fieldInfo;
    private MarketingTask marketingTask;
    private List<String> noflagproductlist;
    private List<String> flagProductList;
    private MarketingTaskService marketingTaskService;
    private Boolean isRetry;
    private String part;
    private MarketingRetryEsMapper marketingRetryEsMapper;
    private MarketingCommonConfig marketingCommonConfig;
    private MarketingRetryRedisMapper marketingRetryRedisMapper;
    private ScoreTaskBatchDTO scoreTaskBatchDTO;
    /** 跑分批次进度 Redis TTL（秒），由客户 expire_day / 同步表量级策略解析 */
    private final int scoreBatchExpireSeconds;
    /** stra_his_file.createTime 毫秒时间戳，用于是否走 ES 新索引 */
    private Long straHisFileCreateTimeMillis;

    public CoreScoreThread(List<MarketingSyncUser> list, Map<String, String> param
            , Long currentPage, boolean firstTime, MarketingCustomer customer, MarketingTask marketingTask
            , List<String> noflagproductlist, List<String> flagProductList, MarketingTaskExtend marketingTaskExtend
            , BaseHeadConfigVO baseHeadConfigVO, StrategyProductDetailVO fieldInfo
            , Boolean isRetry, MarketingRetryEsMapper marketingRetryEsMapper
            , MarketingCommonConfig marketingCommonConfig, MarketingRetryRedisMapper marketingRetryRedisMapper
            , ScoreTaskBatchDTO scoreTaskBatchDTO, int scoreBatchExpireSeconds) {
        this.list = list;
        this.apiCode = param.get("apiCode");
        this.strategyId = param.get("strategyId");
        this.currentPage = currentPage;
        this.path = param.get("path");
        this.strategyStr = param.get("strategyStr");
//        this.redisService = Scheduler.ac.getBean(RedisService.class);
        this.firstTime = firstTime;
        this.url = param.get("url");
        this.sep = param.get("sep");
        this.redisChgService = Scheduler.ac.getBean(RedisChgService.class);
        this.batchNumber = param.get("batchNumber");
        this.cusBatchNumber = param.get("cusBatchNumber");
        this.isRepair = param.get("isRepair");
        this.fileId = param.get("fileId");
        this.customer = customer;
        this.marketingTask = marketingTask;
        this.noflagproductlist = noflagproductlist;
        this.flagProductList = flagProductList;
        this.marketingTaskService = Scheduler.ac.getBean(MarketingTaskService.class);
        this.isRetry = isRetry;
        this.fieldInfo = fieldInfo;
        this.baseHeadConfigVO = baseHeadConfigVO;
        this.part = param.get("part");
        this.marketingRetryEsMapper = marketingRetryEsMapper;
        this.marketingCommonConfig = marketingCommonConfig;
        this.marketingRetryRedisMapper = marketingRetryRedisMapper;
        this.scoreTaskBatchDTO = scoreTaskBatchDTO;
        this.scoreBatchExpireSeconds = scoreBatchExpireSeconds > 0
                ? scoreBatchExpireSeconds : RedisKeyExpireConstant.SCORE_BATCH_EXPIRE_TIME;
        String ctMillis = param.get("straHisFileCreateTimeMillis");
        if (StringUtils.isNotBlank(ctMillis)) {
            this.straHisFileCreateTimeMillis = Long.parseLong(ctMillis.trim());
        } else {
            this.straHisFileCreateTimeMillis = null;
        }
        Scheduler.ac.getBean(ProFieldsClient.class).setLoanPro(strategyStr, meal);
    }

    @Override
    public String call() {

        log.warn("start-----------------");

        if (list.size() == 0) {
            log.warn("开始执行监控任务。。{}。。{}", currentPage, list.size());
            return null;
        }
        if (!isRetry) {
            // 未完全完成的批次第一次执行时已记录了数量，恢复跑分不用再记录了
            if(scoreTaskBatchDTO != null && scoreTaskBatchDTO.getMinUnCompleteId() == null) {
                marketingTaskService.addTaskPercent(marketingTask.getFileId(), Long.valueOf(list.size()));
            }
        }
//        boolean check = this.checkRedisNumber();
        log.warn("开始执行监控任务。。跑分--{}。。页码--{}。。量级--{}",marketingTask.getBatchNumber(), currentPage, list.size());

        File writeName = new File(path);
        if (!writeName.exists()) {
            writeName.mkdirs();
        }

        File errorFile = new File(path + "/error" + currentPage + ".txt");
        File file1 = new File(path + "/" + currentPage + ".txt");

        boolean appendFlag = false;
        if(!isRetry) {
            if(scoreTaskBatchDTO != null && scoreTaskBatchDTO.getMinUnCompleteId() != null) {
                appendFlag = true;
            }
        }else {
            appendFlag = true;
        }
        try (Writer errorFw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(errorFile), "UTF-8"));
             Writer fw = new BufferedWriter(
                     new OutputStreamWriter(
                             new FileOutputStream(file1,appendFlag), "UTF-8"));) {

            JSONObject param = new JSONObject();
            param.put("strategyId", strategyId);
            BrCipherMaker instance = BrCipherMaker.getInstance();
            // 记录当前已处理完成的数据量
            int count = 0;
            for (MarketingSyncUser blu : list) {
                if(!isRetry && Thread.currentThread().isInterrupted()) {
                    // 检测到中断了，跳出循环
                    if(scoreTaskBatchDTO != null) {
                        // 记录待处理的记录ID
                        scoreTaskBatchDTO.setMinUnCompleteId(blu.getId());
                    }
                    break;
                }
                if (blu.getStatus() != 1) {
                    count++;
                    continue;
                }
                if (marketingTask.getTaskType().equals(TaskTypeEnum.DIRECTDATA.getValue())
                        || marketingTask.getIsOnline().equals(2)) {
                    dealResult(fw, blu);
                } else {
                    RequestLog requestLog = new RequestLog();
                    requestLog.setRequestTime(new Date());

                    JSONObject jsonData = new JSONObject();
                    jsonData.put("userType", blu.getUserType());
                    jsonData.put("cusNum", blu.getCustNum());
                    jsonData.put("idCard", instance.decode(blu.getIdCard()));
                    jsonData.put("name", instance.decode(blu.getName()));
                    jsonData.put("cell", instance.decode(blu.getCell()));
                    jsonData.put("isRepair", isRepair);
                    jsonData.put("batch_number", marketingTask.getBatchNumber());
                    JSONObject extData = null;
                    if (customer.getShortName().contains("拍拍贷新客")) {
                        extData = new JSONObject();
                        String sleepGroup = "";
                        try {
                            if (StringUtils.isNotBlank(blu.getReserveField1())) {
                                JSONObject jsonObject = JSONObject.parseObject(blu.getReserveField1());
                                if (StringUtils.isNotBlank(jsonObject.getString("sleepGroup"))) {
                                    sleepGroup = jsonObject.getString("sleepGroup");
                                }
                            }
                        } catch (Exception ex) {
                            log.error("拍拍贷扩展字段转化json出问题" + ex.getMessage(), ex);
                        }
                        extData.put("sleepGroup", sleepGroup);
                    }
                    if (extData != null) {
                        jsonData.put("extData", extData);
                    }
                    param.put("jsonData", jsonData.toString());
                    String resultStr = HxUtil.getReport(customer, jsonData, meal, url,noflagproductlist, flagProductList);
                    dealResult(resultStr, fw, apiCode, blu, isRetry);
                }
                count++;
            }
            if(!isRetry && Thread.currentThread().isInterrupted()) {
                Thread.interrupted();
            }
            if (errorList.size() > 0) {
                for (MarketingSyncUser lu : errorList) {
                    errorFw.append(JSON.toJSONString(lu) + "\n");
                }
                String key = Constants.HXRESULTERROR_RETRY_KEY + ":" + this.fileId;
                try {
                    redisChgService.hset(key, errorFile.getPath(), batchNumber);
                }catch (RuntimeException exception) {
                    try {
                        redisChgService.hset(key, errorFile.getPath(), batchNumber);
                    }catch (RuntimeException e) {
                        if(e.getCause() != null && e.getCause() instanceof UndeclaredThrowableException) {
                            UndeclaredThrowableException e1 = (UndeclaredThrowableException) e.getCause();
                            if(e1 != null && e1.getCause() != null && e1.getCause() instanceof InterruptedException) {
                                redisChgService.hset(key, errorFile.getPath(), batchNumber);
                            }else {
                                throw e;
                            }
                        }else {
                            throw e;
                        }
                    }
                }
            }
            // 保存当前批次进度
            saveScoreBatchProgress(count);
            log.warn("结束执行监控任务。。跑分--{}。。页码--{}。。量级--{}",marketingTask.getBatchNumber(), currentPage, list.size());
        } catch (Exception e) {
            log.error("生成文件出错。。。。", e);
        }
        return null;
    }

    private void setScoreStatus(ScoreTaskBatchDTO scoreTaskBatchDTO) {
        String key = RedisKeyConstant.scoreBatch.concat(":").concat(fileId).concat(":").concat(String.valueOf(scoreTaskBatchDTO.getConditionIndex())).concat(":").concat(String.valueOf(scoreTaskBatchDTO.getGroupId()));
        log.warn("setScoreStatus key:{}", key);
        int retryCount = 0;
        while (retryCount < 3) {
            try {
                // 模拟写redis异常
                checkMockRedisSwitch("writeRedis");

                redisChgService.hset(key,String.valueOf(scoreTaskBatchDTO.getPreId()),JSON.toJSONString(scoreTaskBatchDTO));
                redisChgService.expire(key, scoreBatchExpireSeconds);
                return;
            } catch (Exception e) {
                retryCount++;
                if (retryCount >= 3) {
                    log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SUCCESS_UPLOAD.getCode(),
                            String.format("跑分异常，Redis写入失败3次，RedisKey=%s, fileId=%s, scoreTaskBatchDTO=%s", key, fileId, JSON.toJSONString(scoreTaskBatchDTO)),e.getMessage()));
                    insertRetryRedis(key,JSON.toJSONString(scoreTaskBatchDTO), RedisValueTypeEnum.Hash.getValue());
                } else {
                    try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                }
            }
        }
    }

    private void checkMockRedisSwitch(String key) throws Exception {
        Map<String, Boolean> mockRedisSwitch =  marketingCommonConfig.getMockRedisSwitch();
        if(!CollectionUtils.isEmpty(mockRedisSwitch)){
            if(mockRedisSwitch.get(key)){
                throw new Exception();
            }
        }
    }

    /**
     * redis重试异常记录至异常表
     * @param key
     */
    private void insertRetryRedis(String key, String value, String valueType) {
        MarketingRetryRedis marketingRetryRedis = new MarketingRetryRedis();
        marketingRetryRedis.setApiCode(apiCode);
        marketingRetryRedis.setBatchNumber(marketingTask.getBatchNumber());
        marketingRetryRedis.setPage(String.valueOf(currentPage));
        marketingRetryRedis.setRedisKey(key);
        marketingRetryRedis.setRedisValue(value);
        marketingRetryRedis.setRedisValueType(valueType);
        marketingRetryRedis.setRetryStatus(0);
        marketingRetryRedis.setAppletDate(LocalDate.now().toString());
        marketingRetryRedis.setCreateTime(new Date());
        marketingRetryRedis.setUpdateTime(new Date());

        try {
            marketingRetryRedisMapper.insertSelective(marketingRetryRedis);
        } catch (MyBatisSystemException e) {
            // 检查线程是否被中断
            if (Thread.currentThread().isInterrupted()) {
                // 清除中断标志，以便重新插入数据库
                boolean wasInterrupted = Thread.interrupted();
                // 重新插入数据库
                marketingRetryRedisMapper.insertSelective(marketingRetryRedis);
            } else {
                // 线程未被中断，是其他原因导致的异常，直接抛出
                throw e;
            }
        }
    }

    /**
     * 生成结果文件
     *
     * @param s
     */
    private void dealResult(String s, Writer fw, String apiCode, MarketingSyncUser blu,Boolean isRetry) throws IOException {
        try {
            //最终结果判断处理
            if (!HxUtil.isRetry(s, meal, noflagproductlist,flagProductList, errorList,marketingTask,isRetry,blu,redisChgService)) {
                //跑分请求监控统计
                try {
                    BrCounter.count(PrometheusMonitorUtils.COUNT_CORE_SCORE_API_METRIC_NAME, apiCode, blu.getUserType());
                } catch (Exception ex) {
                    log.error("跑分接口统计异常" + ex.getMessage(), ex);
                }
                JSONObject resultJson = JSONObject.parseObject(s);
                if (fw != null) {
                    ResultUtil.generateFile(resultJson, strategyId
                            , fw, sep, proFieldMap, blu
                            , meal, cusBatchNumber, fileId
                            , customer.getPushCustomer().toString()
                            , baseHeadConfigVO, fieldInfo, marketingTask
                            , marketingTaskService, part
                            , marketingCommonConfig, marketingRetryEsMapper, straHisFileCreateTimeMillis);
                }
            }
        } catch (Exception e) {
            log.error("dealResult出错了", e);
        }
    }

    private void dealResult(Writer fw, MarketingSyncUser blu) throws IOException {
        try {
            if (fw != null) {
                ResultUtil.generateFile(null, strategyId
                        , fw, sep, proFieldMap, blu
                        , meal, cusBatchNumber, fileId
                        , customer.getPushCustomer().toString()
                        , baseHeadConfigVO, fieldInfo, marketingTask
                        , marketingTaskService, part
                        , marketingCommonConfig, marketingRetryEsMapper, straHisFileCreateTimeMillis);
            }
        } catch (Exception e) {
            log.error("dealResult出错了", e);
        }
    }

    private void dealResult(String s, Writer errorFw) throws IOException {
        if (!StringUtils.isEmpty(s)) {
            errorFw.append(s + "\r\n");
        }
    }

    /**
     * 保存当前批次进度
     * @param count 已完成的数据量
     */
    private void saveScoreBatchProgress(int count) {
        if(!isRetry) {
            if(scoreTaskBatchDTO != null) {
                // 说明当前批次已完全处理结束了
                if(count == list.size()) {
                    scoreTaskBatchDTO.setMinUnCompleteId(null);
                }
                // 记录已完成的数据量
                scoreTaskBatchDTO.setCompleteNum(scoreTaskBatchDTO.getCompleteNum() + count);
                setScoreStatus(scoreTaskBatchDTO);
            }
        }
    }


}
