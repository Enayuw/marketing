package com.br.marketing.task.service.Impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.*;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.exception.HxResultRuntimeException;
import com.br.marketing.mapper.*;
import com.br.marketing.service.IProductResultSimpleService;
import com.br.marketing.service.Impl.StrategyCs;
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
    IProductResultSimpleService iProductResultSimpleService;

    @Resource
    GroupStrategyConfigMapper groupStrategyConfigMapper;

    @Resource
    MarketingTaskExtendMapper marketingTaskExtendMapper;

    final SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd");

    final SimpleDateFormat yyyy_MM_dd = new SimpleDateFormat("yyyy-MM-dd");

    private final static String RedisEsOpen="es:open";

    final static Integer allMonitorType = 4;

    @Override
    public void process(Customer customer, JobExecutionMultipleShardingContext context){
        //todo 预发需要去掉这个逻辑
        int count=context==null?1:context.getShardingTotalCount();
        List<Integer> itemList =context==null?Arrays.asList(0):context.getShardingItems();
        String type=customer.getType();
        ExecutorService warrningExecutor;
        String apiCode=customer.getApiCode();
        if(customer.getThreadNum()!=null){
            warrningExecutor = BrExecutors.getThreadPool(customer.getThreadNum(),customer.getThreadNum());
        }else{
            warrningExecutor = BrExecutors.getThreadPool(20,20);
        }
        try{
            List<MarketingTask> allList=new ArrayList<>();
            List<MarketingTask> onceList=new ArrayList<>();
            initBatchNumList(allList,onceList,apiCode,context);
            if(type.contains("all")){
                this.generateAllTask(allList,warrningExecutor,customer);
            }
            if(type.contains("once")){
                this.generateOnceTask(onceList,warrningExecutor,customer);
            }

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
            allList.addAll(onceList);
            for (MarketingTask task : allList) {
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
                    for (MarketingTask task : allList) {
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
                    log.error("hxResult product flag error",hxResultRuntimeException);
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
        Integer sep= marketingTaskMapper.querySep(apiCode);
        String separator= Constants.sepMap.get(sep);
        String baseHeadInfo = this.getBaseHeadInfo(marketingTask.getId(), separator);
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
        int i=Integer.parseInt(s);
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
            boolean flag=false;
            if("incr".equals(s1)){
                flag=true;
            }
            String redisOpen = redisChgService.get(RedisEsOpen);
            Integer esOpenMark = StringUtils.isNotBlank(redisOpen)?Integer.valueOf(redisOpen):1;
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
            warrningExecutor.submit(new LoanWarningThread(list, param,i,flag,customer,esOpenMark));
        }catch (Exception e){
            log.error("重新处理画像异常数据出错:{},{}",errorFile,row,e);
        }
    }
    /**
     * 提交一次性任务
     * @param list
     */
    private void generateOnceTask(List<MarketingTask> list, ExecutorService warrningExecutor,Customer customer) {
        if(list==null||list.size()==0) {
            return;
        }
        for (MarketingTask blt : list) {
            if(blt.getActualNumber()<=0){
                log.error("该批次监控人数为空，跳过执行:apiCode:{} batch_number：{}",blt.getApiCode(), blt.getBatchNumber());
                continue;
            }
            String strategyStr = strategyCS.strategyIdCheck(blt.getApiCode(), blt.getStrategyId());
            if(StringUtils.isEmpty(strategyStr)){
                log.error("贷中策略不可用:apiCode:{} Strategy_id：{}",blt.getApiCode(), blt.getStrategyId());
                continue;
            }
            blt.setTableName("b_marketing_user_"+blt.getApiCode());
            String descPath = path + "/once/" + blt.getApiCode() + "/" + blt.getBatchNumber() + "/"
                    + new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            Integer pushType=0;
            GroupStrategyConfig groupStrategyConfig =getGroupStrategyConfig(blt);
            if(groupStrategyConfig !=null){
                pushType=groupStrategyConfig.getPushType();
            }
            /**
             * 任务提交后，在stra_his_file表中插入一条数据（记录当天该批次的结果文件信息，用于结果文件合并和推送）
             */
            LoanFile blf = new LoanFile();
            blf.setApiCode(blt.getApiCode());
            blf.setFilePath(descPath);
            blf.setStatus(3);
            blf.setType(2);
            blf.setBatchNumber(blt.getBatchNumber());
            blf.setExpectedNum(blt.getActualNumber());
            blf.setShowTitle(createShowTitle(blt));
            blf.setPushType(pushType);
            loanFileMapper.insertFile(blf);

            /**
             * 一次性任务提交后，在b_task_status表中插入一条数据（标识一次性任务已执行）
             */
            TaskStatus bts = new TaskStatus();
            bts.setOnceStatus(1);
            bts.setApiCode(blt.getApiCode());
            bts.setBatchNumber(blt.getBatchNumber());
            bts.setFileId(blf.getId());
            taskStatusMapper.insertTaskStatus(bts);

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
                core(blt, descPath,false,strategyStr,warrningExecutor,blf.getId().toString(),customer);


        }
    }

    /**
     * 提交全量监控任务
     * @param list
     */
    private void generateAllTask(List<MarketingTask> list, ExecutorService warrningExecutor,Customer customer){
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
                String descPath=path+"/all/"+blt.getApiCode()+"/"+blt.getBatchNumber()+"/"
                        + new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                Integer pushType=0;
            GroupStrategyConfig groupStrategyConfig =getGroupStrategyConfig(blt);
            if(groupStrategyConfig !=null){
                pushType=groupStrategyConfig.getPushType();
            }
            /**
             * 任务提交前，在stra_his_file表中插入一条数据（记录当天该批次的结果文件信息，用于结果文件合并和推送）
             */
            LoanFile blf=new LoanFile();
            blf.setApiCode(blt.getApiCode());
            blf.setFilePath(descPath);
            blf.setStatus(3);
            blf.setType(1);
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
            bts.setAllStatus(1);
            bts.setApiCode(blt.getApiCode());
            bts.setBatchNumber(blt.getBatchNumber());
            bts.setFileId(blf.getId());
            taskStatusMapper.insertTaskStatus(bts);

            core(blt,descPath,false,strategyStr,warrningExecutor,blf.getId().toString(),customer);

        }

    }

    /**
     * 提交任务
     * @param blt
     * @param descPath
     */

    private void core(MarketingTask blt, String descPath, boolean isIncr, String strategyStr, ExecutorService warrningExecutor,
                      String fileId,Customer customer){
        try {
            Integer sep= marketingTaskMapper.querySep(blt.getApiCode());
            String separator=Constants.sepMap.get(sep);

            String baseHeadInfo = this.getBaseHeadInfo(blt.getId(), separator);
            String redisOpen = redisChgService.get(RedisEsOpen);
            Integer esOpenMark = StringUtils.isNotBlank(redisOpen)?Integer.valueOf(redisOpen):1;
                Long minId= marketingUserMapper.queryMinId(blt);
                Long maxId= marketingUserMapper.queryMaxId(blt);
                log.warn("min_id--{},max_id--{},pageSize--{}",minId,maxId,pageSize);
                if(minId !=null && minId>0L) {
                    Long begin = minId - 1;
                    int i = 1;
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
                            warrningExecutor.submit(new LoanWarningThread(list, param,i,isIncr,customer,esOpenMark));
                            Thread.sleep(100);
                        }
                        i++;
                    }
                    long endtime = System.currentTimeMillis();
                    if (log.isWarnEnabled()) {
                        log.warn("apicode:".concat(blt.getBatchNumber()).concat("~~查询总耗时："
                                .concat(String.valueOf(endtime - start)).concat("~~轮询总次数：")
                                .concat(String.valueOf(i).concat("~~esOpen:").concat(esOpenMark.toString()))));
                    }
                }else{
                    log.warn(String.format("无符合条件的数据--apiCode:%s,batchNumber:%s",blt.getApiCode(),blt.getBatchNumber()));
                }

        }catch (Exception e){
            log.error("执行任务失败",e);
        }
    }

    private String getBaseHeadInfo(Long taskId,String separator){
        StringBuilder baseHeadInfo = new StringBuilder();
        MarketingTaskExtendExample taskExtendExample = new MarketingTaskExtendExample();
        taskExtendExample.createCriteria().andTaskIdEqualTo(taskId).andIsDelEqualTo(1);
        List<MarketingTaskExtend> marketingTaskExtends = marketingTaskExtendMapper.selectByExample(taskExtendExample);
        if(marketingTaskExtends.size()>0){
            MarketingTaskExtend taskExtend = marketingTaskExtends.get(0);
            Result<String> headInfo = iProductResultSimpleService.getBaseHeadInfo(taskExtend.getApiCode(), taskExtend.getGroupType());
            if(ResultCode.SUCCESS.getValue().equals(headInfo.getCode())){
                String[] split = headInfo.getData().split(",");
                for (String s : split) {
                    switch (s.toLowerCase()){
                        case "grouptype":
                            baseHeadInfo.append(taskExtend.getGroupType()+separator);
                            break;
                        case "taskid":
                            baseHeadInfo.append(taskExtend.getCusTaskId()+separator);
                            break;
                        case "cell":
                            baseHeadInfo.append("{cell}"+separator);
                            break;
                        default:
                            log.warn("switch default s:{}", s);
                    }
                }
            }
        }
        return baseHeadInfo.toString();
    }

    /**
     * 初始化当日需要监控的任务信息
     * @param allList
     */
    private void initBatchNumList(List<MarketingTask> allList, List<MarketingTask> onceList, String apiCode,
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
                            onceList.add(blt);
                        }
                    }else if(4 == blt.getMonitorType()){
                        int days = 0;
                        try {
                            days = DateHelper.daysBetween(blt.getStartDate());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        Integer cycleDay = 0;
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
                                    .andCreateTimeGreaterThanOrEqualTo(DateHelper.getDateAdd(0).concat(" 00:00:00"))
                                    .andCreateTimeLessThan(DateHelper.getDateAdd(1).concat(" 00:00:00"));
                            List<TaskStatus> taskStatuses = taskStatusMapper.selectByExample(statusExample);
                            if(taskStatuses.size()<=0) {
                                allList.add(blt);
                            }
                        }
                    }
                }

            }
            log.warn("当日全量批次数量--{}，一次性数量--{}",allList.size(),onceList.size());
        }catch (Exception e){
            log.error("初始化任务出错",e);
        }
    }

    private String createShowTitle(MarketingTask task){

        MarketingTaskExtendExample extendExample = new MarketingTaskExtendExample();
        extendExample.createCriteria()
                .andTaskIdEqualTo(Long.valueOf(task.getId()))
                .andIsDelEqualTo(1);
        List<MarketingTaskExtend> marketingTaskExtends = marketingTaskExtendMapper.selectByExample(extendExample);
        if(marketingTaskExtends.size()>0 && StringUtils.isNotBlank(marketingTaskExtends.get(0).getGroupType())){
            MarketingTaskExtend taskExtend = marketingTaskExtends.get(0);

            String groupStr = "";
            GroupStrategyConfigExample configExample = new GroupStrategyConfigExample();
            configExample.createCriteria()
                    .andApiCodeEqualTo(task.getApiCode())
                    .andGroupTypeEqualTo(taskExtend.getGroupType())
                    .andIsDelEqualTo(1);
            List<GroupStrategyConfig> groupStrategyConfigs = groupStrategyConfigMapper.selectByExample(configExample);
            if(groupStrategyConfigs.size()>0){
                GroupStrategyConfig groupStrategyConfig = groupStrategyConfigs.get(0);
                groupStr = groupStrategyConfig.getGroupTypeShort().concat("_");
            }

            Date parse = null;
            try {
                parse = yyyy_MM_dd.parse(taskExtend.getUploadTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String showTitle = task.getApiCode().concat("_")
                                    .concat(taskExtend.getCusTaskId()).concat("_")
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
    private GroupStrategyConfig getGroupStrategyConfig(MarketingTask task){

        GroupStrategyConfig groupStrategyConfig=null;

        MarketingTaskExtendExample extendExample = new MarketingTaskExtendExample();
        extendExample.createCriteria()
                .andTaskIdEqualTo(task.getId())
                .andIsDelEqualTo(1);
        List<MarketingTaskExtend> marketingTaskExtends = marketingTaskExtendMapper.selectByExample(extendExample);
        if(marketingTaskExtends.size()>0) {
            MarketingTaskExtend taskExtend = marketingTaskExtends.get(0);

            GroupStrategyConfigExample configExample = new GroupStrategyConfigExample();
            configExample.createCriteria()
                    .andApiCodeEqualTo(task.getApiCode())
                    .andGroupTypeEqualTo(taskExtend.getGroupType())
                    .andIsDelEqualTo(1);
            List<GroupStrategyConfig> groupStrategyConfigs = groupStrategyConfigMapper.selectByExample(configExample);
            if (groupStrategyConfigs.size() > 0) {
                groupStrategyConfig = groupStrategyConfigs.get(0);
            }
        }
        return groupStrategyConfig;
    }
}