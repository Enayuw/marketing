package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.robotaiapi.RobotaiApiServiceClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.*;
import com.br.marketing.dto.PhoneSaleRecordInfoDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.TransferSource;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.service.IDxService;
import com.br.marketing.service.IYiXinTransferService;
import com.br.marketing.service.ZnkfPushService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.br.marketing.vo.PhoneSaleInfoVO;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
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

    @Autowired
    RedisChgService redisChgService;

    @Autowired
    RobotaiApiServiceClient robotaiApiServiceClient;

    @Autowired
    RabbitMqProducter producter;

    @Autowired
    ZnkfPushService znkfPushService;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    IDxService iDxService;
    @Resource
    private DataCompareMapper dataCompareMapper;

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
        String _7startDay = new SimpleDateFormat("yyyy-MM-dd").format(DateUtils.addDays(dayOfDate, -7));
        String _60startDay = new SimpleDateFormat("yyyy-MM-dd").format(DateUtils.addDays(dayOfDate, -60));
        String _endDay = new SimpleDateFormat("yyyy-MM-dd").format(DateUtils.addDays(dayOfDate, -1));
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(5, 5);
        String tcId = tableCreateService.getTcId(apiCode);
        while (mark) {
            Result<List<MarketingTransferSyncUser>> delayData = getDelayData(apiCode, date, page);
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
            List<MarketingTransferSyncUser> dataFilter1 = new ArrayList<>();
            for (MarketingTransferSyncUser datum : data) {
                if (!(StringUtils.isNotBlank(datum.getReserveField1())
                        && datum.getReserveField1().contains("\"transformType\":\"1\""))) {
                    if (!marketingCommonConfig.getYixinNoRealTimeType().contains(datum.getType())) {
                        custNumALL.add(datum.getCustNum());
                        continue;
                    }
                    if (custNumALL.add(datum.getCustNum()) && custNums.add(datum.getCustNum())) {
                        dataFilter1.add(datum);
//                        custNumsList.add(datum.getCustNum());
                    }
                }
            }
            if (custNums.size() <= 0) {
                continue;
            }
            //endregion

            final String _tApicode = apiCode;
            threadPool.submit(() -> {
                //region 获取7天数据和60天数据
                Set<String> _7filerCustNumSet = iDxService
                        .getCustNumByPhoneDx(custNums, _tApicode, _7startDay, _endDay, "1");
                PhoneSaleRecordInfoDTO _60recordInfoDTO = new PhoneSaleRecordInfoDTO();
                _60recordInfoDTO.setCustNums(custNums);
                _60recordInfoDTO.setApiCode(_tApicode);
                _60recordInfoDTO.setStartDate(_60startDay);
                _60recordInfoDTO.setEndDate(_endDay);
                _60recordInfoDTO.setTransferType("0");
                List<PhoneSaleInfoVO> _60records = phoneSaleExtendInfoMapper.getDxRecordByTransferType(_60recordInfoDTO);
                Map<String, List<PhoneSaleInfoVO>> _60filterCustNumsMap = _60records.stream().collect(Collectors.groupingBy(PhoneSaleInfoVO::getCustNum));
                //endregion

                //region 7天实时和60天非实时筛选
                List<MarketingTransferSyncUser> dataFilter2 = new ArrayList<>();
                for (MarketingTransferSyncUser transferSyncUser : dataFilter1) {
                    if (_7filerCustNumSet.contains(transferSyncUser.getCustNum())) {
                        continue;
                    }
                    List<PhoneSaleInfoVO> phoneSaleInfoVOS = _60filterCustNumsMap.get(transferSyncUser.getCustNum());
                    if (phoneSaleInfoVOS != null && phoneSaleInfoVOS.size() > 0) {
                        phoneSaleInfoVOS.sort((t1, t2) -> {
                            return t1.getAppletDate().compareTo(t2.getAppletDate());
                        });
                        PhoneSaleInfoVO phoneSaleInfoVO = phoneSaleInfoVOS.get(0);
                        if (phoneSaleInfoVO.getType().equals(transferSyncUser.getType())) {
                            if (phoneSaleInfoVOS.size() > 1) {
                                PhoneSaleInfoVO phoneSaleInfoVO1 = phoneSaleInfoVOS.get(1);
                                if (phoneSaleInfoVO1.getType().equals(transferSyncUser.getType())) {
                                    continue;
                                } else {
                                    Date sT = null;
                                    Date eT = null;
                                    try {
                                        sT = DateUtils.parseDate(phoneSaleInfoVO.getAppletDate(), "yyyy-MM-dd");
                                        eT = DateUtils.parseDate(transferSyncUser.getRequestData(), "yyyy-MM-dd");
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    if (sT == null || eT == null) {
                                        continue;
                                    }
                                    Integer dayByDate = getDayByDate(sT, eT);
                                    if (dayByDate >= 30) {
                                        dataFilter2.add(transferSyncUser);
                                    } else {
                                        continue;
                                    }
                                }
                            } else {
                                dataFilter2.add(transferSyncUser);
                            }
                        } else {
                            dataFilter2.add(transferSyncUser);
                        }
                    } else {
                        dataFilter2.add(transferSyncUser);
                    }
                }
                //endregion

                //region 黑名单查询
                HashMap<String, String> blackData = new HashMap<>();
                List<List<MarketingTransferSyncUser>> partition = Lists.partition(dataFilter2, 500);
                for (List<MarketingTransferSyncUser> marketingTransferSyncUsers : partition) {
                    Result<Map<String, String>> result = iDxService.getBlackByTransfer(marketingTransferSyncUsers, _tApicode);
                    if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                        blackData.putAll(result.getData());
                    }
                }
                //endregion

                //region 推送MQ
                List<Long> ids = dataFilter2.stream()
                        .filter(t -> StringUtils.isBlank(blackData.get(t.getId()))
                                || !blackData.get(t.getId()).equals("Y"))
                        .map(t -> t.getId()).collect(Collectors.toList());
                List<List<Long>> mqIdgroup = Lists.partition(ids, 1000);
                for (List<Long> longs : mqIdgroup) {
                    JSONObject jo = new JSONObject();
                    jo.put("tcId", tcId);
                    jo.put("ids", longs);
                    HashSet<String> rule = new HashSet<>();
                    rule.add("YiXin_NonRealTime_Dx");
                    MqFact mq = new MqFact();
                    mq.setSource(TransferSource.TRANSFER_DATA_SET_PROCESS.getCode());
                    mq.setIncludeRules(rule);
                    mq.setMessage(JSON.toJSONString(jo));
//                    mq.setIncludeRules();
                    producter.send(MQConstants.ROUTING_KEY_UNIVERSAL_TRANSFER_RECEIVE, JSON.toJSONString(mq));
                }
                //endregion
            });
        }

        threadPool.shutdown();
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
        }
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
        //去重后的Set
        HashSet custNumResult = new HashSet();
        List<Long> ids = new ArrayList<>();
        while (mark) {
            Result<List<MarketingTransferSyncUser>> delayData = getDelayData(apiCode, date, page);
            if (!ResultCode.SUCCESS.getValue().equals(delayData.getCode())) {
                mark = Boolean.FALSE;
                continue;
            }
            page++;
            //获取非实时数据
            List<MarketingTransferSyncUser> data = delayData.getData();
            for (MarketingTransferSyncUser datum : data) {
                if (!(StringUtils.isNotBlank(datum.getReserveField1())
                        && datum.getReserveField1().contains("\"transformType\":\"1\""))) {
                    //不在推客服的type中，过滤
                    if (!marketingCommonConfig.getYixinNoRealTimePushRobotAIType().contains(datum.getType())) {
                        custNumFilterType.add(datum.getCustNum());
                        continue;
                    }
                    //过滤掉 同一custNum的其他insertTime列，custNumResult
                    if (custNumFilterType.add(datum.getCustNum()) && custNumResult.add(datum.getCustNum())) {
                        ids.add(datum.getId());
                    }
                }
            }
        }
        custNumFilterType.clear();
        custNumResult.clear();
        log.warn("宜信非实时数据推送客服数据量 totalNum={}",ids.size());
        if (ids.size() <= 5) {
            log.error("宜信非实时数据量小于500,请检查");
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("宜信非实时数据量小于500");
        }
        long time = System.currentTimeMillis();
        pushRobotAIMessage(apiCode, ids);
        log.warn("apiCode=【{}】宜信非实时数据推送客服结束,耗时={}ms",apiCode,System.currentTimeMillis() - time);
        updateFrontDataStatus(frontId,2);
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
    private Result<List<MarketingTransferSyncUser>> getDelayData(String apiCode, String date, Integer pageIndex) {
        String tcId = tableCreateService.getTcId(apiCode);
        Integer limitStart = pageIndex * 5000;
        List<MarketingTransferSyncUser> transferOrderInsertTime = marketingTransferSyncUserMapper.getTransferOrderInsertTime(tcId, date, limitStart);
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
    private Result<Date> checkPush(String apiCode, String date) {

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
                .andCreateTimeLessThan(endDate);
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
    private Result<TransferActionFront> getFrontData(String apiCode, String date, Integer actionType) {
        TransferActionFrontExample frontExample = new TransferActionFrontExample();
        frontExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andActionDataEqualTo(date)
                .andActionTypeEqualTo(actionType)
                .andIsDelEqualTo(1);

        List<TransferActionFront> transferActionFronts = transferActionFrontMapper.selectByExample(frontExample);

        if (transferActionFronts.size() > 1) {
            log.error(String.format("该推送日志当前有条 请检查apiCode:%s,data:%s,type:%s", apiCode, date, actionType));
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }

        if (transferActionFronts.size() > 0) {
            return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(transferActionFronts.get(0));
        }

        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(null);
    }

    private Long saveFrontData(String apiCode, String date, Integer actionType) {
        TransferActionFront front = new TransferActionFront();
        front.setApiCode(apiCode);
        front.setStatus(1);
        front.setActionType(actionType);
        front.setActionData(date);
        front.setCreateTime(new Date());
        transferActionFrontMapper.insertSelective(front);
        return front.getId();
    }

    private void updateFrontDataStatus(Long id, Integer status) {
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
        int pageSize = 5;
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
                while(true) {
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
                    if (Hours.hoursBetween(beginDate, endDate).getHours() > 1) {
                        log.warn("宜信非实时数据推送客服时间超过1小时，请检查是否存在异常,apiCode:{},send-receive:{},",apiCode,(i-1)+"-"+dateCount);
                        StringBuilder content = new StringBuilder();
                        content.append("apiCode：".concat(apiCode).concat("\r\n"))
                                .append("非实时总量：".concat(String.valueOf(dateCount)).concat("\r\n"))
                                .append("已发送批次量：".concat(String.valueOf(i-1)).concat("\r\n"))
                                .append("接收批次量：".concat(String.valueOf(dateCount)).concat("\r\n"))
                                .append("非实时数据推客服超过1小时，请检查".concat("\r\n"));
                        alarmClient.sendAlarm(content.toString(), "宜信非实时推客服任务", appName, secretKey,
                                Constants.sendCodeMap.get("pushToCustomer"));
                    }
                }
            } else {
                subList = ids.subList((i - 1) * pageSize, pageSize * (i));
            }
            JSONObject paramMessage = new JSONObject();
            paramMessage.put("apicode", apiCode);
            paramMessage.put("cid", tcId);
            paramMessage.put("ids", subList);
            paramMessage.put("last", last);
            MqFact mqFact = new MqFact();
            mqFact.setIncludeRules(Sets.newHashSet("YiXin_NonRealTime_CustomerTransfer"));
            mqFact.setSource(TransferSource.TRANSFER_DATA_SET_PROCESS.getCode());
            mqFact.setMessage(JSONObject.toJSONString(paramMessage));
            producter.sendToUniversalTransferQueue(mqFact);
            log.warn("宜信非实时数据推客服，发送消息，page：{}", i);
        }
    }
}
