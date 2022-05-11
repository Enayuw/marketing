package com.br.marketing.task.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.ZookeeperPath;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.customizedassert.AssertResult;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.service.*;
import com.br.marketing.service.Impl.StrategyCs;
import com.br.marketing.task.thread.CoreScoreThread;
import com.br.marketing.vo.StrategyProductDetailVO;
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

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    @Value("${otherConfig.warning.path:00}")
    private String path;
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
    MarketingTaskMapper marketingTaskMapper;
    @Resource
    MarketingSepService marketingSepService;
    @Resource
    MarketingUserMapper marketingUserMapper;
    @Resource
    LoanFileMapper loanFileMapper;
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

    @Resource
    TaskStatusDistributeMapper taskStatusDistributeMapper;

    @Autowired
    StraHisFileMapper straHisFileMapper;

    @Autowired
    IProductResultSimpleService iProductResultSimpleService;

    @Resource
    FastFileRelationMapper fastFileRelationMapper;

    private final static String RedisEsOpen = "es:open";
    @Autowired
    ObservedScoreThreadServiceImpl observedScoreThreadService;

    final static Integer allMonitorType = 4;

    final static String RedisCodeProduct = "apicodescore:product:";
    static ConcurrentHashMap<String, Integer> threadContextNum = new ConcurrentHashMap<>();

    @Autowired
    private CuratorFramework client;

    @Resource
    MarketingCustomerMapper marketingCustomerMapper;

    @Resource
    MarketingSyncUserMapper marketingSyncUserMapper;

    @Autowired
    IDynamicSqlService iDynamicSqlService;

    public void process(MarketingTask task, String day) {
        String apiCode = task.getApiCode();

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
        ThreadPoolExecutor warrningExecutor = BrExecutors.getThreadPool(customer.getThreadNum(), customer.getThreadNum());

        //线程监听
        threadNumListen(warrningExecutor, customer);

        //线程池注册
        observedScoreThreadService.addObserver(warrningExecutor);

        try {

            //线程池运行情况报告
            Thread thread = threadReport(warrningExecutor, customer);

            //region 跑分
            this.generateTask(task, warrningExecutor, customer, day);
            /**
             * 等待所有任务都执行完成
             **/
            log.warn("所有任务已加入队列，等待结束-----");
            warrningExecutor.shutdown();
            while (true) {
                if (warrningExecutor.isTerminated()) {
                    observedScoreThreadService.removeThread(warrningExecutor);
                    log.warn("所有线程都执行结束");
                    break;
                }
                try {
                    Thread.sleep(6000);
                } catch (Exception e) {
                }
            }
            //endregion

            //region 重试
            try {
                String hkey = Constants.HXRESULTERROR_RETRY_KEY + ":" + task.getFileId();
                Set<String> hkeys = redisChgService.hkeys(hkey);
                if (!hkeys.isEmpty() && hkeys.size() > 0) {
                    warrningExecutor = BrExecutors.getThreadPool(20, 20);
                    int i = 1;
                    for (String errorFile : hkeys) {
//                        String batchNumber = redisChgService.hget(hkey, errorFile);
//                        MarketingTask task = marketingTaskMapper.queryBlt(batchNumber);
                        if (task != null) {
                            this.retry(task, errorFile, warrningExecutor, i, customer);
                            i++;
                        }
                    }
                    log.warn("所有重试任务已加入队列，等待结束-----");
                    warrningExecutor.shutdown();
                    while (true) {
                        if (warrningExecutor.isTerminated()) {
                            log.warn("重试任务所有线程都执行结束");
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

            //region 任务状态表和任务记录表的更新
            TaskStatus updateStatus = new TaskStatus();
            updateStatus.setId(task.getStatusId());
            if (task.getMonitorType().equals(1)) {
                updateStatus.setOnceStatus(observedScoreThreadService.isInterrupt() ? 3 : 2);
            } else {
                updateStatus.setAllStatus(observedScoreThreadService.isInterrupt() ? 3 : 2);
            }
            taskStatusMapper.updateByPrimaryKeySelective(updateStatus);
            if (!observedScoreThreadService.isInterrupt()) {
                StraHisFile updateFile = new StraHisFile();
                updateFile.setId(task.getFileId());
                updateFile.setStatus(1);
                straHisFileMapper.updateByPrimaryKeySelective(updateFile);
            }
            //endregion

            thread.interrupt();
            removeZk(customer);
        } catch (Exception e) {
            log.error("预警调度出错", e);
        }
        return;
    }

    /**
     * 重新处理异常数据
     *
     * @param errorFile        异常数据记录文件
     * @param warrningExecutor 线程池
     * @param num              文件编号
     */
    private void retry(MarketingTask marketingTask, String errorFile, ExecutorService warrningExecutor
            , Integer num, MarketingCustomer customer) {
        String noflagproduct = redisChgService.get(RedisKeyConstant.noFlagProduct);
        List<String> noflagproductlist = new ArrayList<>();
        if (StringUtils.isNotBlank(noflagproduct)) {
            noflagproductlist = Splitter.on(",").splitToList(noflagproduct);
        } else {
            noflagproductlist.add("mappingcust");
            noflagproductlist.add("mappingcust1");
        }
        List<String> flagproductlist = new ArrayList<>();
        Result<List<String>> flagProduct = iProductResultSimpleService.getFlagProduct();
        if (flagProduct.getCode().equals(ResultCode.SUCCESS.getValue())) {
            flagproductlist = flagProduct.getData();
        }
        String separator = marketingSepService.querySepByApiCode(marketingTask.getApiCode());
        MarketingTaskExtend marketingTaskExtend = marketingTaskExtendService.getMarketingTaskExtend(marketingTask.getId());
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
        int currentPage = Integer.parseInt(s);
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
            warrningExecutor.submit(new CoreScoreThread(
                    list, param, currentPage, true, customer
                    , marketingTask, noflagproductlist
                    , flagproductlist, marketingTaskExtend));
        } catch (Exception e) {
            log.error("重新处理画像异常数据出错:{},{}", errorFile, row, e);
        }
    }


    private void generateTask(MarketingTask blt, ExecutorService warrningExecutor, MarketingCustomer customer, String day) {
//        if (blt.getActualNumber() <= 0) {
//            log.error("该批次监控人数为空，跳过执行:apiCode:{} batch_number：{}", blt.getApiCode(), blt.getBatchNumber());
//            return;
//        }
        String productJson = "";
        if (blt.getTaskType().compareTo(new Integer(0)) == 0) {
            productJson = strategyCS.strategyIdCheck(blt.getApiCode(), blt.getStrategyId());
        } else if (blt.getTaskType().compareTo(new Integer(2)) == 0) {
            productJson = blt.getProductInfo();
        }
        if (!blt.getTaskType().equals(1) && StringUtils.isEmpty(productJson)) {
            log.error("贷中策略不可用:apiCode:{} Strategy_id：{}", blt.getApiCode(), blt.getStrategyId());
            return;
        }

        String descPath = path.concat("/").concat(Constants.monitorTypeMap.get(String.valueOf(blt.getMonitorType()))).concat("/").concat(blt.getApiCode()).concat("/")
                .concat(blt.getBatchNumber()).concat("/").concat(day);

        if (blt.getFileId() != null && blt.getFileId() > 1) {
            StraHisFile file = straHisFileMapper.selectByPrimaryKey(blt.getFileId());
            descPath = file.getFilePath();
        } else {
            StraHisFile file = new StraHisFile();
            file.setApiCode(blt.getApiCode());
            file.setBatchNumber(blt.getBatchNumber());
            file.setFilePath(descPath);
            file.setCreateTime(new Date());
            file.setUpdateTime(new Date());
            file.setStatus(3);
            if (1 == blt.getMonitorType()) {
                file.setType(2);
            } else if (4 == blt.getMonitorType()) {
                file.setType(1);
            }
            file.setExpectedNum(blt.getActualNumber());
//            file.setShowTitle(createShowTitle(blt));
            straHisFileMapper.insertSelective(file);
            TaskStatus updateStatus = new TaskStatus();
            updateStatus.setId(blt.getStatusId());
            updateStatus.setFileId(file.getId());
            taskStatusMapper.updateByPrimaryKeySelective(updateStatus);
            blt.setFileId(file.getId());
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
                    marketingStrategyProduct.setFileId(blt.getId());
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
        }

        StringBuilder addTaskContent = new StringBuilder();
        addTaskContent.append(String.format("任务批次号:%s,分片:%d 加入队列", blt.getBatchNumber(), blt.getIndex()).concat("\r\n"));
        sendContent(addTaskContent.toString(), "任务开始", Constants.sendCodeMap.get("uploadSuccess"));
        core(blt, descPath, true, productJson, warrningExecutor, blt.getFileId().toString(), customer);
    }

    private void sendContent(String msg, String title, String code) {
        alarmClient.sendAlarm(msg, title, appName, secretKey, code);
    }

    /**
     * 提交任务
     *
     * @param blt
     * @param descPath
     */

    private void core(MarketingTask blt, String descPath, boolean firstTime, String strategyStr, ExecutorService warrningExecutor,
                      String fileId, MarketingCustomer customer) {
        try {
            String noflagproduct = redisChgService.get(RedisKeyConstant.noFlagProduct);
            List<String> noflagproductlist = new ArrayList<>();
            if (StringUtils.isNotBlank(noflagproduct)) {
                noflagproductlist = Splitter.on(",").splitToList(noflagproduct);
            } else {
                noflagproductlist.add("mappingcust");
                noflagproductlist.add("mappingcust1");
            }
            List<String> flagproductlist = new ArrayList<>();
            Result<List<String>> flagProduct = iProductResultSimpleService.getFlagProduct();
            if (flagProduct.getCode().equals(ResultCode.SUCCESS.getValue())) {
                flagproductlist = flagProduct.getData();
            }
            String strategyProductConfigStr = iProductResultSimpleService.getStrategyProductConfigStr(blt.getApiCode(), blt.getBatchNumber());
            StrategyProductDetailVO strategyProductDetailVO = new StrategyProductDetailVO();
            if (!StringUtils.isEmpty(strategyProductConfigStr)) {
                strategyProductDetailVO = JSON.parseObject(strategyProductConfigStr
                        , new TypeReference<StrategyProductDetailVO>() {
                        }.getType());
            }
            String separator = marketingSepService.querySepByApiCode(blt.getApiCode());
//            String baseHeadInfo = getBaseHeadInfo(blt.getId(), separator);
            String redisOpen = redisChgService.get(RedisEsOpen);
            Integer esOpenMark = StringUtils.isNotBlank(redisOpen) ? Integer.valueOf(redisOpen) : 1;
            MarketingTaskExtend marketingTaskExtend = marketingTaskExtendService.getMarketingTaskExtend(blt.getId());
            StraHisFile file = straHisFileMapper.selectByPrimaryKey(Long.valueOf(fileId));
            String day = new SimpleDateFormat("yyyy-MM-dd").format(file.getCreateTime());
            Result<List<String>> dataCondition = scoreRuleConfigService.getDataCondition(marketingTaskExtend, blt, day);
            AssertResult.assertResult(dataCondition);
            List<String> conditionDatas = dataCondition.getData();
            int currentPage = 1;
            long startTime = System.currentTimeMillis();
            for (String conditionData : conditionDatas) {
                Long minId = iDynamicSqlService.minIdRuleScoreWithDate(blt.getApiCode(), conditionData);
                log.warn("min_id--{},pageSize--{}", minId, pageSize);
                if (minId != null && minId > 0L) {
                    Integer actNum = 0;
                    Long begin = 0L;
                    Boolean threadpoolStatus = Boolean.TRUE;
                    while (threadpoolStatus) {
                        List<MarketingSyncUser> list = iDynamicSqlService.selectDataRuleScoreWithDate(blt.getApiCode(), conditionData, begin, pageSize);
                        if(list.size()<=0){
                            threadpoolStatus = Boolean.FALSE;
                            continue;
                        }
                        begin = list.get(list.size() - 1).getId();
                        if (!getCoreDataStatus(fileId, currentPage)) {
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
                            warrningExecutor.submit(new CoreScoreThread(
                                    list, param, currentPage
                                    , firstTime, customer, blt
                                    , noflagproductlist, flagproductlist, marketingTaskExtend));
                            if (warrningExecutor.isTerminated()) {
                                threadpoolStatus = Boolean.FALSE;
                            }
                            Thread.sleep(100);
                        }
                        currentPage++;
                    }
                } else {
                    log.warn(String.format("无符合条件的数据--apiCode:%s,batchNumber:%s", blt.getApiCode(), blt.getBatchNumber()));
                }
            }
            long endtime = System.currentTimeMillis();
            if (log.isWarnEnabled()) {
                log.warn("apicode:".concat(blt.getBatchNumber()).concat("~~查询总耗时："
                        .concat(String.valueOf(endtime - startTime)).concat("~~轮询总次数：")
                        .concat(String.valueOf(currentPage).concat("~~esOpen:").concat(esOpenMark.toString()))));
            }


        } catch (Exception e) {
            log.error("执行任务失败", e);
        }
    }

    /**
     * 获取跑数状态
     *
     * @param fileId
     * @param page
     * @return false-为暂未跑完；true-已经跑完；
     */
    boolean getCoreDataStatus(String fileId, Integer page) {
        String key = RedisKeyConstant.scoreStatus.concat(fileId).concat(":").concat(page.toString());
        String s = redisChgService.get(key);
        if (StringUtils.isBlank(s)) {
            return false;
        }
        if (s.equals("1")) {
            return true;
        } else {
            return false;
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

    private void threadNumListen(ThreadPoolExecutor executor, MarketingCustomer customer) {
        String zkpath = ZookeeperPath.marketPath.concat("/").concat(getLocalIp().concat("_")).concat(customer.getApiCode());
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
                int threadNum = Integer.valueOf(new String(nodeCache.getCurrentData().getData())).intValue();
                threadContextNum.put(customer.getApiCode(), threadNum);
                executor
                        .setCorePoolSize(threadNum);
                executor
                        .setMaximumPoolSize(threadNum);
            }
        });
        try {
            nodeCache.start();
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
                    log.warn(String.format("跑分线程线程状态(客户：%s,活动线程：%d,核心线程数：%d,变动线程数：%d)"
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

    private void removeZk(MarketingCustomer customer) {
        String zkpath = ZookeeperPath.marketPath.concat("/").concat(getLocalIp().concat("_")).concat(customer.getApiCode());
        try {
            client.delete().guaranteed().forPath(zkpath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
