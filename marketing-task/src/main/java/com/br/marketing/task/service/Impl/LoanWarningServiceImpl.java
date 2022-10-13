package com.br.marketing.task.service.Impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.*;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.exception.HxResultRuntimeException;
import com.br.marketing.mapper.*;
import com.br.marketing.service.IProductResultSimpleService;
import com.br.marketing.service.Impl.StrategyCs;
import com.br.marketing.service.MarketingSepService;
import com.br.marketing.service.MarketingTaskExtendService;
import com.br.marketing.service.ScoreRuleConfigService;
import com.br.marketing.task.service.LoanWarningService;
import com.br.marketing.task.thread.LoanWarningThread;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.HashedMap;
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
 * Created by Bairong on 2019/8/20.
 * 流失预警通用模式
 */
@Service
@Slf4j
public class LoanWarningServiceImpl  implements LoanWarningService{
    @Value("${otherConfig.warning.pageSize:00}")
    private Integer pageSize;
    @Value("${otherConfig.warning.path:00}")
    private String path;
    @Value("${otherConfig.mom.appSecretKey:00}")
    private String appSecretKey;

    @Value("${otherConfig.huaXiangInterface.getReport:00}")
    private String url;

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
    @Resource
    private AlarmApiClient alarmClient;

    private final static String RedisEsOpen="es:open";

    final static Integer allMonitorType = 4;

