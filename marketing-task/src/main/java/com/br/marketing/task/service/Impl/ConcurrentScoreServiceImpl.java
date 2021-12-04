package com.br.marketing.task.service.Impl;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.common.TaskExecCommonField;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.*;
import com.br.marketing.entity.*;
import com.br.marketing.exception.HxResultRuntimeException;
import com.br.marketing.mapper.*;
import com.br.marketing.service.IProductResultSimpleService;
import com.br.marketing.service.Impl.ProductResultByConfigSimpleServiceImpl;
import com.br.marketing.service.Impl.StrategyCs;
import com.br.marketing.service.MarketingSepService;
import com.br.marketing.service.MarketingTaskExtendService;
import com.br.marketing.service.ScoreRuleConfigService;
import com.br.marketing.task.Scheduler;
import com.br.marketing.task.service.LoanWarningService;
import com.br.marketing.task.thread.MarketingThread;
import com.br.marketing.vo.StrategyProductDetailVO;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * 单任务多片跑分
 */
@Service
@Slf4j
public class ConcurrentScoreServiceImpl implements LoanWarningService{
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
    MarketingTaskExtendService marketingTaskExtendService;
    @Resource
    ScoreRuleConfigService scoreRuleConfigService;

    @Autowired
    TaskStatusDistributeMapper taskStatusDistributeMapper;

    @Autowired
    StraHisFileMapper straHisFileMapper;

    @Autowired
    IProductResultSimpleService iProductResultSimpleService;

    private final static String RedisEsOpen="es:open";

    final static Integer allMonitorType = 4;


    /**
     * 1、initBatchNumList 方法统计出所有需要跑分的任务，并且每个任务属性上新增了分片信息和分片个数
     * 2、generateTask 执行跑分，会生成 跑分记录，跑分分片状态表记录
     * 3、失败数据重试
     * 4、删掉失败数据的redis
     * 5、修改跑分分片状态表记录 并且查询该分片所在的任务是不是都已经跑完，如果跑完更新跑分记录表。
     * @param customer
     * @param context
     */
    @Override
    public void process(Customer customer, JobExecutionMultipleShardingContext context){
        ExecutorService warrningExecutor;
        String apiCode=customer.getApiCode();

        if(customer.getThreadNum()==null){
            customer.setThreadNum(20);
        }
        warrningExecutor = BrExecutors.getThreadPool(customer.getThreadNum(),customer.getThreadNum());
        try{
            List<MarketingTask> taskList=new ArrayList<>();

            initBatchNumList(taskList,apiCode,context);

            this.generateTask(taskList,warrningExecutor,customer);


          /**
           * 等待所有任务都执行完成
           **/
          log.warn("所有任务已加入队列，等待结束-----");
            warrningExecutor.shutdown();
            while (true){
                if(warrningExecutor.isTerminated()){
                    log.warn("所有线程都执行结束");
                    break;
                }
                try {
                    Thread.sleep(6000);
                }catch (Exception e){
                }
            }

            try {
                String hkey=Constants.HXRESULTERROR_RETRY_KEY+":"+apiCode;
                Set<String> hkeys = redisChgService.hkeys(hkey);
                if(!hkeys.isEmpty()&&hkeys.size()>0){
                    warrningExecutor = BrExecutors.getThreadPool(20,20);
                    int i=1;
                    for(String errorFile:hkeys){
                        String batchNumber = redisChgService.hget(hkey, errorFile);
                        MarketingTask task =marketingTaskMapper.queryBlt(batchNumber);
                        if(task!=null) {
                            String[] split = errorFile.split("/");
                            if(split.length>2){
                                for (Integer shardingItem : context.getShardingItems()) {
                                    if(Integer.valueOf(split[split.length-2]).equals(shardingItem)) {
                                        this.retry(task,apiCode, batchNumber, errorFile, warrningExecutor, i, customer, shardingItem, context.getShardingTotalCount());
                                    }else{
                                        continue;
                                    }
                                }
                            }else{
                                continue;
                            }

                            i++;
                        }
                    }
                    log.warn("所有重试任务已加入队列，等待结束-----");
                    warrningExecutor.shutdown();
                    while (true){
                        if(warrningExecutor.isTerminated()){
                            log.warn("重试任务所有线程都执行结束");
                            break;
                        }
                        try {
                            Thread.sleep(6000);
                        }catch (Exception e){
                        }
                    }
                    for(String errorFile:hkeys){
                        String batchNumber = redisChgService.hget(hkey, errorFile);
                        MarketingTask task =marketingTaskMapper.queryBlt(batchNumber);
                        if(task!=null) {
                            String[] split = errorFile.split("/");
                            if(split.length>2){
                                for (Integer shardingItem : context.getShardingItems()) {
                                    if(Integer.valueOf(split[split.length-2]).equals(shardingItem)) {
                                        redisChgService.hdel(hkey,errorFile);
                                    }else{
                                        continue;
                                    }
                                }
                            }else{
                                continue;
                            }
                        }
                    }
                }
            }catch (Exception e){
                log.error("重新处理异常数据出错",e);
            }
            for (MarketingTask task : taskList) {
                TaskStatusDistribute updateRecord = new TaskStatusDistribute();
                updateRecord.setStatus(2);
                updateRecord.setFileId(task.getFileId());
                updateRecord.setDistributeIndex(task.getIndex());
                TaskStatusDistributeExample example = new TaskStatusDistributeExample();
                example.createCriteria()
                        .andFileIdEqualTo(task.getFileId())
                        .andDistributeIndexEqualTo(task.getIndex())
                        .andIsDelEqualTo(Constants.DATA_VALID);
                taskStatusDistributeMapper.updateByExampleSelective(updateRecord,example);

                StraHisFile file = straHisFileMapper.selectByPrimaryKey(task.getFileId());

                TaskStatusDistributeExample selStatusExample = new TaskStatusDistributeExample();
                selStatusExample.createCriteria()
                        .andFileIdEqualTo(task.getFileId())
                        .andStatusEqualTo(2)
                        .andIsDelEqualTo(Constants.DATA_VALID);
                int statusCount = taskStatusDistributeMapper.countByExample(selStatusExample);
                if(file.getIndexNum().equals(statusCount)){
                    StraHisFile updateFile = new StraHisFile();
                    updateFile.setId(task.getFileId());
                    updateFile.setStatus(1);
                    straHisFileMapper.updateByPrimaryKeySelective(updateFile);
                }
            }
        }catch (Exception e){
            log.error("预警调度出错",e);
        }
        return;
    }

