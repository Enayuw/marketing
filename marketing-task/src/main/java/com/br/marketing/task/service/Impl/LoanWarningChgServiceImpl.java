package com.br.marketing.task.service.Impl;

import com.br.marketing.client.ProFieldsClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.utils.*;
import com.br.marketing.entity.*;
import com.br.marketing.exception.HxResultRuntimeException;
import com.br.marketing.mapper.LoanFileMapper;
import com.br.marketing.mapper.MarketingTaskMapper;
import com.br.marketing.mapper.TaskStatusMapper;
import com.br.marketing.mapper.MarketingUserMapper;
import com.br.marketing.service.Impl.StrategyCs;
import com.br.marketing.task.service.LoanWarningService;
import com.br.marketing.task.thread.LoanWarningChgThread;
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
 * Created by Bairong on 2020/5/8.
 * 流失预警字段变动模式
 * 每天查询全量数据，然后与redis中缓存的之前的结果对比，有变化的数据返回
 */
@Service
@Slf4j
public class LoanWarningChgServiceImpl implements LoanWarningService {
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
    StrategyCs strategyCs;
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
        ExecutorService warningChgExecutor;
        if(customer.getThreadNum() !=null){
            warningChgExecutor = BrExecutors.getThreadPool(customer.getThreadNum(),customer.getThreadNum());
        }else{
            warningChgExecutor = BrExecutors.getThreadPool(100,100);
        }
        String apiCode=customer.getApiCode();
        List<MarketingTask> stockList=new ArrayList<>();
        List<MarketingTask> incrementList=new ArrayList();
        initBatchNumList(stockList,incrementList,apiCode);
        this.generateTask(stockList,true,warningChgExecutor);
        this.generateTask(incrementList,false,warningChgExecutor);


        /**
         * 等待所有任务都执行完成
         **/
        warningChgExecutor.shutdown();
        while (true){
            if(warningChgExecutor.isTerminated()){
                log.warn("所有线程都执行结束");
                break;
            }
            try {
                Thread.sleep(3000);
            }catch (Exception e){
            }
        }

