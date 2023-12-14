package com.br.marketing.check.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.common.util.DateUtils;
import com.br.marketing.check.service.PushCustomerService;
import com.br.marketing.check.thread.PushDataThread;
import com.br.marketing.check.utils.MomUtil;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.QueryBaseBean;
import com.br.marketing.es.service.impl.MarketingHistoryEsServiceImpl;
import com.br.marketing.es.util.UuidUtils;
import com.br.marketing.mapper.*;
import com.br.marketing.vo.ConditionOfScoreVO;
import com.br.marketing.vo.TaskExtendInfoVO;
import com.google.common.base.Joiner;
import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * //				    _ooOoo_
 * //				   o8888888o
 * //				   88" . "88
 * //				   (| -_- |)
 * //				   O\  =  /O
 * //			    ____/`---'\____
 * //			  .'  \\|     |//  `.
 * //		     /  \\|||  :  |||//  \
 * //		    /  _|||||--:--|||||_  \
 * //		    | / | \\\  -  /// | \ |
 * //		    | \_|  ''\-:-/''  |_/ |
 * //		    \  .-\__  `-`  ___/-. /
 * //		  ___`...'  /--.--\  '...`___
 * //	   ."" '< `.___\_<|>_/___.'  >' "".
 * //	   | | : `- \`.;`\ _ /`;.`/ -` : | |
 * //	    \ \ `-.  \_ __\ /__ _/  .-` / /
 * // ======`-.____`-.____\____/.-`____.-`======
 * //				    `=---='
 * //^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
 * //			  Buddha Bless, No Bug !
 *
 * @Author xiaoxin.pang
 * @Date 2021/8/4 15:40
 * @Description:
 **/
@Slf4j
@Service
public class PushCustomerServiceImpl implements PushCustomerService {
    @Resource
    StraHisFileMapper straHisFileMapper;
    @Resource
    MarketingHistoryEsServiceImpl marketingHistoryEsService;
    @Resource
    PushErrorLogMapper pushErrorLogMapper;
    @Resource
    HttpProxyClient httpProxyClient;

    @Resource
    ScorePushCustomerConfigMapper scorePushCustomerConfigMapper;


    @Resource
    ScoreSearchConditionMapper scoreSearchConditionMapper;

    @Override
    public void push(Customer customer,Long fileId) {

        //region 获取回传配置信息
        String apiCode = customer.getApiCode();
        ExecutorService pushExecutor;
        if(customer.getPushThreadNum()!=null){
            pushExecutor = BrExecutors.getThreadPool(customer.getPushThreadNum(),customer.getPushThreadNum());
        }else{
            pushExecutor = BrExecutors.getThreadPool(20,20);
        }
        Date createTime=new Date();
        try {
            createTime=DateUtils.parse(DateHelper.getDateAdd(-1),"yyyy-mm-dd");
        }catch (ParseException e){
            log.error("格式化日期错误",e);
        }

        //获取回传配置
        ScorePushCustomerConfigExample scorePushCustomerConfigExample = new ScorePushCustomerConfigExample();
        scorePushCustomerConfigExample.createCriteria().andApiCodeEqualTo(apiCode).andIsDelEqualTo(Constants.DATA_VALID);
        List<ScorePushCustomerConfig> scorePushCustomerConfigs = scorePushCustomerConfigMapper.selectByExample(scorePushCustomerConfigExample);
        if(scorePushCustomerConfigs.size()<=0){
            return;
        }

        //跑分筛选条件配置
        List<ConditionOfScoreVO> scoreByConditionType = scoreSearchConditionMapper.getScoreByConditionType(apiCode, 3);

        //endregion

        //region 获取需要回传给客户的跑分文件
        List<StraHisFile> straHisFileList=new ArrayList<>();
        if(fileId!=null && fileId>0){
            StraHisFile straHisFile = straHisFileMapper.selectByPrimaryKey(fileId);
            if (straHisFile == null) {
                return;
            }
            if(!straHisFile.getApiCode().equals(apiCode)){
                return;
            }
            straHisFileList.add(straHisFile);
        }else{
            StraHisFileExample straHisFileExample =new StraHisFileExample();
            straHisFileExample.createCriteria().andApiCodeEqualTo(apiCode).andPushStatusEqualTo(0)
                    .andCreateTimeGreaterThanOrEqualTo(createTime);
            straHisFileList=straHisFileMapper.selectByExample(straHisFileExample);
        }
        //endregion


        straHisFileList.forEach(straHisFile -> {
            List<Long> fileIds=new ArrayList<>();
            fileIds.add(straHisFile.getId());
            StraHisFileExample straHisFileExample1 =new StraHisFileExample();
            straHisFileExample1.createCriteria().andBatchNumberEqualTo(straHisFile.getBatchNumber()).andApiCodeEqualTo(straHisFile.getApiCode());
            List<StraHisFile> straHisFiles=straHisFileMapper.selectByExample(straHisFileExample1);
            List<TaskExtendInfoVO> extendInfosByFileIds = straHisFileMapper.getExtendInfosByFileIds(fileIds);

            QueryBaseBean queryBaseBean = new QueryBaseBean();
            queryBaseBean.setApiCode(straHisFile.getApiCode());
            queryBaseBean.setBatchNumbers(straHisFile.getBatchNumber());
            queryBaseBean.setFileIds(straHisFile.getId().toString());
            int total = marketingHistoryEsService.builderMarketingWithTotal(queryBaseBean);
            String searchAfterStr="";
            int totalPage=total%500==0?total/500:total/500+1;
            for (int i = 1; i <= totalPage; i++) {
                queryBaseBean.setPageSize(500);
                queryBaseBean.setSearchAfter(searchAfterStr);
                List<MarketingHistory> marketingHistories = marketingHistoryEsService.builderMarketingWithList(queryBaseBean,"cus_num,batch_number,request_time,file_id,reserve_field,task_id,user_type");
                if (marketingHistories.size() > 0) {
                    searchAfterStr = marketingHistories.get(marketingHistories.size() - 1).getSearchAfter();
                    pushExecutor.submit(new PushDataThread(customer,extendInfosByFileIds.get(0),marketingHistories,straHisFiles.size()));
                }
            }
            straHisFile.setPushStatus(1);
            straHisFileMapper.updateByPrimaryKeySelective(straHisFile);
        });

        /**
         * 等待所有任务都执行完成
         **/
        log.warn("所有任务已加入队列，等待结束-----");
        pushExecutor.shutdown();
        while (true){
            if(pushExecutor.isTerminated()){
                log.warn("所有线程都执行结束");
                break;
            }
            try {
                Thread.sleep(6000);
            }catch (Exception e){
            }
        }
        log.warn("所有批次推送结束，apiCode={},批次数量为{}", apiCode,straHisFileList.size());
    }