    /**
     * 重新处理异常数据
     * @param apiCode 商户编号
     * @param batchNumber 批次号
     * @param errorFile 异常数据记录文件
     * @param warrningExecutor 线程池
     * @param num 文件编号
     */
    private void retry(MarketingTask task,String apiCode,String batchNumber,String errorFile,ExecutorService warrningExecutor
            ,Integer num,Customer customer,Integer index,Integer indexCount){
        String noflagproduct = redisChgService.get(RedisKeyConstant.noFlagProduct);
        List<String> noflagproductlist = new ArrayList<>();
        if(StringUtils.isNotBlank(noflagproduct)){
            noflagproductlist = Splitter.on(",").splitToList(noflagproduct);
        }else{
            noflagproductlist.add("mappingcust");
            noflagproductlist.add("mappingcust1");
        }
        List<String> flagproductlist = new ArrayList<>();
        Result<List<String>> flagProduct = iProductResultSimpleService.getFlagProduct();
        if(flagProduct.getCode().equals(ResultCode.SUCCESS.getValue())){
            flagproductlist = flagProduct.getData();
        }
        String strategyProductConfigStr = iProductResultSimpleService.getStrategyProductConfigStr(task.getApiCode(),task.getBatchNumber());
        StrategyProductDetailVO strategyProductDetailVO = new StrategyProductDetailVO();
        if(!StringUtils.isEmpty(strategyProductConfigStr)){
            strategyProductDetailVO = JSON.parseObject(strategyProductConfigStr
                    , new TypeReference<StrategyProductDetailVO>() {
                    }.getType());
        }
        MarketingTask marketingTask = marketingTaskMapper.queryBlt(batchNumber);
        marketingTask.setIndex(index);
        marketingTask.setIndexCount(indexCount);
        String separator=marketingSepService.querySepByApiCode(apiCode);
        String baseHeadInfo = getBaseHeadInfo(marketingTask.getId(), separator);
        Map<String,String> paramMap =new HashedMap();
        paramMap.put("apiCode",apiCode);
        paramMap.put("batchNumber",batchNumber);
        LoanFile  file =loanFileMapper.selectFileComplete(paramMap);

        String  productJson="";
        if(marketingTask.getTaskType().compareTo(new Integer(0))==0){
            productJson=strategyCS.strategyIdCheck(marketingTask.getApiCode(),marketingTask.getStrategyId());
        }else if(marketingTask.getTaskType().compareTo(new Integer(1))==0){
            return;
        }else if(marketingTask.getTaskType().compareTo(new Integer(2))==0){
            productJson=marketingTask.getProductInfo();
        }
        if(StringUtils.isEmpty(productJson)){
            log.error("贷中策略不可用:apiCode:{} Strategy_id：{}",apiCode, marketingTask.getStrategyId());
            return;
        }
        String dateAddYyMmDd = DateHelper.getDateAddYyMmDd(0);
        String s = dateAddYyMmDd + num.toString();
        String row = null;
        int currentPage=Integer.parseInt(s);
        try(FileReader read = new FileReader(errorFile);
            BufferedReader br = new BufferedReader(read);){
            List<MarketingUser> list=new ArrayList<>();
            while ((row = br.readLine()) != null) {
                String[] split = row.split(",");
                log.info("split length{}",split.length);
                MarketingUser lu=new MarketingUser();
                lu.setApiCode(apiCode);
                lu.setBatchNumber(split[0]);
                lu.setCusNum(split[1]);
                lu.setIdCard(split[2]);
                lu.setCell(split[3]);
                lu.setName(split[4]);
                lu.setStatus(1);
                lu.setHitData(split[5]);
                list.add(lu);
            }
            String[] split = errorFile.split("/");
            String s1 = split[6];


            String descPath=path+"/"+s1+"/"+ marketingTask.getApiCode()+"/"+ marketingTask.getBatchNumber()+"/"+
                    new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            log.info("{},list:{}",errorFile,list.size());
            Map<String,String> param=new HashMap<>();
            param.put("apiCode", marketingTask.getApiCode());
            param.put("strategyId", marketingTask.getStrategyId());
            param.put("path",descPath);
            param.put("strategyStr",productJson);
            param.put("sep",separator);
            param.put("batchNumber", marketingTask.getBatchNumber());
            param.put("cusBatchNumber",marketingTask.getFileName());
            param.put("url",url);
            param.put("appSecretKey",appSecretKey);
            param.put("isRepair", marketingTask.getIsRepair());
            param.put("fileId",file.getId().toString());
            param.put("baseHeadInfo",StringUtils.isNotBlank(baseHeadInfo)
                    ?baseHeadInfo.substring(0,baseHeadInfo.length()-1):"");
            warrningExecutor.submit(new MarketingThread(list, param,currentPage,true,customer,marketingTask,noflagproductlist,flagproductlist,strategyProductDetailVO));
        }catch (Exception e){
            log.error("重新处理画像异常数据出错:{},{}",errorFile,row,e);
        }
    }


