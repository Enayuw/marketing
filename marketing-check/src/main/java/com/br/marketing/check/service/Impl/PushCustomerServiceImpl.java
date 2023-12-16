package com.br.marketing.check.service.Impl;

import cn.hutool.core.collection.ListUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.DateUtils;
import com.br.marketing.check.service.PushCustomerService;
import com.br.marketing.check.thread.PushDataThread;
import com.br.marketing.check.utils.MomUtil;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.utils.*;
import com.br.marketing.entity.*;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.QueryBaseBean;
import com.br.marketing.es.service.impl.MarketingHistoryEsServiceImpl;
import com.br.marketing.es.util.UuidUtils;
import com.br.marketing.mapper.*;
import com.br.marketing.vo.ConditionOfScoreVO;
import com.br.marketing.vo.TaskExtendInfoVO;
import com.br.marketing.vo.scorepushcustomer.HxResultVO;
import com.br.marketing.vo.scorepushcustomer.ScoreSortJsonVO;
import com.google.common.base.Joiner;
import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
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

    @Resource
    TaskBatchnumberPreMapper taskBatchnumberPreMapper;

    @Override
    public void push(Customer customer,Long fileId) {

        //region 获取回传配置信息
        String apiCode = customer.getApiCode();
        ScorePushCustomerConfig pushCustomerConfig = new ScorePushCustomerConfig();
        ConditionOfScoreVO condition = new ConditionOfScoreVO();
        ExecutorService pushExecutor;
        if(customer.getPushThreadNum()!=null){
            pushExecutor = BrExecutors.getThreadPool(customer.getPushThreadNum(),customer.getPushThreadNum());
        }else{
            pushExecutor = BrExecutors.getThreadPool(20,20);
        }

        Date createTime=Date.from(LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

        //获取回传配置
        ScorePushCustomerConfigExample scorePushCustomerConfigExample = new ScorePushCustomerConfigExample();
        scorePushCustomerConfigExample.createCriteria().andApiCodeEqualTo(apiCode).andIsDelEqualTo(Constants.DATA_VALID);
        List<ScorePushCustomerConfig> scorePushCustomerConfigs = scorePushCustomerConfigMapper.selectByExample(scorePushCustomerConfigExample);
        if(scorePushCustomerConfigs.size()<=0){
            log.warn(String.format("该客户未配置回传参数配置,apiCode:%s",apiCode));
            return;
        }
        pushCustomerConfig = scorePushCustomerConfigs.get(0);

        //跑分筛选条件配置
        List<ConditionOfScoreVO> scoreCondtitions = scoreSearchConditionMapper.getScoreByConditionType(apiCode, 3);
        if(scoreCondtitions.size()<=0||scoreCondtitions.size()>1){
            log.warn(String.format("该客户跑分筛选条件配置异常,apiCode:%s",apiCode));
            return;
        }
        condition = scoreCondtitions.get(0);
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
            String ruleNumber = StringUtils.isNotBlank(pushCustomerConfig.getScoreRuleShortName())
                    ? pushCustomerConfig.getScoreRuleShortName()
                    : "";
            List<StraHisFile> fileByRule = straHisFileMapper.getFileByRule(createTime, ruleNumber);
            StraHisFile straHisFile = fileByRule.get(0);
            straHisFileList.add(straHisFile);
        }
        //endregion

        if(straHisFileList.size()<=0){
            log.warn(String.format("该客户当前无跑分记录,apiCode:%s",apiCode));
            return;
        }

        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(2, 5, "job_scoreBackSort");

        for (StraHisFile straHisFile : straHisFileList) {
            List<ScoreSortJsonVO> vos = new ArrayList<>();
            // region 获取排序字段集合
            for (int i = 0; i < 4; i++) {
                ScoreSortJsonVO scoreSortJson = null;
                switch (i){
                    case 0:
                        if(StringUtils.isNotBlank(pushCustomerConfig.getScoreSort1Mapping())){
                            scoreSortJson = JSON.parseObject(pushCustomerConfig.getScoreSort1Mapping(), ScoreSortJsonVO.class);
                        }
                    case 1:
                        if(StringUtils.isNotBlank(pushCustomerConfig.getScoreSort2Mapping())){
                            scoreSortJson = JSON.parseObject(pushCustomerConfig.getScoreSort1Mapping(), ScoreSortJsonVO.class);
                        }
                    case 2:
                        if(StringUtils.isNotBlank(pushCustomerConfig.getScoreSort3Mapping())){
                            scoreSortJson = JSON.parseObject(pushCustomerConfig.getScoreSort1Mapping(), ScoreSortJsonVO.class);
                        }
                    case 3:
                        if(StringUtils.isNotBlank(pushCustomerConfig.getScoreSort4Mapping())){
                            scoreSortJson = JSON.parseObject(pushCustomerConfig.getScoreSort1Mapping(), ScoreSortJsonVO.class);
                        }
                }
                if(vos.size()<=0 && scoreSortJson !=null){
                    scoreSortJson.setFirst(Boolean.TRUE);
                }
                if(scoreSortJson !=null){
                    vos.add(scoreSortJson);
                }
            }
            // endregion

            for (ScoreSortJsonVO vo : vos) {
                threadPool.submit()
            }
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
        }

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

    private void searchData(String apiCode,String batchNumber
            ,String fileId,JSONObject queryData,ScoreSortJsonVO scoreSortJsonVO
    ,ThreadPoolExecutor executors){
        if(scoreSortJsonVO !=null){
            JSONObject sort = new JSONObject();
            sort.put("key",scoreSortJsonVO.getSourceKey());
            sort.put("order",scoreSortJsonVO.getSort());
            queryData.put("sort",sort);
        }
        QueryBaseBean queryBaseBean = new QueryBaseBean();
        queryBaseBean.setApiCode(apiCode);
        queryBaseBean.setBatchNumbers(batchNumber);
        queryBaseBean.setFileIds(fileId);
        queryBaseBean.setJsonData(JSON.toJSONString(queryData));
        int total = marketingHistoryEsService.builderMarketingWithTotal(queryBaseBean);
        String searchAfterStr="";
        Integer pageSize = 2000;
        int totalYuShu = total % pageSize;
        int totalPage = total / pageSize + (totalYuShu > 0 ? 1 : 0);
        for (int i = 1; i <= totalPage; i++) {
            if (i == totalPage && totalYuShu > 0) {
                queryBaseBean.setPageSize(totalYuShu);
            } else {
                queryBaseBean.setPageSize(pageSize);
            }
            queryBaseBean.setSearchAfter(searchAfterStr);
            List<MarketingHistory> marketingHistories = marketingHistoryEsService.builderMarketingWithList(queryBaseBean);
            if (marketingHistories.size() > 0) {

            }
        }
    }

    class StoreData implements Callable<List<Future<Result<Integer>>>>{

        List<MarketingHistory> marketingHistories;

        ScoreSortJsonVO scoreSortJsonVO;

        Boolean first;

        List<HxResultVO> hxResultVOS;

        Integer startIndex;

        public StoreData(List<MarketingHistory> marketingHistories,ScoreSortJsonVO scoreSortJsonVO,Boolean first,Integer startIndex){
            this.marketingHistories = marketingHistories;
            this.scoreSortJsonVO = scoreSortJsonVO;
            this.first = first;
            this.startIndex = startIndex;
        }

        @Override
        public List<Future<Result<Integer>>> call() throws Exception {
            if(marketingHistories.size()>0){
                for (MarketingHistory marketingHistory : marketingHistories) {
                    Integer nowNumber;
                    if(scoreSortJsonVO !=null){
                        nowNumber = startIndex;
                        startIndex++;
                    }

                    List pushParams = new ArrayList();
                    if(first){
                        hxResultVOS.forEach(t->{
                            HxResultVO hxResultVO = new HxResultVO();
                            BeanUtils.copyProperties(t,hxResultVO);
                            switch (t.getSourceKey()){
                                case "apiCode":
                                    hxResultVO.setValue(marketingHistory.getApiCode());
                                case "custNum":
                                    hxResultVO.setValue(marketingHistory.getCusNum());
                                case "idCard":
                                    hxResultVO.setValue(marketingHistory.getIdCard());
                                case "cell":
                                    hxResultVO.setValue(marketingHistory.getCell());
                                case "name":
                                    hxResultVO.setValue(marketingHistory.getName());
                                case "swiftNumber":
                                    hxResultVO.setValue(marketingHistory.getSwiftNumber());
                                case "requestTime":
                                    hxResultVO.setValue(new SimpleDateFormat("yyyy-MM-dd").format(marketingHistory.getRequestTime()));
                                case "batchNumber":
                                    hxResultVO.setValue(marketingHistory.getBatchNumber());
                                case "cusBatchNumber":
                                    hxResultVO.setValue(marketingHistory.getCusBatchNumber());
                                case "taskId":
                                    hxResultVO.setValue(marketingHistory.getTaskId());
                                case "userType":
                                    hxResultVO.setValue(marketingHistory.getUserType());
                            }
                            pushParams.add(hxResultVO);
                        });
                        PushCustomerDetail pushCustomerDetail = new PushCustomerDetail();
                    }else{

                    }

                }
            }
            return null;
        }
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
