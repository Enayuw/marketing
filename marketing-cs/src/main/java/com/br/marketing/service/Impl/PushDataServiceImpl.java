package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.dassservice.DassServiceClient;
import com.br.marketing.client.dassservice.input.DassImportAdapDTO;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.client.haier.HaierServiceClient;
import com.br.marketing.client.haier.output.PushDTO;
import com.br.marketing.client.marketingapi.MarketingApiService;
import com.br.marketing.client.marketingapi.input.PushTransferDataDTO;
import com.br.marketing.client.marketingapi.input.PushTransferDataDetailDTO;
import com.br.marketing.client.twosevenservice.TwoSevenService;
import com.br.marketing.client.twosevenservice.intput.RequestSevenDTO;
import com.br.marketing.client.twosevenservice.output.ResponseSevenZDTO;
import com.br.marketing.client.twosevenservice.output.SevenDetailVO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.service.PushDataService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PushDataServiceImpl implements PushDataService {

    @Autowired
    PhoneSaleMapper phoneSaleMapper;

    @Autowired
    TwosevenFileMapper twosevenFileMapper;

    @Autowired
    DassServiceClient dassServiceClient;

    @Autowired
    RetryMainLogMapper retryMainLogMapper;

    @Autowired
    RedisChgService redisChgService;

    @Autowired
    LocalFileMapper localFileMapper;

    @Resource
    private MarketingTransferInfoMapper marketingTransferInfoMapper;

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Resource
    private MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Resource
    private AlarmApiClient alarmClient;
    @Value("${otherConfig.alarm.outsideSecretKey:00}")
    private String secretKey;
    @Value("${otherConfig.alarm.outsideAppName:00}")
    private String appName;

    @Autowired
    TwoSevenService twoSevenService;

    @Autowired
    MarketingApiService marketingApiService;

    @Autowired
    HaierDataMapper haierDataMapper;

    @Autowired
    HaierServiceClient haierServiceClient;

    @Override
    public Result pushDassData(Long id) {


        Boolean isContiue = false;
        Boolean actionMark = true;
        Long minId = null;
        String key = "dass:push:threadnum";
        Integer threadNum = 5;
        if (redisChgService.exists(key) && StringUtils.isNotBlank(redisChgService.get(key))) {
            threadNum = Integer.valueOf(redisChgService.get(key));
        }

        LocalFile localFile = localFileMapper.selectByPrimaryKey(id);
        if (localFile == null) {
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("文件不存在").setDate(isContiue);
        }

        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadNum, threadNum);
        Integer number = 0;
        while (actionMark) {
            List<DassImportDataDTO> phoneSales = phoneSaleMapper.getPushDassData(id, minId);
            number += phoneSales.size();
            if (phoneSales.size() > 0) {
                DassImportDataDTO phoneSale = phoneSales.get(phoneSales.size() - 1);
                DassImportAdapDTO dto = new DassImportAdapDTO();
                dto.setLocalId(id);
                dto.setList(phoneSales);
                minId = phoneSale.getId();
                threadPool.submit(() -> {
                    Result result = dassServiceClient.postHermesUserData(dto);
                    if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                        RetryMainLog mainLog = new RetryMainLog();
                        mainLog.setRetryType(1);
                        mainLog.setRetryParam(JSON.toJSONString(dto));
                        mainLog.setRetryParamType(dto.getClass().getName());
                        mainLog.setRetryService("dassServiceClient");
                        mainLog.setRetryMethod("postHermesUserData");
                        mainLog.setRetryNum(0);
                        mainLog.setRetryMaxNum(3);
                        mainLog.setRetryStatus(1);
                        mainLog.setCreateTime(new Date());
                        mainLog.setIncrId(redisChgService.incr(RedisKeyConstant.retryid));
                        retryMainLogMapper.insertSelective(mainLog);
                    }
                });
            } else {
                actionMark = false;
            }
        }
        threadPool.shutdown();
        while (true) {
            if (threadPool.isTerminated()) {
                break;
            }
            try {
                Thread.sleep(3000);
            } catch (Exception e) {
            }
        }
        StringBuilder content = new StringBuilder();
        content.append("apiCode：".concat(localFile.getApiCode()).concat("\r\n"))
                .append("fileName：".concat(localFile.getFileName()).concat("\r\n"))
                .append("数量：".concat(number.toString()).concat("\r\n"))
                .append("文件推送dass结束".concat("\r\n"));
        alarmClient.sendAlarm(content.toString(), "Dass结果文件推送", appName, secretKey,
                Constants.sendCodeMap.get("uploadSuccess"));

        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(isContiue);
    }

    @Override
    public Result pushSevenTransferData(Long id) {
        Boolean isContiue = false;
        try {
            Result result = this.pushAction(id);
            if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                RetryMainLog retryMainLog = new RetryMainLog();
                retryMainLog.setRetryType(1);
                retryMainLog.setRetryParam(JSON.toJSONString(id));
                retryMainLog.setRetryParamType(id.getClass().getName());
                retryMainLog.setRetryService("pushDataServiceImpl");
                retryMainLog.setRetryMethod("pushAction");
                retryMainLog.setRetryNum(0);
                retryMainLog.setRetryMaxNum(3);
                retryMainLog.setRetryStatus(1);
                retryMainLog.setCreateTime(new Date());
                retryMainLog.setIncrId(redisChgService.incr(RedisKeyConstant.retryid));
                retryMainLogMapper.insertSelective(retryMainLog);
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(isContiue);
    }

    public Result pushAction(Long id) {
        Boolean actionMark = true;
        Long minId = null;
        String key = "seven:push:transfer:threadnum";
        Integer threadNum = 5;
        if (redisChgService.exists(key) && StringUtils.isNotBlank(redisChgService.get(key))) {
            threadNum = Integer.valueOf(redisChgService.get(key));
        }

        LocalFile localFile = localFileMapper.selectByPrimaryKey(id);
        if (localFile == null) {
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("文件不存在");
        }
        AtomicInteger errorMark = new AtomicInteger();
        Integer number = 0;
        String yyyyMMddHHmmss = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        while (actionMark) {
            ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadNum, threadNum);
            List<TransferDataItemDTO> dataItems = Collections.synchronizedList(new ArrayList<>());
            List<Long> twoFileIds = Collections.synchronizedList(new ArrayList<>());
            List<TwosevenFile> data = twosevenFileMapper.getPushData(id, minId);
            if (data.size() <= 0) {
                actionMark = false;
                continue;
            }
            minId = data.get(data.size() - 1).getId();
            //region 调用撞库接口
            for (TwosevenFile datum : data) {
                threadPool.submit(() -> {
                    TwosevenFile updateData = new TwosevenFile();
                    updateData.setId(datum.getId());
                    RequestSevenDTO dto = new RequestSevenDTO();
                    dto.setMobile(datum.getMobile());
                    String extendInfo = datum.getLocalId().toString().concat("-").concat(datum.getId().toString());
                    Result<ResponseSevenZDTO> responseSevenZDTOResult = twoSevenService.requestTransferStatus(dto, extendInfo);
                    if (!ResultCode.SUCCESS.getValue().equals(responseSevenZDTOResult.getCode())) {
                        responseSevenZDTOResult = twoSevenService.requestTransferStatus(dto, extendInfo);
                    }
                    if (ResultCode.SUCCESS.getValue().equals(responseSevenZDTOResult.getCode())) {
                        ResponseSevenZDTO responSeven = responseSevenZDTOResult.getData();
                        if ("200".equals(responSeven.getRet())) {
                            SevenDetailVO sevenDetailVO = responSeven.getVolist().get(0);
                            if ("1".equals(sevenDetailVO.getStatus())) {
                                TransferDataItemDTO dataItemDTO = new TransferDataItemDTO();
                                dataItemDTO.setApiCode(datum.getApiCode());
                                dataItemDTO.setCustNum(datum.getCustNum());
                                dataItemDTO.setUserType(datum.getUserType());
                                dataItemDTO.setIfTransform("1");
                                updateData.setTransferOk("1");
                                twoFileIds.add(datum.getId());
                                dataItems.add(dataItemDTO);
                            } else {
                                updateData.setTransferOk("0");
                            }
                            twosevenFileMapper.updateByPrimaryKeySelective(updateData);
                        } else {
                            updateData.setTransferOk(responSeven.getRet());
                            updateData.setDataMessage(responSeven.getMsg());
                            twosevenFileMapper.updateByPrimaryKeySelective(updateData);
                        }
                    } else {
                        errorMark.getAndIncrement();
                    }
                });
            }
            threadPool.shutdown();
            while (true) {
                if (threadPool.isTerminated()) {
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
            }
            //endregion

            //region 推送转化接口
            if (dataItems.size() == 0) {
                continue;
            }
            TransferDataDTO transferDataDTO = new TransferDataDTO();
            transferDataDTO.setDataItems(dataItems);
            transferDataDTO.setRequestId(localFile.getApiCode().concat("_")
                    .concat(yyyyMMddHHmmss).concat("_")
                    .concat(number.toString()));
            PushTransferDataDTO pushTransferDataDTO = new PushTransferDataDTO();
            pushTransferDataDTO.setTwoFileIds(twoFileIds);
            PushTransferDataDetailDTO detailDTO = new PushTransferDataDetailDTO();
            pushTransferDataDTO.setDto(detailDTO);
            Long miId = twoFileIds.get(0);
            Long maId = twoFileIds.get(twoFileIds.size() - 1);
            pushTransferDataDTO.setExtendInfo(localFile.getId().toString()
                    .concat("-").concat(miId.toString())
                    .concat("-").concat(maId.toString()));
            detailDTO.setApiCode(localFile.getApiCode());
            detailDTO.setJsonData(JSON.toJSONString(transferDataDTO));
            Result<Boolean> booleanResult = marketingApiService.pushTransfer(pushTransferDataDTO);
            /** 调用转化接口失败需要重试 */
            if (ResultCode.FAIL.getValue().equals(booleanResult.getCode()) && booleanResult.getData()) {
                RetryMainLog retryMainLog = new RetryMainLog();
                retryMainLog.setRetryType(1);
                retryMainLog.setRetryParam(JSON.toJSONString(pushTransferDataDTO));
                retryMainLog.setRetryParamType(pushTransferDataDTO.getClass().getName());
                retryMainLog.setRetryService("marketingApiService");
                retryMainLog.setRetryMethod("pushTransfer");
                retryMainLog.setRetryNum(0);
                retryMainLog.setRetryMaxNum(3);
                retryMainLog.setRetryStatus(1);
                retryMainLog.setCreateTime(new Date());
                retryMainLog.setIncrId(redisChgService.incr(RedisKeyConstant.retryid));
                retryMainLogMapper.insertSelective(retryMainLog);
            }
            //endregion
            number++;
        }
        /** 调用撞库接口有网络失败的 需要重试 */
        if (errorMark.get() > 0) {
            return new Result().setCode(ResultCode.FAIL.getValue());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }


    @Override
    public Result pushHaierData(Long id) {
        LocalFile localFile = localFileMapper.selectByPrimaryKey(id);
        if (localFile == null) {
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("文件不存在");
        }
        Boolean mark = Boolean.TRUE;
        Long minId = null;
        while (mark) {
            List<HaierData> haierData = haierDataMapper.selectDataLimitId(id, minId);
            if (haierData.size() <= 0) {
                mark = Boolean.FALSE;
            }
            minId = haierData.get(haierData.size() - 1).getId() + 1;
            HashMap<String, Set<PushDTO.DataItems>> types = new HashMap<>();
            for (HaierData haierDatum : haierData) {
                String key = haierDatum.getType().concat("|").concat(haierDatum.getBatchNo());
                if (types.get(key) == null) {
                    Set<PushDTO.DataItems> dataItems = new HashSet<>();
                    types.put(key, dataItems);
                    dataItems.add(new PushDTO.DataItems(haierDatum.getTaskId(), haierDatum.getCustNum()));
                } else {
                    types.get(key)
                            .add(new PushDTO.DataItems(haierDatum.getTaskId(), haierDatum.getCustNum()));
                }
            }
            for (String s : types.keySet()) {
                String[] split = s.split("\\|");
                Set<PushDTO.DataItems> dataItems = types.get(s);
                List<PushDTO.DataItems> collect = dataItems.stream().collect(Collectors.toList());
                List<List<PushDTO.DataItems>> partition = Lists.partition(collect, 500);
                for (List<PushDTO.DataItems> items : partition) {
                    PushDTO.FormData formData = new PushDTO.FormData();
                    formData.setDataItems(items.stream().collect(Collectors.toSet()));
                    formData.setBatchNo(split[1]);
                    formData.setType(split[0]);
                    formData.setRequestId(null);
                    try {
                        haierServiceClient.pushToTeleSales(formData, 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }


        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> pushHaierTransferData(Long id) {
        // 1 先查转化信息表b_marketing_transfer_apiCode 获取apiCode、request_id
        // 2 通过apiCode再查tableCreateService.getTcId(apiCode) 获取Tcid
        // 3 通过Tcid、 apiCode、request_id、user_type=3 查询b_marketing_transfer_sync_cid 获取 cust_num
        // 4 通过 cust_num 查询 b_marketing_sync_apiCode 获取 cus_batch、reserve_field1字段中的type
        // 5 组装完数据入表haierData
        Result<Boolean> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getValue());
        // 1 根据保存到队列的ID查询记录对应的ApiCode、RequestId
        List<MarketingTransferInfo> list = marketingTransferInfoMapper.findApiCodeRequestIdByIdList(id);
        if (CollectionUtils.isEmpty(list)) {
            result.setDate(false);
            String smg = String.format("海尔消金客户转化数据主键为[%s]的基础信息不存在,该信息直接消费,不再重放队列", id);
            log.error(smg);
            result.setMessage(smg);
            alarmClient.sendAlarm(smg, "海尔消金转电销(转化数据)警告", appName, secretKey,
                    Constants.sendCodeMap.get("pushToCustomer"));
            return result;
        }
        result.setDate(true);
        MarketingTransferInfo info = list.get(0);
        String apiCode = info.getApiCode();
//        Date createTime = ObjectUtils.isEmpty(info.getCreateTime()) ? new Date() : info.getCreateTime();
        String requestId = info.getRequestId();
        // 2 获取分表后缀
        String key = "marketing:check:push:haier:".concat(apiCode);
        String tcId = redisChgService.get(key);
        if (StringUtils.isEmpty(tcId)) {
            tcId = tableCreateService.getTcId(apiCode);
            // 缓存一天
            redisChgService.setex(key, tcId, 24 * 3600);
        }
        // 3 获取转化数据,user_type=3
        MarketingTransferSyncUserExample example = new MarketingTransferSyncUserExample();
        example.createCriteria().andApiCodeEqualTo(apiCode).andRequestIdEqualTo(requestId).andUserTypeEqualTo("3");
        example.settCid(tcId);
        int page = 1;
        final int pageSize = 1000;
        List<HaierData> haierDataSet = new ArrayList<>();
        try {
            for (; ; ) {
                PageHelper.startPage(page, pageSize);
                List<MarketingTransferSyncUser> transferList = marketingTransferSyncUserMapper.selectByExample(example);
                if (CollectionUtils.isEmpty(transferList)) {
                    break;
                }
                Set<String> set = transferList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
                if (CollectionUtils.isEmpty(set)) {
                    String msg = String.format("海尔消金转化数据CustNum不存在！infoID:{%s};apiCode:{%s};requestId:{%s};tcid:{%s}"
                            , id, apiCode, requestId, tcId);
                    log.warn(msg);
                    page++;
                    continue;
                }
                List<MarketingSyncUser> preUserByTask = marketingSyncInfoMapper.getPreUserByInCust(apiCode, set);
                if (CollectionUtils.isEmpty(preUserByTask)) {
                    String msg = String.format("海尔消金案件数据不存在！infoID:{%s};apiCode:{%s};requestId:{%s};tcid:{%s};CustNum:{%s}"
                            , id, apiCode, requestId, tcId, Arrays.toString(set.toArray()));
                    log.warn(msg);
                    page++;
                    continue;
                }
                Map<String, MarketingSyncUser> map = preUserByTask.stream().collect(Collectors.toMap(
                        MarketingSyncUser::getCustNum, syncUser -> syncUser
                        , (v1, v2) -> StringUtils.isNotBlank(v2.getCusBatch())
                                && StringUtils.isNotBlank(v2.getReserveField1())
                                && !ObjectUtils.isEmpty(v2.getCreateTime())
                                && v2.getCreateTime().before(v1.getCreateTime())
                                ? v2 : v1));
                transferList.forEach(l -> {
                    HaierData haierData = new HaierData();
                    haierData.setLocalId(l.getId());
                    haierData.setApiCode(apiCode);
                    final String custNum = l.getCustNum();
                    haierData.setCustNum(custNum);
                    final MarketingSyncUser orDefault = map.getOrDefault(custNum, new MarketingSyncUser());
                    haierData.setTaskId(orDefault.getCusBatch());
                    haierData.setExtend(orDefault.getReserveField1());
                    final JSONObject object = JSONObject.parseObject(orDefault.getReserveField1());
                    if (object.containsKey("type")) {
                        haierData.setType(object.get("type").toString());
                    } else {
                        log.warn("海尔消金案件信息中custNum:{} 扩展字段没有type信息！", custNum);
                    }
                    haierData.setType("1");
                    haierData.setSourceType(2);
                    haierData.setPushStatus(1);
                    haierData.setStatus(1);
                    haierData.setCreateDate(Integer.valueOf(LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE)));
                    haierData.setCreateTime(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
                    haierData.setBatchNo(haierData.getCreateDate() + haierData.getType());
                    haierDataSet.add(haierData);
                });
                haierDataMapper.insert1000Batch(haierDataSet);
                haierDataSet.clear();
                //            final ConcurrentMap<String, List<MarketingSyncUser>> typeMap = stream.collect(
                //                    Collectors.groupingByConcurrent(MarketingSyncUser::getReserveField1));
                //            final Map<String, MarketingTransferSyncUser> transferSyncUserMap = transferList.stream().collect(
                //                    Collectors.toMap(MarketingTransferSyncUser::getCustNum, syncUser -> syncUser
                //                    , (v1, v2) -> !ObjectUtils.isEmpty(v2.getCreateTime()) && v2.getCreateTime().before(v1.getCreateTime())
                //                            ? v2 : v1));
                //            for (Map.Entry<String, List<MarketingSyncUser>> entry : typeMap.entrySet()) {
                //                final List<MarketingSyncUser> list1 = entry.getValue();
                //                final String requestid =  System.currentTimeMillis() + String.format("%04d", random.nextInt(bound));
                //                PushDTO.FormData formData = new PushDTO.FormData(requestid, entry.getKey(), list1, li -> {
                //                    Set<PushDTO.DataItems> dataItemsSet = new HashSet<>();
                //                    li.forEach(l -> {
                //                        dataItemsSet.add(new PushDTO.DataItems(l.getCusBatch(), l.getCustNum()));
                //                        final String custNum = l.getCustNum();
                //                        dataItemsSet.add(new PushDTO.DataItems(map.getOrDefault(custNum
                //                                , new MarketingSyncUser()).getCusBatch(), custNum));
                //                    });
                //                    return dataItemsSet;
                //                });
                //                try {
                //                    final Result<Response2Entity> result1 = haierServiceClient.pushToTeleSales(formData);
                //                } catch (Exception e) {
                //                    log.error(e.getMessage(), e);
                //                }
                //            }
                PageInfo<MarketingTransferSyncUser> pageInfo = new PageInfo<>(transferList);
                if (page == pageInfo.getPages() || transferList.size() == 0) {
                    break;
                }
                page++;
            }
            result.setDate(false);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.setMessage(e.getMessage());
        }
        return result;
    }
}
