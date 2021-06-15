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
import com.br.marketing.service.Impl.StrategyCs;
import com.br.marketing.task.service.LoanWarningService;
import com.br.marketing.task.thread.LoanWarningThread;
import lombok.extern.slf4j.Slf4j;
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
    @Resource
    MarketingTaskMapper marketingTaskMapper;
    @Resource
    MarketingUserMapper marketingUserMapper;
    @Resource
    LoanFileMapper loanFileMapper;
    @Resource
    LoanWarningClient loanWarningClient;

    @Resource
    RedisService redisService;
    @Resource
    TaskStatusMapper taskStatusMapper;

    @Resource
    StrategyCs strategyCS;

    @Resource
    ProFieldsClient proFieldsClient;

    @Value("${otherConfig.mom.appSecretKey:00}")
    private String appSecretKey;

    @Value("${otherConfig.huaXiangInterface.getReport:00}")
    private String url;
    private Map<String,String> map=new HashMap<>();
    @Resource
    RedisChgService redisChgService;
    @Resource
    MarketingStrategyProductMapper marketingStrategyProductMapper;

    @Override
    public void process(Customer customer){

        String type=customer.getType();
        ExecutorService warrningExecutor;
        String apiCode=customer.getApiCode();
        if(customer.getThreadNum()!=null){
            warrningExecutor = BrExecutors.getThreadPool(customer.getThreadNum(),customer.getThreadNum());
        }else{
            warrningExecutor = BrExecutors.getThreadPool(20,20);
        }
        try{
            List<MarketingTask> incrList=new ArrayList<>();
            List<MarketingTask> allList=new ArrayList<>();
            List<MarketingTask> onceList=new ArrayList<>();
            initBatchNumList(incrList,allList,onceList,apiCode);
            if("incr".equals(type)){
                this.generateIncreTask(incrList,warrningExecutor);
            }
            if("all".equals(type)){
                this.generateAllTask(allList,warrningExecutor);
            }
            if("once".equals(type)){
                this.generateOnceTask(onceList,warrningExecutor);
            }

          /**
           * 等待所有任务都执行完成
           **/
            warrningExecutor.shutdown();
            while (true){
                if(warrningExecutor.isTerminated()){
                    log.warn("所有线程都执行结束");
                    break;
                }
                try {
                    Thread.sleep(3000);
                    log.warn("waiting-----------");
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
                        this.retry(apiCode,batchNumber,errorFile,warrningExecutor,i);
                        i++;
                        redisChgService.hdel(hkey,errorFile);
                    }
                    redisChgService.del(hkey);
                }
            }catch (Exception e){
                log.error("重新处理异常数据出错",e);
            }


            /**
             * 等待所有任务都执行完成
             **/
            warrningExecutor.shutdown();
            while (true){
                if(warrningExecutor.isTerminated()){
                    log.warn("所有线程都执行结束");
                    break;
                }
                try {
                    Thread.sleep(3000);
                }catch (Exception e){
                }
            }

            try {
                String flagKey=Constants.HX_FLAG_98_NUM+ apiCode+"_"+DateHelper.getDateAddYyMmDd(0);
                String s = redisChgService.get(flagKey);
                if(StringUtils.isNotEmpty(s)&&Integer.parseInt(s)>0){
                    HxResultRuntimeException hxResultRuntimeException = new HxResultRuntimeException(
                            String.format("【紧急报警】【%s】存量客户监控- 数据产品flag异常  \001 您好:  数据产品flag异常,flag为98的请求有：%s条，请及时跟进"
                                    ,apiCode,s));
                    log.error("hxResult product flag error",hxResultRuntimeException);
                }
                redisChgService.expire(flagKey,172800);
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
    private void retry(String apiCode,String batchNumber,String errorFile,ExecutorService warrningExecutor,Integer num){
        MarketingTask marketingTask = marketingTaskMapper.queryBlt(batchNumber);
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

            Integer sep= marketingTaskMapper.querySep(apiCode);
            String separator= Constants.sepMap.get(sep);
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
            warrningExecutor.submit(new LoanWarningThread(list, param,loanWarningClient, i,
                    redisService, proFieldsClient,flag,map,redisChgService));
        }catch (Exception e){
            log.error("重新处理画像异常数据出错:{},{}",errorFile,row,e);
        }
    }
    /**
     * 提交一次性任务
     * @param list
     */
    private void generateOnceTask(List<MarketingTask> list, ExecutorService warrningExecutor) {
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
                log.warn("batchNumber:{}", blt.getBatchNumber());
                blt.setTableName("b_marketing_user_"+blt.getApiCode());
                String descPath = path + "/once/" + blt.getApiCode() + "/" + blt.getBatchNumber() + "/"
                        + new SimpleDateFormat("yyyy-MM-dd").format(new Date());

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
                marketingStrategyProductMapper.insertSelective(marketingStrategyProduct);
            }
                core(blt, descPath,false,strategyStr,warrningExecutor);

                /**
                 * 任务提交后，在stra_his_file表中插入一条数据（记录当天该批次的结果文件信息，用于结果文件合并和推送）
                 */
                LoanFile blf = new LoanFile();
                blf.setApiCode(blt.getApiCode());
                blf.setFilePath(descPath);
                blf.setStatus(1);
                blf.setType(2);
                blf.setBatchNumber(blt.getBatchNumber());
                blf.setExpectedNum(blt.getActualNumber());
                Integer id=loanFileMapper.insertFile(blf);

                /**
                 * 一次性任务提交后，在b_task_status表中插入一条数据（标识一次性任务已执行）
                 */
                TaskStatus bts = new TaskStatus();
                bts.setOnceStatus(1);
                bts.setApiCode(blt.getApiCode());
                bts.setBatchNumber(blt.getBatchNumber());
                bts.setFileId(blf.getId());
                taskStatusMapper.insertTaskStatus(bts);
        }
    }

    /**
     * 提交全量监控任务
     * @param list
     */
    private void generateAllTask(List<MarketingTask> list, ExecutorService warrningExecutor){
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

            /**
             * 任务提交前，在stra_his_file表中插入一条数据（记录当天该批次的结果文件信息，用于结果文件合并和推送）
             */
            LoanFile blf=new LoanFile();
            blf.setApiCode(blt.getApiCode());
            blf.setFilePath(descPath);
            blf.setStatus(1);
            blf.setType(1);
            blf.setBatchNumber(blt.getBatchNumber());
            blf.setExpectedNum(blt.getActualNumber());
            Integer id=loanFileMapper.insertFile(blf);

            /**
             * 全量任务提交前，在b_task_status表中插入一条数据（标识全量任务已执行，之后应该按增量处理）
             */
            TaskStatus bts=new TaskStatus();
            bts.setAllStatus(1);
            bts.setApiCode(blt.getApiCode());
            bts.setBatchNumber(blt.getBatchNumber());
            bts.setFileId(blf.getId());
            taskStatusMapper.insertTaskStatus(bts);



            core(blt,descPath,false,strategyStr,warrningExecutor);

        }

    }
    /**
     * 提交增量监控任务
     * @param list
     */
    private void generateIncreTask(List<MarketingTask> list, ExecutorService warrningExecutor){
        if(list==null||list.size()==0) {
            return;
        }
        Integer totalNum= marketingUserMapper.getTotalNum(list);
        if(totalNum==0){
            log.error("流失预警当天增量监控总数为0，请关注");
        }
        log.warn("当天增量监控总数:{}",totalNum);
        String dateAddYyMmDd = DateHelper.getDateAddYyMmDd(1);
        for (MarketingTask blt : list) {
            String  strategyStr=strategyCS.strategyIdCheck(blt.getApiCode(),blt.getStrategyId());
            if(StringUtils.isEmpty(strategyStr)){
                log.error("贷中策略不可用:apiCode:{} Strategy_id：{}",blt.getApiCode(), blt.getStrategyId());
                continue;
            }
                List<MarketingTask> batctList=new ArrayList<>();
                blt.setHitDate(dateAddYyMmDd);
                batctList.add(blt);
                Integer batctNum= marketingUserMapper.getTotalNum(batctList);
            if(batctNum>0) {
                log.warn("batchNumber:{}", blt.getBatchNumber());
                blt.setTableName("b_marketing_user_chg");
                String descPath = path + "/incr/" + blt.getApiCode() + "/" + blt.getBatchNumber() + "/"
                        + new SimpleDateFormat("yyyy-MM-dd").format(new Date());

                /**
                 * 任务提交后，在stra_his_file表中插入一条数据（记录当天该批次的结果文件信息，用于结果文件合并和推送）
                 */
                LoanFile blf = new LoanFile();
                blf.setApiCode(blt.getApiCode());
                blf.setFilePath(descPath);
                blf.setStatus(1);
                blf.setType(0);
                blf.setBatchNumber(blt.getBatchNumber());
                blf.setExpectedNum(batctNum);
                Integer id=loanFileMapper.insertFile(blf);

                /**
                 * 增量任务提交后，在b_task_status表中插入一条数据（标识当天增量任务已执行）
                 */
                TaskStatus bts = new TaskStatus();
                bts.setIncrStatus(1);
                bts.setIncrDate(DateHelper.getDateAdd(0));
                bts.setApiCode(blt.getApiCode());
                bts.setBatchNumber(blt.getBatchNumber());
                bts.setFileId(blf.getId());
                taskStatusMapper.insertTaskStatus(bts);


                core(blt, descPath,true,strategyStr,warrningExecutor);




            }
        }
    }

    /**
     * 提交任务
     * @param blt
     * @param descPath
     */
    private void core(MarketingTask blt, String descPath, boolean isIncr, String strategyStr, ExecutorService warrningExecutor){
        try {
                Integer sep= marketingTaskMapper.querySep(blt.getApiCode());
                String separator=Constants.sepMap.get(sep);
                int minId= marketingUserMapper.queryMinId(blt);
                int maxId= marketingUserMapper.queryMaxId(blt);
                log.warn("min_id--{},max_id--{},pageSize--{}",minId,maxId,pageSize);

                int end=0;
                int i=1;
                while (end<maxId){
                    int begin = (i - 1) * pageSize + minId;
                    end=begin+pageSize;
                    if(end>=maxId){
                        end=maxId+1;
                    }

                    blt.setBegin(begin);
                    blt.setEnd(end);

                    List<MarketingUser> list= marketingUserMapper.queryUserByid(blt);
                    if(list.size()>0){
                        Map<String,String> param=new HashMap<>();
                        param.put("apiCode", blt.getApiCode());
                        param.put("strategyId",blt.getStrategyId());
                        param.put("path",descPath);
                        param.put("strategyStr",strategyStr);
                        param.put("sep",separator);
                        param.put("batchNumber",blt.getBatchNumber());
                        param.put("cusBatchNumber",blt.getFileName());
                        param.put("url",url);
                        param.put("appSecretKey",appSecretKey);
                        param.put("isRepair",blt.getIsRepair());
                        warrningExecutor.submit(new LoanWarningThread(list, param,loanWarningClient, i,
                                redisService, proFieldsClient,isIncr,map,redisChgService));
                        Thread.sleep(100);
                    }
                    i++;
                }

        }catch (Exception e){
            log.error("执行任务失败",e);
        }
    }

    /**
     * 初始化当日需要监控的任务信息，并将增量监控和全量监控区分开来
     * @param incrList
     * @param allList
     */
    private void initBatchNumList(List<MarketingTask> incrList, List<MarketingTask> allList, List<MarketingTask> onceList, String apiCode){
        try{
            List<MarketingTask> list= marketingTaskMapper.queryBatchNumByapiCode(apiCode);
            log.warn("当日批次数量--{}",list.size());
            for(MarketingTask blt:list) {
                if (1 == blt.getMonitorType()) {
                    List<TaskStatus> bts = taskStatusMapper.queryOnceBts(blt.getBatchNumber());
                    if (bts.size()==0) {
                        onceList.add(blt);
                    }
                } else if (2 == blt.getMonitorType()) {
                    TaskStatus bts = taskStatusMapper.queryBts(blt.getBatchNumber());
                    if (bts == null) {
                        allList.add(blt);
                    } else {
                        //如果第一次全量执行结束时间早于今天，则该批次任务今天按增量处理
                        String updateTime = bts.getUpdateTime();
                        if (DateHelper.daysBetween(updateTime) > 0) {
                            TaskStatus bts1 = taskStatusMapper.queryTodayIncrBts(blt.getBatchNumber());
                            if (bts1 == null) {
                                incrList.add(blt);
                            } else {
                                log.warn("当日已处理--{}", blt.getBatchNumber());
                            }
                        }
                    }
                }else if(3 == blt.getMonitorType()){
                    incrList.add(blt);
                }else if(4 == blt.getMonitorType()){
                    int days = 0;
                    try {
                        days = DateHelper.daysBetween(blt.getStartDate());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if(days%Constants.frequencyMap.get(blt.getFrequency())==0){
                        allList.add(blt);
                    }
                }
            }
            log.warn("当日增量批次数量--{}、全量批次数量--{}，一次性数量--{}",incrList.size(),allList.size(),onceList.size());
        }catch (Exception e){
            log.error("初始化任务出错",e);
        }
    }
}
