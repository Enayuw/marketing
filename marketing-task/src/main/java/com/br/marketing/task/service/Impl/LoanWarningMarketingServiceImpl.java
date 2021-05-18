package com.br.marketing.task.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.ProFieldsClient;
import com.br.marketing.client.RedisChgService;
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
import com.br.marketing.task.thread.LoanWarningMarketingThread;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * Created by Bairong on 2020/4/20.
 * 360营销定制模式
 */
@Service
@Slf4j
public class LoanWarningMarketingServiceImpl implements LoanWarningService {
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
        ExecutorService warningxecutor;
        if(customer.getThreadNum()!=null){
            warningxecutor = BrExecutors.getThreadPool(customer.getThreadNum(),customer.getThreadNum());
        }else{
            warningxecutor = BrExecutors.getThreadPool(60,60);
        }
        String apiCode=customer.getApiCode();
        List<MarketingTask> onceList=new ArrayList();
        initBatchNumList(onceList,apiCode);
        Integer sep= marketingTaskMapper.querySep(apiCode);
        String separator= Constants.sepMap.get(sep);
        this.generateTask(onceList,false,warningxecutor,separator);

        /**
         * 等待所有任务都执行完成
         **/
        warningxecutor.shutdown();
        while (true){
            if(warningxecutor.isTerminated()){
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
            if(!hkeys.isEmpty()){
                warningxecutor = BrExecutors.getThreadPool(60,60);
                int i=1;
                for(String errorFile:hkeys){
                    String batchNumber = redisChgService.hget(hkey, errorFile);
                    this.retry(apiCode,batchNumber,errorFile,true,warningxecutor,i,separator);
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
        warningxecutor.shutdown();
        while (true){
            if(warningxecutor.isTerminated()){
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
     * @param warningxecutor 线程池
     * @param num 结果文件名称
     */
    private void retry(String apiCode,String batchNumber,String errorFile,boolean flag,ExecutorService warningxecutor,Integer num,String separator){
        MarketingTask blt = marketingTaskMapper.queryBlt(batchNumber);
        JSONObject strategyJson = new JSONObject();
        setStrategyInfo(strategyJson,blt,separator);
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
            warningxecutor.submit(new LoanWarningMarketingThread(list, param,
                    i,strategyJson,proFieldsClient,flag,redisChgService));
        }catch (Exception e){
            log.error("重新处理画像异常数据出错{}",row,e);
        }
    }

    /**
     * 设置全量字段策略和重点字段策略信息
     * @param dtbJson 重点字段策略信息
     * @param blt 任务信息
     */
    private void setStrategyInfo(JSONObject dtbJson, MarketingTask blt, String separator){
        String strategyId = blt.getStrategyId();
        String dtbStr = strategyCS.strategyIdCheck(blt.getApiCode(), strategyId);
        log.warn("dtbStr:{}",dtbStr);
        if (StringUtils.isEmpty(dtbStr)) {
            log.error("贷中策略不可用:apiCode:{} Strategy_id：{}",blt.getApiCode(),strategyId);
            return;
        }
        String pathStr = path + "/once/" + blt.getApiCode() + "/" + blt.getBatchNumber() + "/"
                + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "/" ;
        dtbJson.put("strategy", dtbStr);
        dtbJson.put("path", pathStr);
        dtbJson.put("strategyId", strategyId);
        dtbJson.put("sep",separator);

    }

    /**
     * 处理任务
     * @param list 任务列表
     * @param flag 是否留存日志
     * @param warningxecutor 线程池
     * @param separator 分隔符
     */
    private void generateTask(List<MarketingTask> list, boolean flag, ExecutorService warningxecutor, String separator){
        try {

            if ( list.size() == 0) {
                return;
            }
            for (MarketingTask blt : list) {
                JSONObject strategyJson = new JSONObject();
                setStrategyInfo(strategyJson,blt,separator);

                log.info("batchNumber:{}", blt.getBatchNumber());
                blt.setTableName("b_marketing_user_"+blt.getApiCode());
                core(blt, strategyJson, flag,warningxecutor);

                /**
                 * 任务提交后，在stra_his_file表中插入一条数据（记录当天该批次的结果文件信息，用于结果文件合并和推送）
                 */
                LoanFile blf = new LoanFile();
                blf.setApiCode(blt.getApiCode());
                blf.setStatus(1);
                blf.setType(1);
                blf.setBatchNumber(blt.getBatchNumber());

                TaskStatus bts = new TaskStatus();
                bts.setOnceStatus(1);
                bts.setApiCode(blt.getApiCode());
                bts.setBatchNumber(blt.getBatchNumber());
                /**
                 * 全量任务提交后，在b_task_status表中插入一条数据（标识全量任务已执行，之后应该按增量处理）
                 */
                blf.setFilePath(strategyJson.getString("path"));
                blf.setIsSec(0);
                blf.setExpectedNum(blt.getActualNumber());
                loanFileMapper.insertFile(blf);

                bts.setFileId(blf.getId());
                taskStatusMapper.insertTaskStatus(bts);
            }
        }catch (Exception e){
            log.error("error ---{}",e);
        }
    }


    /**
     * 提交任务
     * @param blt 任务信息
     * @param strategyJson 策略详情信息
     * @param flag 是否留存日志
     * @param warningxecutor 线程池
     */
    private void core(MarketingTask blt, JSONObject strategyJson, boolean flag, ExecutorService warningxecutor){
        int minId= marketingUserMapper.queryMinId(blt);
        int maxId= marketingUserMapper.queryMaxId(blt);
        log.warn("min_id--{},max_id--{}",minId,maxId);
        int end=0;
        int i=1;
        while (end<maxId){
            int begin = (i - 1) * pageSize + minId;
            end=begin+pageSize;
            if(end>maxId){
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
                warningxecutor.submit(new LoanWarningMarketingThread(list, param,
                        i,strategyJson,proFieldsClient,flag,redisChgService));
            }catch (Exception e){
                log.error("执行解密任务失败",e);
            }
            i++;
        }
    }
    private void initBatchNumList(List<MarketingTask> onceList, String apiCode) {
        try{
            List<MarketingTask> list= marketingTaskMapper.queryBatchNumByapiCode(apiCode);
            for(MarketingTask blt:list){
                if (1 == blt.getMonitorType()) {
                    List<TaskStatus> taskStatusList = taskStatusMapper.queryOnceBts(blt.getBatchNumber());
                    if (taskStatusList.size()==0) {
                        onceList.add(blt);
                    }
                }
            }
            log.warn("当日一次性批次数量--{}，apiCode-{}",onceList.size(),apiCode);
        }catch (Exception e){
            log.error("初始化任务出错",e);
        }

    }


}