    @Override
    public void retry(Customer customer) {
        ExecutorService retryPushExecutor;
        if(customer.getPushThreadNum()!=null){
            retryPushExecutor = BrExecutors.getThreadPool(customer.getPushThreadNum(),customer.getPushThreadNum());
        }else{
            retryPushExecutor = BrExecutors.getThreadPool(20,20);
        }
        Date createTime=new Date();
        try {
            createTime=DateUtils.parse(DateHelper.getDateAdd(-1),"yyyy-mm-dd");
        }catch (ParseException e){
            log.error("格式化日期错误",e);
        }

        PushErrorLogExample pushErrorLogExample = new PushErrorLogExample();
        pushErrorLogExample.createCriteria().andApiCodeEqualTo(customer.getApiCode()).andCreateTimeGreaterThanOrEqualTo(createTime).andStatusEqualTo(2);
        List<PushErrorLog> pushErrorLogList =pushErrorLogMapper.selectByExample(pushErrorLogExample);
        pushErrorLogList=pushErrorLogList.stream()
                 .filter(pushErrorLogWithBLOBs1 -> pushErrorLogWithBLOBs1.getActualPushTimes()<pushErrorLogWithBLOBs1.getPushTimes())
         .collect(Collectors.toList());

        List<Callable<Boolean>> list = new ArrayList<>();
        for (PushErrorLog pushErrorLog : pushErrorLogList) {
            list.add(() -> {
                JSONObject param =JSONObject.parseObject(pushErrorLog.getRequestStr());
                param.put("requestId", UuidUtils.getUuid());
                Long begin=System.currentTimeMillis();
                JSONObject extendConfigInfoJson=new JSONObject();
                String extendConfigInfo=customer.getExtendConfigInfo();
                if(StringUtils.isNotBlank(extendConfigInfo)){
                    extendConfigInfoJson=JSONObject.parseObject(extendConfigInfo);
                }
                Boolean isProxy=extendConfigInfoJson.getBoolean("isProxy")==null?Boolean.TRUE:extendConfigInfoJson.getBoolean("isProxy");
                Map<String,Object> result=httpProxyClient.request(customer.getPushUrl().trim(),param.toJSONString(),isProxy);
                Long end =System.currentTimeMillis();
                String resultStr=result.get("data")!=null?result.get("data").toString():"";
                String code="9999";
                if(StringUtils.isNotBlank(resultStr)){
                    try {
                        JSONObject resultJson =JSONObject.parseObject(resultStr);
                        code=resultJson.getString("code");
                    }catch (Exception e){
                    }
                }
                if((Boolean) result.get("result")){
                    pushErrorLog.setStatus(1);
                }
                pushErrorLog.setActualPushTimes(pushErrorLog.getActualPushTimes()+1);
                pushErrorLog.setUpdateTime(new Date());
                pushErrorLog.setRequestStr(param.toJSONString());
                pushErrorLog.setResponseStr(resultStr);
                pushErrorLogMapper.updateByPrimaryKeySelective(pushErrorLog);
                MomUtil.sendMom(customer.getApiCode(),pushErrorLog.getRequestStr(),resultStr,end-begin,param.getString("requestId"),code);
                return null;
            });
        }
        try {
            retryPushExecutor.invokeAll(list);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        /**
         * 等待所有重试任务都执行完成
         **/
        log.warn("所有任务已加入队列，等待结束-----");
        retryPushExecutor.shutdown();
        while (true){
            if(retryPushExecutor.isTerminated()){
                log.warn("所有线程都执行结束");
                break;
            }
            try {
                Thread.sleep(6000);
            }catch (Exception e){
            }
        }
        log.warn("所有重试任务推送结束，apiCode={},重试任务数量为{}",customer.getApiCode(),pushErrorLogList.size());
    }

}
