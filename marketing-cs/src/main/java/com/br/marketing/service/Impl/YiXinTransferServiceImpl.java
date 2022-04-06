package com.br.marketing.service.Impl;

import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.origin.TransferSource;
import com.br.marketing.service.IDxService;
import com.br.marketing.service.ZnkfPushService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Sets;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.robotaiapi.RobotaiApiServiceClient;
import com.br.marketing.client.robotaiapi.input.BlackQueryDetailDTO;
import com.br.marketing.client.robotaiapi.input.ReqBlackPhoneQueryDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.PhoneSaleRecordInfoDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingTransferInfoMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import com.br.marketing.mapper.TransferActionFrontMapper;
import com.br.marketing.origin.MqFact;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.service.IYiXinTransferService;
import com.br.marketing.vo.PhoneSaleInfoVO;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
                    MqFact mq = new MqFact();
                    mq.setSource(TransferSource.TRANSFER_DATA_SET_PROCESS.getCode());
                    mq.setIncludeRules(Sets.newHashSet());
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
}
