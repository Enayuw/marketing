package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.haier.HaierServiceClient;
import com.br.marketing.client.haier.input.HaierReqDTO;
import com.br.marketing.client.haier.output.PushDTO;
import com.br.marketing.client.haier.output.Response2Entity;
import com.br.marketing.client.robotaiapi.RobotaiApiServiceClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rocketmq.MarketingTransferConstants;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.DistributeTypeEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.entity.*;
import com.br.marketing.handle.SnowflakeRedisGeneratorHandle;
import com.br.marketing.mapper.*;
import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.TransferSource;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.service.*;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.br.rocketmq.rocketmq.template.RocketMqTemplate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class YiXinTransferServiceImpl implements IYiXinTransferService {


    @Autowired
    TableCreateServiceImpl tableCreateService;

    @Resource
    MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Resource
    MarketingTransferInfoMapper transferInfoMapper;

    @Resource
    TransferActionFrontMapper transferActionFrontMapper;

    @Resource
    PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;

    @Resource
    MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Autowired
    RedisChgService redisChgService;

    @Autowired
    RobotaiApiServiceClient robotaiApiServiceClient;

    @Autowired
    RabbitMqProducter producter;
    @Resource
    private RocketMqSwitch rocketMqSwitch;
    @Resource
    private RocketMqTemplate template;

    @Autowired
    ZnkfPushService znkfPushService;

    @Autowired
    DynamicParameterServiceImpl dynamicParameterService;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    IDxService iDxService;
    @Resource
    private DataCompareMapper dataCompareMapper;

    @Resource
    HaierDataMapper haierDataMapper;

    @Autowired
    PushDataService pushDataService;

    @Autowired
    HaierServiceClient haierServiceClient;

    @Resource
    DataDistributeDetailLogMapper dataDistributeDetailLogMapper;

    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;
    @Resource
    private SnowflakeRedisGeneratorHandle snowflakeRedisGeneratorHandle;


    @Resource
    private AlarmApiClient alarmClient;
    @Value("${otherConfig.alarm.outsideSecretKey:00}")
    private String secretKey;
    @Value("${otherConfig.alarm.outsideAppName:00}")
    private String appName;


    @Override
    public Result actionYiXinToDx(String apiCode, String date) {

        if (StringUtils.isBlank(date)) {
            date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        if (StringUtils.isBlank(apiCode)) {
            apiCode = "3710012";
        }

        Date dayOfDate = null;
        try {
            dayOfDate = DateUtils.parseDate(date, "yyyy-MM-dd");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //region check 1.查询推送记录；2.查询推送记录的状态；3.查询数据处理情况
        Result<TransferActionFront> frontDataRes = getFrontData(apiCode, date, 2);
        if (!ResultCode.SUCCESS.getValue().equals(frontDataRes.getCode())) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage(frontDataRes.getMessage());
        }
        TransferActionFront frontData = frontDataRes.getData();
        if (frontData != null && new Integer(2).equals(frontData.getStatus())) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("该任务今日已经推送");
        }
        Result<Date> dateResult = checkPush(apiCode, date);
        if (!ResultCode.SUCCESS.getValue().equals(dateResult.getCode())) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage(dateResult.getMessage());
        }
        int hour = LocalDateTime.now().getHour();
        Boolean pushBlackPhoneEnd = znkfPushService.isPushBlackPhoneEnd(apiCode, date);
        if (!pushBlackPhoneEnd && hour < 11) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("11点前未接收到黑名单标志不推送");
        }
        //endregion
        Long frontId = saveFrontData(apiCode, date, 2);

        Boolean mark = Boolean.TRUE;
        Integer page = 0;
        //全局去重custNum集合
        HashSet custNumALL = new HashSet();
        String _nowDay = date;
        String _7startDay = new SimpleDateFormat("yyyy-MM-dd").format(DateUtils.addDays(dayOfDate, -7));
        String _60startDay = new SimpleDateFormat("yyyy-MM-dd").format(DateUtils.addDays(dayOfDate, -60));
        String _endDay = new SimpleDateFormat("yyyy-MM-dd").format(DateUtils.addDays(dayOfDate, -1));

        /*ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(5, 5);*/
        String startDate = LocalDate.now().minusDays(7).toString();
        //查询实时推决策7天内数据
        DataDistributeDetailLogExample logExample = new DataDistributeDetailLogExample();
        DataDistributeDetailLogExample.Criteria criteria = logExample.createCriteria().andApiCodeEqualTo(marketingCommonConfig.
                getYiXinToPolicyApiCode()).andDistributeTypeEqualTo(DistributeTypeEnum.YIXIN_REALTIME_POLICYDATA.getValue()).
                andDistributeDateGreaterThanOrEqualTo(startDate).andDistributeDateLessThan(LocalDate.now().toString());
        List<DataDistributeDetailLog> detailLogList = dataDistributeDetailLogMapper.selectByExample(logExample);
        List<String> logCustNumList = detailLogList.stream().map(DataDistributeDetailLog::getCustNum).collect(Collectors.toList());
        String tcId = tableCreateService.getTcId(apiCode);
        Integer pageSize = dynamicParameterService.getPageSize("yxToDx");
        Integer threadvalue = 0;
        while (mark) {
            threadvalue++;
            final Integer _threadValue = threadvalue;
            Result<List<MarketingTransferSyncUser>> delayData = getDelayData(apiCode, date, page, pageSize);
            if (!ResultCode.SUCCESS.getValue().equals(delayData.getCode())) {
                mark = Boolean.FALSE;
                continue;
            }
            page++;

            //region 获取非实时数据
            List<MarketingTransferSyncUser> data = delayData.getData();
            HashSet<String> custNums = new HashSet();
            //如果返回的数据样本较大，考虑用list在分批查询，暂时先未使用
//            List<String> custNumsList = new ArrayList<>();
            List<Long> dataFilter1 = new ArrayList<>();
            for (MarketingTransferSyncUser datum : data) {
                if (!(StringUtils.isNotBlank(datum.getReserveField1())
                        && datum.getReserveField1().contains("\"transformType\":\"1\""))) {
                    if (!marketingCommonConfig.getYixinNoRealTimeType().contains(datum.getType())) {
                        custNumALL.add(datum.getCustNum());
                        continue;
                    }
                    boolean tagOne = custNumALL.add(datum.getCustNum());
                    boolean tagTwo = logCustNumList.contains(datum.getCustNum());
                    if (tagOne && (!tagTwo)) {
                        //剔除实时推决策7天内数据
                        dataFilter1.add(datum.getId());
//                        custNumsList.add(datum.getCustNum());
                    }
                }
            }
            if (dataFilter1.size() <= 0) {
                continue;
            }
            //endregion

            final String _tApicode = apiCode;
            String pushUid = UUID.randomUUID().toString();

            /*threadPool.submit(() -> {*/
                try {
                    /*//region 获取7天实时和60天非实时 推送记录

                    // 获取不包含当天的前7天实时推人工的custNum _7filerCustNumSet
                    Set<String> _7filerCustNumSet = iDxService
                            .getCustNumByPhoneDx(custNums, _tApicode, _7startDay, _endDay, "1");
                    PhoneSaleRecordInfoDTO _60recordInfoDTO = new PhoneSaleRecordInfoDTO();
                    _60recordInfoDTO.setCustNums(custNums);
                    _60recordInfoDTO.setApiCode(_tApicode);
                    _60recordInfoDTO.setTransferType("0");

                    // 获取最新的一条推送记录 dxRecordLastOne
                    List<PhoneSaleInfoVO> dxRecordLastOne = phoneSaleExtendInfoMapper.getDxRecordLastOne(_60recordInfoDTO);
//                    List<PhoneSaleInfoVO> _60records = phoneSaleExtendInfoMapper.getDxRecordByTransferType(_60recordInfoDTO);
                    Map<String, PhoneSaleInfoVO> _dxRecordLastOneCustNumsMap = new HashMap<>();
                    Map<String, PhoneSaleInfoVO> _dxRecordLastTwo = new HashMap<>();
                    Map<String, PhoneSaleInfoVO> _dxRecordLastThree = new HashMap<>();

                    List<List<PhoneSaleInfoVO>> onwPart = Lists.partition(dxRecordLastOne, 200);
                    for (List<PhoneSaleInfoVO> phoneSaleInfoVOS : onwPart) {
                        List<HashMap<String, String>> _dxRecordLastConditionList = new ArrayList<>();
                        PhoneSaleRecordInfoDTO _60recordInfoDTOpart = new PhoneSaleRecordInfoDTO();
                        _60recordInfoDTOpart.setCustNums(custNums);
                        _60recordInfoDTOpart.setApiCode(_tApicode);
                        _60recordInfoDTOpart.setTransferType("0");
                        for (PhoneSaleInfoVO phoneSaleInfoVO : phoneSaleInfoVOS) {
                            _dxRecordLastOneCustNumsMap.put(phoneSaleInfoVO.getCustNum(), phoneSaleInfoVO);
                            HashMap _dxRecordLastCondition = new HashMap<String, String>();
                            _dxRecordLastCondition.put("custNum", phoneSaleInfoVO.getCustNum());
                            _dxRecordLastCondition.put("appletDate", phoneSaleInfoVO.getAppletDate());
                            _dxRecordLastConditionList.add(_dxRecordLastCondition);
                        }
                        _60recordInfoDTOpart.setCustNumAndApplets(_dxRecordLastConditionList);
                        List<HashMap<String, String>> _dxRecordTwoConditionList = new ArrayList<>();
                        // 获取案件倒数第二条的推送人工记录 _dxRecordLastTwo
                        List<PhoneSaleInfoVO> dxRecordLastTwo = phoneSaleExtendInfoMapper.getDxRecordLastTwo(_60recordInfoDTOpart);
                        if (dxRecordLastTwo != null && dxRecordLastTwo.size() > 0) {
                            for (PhoneSaleInfoVO phoneSaleInfoVO : dxRecordLastTwo) {
                                if (!_dxRecordLastTwo.containsKey(phoneSaleInfoVO.getCustNum())) {
                                    _dxRecordLastTwo.put(phoneSaleInfoVO.getCustNum(),phoneSaleInfoVO);
                                    HashMap _dxRecordTwoCondition = new HashMap<String, String>();
                                    _dxRecordTwoCondition.put("custNum", phoneSaleInfoVO.getCustNum());
                                    _dxRecordTwoCondition.put("appletDate", phoneSaleInfoVO.getAppletDate());
                                    _dxRecordTwoConditionList.add(_dxRecordTwoCondition);
                                }
                            }
                        }

                        // 获取案件倒数第三条的推送人工记录 _dxRecordLastThree
                        if(_dxRecordTwoConditionList.size()>0) {
                            PhoneSaleRecordInfoDTO _90recordInfoDTOpart = new PhoneSaleRecordInfoDTO();
                            _90recordInfoDTOpart.setCustNums(custNums);
                            _90recordInfoDTOpart.setApiCode(_tApicode);
                            _90recordInfoDTOpart.setTransferType("0");
                            _90recordInfoDTOpart.setCustNumAndApplets(_dxRecordTwoConditionList);
                            List<PhoneSaleInfoVO> dxRecordLastThree = phoneSaleExtendInfoMapper.getDxRecordLastTwo(_90recordInfoDTOpart);
                            dxRecordLastThree.forEach(t -> {
                                if (!_dxRecordLastThree.containsKey(t.getCustNum())) {
                                    _dxRecordLastThree.put(t.getCustNum(), t);
                                }
                            });
                        }
                    }
                    //endregion

                    //region 获取当天非实时 推送记录
                    Set<String> _nowfilerCustNumSet = iDxService
                            .getCustNumByPhoneDx(custNums, _tApicode, _nowDay, _nowDay, "0");
                    //endregion

                    //region 7天实时和当天非实时和60天非实时筛选
                    List<MarketingTransferSyncUser> dataFilter2 = new ArrayList<>();
                    for (MarketingTransferSyncUser transferSyncUser : dataFilter1) {
                        if (_7filerCustNumSet.contains(transferSyncUser.getCustNum())) {
                            continue;
                        }
                        if (_nowfilerCustNumSet.contains(transferSyncUser.getCustNum())) {
                            continue;
                        }
                        PhoneSaleInfoVO phoneSaleInfoVO = _dxRecordLastOneCustNumsMap.get(transferSyncUser.getCustNum());
                        if (phoneSaleInfoVO != null) {
                            //与最新的人工记录type进行比较
                            if (!phoneSaleInfoVO.getType().equals(transferSyncUser.getType())) {
                                dataFilter2.add(transferSyncUser);
                                continue;
                            }
                            //与倒数第二新的人工记录type不相同
                            PhoneSaleInfoVO phoneSaleTwo = _dxRecordLastTwo.get(transferSyncUser.getCustNum());
                            if (phoneSaleTwo == null ||
                                    (phoneSaleTwo!=null && !phoneSaleInfoVO.getType().equals(phoneSaleTwo.getType())) ) {
                                long distanceDays = DateHelper
                                        .getDistanceDays(phoneSaleInfoVO.getAppletDate(), transferSyncUser.getRequestData()) + 1;
                                if (distanceDays > 30 && distanceDays <= 90) {
                                    dataFilter2.add(transferSyncUser);
                                    continue;
                                }
                            }

                            //与倒数第三新的人工记录type相同
                            if(phoneSaleTwo!=null && phoneSaleInfoVO.getType().equals(phoneSaleTwo.getType())){
                                PhoneSaleInfoVO phoneSaleThree = _dxRecordLastThree.get(transferSyncUser.getCustNum());
                                if(phoneSaleThree == null ||
                                        (phoneSaleThree !=null && !phoneSaleTwo.getType().equals(phoneSaleThree.getType()))){
                                    long distanceDaysOne = DateHelper
                                            .getDistanceDays(phoneSaleTwo.getAppletDate(), phoneSaleInfoVO.getAppletDate()) + 1;
                                    long distanceDaysTwo = DateHelper
                                            .getDistanceDays(phoneSaleTwo.getAppletDate(), transferSyncUser.getRequestData()) + 1;
                                    if (distanceDaysTwo > 60 && distanceDaysTwo <= 90 && distanceDaysOne >30 && distanceDaysOne <=60) {
                                        dataFilter2.add(transferSyncUser);
                                    }
                                }
                            }
                        } else {
                            dataFilter2.add(transferSyncUser);
                        }
                    }
                    //endregion
                    //region 剔除实时推决策7天内数据
                    //endregion
                    //region 黑名单查询
                    HashMap<String, String> blackData = new HashMap<>();
                    //todo 上线删除
                    if (log.isWarnEnabled()) {
                        List<Long> collect = dataFilter2.stream().map(t -> t.getId()).collect(Collectors.toList());
                        log.warn(String.format("黑名单查询总数据 pushUid：%s,线程数：%d,黑名单数据：%s", pushUid, _threadValue, JSON.toJSONString(collect)));
                    }
                    List<List<MarketingTransferSyncUser>> partition = Lists.partition(dataFilter2, 500);
                    for (List<MarketingTransferSyncUser> marketingTransferSyncUsers : partition) {
                        //todo 上线删除
                        if (log.isWarnEnabled()) {
                            List<Long> collect = marketingTransferSyncUsers.stream().map(t -> t.getId()).collect(Collectors.toList());
                            log.warn(String.format("黑名单查询分页数据 pushUid：%s,线程数：%d,黑名单数据：%s", pushUid, _threadValue, JSON.toJSONString(collect)));
                        }
                        Result<Map<String, String>> result = iDxService.getBlackByTransfer(marketingTransferSyncUsers, _tApicode);
                        if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                            blackData.putAll(result.getData());
                        }
                    }
                    if (log.isInfoEnabled()) {
                        log.info(String.format("黑名单查询 pushUid：%s,黑名单数据：%s", pushUid, JSON.toJSONString(blackData)));
                    }
                    //endregion

                    //region 推送MQ
                    List<Long> ids = dataFilter2.stream()
                            .filter(t -> StringUtils.isBlank(blackData.get(t.getId().toString()))
                                    || !blackData.get(t.getId().toString()).equals("Y"))
                            .map(t -> t.getId()).collect(Collectors.toList());*/
                    List<List<Long>> mqIdgroup = Lists.partition(dataFilter1, 1000);
                    for (List<Long> longs : mqIdgroup) {
                        JSONObject jo = new JSONObject();
                        jo.put("tcId", tcId);
                        jo.put("ids", longs);
                        HashSet<String> rule = new HashSet<>();
                        rule.add("YiXin_NonRealTime_Policy");
                        MqFact mq = new MqFact();
                        mq.setSource(TransferSource.TRANSFER_DATA_SET_PROCESS.getCode());
                        mq.setIncludeRules(rule);
                        mq.setMessage(JSON.toJSONString(jo));
                        mq.setIdempotentKey(snowflakeRedisGeneratorHandle.nextId());

                        String mqStr = JSON.toJSONString(mq);
                        if(rocketMqSwitch.rocketMQSwitchFlag(apiCode, MarketingTransferConstants.TAG_MARKETING_UNIVERSAL_TRANSFER_RECEIVE)){
                            rocketMqSwitch.syncSend(MarketingTransferConstants.TOPIC
                                    , MarketingTransferConstants.TAG_MARKETING_UNIVERSAL_TRANSFER_RECEIVE, mqStr);
                        }else{
                            producter.send(MQConstants.ROUTING_KEY_UNIVERSAL_TRANSFER_RECEIVE, mqStr);
                        }
                        if (log.isWarnEnabled()) {
                            log.warn(String.format("推送非实时决策 pushUid:%s,mq消息：%s", pushUid, mqStr));
                        }
                    }
                    //endregion
                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                }
            //手动清除大集合
            data.clear();
            dataFilter1.clear();
           /* });*/
        }

        /*threadPool.shutdown();
        Boolean threadMark = Boolean.TRUE;
        while (threadMark) {
            if (threadPool.isTerminated()) {
                threadMark = Boolean.FALSE;
            }
            try {
                Thread.sleep(3000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/
        updateFrontDataStatus(frontId, 2);
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }


    /**
     * 推送非实时数据到客服
     */
    @Override
    public Result actionYiXinToRobotAI(String apiCode, String date) {
        if (StringUtils.isBlank(date)) {
            date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        if (StringUtils.isBlank(apiCode)) {
            apiCode = "3710012";
        }
        //region check 1.查询推送记录；2.查询推送记录的状态；3.查询数据处理情况
        Result<TransferActionFront> frontDataRes = getFrontData(apiCode, date, 1);
        if (!ResultCode.SUCCESS.getValue().equals(frontDataRes.getCode())) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage(frontDataRes.getMessage());
        }
        TransferActionFront frontData = frontDataRes.getData();
        if (frontData != null && new Integer(2).equals(frontData.getStatus())) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("该任务今日已经推送");
        }
        Result<Date> dateResult = checkPush(apiCode, date);
        if (!ResultCode.SUCCESS.getValue().equals(dateResult.getCode())) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage(dateResult.getMessage());
        }
        Long frontId = saveFrontData(apiCode, date, 1);
        Boolean mark = Boolean.TRUE;
        Integer page = 0;
        //过滤type的Set
        HashSet custNumFilterType = new HashSet();
        List<Long> ids = new ArrayList<>();
        Integer pageSize = dynamicParameterService.getPageSize("yxToCustomer");
        while (mark) {
            Result<List<MarketingTransferSyncUser>> delayData = getDelayData(apiCode, date, page, pageSize);
            if (!ResultCode.SUCCESS.getValue().equals(delayData.getCode())) {
                mark = Boolean.FALSE;
                continue;
            }
            page++;
            //获取非实时数据
            List<MarketingTransferSyncUser> data = delayData.getData();
            Set<String> set = data.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            Map<String, SyncUserValidityPeriodsBO> syncUserMap =
                    transferDataValidityPeriodService.getValidityPeriodsByCustNum(set, apiCode, new Date());
            for (MarketingTransferSyncUser datum : data) {
                if (!(StringUtils.isNotBlank(datum.getReserveField1())
                        && datum.getReserveField1().contains("\"transformType\":\"1\""))) {
                    //不在推客服的type中，过滤
                    if (!marketingCommonConfig.getYixinNoRealTimePushRobotAIType().contains(datum.getType())) {
                        custNumFilterType.add(datum.getCustNum());
                        continue;
                    }
                    //过滤掉 同一custNum的其他insertTime列，custNumResult
                    if (custNumFilterType.add(datum.getCustNum())) {
                        //过滤type=13，且registerChannel！=1,2的
                        if ("13".equals(datum.getType())) {
                            String registerChannel = JSON.parseObject(datum.getReserveField1()).getString("registerChannel");
                            if (!("1".equals(registerChannel) || "2".equals(registerChannel))) {
                                continue;
                            }
                        }
                        //判断数据有效期
                        SyncUserValidityPeriodsBO userValidityPeriodsBO = syncUserMap.get(datum.getCustNum());
                        if (userValidityPeriodsBO == null) {
                            continue;
                        }
                        ids.add(datum.getId());
                    }
                }
            }
            data.clear();
            syncUserMap.clear();
        }
        custNumFilterType.clear();
        log.warn("宜信非实时数据推送客服数据量 totalNum={}", ids.size());
        long time = System.currentTimeMillis();
        pushRobotAIMessage(apiCode, ids);
        log.warn("apiCode=【{}】宜信非实时数据推送客服结束,耗时={}ms", apiCode, System.currentTimeMillis() - time);
        updateFrontDataStatus(frontId, 2);
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    /**
     * 获取数据
     *
     * @param apiCode
     * @param date
     * @param pageIndex
     * @return
     */
    private Result<List<MarketingTransferSyncUser>> getDelayData(String apiCode, String date, Integer pageIndex, Integer pageSize) {
        String tcId = tableCreateService.getTcId(apiCode);
        Integer limitStart = pageIndex * pageSize;
        List<MarketingTransferSyncUser> transferOrderInsertTime = marketingTransferSyncUserMapper.getTransferOrderInsertTime(tcId, date, limitStart,pageSize);
        if (transferOrderInsertTime.size() <= 0) {
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(transferOrderInsertTime);
    }

    /**
     * 校验数据解析是否完成
     *
     * @param apiCode
     * @param date
     * @return
     */
    @Override
    public Result<Date> checkPush(String apiCode, String date) {

        Date startDate = null;
        try {
            startDate = DateUtils.parseDate(date.concat(" 00:00:00"), "yyyy-MM-dd HH:mm:ss");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date endDate = DateUtils.addDays(startDate, 1);
        MarketingTransferInfoExample infoExample = new MarketingTransferInfoExample();
        infoExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andLastEqualTo("1")
                .andCreateTimeGreaterThanOrEqualTo(startDate)
                .andCreateTimeLessThan(endDate);
        List<MarketingTransferInfo> marketingTransferInfos = transferInfoMapper.selectByExample(infoExample);
        if (marketingTransferInfos.size() <= 0) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("还未传输last标识数据");
        }

        MarketingTransferInfoExample statusExample = new MarketingTransferInfoExample();
        statusExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andStatusEqualTo(1)
                .andCreateTimeGreaterThanOrEqualTo(startDate)
                .andCreateTimeLessThan(marketingTransferInfos.get(0).getCreateTime());
        int statusIngs = transferInfoMapper.countByExample(statusExample);
        if (statusIngs > 0) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("数据还未解析完");
        }

        MarketingTransferInfo transferInfo = marketingTransferInfos.get(0);
        Date limitTime = transferInfo.getCreateTime();
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(limitTime);
    }

    /**
     * 获取推送记录
     *
     * @param apiCode
     * @param date
     * @param actionType
     * @return
     */
    public Result<TransferActionFront> getFrontData(String apiCode, String date, Integer actionType) {
        TransferActionFrontExample frontExample = new TransferActionFrontExample();
        frontExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andActionDataEqualTo(date)
                .andActionTypeEqualTo(actionType)
                .andIsDelEqualTo(1);

        List<TransferActionFront> transferActionFronts = transferActionFrontMapper.selectByExample(frontExample);

        if (transferActionFronts.size() > 1) {
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }

        if (transferActionFronts.size() > 0) {
            return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(transferActionFronts.get(0));
        }

        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(null);
    }

    public Long saveFrontData(String apiCode, String date, Integer actionType) {
        TransferActionFront front = new TransferActionFront();
        front.setApiCode(apiCode);
        front.setStatus(1);
        front.setActionType(actionType);
        front.setActionData(date);
        front.setCreateTime(new Date());
        transferActionFrontMapper.insertSelective(front);
        return front.getId();
    }

    public void updateFrontDataStatus(Long id, Integer status) {
        TransferActionFront front = new TransferActionFront();
        front.setId(id);
        front.setStatus(status);
        transferActionFrontMapper.updateByPrimaryKeySelective(front);
    }

    private Integer getDayByDate(Date d1, Date d2) {
        Calendar aCalendar = Calendar.getInstance();

        aCalendar.setTime(d1);

        int day1 = aCalendar.get(Calendar.DAY_OF_YEAR);

        aCalendar.setTime(d2);

        int day2 = aCalendar.get(Calendar.DAY_OF_YEAR);

        return day2 - day1;
    }

    /**
     * 推送非实时数据到通用mq
     */
    private void pushRobotAIMessage(String apiCode, List<Long> ids) {
        String tcId = tableCreateService.getTcId(apiCode);
        //小于等于500，直接发送last为1
        if (ids.size() <= 500) {
            sendUniversalTransferMq(apiCode, tcId, ids, "1");
            return;
        }
        int pageSize = 500;
        int totalCount = ids.size();
        int pageCount = totalCount % pageSize == 0 ? totalCount / pageSize : totalCount / pageSize + 1;
        String last = "0";
        for (int i = 1; i <= pageCount; i++) {
            List<Long> subList;
            if (i == pageCount) {
                subList = ids.subList((i - 1) * pageSize, totalCount);
                last = "1";
                //最后一次查询
                Date nowDayStartTime = DateHelper.getNowDayStartTime();
                Date newDay = DateHelper.addDays(nowDayStartTime, 1);
                DateTime beginDate = DateTime.now();
                while (true) {
                    DataCompareExample dataCompareExample = new DataCompareExample();
                    dataCompareExample.createCriteria().andCreateTimeBetween(nowDayStartTime, newDay).andTransferInfoIdEqualTo(-1L)
                            .andExternalInterfaceEqualTo(InterfaceHandlerEnum.CUSTOMER_TRANSFER.getCode());
                    int dateCount = dataCompareMapper.countByExample(dataCompareExample);
                    log.warn("宜信非实时数据推客服最后一条消息，dateCount：{}", dateCount);
                    if (dateCount == i - 1) {
                        break;
                    }
                    try {
                        Thread.sleep(10000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    DateTime endDate = DateTime.now();
                    if (Hours.hoursBetween(beginDate, endDate).getHours() >= 1) {
                        log.warn("宜信非实时数据推送客服时间超过1小时，请检查是否存在异常,apiCode:{},send-receive:{},", apiCode, (i - 1) + "-" + dateCount);
                        StringBuilder content = new StringBuilder();
                        content.append("apiCode：".concat(apiCode).concat("\r\n"))
                                .append("已发送批次量：".concat(String.valueOf(i - 1)).concat("\r\n"))
                                .append("接收批次量：".concat(String.valueOf(dateCount)).concat("\r\n"))
                                .append("非实时数据推客服超过1小时，请检查".concat("\r\n"));
                        alarmClient.sendAlarm(content.toString(), "宜信非实时推客服任务", AlarmSendCodeEnum.EXCEPTION_YIXIN_PUSH_CUSTOMER.getCode());
                    }
                }
            } else {
                subList = ids.subList((i - 1) * pageSize, pageSize * (i));
            }
            sendUniversalTransferMq(apiCode, tcId, subList, last);
        }
    }

    private void sendUniversalTransferMq(String apiCode, String tcId, List<Long> subList, String last) {
        JSONObject paramMessage = new JSONObject();
        paramMessage.put("apiCode", apiCode);
        paramMessage.put("tcId", tcId);
        paramMessage.put("ids", subList);
        paramMessage.put("last", last);
        MqFact mqFact = new MqFact();
        mqFact.setIncludeRules(Sets.newHashSet("YiXin_NonRealTime_CustomerTransfer"));
        mqFact.setSource(TransferSource.TRANSFER_DATA_SET_PROCESS.getCode());
        mqFact.setMessage(JSONObject.toJSONString(paramMessage));
        mqFact.setIdempotentKey(snowflakeRedisGeneratorHandle.nextId());

        if(rocketMqSwitch.rocketMQSwitchFlag(apiCode, MarketingTransferConstants.TAG_MARKETING_UNIVERSAL_TRANSFER_RECEIVE)){
            String message = JSON.toJSONString(mqFact);
            rocketMqSwitch.syncSend(MarketingTransferConstants.TOPIC
                    , MarketingTransferConstants.TAG_MARKETING_UNIVERSAL_TRANSFER_RECEIVE, message);
        }else{
            producter.sendToUniversalTransferQueue(mqFact);
        }
    }

    @Override
    public Result actionHaierToDx(String apiCode) {

        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String endDate = LocalDate.parse(date).plusDays(1L).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Integer haierStart = Integer.valueOf(LocalDate.now().minusDays(29L).format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        if (StringUtils.isBlank(apiCode)) {
            apiCode = "3710018";
        }

        //region check 1.查询推送记录；2.查询推送记录的状态；
        if (!(LocalDateTime.now().getHour() >= 10)) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("执行时间未到");
        }
        Result<TransferActionFront> frontDataRes = getFrontData(apiCode, date, 4);
        if (!ResultCode.SUCCESS.getValue().equals(frontDataRes.getCode())) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage(frontDataRes.getMessage());
        }
        TransferActionFront frontData = frontDataRes.getData();
        if (frontData != null && new Integer(2).equals(frontData.getStatus())) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("该任务今日已经推送");
        }
        Long frontId = null;
        if(frontData == null) {
             frontId = saveFrontData(apiCode, date, 4);
        }else{
            frontId = frontData.getId();
        }
        //endregion

        String tcId = tableCreateService.getTcId(apiCode);
        if(StringUtils.isBlank(tcId)){
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("客户表不存在");
        }
        Integer pageIndex = 0;
        Integer pageSize = 5000;
        Boolean action = Boolean.TRUE;

        //region 获取有效期的开始时间
        String haierPeriodOfValidityDay = StringUtils.isBlank(marketingCommonConfig.getHaierPeriodOfValidityDay()) ? "T+30" : marketingCommonConfig.getHaierPeriodOfValidityDay();
        Matcher matcher = Pattern.compile("\\d+").matcher(haierPeriodOfValidityDay);
        String upLoadStart = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        if (matcher.find()) {
            upLoadStart = LocalDate.now().minusDays(Long.valueOf(matcher.group()))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        //endregion

        Integer day = Integer.valueOf(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        List<Result> results = new ArrayList<>();
        List<String> _hasCustNums = new ArrayList<>();
        while (action) {
            Integer pageStart = pageIndex * pageSize;
            List<MarketingTransferSyncUser> transferUsers = marketingTransferSyncUserMapper
                    .getTransferUserByCreateTimeOrder(tcId, apiCode, date, endDate, pageStart, pageSize);
            if (transferUsers.size() <= 0) {
                action = Boolean.FALSE;
                continue;
            }
            List<List<MarketingTransferSyncUser>> partition = ListUtils.partition(transferUsers, 500);
            for (List<MarketingTransferSyncUser> marketingTransferSyncUsers : partition) {
                Set<String> custNums = marketingTransferSyncUsers.stream().map(t -> t.getCustNum()).collect(Collectors.toSet());

                //region 获取原始数据
                List<MarketingSyncUser> custNumAppletDateByCustNumStart = marketingSyncInfoMapper.getCustNumAppletDateByCustNumStart(apiCode, custNums, upLoadStart);
                Map<String, MarketingSyncUser> syncUserMap = custNumAppletDateByCustNumStart.stream().collect(Collectors.toMap(MarketingSyncUser::getCustNum
                        , t -> t
                        , (v1, v2) -> v1.getCreateTime().after(v2.getCreateTime()) ? v1 : v2));
                //endregion

                List<String> custNumList = custNums.stream().collect(Collectors.toList());

                //region 推送记录
                HaierDataExample example = new HaierDataExample();
                example.createCriteria()
                        .andApiCodeEqualTo(apiCode)
                        .andCustNumIn(custNumList)
                        .andTypeEqualTo("1")
                        .andPushStatusEqualTo(2)
                        .andCreateDateGreaterThanOrEqualTo(haierStart);
                List<HaierData> repeatData = haierDataMapper.selectByExample(example);
                Set<String> hasHaierData = repeatData.stream().map(t -> t.getCustNum()).collect(Collectors.toSet());
                //endregion

                Set<PushDTO.DataItems> datas = new HashSet<>();
                HashMap<String, String> ruleTypeMap = new HashMap<>();
                for (MarketingTransferSyncUser marketingTransferSyncUser : marketingTransferSyncUsers) {
                    try {
                        //region check
                        if (!syncUserMap.containsKey(marketingTransferSyncUser.getCustNum())) {
                            continue;
                        }
                        if (hasHaierData.contains(marketingTransferSyncUser.getCustNum())) {
                            continue;
                        }
                        String applyDt = marketingTransferSyncUser.getApplyDt();
                        if (StringUtils.isBlank(applyDt)) {
                            continue;
                        }
                        String applyResult = marketingTransferSyncUser.getApplyResult();
                        if (StringUtils.isBlank(applyResult)) {
                            continue;
                        }
                        if (!"1".equals(applyResult)) {
                            continue;
                        }
                        if (_hasCustNums.contains(marketingTransferSyncUser.getCustNum())) {
                            continue;
                        }
                        //endregion

                        //region 符合type=1的判断
                        Integer type = 0;
                        String ruleType = "";
                        LocalDate _applyDtDate = LocalDate.parse(applyDt.substring(0, 10), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        MarketingSyncUser syncUser = syncUserMap.get(marketingTransferSyncUser.getCustNum());
                        LocalDate userStart = LocalDate.parse(syncUser.getAppletDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        if (StringUtils.isBlank(marketingTransferSyncUser.getRegisterTime())
                                && "1".equals(applyResult)
                                && _applyDtDate.compareTo(userStart) >= 0) {
                            type = 1;
                            ruleType= "1";
                        }
                        if (StringUtils.isNotBlank(marketingTransferSyncUser.getRegisterTime())
                                && LocalDate.parse(marketingTransferSyncUser.getRegisterTime().substring(0, 10), DateTimeFormatter.ofPattern("yyyy-MM-dd")).compareTo(userStart) >= 0
                                && "1".equals(applyResult)
                                && _applyDtDate.compareTo(userStart) >= 0
                                && StringUtils.isNotBlank(marketingTransferSyncUser.getAuditTime())
                                && LocalDate.parse(marketingTransferSyncUser.getAuditTime().substring(0, 10), DateTimeFormatter.ofPattern("yyyy-MM-dd")).compareTo(userStart) >= 0
                                && StringUtils.isBlank(marketingTransferSyncUser.getLentTime())) {
                            type = 1;
                            ruleType= "2";
                        }
                        if (!type.equals(1)) {
                            continue;
                        }
                        //endregion
                        _hasCustNums.add(syncUser.getCustNum());
                        ruleTypeMap.put(syncUser.getCusBatch().concat(":").concat(syncUser.getCustNum()),ruleType);
                        datas.add(new PushDTO.DataItems(syncUser.getCusBatch(), syncUser.getCustNum()));
                    }catch (Exception ex){
                        log.error("数据有问题 数据id："+marketingTransferSyncUser.getId()+";apicode:"+marketingTransferSyncUser.getApiCode());
                    }
                }
                //region 推送数据
                PushDTO.FormData formData = new PushDTO.FormData();
                formData.setDataItems(datas);
                formData.setBatchNo(day.toString().concat("_").concat("1"));
                formData.setType("1");
                formData.setRequestId(pushDataService.getHaierRequestId("1"));

                HaierReqDTO haierReqDTO = new HaierReqDTO();
                haierReqDTO.setFormData(formData);
                haierReqDTO.setApiCode(apiCode);
                haierReqDTO.setRuleMap(ruleTypeMap);
                if(datas.size()>0){
                    Result<Response2Entity> response2EntityResult = haierServiceClient.pushToTeleSalesWithSave(haierReqDTO);
                    results.add(response2EntityResult);
                }
                //endregion
            }
            pageIndex++;
        }
        long count = results.stream().filter(t -> !ResultCode.SUCCESS.getValue().equals(t.getCode())).count();
        if(count<=0){
            updateFrontDataStatus(frontId, 2);
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

}
