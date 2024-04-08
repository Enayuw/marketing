package com.br.marketing.task.thread;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.cloud.counter.BrCounter;
import com.br.common.encryption.BrCipherMaker;
import com.br.common.util.StringUtils;
import com.br.marketing.client.ProFieldsClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.TaskTypeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.entity.*;
import com.br.marketing.monitor.PrometheusMonitorUtils;
import com.br.marketing.service.MarketingTaskService;
import com.br.marketing.task.Scheduler;
import com.br.marketing.task.utils.HxUtil;
import com.br.marketing.task.utils.ResultUtil;
import com.br.marketing.task.utils.VaildHxResultUtil;
import com.br.marketing.vo.BaseHeadConfigVO;
import com.br.marketing.vo.StrategyProductDetailVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
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

    public CoreScoreThread(List<MarketingSyncUser> list, Map<String, String> param
            , Long currentPage, boolean firstTime, MarketingCustomer customer, MarketingTask marketingTask
            , List<String> noflagproductlist, List<String> flagProductList, MarketingTaskExtend marketingTaskExtend
            , BaseHeadConfigVO baseHeadConfigVO, StrategyProductDetailVO fieldInfo, Boolean isRetry) {
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
            marketingTaskService.addTaskPercent(marketingTask.getFileId(), Long.valueOf(list.size()));
        }
//        boolean check = this.checkRedisNumber();
        log.warn("开始执行监控任务。。{}。。{}", currentPage, list.size());

        File writeName = new File(path);
        if (!writeName.exists()) {
            writeName.mkdirs();
        }

        File errorFile = new File(path + "/error" + currentPage + ".txt");
        File file1 = new File(path + "/" + currentPage + ".txt");

        try (Writer errorFw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(errorFile), "UTF-8"));
             Writer fw = new BufferedWriter(
                     new OutputStreamWriter(
                             new FileOutputStream(file1), "UTF-8"));) {

            JSONObject param = new JSONObject();
            param.put("strategyId", strategyId);
            BrCipherMaker instance = BrCipherMaker.getInstance();
            for (MarketingSyncUser blu : list) {
                if (blu.getStatus() != 1) {
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
            }
            if (errorList.size() > 0) {
                for (MarketingSyncUser lu : errorList) {
                    errorFw.append(JSON.toJSONString(lu) + "\n");
                }
                String key = Constants.HXRESULTERROR_RETRY_KEY + ":" + this.fileId;
                redisChgService.hset(key, errorFile.getPath(), batchNumber);
            }
            setScoreStatus();
        } catch (Exception e) {
            log.error("生成文件出错。。。。", e);
        }
        return null;
    }

    private void setScoreStatus() {
        String key = RedisKeyConstant.scoreStatus.concat(fileId).concat(":").concat(String.valueOf(currentPage));
        redisChgService.set(key, "1");
        redisChgService.expire(key, 60 * 60 * 24 * 10);
    }


    /**
     * 生成结果文件
     *
     * @param s
     */
    private void dealResult(String s, Writer fw, String apiCode, MarketingSyncUser blu,Boolean isRetry) throws IOException {
        try {
            //最终结果判断处理
            if (!HxUtil.isRetry(s, meal, noflagproductlist,flagProductList,apiCode, errorList,marketingTask,isRetry,blu,redisChgService)) {
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
                            , baseHeadConfigVO, fieldInfo, marketingTask, marketingTaskService,part);
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
                        , baseHeadConfigVO, fieldInfo, marketingTask, marketingTaskService,part);
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


}