    /**
     * 提交一次性任务或周期性任务
     * @param list
     */
    private void generateTask(List<MarketingTask> list, ExecutorService warrningExecutor,Customer customer){
        if(list==null||list.size()==0) {
            return;
        }
        for(MarketingTask blt:list){
            if(blt.getActualNumber()<=0){
                log.error("该批次监控人数为空，跳过执行:apiCode:{} batch_number：{}",blt.getApiCode(), blt.getBatchNumber());
                continue;
            }
            String  productJson="";
            if(blt.getTaskType().compareTo(new Integer(0))==0){
                productJson=strategyCS.strategyIdCheck(blt.getApiCode(),blt.getStrategyId());
            }else if(blt.getTaskType().compareTo(new Integer(1))==0){
                continue;
            }else if(blt.getTaskType().compareTo(new Integer(2))==0){
                productJson=blt.getProductInfo();
            }
            if(StringUtils.isEmpty(productJson)){
                log.error("贷中策略不可用:apiCode:{} Strategy_id：{}",blt.getApiCode(), blt.getStrategyId());
                continue;
            }
                log.warn("batchNumber:{}",blt.getBatchNumber());
                blt.setTableName("b_marketing_user_"+blt.getApiCode());
                String descPath=path.concat("/").concat(Constants.monitorTypeMap.get(String.valueOf(blt.getMonitorType()))).concat("/").concat(blt.getApiCode()).concat("/")
                        .concat(blt.getBatchNumber()).concat("/").concat(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                Integer pushType=0;
            ScoreRuleConfig scoreRuleConfig =getScoreRuleConfig(blt);
            if(scoreRuleConfig !=null){
                pushType=scoreRuleConfig.getPushType();
            }
            /**
             * 任务提交前，在stra_his_file表中插入一条数据（记录当天该批次的结果文件信息，用于结果文件合并和推送）
             */
            LoanFile blf=new LoanFile();
            blf.setApiCode(blt.getApiCode());
            blf.setFilePath(descPath);
            blf.setStatus(3);
            if(1 == blt.getMonitorType()){
                blf.setType(2);
            }else if(4==blt.getMonitorType()){
                blf.setType(1);
            }
            blf.setBatchNumber(blt.getBatchNumber());
            blf.setExpectedNum(blt.getActualNumber());
            blf.setShowTitle(createShowTitle(blt));
            blf.setPushType(pushType);
            blf.setIndexNum(blt.getIndexCount());
            boolean fileMark = inserTaskInfo(blf,customer.getPushCustomer()==1?JSONArray.parseArray(productJson):null,blt);
            if(!fileMark){
                log.error("创建跑分记录有问题，校验redis或者tidb网络是否有问题:apiCode:{} batchNumber：{}",blt.getApiCode(), blt.getBatchNumber());
                continue;
            }
            core(blt,descPath,true,productJson,warrningExecutor,blf.getId().toString(),customer);

            if(TaskExecCommonField.isExecTaskJob.equals(2)){
                TaskExecCommonField.isExecTaskJob = 3;
                StringBuilder content = new StringBuilder();
                content.append("当前正在停止跑分的任务批次号：".concat(blt.getBatchNumber()).concat("\r\n"));
                alarmClient.sendAlarm(content.toString(),"跑分暂停",appName,secretKey,
                        Constants.sendCodeMap.get("uploadSuccess"));
            }
        }

    }

    /**
     * 提交任务
     * @param blt
     * @param descPath
     */

    private void core(MarketingTask blt, String descPath, boolean firstTime, String strategyStr, ExecutorService warrningExecutor,
                      String fileId,Customer customer){
        try {
            String noflagproduct = redisChgService.get(RedisKeyConstant.noFlagProduct);
            List<String> noflagproductlist = new ArrayList<>();
            if(StringUtils.isNotBlank(noflagproduct)){
                noflagproductlist = Splitter.on(",").splitToList(noflagproduct);
            }else{
                noflagproductlist.add("mappingcust");
                noflagproductlist.add("mappingcust1");
            }
            List<String> flagproductlist = new ArrayList<>();
            Result<List<String>> flagProduct = iProductResultSimpleService.getFlagProduct();
            if(flagProduct.getCode().equals(ResultCode.SUCCESS.getValue())){
                flagproductlist = flagProduct.getData();
            }
            String strategyProductConfigStr = iProductResultSimpleService.getStrategyProductConfigStr(blt.getApiCode(),blt.getBatchNumber());
            StrategyProductDetailVO strategyProductDetailVO = new StrategyProductDetailVO();
            if(!StringUtils.isEmpty(strategyProductConfigStr)){
                strategyProductDetailVO = JSON.parseObject(strategyProductConfigStr
                        , new TypeReference<StrategyProductDetailVO>() {
                        }.getType());
            }
            String separator=marketingSepService.querySepByApiCode(blt.getApiCode());
            String baseHeadInfo = getBaseHeadInfo(blt.getId(), separator);
            String redisOpen = redisChgService.get(RedisEsOpen);
            Integer esOpenMark = StringUtils.isNotBlank(redisOpen)?Integer.valueOf(redisOpen):1;
                Long minId= marketingUserMapper.queryMinId(blt);
                Long maxId= marketingUserMapper.queryMaxId(blt);
                log.warn("min_id--{},max_id--{},pageSize--{}",minId,maxId,pageSize);

            /**
             * 全量任务提交前，在b_task_status表中插入一条数据（标识全量任务已执行，之后应该按增量处理）
             */
            TaskStatusDistribute statusDistribute = new TaskStatusDistribute();
            statusDistribute.setFileId(Long.valueOf(fileId));
            statusDistribute.setApiCode(blt.getApiCode());
            statusDistribute.setBatchNumber(blt.getBatchNumber());
            statusDistribute.setDistributeIndex(blt.getIndex());
            Date date = new Date();
            statusDistribute.setCreateTime(date);
            statusDistribute.setUpdateTime(date);
            taskStatusDistributeMapper.insertSelective(statusDistribute);
                if(minId !=null && minId>0L) {
                    Long begin = minId - 1;
                    int currentPage = 1;
                    long start = System.currentTimeMillis();
                    Integer actNum = 0;
                    while (begin < maxId && TaskExecCommonField.isExecTaskJob.equals(1)) {
                        blt.setBegin(begin);
                        List<MarketingUser> list = marketingUserMapper.queryUserByid(blt);
                        begin = list.get(list.size() - 1).getId();
                        if (list.size() > 0 && blt.getIndex().equals(currentPage%blt.getIndexCount())) {
                            actNum+=list.size();
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
                            param.put("noflagproduct",noflagproduct);
                            param.put("baseHeadInfo",StringUtils.isNotBlank(baseHeadInfo)
                                    ?baseHeadInfo.substring(0,baseHeadInfo.length()-1):"");
                            warrningExecutor.submit(new MarketingThread(list, param,currentPage,firstTime,customer,blt,noflagproductlist,flagproductlist,strategyProductDetailVO));
                            Thread.sleep(100);
                        }
                        currentPage++;
                    }
                    long endtime = System.currentTimeMillis();

                    if (log.isWarnEnabled()) {
                        log.warn("apicode:".concat(blt.getBatchNumber()).concat("~~查询总耗时："
                                .concat(String.valueOf(endtime - start)).concat("~~轮询总次数：")
                                .concat(String.valueOf(currentPage).concat("~~esOpen:").concat(esOpenMark.toString()))));
                    }
                    TaskStatusDistribute updateStatus = new TaskStatusDistribute();
                    updateStatus.setActualNum(Long.valueOf(actNum));
                    updateStatus.setId(statusDistribute.getId());
                    taskStatusDistributeMapper.updateByPrimaryKeySelective(updateStatus);
                }else{
                    log.warn(String.format("无符合条件的数据--apiCode:%s,batchNumber:%s",blt.getApiCode(),blt.getBatchNumber()));
                }

        }catch (Exception e){
            log.error("执行任务失败",e);
        }
    }

    private String getBaseHeadInfo(Long taskId,String separator){
        MarketingTaskExtend taskExtend = marketingTaskExtendService.getMarketingTaskExtend(taskId);
        if(taskExtend !=null&&StringUtils.isNotBlank(taskExtend.getExtendShowTitle())){
            return taskExtend.getExtendShowTitle().concat(separator);
        }
        return "";
    }

    /**
     * 初始化当日需要监控的任务信息
     * 遍历任务
     *  遍历分片信息，根据任务和分片信息查找当前任务分片的状态记录是否存在 存在即加入待跑list，并且携带分片信息
     * @param taskList
     */
    private void initBatchNumList(List<MarketingTask> taskList, String apiCode,
                                  JobExecutionMultipleShardingContext context){
        try{
            List<MarketingTask> list= marketingTaskMapper.queryBatchNumByapiCode(apiCode);
            log.warn("当日批次数量--{}",list.size());
            for(MarketingTask blt:list) {

                if(blt.getContextId()==null){
                    continue;
                }
                if (1 == blt.getMonitorType()) {
                    List<TaskStatus> bts = taskStatusMapper.queryOnceBts(blt.getBatchNumber());
                    if (bts.size()>0) {
                        continue;
                    }
                    context.getShardingItems().forEach(t->{
                        if(taskCanAction(blt,t)){
                            MarketingTask task = new MarketingTask();
                            BeanUtils.copyProperties(blt,task);
                            task.setIndex(t);
                            task.setIndexCount(context.getShardingTotalCount());
                            taskList.add(task);
                        }
                    });
                }else if(4 == blt.getMonitorType()){
                    int days = 0;
                    try {
                        days = DateHelper.daysBetween(blt.getStartDate());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Integer cycleDay;
                    if(StringUtils.isNotBlank(blt.getCycleDay())){
                        cycleDay = Integer.valueOf(blt.getCycleDay());
                    }else{
                        cycleDay = Constants.frequencyMap.get(blt.getFrequency());
                    }
                    if(cycleDay==null||cycleDay == 0){
                        continue;
                    }
                    if(days%cycleDay==0){
                        TaskStatusExample statusExample= new TaskStatusExample();
                        statusExample.createCriteria()
                                .andBatchNumberEqualTo(blt.getBatchNumber())
                                .andAllStatusEqualTo(1)
                                .andCreateTimeGreaterThanOrEqualTo(DateHelper.getDateAdd(0))
                                .andCreateTimeLessThan(DateHelper.getDateAdd(1));
                        List<TaskStatus> taskStatuses = taskStatusMapper.selectByExample(statusExample);
                        if(taskStatuses.size()>0) {
                            continue;
                        }

                        context.getShardingItems().forEach(t->{
                            if(taskCanAction(blt,t)){
                                MarketingTask task = new MarketingTask();
                                BeanUtils.copyProperties(blt,task);
                                task.setIndex(t);
                                task.setIndexCount(context.getShardingTotalCount());
                                taskList.add(task);
                            }
                        });
                    }
                }
            }
            log.warn("当日待跑批任务数量-{}",taskList.size());
        }catch (Exception e){
            log.error("初始化任务出错",e);
        }
    }

    private boolean taskCanAction(MarketingTask task,Integer index){
        Date nowDayStartTime = DateHelper.getNowDayStartTime();
        Date newDay = DateHelper.addDays(nowDayStartTime, 1);

        //防止程序运行中 添加分片导致的数据多跑
        StraHisFileExample fileExample = new StraHisFileExample();
        fileExample.createCriteria()
                .andBatchNumberEqualTo(task.getBatchNumber())
                .andCreateTimeGreaterThanOrEqualTo(nowDayStartTime)
                .andCreateTimeLessThan(newDay);
        List<StraHisFile> straHisFiles = straHisFileMapper.selectByExample(fileExample);
        if(straHisFiles.size()>0){
            StraHisFile file = straHisFiles.get(0);
            if(file.getIndexNum()>0&&(index>file.getIndexNum()-1)){
                return false;
            }
        }
        //一次性任务，该分片只有有状态数据就不能跑
        if(task.getMonitorType().equals(1)){
            TaskStatusDistributeExample exampleOnce = new TaskStatusDistributeExample();
            exampleOnce.createCriteria()
                    .andBatchNumberEqualTo(task.getBatchNumber())
                    .andDistributeIndexEqualTo(index)
                    .andIsDelEqualTo(Constants.DATA_VALID);
            List<TaskStatusDistribute> exampleOnceStatus = taskStatusDistributeMapper.selectByExample(exampleOnce);
            if(exampleOnceStatus.size()>0){
                return false;
            }else{
                return true;
            }
        }
        TaskStatusDistributeExample example = new TaskStatusDistributeExample();
        example.createCriteria()
                .andBatchNumberEqualTo(task.getBatchNumber())
                .andDistributeIndexEqualTo(index)
                .andIsDelEqualTo(Constants.DATA_VALID)
                .andCreateTimeGreaterThanOrEqualTo(nowDayStartTime)
        .andCreateTimeLessThan(newDay);
        List<TaskStatusDistribute> taskStatusDistributes = taskStatusDistributeMapper.selectByExample(example);
        if(taskStatusDistributes.size()==0){
            return true;
        }else{
            return false;
        }
    }

    private boolean inserTaskInfo(LoanFile blf,JSONArray pList,MarketingTask task){
        boolean actionMark = Boolean.TRUE;
        Integer actionCount =0;
        while (actionMark){
            String dateAddYyMmDd = DateHelper.getDateAddYyMmDd(0);
            String key = "distribute:taskfile:".concat(blf.getBatchNumber().concat(":").concat(dateAddYyMmDd));
            String v = String.valueOf(System.currentTimeMillis());
            Date nowDate = null;
            try {
                nowDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .parse(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")).concat(" 00:00:00"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            StraHisFileExample hisFileExample = new StraHisFileExample();
            hisFileExample.createCriteria()
                    .andApiCodeEqualTo(blf.getApiCode())
                    .andBatchNumberEqualTo(blf.getBatchNumber())
                    .andCreateTimeGreaterThanOrEqualTo(nowDate);
            List<StraHisFile> straHisFiles = straHisFileMapper.selectByExample(hisFileExample);
            if(straHisFiles.size()>0){
                StraHisFile file = straHisFiles.get(0);
                blf.setId(file.getId());
                task.setFileId(file.getId());
                return true;
            }

            if(redisChgService.setnx(key,v,2).equals(1L)){
                loanFileMapper.insertFile(blf);
                task.setFileId(blf.getId());
                if(pList != null) {
                    for (int i = 0; i < pList.size(); i++) {
                        JSONObject jsonObject = pList.getJSONObject(i);
                        MarketingStrategyProduct marketingStrategyProduct = new MarketingStrategyProduct();
                        marketingStrategyProduct.setApiCode(task.getApiCode());
                        marketingStrategyProduct.setBatchNumber(task.getBatchNumber());
                        marketingStrategyProduct.setCreateTime(new Date());
                        marketingStrategyProduct.setCusBatchNumber(task.getFileName());
                        marketingStrategyProduct.setProductName(jsonObject.getString("code"));
                        marketingStrategyProduct.setProductVersion(jsonObject.getString("version"));
                        marketingStrategyProduct.setStrategyId(task.getStrategyId());
                        marketingStrategyProduct.setFileId(blf.getId());
                        marketingStrategyProductMapper.insertSelective(marketingStrategyProduct);
                    }
                }
                if(redisChgService.get(key).equals(v)){
                    redisChgService.del(key);
                }
                return true;
            }else{
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            actionCount++;
            if(actionCount.equals(3)){
                actionMark=Boolean.FALSE;
            }
        }
        return false;

    }

    private String createShowTitle(MarketingTask task){
        SimpleDateFormat yyyy_MM_dd = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd");
        MarketingTaskExtendExample extendExample = new MarketingTaskExtendExample();
        extendExample.createCriteria()
                .andTaskIdEqualTo(Long.valueOf(task.getId()))
                .andIsDelEqualTo(1);
        MarketingTaskExtend extend = marketingTaskExtendService.getMarketingTaskExtend(task.getId());
        if(extend !=null){
            String groupStr = "";
            ScoreRuleConfig scoreRule = scoreRuleConfigService.getScoreRule(extend.getRuleId());
            if(scoreRule !=null){
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
        if(allMonitorType.equals(task.getMonitorType())){
            return task.getCusBatch().concat("_").concat(yyyyMMdd.format(new Date()));
        }
        return task.getCusBatch();
    }
    private ScoreRuleConfig getScoreRuleConfig(MarketingTask task){

        ScoreRuleConfig scoreRuleConfig=null;
        MarketingTaskExtend marketingTaskExtend = marketingTaskExtendService.getMarketingTaskExtend(task.getId());
        if(marketingTaskExtend !=null) {
            scoreRuleConfig= scoreRuleConfigService.getScoreRule(marketingTaskExtend.getRuleId());
        }
        return scoreRuleConfig;
    }
}
