package com.br.marketing.task.service.Impl;

import com.br.marketing.client.LoanWarningClient;
import com.br.marketing.client.ProFieldsClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.LoanFileMapper;
import com.br.marketing.mapper.MarketingTaskMapper;
import com.br.marketing.mapper.TaskStatusMapper;
import com.br.marketing.mapper.MarketingUserMapper;
import com.br.marketing.service.Impl.StrategyCs;
import com.br.marketing.task.service.LoanWarningService;
import com.br.marketing.task.thread.LoanWarningHNNXThread;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * Created by Bairong on 2020/5/8.
 * 海南农信定制模式
 */
@Service
@Slf4j
public class LoanWarningHnnxServiceImpl implements LoanWarningService {
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
    @Resource
    LoanWarningClient loanWarningClient;



    @Override
    public void process(Customer customer) {
        ExecutorService warningChgExecutor;
        String startDate="";
        if(customer.getThreadNum() !=null){
            warningChgExecutor = BrExecutors.getThreadPool(customer.getThreadNum(),customer.getThreadNum());
        }else{
            warningChgExecutor = BrExecutors.getThreadPool(100,100);
        }
        if(StringUtils.isNotEmpty(customer.getStartDate())){
            startDate=customer.getStartDate();
        }
        String apiCode=customer.getApiCode();
        List<MarketingTask> stockList=new ArrayList<>();
        List<MarketingTask> incrementList=new ArrayList();
        List<MarketingTask> list = new ArrayList<>();
        initBatchNumList(stockList,incrementList,apiCode);
        list.addAll(stockList);
        list.addAll(incrementList);
        this.generateTask(incrementList,list,isCycle(startDate),warningChgExecutor,apiCode);


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
    }

    private void generateTask(List<MarketingTask> incrementList, List<MarketingTask> totalList, boolean isCycle, ExecutorService warningChgExecutor, String apiCode){
        try {
            MarketingTask blt;
            if(incrementList!=null&&incrementList.size()>0){
                blt = incrementList.get(incrementList.size()-1);
            }else {
                if(isCycle&&totalList!=null&&totalList.size()>0){
                    blt=new MarketingTask();
                    MarketingTask marketingTask = totalList.get(0);
                    blt.setApiCode(apiCode);
                    blt.setStrategyId(marketingTask.getStrategyId());
                    blt.setBatchNumber(getBatchNumber(apiCode));
                }else {
                    return;
                }
            }

            int incrDataNum=0;
                Integer cnt=0;
                String  strategyStr=strategyCs.strategyIdCheck(blt.getApiCode(),blt.getStrategyId());
                if(StringUtils.isEmpty(strategyStr)){
                    log.error("贷中策略不可用:apiCode:{} Strategy_id：{}",blt.getApiCode(),blt.getStrategyId());
                    return;
                }
                log.warn("batchNumber:{}",blt.getBatchNumber());
                blt.setTableName("b_marketing_user_"+blt.getApiCode());
                String descPath=path+"/all/"+blt.getApiCode()+"/"+blt.getBatchNumber()+"/"+ new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                if(isCycle){
                    for(MarketingTask blt1:totalList){
                        blt1.setTableName("b_marketing_user_"+blt.getApiCode());
                        log.warn("blt1:{}",blt1);
                        cnt=core(blt1,descPath,isCycle,strategyStr,cnt,warningChgExecutor);
                    }
                }else {
                    for(MarketingTask blt2:incrementList){
                        blt2.setTableName("b_marketing_user_"+blt.getApiCode());
                        log.warn("blt1:{}",blt2);
                        cnt=core(blt2,descPath,isCycle,strategyStr,cnt,warningChgExecutor);
                        TaskStatus bts=new TaskStatus();
                        bts.setAllStatus(1);
                        bts.setApiCode(blt2.getApiCode());
                        bts.setBatchNumber(blt2.getBatchNumber());
                        incrDataNum=incrDataNum+blt2.getActualNumber();
                        taskStatusMapper.insertTaskStatus(bts);
                    }
                }

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
                if(isCycle){
                    blf.setExpectedNum(getExpectedNum(totalList));
                }else {
                    blf.setExpectedNum(incrDataNum);
                }
                blf.setFileNum(1);
                Integer id=loanFileMapper.insertFile(blf);

                /**
             * 全量任务提交后，在b_task_status表中插入一条数据（标识全量任务已执行，之后应该按增量处理）
             */
            if(isCycle) {
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

    private Integer getExpectedNum(List<MarketingTask> totalList) {
        int num=0;
        for(MarketingTask blt:totalList){
            num=num+blt.getActualNumber();
        }
        return num;
    }



    /**
     * 提交任务
     * @param blt
     * @param descPath
     */
    private Integer core(MarketingTask blt, String descPath, boolean isCycle, String strategyStr,
                         Integer cnt, ExecutorService warningChgExecutor){
        try {
            int  minId= marketingUserMapper.queryMinId(blt);
            int  maxId= marketingUserMapper.queryMaxId(blt);

            log.warn("min_id--{},max_id--{}",minId,maxId);
            Integer sep= marketingTaskMapper.querySep(blt.getApiCode());
            String separator= Constants.sepMap.get(sep);

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
                param.put("batchNumber",blt.getBatchNumber());
                warningChgExecutor.submit(new LoanWarningHNNXThread(list,param,
                        cnt,descPath,redisChgService, proFieldsClient,isCycle,loanWarningClient));
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
        return cnt;
    }
    public static String getBatchNumber(String apiCode) {
        String dateAddYyMmDdHhMmSs = DateHelper.getDateAddYyMmDdHhMmSs(0);
        int i = (int) ((Math.random()*9+1)*1000);
        return apiCode+"_"+dateAddYyMmDdHhMmSs+"_"+i;
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
        if (days % Constants.FREQUENCYHNNX == 0) {
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
        System.out.println(isCycle("2021-01-05"));
    }
}
