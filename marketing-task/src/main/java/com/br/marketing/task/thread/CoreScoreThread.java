package com.br.marketing.task.thread;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.common.encryption.BrCipherMaker;
import com.br.common.util.StringUtils;
import com.br.marketing.client.*;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.entity.*;
import com.br.marketing.task.Scheduler;
import com.br.marketing.task.utils.HxUtil;
import com.br.marketing.task.utils.ResultUtil;
import com.br.marketing.task.utils.VaildHxResultUtil;
import com.br.marketing.vo.BaseHeadConfigVO;
import com.br.marketing.vo.StrategyProductDetailVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;


public class CoreScoreThread implements Callable<String> {
    private static final Logger log = LoggerFactory.getLogger(CoreScoreThread.class);
    private List<MarketingSyncUser> list;
    private String apiCode;
    private String strategyId;
    private int currentPage;
    private String path;
    private String strategyStr;
    private RedisService redisService;
//    private ObservedScoreThreadServiceImpl observedThread;
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

    public CoreScoreThread(List<MarketingSyncUser> list, Map<String, String> param
            , int currentPage, boolean firstTime, MarketingCustomer customer, MarketingTask marketingTask
            , List<String> noflagproductlist, List<String> flagProductList,MarketingTaskExtend marketingTaskExtend) {
        this.list = list;
        this.apiCode = param.get("apiCode");
        this.strategyId = param.get("strategyId");
        this.currentPage = currentPage;
        this.path = param.get("path");
        this.strategyStr = param.get("strategyStr");
        this.redisService = Scheduler.ac.getBean(RedisService.class);
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
        BaseHeadConfigVO o = JSON.parseObject(marketingTaskExtend.getExtendShowTitle(), new TypeReference<BaseHeadConfigVO>() {
        }.getType());
        StrategyProductDetailVO fieldInfo = JSON.parseObject(marketingTaskExtend.getStrategyProductJson(), new TypeReference<StrategyProductDetailVO>() {
        }.getType());
        this.fieldInfo = fieldInfo;
        this.baseHeadConfigVO = o;
        Scheduler.ac.getBean(ProFieldsClient.class).setLoanPro(strategyStr, meal);
    }

