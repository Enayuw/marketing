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
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailDTO;
import com.br.marketing.client.zbank.ZbankClient;
import com.br.marketing.client.zbank.ZbankResponse;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
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
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.ConditionOfScoreVO;
import com.br.marketing.vo.TaskExtendInfoVO;
import com.br.marketing.vo.scorepushcustomer.HxResultVO;
import com.br.marketing.vo.scorepushcustomer.ScoreSortJsonVO;
import com.google.common.base.Joiner;
import com.sun.org.apache.xpath.internal.operations.Bool;
import io.lettuce.core.KeyValue;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
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
    IJobManagerService jobManagerByScorePushServiceImpl;

    @Autowired
    AlarmApiClient alarmApiClient;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;


    @Override
    public void push(Customer customer, Long fileId) {

        long start = System.currentTimeMillis();

        //region 获取回传配置信息
        String apiCode = customer.getApiCode();
        ScorePushCustomerConfig pushCustomerConfig = new ScorePushCustomerConfig();
        ConditionOfScoreVO condition = new ConditionOfScoreVO();
        Date createTime = Date.from(LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

        //获取回传配置
        ScorePushCustomerConfigExample scorePushCustomerConfigExample = new ScorePushCustomerConfigExample();
        scorePushCustomerConfigExample.createCriteria().andApiCodeEqualTo(apiCode).andIsDelEqualTo(Constants.DATA_VALID);
        List<ScorePushCustomerConfig> scorePushCustomerConfigs = scorePushCustomerConfigMapper.selectByExample(scorePushCustomerConfigExample);
        if (scorePushCustomerConfigs.size() <= 0) {
            log.warn(String.format("该客户未配置回传参数配置,apiCode:%s", apiCode));
            return;
        }
        pushCustomerConfig = scorePushCustomerConfigs.get(0);

        //跑分筛选条件配置
        List<ConditionOfScoreVO> scoreCondtitions = scoreSearchConditionMapper.getScoreByConditionType(apiCode, 3);
        if (scoreCondtitions.size() <= 0 || scoreCondtitions.size() > 1) {
            log.warn(String.format("该客户跑分筛选条件配置异常,apiCode:%s", apiCode));
            return;
        }
        condition = scoreCondtitions.get(0);
        //endregion

        //region 获取需要回传给客户的跑分文件
        List<StraHisFile> straHisFileList = new ArrayList<>();
        if (fileId != null && fileId > 0) {
            StraHisFile straHisFile = straHisFileMapper.selectByPrimaryKey(fileId);
            if (straHisFile == null) {
                return;
            }
            if (!straHisFile.getApiCode().equals(apiCode)) {
                return;
            }
            straHisFileList.add(straHisFile);
        } else {
            List<StraHisFile> fileByRule = straHisFileMapper.getFileByRule(createTime, pushCustomerConfig.getScoreRuleShortName());
            if (fileByRule.size() > 0) {
                StraHisFile straHisFile = fileByRule.get(0);
                straHisFileList.add(straHisFile);
            }
        }

        if (straHisFileList.size() <= 0) {
            log.warn(String.format("该客户当前无跑分记录,apiCode:%s", apiCode));
            return;
        }
        //endregion

        int dataBuildThread = marketingCommonConfig.getScoreDbAndRedisThreadNum() != null ? marketingCommonConfig.getScoreDbAndRedisThreadNum() : 10;

        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(2, 4, "job_scoreBackSort");
        ThreadPoolExecutor dataBuild = BrExecutors.getThreadPool(dataBuildThread, dataBuildThread, "job_dataBuild");

        for (StraHisFile straHisFile : straHisFileList) {
            Result<TransferActionFront> allowExecute =
                    jobManagerByScorePushServiceImpl
                            .isAllowExecute(apiCode, 11
                                    , LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                    , straHisFile);
            if (!ResultCode.SUCCESS.getValue().equals(allowExecute.getCode())) {
                continue;
            }
            Integer pointStatus = straHisFile.getPushStatus();
            List<ScoreSortJsonVO> vos = getScoreSortField(pushCustomerConfig);
            JSONObject conditionJb = JSON.parseObject(condition.getContent());
            //region 数据捞取
            AtomicInteger getRes = new AtomicInteger();
            Boolean pause = Boolean.FALSE;
            if (pointStatus == 0) {
                if (vos.size() > 0) {
                    List<Future<List<Future<Result<Integer>>>>> res = new ArrayList<>();
                    for (ScoreSortJsonVO vo : vos) {
                        Future<List<Future<Result<Integer>>>> resFuture = threadPool.submit(new Callable() {
                            @Override
                            public List<Future<Result<Integer>>> call() throws Exception {
                                try {
                                    List<Future<Result<Integer>>> futures = searchData(apiCode, straHisFile.getBatchNumber(), straHisFile.getId()
                                            , conditionJb, vo, vo.getFirst(), dataBuild);
                                    return futures;
                                } catch (Exception ex) {
                                    log.error(ex.getMessage(), ex);
                                    return null;
                                }
                            }
                        });
                        res.add(resFuture);
                    }
                    waitThreadPool(threadPool);
                    waitThreadPool(dataBuild);

                    for (Future<List<Future<Result<Integer>>>> re : res) {
                        try {
                            if (re == null) {
                                pause = Boolean.TRUE;
                            } else {
                                List<Future<Result<Integer>>> futures = re.get();
                                for (Future<Result<Integer>> future : futures) {
                                    if (!ResultCode.SUCCESS.getValue().equals(future.get().getCode())) {
                                        pause = Boolean.TRUE;
                                    }
                                }
                            }
                        } catch (InterruptedException e) {
                            log.error(e.getMessage(),e);
                            Thread.currentThread().interrupt();
                        } catch (ExecutionException e) {
                            log.error(e.getMessage(),e);
                            Thread.currentThread().interrupt();
                        }

                    }

                } else {
                    List<Future<Result<Integer>>> futures = searchData(apiCode, straHisFile.getBatchNumber()
                            , straHisFile.getId(), conditionJb
                            , null, true, dataBuild);
                    waitThreadPool(dataBuild);
                    for (Future<Result<Integer>> future : futures) {
                        try {
                            if (!ResultCode.SUCCESS.getValue().equals(future.get().getCode())) {
                                pause = Boolean.TRUE;
                            }
                        } catch (InterruptedException e) {
                            log.error(e.getMessage(),e);
                            Thread.currentThread().interrupt();
                        } catch (ExecutionException e) {
                            log.error(e.getMessage(),e);
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
            //endregion
            log.warn(String.format("数据捞取耗时：%d",System.currentTimeMillis()-start));
            if(pause){
                pointStatus=3;
                straHisFile.setPushStatus(3);
                straHisFileMapper.updateByPrimaryKeySelective(straHisFile);
            }
            if(pointStatus == 3){
                sendAlarm(String.format("数据捞取过程有错误，暂停后续的推送动作！fileId:%d",straHisFile.getId()));
                jobManagerByScorePushServiceImpl.updateJobStatus(allowExecute.getData(), Boolean.FALSE);
                return;
            }
            //region数据更新排序
            if(pointStatus == 0 || pointStatus == 4) {
                AtomicInteger errorSort = new AtomicInteger();
                sortDb(customer, straHisFile, vos, errorSort);
                if (errorSort.get() <= 0) {
                    pointStatus = 0;
                } else {
                    straHisFile.setPushStatus(4);
                    straHisFileMapper.updateByPrimaryKeySelective(straHisFile);
                    sendAlarm(String.format("数据更新顺序过程有错误，暂停后续的推送动作！fileId:%d",straHisFile.getId()));
                    jobManagerByScorePushServiceImpl.updateJobStatus(allowExecute.getData(), Boolean.FALSE);
                    return;
                }
            }
            //endregion
            log.warn(String.format("更新排序耗时：%d",System.currentTimeMillis()-start));
            //region 数据推送
            if (pointStatus == 0 || pointStatus == 2) {
                AtomicInteger error = new AtomicInteger();
                pushCustomer(customer,straHisFile, vos,error);
                if (error.get() > 0) {
                    straHisFile.setPushStatus(2);
                } else {
                    straHisFile.setPushStatus(1);
                }
                straHisFileMapper.updateByPrimaryKeySelective(straHisFile);
            }
            //endregion
            log.warn(String.format("数据推送耗时：%d",System.currentTimeMillis()-start));
            //region 修改状态
            if (straHisFile.getPushStatus().equals(1)) {
                jobManagerByScorePushServiceImpl.updateJobStatus(allowExecute.getData(), Boolean.TRUE);
            } else {
                jobManagerByScorePushServiceImpl.updateJobStatus(allowExecute.getData(), Boolean.FALSE);
            }
            //endregion
        }
    }

    private void sendAlarm(String message){
        alarmApiClient.sendAlarm(message,"跑分推送客户", AlarmSendCodeEnum.EXCEPTION_URGENT.getCode());
    }

    private void sortDb(Customer customer,StraHisFile straHisFile,List<ScoreSortJsonVO> vos,AtomicInteger error){
        int pushThream = (customer.getPushThreadNum() == null
                ||Integer.valueOf(0).equals(customer.getPushThreadNum()))
                ? 5 : customer.getPushThreadNum();
        ThreadPoolExecutor pushPool = BrExecutors.getThreadPool(pushThream, pushThream, "job_pushCustomer");
        PushCustomerDetailExample pushCustomerDetailExample = new PushCustomerDetailExample();
        pushCustomerDetailExample.setOrderByClause(" id limit 2000");
        PushCustomerDetailExample.Criteria criteria = pushCustomerDetailExample.createCriteria();
        criteria.andFileIdEqualTo(straHisFile.getId()).andPushStatusEqualTo(1);
        Boolean action = Boolean.TRUE;
        Long minId = null;
        while (action) {
            if (minId != null) {
                criteria.andIdGreaterThan(minId);
            }
            List<PushCustomerDetail> pushCustomerDetails = pushCustomerDetailMapper.selectByExample(pushCustomerDetailExample);
            if (pushCustomerDetails.size() <= 0) {
                action = Boolean.FALSE;
                continue;
            }
            minId = pushCustomerDetails.get(pushCustomerDetails.size() - 1).getId();
            pushPool.submit(() -> {
                try{
                    String[] scorIds = new String[pushCustomerDetails.size()];
                    HashMap<String,PushCustomerDetail> detalMap = new HashMap();
                    for (int i = 0; i < pushCustomerDetails.size(); i++) {
                        scorIds[i]= pushCustomerDetails.get(i).getScoreId();
                        PushCustomerDetail updateEntity = new PushCustomerDetail();
                        updateEntity.setId(pushCustomerDetails.get(i).getId());
                        detalMap.put(pushCustomerDetails.get(i).getScoreId(),updateEntity);
                    }
                    for (ScoreSortJsonVO vo : vos) {
                        if (!vo.getFirst()) {
                            String key = RedisKeyConstant.SCORE_TO_CUSTOMER_SORT_KEY
                                    .concat(":").concat(straHisFile.getId().toString())
                                    .concat(":").concat(vo.getDbNumber().toString());
                            List<KeyValue<String, String>> hmget = redisChgService.hmget(key, scorIds);
                            for (KeyValue<String, String> kv : hmget) {
                                if (detalMap.get(kv.getKey())!=null) {
                                    setScoreSort(vo,detalMap.get(kv.getKey()),Integer.valueOf(kv.getValue()));
                                }
                            }
                        }
                    }
                    for (String s : detalMap.keySet()) {
                        PushCustomerDetail pushCustomerDetail = detalMap.get(s);
                        pushCustomerDetailMapper.updateByPrimaryKeySelective(pushCustomerDetail);
                    }
                }catch (Exception ex){
                    error.incrementAndGet();
                    log.error("更新顺序报错："+ex.getMessage(),ex);
                }
            });
        }
        waitThreadPool(pushPool);
    }


    private void pushCustomer(Customer customer,StraHisFile straHisFile,List<ScoreSortJsonVO> vos,AtomicInteger error){
        int pushThream = (customer.getPushThreadNum() == null
                ||Integer.valueOf(0).equals(customer.getPushThreadNum()))
                ? 5 : customer.getPushThreadNum();
        ThreadPoolExecutor pushPool = BrExecutors.getThreadPool(pushThream, pushThream, "job_pushCustomer");
        PushCustomerDetailExample pushCustomerDetailExample = new PushCustomerDetailExample();
        pushCustomerDetailExample.setOrderByClause(" id limit 1000");
        PushCustomerDetailExample.Criteria criteria = pushCustomerDetailExample.createCriteria();
        criteria.andFileIdEqualTo(straHisFile.getId()).andPushStatusIn(Arrays.asList(1,3));
        Boolean action = Boolean.TRUE;

        Long minId = null;
        while (action) {
            if (minId != null) {
                criteria.andIdGreaterThan(minId);
            }
            List<PushCustomerDetail> pushCustomerDetails = pushCustomerDetailMapper.selectByExample(pushCustomerDetailExample);
            if (pushCustomerDetails.size() <= 0) {
                action = Boolean.FALSE;
                continue;
            }
            minId = pushCustomerDetails.get(pushCustomerDetails.size() - 1).getId();
            pushPool.submit(() -> {
                try {
                    Map<String, List<PushCustomerDetail>> taskByMap = pushCustomerDetails.stream()
                            .collect(Collectors.groupingBy(PushCustomerDetail::getTaskId));
                    for (String s : taskByMap.keySet()) {
                        JSONObject reqJb = new JSONObject();
                        JSONObject request = new JSONObject();
                        JSONArray cstInfoArray = new JSONArray();
                        reqJb.put("request", request);
                        request.put("CstInfoArray", cstInfoArray);
                        request.put("TxnSrlNo", appId + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                                + RandomStringUtils.randomNumeric(8));
                        request.put("TskId", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
                        request.put("TxnDt", s);
                        request.put("TxnTs", LocalTime.now().format(DateTimeFormatter.ofPattern("HHmmssSSS")));
                        request.put("RqsSeqNo", customer.getApiCode()
                                + "_" + request.getString("TskId")
                                + "_" + UUID.randomUUID().toString());
                        List<PushCustomerDetail> pushCustomerDetails1 = taskByMap.get(s);
                        List<Long> detailIds = new ArrayList<>();
                        for (PushCustomerDetail pushCustomerDetail : pushCustomerDetails1) {
                            JSONObject cstInfo = new JSONObject();
                            cstInfo.put("GrpTp", pushCustomerDetail.getUserType());
                            detailIds.add(pushCustomerDetail.getId());
                            for (ScoreSortJsonVO vo : vos) {
                                cstInfo.put(vo.getMappingKey(), getScoreSortByDb(vo.getDbNumber(), pushCustomerDetail));
                            }
                            cstInfo.put("CstNo", pushCustomerDetail.getCustNum());
                            cstInfoArray.add(cstInfo);
                        }
                        //region push
                        PushCustomerDetailExample example = new PushCustomerDetailExample();
                        example.createCriteria().andIdIn(detailIds);
                        PushCustomerDetail update = new PushCustomerDetail();
                        String rqsSeqNo = "";
                        try {
                            rqsSeqNo = zbankClient.cMBrScoDaFeBack(reqJb, request.getString("RqsSeqNo"));
                            ZbankResponse<ZbankLabelRatingReResultDTO> rqZbank = JSONObject.parseObject(rqsSeqNo
                                    , new TypeReference<ZbankResponse<ZbankLabelRatingReResultDTO>>() {
                                    });
                            if ("000000".equals(rqZbank.getCode())) {
                                ZbankLabelRatingReResultDTO result1 = rqZbank.getResult();
                                if ("00".equals(result1.getErrCd())) {
                                    update.setPushStatus(2);
                                } else if ("500".equals(result1.getErrCd())) {
                                    update.setPushStatus(3);
                                    error.incrementAndGet();
                                } else {
                                    update.setPushStatus(3);
                                    error.incrementAndGet();
                                }
                            } else {
                                update.setPushStatus(3);
                                error.incrementAndGet();
                            }
                        } catch (Exception ex) {
                            log.error(ex.getMessage() + "响应：" + rqsSeqNo, ex);
                            update.setPushStatus(3);
                            error.incrementAndGet();
                        }
                        pushCustomerDetailMapper.updateByExampleSelective(update, example);
                        //endregion
                    }
                } catch (Exception e) {
                    log.error("推送客户线程报错" + e.getMessage(), e);
                }
            });
        }
        waitThreadPool(pushPool);
    }
    private void waitThreadPool(ThreadPoolExecutor executor) {
        executor.shutdown();
        while (true) {
            if (executor.isTerminated()) {
                log.warn("所有线程都执行结束");
                break;
            }
            try {
                Thread.sleep(6000);
            } catch (Exception e) {
            }
        }
    }

    private List<ScoreSortJsonVO> getScoreSortField(ScorePushCustomerConfig pushCustomerConfig) {
        List<ScoreSortJsonVO> vos = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            ScoreSortJsonVO scoreSortJson = null;
            switch (i) {
                case 0:
                    if (StringUtils.isNotBlank(pushCustomerConfig.getScoreSort1Mapping())) {
                        scoreSortJson = JSON.parseObject(pushCustomerConfig.getScoreSort1Mapping(), ScoreSortJsonVO.class);
                        scoreSortJson.setDbNumber(0);
                    }
                    break;
                case 1:
                    if (StringUtils.isNotBlank(pushCustomerConfig.getScoreSort2Mapping())) {
                        scoreSortJson = JSON.parseObject(pushCustomerConfig.getScoreSort2Mapping(), ScoreSortJsonVO.class);
                        scoreSortJson.setDbNumber(1);
                    }
                    break;
                case 2:
                    if (StringUtils.isNotBlank(pushCustomerConfig.getScoreSort3Mapping())) {
                        scoreSortJson = JSON.parseObject(pushCustomerConfig.getScoreSort3Mapping(), ScoreSortJsonVO.class);
                        scoreSortJson.setDbNumber(2);
                    }
                    break;
                case 3:
                    if (StringUtils.isNotBlank(pushCustomerConfig.getScoreSort4Mapping())) {
                        scoreSortJson = JSON.parseObject(pushCustomerConfig.getScoreSort4Mapping(), ScoreSortJsonVO.class);
                        scoreSortJson.setDbNumber(3);
                    }
                    break;
                default:
                    break;
            }
            if (vos.size() <= 0 && scoreSortJson != null) {
                scoreSortJson.setFirst(Boolean.TRUE);
            }
            if (scoreSortJson != null) {
                vos.add(scoreSortJson);
            }
        }
        return vos;
    }

    private void setScoreSort(ScoreSortJsonVO scoreSortJsonVO, PushCustomerDetail detail, Integer index) {
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
            default:
                break;
        }
    }

    private String getScoreSortByDb(Integer dbNumber, PushCustomerDetail detail) {
        switch (dbNumber) {
            case 0:
                return detail.getScoreSort1();
            case 1:
                return detail.getScoreSort2();
            case 2:
                return detail.getScoreSort3();
            case 3:
                return detail.getScoreSort4();
            default:
                return "";
        }
    }

    private List<Future<Result<Integer>>> searchData(String apiCode, String batchNumber
            , Long fileId, JSONObject queryData
            , ScoreSortJsonVO scoreSortJsonVO
            , Boolean first
            , ThreadPoolExecutor executors) {
        if (scoreSortJsonVO != null) {
            JSONObject sort = new JSONObject();
            sort.put("key", scoreSortJsonVO.getSourceKey());
            sort.put("order", scoreSortJsonVO.getSort());
            queryData.put("sort", sort);
        }
        List<Future<Result<Integer>>> futures = new ArrayList<>();
        QueryBaseBean queryBaseBean = new QueryBaseBean();
        queryBaseBean.setApiCode(apiCode);
        queryBaseBean.setBatchNumbers(batchNumber);
        queryBaseBean.setFileIds(fileId.toString());
        queryBaseBean.setJsonData(JSON.toJSONString(queryData));
        int total = marketingHistoryEsService.builderMarketingWithTotal(queryBaseBean);
        String searchAfterStr = "";
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
                futures.add(executors.submit(new StoreData(marketingHistories
                        , fileId, scoreSortJsonVO
                        , first != null ? first : scoreSortJsonVO.getFirst()
                        , partStart)));
                searchAfterStr = marketingHistories.get(marketingHistories.size()-1).getSearchAfter();
            }
            partStart += queryBaseBean.getPageSize();
        }
        return futures;
    }

    class StoreData implements Callable<Result<Integer>> {

        List<MarketingHistory> marketingHistories;

        ScoreSortJsonVO scoreSortJsonVO;

        Boolean first;

        Long fileId;

        Integer startIndex;

        public StoreData(List<MarketingHistory> marketingHistories
                , Long fileId
                , ScoreSortJsonVO scoreSortJsonVO
                , Boolean first, Integer startIndex) {
            this.marketingHistories = marketingHistories;
            this.scoreSortJsonVO = scoreSortJsonVO;
            this.first = first;
            this.startIndex = startIndex;
            this.fileId = fileId;
        }

        @Override
        public Result<Integer> call() throws Exception {
            try {
                if (marketingHistories.size() > 0) {
                    HashMap<String, String> sortMap = new HashMap<>();
                    ArrayList<PushCustomerDetail> dbEntitys = new ArrayList<>();
                    String key = RedisKeyConstant.SCORE_TO_CUSTOMER_SORT_KEY
                            .concat(":").concat(fileId.toString())
                            .concat(":").concat(scoreSortJsonVO.getDbNumber().toString());
                    for (int i = 0; i < marketingHistories.size(); i++) {
                        MarketingHistory marketingHistory = marketingHistories.get(i);
                        Integer nowNumber;
                        PushCustomerDetail pushCustomerDetail = new PushCustomerDetail();
                        pushCustomerDetail.setScoreId(marketingHistory.getSwiftNumber());
                        pushCustomerDetail.setFileId(fileId);
                        if (scoreSortJsonVO != null) {
                            nowNumber = startIndex;
                            if(first){
                                setScoreSort(scoreSortJsonVO, pushCustomerDetail, nowNumber);
                            }else{
                                sortMap.put(pushCustomerDetail.getScoreId(),nowNumber.toString());
                            }
                            startIndex++;
                        }

                        if (first) {
                            pushCustomerDetail.setApiCode(marketingHistory.getApiCode());
                            pushCustomerDetail.setCustNum(marketingHistory.getCusNum());
                            pushCustomerDetail.setCell(marketingHistory.getCell());
                            pushCustomerDetail.setTaskId(marketingHistory.getTaskId());
                            pushCustomerDetail.setUserType(marketingHistory.getUserType());
                            pushCustomerDetail.setUserType(marketingHistory.getUserType());
                            pushCustomerDetail.setCreateTime(new Date());
                            dbEntitys.add(pushCustomerDetail);
                        }
                        if(dbEntitys.size()==50||(first && i==marketingHistories.size()-1)){
                            pushCustomerDetailMapper.insertBatch(dbEntitys);
                            dbEntitys.clear();
                        }
                        if(sortMap.keySet().size()==50||(!first && i==marketingHistories.size()-1)){
                            redisChgService.hset(key,sortMap);
                            sortMap.clear();
                        }
                    }
                }
                return new Result().setCode(ResultCode.SUCCESS.getValue());
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                return new Result().setCode(ResultCode.SUCCESS.getValue());
            }
        }
    }


    @Override
    public void retry(Customer customer) {
        ExecutorService retryPushExecutor;
        if (customer.getPushThreadNum() != null) {
            retryPushExecutor = BrExecutors.getThreadPool(customer.getPushThreadNum(), customer.getPushThreadNum());
        } else {
            retryPushExecutor = BrExecutors.getThreadPool(20, 20);
        }
        Date createTime = new Date();
        try {
            createTime = DateUtils.parse(DateHelper.getDateAdd(-1), "yyyy-mm-dd");
        } catch (ParseException e) {
            log.error("格式化日期错误", e);
        }

        PushErrorLogExample pushErrorLogExample = new PushErrorLogExample();
        pushErrorLogExample.createCriteria().andApiCodeEqualTo(customer.getApiCode()).andCreateTimeGreaterThanOrEqualTo(createTime).andStatusEqualTo(2);
        List<PushErrorLog> pushErrorLogList = pushErrorLogMapper.selectByExample(pushErrorLogExample);
        pushErrorLogList = pushErrorLogList.stream()
                .filter(pushErrorLogWithBLOBs1 -> pushErrorLogWithBLOBs1.getActualPushTimes() < pushErrorLogWithBLOBs1.getPushTimes())
                .collect(Collectors.toList());

        List<Callable<Boolean>> list = new ArrayList<>();
        for (PushErrorLog pushErrorLog : pushErrorLogList) {
            list.add(() -> {
                JSONObject param = JSONObject.parseObject(pushErrorLog.getRequestStr());
                param.put("requestId", UuidUtils.getUuid());
                Long begin = System.currentTimeMillis();
                JSONObject extendConfigInfoJson = new JSONObject();
                String extendConfigInfo = customer.getExtendConfigInfo();
                if (StringUtils.isNotBlank(extendConfigInfo)) {
                    extendConfigInfoJson = JSONObject.parseObject(extendConfigInfo);
                }
                Boolean isProxy = extendConfigInfoJson.getBoolean("isProxy") == null ? Boolean.TRUE : extendConfigInfoJson.getBoolean("isProxy");
                Map<String, Object> result = httpProxyClient.request(customer.getPushUrl().trim(), param.toJSONString(), isProxy);
                Long end = System.currentTimeMillis();
                String resultStr = result.get("data") != null ? result.get("data").toString() : "";
                String code = "9999";
                if (StringUtils.isNotBlank(resultStr)) {
                    try {
                        JSONObject resultJson = JSONObject.parseObject(resultStr);
                        code = resultJson.getString("code");
                    } catch (Exception e) {
                    }
                }
                if ((Boolean) result.get("result")) {
                    pushErrorLog.setStatus(1);
                }
                pushErrorLog.setActualPushTimes(pushErrorLog.getActualPushTimes() + 1);
                pushErrorLog.setUpdateTime(new Date());
                pushErrorLog.setRequestStr(param.toJSONString());
                pushErrorLog.setResponseStr(resultStr);
                pushErrorLogMapper.updateByPrimaryKeySelective(pushErrorLog);
                MomUtil.sendMom(customer.getApiCode(), pushErrorLog.getRequestStr(), resultStr, end - begin, param.getString("requestId"), code);
                return null;
            });
        }
        try {
            retryPushExecutor.invokeAll(list);
        } catch (InterruptedException e) {
            log.error(e.getMessage(),e);
            Thread.currentThread().interrupt();
        }
        /**
         * 等待所有重试任务都执行完成
         **/
        log.warn("所有任务已加入队列，等待结束-----");
        retryPushExecutor.shutdown();
        while (true) {
            if (retryPushExecutor.isTerminated()) {
                log.warn("所有线程都执行结束");
                break;
            }
            try {
                Thread.sleep(6000);
            } catch (Exception e) {
            }
        }
        log.warn("所有重试任务推送结束，apiCode={},重试任务数量为{}", customer.getApiCode(), pushErrorLogList.size());
    }

}
