package com.br.marketing.check.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.check.service.PushCallBackService;
import com.br.marketing.check.service.PushCustomerService;
import com.br.marketing.check.utils.MomUtil;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.zbank.ZbankClient;
import com.br.marketing.client.zbank.ZbankResponse;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.*;
import com.br.marketing.dto.zbank.ZbankLabelRatingReResultDTO;
import com.br.marketing.entity.*;
import com.br.marketing.enums.CallBackPushStatusEnum;
import com.br.marketing.enums.CallBackScoreResourceEnum;
import com.br.marketing.es.bean.MarketingCondition;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.QueryBaseBean;
import com.br.marketing.es.service.impl.MarketingHistoryEsServiceImpl;
import com.br.marketing.es.util.UuidUtils;
import com.br.marketing.mapper.*;
import com.br.marketing.service.IJobManagerService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.EsNewIndexRuleUtils;
import com.br.marketing.vo.ConditionOfScoreVO;
import com.br.marketing.vo.scorepushcustomer.ScoreSortJsonVO;
import io.lettuce.core.KeyValue;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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

    @Resource
    MarketingCustomerMapper marketingCustomerMapper;

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

    @Autowired
    private Map<String, PushCallBackService> pushCallBackMap;

    @Resource
    MarketingTaskMapper marketingTaskMapper;

    @Resource
    MarketingTaskExtendMapper marketingTaskExtendMapper;


    @Override
    public void push(ScorePushCustomerConfig pushCustomerConfig, StraHisFile straHisFile) {

        long start = System.currentTimeMillis();

        //region 获取回传配置信息
        String apiCode = straHisFile.getApiCode();
        Date createTime = Date.from(LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

        //跑分筛选条件配置
        List<ConditionOfScoreVO> scoreCondtitions = scoreSearchConditionMapper
                .getScoreByConditionType(apiCode, 3, pushCustomerConfig.getScoreRuleShortName());
        if (scoreCondtitions.size() <= 0 || scoreCondtitions.size() > 1) {
            log.warn(String.format("该客户跑分筛选条件配置异常,apiCode:%s", apiCode));
            return;
        }
        ConditionOfScoreVO condition = scoreCondtitions.get(0);
        //endregion

        //写入数据的线程数-写入db或者redis
        int dataBuildThread = getPushCustomerResource(pushCustomerConfig, CallBackScoreResourceEnum.WriteDbThreadNumber);

        //查询数据的线程池 按照不同顺序并发查询
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(2, 4, "job_scoreBackSort");
        //写入数据的线程池-写入db或者redis
        ThreadPoolExecutor dataBuild = BrExecutors.getThreadPool(dataBuildThread, dataBuildThread, "job_dataBuild");

        Result<TransferActionFront> allowExecute =
                jobManagerByScorePushServiceImpl
                        .isAllowExecute(apiCode, 11
                                , LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                , straHisFile);
        if (!ResultCode.SUCCESS.getValue().equals(allowExecute.getCode())) {
            return;
        }

        // 获取排序配置
        List<ScoreSortJsonVO> vos = getScoreSortField(pushCustomerConfig);

        // 获取筛选条件
        JSONObject conditionJb = StringUtils.isNotBlank(condition.getContent()) ? JSON.parseObject(condition.getContent()) : null;

        // region 数据捞取
        AtomicInteger getRes = new AtomicInteger();
        Boolean pause = Boolean.FALSE;
        if (CallBackPushStatusEnum.STARTING.getValue().equals(straHisFile.getPushStatus())) {
            if (vos.size() > 0) {
                //region 写入数据库和写入redis顺序
                List<Future<List<Future<Result<Integer>>>>> res = new ArrayList<>();
                for (ScoreSortJsonVO vo : vos) {
                    Future<List<Future<Result<Integer>>>> resFuture = threadPool.submit(new Callable() {
                        @Override
                        public List<Future<Result<Integer>>> call() throws Exception {
                            try {
                                List<Future<Result<Integer>>> futures = searchData(apiCode, straHisFile.getBatchNumber(), straHisFile.getId()
                                        , conditionJb, vo, vo.getFirst(), dataBuild, Collections.singletonList(straHisFile));
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
                //endregion

                //region redis添加过期时间
                for (ScoreSortJsonVO vo : vos) {
                    if (!vo.getFirst()) {
                        String key = RedisKeyConstant.SCORE_TO_CUSTOMER_SORT_KEY
                                .concat(":").concat(straHisFile.toString())
                                .concat(":").concat(vo.getDbNumber().toString());
                        if (redisChgService.exists(key)) {
                            redisChgService.expire(key,60*60*24*3);
                        }
                    }
                }
                //endregion

                //region 核验数据捞取过程是否有错误
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
                        log.error(e.getMessage(), e);
                        Thread.currentThread().interrupt();
                    } catch (ExecutionException e) {
                        log.error(e.getMessage(), e);
                        Thread.currentThread().interrupt();
                    }

                }
                //endregion
            } else {
                //region 无排序字段
                List<Future<Result<Integer>>> futures = searchData(apiCode, straHisFile.getBatchNumber()
                        , straHisFile.getId(), conditionJb
                        , null, true, dataBuild, Collections.singletonList(straHisFile));
                waitThreadPool(dataBuild);
                for (Future<Result<Integer>> future : futures) {
                    try {
                        if (!ResultCode.SUCCESS.getValue().equals(future.get().getCode())) {
                            pause = Boolean.TRUE;
                        }
                    } catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                        Thread.currentThread().interrupt();
                    } catch (ExecutionException e) {
                        log.error(e.getMessage(), e);
                        Thread.currentThread().interrupt();
                    }
                }
                //endregion
            }
        }
        //endregion
        log.warn(String.format("【%s】,跑分文件id【%s】数据捞取耗时：%d"
                , pushCustomerConfig.getScoreRuleShortName()
                , straHisFile.getId()
                , System.currentTimeMillis() - start));
        if (pause) {
            updateFilePushStatus(straHisFile, CallBackPushStatusEnum.GETFAIL);
        }
        if (CallBackPushStatusEnum.GETFAIL.getValue().equals(straHisFile.getPushStatus())) {
            sendAlarm(String.format("数据捞取过程有错误，暂停后续的推送动作！fileId:%d", straHisFile.getId()));
            jobManagerByScorePushServiceImpl.updateJobStatus(allowExecute.getData(), Boolean.FALSE);
            return;
        }

        //region数据更新排序
        Integer retrySort = 2;
        Integer sortIndex = 1;
        while (retrySort != 0) {
            if (CallBackPushStatusEnum.STARTING.getValue().equals(straHisFile.getPushStatus())
                    || CallBackPushStatusEnum.SORTFAIL.getValue().equals(straHisFile.getPushStatus())) {
                log.warn(String.format("【%s】,跑分文件id【%s】更新排序第【%d】次"
                        , pushCustomerConfig.getScoreRuleShortName()
                        , straHisFile.getId()
                        , sortIndex));
                AtomicInteger errorSort = new AtomicInteger();
                if (vos.size() > 1) {
                    sortDb(straHisFile, vos, errorSort, pushCustomerConfig);
                }
                if (errorSort.get() <= 0) {
                    updateFilePushStatus(straHisFile, CallBackPushStatusEnum.SORTOK);
                    retrySort = 0;
                } else {
                    retrySort--;
                    updateFilePushStatus(straHisFile, CallBackPushStatusEnum.SORTFAIL);
                    sendAlarm(String.format("数据更新顺序过程有错误，暂停后续的推送动作！fileId:%d", straHisFile.getId()));
                    jobManagerByScorePushServiceImpl.updateJobStatus(allowExecute.getData(), Boolean.FALSE);
                }
            }else{
                retrySort = 0;
            }
            sortIndex++;
        }
        if (CallBackPushStatusEnum.SORTFAIL.getValue().equals(straHisFile.getPushStatus())) {
            return;
        }
        //endregion
        log.warn(String.format("【%s】,跑分文件id【%s】更新排序耗时：%d"
                , pushCustomerConfig.getScoreRuleShortName()
                , straHisFile.getId()
                , System.currentTimeMillis() - start));

        //region 数据推送
        Boolean push = Boolean.TRUE;
        Integer num = 1;
        TransferActionFront actionFront = allowExecute.getData();
        while (push) {
            if (CallBackPushStatusEnum.SORTOK.getValue().equals(straHisFile.getPushStatus())
                    || CallBackPushStatusEnum.CALLBACKFAIL.getValue().equals(straHisFile.getPushStatus())) {
                log.warn(String.format("【%s】,跑分文件id【%s】开始推送客户第【%d】次数"
                        , pushCustomerConfig.getScoreRuleShortName()
                        , straHisFile.getId()
                        , num));
                AtomicInteger error = new AtomicInteger();
                PushCallBackService pushCallBackService = pushCallBackMap.get(pushCustomerConfig.getPushMethod());
                pushCallBackService.pushCustomer(straHisFile, vos, error, pushCustomerConfig);
                if (error.get() > 0) {
                    straHisFile.setPushStatus(2);
                    updateFilePushStatus(straHisFile, CallBackPushStatusEnum.CALLBACKFAIL);
                } else {
                    updateFilePushStatus(straHisFile, CallBackPushStatusEnum.SUCCESS);
                }
            } else {
                push = Boolean.FALSE;
            }

            log.warn(String.format("【%s】,跑分文件id【%s】数据推送耗时：%d"
                    , pushCustomerConfig.getScoreRuleShortName()
                    , straHisFile.getId()
                    , System.currentTimeMillis() - start));

            // 修改状态
            if (CallBackPushStatusEnum.SUCCESS.getValue().equals(straHisFile.getPushStatus())) {
                jobManagerByScorePushServiceImpl.updateJobStatus(actionFront, Boolean.TRUE);
            } else {
                jobManagerByScorePushServiceImpl.updateJobStatus(actionFront, Boolean.FALSE);
            }

            //判断是否允许重试
            Result<TransferActionFront> allow = jobManagerByScorePushServiceImpl.isAllowExecute(apiCode, 11
                    , LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    , straHisFile);
            actionFront = allow.getData();
            if (!ResultCode.SUCCESS.getValue().equals(allow.getCode())) {
                push = Boolean.FALSE;
            }
            num++;
        }
        //endregion

    }


    @Override
    public Integer getPushCustomerResource(ScorePushCustomerConfig pushCustomerConfig, CallBackScoreResourceEnum callBackScoreResourceEnum) {
        Integer num = null;
        if (pushCustomerConfig != null
                && StringUtils.isNotBlank(pushCustomerConfig.getResourceConfig())) {
            try {
                JSONObject resourceConfig = JSON.parseObject(pushCustomerConfig.getResourceConfig());
                num = (Integer) resourceConfig.getOrDefault(callBackScoreResourceEnum.getKey(), callBackScoreResourceEnum.getValue());
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }
        if (num == null) {
            num = callBackScoreResourceEnum.getValue();
        }
        log.warn(String.format("跑批回调获取资源配置：【%s】获取【%s】的数量是【%d】"
                , pushCustomerConfig.getScoreRuleShortName()
                , callBackScoreResourceEnum.getKey()
                , num));
        return num;
    }

    private void sendAlarm(String message) {
        alarmApiClient.sendAlarm(message, "跑分推送客户", AlarmSendCodeEnum.EXCEPTION_URGENT.getCode());
    }

    private void sortDb(StraHisFile straHisFile, List<ScoreSortJsonVO> vos, AtomicInteger error, ScorePushCustomerConfig scorePushCustomerConfig) {
        int sortThreadNum = getPushCustomerResource(scorePushCustomerConfig, CallBackScoreResourceEnum.UpdateSortThreadNumber);
        ThreadPoolExecutor pushPool = BrExecutors.getThreadPool(sortThreadNum, sortThreadNum, "job_updateSort");
        Boolean action = Boolean.TRUE;
        Long minId = null;
        while (action) {
            PushCustomerDetailExample pushCustomerDetailExample = new PushCustomerDetailExample();
            pushCustomerDetailExample.setOrderByClause(" id limit 2000");
            PushCustomerDetailExample.Criteria criteria = pushCustomerDetailExample.createCriteria();
            criteria.andFileIdEqualTo(straHisFile.getId()).andPushStatusEqualTo(1);
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
//                    mockError("2");
                    String[] scorIds = new String[pushCustomerDetails.size()];
                    HashMap<String, PushCustomerDetail> detalMap = new HashMap();
                    for (int i = 0; i < pushCustomerDetails.size(); i++) {
                        scorIds[i] = pushCustomerDetails.get(i).getScoreId();
                        PushCustomerDetail updateEntity = new PushCustomerDetail();
                        updateEntity.setId(pushCustomerDetails.get(i).getId());
                        detalMap.put(pushCustomerDetails.get(i).getScoreId(), updateEntity);
                    }
                    for (ScoreSortJsonVO vo : vos) {
                        if (!vo.getFirst()) {
                            String key = RedisKeyConstant.SCORE_TO_CUSTOMER_SORT_KEY
                                    .concat(":").concat(straHisFile.getId().toString())
                                    .concat(":").concat(vo.getDbNumber().toString());
                            List<KeyValue<String, String>> hmget = redisChgService.hmget(key, scorIds);
                            for (KeyValue<String, String> kv : hmget) {
                                if (detalMap.get(kv.getKey()) != null) {
                                    setScoreSort(vo, detalMap.get(kv.getKey()), Integer.valueOf(kv.getValue()));
                                }
                            }
                        }
                    }
                    for (String s : detalMap.keySet()) {
                        PushCustomerDetail pushCustomerDetail = detalMap.get(s);
                        pushCustomerDetailMapper.updateByPrimaryKeySelective(pushCustomerDetail);
                    }
                } catch (Exception ex) {
                    error.incrementAndGet();
                    log.error("更新顺序报错：" + ex.getMessage(), ex);
                }
            });
        }
        waitThreadPool(pushPool);
    }


    private void pushCustomer(StraHisFile straHisFile, List<ScoreSortJsonVO> vos, AtomicInteger error) {
        MarketingCustomerExample customerExample = new MarketingCustomerExample();
        customerExample.createCriteria().andApiCodeEqualTo(straHisFile.getApiCode()).andStatusEqualTo(Byte.valueOf("1"));
        List<MarketingCustomer> marketingCustomers = marketingCustomerMapper.selectByExample(customerExample);
        if (marketingCustomers.size() <= 0) {
            log.warn(String.format("【%s】客户被删除!", straHisFile.getApiCode()));
            return;
        }
        MarketingCustomer marketingCustomer = marketingCustomers.get(0);
        int pushThream = (marketingCustomer.getPushThreadNum() == null
                || Integer.valueOf(0).equals(marketingCustomer.getPushThreadNum()))
                ? 5 : marketingCustomer.getPushThreadNum();
        ThreadPoolExecutor pushPool = BrExecutors.getThreadPool(pushThream, pushThream, "job_pushCustomer");
        Integer pageIndex = 0;
        Integer pageSize = marketingCommonConfig.getScoreTaskPageSizeByPushCustomer() == null
                ? 2000 : marketingCommonConfig.getScoreTaskPageSizeByPushCustomer();
        Integer dataPageSize = marketingCommonConfig.getScoreDataPageSizeByPushCustomer() == null
                ? 1000 : marketingCommonConfig.getScoreDataPageSizeByPushCustomer();
        Boolean taskAction = Boolean.TRUE;
        while (taskAction) {
            Integer start = pageIndex * pageSize;
            List<String> taskId = pushCustomerDetailMapper.getTaskId(straHisFile.getId(), start, pageSize);
            if (taskId.size() <= 0) {
                taskAction = Boolean.FALSE;
                continue;
            }
            log.warn(String.format("分页：%d", start) + JSON.toJSONString(taskId));
            pageIndex++;
            for (String s : taskId) {
                Boolean dataAction = Boolean.TRUE;
                Long minId = null;
                while (dataAction) {
                    PushCustomerDetailExample pushCustomerDetailExample = new PushCustomerDetailExample();
                    pushCustomerDetailExample.setOrderByClause(String.format(" id limit %d", dataPageSize));
                    PushCustomerDetailExample.Criteria criteria = pushCustomerDetailExample.createCriteria();
                    criteria.andFileIdEqualTo(straHisFile.getId()).andTaskIdEqualTo(s).andPushStatusIn(Arrays.asList(1, 3));
                    if (minId != null) {
                        criteria.andIdGreaterThan(minId);
                    }
                    List<PushCustomerDetail> pushCustomerDetails = pushCustomerDetailMapper.selectByExample(pushCustomerDetailExample);
                    if (pushCustomerDetails.size() <= 0) {
                        dataAction = Boolean.FALSE;
                        continue;
                    }
                    minId = pushCustomerDetails.get(pushCustomerDetails.size() - 1).getId();
                    pushPool.submit(() -> {
                        try {
                            JSONObject reqJb = new JSONObject();
                            JSONObject request = new JSONObject();
                            JSONArray cstInfoArray = new JSONArray();
                            reqJb.put("request", request);
                            request.put("CstInfoArray", cstInfoArray);
                            request.put("TxnSrlNo", appId + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                                    + RandomStringUtils.randomNumeric(8));
                            request.put("TskId", s);
                            request.put("TxnDt", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
                            request.put("TxnTs", LocalTime.now().format(DateTimeFormatter.ofPattern("HHmmssSSS")));
                            request.put("RqsSeqNo", marketingCustomer.getApiCode()
                                    + "_" + request.getString("TskId")
                                    + "_" + UUID.randomUUID().toString());
                            List<Long> detailIds = new ArrayList<>();
                            for (PushCustomerDetail pushCustomerDetail : pushCustomerDetails) {
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
                        } catch (Exception e) {
                            log.error("推送客户线程报错" + e.getMessage(), e);
                        }
                    });

                }
            }

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
                        scoreSortJson.setFirst(Boolean.TRUE);
                        scoreSortJson.setDbNumber(0);
                    }
                    break;
                case 1:
                    if (StringUtils.isNotBlank(pushCustomerConfig.getScoreSort2Mapping())) {
                        scoreSortJson = JSON.parseObject(pushCustomerConfig.getScoreSort2Mapping(), ScoreSortJsonVO.class);
                        scoreSortJson.setFirst(Boolean.FALSE);
                        scoreSortJson.setDbNumber(1);
                    }
                    break;
                case 2:
                    if (StringUtils.isNotBlank(pushCustomerConfig.getScoreSort3Mapping())) {
                        scoreSortJson = JSON.parseObject(pushCustomerConfig.getScoreSort3Mapping(), ScoreSortJsonVO.class);
                        scoreSortJson.setFirst(Boolean.FALSE);
                        scoreSortJson.setDbNumber(2);
                    }
                    break;
                case 3:
                    if (StringUtils.isNotBlank(pushCustomerConfig.getScoreSort4Mapping())) {
                        scoreSortJson = JSON.parseObject(pushCustomerConfig.getScoreSort4Mapping(), ScoreSortJsonVO.class);
                        scoreSortJson.setFirst(Boolean.FALSE);
                        scoreSortJson.setDbNumber(3);
                    }
                    break;
                default:
                    break;
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
            , ThreadPoolExecutor executors
            , List<StraHisFile> straHisFiles) {

        JSONObject condtionQuery = new JSONObject();
        if (queryData != null) {
            condtionQuery.putAll(queryData);
        }
        if (scoreSortJsonVO != null) {
            JSONObject sort = new JSONObject();
            sort.put("key", scoreSortJsonVO.getSourceKey());
            sort.put("order", scoreSortJsonVO.getSort());
            condtionQuery.put("sort", sort);
        }
        List<Future<Result<Integer>>> futures = new ArrayList<>();
        QueryBaseBean queryBaseBean = new QueryBaseBean();
        queryBaseBean.setApiCode(apiCode);
        queryBaseBean.setBatchNumbers(batchNumber);
        queryBaseBean.setFileIds(fileId.toString());
        queryBaseBean.setUseNewIndexRule(EsNewIndexRuleUtils.resolveAsMap(straHisFiles, marketingCommonConfig));
        if (condtionQuery.keySet().size() > 0) {
            queryBaseBean.setJsonData(JSON.toJSONString(condtionQuery));
        }
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
                searchAfterStr = marketingHistories.get(marketingHistories.size() - 1).getSearchAfter();
            }
            partStart += queryBaseBean.getPageSize();
        }
        return futures;
    }

    class StoreData implements Callable<Result<Integer>> {

        /**
         * 获取到的跑分数据
         */
        List<MarketingHistory> marketingHistories;

        /**
         * 排序字段配置信息
         */
        ScoreSortJsonVO scoreSortJsonVO;

        /**
         * true-写入DB;false-写入redis
         */
        Boolean first;

        /**
         * 文件id
         */
        Long fileId;

        /**
         * 排序值
         */
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
//                mockError("1");
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
                            if (first) {
                                setScoreSort(scoreSortJsonVO, pushCustomerDetail, nowNumber);
                            } else {
                                sortMap.put(pushCustomerDetail.getScoreId(), nowNumber.toString());
                            }
                            startIndex++;
                        }

                        if (first) {
                            pushCustomerDetail.setApiCode(marketingHistory.getApiCode());
                            pushCustomerDetail.setCustNum(marketingHistory.getCusNum());
                            pushCustomerDetail.setCell(BrCipherMaker.getInstance().encode(marketingHistory.getCell()));
                            pushCustomerDetail.setTaskId(marketingHistory.getTaskId());
                            pushCustomerDetail.setUserType(marketingHistory.getUserType());
                            pushCustomerDetail.setCreateTime(new Date());
                            if (marketingHistory.getCondition().size() > 0) {
                                JSONObject varObject = new JSONObject();
                                for (MarketingCondition marketingCondition : marketingHistory.getCondition()) {
                                    if (org.apache.commons.lang3.StringUtils.isNotBlank(marketingCondition.getCode())) {
                                        varObject.put(marketingCondition.getFieldKey(), marketingCondition.getDValue());
                                    } else {
                                        varObject.put(marketingCondition.getFieldKey(), marketingCondition.getStrValue());
                                    }
                                }
                                pushCustomerDetail.setPushJson(JSON.toJSONString(varObject));
                            }
                            dbEntitys.add(pushCustomerDetail);
                        }
                        if (dbEntitys.size() == 50 || (first && i == marketingHistories.size() - 1)) {
                            pushCustomerDetailMapper.insertBatch(dbEntitys);
                            dbEntitys.clear();
                        }
                        if (sortMap.keySet().size() == 50 || (!first && i == marketingHistories.size() - 1)) {
                            redisChgService.hset(key, sortMap);
                            sortMap.clear();
                        }
                    }
                }
                return new Result().setCode(ResultCode.SUCCESS.getValue());
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                return new Result().setCode(ResultCode.FAIL.getValue());
            }
        }
    }

    @Override
    public List<ScorePushCustomerConfig> getScorePushConfigs() {
        ScorePushCustomerConfigExample scorePushCustomerConfigExample = new ScorePushCustomerConfigExample();
        scorePushCustomerConfigExample.createCriteria().andIsDelEqualTo(Constants.DATA_VALID);
        List<ScorePushCustomerConfig> scorePushCustomerConfigs = scorePushCustomerConfigMapper.selectByExample(scorePushCustomerConfigExample);
        return scorePushCustomerConfigs;
    }

    @Override
    public List<ScorePushCustomerConfig> getScorePushConfigs(Long fileId) {
        List<ScorePushCustomerConfig> configByFileId = scorePushCustomerConfigMapper.getConfigByFileId(fileId);
        return configByFileId;
    }

    @Override
    public StraHisFile getFile(Long fileId) {
        return straHisFileMapper.selectByPrimaryKey(fileId);
    }

    @Override
    public String hasFileLock(Long fileId) {
        String lockValue = UUID.randomUUID().toString();
        String pushKey = RedisKeyConstant.SCORE_TO_CUSTOMER_FILE_KEY.concat(":").concat(fileId.toString());
        if (redisChgService.setnx(pushKey, lockValue, 60 * 60 * 10)) {
            return lockValue;
        }
        return null;
    }

    @Override
    public void removeFileLock(Long fileId, String value) {
        String pushKey = RedisKeyConstant.SCORE_TO_CUSTOMER_FILE_KEY.concat(":").concat(fileId.toString());
        if (value.equals(redisChgService.get(pushKey))) {
            redisChgService.del(pushKey);
        }
    }

    @Override
    public Result<StraHisFile> isPush(ScorePushCustomerConfig pushCustomerConfig) {
        try {
            String lockValue = UUID.randomUUID().toString();
            boolean taskLock = hasTaskLock(pushCustomerConfig.getId(), lockValue);
            if (!taskLock) {
                return new Result().setCode(ResultCode.FAIL.getValue());
            }
            Date createTime = Date.from(LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
            // 一天只推送一次
            if (Integer.valueOf(1).equals(pushCustomerConfig.getPushType())) {
                List<StraHisFile> files = straHisFileMapper.getFileByRule(createTime
                        , pushCustomerConfig.getScoreRuleShortName()
                        , Arrays.asList(CallBackPushStatusEnum.TOBEEXECUTED.getValue()), 0);
                // 判断是否有任务回调过
                if (files.size() > 0) {
                    StraHisFile straHisFile = files.get(0);
                    if (CallBackPushStatusEnum.SORTFAIL.getValue().equals(straHisFile.getPushStatus())) {
                        String content = String.format("客户【%s】，跑分文件【%s】在回调客户作业中执行更新排序错误，请介入"
                                , straHisFile.getApiCode(), straHisFile.getBatchNumber());
                        log.error(content);
                    }
                    if (CallBackPushStatusEnum.GETFAIL.getValue().equals(straHisFile.getPushStatus())) {
                        String content = String.format("客户【%s】，跑分文件【%s】在回调客户作业中执行捞取错误，请介入"
                                , straHisFile.getApiCode(), straHisFile.getBatchNumber());
                        log.error(content);
                    }
                    removeTaskLock(pushCustomerConfig.getId(), lockValue);
                    log.warn("");
                    return new Result().setCode(ResultCode.FAIL.getValue());
                }
            }

            List<StraHisFile> needFiles = straHisFileMapper.getFileByRule(createTime
                    , pushCustomerConfig.getScoreRuleShortName()
                    , Arrays.asList(CallBackPushStatusEnum.TOBEEXECUTED.getValue()), 1);

            if (needFiles.size() <= 0) {
                removeTaskLock(pushCustomerConfig.getId(), lockValue);
                return new Result<>().setCode(ResultCode.FAIL.getValue());
            }
            StraHisFile straHisFile = needFiles.get(0);

            updateFilePushStatus(straHisFile, CallBackPushStatusEnum.STARTING);
            removeTaskLock(pushCustomerConfig.getId(), lockValue);
            return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(straHisFile);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return new Result<>().setCode(ResultCode.FAIL.getValue());
    }

    private void updateFilePushStatus(StraHisFile straHisFile, CallBackPushStatusEnum callBackPushStatusEnum) {
        straHisFile.setPushStatus(callBackPushStatusEnum.getValue());
        StraHisFile updateFile = new StraHisFile();
        updateFile.setPushStatus(callBackPushStatusEnum.getValue());
        updateFile.setId(straHisFile.getId());
        straHisFileMapper.updateByPrimaryKeySelective(updateFile);
    }

    private boolean hasTaskLock(Long id, String lockValue) {
        String pushKey = RedisKeyConstant.SCORE_TO_CUSTOMER_CONFIG_KEY.concat(":").concat(id.toString());
        return redisChgService.setnx(pushKey, lockValue, 10);
    }

    private void removeTaskLock(Long id, String lockValue) {
        String pushKey = RedisKeyConstant.SCORE_TO_CUSTOMER_CONFIG_KEY.concat(":").concat(id.toString());
        String s = redisChgService.get(pushKey);
        if (lockValue.equals(s)) {
            redisChgService.del(pushKey);
        }
    }

    @Override
    public void mockError(String type) {
        if (marketingCommonConfig.getMockCallBackError() != null && marketingCommonConfig.getMockCallBackError().get(type) != null
            && marketingCommonConfig.getMockCallBackError().get(type)) {
            throw new RuntimeException("伪造错误");
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
            log.error(e.getMessage(), e);
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