        try {
            String hkey=Constants.HXRESULTERROR_RETRY_KEY+":"+apiCode;
            Set<String> hkeys = redisChgService.hkeys(hkey);
            if(!hkeys.isEmpty()&&hkeys.size()>0){
                warningChgExecutor = BrExecutors.getThreadPool(100,100);
                int i=1;
                for(String errorFile:hkeys){
                    String batchNumber = redisChgService.hget(hkey, errorFile);
                    this.retry(apiCode,batchNumber,errorFile,true,warningChgExecutor,i);
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
        warningChgExecutor.shutdown();
        while (true){
            if(warningChgExecutor.isTerminated()){
                log.warn("所有线程都执行结束");
                break;
            }
            try {
                Thread.sleep(3000);
            }catch (Exception e){
                log.warn("sleep error");
            }
        }

        try {
            for(MarketingTask blt: stockList){
                String apiCode1 = blt.getApiCode();
                String batchNumber = blt.getBatchNumber();
                String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                String cntKey= Constants.LOAN_WARNING_CHF_CNT_KEY+apiCode1+"_"+batchNumber+"_"+today;
                String s = redisChgService.get(cntKey);
                LoanFile blf=new LoanFile();
                blf.setApiCode(apiCode1);
                blf.setBatchNumber(batchNumber);
                if(!StringUtils.isEmpty(s)){
                    blf.setExpectedNum(Integer.parseInt(s));
                }else{
                    blf.setExpectedNum(0);
                }
                log.warn("设置变动数量-批次号：{}，变动量：{}",batchNumber,s);
                loanFileMapper.updateExpectedNum(blf);
                redisChgService.expire(cntKey,172800);
            }
            for(MarketingTask blt: incrementList){
                String apiCode1 = blt.getApiCode();
                String batchNumber = blt.getBatchNumber();
                LoanFile blf=new LoanFile();
                blf.setApiCode(apiCode1);
                blf.setBatchNumber(batchNumber);
                blf.setExpectedNum(blt.getActualNumber());
                log.warn("新增数据-批次号：{}，变动量：{}",batchNumber,blt.getActualNumber());
                loanFileMapper.updateExpectedNum(blf);
            }

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
            log.error("更新任务信息出错",e);
        }

    }


    /**
     * 画像异常结果处理
     * @param apiCode 商户编号
     * @param batchNumber 批次号
     * @param errorFile 异常数据记录文件
     * @param isIncr 是否是新增数据
     * @param warningChgExecutor 线程池
     * @param num  文件编号
     */
    private void retry(String apiCode,String batchNumber,String errorFile,boolean isIncr,ExecutorService warningChgExecutor,Integer num){
        MarketingTask marketingTask = marketingTaskMapper.queryBlt(batchNumber);
        String  strategyStr=strategyCs.strategyIdCheck(apiCode, marketingTask.getStrategyId());
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
            boolean isCycle=isCycle(marketingTask.getStartDate());
            List<MarketingUser> list=new ArrayList<>();
            while ((row = br.readLine()) != null) {
                String[] split = row.split(",");
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
            Map<String,String> param=new HashMap<>();
            param.put("apiCode",apiCode);
            param.put("strategyId", marketingTask.getStrategyId());
            param.put("strategyStr",strategyStr);
            param.put("sep",separator);
            param.put("url",url);
            param.put("batchNumber", marketingTask.getBatchNumber());
            String descPath=path+"/all/"+ marketingTask.getApiCode()+"/"+ marketingTask.getBatchNumber()+"/"+
                    new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String noChgPath=path+"/nochg/"+ marketingTask.getApiCode()+"/"+ marketingTask.getBatchNumber()+"/"+
                    new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            log.info("{},list:{}",errorFile,list.size());
            warningChgExecutor.submit(new LoanWarningChgThread(list,param,isCycle,
                    i,descPath,noChgPath,redisChgService, proFieldsClient,isIncr,appSecretKey));
        }catch (Exception e){
            log.error("重新处理画像异常数据出错{}",row,e);
        }
    }

    /**
     *
     * @param list 任务列表
     * @param flag 是否是客户上传的增量数据
     */
    private void generateTask(List<MarketingTask> list, boolean flag, ExecutorService warningChgExecutor){
        try {
            if (list == null || list.size() == 0) {
                return;
            }

            int cnt=0;
            for (MarketingTask blt : list) {

               /*if(!"3005390_20200601215907_8424".equals(blt.getBatch_number())){
                    continue;
                }*/
                String  strategyStr=strategyCs.strategyIdCheck(blt.getApiCode(),blt.getStrategyId());
                if(StringUtils.isEmpty(strategyStr)){
                    log.error("贷中策略不可用:apiCode:{} Strategy_id：{}",blt.getApiCode(),blt.getStrategyId());
                    continue;
                }
                log.warn("batchNumber:{}",blt.getBatchNumber());
                blt.setTableName("b_marketing_user_"+blt.getApiCode());
                String descPath=path+"/all/"+blt.getApiCode()+"/"+blt.getBatchNumber()+"/"+ new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                String noChgPath=path+"/nochg/"+blt.getApiCode()+"/"+blt.getBatchNumber()+"/"+ new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                core(blt,descPath,flag,strategyStr,cnt,noChgPath,warningChgExecutor);


                /**
                 * 任务提交后，在stra_his_file表中插入一条数据（记录当天该批次的结果文件信息，用于结果文件合并和推送）
                 */
                LoanFile blf=new LoanFile();
                blf.setApiCode(blt.getApiCode());
                blf.setFilePath(descPath);
                blf.setStatus(1);
                blf.setType(1);
                blf.setIsSec(0);
                blf.setBatchNumber(blt.getBatchNumber());
                Integer id=loanFileMapper.insertFile(blf);

                /**
                 * 全量任务提交后，在b_task_status表中插入一条数据（标识全量任务已执行，之后应该按增量处理）
                 */
                TaskStatus bts=new TaskStatus();
                bts.setAllStatus(1);
                bts.setApiCode(blt.getApiCode());
                bts.setBatchNumber(blt.getBatchNumber());
                bts.setFileId(blf.getId());
                taskStatusMapper.insertTaskStatus(bts);
            }
        }catch (Exception e){
            log.error("error ---{}",e);
        }
    }


    /**
     * 提交任务
     * @param blt
     * @param descPath
     */
    private void core(MarketingTask blt, String descPath, boolean isIncr, String strategyStr,
                      int cnt, String noChgPath, ExecutorService warningChgExecutor){
        try {
            int minId= marketingUserMapper.queryMinId(blt);
            int maxId= marketingUserMapper.queryMaxId(blt);
            log.warn("min_id--{},max_id--{}",minId,maxId);
            Integer sep= marketingTaskMapper.querySep(blt.getApiCode());
            String separator= Constants.sepMap.get(sep);
            boolean isCycle=isCycle(blt.getStartDate());

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
                Map<String,String> param=new HashMap<>();
                param.put("apiCode",blt.getApiCode());
                param.put("strategyId",blt.getStrategyId());
                param.put("strategyStr",strategyStr);
                param.put("sep",separator);
                param.put("url",url);
                param.put("batchNumber",blt.getBatchNumber());
                warningChgExecutor.submit(new LoanWarningChgThread(list,param,isCycle,
                        i,descPath,noChgPath,redisChgService, proFieldsClient,isIncr,appSecretKey));
                i++;
                cnt++;
                if(cnt<100){
                    log.warn("cnt：{}，休眠2s",cnt);
                    Thread.sleep(2000);
                }else{
                    Thread.sleep(100);
                }
            }
        }catch (Exception e){
            log.error("执行任务失败",e);
        }
    }

    /**
     * 计算当前批次是否到返回全量的周期日
     * @param startDate
     * @return
     */
    private static  boolean isCycle(String startDate){
        int days = 0;
        try {
            days = DateHelper.daysBetween(startDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (days % Constants.FREQUENCY360 == 0) {
            return true;
        }
        return false;
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

    public static void main(String[] args) {
        System.out.println(isCycle("2021-01-07"));
    }
}
