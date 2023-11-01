package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.encryption.Sha256Util;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.dassservice.DassServiceClient;
import com.br.marketing.client.dassservice.input.DassImportAdapDTO;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.client.dassservice.input.IbuReqDTO;
import com.br.marketing.client.dassservice.input.transfer.DassTransferDataAdapDTO;
import com.br.marketing.client.dassservice.input.transfer.DassTransferDataDTO;
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
import com.br.marketing.client.xiecheng.SmsQuitReq;
import com.br.marketing.client.xiecheng.XieChengService;
import com.br.marketing.client.xiecheng.intput.AdReqDTO;
import com.br.marketing.client.yiqianbao.YiQianBaoService;
import com.br.marketing.client.yiqianbao.input.YqbDetailVo;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.SftpFileTypeEnum;
import com.br.marketing.common.utils.*;
import com.br.marketing.dto.PushShDXDTO;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.rpcclient.rpcclientImpl.DecodeClient;
import com.br.marketing.service.PushDataService;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.service.ValidityPeriodDataService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.br.marketing.common.utils.MQConstants.ROUTING_KEY_XIECHENG_SMSCOLLIDINGVT_CUSTOMER;

@Slf4j
@Service
public class PushDataServiceImpl implements PushDataService {

    @Value("${api.dass.aesKey:00}")
    private String aesKey;

    @Resource
    PhoneSaleMapper phoneSaleMapper;

    @Resource
    PhoneSaleTransferMapper phoneSaleTransferMapper;

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
    private XiechengSmsQuitDataMapper xiechengSmsQuitDataMapper;

    @Resource
    private XieChengSmsCollidingDataMapper xieChengSmsCollidingDataMapper;

    @Resource
    private XieChengSmsCollidingDataLogMapper xieChengSmsCollidingDataLogMapper;


    @Resource
    private XieChengSmsCollidingDataVtMapper xieChengSmsCollidingDataVtMapper;

    @Resource
    private XieChengSmsCollidingDataLogVtMapper xieChengSmsCollidingDataLogVtMapper;

    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;
    @Resource
    @Qualifier("xieChengThreadPool")
    ThreadPoolExecutor xieChengThreadPool;


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

    @Resource
    PhoneSaleIbuMapper phoneSaleIbuMapper;

    @Autowired
    RabbitMqProducter producter;

    @Autowired
    YiqianbaoDataMapper yiqianbaoDataMapper;

    @Autowired
    YiQianBaoService yiQianBaoService;

    @Autowired
    XieChengService xieChengService;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    MethodRetryHandlerService methodRetryHandlerService;

    @Resource
    private ValidityPeriodDataService validityPeriodDataService;

    final static DateTimeFormatter yyyyMMddDF = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final static int XIECHENGSMSCOLLIDINGPARTATIONNUM = 50;

    private final static String XIECHENGSMSCOLLIDINGFORMATTER = "yyyy-MM-dd HH:mm:ss";


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

        localFile.setPushStartTime(new Date());
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
        localFile.setPushNumber(number);
        localFileMapper.updateByPrimaryKeySelective(localFile);
        if (SftpFileTypeEnum.DX.getValue().equals(localFile.getFileType())) {
            StringBuilder content = new StringBuilder();
            content.append("apiCode：".concat(localFile.getApiCode()).concat("\r\n"))
                    .append("fileName：".concat(localFile.getFileName()).concat("\r\n"))
                    .append("数量：".concat(number.toString()).concat("\r\n"))
                    .append("文件推送dass结束".concat("\r\n"));
            alarmClient.sendAlarm(content.toString(), "Dass结果文件推送", AlarmSendCodeEnum.SUCCESS_UPLOAD.getCode());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(isContiue);
    }