    @Override
    public void process(Customer customer, JobExecutionMultipleShardingContext context){

        int count=context==null?1:context.getShardingTotalCount();
        List<Integer> itemList =context==null?Arrays.asList(0):context.getShardingItems();
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
                        if(task!=null&&itemList.contains(Integer.valueOf(task.getContextId().toString())%count)) {
                            this.retry(apiCode,batchNumber,errorFile,warrningExecutor,i,customer);
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
                        if(task!=null&&itemList.contains(Integer.valueOf(task.getContextId().toString())%count)) {
                            redisChgService.hdel(hkey,errorFile);
                        }
                    }
                }
            }catch (Exception e){
                log.error("重新处理异常数据出错",e);
            }
            for (MarketingTask task : taskList) {
                Map<String,String> param =new HashedMap();
                param.put("apiCode",apiCode);
                param.put("batchNumber",task.getBatchNumber());
                loanFileMapper.updateFileComplete(param);
            }

            try {
                String hkey= Constants.HX_FLAG_98_NUM+":"+apiCode;
                Set<String> hkeys = redisChgService.hkeys(hkey);
                Integer sum=0;
                for (String batchNumber : hkeys) {
                    for (MarketingTask task : taskList) {
                        if(task.getBatchNumber().equals(batchNumber)){
                            if(task!=null&&itemList.contains(Integer.valueOf(task.getContextId().toString())%count)) {
                                String flagKey=Constants.HX_FLAG_98_NUM+ batchNumber+"_"+DateHelper.getDateAddYyMmDd(0);
                                String num = redisChgService.get(flagKey);
                                if(StringUtils.isNotEmpty(num)&&Integer.parseInt(num)>0){
                                    sum+=Integer.parseInt(num);
                                }
                                redisChgService.expire(flagKey,5);
                            }
                        }
                    }
                }
                if(sum>0){
                    HxResultRuntimeException hxResultRuntimeException = new HxResultRuntimeException(
                            String.format("【紧急报警】【%s】营销平台客户监控- 数据产品flag异常  \001 您好:  数据产品flag异常,flag为98的请求有：%s条，请及时跟进"
                                    ,apiCode,sum));
                    log.warn("hxResult product flag error",hxResultRuntimeException);
                    String title = String.format("【紧急报警】【%s】智能营销平台- 数据产品flag异常", apiCode);
                    alarmClient.sendAlarm(hxResultRuntimeException.getMessage(),title, Constants.sendCodeMap.get("huaxiangCommonly"));
                }

            }catch (Exception e){
                log.error("发送报警出错",e);
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
    private void retry(String apiCode,String batchNumber,String errorFile,ExecutorService warrningExecutor,Integer num,Customer customer){
        MarketingTask marketingTask = marketingTaskMapper.queryBlt(batchNumber);
        String separator=marketingSepService.querySepByApiCode(apiCode);
        String baseHeadInfo = getBaseHeadInfo(marketingTask.getId(), separator);
        Map<String,String> paramMap =new HashedMap();
        paramMap.put("apiCode",apiCode);
        paramMap.put("batchNumber",batchNumber);
        LoanFile  file =loanFileMapper.selectFileComplete(paramMap);
        String  strategyStr=strategyCS.strategyIdCheck(apiCode, marketingTask.getStrategyId());
        if(StringUtils.isEmpty(strategyStr)){
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
                String[] split = row.split("#");
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
                lu.setExtendJson(split[6]);
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
            param.put("strategyStr",strategyStr);
            param.put("sep",separator);
            param.put("batchNumber", marketingTask.getBatchNumber());
            param.put("cusBatchNumber",marketingTask.getFileName());
            param.put("url",url);
            param.put("appSecretKey",appSecretKey);
            param.put("isRepair", marketingTask.getIsRepair());
            param.put("fileId",file.getId().toString());
            param.put("baseHeadInfo",StringUtils.isNotBlank(baseHeadInfo)
                    ?baseHeadInfo.substring(0,baseHeadInfo.length()-1):"");
            warrningExecutor.submit(new LoanWarningThread(list, param,currentPage,true,customer));
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
           String  strategyStr=strategyCS.strategyIdCheck(blt.getApiCode(),blt.getStrategyId());
            if(StringUtils.isEmpty(strategyStr)){
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
            loanFileMapper.insertFile(blf);
            if(customer.getPushCustomer()==1){
                JSONArray dtbArray=JSONArray.parseArray(strategyStr);
                for(int i=0;i<dtbArray.size();i++){
                    JSONObject jsonObject = dtbArray.getJSONObject(i);
                    MarketingStrategyProduct marketingStrategyProduct = new MarketingStrategyProduct();
                    marketingStrategyProduct.setApiCode(blt.getApiCode());
                    marketingStrategyProduct.setBatchNumber(blt.getBatchNumber());
                    marketingStrategyProduct.setCreateTime(new Date());
                    marketingStrategyProduct.setCusBatchNumber(blt.getFileName());
                    marketingStrategyProduct.setProductName(jsonObject.getString("code"));
                    marketingStrategyProduct.setProductVersion(jsonObject.getString("version"));
                    marketingStrategyProduct.setStrategyId(blt.getStrategyId());
                    marketingStrategyProduct.setFileId(blf.getId());
                    marketingStrategyProductMapper.insertSelective(marketingStrategyProduct);
                }
            }

            /**
             * 全量任务提交前，在b_task_status表中插入一条数据（标识全量任务已执行，之后应该按增量处理）
             */
            TaskStatus bts=new TaskStatus();
            if(1 == blt.getMonitorType()){
                bts.setOnceStatus(1);
            }else if(4==blt.getMonitorType()){
                bts.setAllStatus(1);
            }
            bts.setApiCode(blt.getApiCode());
            bts.setBatchNumber(blt.getBatchNumber());
            bts.setFileId(blf.getId());
            taskStatusMapper.insertTaskStatus(bts);

            //Boolean firstTime=blt.getFirstTime()==null?Boolean.FALSE:blt.getFirstTime();
            core(blt,descPath,true,strategyStr,warrningExecutor,blf.getId().toString(),customer);

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
            String separator=marketingSepService.querySepByApiCode(blt.getApiCode());
            String baseHeadInfo = getBaseHeadInfo(blt.getId(), separator);
            String redisOpen = redisChgService.get(RedisEsOpen);
            Integer esOpenMark = StringUtils.isNotBlank(redisOpen)?Integer.valueOf(redisOpen):1;
                Long minId= marketingUserMapper.queryMinId(blt);
                Long maxId= marketingUserMapper.queryMaxId(blt);
                log.warn("min_id--{},max_id--{},pageSize--{}",minId,maxId,pageSize);
                if(minId !=null && minId>0L) {
                    Long begin = minId - 1;
                    int currentPage = 1;
                    long start = System.currentTimeMillis();
                    while (begin < maxId) {
                        blt.setBegin(begin);
                        List<MarketingUser> list = marketingUserMapper.queryUserByid(blt);
                        if (list.size() > 0) {
                            begin = list.get(list.size() - 1).getId();
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
                            param.put("baseHeadInfo",StringUtils.isNotBlank(baseHeadInfo)
                                    ?baseHeadInfo.substring(0,baseHeadInfo.length()-1):"");
                            warrningExecutor.submit(new LoanWarningThread(list, param,currentPage,firstTime,customer));
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
     * @param taskList
     */
    private void initBatchNumList(List<MarketingTask> taskList, String apiCode,
                                  JobExecutionMultipleShardingContext context){
        int count=context==null?1:context.getShardingTotalCount();
        List<Integer> itemList =context==null?Arrays.asList(0):context.getShardingItems();
        try{
            List<MarketingTask> list= marketingTaskMapper.queryBatchNumByapiCode(apiCode);
            log.warn("当日批次数量--{}",list.size());
            for(MarketingTask blt:list) {
                if(blt.getContextId()==null){
                    continue;
                }
                if(itemList.contains(Integer.valueOf(blt.getContextId().toString())%count)||context==null){
                    if (1 == blt.getMonitorType()) {
                        List<TaskStatus> bts = taskStatusMapper.queryOnceBts(blt.getBatchNumber());
                        if (bts.size()==0) {
                            //blt.setFirstTime(Boolean.TRUE);
                            taskList.add(blt);
                        }
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
                            if(taskStatuses.size()<=0) {
                                taskList.add(blt);
                            }
                        }
                    }
                }
            }
            log.warn("当日待跑批任务数量-{}",taskList.size());
        }catch (Exception e){
            log.error("初始化任务出错",e);
        }
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
