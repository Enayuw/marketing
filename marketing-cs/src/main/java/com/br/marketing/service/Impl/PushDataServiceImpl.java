package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.dassservice.DassServiceClient;
import com.br.marketing.client.dassservice.input.DassImportAdapDTO;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.client.haier.HaierServiceClient;
import com.br.marketing.client.haier.input.HaierReqDTO;
import com.br.marketing.client.haier.output.PushDTO;
import com.br.marketing.client.haier.output.Response2Entity;
import com.br.marketing.client.haier.output.ResponseInfoEntity;
import com.br.marketing.client.marketingapi.MarketingApiService;
import com.br.marketing.client.marketingapi.input.PushTransferDataDTO;
import com.br.marketing.client.marketingapi.input.PushTransferDataDetailDTO;
import com.br.marketing.client.twosevenservice.TwoSevenService;
import com.br.marketing.client.twosevenservice.intput.RequestSevenDTO;
import com.br.marketing.client.twosevenservice.output.ResponseSevenZDTO;
import com.br.marketing.client.twosevenservice.output.SevenDetailVO;
import com.br.marketing.client.xiecheng.XieChengService;
import com.br.marketing.client.yiqianbao.YiQianBaoService;
import com.br.marketing.client.yiqianbao.input.YqbDetailVo;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.SftpFileTypeEnum;
import com.br.marketing.common.utils.*;
import com.br.marketing.dto.PushShDXDTO;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.service.PushDataService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
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

    @Value("${api.dass.aesKey:00}")
    private String aesKey;

    @Resource
    PhoneSaleMapper phoneSaleMapper;

    @Resource
    TwosevenFileMapper twosevenFileMapper;

    @Autowired
    DassServiceClient dassServiceClient;

    @Resource
    RetryMainLogMapper retryMainLogMapper;

    @Autowired
    RedisChgService redisChgService;

    @Resource
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
    private XieChengDataMapper xieChengDataMapper;

    @Resource
    private AlarmApiClient alarmClient;
    @Value("${otherConfig.alarm.outsideSecretKey:00}")
    private String secretKey;
    @Value("${otherConfig.alarm.outsideAppName:00}")
    private String appName;

    @Value("${otherConfig.alarm.secretKey:00}")
    private String secret2Key;
    @Value("${otherConfig.alarm.appName:00}")
    private String app2Name;

    @Autowired
    TwoSevenService twoSevenService;

    @Autowired
    MarketingApiService marketingApiService;

    @Resource
    HaierDataMapper haierDataMapper;

    @Resource
    HaierReqMapper haierReqMapper;

    @Autowired
    HaierServiceClient haierServiceClient;

    @Resource
    PhoneSaleExtendShuheMapper phoneSaleExtendShuheMapper;

    @Autowired
    RabbitMqProducter producter;

    @Autowired
    YiqianbaoDataMapper yiqianbaoDataMapper;

    @Autowired
    YiQianBaoService yiQianBaoService;

    @Autowired
    XieChengService xieChengService;


    final static DateTimeFormatter yyyyMMddDF = DateTimeFormatter.ofPattern("yyyyMMdd");

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

        localFile.setPushStartTime(localFile.getPushStartTime()==null?new Date():localFile.getPushStartTime());
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadNum, threadNum);
        Integer number = 0;
        while (actionMark) {
            List<DassImportDataDTO> phoneSales = phoneSaleMapper.getPushDassData(id, minId);
            number += phoneSales.size();
            if (phoneSales.size() > 0) {
                DassImportDataDTO phoneSale = phoneSales.get(phoneSales.size() - 1);
                DassImportAdapDTO dto = new DassImportAdapDTO();
                dto.setInterfaceExtendInfo(id.toString());
                List<DassImportDataDTO> collect = phoneSales.stream().map(t -> (DassImportDataDTO) t).collect(Collectors.toList());
                dto.setList(collect);
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

        localFile.setPushEndTime(new Date());
        localFile.setPushNumber(localFile.getPushNumber()+number);
        localFileMapper.updateByPrimaryKeySelective(localFile);
        if (SftpFileTypeEnum.DX.getValue().equals(localFile.getFileType())) {
            StringBuilder content = new StringBuilder();
            content.append("apiCode：".concat(localFile.getApiCode()).concat("\r\n"))
                    .append("fileName：".concat(localFile.getFileName()).concat("\r\n"))
                    .append("数量：".concat(number.toString()).concat("\r\n"))
                    .append("文件推送dass结束".concat("\r\n"));
            alarmClient.sendAlarm(content.toString(), "Dass结果文件推送", appName, secretKey,
                    Constants.sendCodeMap.get("uploadSuccess"));
        }
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
        localFile.setPushStartTime(localFile.getPushStartTime()==null?new Date():localFile.getPushStartTime());
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
        localFile.setPushNumber(localFile.getPushNumber()+number);
        localFile.setPushEndTime(new Date());
        localFileMapper.updateByPrimaryKeySelective(localFile);
        /** 调用撞库接口有网络失败的 需要重试 */
        if (errorMark.get() > 0) {
            return new Result().setCode(ResultCode.FAIL.getValue());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }


    @Override
    public Result pushHaierData() {
        Integer day = Integer.valueOf(LocalDate.now().format(yyyyMMddDF));
        Boolean mark = Boolean.TRUE;
        Long minId = null;
        LocalFile localFile = new LocalFile();
        localFile.setPushStartTime(new Date());
        List<Long> countIds = new ArrayList<>();
        while (mark) {
            List<HaierData> haierData = haierDataMapper.selectDataLimitId(day, minId);
            if (haierData.size() == 0) {
                mark = Boolean.FALSE;
                continue;
            }
            String apiCode = haierData.get(0).getApiCode();
             localFile = localFileMapper.selectByPrimaryKey(haierData.get(0).getLocalId());
            minId = haierData.get(haierData.size() - 1).getId() + 1;
            Map<String, List<HaierData>> types = haierData.stream().collect(Collectors.groupingBy(HaierData::getType));

            for (String s : types.keySet()) {
                String type = s;
                List<HaierData> haierList = types.get(s);
                List<List<HaierData>> partition = Lists.partition(haierList, 500);
                for (List<HaierData> items : partition) {
                    Set<PushDTO.DataItems> datas = new HashSet<>();
                    //没有去重逻辑了 v2.0->3.0不需要去重了
//                    ArrayList<HaierData> nolist = new ArrayList<>();
//                    ArrayList<HaierData> yeslist = new ArrayList<>();
//                    getDistinctData(items, yeslist, nolist, type, day.toString());
//                    updateHaierFalse(nolist);
                    List<Long> ids = new ArrayList<>();
                    Set<String> custNumsByNeed = new HashSet<>();
                    List<HaierData> haierByNeed = new ArrayList<>();
                    for (HaierData item : items) {
                        if (StringUtils.isNotBlank(item.getTaskId())) {
                            datas.add(new PushDTO.DataItems(item.getTaskId(), item.getCustNum()));
                            ids.add(item.getId());
                        } else {
                            custNumsByNeed.add(item.getCustNum());
                            haierByNeed.add(item);
                        }
                    }
                    if (custNumsByNeed.size() > 0) {
                        List<MarketingSyncUser> preUserByTask = marketingSyncInfoMapper.getPreUserByInCust(apiCode, custNumsByNeed);
                        Map<String, MarketingSyncUser> custMaps = preUserByTask.stream().collect(Collectors.groupingBy(MarketingSyncUser::getCustNum
                                , Collectors.collectingAndThen(
                                        Collectors.reducing((v1, v2) -> v1.getCreateTime().compareTo(v2.getCreateTime()) > 0 ? v1 : v2)
                                        , Optional::get)));
                        for (HaierData data : haierByNeed) {
                            if (custMaps.containsKey(data.getCustNum())) {
                                datas.add(new PushDTO.DataItems(custMaps.get(data.getCustNum()).getCusBatch(), data.getCustNum()));
                                ids.add(data.getId());
                            }
                        }
                    }
                    PushDTO.FormData formData = new PushDTO.FormData();
                    formData.setDataItems(datas);
                    formData.setBatchNo(day.toString().concat("_").concat(type));
                    formData.setType(type);
                    formData.setRequestId(getHaierRequestId(type));

                    HaierReqDTO haierReqDTO = new HaierReqDTO();
                    haierReqDTO.setIds(ids);
                    haierReqDTO.setFormData(formData);
                    if (datas.size() > 0) {
                        try {
                            Result<Response2Entity> response2EntityResult = haierServiceClient.pushToTeleSalesWithIds(haierReqDTO, 0);
                            if(response2EntityResult.getCode()==1){
                                countIds.addAll(ids);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }


        localFile.setPushEndTime(new Date());
        localFile.setPushNumber(localFile.getPushNumber()+countIds.size());
        localFileMapper.updateByPrimaryKeySelective(localFile);
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public Result queryHaierData() {
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(20, 20);
        Long minId = null;
        Boolean isAction = Boolean.TRUE;
        while (isAction) {
            List<HaierReq> dataWithStatus = haierReqMapper.getDataWithStatus(minId);
            if (dataWithStatus.size() <= 0) {
                isAction = Boolean.FALSE;
                continue;
            }
            minId = dataWithStatus.get(dataWithStatus.size() - 1).getId() + 1;

            for (HaierReq reqData : dataWithStatus) {
                threadPool.submit(() -> {
                    try {
                        Result<ResponseInfoEntity> responseInfoEntityResult = haierServiceClient.resultQueryPushToTeleSales(reqData.getReqId());
                        if (ResultCode.SUCCESS.getValue().equals(responseInfoEntityResult.getCode())) {
                            ResponseInfoEntity data = responseInfoEntityResult.getData();
                            if (data != null && data.getHead() != null && "00000".equals(data.getHead().getRetFlag())
                                    && data.getBody() != null && StringUtils.isNotBlank(data.getBody().getSts())) {
                                HaierReq record = new HaierReq();
                                record.setId(reqData.getId());
                                record.setStatus(data.getBody().getSts());
                                haierReqMapper.updateByPrimaryKeySelective(record);
                                if ("fail".equals(data.getBody().getSts())) {
                                    alarmClient.sendAlarm(String.format("海尔查询结果 reqId:%s 推送失败", reqData.getReqId())
                                            , "海尔推送结果查询"
                                            , app2Name, secret2Key, Constants.sendCodeMap.get("pushToHaier"));
                                }
                            }
                        }
                    } catch (Exception ex) {
                        log.error(ex.getMessage(), ex);
                    }
                });
            }
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
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    void getDistinctData(List<HaierData> list, List<HaierData> yeslist, List<HaierData> nolist, String type, String day) {
        Integer start = Integer.valueOf(LocalDate.parse(day, yyyyMMddDF).minusDays(29L).format(yyyyMMddDF));
        Integer end = Integer.valueOf(day);
        List<String> custNums = list.stream().map(t -> t.getCustNum()).collect(Collectors.toList());
        HaierDataExample example = new HaierDataExample();
        example.createCriteria()
                .andCustNumIn(custNums)
                .andTypeEqualTo(type)
                .andPushStatusEqualTo(2)
                .andCreateDateGreaterThanOrEqualTo(start)
                .andCreateDateLessThanOrEqualTo(end);
        List<HaierData> repeatData = haierDataMapper.selectByExample(example);
        Set<String> custs = repeatData.stream().map(t -> t.getCustNum()).collect(Collectors.toSet());
        Set<String> custNumNow = new HashSet<>();
        for (HaierData haierData : list) {
            if (custs.contains(haierData.getCustNum())) {
                nolist.add(haierData);
                continue;
            }
            if (custNumNow.contains(haierData.getCustNum())) {
                nolist.add(haierData);
                continue;
            }
            custNumNow.add(haierData.getCustNum());
            yeslist.add(haierData);
        }

    }

    void updateHaierFalse(List<HaierData> list) {
        if (list.size() > 0) {
            List<Long> ids = list.stream().map(t -> t.getId()).collect(Collectors.toList());
            HaierDataExample updateExample = new HaierDataExample();
            updateExample.createCriteria().andIdIn(ids);
            HaierData record = new HaierData();
            record.setPushStatus(3);
            haierDataMapper.updateByExampleSelective(record, updateExample);
        }
    }


    @Override
    public String getHaierRequestId(String type) {
        String yyyyMMddHHmmss = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String s = RandomUtils.randomStr(4);
        return yyyyMMddHHmmss.concat("_").concat(type).concat(s);
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
            String msg = String.format("海尔消金客户转化数据主键为[%s]的基础信息不存在,该信息直接消费,不再重放队列", id);
            log.error(msg);
            result.setMessage(msg);
            sendAlarm(msg);
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
            // 缓存一周
            redisChgService.setex(key, tcId, 7 * 24 * 3600);
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
                Page<MarketingTransferSyncUser> pageInfo = PageHelper.startPage(page, pageSize, true).setOrderBy(" id ASC");
                List<MarketingTransferSyncUser> transferList = marketingTransferSyncUserMapper.selectByExample(example);
                if (CollectionUtils.isEmpty(transferList) && page <= pageInfo.getPages()) {
                    String msg = String.format("海尔消金转化详情数据不存在！infoID:{%s};apiCode:{%s};requestId:{%s};tcid:{%s}" +
                                    "\n该数据将被放弃！"
                            , id, apiCode, requestId, tcId);
                    sendAlarm(msg);
                    break;
                }
                /*
                 *2021/12/28 10:46  推送电销逻辑
                 * usertype   3
                 * auditTime  非空非null（该字段有日期值）
                 * lenttime   null或者该字段为空或无该字段
                 * ifLent     0
                 */
                transferList = transferList.stream().filter(syncUser -> StringUtils.isNotEmpty(syncUser.getAuditTime())
                        && !"null".equalsIgnoreCase(syncUser.getAuditTime())
                        && (StringUtils.isEmpty(syncUser.getLentTime()) || "null".equalsIgnoreCase(syncUser.getLentTime()))
                        && "0".equals(syncUser.getIfLent())).collect(Collectors.toList());
                if (transferList.size() < 1) {
                    if (page < pageInfo.getPages()) {
                        page++;
                        continue;
                    }
                    break;
                }
                Set<String> set = transferList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
                if (CollectionUtils.isEmpty(set)) {
                    String msg = String.format("海尔消金转化数据CustNum不存在！infoID:{%s};apiCode:{%s};requestId:{%s};tcid:{%s}" +
                                    "\n该数据将被放弃！"
                            , id, apiCode, requestId, tcId);
                    sendAlarm(msg);
                    if (page < pageInfo.getPages()) {
                        page++;
                        continue;
                    }
                    break;
                }
                List<MarketingSyncUser> preUserByTask = marketingSyncInfoMapper.getPreUserByInCust(apiCode, set);
                if (CollectionUtils.isEmpty(preUserByTask)) {
                    String msg = String.format("海尔消金基础信息数据不存在！infoID:{%s};apiCode:{%s};requestId:{%s};tcid:{%s}" +
                                    "\n该数据将被放弃！"
                            , id, apiCode, requestId, tcId);
                    sendAlarm(msg);
                    if (page < pageInfo.getPages()) {
                        page++;
                        continue;
                    }
                    break;
                }
                Map<String, MarketingSyncUser> map = preUserByTask.stream().collect(Collectors.toMap(
                        MarketingSyncUser::getCustNum, syncUser -> syncUser
                        , (v1, v2) -> StringUtils.isNotBlank(v2.getCusBatch()) && StringUtils.isNotBlank(
                                v2.getReserveField1()) && !ObjectUtils.isEmpty(v2.getCreateTime())
                                && v2.getCreateTime().after(v1.getCreateTime()) ? v2 : v1));
                for (MarketingTransferSyncUser l : transferList) {
                    HaierData haierData = new HaierData();
                    final String custNum = l.getCustNum();
                    if (map.containsKey(custNum)) {
                        final MarketingSyncUser orDefault = map.get(custNum);
                        final String reserveField1 = orDefault.getReserveField1();
                        if (StringUtils.isEmpty(reserveField1) || !reserveField1.contains("type")) {
                            String msg = String.format("海尔消金客户[%s]转化数据custNum为[%s];主键[%s];tcId为[%s]匹配到基础信息," +
                                            "扩展字段不符合要求,reserveField1:[%s];\n该数据将被放弃！"
                                    , apiCode, custNum, l.getId(), tcId, reserveField1);
                            sendAlarm(msg);
                            continue;
                        }
                        final JSONObject object = JSONObject.parseObject(reserveField1);
                        if (object.containsKey("type")) {
                            haierData.setType(object.get("type").toString());
                        } else {
                            String msg = String.format("海尔消金客户[%s]转化数据custNum为[%s];主键[%s];tcId为[%s]匹配到基础信息," +
                                            "扩展字段中不存在“type”,reserveField1:[%s];\n该数据将被放弃！"
                                    , apiCode, custNum, l.getId(), tcId, reserveField1);
                            sendAlarm(msg);
                            continue;
                        }
                        haierData.setTaskId(orDefault.getCusBatch());
                        haierData.setExtend(orDefault.getReserveField1());
                    } else {
                        String msg = String.format("海尔消金客户[%s]转化数据custNum为[%s];主键[%s];tcId为[%s]未匹配到基础信息;" +
                                        "\n该数据将被放弃！"
                                , apiCode, custNum, l.getId(), tcId);
                        sendAlarm(msg);
                        continue;
                    }
                    haierData.setSourceId(l.getId());
                    haierData.setApiCode(apiCode);
                    haierData.setCustNum(custNum);
                    haierData.setSourceType(2);
                    haierData.setPushStatus(1);
                    haierData.setStatus(1);
                    haierData.setCreateDate(Integer.valueOf(LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE)));
                    haierData.setCreateTime(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
                    haierData.setBatchNo(haierData.getCreateDate() + haierData.getType());
                    haierDataSet.add(haierData);
                }
                haierDataMapper.insert1000Batch(haierDataSet);
                haierDataSet.clear();
                if (page >= pageInfo.getPages()) {
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

    private void sendAlarm(String msg) {
        log.warn(msg);
        alarmClient.sendAlarm(msg, "海尔消金转电销(转化数据)警告", app2Name, secret2Key,
                Constants.sendCodeMap.get("pushToHaier"));
    }

    /**
     * 数禾推送电销
     *
     * @param pushShDXDTO
     * @return 传输数据样例
     * LocalFile localFile = new LocalFile();
     * PhoneSale phoneSale = new PhoneSale();
     * PhoneSaleExtendShuhe phoneSaleExtendShuhe = new PhoneSaleExtendShuhe();
     * PushShDXDTO pushShDXDTO = new PushShDXDTO()
     * .setLocalFile(localFile)
     * .setPhoneSale(phoneSale)
     * .setPhoneSaleExtendShuhe(phoneSaleExtendShuhe);
     * localFile.setCid("");
     * localFile.setApiCode("");
     * localFile.setFileName("数禾-转化/客服+数据id");
     * phoneSale.setUid("custNum");
     * phoneSale.setPhone("手机号明文");
     * phoneSale.setName("");
     * phoneSale.setOrgname("shuheshenwan");
     * phoneSale.setSource("16");
     * phoneSale.setUserType("2");
     * phoneSale.setLoginTime("");
     * phoneSale.setExtend("{\"clc_usr_iso_pho_tim\":\"\",\"clc_usr_iso_idt_tim\":\"\",\"clc_usr_iso_crd_tim\":\"\",\"clc_usr_iso_inf_tim\":\"\"}");
     * phoneSaleExtendShuhe.setCustNum("custNum");
     * phoneSaleExtendShuhe.setAppletDate("当前日期yyyy-MM-dd");
     * phoneSaleExtendShuhe.setAppletTime("当前时间yyyy-MM-dd HH:mm:ss");
     * phoneSaleExtendShuhe.setStatus("a/b");
     */
    @Override
    public Result<Boolean> pushShDX(PushShDXDTO pushShDXDTO) {

        Date date = new Date();
        LocalFile localFile = pushShDXDTO.getLocalFile();
        localFile.setFileType(SftpFileTypeEnum.SHBYTRANSFORM.getValue());
        localFile.setCreateTime(date);
        String apiCode = localFile.getApiCode();
        String phone = "";
        PhoneSale phoneSale = pushShDXDTO.getPhoneSale();
        phone = phoneSale.getPhone();
        phoneSale.setCreateTime(date);
        String s = AESUtil.aesEncrypty(phoneSale.getPhone(), aesKey);
        phoneSale.setPhone(s);
        phoneSale.setPhoneAes(BrCipherMaker.getInstance().encode(phone));
        phoneSale.setApiCode(apiCode);
        phoneSale.setApiCid(localFile.getCid());
        JSONObject jo = new JSONObject();
        jo.put("face_recognitiion", "0");
        jo.put("is_usr_idt", "0");
        jo.put("is_bindcard", "0");
        jo.put("is_usr_inf", "0");
        if (StringUtils.isNotBlank(phoneSale.getExtend())) {
            JSONObject jsonObject = JSON.parseObject(phoneSale.getExtend());
            String pho = jsonObject.getString("clc_usr_iso_pho_tim");
            String idt = jsonObject.getString("clc_usr_iso_idt_tim");
            String crd = jsonObject.getString("clc_usr_iso_crd_tim");
            String inf = jsonObject.getString("clc_usr_iso_inf_tim");
            if (StringUtils.isNotBlank(pho)) {
                jo.put("face_recognitiion", "1");
            }
            if (StringUtils.isNotBlank(idt)) {
                jo.put("is_usr_idt", "1");
            }
            if (StringUtils.isNotBlank(crd)) {
                jo.put("is_bindcard", "1");
            }
            if (StringUtils.isNotBlank(inf)) {
                jo.put("is_usr_inf", "1");
            }
        }
        phoneSale.setExtend(JSON.toJSONString(jo));
        PhoneSaleExtendShuhe phoneSaleExtendShuhe = pushShDXDTO.getPhoneSaleExtendShuhe();
        phoneSaleExtendShuhe.setCreateTime(date);

        PhoneSaleExtendShuheExample shuheExample = new PhoneSaleExtendShuheExample();
        shuheExample.createCriteria().andCustNumEqualTo(phoneSaleExtendShuhe.getCustNum()).andAppletDateEqualTo(phoneSaleExtendShuhe.getAppletDate());
        List<PhoneSaleExtendShuhe> phoneSaleExtendShuhes = phoneSaleExtendShuheMapper.selectByExample(shuheExample);
        if (phoneSaleExtendShuhes.size() > 0) {
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
        }

        Result result = addShuHeLock(apiCode, phoneSaleExtendShuhe.getCustNum(), phoneSaleExtendShuhe.getStatus());
        if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
        }

        localFileMapper.insertSelective(localFile);
        phoneSale.setLocalId(localFile.getId().toString());
        phoneSaleMapper.insertSelective(phoneSale);
        phoneSaleExtendShuhe.setLocalId(localFile.getId());
        phoneSaleExtendShuhe.setpId(phoneSale.getId());
        phoneSaleExtendShuheMapper.insertSelective(phoneSaleExtendShuhe);
        producter.send(MQConstants.ROUTING_KEY_MARKETING_PUSH_DASS_SCORE, localFile.getId().toString());

        removeHaluoLock(apiCode, phoneSaleExtendShuhe.getCustNum(), phoneSaleExtendShuhe.getStatus());
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.TRUE);
    }

    @Override
    public Boolean pushShDXSingleMutex(String apiCode, String custNum, String status, String userType) {
        //a/b状态一天只能推一条,apicode+casenum+usertype下
        String key = RedisKeyConstant.shuhePushDxSingleMutex.concat(":")
                .concat(apiCode).concat(":")
                .concat(custNum).concat(":")
                .concat(userType);
        if (redisChgService.exists(key)) {
            return false;
        }
        Integer seconds = DateHelper.getRemainSecondsOneDay(new Date());
        redisChgService.setex(key, status, seconds);
        return true;
    }

    @Override
    public Result pushSftpToDbData(Long id) {
        LocalFile localFile = localFileMapper.selectByPrimaryKey(id);
        if (localFile == null) {
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("文件不存在");
        }
        //壹钱包推送营销数据
        if ("yiqianbao".equals(localFile.getFileType())) {
            localFile.setPushStartTime(new Date());
            Boolean actionMark = true;
            Long minId = null;
            Integer pushCount = 0;
            while (actionMark) {
                List<YiqianbaoData> dataList = yiqianbaoDataMapper.getPushData(id, minId);
                pushCount = pushCount +dataList.size();
                if (dataList.size() <= 0) {
                    actionMark = false;
                    continue;
                }
                minId = dataList.get(dataList.size() - 1).getId();
                List<List<YiqianbaoData>> dataPartList = Lists.partition(dataList, 50);
                dataPartList.forEach(pushList -> {
                    YqbDetailVo yqbDetailVo = getRequestTransfer(pushList);
                    yiQianBaoService.pushMarketingData(yqbDetailVo);
                    updatePushStatus(pushList);
                });
            }

            localFile.setPushEndTime(new Date());
            localFile.setPushNumber(localFile.getPushNumber()+pushCount);
            localFileMapper.updateByPrimaryKeySelective(localFile);
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
    }

    @Override
    public Result pushXieChengToDbData(Long id) {
        LocalFile localFile = localFileMapper.selectByPrimaryKey(id);
        if (localFile == null) {
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("文件不存在");
        }
        //携程推送营销数据
        if ("xiecheng".equals(localFile.getFileType())) {
            localFile.setPushStartTime(localFile.getPushStartTime()==null?new Date():localFile.getPushStartTime());
            Boolean actionMark = true;
            Integer pushCount = 0;
            while (actionMark) {

                List<XieChengData> xieChengDatalist = xieChengDataMapper.selectByLocalId(id);
                pushCount = pushCount+xieChengDatalist.size();
                if (xieChengDatalist.size() <= 0) {
                    actionMark = false;
                    continue;
                }
                for (int i = 0; i < xieChengDatalist.size(); i++) {
                    try {
                        Thread.sleep(500L);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    XieChengData xieChengData = xieChengDatalist.get(i);
                    String result = xieChengService.pushXieChengData(xieChengData);
                    JSONObject resultJson = JSONObject.parseObject(result);
                    Integer code = resultJson.getInteger("code");
                    XieChengData resultData = new XieChengData();
                    resultData.setId(xieChengData.getId());
                    if (code == 0) {
                        resultData.setPushStatus(2);
                    }else {
                        resultData.setPushStatus(3);
                    }
                    resultData.setDataMessage(result);
                    xieChengDataMapper.updateByPrimaryKeySelective(resultData);
                }
            }
            localFile.setPushEndTime(new Date());
            localFile.setPushNumber(localFile.getPushNumber()+pushCount);
            localFileMapper.updateByPrimaryKeySelective(localFile);
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
    }

    private YqbDetailVo getRequestTransfer(List<YiqianbaoData> pushList) {
        YqbDetailVo yqbDetailVo = new YqbDetailVo();
        List<YqbDetailVo.UserInfo> userInfoList = new ArrayList<>();
        pushList.forEach(pushMarketingData -> {
            YqbDetailVo.UserInfo userInfo = new YqbDetailVo.UserInfo();
            userInfo.setPhoneMd5(pushMarketingData.getPhoneMd5());
            userInfo.setDataTime(DateUtils.format(pushMarketingData.getCreateTime(), "yyyyMMddHHmmss"));
            userInfo.setOuterApplyNo(pushMarketingData.getId().toString());
            userInfo.setMarketFlag(pushMarketingData.getMarketFlag());
            userInfoList.add(userInfo);
        });
        yqbDetailVo.setUserInfoList(userInfoList);
        return yqbDetailVo;
    }

    void updatePushStatus(List<YiqianbaoData> list) {
        if (list.size() > 0) {
            List<Long> ids = list.stream().map(t -> t.getId()).collect(Collectors.toList());
            YiqianbaoDataExample updateExample = new YiqianbaoDataExample();
            updateExample.createCriteria().andIdIn(ids);
            YiqianbaoData record = new YiqianbaoData();
            record.setPushStatus(2);
            yiqianbaoDataMapper.updateByExampleSelective(record, updateExample);
        }
    }

    private Result addShuHeLock(String apiCode, String custNum, String status) {
        String key = RedisKeyConstant.shuhePushDx.concat(":")
                .concat(apiCode).concat(":")
                .concat(custNum);
        Long setnx = redisChgService.setnx(key, status, 3);
        //已经被其他数据抢占锁了
        if (setnx.equals(0L)) {
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue());
    }

    private void removeHaluoLock(String apiCode, String custNum, String status) {
        String key = RedisKeyConstant.shuhePushDx.concat(":")
                .concat(apiCode).concat(":")
                .concat(custNum);
        String s = redisChgService.get(key);
        if (status.equals(s)) {
            redisChgService.del(key);
        }
    }
}