    @Override
    public Result pushDassTransferData(Long id) {

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

        localFile.setPushStartTime(new Date());
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadNum, threadNum);
        Integer number = 0;
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);
        AtomicInteger retry = new AtomicInteger(0);
        while (actionMark) {
            List<DassTransferDataDTO> transferDataDTOS = phoneSaleTransferMapper.getPushDassTransferData(id, minId);
            number += transferDataDTOS.size();
            if (transferDataDTOS.size() > 0) {
                for (DassTransferDataDTO transferDataDTO : transferDataDTOS) {
                    if (StringUtils.isNotBlank(transferDataDTO.getPhone())) {
                        transferDataDTO.setPhone(AESUtil.decrypt(transferDataDTO.getPhone(), aesKey));
                    }
                }
                DassTransferDataDTO transferDataDTO = transferDataDTOS.get(transferDataDTOS.size() - 1);
                DassTransferDataAdapDTO dto = new DassTransferDataAdapDTO();
                dto.setDassTransferDataDTOList(transferDataDTOS);
                minId = transferDataDTO.getId();
                threadPool.submit(() -> {
                    Result result = methodRetryHandlerService.dassTransferWithFile(dto, null);
                    int size = dto.getDassTransferDataDTOList().size();
                    if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                        success.addAndGet(size);
                    } else if (ResultCode.FAIL.getValue().equals(result.getCode())) {
                        fail.addAndGet(size);
                    } else {
                        retry.addAndGet(size);
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
        localFile.setPushNumber(success.get());
        localFile.setErrorActualNumber(fail.get());
        localFileMapper.updateByPrimaryKeySelective(localFile);
        if (SftpFileTypeEnum.DXTRANSFORM.getValue().equals(localFile.getFileType())) {
            StringBuilder content = new StringBuilder();
            content.append("apiCode：".concat(localFile.getApiCode()).concat("\r\n"))
                    .append("fileName：".concat(localFile.getFileName()).concat("\r\n"))
                    .append("数量：".concat(number.toString()).concat("\r\n"))
                    .append("成功数量：".concat(success.get() + "").concat("\r\n"))
                    .append("失败数量：".concat(fail.get() + "").concat("\r\n"))
                    .append("需重试数量：".concat(retry.get() + "").concat("\r\n"))
                    .append("文件推送dass转化结束".concat("\r\n"));
            alarmClient.sendAlarm(content.toString(), "Dass转化结果文件推送", AlarmSendCodeEnum.SUCCESS_UPLOAD.getCode());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(isContiue);
    }

    @Override
    public Result pushDassTransferIbu(Long id) {

        Boolean isContiue = false;
        Boolean actionMark = true;
        Long minId = null;
        String key = "dass:push:threadnum";
        Integer threadNum = 5;

        LocalFile localFile = localFileMapper.selectByPrimaryKey(id);
        if (localFile == null) {
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("文件不存在").setDate(isContiue);
        }

        localFile.setPushStartTime(new Date());
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadNum, threadNum);
        Integer number = 0;
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);
        AtomicInteger retry = new AtomicInteger(0);
        while (actionMark) {
            List<PhoneSaleIbu> phoneSaleIbus =  phoneSaleIbuMapper.getPushDassTransferData(id, minId);
            number += phoneSaleIbus.size();
            if (phoneSaleIbus.size() > 0) {
                ArrayList<IbuReqDTO.Datum> reqlist = new ArrayList<>();
                for (PhoneSaleIbu ibu : phoneSaleIbus) {
                    IbuReqDTO.Datum dataum = new IbuReqDTO.Datum();
                    BeanUtils.copyProperties(ibu,dataum);
                    dataum.setPlanId(StringUtils.isNotBlank(ibu.getPlanId())?Integer.valueOf(ibu.getPlanId()):null);
                    dataum.setCallAccessScore(StringUtils.isNotBlank(ibu.getCallAccessScore())?Integer.valueOf(ibu.getCallAccessScore()):null);
                    dataum.setPid(StringUtils.isNotBlank(ibu.getPid())?Integer.valueOf(ibu.getPid()):null);
                    dataum.setConnectTimes(StringUtils.isNotBlank(ibu.getConnectTimes())?Integer.valueOf(ibu.getConnectTimes()):null);
                    dataum.setZyTotalUsableAmount(StringUtils.isNotBlank(ibu.getZyTotalUsableAmount())?new BigDecimal(ibu.getZyTotalUsableAmount()):null);
                    if(StringUtils.isNotBlank(ibu.getRecommendH5List())){
                        dataum.setRecommendH5List(Arrays.asList(ibu.getRecommendList()));
                    }
                    if(StringUtils.isNotBlank(ibu.getRecommendList())){
                        dataum.setRecommendList(Arrays.asList(ibu.getRecommendList()));
                    }
                    dataum.setZyApplyFlag(StringUtils.isNotBlank(ibu.getZyApplyFlag())?Boolean.valueOf(ibu.getZyApplyFlag()):null);
                    dataum.setZyApplySuccessFlag(StringUtils.isNotBlank(ibu.getZyApplySuccessFlag())?Boolean.valueOf(ibu.getZyApplySuccessFlag()):null);
                    if (StringUtils.isNotBlank(ibu.getPhone())) {
                        dataum.setPhone(BrCipherMaker.getInstance().decode(ibu.getPhone()));
                    }
                    if (StringUtils.isNotBlank(ibu.getUserName())) {
                        dataum.setUserName(BrCipherMaker.getInstance().decode(ibu.getUserName()));
                    }
                    reqlist.add(dataum);
                }

                PhoneSaleIbu lastIbu = phoneSaleIbus.get(phoneSaleIbus.size() - 1);
//                DassTransferDataAdapDTO dto = new DassTransferDataAdapDTO();
//                dto.setDassTransferDataDTOList(transferDataDTOS);
                minId = lastIbu.getId();
                threadPool.submit(() -> {
                    Result result = methodRetryHandlerService.dassIbuWithFile(reqlist, null);
                    int size = reqlist.size();
                    if(ResultCode.SUCCESS.getValue().equals(result.getCode())){
                        success.addAndGet(size);
                    }else if(ResultCode.FAIL.getValue().equals(result.getCode())){
                        fail.addAndGet(size);
                    }else{
                        retry.addAndGet(size);
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
        localFile.setPushNumber(success.get());
        localFileMapper.updateByPrimaryKeySelective(localFile);
        if (SftpFileTypeEnum.DXIBU.getValue().equals(localFile.getFileType())) {
            StringBuilder content = new StringBuilder();
            content.append("apiCode：".concat(localFile.getApiCode()).concat("\r\n"))
                    .append("fileName：".concat(localFile.getFileName()).concat("\r\n"))
                    .append("数量：".concat(number.toString()).concat("\r\n"))
                    .append("成功数量：".concat(success.get()+"").concat("\r\n"))
                    .append("失败数量：".concat(fail.get()+"").concat("\r\n"))
                    .append("需重试数量：".concat(retry.get()+"").concat("\r\n"))
                    .append("文件推送dass转化结束".concat("\r\n"));
            alarmClient.sendAlarm(content.toString(), "Dass转化结果文件推送", AlarmSendCodeEnum.SUCCESS_UPLOAD.getCode());
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
        localFile.setPushStartTime(new Date());
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
        localFile.setPushNumber(number);

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
            localFile.setId(haierData.get(0).getLocalId());
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
                            if (response2EntityResult.getCode() == 1) {
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
        localFile.setPushNumber(countIds.size());
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
                                    alarmClient.sendAlarm(String.format("海尔查询结果-reqId:%s-推送失败", reqData.getReqId())
                                            , "海尔推送结果查询"
                                            , AlarmSendCodeEnum.EXCEPTION_URGENT.getCode());
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
    public Boolean isPushDassWithCallGrade(String ruleLabel,String intentionGrade) {
        HashMap<String, List<String>> gradeOfcallToDass = marketingCommonConfig.getGradeOfcallToDass();
        List<String> grades = gradeOfcallToDass.get(ruleLabel);
        if(grades == null){
            return false;
        }
        if(org.apache.commons.lang3.StringUtils.isBlank(intentionGrade)){
            return false;
        }
        for (String grade : grades) {
            if(intentionGrade.toUpperCase().contains(grade)){
                return true;
            }
        }
        return false;
    }

    @Override
    public String getStatusByGrade(String ruleLabel, String intentionGrade) {
        HashMap<String, List<String>> gradeOfcallToDass = marketingCommonConfig.getGradeOfcallToDass();
        List<String> grades = gradeOfcallToDass.get(ruleLabel);
        if(grades == null){
            return "";
        }
        if(org.apache.commons.lang3.StringUtils.isBlank(intentionGrade)){
            return "";
        }
        for (String grade : grades) {
            if(intentionGrade.toUpperCase().contains(grade)){
                return grade.toLowerCase();
            }
        }
        return "";
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
        alarmClient.sendAlarm(msg, "海尔消金转电销(转化数据)警告", AlarmSendCodeEnum.EXCEPTION_URGENT.getCode());
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
                pushCount = pushCount + dataList.size();
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
            localFile.setPushNumber(pushCount);
            localFileMapper.updateByPrimaryKeySelective(localFile);
        }
        //携程短信退订推送
        if ("xiechengsms".equals(localFile.getFileType())) {
            pushSmsQuitData(localFile);
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
    }

    /**
     * // TODO: 2022/12/6
     * // 1. 通过循环，根据localId 和当前最小id 查询数据，第一个id 为 null，分页为每页1w条 type =0 为 sftp 上传数据，1 为 api上传数据。
     * // 2. 启动线程池。将每条数据放入到线程池里。
     * // 3. 新建redis锁key  public static final String pushXieCheng = prefix.concat("xieCheng:pushXieCheng");
     * // 4. 判断当前数据是否已推送过，如果推送过 直接剔除 ，sftp 不会计算推送条数。
     * // 5. 执行推送逻辑，根据返回值 进行重试。
     * // 7. 成功后释放锁
     * // 8. 全部推送结束  关闭线程池。
     * // 9. 若 type 为 0 ，则需要统计上传推送数量 和重复数据
     *
     * @param
     * @return
     */
    public void pushSmsQuitData(LocalFile localFile) {
        ThreadPoolExecutor pool = BrExecutors.getThreadPool(5, 5);
        localFile.setPushStartTime(new Date());
        Boolean actionMark = true;
        Long minId = null;
        AtomicInteger failNum = new AtomicInteger(0);
        while (actionMark) {
            if (StringUtils.isNotEmpty(marketingCommonConfig.getXieChengSmsQuitThreadNum())) {
                pool.setCorePoolSize(Integer.valueOf(marketingCommonConfig.getXieChengSmsQuitThreadNum()));
                pool.setMaximumPoolSize(Integer.valueOf(marketingCommonConfig.getXieChengSmsQuitThreadNum()));
                log.warn("携程推送短信退订接口线程调整，corePoolSize={},maxPoolSize={}", pool.getCorePoolSize(), pool.getMaximumPoolSize());
            }
            List<XiechengSmsQuitData> dataList = xiechengSmsQuitDataMapper.getSmsQuitData(localFile.getId(), minId);
            if (dataList.size() <= 0) {
                actionMark = false;
                continue;
            }
            minId = dataList.get(dataList.size() - 1).getId();
            dataList.forEach(pushList -> {
                pool.submit(() -> {
                    SmsQuitReq smsQuitReq = new SmsQuitReq(pushList.getCipherMobile(), pushList.getBlackListType());
                    //兼容Md5手机号
                    String phone = smsQuitReq.getCipherMobile();
                    if (DecodeClient.isMd5(phone)) {
                        smsQuitReq.setCipherMobile(Sha256Util.getSHA256Encrypt(RpcClientProxy.decode(phone, "cell", "md5", "")));
                    }
                    Result result = xieChengService.sendSmsQuitData(smsQuitReq);
                    XiechengSmsQuitData xiechengSmsQuitData = new XiechengSmsQuitData();
                    xiechengSmsQuitData.setId(pushList.getId());
                    if (result.getCode().equals(ResultCode.SUCCESS.getValue())) {
                        xiechengSmsQuitData.setPushStatus(2);
                    } else {
                        xiechengSmsQuitData.setPushStatus(3);
                        failNum.getAndIncrement();
                    }
                    xiechengSmsQuitDataMapper.updateByPrimaryKeySelective(xiechengSmsQuitData);
                });
            });
        }
        pool.shutdown();
        try {
            while (!pool.awaitTermination(10L, TimeUnit.SECONDS)) {
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        localFile.setPushEndTime(new Date());
        XiechengSmsQuitDataExample xiechengSmsQuitDataExample = new XiechengSmsQuitDataExample();
        xiechengSmsQuitDataExample.createCriteria().andLocalIdEqualTo(localFile.getId())
                .andPushStatusEqualTo(2)
                .andStatusEqualTo(1);
        Long i = xiechengSmsQuitDataMapper.countByExample(xiechengSmsQuitDataExample);
        localFile.setPushNumber(i.intValue());
        localFileMapper.updateByPrimaryKeySelective(localFile);
        xieChengSendAlarm(failNum, "携程短信退订接口推送异常，请检查");
    }


    @Override
    public Result pushXieChengToDbData(String data) {
        try {
            LocalFile localFile = new LocalFile();
            Long id;
            int xieChengCount = 1;
            if (isJson(data)) {
                JSONObject jsonObject = JSONObject.parseObject(data);
                id = Long.valueOf(jsonObject.getInteger("localId"));
            } else {
                id = Long.valueOf(data);
                localFile = localFileMapper.selectByPrimaryKey(id);
                if (localFile != null) {
                    xieChengCount = localFile.getActualNumber();
                    localFile.setPushStartTime(localFile.getPushStartTime() == null ? new Date() : localFile.getPushStartTime());
                } else {
                    return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("文件不存在").setDate(false);
                }
            }
            Boolean actionMark = true;
            Long minId = null;
            AtomicInteger failNum = new AtomicInteger(0);
            CountDownLatch countDownLatch = new CountDownLatch(xieChengCount);
            while (actionMark) {
                List<XieChengData> xieChengDatalist = xieChengDataMapper.selectByLocalId(id, minId);
                if (xieChengDatalist.size() == 0) {
                    actionMark = false;
                    continue;
                }
                for (int i = 0; i < xieChengDatalist.size(); i++) {
                    XieChengData xieChengData = xieChengDatalist.get(i);
                    minId = xieChengData.getId();
                    xieChengThreadPool.submit(() -> pushXieChengData(xieChengData, failNum, countDownLatch));
                }
            }
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                log.error("countDownLatch 线程执行异常", e);
            }
            if (!isJson(data)) {
                updateLocalFile(localFile);
            }
            xieChengSendAlarm(failNum, "携程广告上报接口推送异常，请检查");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
    }

    @Override
    public void  pushXieChengSmsCollidingToDbData(String data) {
        try {
            JSONObject jsonObject = JSONObject.parseObject(data);
            Long localId = Long.valueOf(jsonObject.getInteger("localId"));
            Boolean isNewFile = jsonObject.getBooleanValue("isNewFile");
            // 创建线程池
            ThreadPoolExecutor xieChengSmsCollidingThread =
                    BrExecutors.getThreadPool(marketingCommonConfig.getXieChengSmsCollidingThread(), marketingCommonConfig.getXieChengSmsCollidingThread());

            boolean actionMark = true;
            // 根据id匹配 进行数据查询 每批次查询 20000
            Long minId = null;
            AtomicInteger failNum = new AtomicInteger(0);
            while (actionMark) {
                List<XieChengSmsCollidingData> xieChengSmsCollidingDataList =
                        xieChengSmsCollidingDataMapper.selectByLocalId(localId, minId, getEndTime(isNewFile));
                if (xieChengSmsCollidingDataList.size() == 0) {
                    actionMark = false;
                    continue;
                }
                // 更新minId 为当前集合最大的id
                minId = xieChengSmsCollidingDataList.get(xieChengSmsCollidingDataList.size() - 1).getId();
                // 将查询出来的明细数据进行分组，每组50个数据
                List<List<XieChengSmsCollidingData>> xieChengSmsCollidingDataPartitions =
                        Lists.partition(xieChengSmsCollidingDataList, XIECHENGSMSCOLLIDINGPARTATIONNUM);
                for (List<XieChengSmsCollidingData> xieChengSmsCollidingDataListPartition : xieChengSmsCollidingDataPartitions) {
                    xieChengSmsCollidingThread.submit(() ->
                            pushXieChengSmsCollidingData(xieChengSmsCollidingDataListPartition, failNum, localId));
                }
            }
            xieChengSmsCollidingThread.shutdown();
            try {
                while (!xieChengSmsCollidingThread.awaitTermination(10L, TimeUnit.SECONDS)) {
                }
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }

            xieChengSendAlarm(failNum, "携程短信撞库接口推送异常，请检查");
        } catch (Exception e) {
            log.error("携程短信撞库接口推送异常:{}", e);
        }
    }
    @Override
    public void pushXieChengSmsCollidingToDbDataVt(Long localId) {
        try {
            // 初始化线程池
            ThreadResult result = getThreadResult();
            Integer sendDate = Integer.valueOf(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            Long indexId = 1L;
            while (true) {
                // 动态修改线程参数
                changeTpProperties(result.xieChengSmsCollidingThreadLogSaveVt,result.xieChengSmsCollidingThreadVt, result.xieChengSmsCollidingThreadLogUpdateVt);

                // 查询需要推送的基础数据
                List<XieChengSmsCollidingDataVt> xieChengSmsCollidingDataVtList =
                        xieChengSmsCollidingDataVtMapper.selectByLocalIdVttikv_(indexId,localId,sendDate,marketingCommonConfig.getXieChengSmsCollidingDataVtPageSize());
                if (xieChengSmsCollidingDataVtList.isEmpty()) break;
                indexId = xieChengSmsCollidingDataVtList.get(xieChengSmsCollidingDataVtList.size()-1).getId();
                // 存储推送日志
                List<List<XieChengSmsCollidingDataVt>> partition = Lists.partition(xieChengSmsCollidingDataVtList, 1000);
                List<Callable<Integer>> saveLogListTask = new ArrayList<>();
                partition.forEach(p->{
                    saveLogListTask.add(()->{
                        return saveXieChengSmsCollidingDataLogVts(sendDate, p);
                    });
                });
                List<Future<Integer>> futures = result.xieChengSmsCollidingThreadLogSaveVt.invokeAll(saveLogListTask);
                for(int i=0;i<futures.size();i++){
                    Integer integer = futures.get(i).get();
                    if(integer==0){
                        log.error("插入数据库异常");
                    }
                }
                // 将查询出来的明细数据进行分组，每组50个数据
                sendXieChengDataVt(result, xieChengSmsCollidingDataVtList,sendDate);
            }
            // 线程池关门
            closedThreadPoll(result);

            // 发送异常统计信息
            sendAlertMessage(sendDate);
        } catch (Exception e) {
            log.error("携程短信撞库【VT】推送异常: {}", e);

        }
    }

    private void sendAlertMessage(Integer sendDate) {
        XieChengSmsCollidingDataLogVtExample xevt = new XieChengSmsCollidingDataLogVtExample();
        xevt.createCriteria().andStatusEqualTo(3).andSendDateEqualTo(sendDate);
        AtomicInteger failNum  = new AtomicInteger(xieChengSmsCollidingDataLogVtMapper.countByExample(xevt));
        xieChengSendAlarm(failNum, "携程短信撞库【VT】失败信息。");
    }

    private static void closedThreadPoll(ThreadResult result) {
        result.xieChengSmsCollidingThreadLogSaveVt.shutdown();
        try {
            while (!result.xieChengSmsCollidingThreadLogSaveVt.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("日志保存线程池结束");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

        result.xieChengSmsCollidingThreadVt.shutdown();
        try {
            while (!result.xieChengSmsCollidingThreadVt.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("推送过线程池结束");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }


        result.xieChengSmsCollidingThreadLogUpdateVt.shutdown();
        try {
            while (!result.xieChengSmsCollidingThreadLogUpdateVt.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("日志更新线程池结束");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

    }


    private void sendXieChengDataVt(ThreadResult result, List<XieChengSmsCollidingDataVt> xieChengSmsCollidingDataVtList,Integer sendDate) {
        List<List<XieChengSmsCollidingDataVt>> xieChengSmsCollidingDataVtPartitions =
                Lists.partition(xieChengSmsCollidingDataVtList, XIECHENGSMSCOLLIDINGPARTATIONNUM);
        for (List<XieChengSmsCollidingDataVt> xieChengSmsCollidingDataListVtPartition : xieChengSmsCollidingDataVtPartitions) {
            result.xieChengSmsCollidingThreadVt.submit(() ->
                    pushXieChengSmsCollidingDataVt(xieChengSmsCollidingDataListVtPartition, result,sendDate));
        }
    }

    private ThreadResult getThreadResult() {
        // 创建日志插入线程池
        ThreadPoolExecutor xieChengSmsCollidingThreadLogSaveVt = BrExecutors.getThreadPool(
                marketingCommonConfig.getXieChengSmsCollidingThreadLogSaveVt(),
                marketingCommonConfig.getXieChengSmsCollidingThreadLogSaveVt());

        // 创建推送线程池
        ThreadPoolExecutor xieChengSmsCollidingThreadVt = BrExecutors.getThreadPool(
                marketingCommonConfig.getXieChengSmsCollidingThreadVt(),
                marketingCommonConfig.getXieChengSmsCollidingThreadVt());

        // 创建更新线程池
        ThreadPoolExecutor xieChengSmsCollidingThreadLogUpdateVt = BrExecutors.getThreadPool(
                marketingCommonConfig.getXieChengSmsCollidingThreadLogUpdateVt(),
                marketingCommonConfig.getXieChengSmsCollidingThreadLogUpdateVt());
        ThreadResult result = new ThreadResult(xieChengSmsCollidingThreadLogSaveVt,xieChengSmsCollidingThreadVt, xieChengSmsCollidingThreadLogUpdateVt);
        return result;
    }

    private static class ThreadResult {

        public final ThreadPoolExecutor xieChengSmsCollidingThreadLogSaveVt;
        public final ThreadPoolExecutor xieChengSmsCollidingThreadVt;
        public final ThreadPoolExecutor xieChengSmsCollidingThreadLogUpdateVt;



        public ThreadResult(ThreadPoolExecutor xieChengSmsCollidingThreadLogSaveVt,ThreadPoolExecutor xieChengSmsCollidingThreadVt, ThreadPoolExecutor xieChengSmsCollidingThreadLogUpdateVt) {
            this.xieChengSmsCollidingThreadLogSaveVt = xieChengSmsCollidingThreadLogSaveVt;
            this.xieChengSmsCollidingThreadVt = xieChengSmsCollidingThreadVt;
            this.xieChengSmsCollidingThreadLogUpdateVt = xieChengSmsCollidingThreadLogUpdateVt;
        }
    }

    private void changeTpProperties(ThreadPoolExecutor xieChengSmsCollidingThreadLogSaveVt,ThreadPoolExecutor xieChengSmsCollidingThreadVt, ThreadPoolExecutor xieChengSmsCollidingThreadLogUpdateVt) {
        xieChengSmsCollidingThreadLogSaveVt.setMaximumPoolSize(marketingCommonConfig.getXieChengSmsCollidingThreadLogSaveVt());
        xieChengSmsCollidingThreadLogSaveVt.setCorePoolSize(marketingCommonConfig.getXieChengSmsCollidingThreadLogSaveVt());
        xieChengSmsCollidingThreadVt.setMaximumPoolSize(marketingCommonConfig.getXieChengSmsCollidingThreadVt());
        xieChengSmsCollidingThreadVt.setCorePoolSize(marketingCommonConfig.getXieChengSmsCollidingThreadVt());
        xieChengSmsCollidingThreadLogUpdateVt.setMaximumPoolSize(marketingCommonConfig.getXieChengSmsCollidingThreadLogUpdateVt());
        xieChengSmsCollidingThreadLogUpdateVt.setCorePoolSize(marketingCommonConfig.getXieChengSmsCollidingThreadLogUpdateVt());
    }

    private Integer  saveXieChengSmsCollidingDataLogVts(Integer sendDate, List<XieChengSmsCollidingDataVt> xieChengSmsCollidingDataVtList) {

        List<XieChengSmsCollidingDataLogVt> xcvtList = xieChengSmsCollidingDataVtList.stream()
                .map(x -> {
                            XieChengSmsCollidingDataLogVt xieChengSmsCollidingDataLogVt = new XieChengSmsCollidingDataLogVt();
                            xieChengSmsCollidingDataLogVt.setApiCode(x.getApiCode());
                            xieChengSmsCollidingDataLogVt.setLocalId(x.getLocalId());
                            xieChengSmsCollidingDataLogVt.setSha256CodeList(x.getSha256CodeList());
                            xieChengSmsCollidingDataLogVt.setSmsCollidingDataVtId(x.getId());
                            xieChengSmsCollidingDataLogVt.setStatus(1);
                            xieChengSmsCollidingDataLogVt.setType("1");
                            xieChengSmsCollidingDataLogVt.setCreateTime(new Date());
                            xieChengSmsCollidingDataLogVt.setSendDate(sendDate);
                            return xieChengSmsCollidingDataLogVt;
                        }
                ).collect(Collectors.toList());
        return   xieChengSmsCollidingDataLogVtMapper.saveBatchLogVt(xcvtList);

    }

    public void pushXieChengSmsCollidingDataVt(List<XieChengSmsCollidingDataVt> xieChengSmsCollidingDataVtPartition,
                                               ThreadResult result,Integer sendDate) {
        try {
            List<String> sha256CodeList = xieChengSmsCollidingDataVtPartition.stream()
                    .map(XieChengSmsCollidingDataVt::getSha256CodeList).collect(Collectors.toList());

            if (!sha256CodeList.isEmpty()) {
                // 携程短信撞库接口
                Result<String> postResult = xieChengService.pushXieChengSmsCollidingDataVt(sha256CodeList);
                JSONObject resultJson = JSONObject.parseObject(postResult.getData());
                // 请求正常
                if (postResult.getCode().equals(ResultCode.SUCCESS.getValue())) {

                    JSONArray returnDataList = resultJson.getJSONArray("data");
                    // mq 更新日志
                    List<XieChengSmsCollidingDataLogVt> xieChengSmsCollidingDataLogVtList= initLogVt(returnDataList,sendDate);
                    for (int i = 0; i < xieChengSmsCollidingDataLogVtList.size(); i++){
                        XieChengSmsCollidingDataLogVt xieChengSmsCollidingDataLogVt = xieChengSmsCollidingDataLogVtList.get(i);
                        result.xieChengSmsCollidingThreadLogUpdateVt.submit(() -> {
                            try {
                                xieChengSmsCollidingDataLogVtMapper.updateSelectiveVt(xieChengSmsCollidingDataLogVt);
                            } catch (Exception e) {
                                log.error("携程更新日志异常！");
                            }
                        });
                }
                    // mq 消息发送
                    sendMqData(xieChengSmsCollidingDataLogVtList);

                } else {
                    // 异常请求 只更新日志表状态3
                    String msg = resultJson.getString("msg");
                    xieChengSmsCollidingDataLogVtMapper.updateBatchVt(sha256CodeList, 3, msg,sendDate);
                }
            }
        } catch (Exception e) {
            log.error("携程短信撞库【VT】接口推送异常", e);
        }
    }

    private List<XieChengSmsCollidingDataLogVt> initLogVt(JSONArray returnDataList, Integer sendDate) {
        List<XieChengSmsCollidingDataLogVt> xieChengSmsCollidingDataLogVtList = new ArrayList<>();
        for (int i = 0; i < returnDataList.size(); i++) {
            JSONObject returnData = returnDataList.getJSONObject(i);
            XieChengSmsCollidingDataLogVt xieChengSmsCollidingDataLogVt = new XieChengSmsCollidingDataLogVt();
            xieChengSmsCollidingDataLogVt.setSha256CodeList(returnData.getString("sha256Code"));
            xieChengSmsCollidingDataLogVt.setInfo(returnData.getString("info"));
            xieChengSmsCollidingDataLogVt.setMktLevel(returnData.getString("mktLevel"));
            xieChengSmsCollidingDataLogVt.setResult(returnData.getBoolean("result"));
            xieChengSmsCollidingDataLogVt.setOrgChannel(returnData.getString("orgChannel"));
            xieChengSmsCollidingDataLogVt.setStatus(2);
            xieChengSmsCollidingDataLogVt.setSendDate(sendDate);
            xieChengSmsCollidingDataLogVtList.add(xieChengSmsCollidingDataLogVt);
        }
        return xieChengSmsCollidingDataLogVtList;
    }


    private void sendMqData(List<XieChengSmsCollidingDataLogVt> xieChengSmsCollidingDataLogVtList) {
        List<String> sha256CodeListFalseList = xieChengSmsCollidingDataLogVtList.stream()
                .filter(item -> !item.getResult())
                .map(XieChengSmsCollidingDataLogVt::getSha256CodeList)
                .collect(Collectors.toList());
        producter.send(ROUTING_KEY_XIECHENG_SMSCOLLIDINGVT_CUSTOMER
                , JSON.toJSONString(sha256CodeListFalseList));
    }


    private String getEndTime(Boolean isNewFile) {
        String endTime = getTimeDay(marketingCommonConfig.getXieChengSmsCollidingDays());
        // 新增件 next_push_time 是空的
        if (isNewFile) {
            endTime = null;
        }
        return endTime;
    }

    private boolean isJson(String str) {
        try {
            JSONObject.parseObject(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void updateLocalFile(LocalFile localFile) {
        if (localFile != null) {
            localFile.setPushEndTime(new Date());
            XieChengDataExample xieChengDataExample = new XieChengDataExample();
            xieChengDataExample.createCriteria().andLocalIdEqualTo(localFile.getId())
                    .andPushStatusEqualTo(2)
                    .andStatusEqualTo(1);
            int i = xieChengDataMapper.countByExample(xieChengDataExample);
            localFile.setPushNumber(i);
            localFileMapper.updateByPrimaryKeySelective(localFile);
        }
    }

    private void pushXieChengData(XieChengData xieChengData, AtomicInteger failNum, CountDownLatch countDownLatch) {
        try {
            countDownLatch.countDown();
            AdReqDTO adReqDTO = new AdReqDTO();
            BeanUtils.copyProperties(xieChengData,adReqDTO);

            //region 获取配置信息
            String apiCode = adReqDTO.getApiCode();
            HashMap<String, JSONObject> xieChengCallPushCondition = marketingCommonConfig.getXieChengCallPushCondition();
            if(xieChengCallPushCondition == null){
                xieChengCallPushCondition=new HashMap<>();
                xieChengCallPushCondition.put("3710058",getJo("1",Arrays.asList("3710058","3710078"), "3710058"));
                xieChengCallPushCondition.put("3710078",getJo("1",Arrays.asList("3710058","3710078"), "3710058"));
                xieChengCallPushCondition.put("3710090",getJo("2",Arrays.asList("3710090","3710091"), "3710090"));
                xieChengCallPushCondition.put("3710091",getJo("2",Arrays.asList("3710090","3710091"), "3710090"));
            }
            JSONObject condition = xieChengCallPushCondition.get(apiCode);
            String conditionKey = condition.getString("condition");
            JSONArray soleCellApiCodes = condition.getJSONArray("soleCellApiCodes");
            JSONArray isBlackApiCodes = condition.getJSONArray("isBlackApiCodes");
            JSONArray convTypeApiCodes = condition.getJSONArray("convTypeApiCodes");
            String mainApiCode = condition.getString("mainApiCode");
            //endregion

            XieChengData resultData = new XieChengData();
            resultData.setId(adReqDTO.getId());

            if(condition==null){
                resultData.setStatus(2);
                resultData.setDataMessage("该apiCode未配置规则数据");
                xieChengDataMapper.updateByPrimaryKeySelective(resultData);
                return;
            }

            adReqDTO.setConditionKey(conditionKey);
            String tcId = tableCreateService.getTcId(apiCode);
            // 字段修改兼容
            String sha256Tel = xieChengData.getSha256Tel();
            xieChengData.setSha256Tel(sha256Tel);
            // 获取redis 锁
            String key = RedisKeyConstant.pushXieChengLock.concat(":")
                    .concat(conditionKey)
                    .concat(sha256Tel);
            String value = UUID.randomUUID().toString();
            redisChgService.lock(key, value);

            //查询投诉退订
            Integer xiechengSmsQuitDataSize = xiechengSmsQuitDataMapper.getCountSmsQuitDataByMobile(sha256Tel);
            if (xiechengSmsQuitDataSize > 0) {
                resultData.setStatus(2);
                resultData.setDataMessage("命中投诉退订数据");
                xieChengDataMapper.updateByPrimaryKeySelective(resultData);
                redisChgService.unlock(key, value);
                return;
            }

            //region 特定剔除规则
            if("1".equals(conditionKey)){
                //region 剔除规则1 查询黑名单和有效期内命中convType=106或107或110
                MarketingTransferSyncUser xcTransferBlack = marketingTransferSyncUserMapper.getXcTransferNoAdDataByOnlyBlack(tcId, sha256Tel, isBlackApiCodes);
                if(xcTransferBlack!=null){
                    resultData.setDataMessage("命中黑名单");
                    resultData.setStatus(2);
                    xieChengDataMapper.updateByPrimaryKeySelective(resultData);
                    redisChgService.unlock(key, value);
                    return;
                }

                boolean hasConvType = hasConvType(mainApiCode, convTypeApiCodes, tcId, sha256Tel);
                if (hasConvType) {
                    resultData.setDataMessage("有效期内命中convType106或107或110");
                    resultData.setStatus(2);
                    xieChengDataMapper.updateByPrimaryKeySelective(resultData);
                    redisChgService.unlock(key, value);
                    return;
                }
                //endregion
            }else{
                //region 剔除规则2 查询黑名单和当日撞库结果
                MarketingTransferSyncUser xcTransferBlack = marketingTransferSyncUserMapper.getXcTransferNoAdDataByOnlyBlack(tcId, sha256Tel,isBlackApiCodes);
                if (xcTransferBlack != null) {
                    resultData.setDataMessage("命中黑名单");
                    resultData.setStatus(2);
                    xieChengDataMapper.updateByPrimaryKeySelective(resultData);
                    redisChgService.unlock(key, value);
                    return;
                }

                Integer day = Integer.valueOf(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
                XieChengSmsCollidingDataLogVtExample vtExample = new XieChengSmsCollidingDataLogVtExample();
                vtExample.createCriteria()
                        .andSha256CodeListEqualTo(sha256Tel)
                        .andStatusEqualTo(2)
                        .andSendDateEqualTo(day);
                List<XieChengSmsCollidingDataLogVt> xieChengSmsCollidingDataLogVts = xieChengSmsCollidingDataLogVtMapper.selectByExample(vtExample);
                if (xieChengSmsCollidingDataLogVts.size()<=0) {
                    resultData.setDataMessage("没有获取到当日撞库结果");
                    resultData.setStatus(2);
                    xieChengDataMapper.updateByPrimaryKeySelective(resultData);
                    redisChgService.unlock(key, value);
                    return;
                }
                XieChengSmsCollidingDataLogVt xieChengSmsCollidingDataLogVt = xieChengSmsCollidingDataLogVts.get(0);
                if(!xieChengSmsCollidingDataLogVt.getResult()){
                    resultData.setDataMessage("命中当日撞库结果为false");
                    resultData.setStatus(2);
                    xieChengDataMapper.updateByPrimaryKeySelective(resultData);
                    redisChgService.unlock(key, value);
                    return;
                }
                if (StringUtils.isBlank(xieChengSmsCollidingDataLogVt.getOrgChannel())) {
                    resultData.setDataMessage("命中当日OrgChannel为空,id="+xieChengSmsCollidingDataLogVt.getSmsCollidingDataVtId());
                    resultData.setStatus(2);
                    xieChengDataMapper.updateByPrimaryKeySelective(resultData);
                    redisChgService.unlock(key, value);
                    return;
                }
                //endregion
                adReqDTO.setMktMode("CPS");
                adReqDTO.setMktChannel(xieChengSmsCollidingDataLogVt.getOrgChannel());
                adReqDTO.setMktProductNo("CASH");
            }
            //endregion

            //region 查询到当前电话数据是否推送过 有不推，反之就推。
            List<XieChengData> xieChengRepeatDatalist = xieChengDataMapper.getByCellToday(sha256Tel,soleCellApiCodes);
            if (xieChengRepeatDatalist.isEmpty()) {
                // 组装 clickId 13位时间戳+ 随机5位数字字母 + sha256tel
                String clickId = System.currentTimeMillis() + getCode(5) + sha256Tel;
                adReqDTO.setClickId(clickId);
                // 携程推送
                Result result = xieChengService.pushXieChengData(adReqDTO);
                if (result.getCode().equals(ResultCode.SUCCESS.getValue())) {
                    resultData.setPushStatus(2);
                } else {
                    resultData.setPushStatus(3);
                    failNum.getAndIncrement();
                }
                resultData.setClickId(clickId);
                resultData.setDataMessage(result.getMessage());
            } else {
                resultData.setId(xieChengData.getId());
                resultData.setStatus(2);
                resultData.setDataMessage("数据重复未推送");
            }
            //endregion
            xieChengDataMapper.updateByPrimaryKeySelective(resultData);
            redisChgService.unlock(key, value);
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }
    }

    private boolean hasConvType(String apiCode, JSONArray convTypeApiCodes, String tcId, String sha256Tel) {
        Set<String> syncCustNumSet = new HashSet<>();
        // sha256解密，log加密
        String phone = RpcClientProxy.decode(sha256Tel, "cell", "sha", "");
        String encode = BrCipherMaker.getInstance().encode(phone);
        syncCustNumSet.add(encode);
        Map<String, SyncUserValidityPeriodBO> syncUser =
                transferDataValidityPeriodService.getValidityPeriodCellBatchFirstVersion(syncCustNumSet, apiCode, new Date());
        SyncUserValidityPeriodBO bo = syncUser.get(encode);
        if (bo != null) {
            Pair<String, String> validityRange =
                    validityPeriodDataService.getMarketingTransferDataWithValidityRange(apiCode);
            if (null == validityRange) {
                log.error("携程所有配置在有效期配置表中的上传数据均已失效！");
                return false;
            }

            String startDate = validityRange.getKey();
            String endDate = validityRange.getValue();

            Set<String> custNumSet = new HashSet<>();
            custNumSet.add(sha256Tel);
            List<XieChengJudgeConvTypeValue> xieChengJudgeConvType = marketingTransferSyncUserMapper.getXieChengJudgeConvType(tcId,
                    convTypeApiCodes,
                    startDate, endDate, custNumSet);

            if (CollectionUtils.isEmpty(xieChengJudgeConvType)) {
                return false;
            }
            XieChengJudgeConvTypeValue convTypeValue = xieChengJudgeConvType.get(0);

            // 命中convType=106或107或110
            if (convTypeValue.getHasApplySuccess() || convTypeValue.getHasInputSuccess() || convTypeValue.getHasRiskControl()) {
                return true;
            }
        }

        return false;
    }

    private JSONObject getJo(String condition,List<String> soleCellApiCodes, String mainApiCode){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("condition",condition);
        jsonObject.put("isBlackApiCodes",soleCellApiCodes);
        jsonObject.put("convTypeApiCodes",soleCellApiCodes);
        jsonObject.put("soleCellApiCodes",soleCellApiCodes);
        jsonObject.put("mainApiCode",mainApiCode);
        return jsonObject;
    }

    /**
     * 随机生成由数字、字母组成的N位验证码
     *
     * @return 返回一个字符串
     */
    public static String getCode(int n) {
        char arr[] = new char[n];
        int i = 0;
        while (i < n) {
            char ch = (char) (int) (Math.random() * 124);
            if (ch >= 'a' && ch <= 'z' || ch >= '0' && ch <= '9') {
                arr[i++] = ch;
            }
        }
        //将数组转为字符串
        return new String(arr);
    }

    /**
     * 获取某天的时间,支持自定义时间格式
     *
     * @param
     * @param index 为正表示当前时间加天数，为负表示当前时间减天数
     * @return String
     */
    public static String getTimeDay(int index) {
        TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
        TimeZone.setDefault(tz);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat fmt = new SimpleDateFormat(XIECHENGSMSCOLLIDINGFORMATTER);
        calendar.add(Calendar.DAY_OF_MONTH, -index);
        String date = fmt.format(calendar.getTime());
        return date;
    }

    public void pushXieChengSmsCollidingData(List<XieChengSmsCollidingData> xieChengSmsCollidingDataPartition, AtomicInteger failNum, long localId) {
        // 在拆分后50个一组的集合里xieChengSmsCollidingDataPartition 将sha256Code电话组装成 集合collect
        // 循环xieChengSmsCollidingDataPartition 集合 判断集合里的电话在15天内是否推送过，如果没有推送过 新增推送记录 状态为待推送状态。防止高并发下 数据重复推送
        // 如果有推送过 ，不新增推送记录，并将collect 集合中这个sha256Code 删除掉

        try {
            List<String> collect = new ArrayList<>();
            for (int i = 0; i < xieChengSmsCollidingDataPartition.size(); i++) {
                XieChengSmsCollidingData xieChengSmsCollidingData = xieChengSmsCollidingDataPartition.get(i);

                // 小写加密数据
                String sha256CodeList = xieChengSmsCollidingData.getSha256CodeList();

                // 获取redis 锁
                String key = RedisKeyConstant.pushXieChengSmsCollidingLock.concat(":")
                        .concat(xieChengSmsCollidingData.getApiCode())
                        .concat(sha256CodeList);
                String value = UUID.randomUUID().toString();
                redisChgService.lock(key, value);
                String lastTimeDay = getTimeDay(marketingCommonConfig.getXieChengSmsCollidingDays());

                // 查询到当前数据距离当前时间 14*24 小时的范围内是否推送过
                XieChengSmsCollidingDataLog xieChengSmsCollidingDataLogRe = xieChengSmsCollidingDataLogMapper.selectByCodeAndTime(sha256CodeList, lastTimeDay);
                if (xieChengSmsCollidingDataLogRe == null) {
                    // 添加集合数据
                    collect.add(sha256CodeList);

                    // 构造待推送数据
                    XieChengSmsCollidingDataLog xieChengSmsCollidingDataLog = new XieChengSmsCollidingDataLog();
                    xieChengSmsCollidingDataLog.setApiCode(xieChengSmsCollidingData.getApiCode());
                    xieChengSmsCollidingDataLog.setLocalId(xieChengSmsCollidingData.getLocalId());
                    xieChengSmsCollidingDataLog.setSha256CodeList(sha256CodeList);
                    xieChengSmsCollidingDataLog.setSmsCollidingDataId(xieChengSmsCollidingData.getId());
                    xieChengSmsCollidingDataLog.setStatus(1);
                    xieChengSmsCollidingDataLog.setType("1");
                    xieChengSmsCollidingDataLog.setCreateTime(new Date());
                    xieChengSmsCollidingDataLogMapper.insertSelective(xieChengSmsCollidingDataLog);
                } else {
                    // 更新推送时间 如果状态是2 说明当前数据推送过
                    if (xieChengSmsCollidingDataLogRe.getStatus() == 2) {
                        XieChengSmsCollidingData xieChengSmsCollidingDataNew = new XieChengSmsCollidingData();
                        xieChengSmsCollidingDataNew.setNextPushTime(xieChengSmsCollidingDataLogRe.getUpdateTime());

                        XieChengSmsCollidingDataExample xieChengSmsCollidingDataExample = new XieChengSmsCollidingDataExample();
                        List<String> sha256List = new ArrayList<>();
                        sha256List.add(xieChengSmsCollidingDataLogRe.getSha256CodeList());
                        sha256List.add(xieChengSmsCollidingDataLogRe.getSha256CodeList().toUpperCase());
                        xieChengSmsCollidingDataExample.createCriteria().andSha256CodeListIn(sha256List);
                        xieChengSmsCollidingDataMapper.updateByExampleSelective(xieChengSmsCollidingDataNew, xieChengSmsCollidingDataExample);
                    }

                }
                redisChgService.unlock(key, value);
            }
            if (!collect.isEmpty()) {
                // 携程短信撞库接口
                Result postResult = xieChengService.pushXieChengSmsCollidingData(collect);
                JSONObject resultJson = JSONObject.parseObject(postResult.getMessage());
                // 请求正常
                if (postResult.getCode().equals(ResultCode.SUCCESS.getValue())) {
                    // 更新 next_push_time
                    xieChengSmsCollidingDataMapper.updateBatch(collect);
                    JSONArray returnDataList = resultJson.getJSONArray("data");
                    List<XieChengSmsCollidingDataLog> xieChengSmsCollidingDataLogList = new ArrayList<>();
                    for (int i = 0; i < returnDataList.size(); i++) {
                        JSONObject returnData = returnDataList.getJSONObject(i);
                        String sha256Code = returnData.getString("sha256Code");
                        Boolean result = returnData.getBoolean("result");
                        String orgChannel = returnData.getString("orgChannel");
                        String mktLevel = returnData.getString("mktLevel");
                        String info = returnData.getString("info");

                        XieChengSmsCollidingDataLog xieChengSmsCollidingDataLog = new XieChengSmsCollidingDataLog();
                        xieChengSmsCollidingDataLog.setSha256CodeList(sha256Code);
                        xieChengSmsCollidingDataLog.setInfo(info);
                        xieChengSmsCollidingDataLog.setMktLevel(mktLevel);
                        xieChengSmsCollidingDataLog.setResult(result);
                        xieChengSmsCollidingDataLog.setOrgChannel(orgChannel);
                        xieChengSmsCollidingDataLog.setStatus(2);
                        xieChengSmsCollidingDataLog.setLocalId(localId);
                        XieChengSmsCollidingDataLogExample xe = new XieChengSmsCollidingDataLogExample();
                        xe.createCriteria()
                                .andStatusEqualTo(1)
                                .andSha256CodeListEqualTo(sha256Code);
                        xieChengSmsCollidingDataLogMapper.updateByExampleSelective(xieChengSmsCollidingDataLog,xe);
//                        xieChengSmsCollidingDataLogList.add(xieChengSmsCollidingDataLog);
                    }
//                    xieChengSmsCollidingDataLogMapper.updateBatch(xieChengSmsCollidingDataLogList);

                } else {
                    // 异常请求 只更新日志表状态3  不更新 next_push_time
                    String msg = resultJson.getString("msg");
                    List<XieChengSmsCollidingDataLog> xieChengSmsCollidingDataLogList = new ArrayList<>();
                    for (int i = 0; i < collect.size(); i++) {
                        failNum.getAndIncrement();
                        String sha256Code = collect.get(i);
                        XieChengSmsCollidingDataLog xieChengSmsCollidingDataLog = new XieChengSmsCollidingDataLog();
                        xieChengSmsCollidingDataLog.setStatus(3);
                        xieChengSmsCollidingDataLog.setDataMessage(msg);
                        xieChengSmsCollidingDataLog.setSha256CodeList(sha256Code);
                        xieChengSmsCollidingDataLog.setLocalId(localId);
                        xieChengSmsCollidingDataLogList.add(xieChengSmsCollidingDataLog);
                    }
                    xieChengSmsCollidingDataLogMapper.updateBatch(xieChengSmsCollidingDataLogList);
                }
            }
        } catch (Exception e) {
            log.error("携程短信撞库接口推送异常", e);
        }
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

    private void xieChengSendAlarm(AtomicInteger failNum, String title) {
        if (failNum.get() > 0) {
            try {
                alarmClient.sendAlarm("推送失败条数=" + failNum.get(), title, AlarmSendCodeEnum.EXCEPTION_URGENT.getCode());
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }
    }
}
