package com.br.marketing.task.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.*;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.exception.HxResultRuntimeException;
import com.br.marketing.mapper.LoanFileMapper;
import com.br.marketing.mapper.MarketingTaskMapper;
import com.br.marketing.mapper.TaskStatusMapper;
import com.br.marketing.mapper.MarketingUserMapper;
import com.br.marketing.service.Impl.StrategyCs;
import com.br.marketing.task.service.LoanWarningService;
import com.br.marketing.task.thread.LoanWarningThreadPpd;
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
 * Created by Bairong on 2020/4/20.
 * 流失预警ppd定制模式
 */
@Service
@Slf4j
public class LoanWarningPpdServiceImpl implements LoanWarningService {
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
    TaskStatusMapper taskStatusMapper;

    @Resource
    StrategyCs strategyCS;

    @Resource
    ProFieldsClient proFieldsClient;
    @Resource
    RedisChgService redisChgService;

    @Value("${otherConfig.mom.appSecretKey:00}")
    private String appSecretKey;

    @Value("${otherConfig.huaXiangInterface.getReport:00}")
    private String url;

    @Override
    public void process(Customer customer) {
        ExecutorService warrningPpdExecutor;
        String apiCode=customer.getApiCode();
        if(customer.getThreadNum()!=null){
            warrningPpdExecutor = BrExecutors.getThreadPool(customer.getThreadNum(),customer.getThreadNum());
        }else{
            warrningPpdExecutor = BrExecutors.getThreadPool(60,60);
        }
        List<MarketingTask> stockList=new ArrayList<>();
        List<MarketingTask> incrementList=new ArrayList();
        initBatchNumList(stockList,incrementList,apiCode);
        this.generateTask(stockList,true,warrningPpdExecutor);
        this.generateTask(incrementList,false,warrningPpdExecutor);

        /**
         * 等待所有任务都执行完成
         **/
        warrningPpdExecutor.shutdown();
        while (true){
            if(warrningPpdExecutor.isTerminated()){
                log.warn("所有线程都执行结束");
                break;
            }
            try {
                Thread.sleep(3000);
                log.info("waiting-----------");
            }catch (Exception e){
                log.warn("sleep error");
            }
        }


        try {
            String hkey=Constants.HXRESULTERROR_RETRY_KEY+":"+apiCode;
            Set<String> hkeys = redisChgService.hkeys(hkey);
            if(!hkeys.isEmpty()&&hkeys.size()>0){
                warrningPpdExecutor = BrExecutors.getThreadPool(60,60);
                int i=1;
                for(String errorFile:hkeys){
                    String batchNumber = redisChgService.hget(hkey, errorFile);
                    this.retry(apiCode,batchNumber,errorFile,true,warrningPpdExecutor,i);
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
        warrningPpdExecutor.shutdown();
        while (true){
            if(warrningPpdExecutor.isTerminated()){
                log.warn("所有线程都执行结束");
                break;
            }
            try {
                Thread.sleep(3000);
            }catch (Exception e){
                log.warn("sleep error",e);
            }
        }

        try{
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
    }


    /**
     * 画像异常结果重试处理
     * @param apiCode 客户编号
     * @param batchNumber 批次号
     * @param errorFile 异常结果记录文件
     * @param flag 是否记录日志
     * @param warningChgExecutor 线程池
     * @param num 结果文件名称
     */
    private void retry(String apiCode,String batchNumber,String errorFile,boolean flag,ExecutorService warningChgExecutor,Integer num){
        MarketingTask blt = marketingTaskMapper.queryBlt(batchNumber);
        JSONObject firstJson = new JSONObject();
        JSONObject secJson = new JSONObject();
        setStrategyInfo(firstJson,secJson,blt);
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
                list.add(lu);
            }
            log.info("{},list:{}",errorFile,list.size());
            Map<String,String> param=new HashMap<>();
            param.put("apiCode",blt.getApiCode());
            param.put("appSecretKey",appSecretKey);
            param.put("url",url);
            param.put("batchNumber",blt.getBatchNumber());
            warningChgExecutor.submit(new LoanWarningThreadPpd(list, param,
                    i, firstJson, secJson,proFieldsClient,flag,redisChgService));
        }catch (Exception e){
            log.error("重新处理画像异常数据出错{}",row,e);
        }
    }

    /**
     * 设置全量字段策略和重点字段策略信息
     * @param firstJson 全量字段策略信息
     * @param secJson 重点字段策略信息
     * @param blt 任务信息
     */
    private void setStrategyInfo(JSONObject firstJson, JSONObject secJson, MarketingTask blt){
        int days = 0;
        try {
            days = DateHelper.daysBetween(blt.getStartDate());
        } catch (ParseException e) {
            log.error("监控开始日期格式错误",e);
        }
        String secStrategyId = "",secStra="";
//        String secStrategyId =  blt.getSecStrategyId().split(":")[0];
//        String secStra = strategyCS.strategyIdCheck(blt.getApiCode(), secStrategyId);
//        if (StringUtils.isEmpty(secStra)) {
//            log.error("贷中策略不可用:apiCode:{} Strategy_id：{}",blt.getApiCode(),secStrategyId);
//            return;
//        }

        String secPath = path + "/all/" + blt.getApiCode() + "/" + blt.getBatchNumber() + "/"
                + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "/" + secStrategyId;
        String stmtKey = "";
//        if (blt.getSecStrategyId().split(":").length > 1) {
//            stmtKey = blt.getSecStrategyId().split(":")[1];
//        }
        secJson.put("strategy", secStra);
        secJson.put("path", secPath);
        secJson.put("stmt_key", stmtKey);
        secJson.put("strategyId", secStrategyId);

        if (days % Constants.PPDFREQUENCY == 0) {
            stmtKey = "";
            String strategyId = blt.getStrategyId().split(":")[0];
            String firstStra = strategyCS.strategyIdCheck(blt.getApiCode(), strategyId);
            if (StringUtils.isEmpty(firstStra)) {
                log.error("贷中策略不可用:apiCode:{} Strategy_id：{}",blt.getApiCode(),strategyId);
                return;
            }
            String firstPath = path + "/all/" + blt.getApiCode() + "/" + blt.getBatchNumber() + "/"
                    + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "/" + strategyId;

            if (blt.getStrategyId().split(":").length > 1) {
                stmtKey = blt.getStrategyId().split(":")[1];
            }
            firstJson.put("strategy", firstStra);
            firstJson.put("path", firstPath);
            firstJson.put("stmt_key", stmtKey);
            firstJson.put("strategyId", strategyId);
        }
    }
    /**
     *
     * @param list
     * @param flag 是否留存日志
     */
    private void generateTask(List<MarketingTask> list, boolean flag, ExecutorService warrningPpdExecutor){
        try {

            if (list == null || list.size() == 0) {
                return;
            }
            for (MarketingTask blt : list) {
                JSONObject firstJson = new JSONObject();
                JSONObject secJson = new JSONObject();
                setStrategyInfo(firstJson,secJson,blt);

                log.info("batchNumber:{}", blt.getBatchNumber());
                blt.setTableName("b_marketing_user_"+blt.getApiCode());
                core(blt, firstJson, secJson, flag,warrningPpdExecutor);

                /**
                 * 任务提交后，在stra_his_file表中插入一条数据（记录当天该批次的结果文件信息，用于结果文件合并和推送）
                 */
                LoanFile blf = new LoanFile();
                blf.setApiCode(blt.getApiCode());
                blf.setStatus(1);
                blf.setType(1);
                blf.setBatchNumber(blt.getBatchNumber());

                TaskStatus bts = new TaskStatus();
                bts.setAllStatus(1);
                bts.setApiCode(blt.getApiCode());
                bts.setBatchNumber(blt.getBatchNumber());
                /**
                 * 全量任务提交后，在b_task_status表中插入一条数据（标识全量任务已执行，之后应该按增量处理）
                 */
                blf.setFilePath(secJson.getString("path"));
                blf.setIsSec(1);
                blf.setExpectedNum(blt.getActualNumber());
                loanFileMapper.insertFile(blf);

                bts.setFileId(blf.getId());
                taskStatusMapper.insertTaskStatus(bts);

                if ( StringUtils.isNotEmpty(firstJson.getString("path"))) {
                    blf.setIsSec(0);
                    blf.setFilePath(firstJson.getString("path"));
                    loanFileMapper.insertFile(blf);

                    bts.setFileId(blf.getId());
                    taskStatusMapper.insertTaskStatus(bts);
                }
            }
        }catch (Exception e){
            log.error("error ---{}",e);
        }
    }


    /**
     * 提交任务
     * @param blt 任务信息
     * @param firstJson 全量字段策略信息
     * @param secJson 重点字段策略信息
     * @param flag 是否记录诶只
     * @param warrningPpdExecutor 线程池
     */
    private void core(MarketingTask blt, JSONObject firstJson, JSONObject secJson , boolean flag, ExecutorService warrningPpdExecutor){
        int minId= marketingUserMapper.queryMinId(blt);
        int maxId= marketingUserMapper.queryMaxId(blt);
        log.warn("min_id--{},max_id--{}",minId,maxId);
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
            try {
                List<MarketingUser> list= marketingUserMapper.queryUserByid(blt);
                Map<String,String> param=new HashMap<>();
                param.put("apiCode",blt.getApiCode());
                param.put("appSecretKey",appSecretKey);
                param.put("url",url);
                param.put("batchNumber",blt.getBatchNumber());
                warrningPpdExecutor.submit(new LoanWarningThreadPpd(list, param,
                        i,firstJson,secJson, proFieldsClient,flag,redisChgService));
            }catch (Exception e){
                log.error("执行解密任务失败",e);
            }
            i++;
        }
    }
    private void initBatchNumList(List<MarketingTask> stockList, List<MarketingTask> incrementList, String apiCode) {
        try{
            List<MarketingTask> list= marketingTaskMapper.queryBatchNumByapiCode(apiCode);
            for(MarketingTask blt:list){
                List<TaskStatus> bts= taskStatusMapper.queryBtsList(blt.getBatchNumber());
                if(bts.size()==0){
                    incrementList.add(blt);
                }else{
                    stockList.add(blt);
                }
            }
            log.warn("当日存量批次数量--{}、增量批次数量--{}",stockList.size(),incrementList.size());
        }catch (Exception e){
            log.error("初始化任务出错",e);
        }

    }


}