    @Override
    public String call() {

        log.warn("start-----------------");
        Long fileId = marketingTask.getFileId();

        if (list.size() == 0) {
            log.warn("开始执行监控任务。。{}。。{}", currentPage, list.size());
            return null;
        }

        boolean check = this.checkRedisNumber();
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

            if (!check) {
                log.error("条数不足--{}", message);
                dealResult(message, errorFw);
                errorFw.close();
                return null;
            }
            JSONObject param = new JSONObject();
            param.put("strategyId", strategyId);
            BrCipherMaker instance = BrCipherMaker.getInstance();
            for (MarketingSyncUser blu : list) {
//                TimeUnit.MILLISECONDS.sleep(1L);
                if (blu.getStatus() != 1) {
                    continue;
                }
                RequestLog requestLog = new RequestLog();
                requestLog.setRequestTime(new Date());

                JSONObject jsonData = new JSONObject();
                jsonData.put("userType", blu.getUserType());
                jsonData.put("cusNum", blu.getCustNum());
                jsonData.put("idCard", instance.decode(blu.getIdCard()));
                jsonData.put("name", instance.decode(blu.getName()));
                jsonData.put("cell", instance.decode(blu.getCell()));
                jsonData.put("isRepair", isRepair);
//                jsonData.put("passDate", blu.getPassDate());
//                jsonData.put("loanMaturityDate", blu.getLoanMaturityDate());
//                if (StringUtils.isNotEmpty(blu.getDecodeFailType())) {
//                    jsonData.put("decodeFailType", blu.getDecodeFailType());
//                }
//                String approvalResult = blu.getApprovalResult();
//                String result = "";
//                if (StringUtils.isNotEmpty(approvalResult)) {
//                    switch (approvalResult) {
//                        case "通过":
//                            result = "1";
//                            break;
//                        case "拒绝":
//                            result = "2";
//                            break;
//                        case "复议":
//                            result = "3";
//                            break;
//                        case "无结果":
//                            result = "4";
//                            break;
//                        case "无贷前审批":
//                            result = "5";
//                            break;
//                        default:
//                            break;
//                    }
//                }
//
//                if (StringUtils.isNotEmpty(result)) {
//                    jsonData.put("approveResult", result);
//                }
                jsonData.put("batch_number", marketingTask.getBatchNumber());
                param.put("jsonData", jsonData.toString());
                String resultStr = HxUtil.getReport(customer, jsonData, meal, firstTime, url);
                if(marketingTask.getTaskType().equals(1)){
                    dealResult(fw, blu);
                }else {
                    dealResult(resultStr, fw, apiCode, blu);
                }
            }
            if (errorList.size() > 0) {
                for (MarketingSyncUser lu : errorList) {
                    errorFw.append(JSON.toJSONString(lu)+"\n");
                }
                String key = Constants.HXRESULTERROR_RETRY_KEY + ":" + this.fileId;
                redisChgService.hset(key, errorFile.getPath(), batchNumber);
            }
            setScoreStatus();
        }
        catch (Exception e) {
            log.error("生成文件出错。。。。", e);
        }
        return null;
    }

    private void setScoreStatus() {
        String key = RedisKeyConstant.scoreStatus.concat(fileId).concat(":").concat(String.valueOf(currentPage));
        redisChgService.set(key, "1");
        redisChgService.expire(key,60*60*24*10);
    }


    /**
     * 校验api_code的数据产品条数
     *
     * @return
     */
    private boolean checkRedisNumber() {
        boolean flag = true;
        if (customer.getCheckRedisNumber() == 0) {
            return flag;
        }
        try {
            String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
            Map<String, String> dayNumMap = new HashMap<>();
            List<String> typeNoList = new ArrayList<>();
            addDTBPro(typeNoList);
            MerchantParam merchantParam = IceClient.getMerchantParam(apiCode);
            if (merchantParam == null) {
                log.error("用户中心结果为空");
                return false;
            }
            getDayNumMap(dayNumMap, merchantParam);
            if (merchantParam.getAccountType() == 1) {
                //在redis中加上使用条数
                addRedisNum(Constants.REDIS_RADAR_PREFIX + ":" + apiCode, typeNoList, list.size());
                for (String proCode : typeNoList) {
                    String key = Constants.REDIS_RADAR_PREFIX + ":" + apiCode + ":" + proCode + ":" + date;
                    String currentNum = redisService.get(key);
                    if (currentNum == null) {
                        redisService.set(key, null, 604800);
                        addRedisNumForDayNum(key, null, list.size());
                    } else {
                        addRedisNumForDayNum(key, null, list.size());
                    }
                }
            } else {
                long min = 0;
                String result = getMinNum(apiCode, typeNoList, merchantParam);
                try {
                    min = Long.parseLong(result);
                } catch (Exception e) {
                    message = result;
                    log.error("message--{}", message);
                    flag = false;
                    return flag;
                }

                if (min >= list.size()) {
                    addRedisNum(Constants.REDIS_RADAR_TEST_PREFIX + ":" + apiCode, typeNoList, list.size());
                    for (String proCode : typeNoList) {
                        String keyTest = Constants.REDIS_RADAR_TEST_PREFIX + ":" + apiCode + ":" + proCode + ":" + date;
                        String currentNum = redisService.get(keyTest);
                        String dayNum = dayNumMap.get(proCode);
                        if (currentNum == null) {
                            redisService.set(keyTest, null, 604800);
                            if (list.size() > Integer.parseInt(dayNum)) {
                                message = "可用条数不足，请确认，若需要请联系客服";
                                log.error("message--{}", message);
                                flag = false;
                            } else {
                                addRedisNumForDayNum(keyTest, null, list.size());
                            }
                        } else {
                            if (Integer.parseInt(currentNum) + list.size() > Integer.parseInt(dayNum)) {
                                message = "可用条数不足，请确认，若需要请联系客服";
                                log.error("message--{}", message);
                                flag = false;
                            } else {
                                addRedisNumForDayNum(keyTest, null, list.size());
                            }
                        }
                    }
                } else {
                    message = "可用条数不足，请确认，若需要请联系客服";
                    log.error("message--{}", message);
                    flag = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("error----{}", e);
        }

        return flag;
    }


    /**
     * 获取redis中最小的产品条数
     *
     * @param apiCode
     * @param typeNoList
     * @return
     */
    private String getMinNum(String apiCode, List<String> typeNoList, MerchantParam merchantParam) {
        List<Long> numList = new ArrayList<>();
        for (String typeNo : typeNoList) {
            String keyTest = Constants.REDIS_RADAR_TEST_PREFIX + ":" + apiCode + ":" + typeNo + ":" + Constants.REDIS_RADAR_TOTALCOUNT;
            String str = redisService.get(keyTest);
            long num = 0;
            if (!org.springframework.util.StringUtils.isEmpty(str)) {
                num = Long.parseLong(str);
            }
            String limitNumFromUserCenter = getLimitNumFromUserCenter(typeNo, merchantParam);
            long canUseNum = 0;
            if (!"empty".equals(limitNumFromUserCenter)) {
                canUseNum = Long.parseLong(limitNumFromUserCenter) - num;
            } else {
                return "策略状态不可用";
            }
            numList.add(canUseNum);
        }
        //log.info("各个数据产品的可用条数：数据产品代号：{}，剩余条数：{}", JSON.toJSONString(typeNoList), JSON.toJSONString(numList));
        if (numList.size() > 0) {
            String string = Collections.min(numList).toString();
            return string;
        } else {
            return "0";
        }

    }

    private String getLimitNumFromUserCenter(String proCode, MerchantParam merchantParam) {
        String limitNum = null;
        String meal = merchantParam.getMealJson();
        if (!org.springframework.util.StringUtils.isEmpty(meal)) {
            JSONObject mealJson = JSON.parseObject(meal);
            String proCodeString = mealJson.getString(proCode);
            if (!org.springframework.util.StringUtils.isEmpty(proCodeString)) {
                JSONObject proCodeJson = JSON.parseObject(proCodeString);
                limitNum = proCodeJson.getString("limit_num");
            } else {
                log.warn("产品---{}---没有权限", proCode);
                return "empty";
            }
        }
        return limitNum;
    }

    private void getDayNumMap(Map<String, String> dayNumMap, MerchantParam merchantParam) {
        String meal = merchantParam.getMealJson();
        if (!org.springframework.util.StringUtils.isEmpty(meal)) {
            JSONObject mealJson = JSON.parseObject(meal);
            Set<String> strings = mealJson.keySet();
            for (String key : strings) {
                String proCodeString = mealJson.getString(key);
                if (!org.springframework.util.StringUtils.isEmpty(proCodeString)) {
                    JSONObject proCodeJson = JSON.parseObject(proCodeString);
                    String dayNum = "";
                    if (proCodeJson != null && proCodeJson.containsKey("dayNum")) {
                        dayNum = proCodeJson.getString("dayNum");
                    } else if (proCodeJson != null && !proCodeJson.containsKey("dayNum")) {
                        dayNum = proCodeJson.getString("limit_num");
                    }
                    if (!org.springframework.util.StringUtils.isEmpty(dayNum)) {
                        dayNumMap.put(key, dayNum);
                    }

                }
            }
        }
    }

    private void addRedisNumForDayNum(String apiCode, String typeNo, int num) {
        long l = System.currentTimeMillis();
        redisService.incrBy(apiCode, typeNo, num);
    }

    private void addRedisNum(String apiCode, List<String> typeNoList, int num) {
        long l = System.currentTimeMillis();
        for (String typeNo : typeNoList) {
            redisService.incrBy(apiCode + ":" + typeNo + ":" + Constants.REDIS_RADAR_TOTALCOUNT, null, num);
        }
    }

    /**
     * 添加风险策略需要校验的数据产品列表
     *
     * @param typeNoList
     */
    private void addSTRBPro(List<String> typeNoList) {
        String strategy = StrategyClient.getStrategy(apiCode, strategyId);
        JSONObject jsonObject = JSONObject.parseObject(strategy);
        if (jsonObject != null && !jsonObject.isEmpty()) {
            if (StringUtils.isNotEmpty(jsonObject.getString("canUse")) && "0".equals(jsonObject.getString("canUse"))
                    && StringUtils.isNotEmpty(jsonObject.getString("status")) && "1".equals(jsonObject.getString("status"))) {
                JSONObject ruleTypeJson = jsonObject.getJSONObject("ruleType");
                if (StringUtils.isNotEmpty(ruleTypeJson.getString("status")) && "1".equals(ruleTypeJson.getString("status"))) {
                    JSONArray jsonArray = ruleTypeJson.getJSONArray("ruleTypeList");
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject ruleType = jsonArray.getJSONObject(i);
                        String typeNo = ruleType.getString("ruleType");
                        typeNoList.add(typeNo);
                    }
                }
            }
        }
    }

    /**
     * {"dataProdList":[{"code":"TotalLoan","version":"V1.0"}]}
     *
     * @param typeNoList
     */
    private void addDTBPro(List<String> typeNoList) {
        JSONArray dataProdList1 = JSONArray.parseArray(strategyStr);
        for (int j = 0; j < dataProdList1.size(); j++) {
            JSONObject jsonObject = dataProdList1.getJSONObject(j);
            if (jsonObject != null && jsonObject.containsKey("code")) {
                String code = jsonObject.getString("code");
                typeNoList.add(code);
            }
        }
    }

    /**
     * 生成结果文件
     *
     * @param s
     */
    private void dealResult(String s, Writer fw, String apiCode, MarketingSyncUser blu) throws IOException {
        try {
            if (VaildHxResultUtil.isPass(s, meal, apiCode, redisChgService, blu, errorList, noflagproductlist, flagProductList)) {
                JSONObject resultJson = JSONObject.parseObject(s);
                if (fw != null) {
                    ResultUtil.generateFile(resultJson, strategyId
                            , fw, sep, proFieldMap, blu
                            , meal, cusBatchNumber, fileId
                            , customer.getPushCustomer().toString()
                            , baseHeadConfigVO, fieldInfo,marketingTask);
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
                        , baseHeadConfigVO, fieldInfo,marketingTask);
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
