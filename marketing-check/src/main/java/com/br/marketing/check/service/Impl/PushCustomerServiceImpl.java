package com.br.marketing.check.service.Impl;

import IceInternal.Ex;
import cn.hutool.core.collection.ListUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.common.util.DateUtils;
import com.br.marketing.check.service.PushCustomerService;
import com.br.marketing.check.thread.PushDataThread;
import com.br.marketing.check.utils.MomUtil;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailDTO;
import com.br.marketing.client.zbank.ZbankClient;
import com.br.marketing.client.zbank.ZbankResponse;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.*;
import com.br.marketing.dto.zbank.ZbankLabelRatingReResultDTO;
import com.br.marketing.entity.*;
import com.br.marketing.es.bean.MarketingCondition;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.QueryBaseBean;
import com.br.marketing.es.service.impl.MarketingHistoryEsServiceImpl;
import com.br.marketing.es.util.UuidUtils;
import com.br.marketing.mapper.*;
import com.br.marketing.service.IJobManagerService;
import com.br.marketing.service.Impl.jobmanager.JobManagerServiceImpl;
import com.br.marketing.vo.ConditionOfScoreVO;
import com.br.marketing.vo.TaskExtendInfoVO;
import com.br.marketing.vo.scorepushcustomer.HxResultVO;
import com.br.marketing.vo.scorepushcustomer.ScoreSortJsonVO;
import com.google.common.base.Joiner;
import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
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

    @Autowired
    RedisChgService redisChgService;

    @Resource
    PushCustomerDetailMapper pushCustomerDetailMapper;

    @Value("${api.zbank.api.appId:2a0f9f71_29e5_466c_95a7_8cab99d93880}")
    private String appId;

    @Autowired
    ZbankClient zbankClient;

    @Autowired
    IJobManagerService iJobManagerService;

    @Override
    public void push(Customer customer,Long fileId) {

        //region 获取回传配置信息
        String apiCode = customer.getApiCode();
        ScorePushCustomerConfig pushCustomerConfig = new ScorePushCustomerConfig();
        ConditionOfScoreVO condition = new ConditionOfScoreVO();

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
            if(fileByRule.size()>0) {
                StraHisFile straHisFile = fileByRule.get(0);
                straHisFileList.add(straHisFile);
            }
        }

        if(straHisFileList.size()<=0){
            log.warn(String.format("该客户当前无跑分记录,apiCode:%s",apiCode));
            return;
        }
        //endregion

        //region
        iJobManagerService.isAllowExecute(apiCode,11,LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        //endregion
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(2, 4, "job_scoreBackSort");
        ThreadPoolExecutor dataBuild = BrExecutors.getThreadPool(2, 5, "job_dataBuild");

        for (StraHisFile straHisFile : straHisFileList) {
            List<ScoreSortJsonVO> vos = getScoreSortField(pushCustomerConfig);
            JSONObject conditionJb = JSON.parseObject(condition.getContent());
            if(vos.size()>0){
                for (ScoreSortJsonVO vo : vos) {
                    threadPool.submit(()->{
                        try {
                            searchData(apiCode, straHisFile.getBatchNumber(), straHisFile.getId()
                                    , conditionJb, vo, vo.getFirst(), dataBuild);
                        }catch (Exception ex){
                            log.error(ex.getMessage(),ex);
                        }
                    });
                }
                waitThreadPool(threadPool);
                waitThreadPool(dataBuild);
            }else{
                searchData(apiCode,straHisFile.getBatchNumber()
                        ,straHisFile.getId(),conditionJb
                ,null,true,dataBuild);
                waitThreadPool(dataBuild);
            }

            ThreadPoolExecutor pushPool = BrExecutors.getThreadPool(5, 5, "job_pushCustomer");
            PushCustomerDetailExample pushCustomerDetailExample = new PushCustomerDetailExample();
            pushCustomerDetailExample.setOrderByClause(" id limit 2000");
            PushCustomerDetailExample.Criteria criteria = pushCustomerDetailExample.createCriteria();
            criteria.andFileIdEqualTo(straHisFile.getId()).andPushStatusEqualTo(1);
            Boolean action = Boolean.TRUE;
            Long minId = null;
            while (action){
                if(minId!=null){
                    criteria.andIdGreaterThan(minId);
                }
                List<PushCustomerDetail> pushCustomerDetails = pushCustomerDetailMapper.selectByExample(pushCustomerDetailExample);
                if(pushCustomerDetails.size()<=0){
                    action=Boolean.FALSE;
                    continue;
                }
                minId = pushCustomerDetails.get(pushCustomerDetails.size()-1).getId();
                AtomicInteger error = new AtomicInteger();
                pushPool.submit(()->{
                    Map<String, List<PushCustomerDetail>> taskByMap = pushCustomerDetails.stream()
                            .collect(Collectors.groupingBy(PushCustomerDetail::getTaskId));
                    for (String s : taskByMap.keySet()) {
                        JSONObject reqJb = new JSONObject();
                        JSONObject request = new JSONObject();
                        JSONArray CstInfoArray = new JSONArray();
                        reqJb.put("request", request);
                        request.put("CstInfoArray", CstInfoArray);
                        request.put("TxnSrlNo", appId+LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                                + RandomStringUtils.randomNumeric(8));
                        request.put("TskId", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
                        request.put("TxnDt", s);
                        request.put("TxnTs", LocalTime.now().format(DateTimeFormatter.ofPattern("HHmmssSSS")));
                        request.put("RqsSeqNo", apiCode
                                +"_"+request.getString("TskId")
                                +"_"+UUID.randomUUID().toString());
                        List<PushCustomerDetail> pushCustomerDetails1 = taskByMap.get(s);
                        List<Long> detailIds = new ArrayList<>();
                        for (PushCustomerDetail pushCustomerDetail : pushCustomerDetails1) {
                            PushCustomerDetail updateEntity = new PushCustomerDetail();
                            JSONObject cstInfo = new JSONObject();
                            cstInfo.put("GrpTp", pushCustomerDetail.getUserType());
                            updateEntity.setId(pushCustomerDetail.getId());
                            detailIds.add(pushCustomerDetail.getId());
                            for (ScoreSortJsonVO vo : vos) {
                                if (!vo.getFirst()) {
                                    String key = RedisKeyConstant.SCORE_TO_CUSTOMER_SORT_KEY
                                            .concat(":").concat(straHisFile.getId().toString())
                                            .concat(":").concat(vo.getDbNumber().toString());
                                    String sortIndex = redisChgService.hget(key, pushCustomerDetail.getScoreId());
                                    setScoreSort(vo,updateEntity,Integer.valueOf(sortIndex));
                                    cstInfo.put(vo.getMappingKey(),sortIndex);
                                }else{
                                    cstInfo.put(vo.getMappingKey(),getScoreSortByDb(vo.getDbNumber(),pushCustomerDetail));
                                }
                            }
                            pushCustomerDetailMapper.updateByPrimaryKeySelective(updateEntity);
                            cstInfo.put("CstNo", pushCustomerDetail.getCustNum());
                            CstInfoArray.add(cstInfo);
                        }
                        //region push
                        try {
                            String rqsSeqNo = zbankClient.cMBrScoDaFeBack(reqJb, request.getString("RqsSeqNo"));
                            ZbankResponse<ZbankLabelRatingReResultDTO> rqZbank = new ZbankResponse<>();
                            try {
                                rqZbank = JSONObject.parseObject(rqsSeqNo
                                        , new TypeReference<ZbankResponse<ZbankLabelRatingReResultDTO>>() {
                                        });
                            } catch (Exception e) {
                                log.error(e.getMessage() + "响应：" + rqsSeqNo, e);
                                error.incrementAndGet();
                            }
                            if ("000000".equals(rqZbank.getCode())) {
                                ZbankLabelRatingReResultDTO result1 = rqZbank.getResult();
                                if ("00".equals(result1.getErrCd())) {
                                    PushCustomerDetailExample example = new PushCustomerDetailExample();
                                    example.createCriteria().andIdIn(detailIds);
                                    PushCustomerDetail update = new PushCustomerDetail();
                                    update.setPushStatus(2);
                                    pushCustomerDetailMapper.updateByExampleSelective(update,example);
                                } else if ("500".equals(result1.getErrCd())) {
                                    error.incrementAndGet();
                                } else {
                                    error.incrementAndGet();
                                }
                            } else {
                                error.incrementAndGet();
                            }
                        }catch (Exception ex){
                            error.incrementAndGet();
                        }
                        //endregion
                    }

                });
            }

            straHisFile.setPushStatus(1);
            straHisFileMapper.updateByPrimaryKeySelective(straHisFile);
        }

        /**
         * 等待所有任务都执行完成
         **/
        log.warn("所有任务已加入队列，等待结束-----");

        log.warn("所有批次推送结束，apiCode={},批次数量为{}", apiCode,straHisFileList.size());
    }

    private void waitThreadPool(ThreadPoolExecutor executor){
        executor.shutdown();
        while (true){
            if(executor.isTerminated()){
                log.warn("所有线程都执行结束");
                break;
            }
            try {
                Thread.sleep(6000);
            }catch (Exception e){
            }
        }
    }

    private List<ScoreSortJsonVO> getScoreSortField(ScorePushCustomerConfig pushCustomerConfig){
        List<ScoreSortJsonVO> vos = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            ScoreSortJsonVO scoreSortJson = null;
            switch (i){
                case 0:
                    if(StringUtils.isNotBlank(pushCustomerConfig.getScoreSort1Mapping())){
                        scoreSortJson = JSON.parseObject(pushCustomerConfig.getScoreSort1Mapping(), ScoreSortJsonVO.class);
                        scoreSortJson.setDbNumber(0);
                    }
                    break;
                case 1:
                    if(StringUtils.isNotBlank(pushCustomerConfig.getScoreSort2Mapping())){
                        scoreSortJson = JSON.parseObject(pushCustomerConfig.getScoreSort2Mapping(), ScoreSortJsonVO.class);
                        scoreSortJson.setDbNumber(1);
                    }
                    break;
                case 2:
                    if(StringUtils.isNotBlank(pushCustomerConfig.getScoreSort3Mapping())){
                        scoreSortJson = JSON.parseObject(pushCustomerConfig.getScoreSort3Mapping(), ScoreSortJsonVO.class);
                        scoreSortJson.setDbNumber(2);
                    }
                    break;
                case 3:
                    if(StringUtils.isNotBlank(pushCustomerConfig.getScoreSort4Mapping())){
                        scoreSortJson = JSON.parseObject(pushCustomerConfig.getScoreSort4Mapping(), ScoreSortJsonVO.class);
                        scoreSortJson.setDbNumber(3);
                    }
                    break;
            }
            if(vos.size()<=0 && scoreSortJson !=null){
                scoreSortJson.setFirst(Boolean.TRUE);
            }
            if(scoreSortJson !=null){
                vos.add(scoreSortJson);
            }
        }
        return vos;
    }

    private void setScoreSortField(ScoreSortJsonVO scoreSortJsonVO,PushCustomerDetail detail,Long fileId,Integer index){
        if (scoreSortJsonVO.getFirst()) {
            setScoreSort(scoreSortJsonVO,detail,index);
        }else{
            String key = RedisKeyConstant.SCORE_TO_CUSTOMER_SORT_KEY
                    .concat(":").concat(fileId.toString())
                    .concat(":").concat(scoreSortJsonVO.getDbNumber().toString());
            redisChgService.hset(key,detail.getScoreId(),index.toString());
        }
    }

    private void setScoreSort(ScoreSortJsonVO scoreSortJsonVO,PushCustomerDetail detail,Integer index){
        switch (scoreSortJsonVO.getDbNumber()) {
            case 0:
                detail.setScoreSort1(index.toString());
                break;
            case 1:
                detail.setScoreSort2(index.toString());
                break;
            case 2:
                detail.setScoreSort3(index.toString());
                break;
            case 3:
                detail.setScoreSort4(index.toString());
                break;
        }
    }

    private String getScoreSortByDb(Integer dbNumber,PushCustomerDetail detail){
        switch (dbNumber) {
            case 0:
               return detail.getScoreSort1();
            case 1:
                return detail.getScoreSort2();
            case 2:
                return detail.getScoreSort3();
            case 3:
                return detail.getScoreSort4();
        }
        return "";
    }

    private void searchData(String apiCode,String batchNumber
            ,Long fileId,JSONObject queryData
            ,ScoreSortJsonVO scoreSortJsonVO
            ,Boolean first
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
        queryBaseBean.setFileIds(fileId.toString());
        queryBaseBean.setJsonData(JSON.toJSONString(queryData));
        int total = marketingHistoryEsService.builderMarketingWithTotal(queryBaseBean);
        String searchAfterStr="";
        Integer pageSize = 2000;
        int totalYuShu = total % pageSize;
        int totalPage = total / pageSize + (totalYuShu > 0 ? 1 : 0);
        Integer partStart = 1;
        for (int i = 1; i <= totalPage; i++) {
            if (i == totalPage && totalYuShu > 0) {
                queryBaseBean.setPageSize(totalYuShu);
            } else {
                queryBaseBean.setPageSize(pageSize);
            }
            queryBaseBean.setSearchAfter(searchAfterStr);
            List<MarketingHistory> marketingHistories = marketingHistoryEsService.builderMarketingWithList(queryBaseBean);
            if (marketingHistories.size() > 0) {
                executors.submit(new StoreData(marketingHistories
                        ,fileId,scoreSortJsonVO
                        ,first!=null?first:scoreSortJsonVO.getFirst()
                        ,partStart));
            }
            partStart+=queryBaseBean.getPageSize();
        }
    }

    class StoreData implements Callable<List<Future<Result<Integer>>>>{

        List<MarketingHistory> marketingHistories;

        ScoreSortJsonVO scoreSortJsonVO;

        Boolean first;

        Long fileId;

        List<HxResultVO> hxResultVOS;

        Integer startIndex;

        public StoreData(List<MarketingHistory> marketingHistories
                ,Long fileId
                ,ScoreSortJsonVO scoreSortJsonVO
                ,Boolean first,Integer startIndex){
            this.marketingHistories = marketingHistories;
            this.scoreSortJsonVO = scoreSortJsonVO;
            this.first = first;
            this.startIndex = startIndex;
            this.fileId = fileId;
        }

        @Override
        public List<Future<Result<Integer>>> call() throws Exception {
            try {
                if (marketingHistories.size() > 0) {
                    for (MarketingHistory marketingHistory : marketingHistories) {
                        Integer nowNumber;
                        PushCustomerDetail pushCustomerDetail = new PushCustomerDetail();
                        pushCustomerDetail.setScoreId(marketingHistory.getSwiftNumber());
                        pushCustomerDetail.setFileId(fileId);
                        if (scoreSortJsonVO != null) {
                            nowNumber = startIndex;
                            setScoreSortField(scoreSortJsonVO, pushCustomerDetail, fileId, nowNumber);
                            startIndex++;
                        }

                        List pushParams = new ArrayList();

                        if (first) {
                            pushCustomerDetail.setApiCode(marketingHistory.getApiCode());
                            pushCustomerDetail.setCustNum(marketingHistory.getCusNum());
                            pushCustomerDetail.setCell(marketingHistory.getCell());
                            pushCustomerDetail.setTaskId(marketingHistory.getTaskId());
                            pushCustomerDetail.setUserType(marketingHistory.getUserType());
                            pushCustomerDetail.setUserType(marketingHistory.getUserType());
                            pushCustomerDetail.setCreateTime(new Date());
                            pushCustomerDetailMapper.insertSelective(pushCustomerDetail);
                        }
                    }
                }
                return null;
            }catch (Exception ex){
                log.error(ex.getMessage(),ex);
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
