package com.br.marketing.service.Impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Pair;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.encryption.Sha256Util;
import com.br.common.log.AlertLog;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.dassservice.DassServiceClient;
import com.br.marketing.client.dassservice.input.DassImportAdapDTO;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.client.dassservice.input.DassWeiZhongDTO;
import com.br.marketing.client.dassservice.input.IbuReqDTO;
import com.br.marketing.client.dassservice.input.csos.DaasCsosDataAdapDTO;
import com.br.marketing.client.dassservice.input.csos.DaasCsosDataDTO;
import com.br.marketing.client.dassservice.input.transfer.DassTransferDataAdapDTO;
import com.br.marketing.client.dassservice.input.transfer.DassTransferDataDTO;
import com.br.marketing.client.dassservice.input.update.DaasUpdateDataDTO;
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
import com.br.marketing.common.constants.rocketmq.MarketingOutsideInterfaceConstants;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.SftpFileTypeEnum;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.common.utils.AESUtil;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.RandomUtils;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.rpcclient.rpcclientImpl.DecodeGrpcClient;
import com.br.marketing.service.Impl.xc.XieChengCpsCollidingDataLogService;
import com.br.marketing.service.PushDataService;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.service.ValidityPeriodDataService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.br.marketing.util.TimeUtils;
import com.br.marketing.webhook.dingding.service.DingDingRobotHookService;
import com.br.rocketmq.rocketmq.template.RocketMqTemplate;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.br.marketing.common.utils.MQConstants.ROUTING_KEY_XIECHENG_SMSCOLLIDINGVT_CUSTOMER;

@Slf4j
@Service
public class PushDataServiceImpl implements PushDataService {

    // 常量定义
    private static final int PHONE_PAGE_SIZE = 1000;
    private static final int BATCH_SIZE = 1000;

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
    private XieChengCollidingDataLogMapper xieChengCollidingDataLogMapper;

    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Resource
    private XieChengCpsCollidingDataLogMapper xieChengCpsCollidingDataLogMapper;
    @Resource
    @Qualifier("xieChengThreadPool")
    ThreadPoolExecutor xieChengThreadPool;

    @Resource
    private DingDingRobotHookService dingDingRobotHookService;

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
    @Resource
    private RocketMqSwitch rocketMqSwitch;
    @Resource
    private RocketMqTemplate template;

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

    @Resource
    private HaierCollidingDataMapper haierCollidingDataMapper;
    @Resource
    private HaierCollidingDataLogMapper haierCollidingDataLogMapper;

    @Resource
    private CsosPhoneSaleMapper csosPhoneSaleMapper;

    @Resource
    UpdatePhoneSaleMapper updatePhoneSaleMapper;
    @Resource
    private XieChengCpsCollidingDataLogService cpsLogService;
    final static DateTimeFormatter yyyyMMddDF = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final static int XIECHENGSMSCOLLIDINGPARTATIONNUM = 50;

    private final static String SMS_QUIT ="saveAdMobileBlack";

    private final static String XIECHENGSMSCOLLIDINGFORMATTER = "yyyy-MM-dd HH:mm:ss";

    ThreadPoolExecutor pushDassThreadPool = BrExecutors.getThreadPool(5, 5);

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
        modifyThreadPool(pushDassThreadPool, threadNum);

        LocalFile localFile = localFileMapper.selectByPrimaryKey(id);
        if (localFile == null) {
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("文件不存在").setDate(isContiue);
        }

        localFile.setPushStartTime(new Date());

        Integer number = 0;
        List<CompletableFuture<Void>> futures = Lists.newArrayList();
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

                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
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
                    } catch (Exception e) {
                        log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DAASERROR.getCode(),
                                "sftp文件推送Dass子线程异常，异常日志：" + e.getMessage()), e);
                    }
                }, pushDassThreadPool);
                futures.add(future);
            } else {
                actionMark = false;
            }
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

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
        modifyThreadPool(pushDassThreadPool, threadNum);

        LocalFile localFile = localFileMapper.selectByPrimaryKey(id);
        if (localFile == null) {
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("文件不存在").setDate(isContiue);
        }

        localFile.setPushStartTime(new Date());
        Integer number = 0;
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);
        AtomicInteger retry = new AtomicInteger(0);
        List<CompletableFuture<Void>> futures = Lists.newArrayList();
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

                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    Result result = methodRetryHandlerService.dassTransferWithFile(dto, null);
                    int size = dto.getDassTransferDataDTOList().size();
                    if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                        success.addAndGet(size);
                    } else if (ResultCode.FAIL.getValue().equals(result.getCode())) {
                        fail.addAndGet(size);
                    } else {
                        retry.addAndGet(size);
                    }
                }, pushDassThreadPool);
                futures.add(future);
            } else {
                actionMark = false;
            }
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

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
            List<PhoneSaleIbu> phoneSaleIbus = phoneSaleIbuMapper.getPushDassTransferData(id, minId);
            number += phoneSaleIbus.size();
            if (phoneSaleIbus.size() > 0) {
                ArrayList<IbuReqDTO.Datum> reqlist = new ArrayList<>();
                for (PhoneSaleIbu ibu : phoneSaleIbus) {
                    IbuReqDTO.Datum dataum = new IbuReqDTO.Datum();
                    BeanUtils.copyProperties(ibu, dataum);
                    dataum.setPlanId(StringUtils.isNotBlank(ibu.getPlanId()) ? Integer.valueOf(ibu.getPlanId()) : null);
                    dataum.setCallAccessScore(StringUtils.isNotBlank(ibu.getCallAccessScore()) ? Integer.valueOf(ibu.getCallAccessScore()) : null);
                    dataum.setPid(StringUtils.isNotBlank(ibu.getPid()) ? Integer.valueOf(ibu.getPid()) : null);
                    dataum.setConnectTimes(StringUtils.isNotBlank(ibu.getConnectTimes()) ? Integer.valueOf(ibu.getConnectTimes()) : null);
                    dataum.setZyTotalUsableAmount(StringUtils.isNotBlank(ibu.getZyTotalUsableAmount()) ? new BigDecimal(ibu.getZyTotalUsableAmount()) : null);
                    if (StringUtils.isNotBlank(ibu.getRecommendH5List())) {
                        dataum.setRecommendH5List(Arrays.asList(ibu.getRecommendList()));
                    }
                    if (StringUtils.isNotBlank(ibu.getRecommendList())) {
                        dataum.setRecommendList(Arrays.asList(ibu.getRecommendList()));
                    }
                    dataum.setZyApplyFlag(StringUtils.isNotBlank(ibu.getZyApplyFlag()) ? Boolean.valueOf(ibu.getZyApplyFlag()) : null);
                    dataum.setZyApplySuccessFlag(StringUtils.isNotBlank(ibu.getZyApplySuccessFlag()) ? Boolean.valueOf(ibu.getZyApplySuccessFlag()) : null);
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
        localFileMapper.updateByPrimaryKeySelective(localFile);
        if (SftpFileTypeEnum.DXIBU.getValue().equals(localFile.getFileType())) {
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
    public Boolean isPushDassWithCallGrade(String ruleLabel, String intentionGrade) {
        HashMap<String, List<String>> gradeOfcallToDass = marketingCommonConfig.getGradeOfcallToDass();
        List<String> grades = gradeOfcallToDass.get(ruleLabel);
        if (grades == null) {
            return false;
        }
        if (org.apache.commons.lang3.StringUtils.isBlank(intentionGrade)) {
            return false;
        }
        for (String grade : grades) {
            if (intentionGrade.toUpperCase().contains(grade)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getStatusByGrade(String ruleLabel, String intentionGrade) {
        HashMap<String, List<String>> gradeOfcallToDass = marketingCommonConfig.getGradeOfcallToDass();
        List<String> grades = gradeOfcallToDass.get(ruleLabel);
        if (grades == null) {
            return "";
        }
        if (org.apache.commons.lang3.StringUtils.isBlank(intentionGrade)) {
            return "";
        }
        for (String grade : grades) {
            if (intentionGrade.toUpperCase().contains(grade)) {
                return grade.toLowerCase();
            }
        }
        return "";
    }

    /**
     * 海尔撞库数据推送
     *
     * @param localId 本地文件id
     * @author senyang.zheng
     * @date 2023/12/23
     */
    @Override
    public void pushHaierCollidingData(Long localId) {
        // 初始化线程池
        // 创建推送线程池
        ThreadPoolExecutor collidingExecutor =
                BrExecutors.getThreadPool(marketingCommonConfig.getHaierCollidingDataThreadNum(), marketingCommonConfig.getHaierCollidingDataThreadNum());
        try {
            Integer sendDate = Integer.valueOf(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            // 查询需要撞库的数据
            List<HaierCollidingData> haierCollidingDataList =
                    haierCollidingDataMapper.selectByLocalId(localId, sendDate, marketingCommonConfig.getHaierCollidingDataPageSize());
            if (CollectionUtils.isEmpty(haierCollidingDataList)) {
                return;
            }
            // 存储推送日志
            Integer pushNum = saveHaierCollingDataLog(sendDate, haierCollidingDataList);

            sendHaierCollidingData(collidingExecutor, haierCollidingDataList, sendDate);

        } catch (Exception e) {
            log.error("海尔撞库数据推送异常。", e);
        } finally {
            // 关闭线程池
            collidingExecutor.shutdown();
            try {
                while (!collidingExecutor.awaitTermination(10L, TimeUnit.SECONDS)) {
                    log.warn("海尔撞库线程池等待释放");
                }
            } catch (InterruptedException e) {
                collidingExecutor.shutdownNow();
                Thread.currentThread().interrupt();
                log.error("海尔撞库线程池等待释放线程池关闭异常,直接关闭-InterruptedException-", e);
            } catch (Exception e) {
                collidingExecutor.shutdownNow();
                log.error("海尔撞库线程池等待释放线程池关闭异常,直接关闭-Exception-", e);
            }
        }
    }

    private void sendHaierCollidingData(ThreadPoolExecutor collidingExecutor, List<HaierCollidingData> haierCollidingDataList, Integer sendDate) {
        haierCollidingDataList.forEach((HaierCollidingData data) -> collidingExecutor.submit(() -> processHaierCollidingData(data, sendDate)));
    }

    private void processHaierCollidingData(HaierCollidingData data, Integer sendDate) {
        try {
            Result<String> postResult = haierServiceClient.pushHaierCollidingData(data.getMobileDigest(), data.getApiCode());
            JSONObject resultJson = JSONObject.parseObject(postResult.getData());
            if (ResultCode.SUCCESS.getValue().equals(postResult.getCode())) {
                // 更新成功
                updateHaierCollidingDataStatus(data.getId(), sendDate, resultJson, 2);
            } else {
                // 更新失败
                updateHaierCollidingDataStatus(data.getId(), sendDate, resultJson, 3);
            }
        } catch (Exception e) {
            log.error("haier处理撞库异常", e);
        }
    }

    private void updateHaierCollidingDataStatus(Long id, Integer sendDate, JSONObject resultJson, Integer status) {
        JSONObject data = resultJson.getJSONObject("data");
        Integer result = data != null ? data.getInteger("status") : null;
        HaierCollidingDataLog updateLog = new HaierCollidingDataLog();
        updateLog.setCollidingDataId(id);
        updateLog.setSendDate(sendDate);
        updateLog.setResult(result);
        updateLog.setStatus(status);
        haierCollidingDataLogMapper.updateBySelective(updateLog);
    }


    private Integer saveHaierCollingDataLog(Integer sendDate, List<HaierCollidingData> haierCollidingDataList) {
        List<HaierCollidingDataLog> haierCollidingDataLogs = haierCollidingDataList.stream()
                .map((HaierCollidingData data) -> {
                            HaierCollidingDataLog haierCollidingDataLog = new HaierCollidingDataLog();
                            haierCollidingDataLog.setCollidingDataId(data.getId());
                            haierCollidingDataLog.setApiCode(data.getApiCode());
                            haierCollidingDataLog.setLocalId(data.getLocalId());
                            haierCollidingDataLog.setType("1");
                            haierCollidingDataLog.setMobileDigest(data.getMobileDigest());
                            haierCollidingDataLog.setStatus(1);
                            haierCollidingDataLog.setSendDate(sendDate);
                            return haierCollidingDataLog;
                        }
                ).collect(Collectors.toList());
        return haierCollidingDataLogMapper.saveBatchLog(haierCollidingDataLogs);
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
                ThreadPoolAdjustmentUtil.adjustThreadPoolSize(
                    pool,
                    Integer.parseInt(marketingCommonConfig.getXieChengSmsQuitThreadNum())
                );
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
                    SmsQuitReq smsQuitReq = new SmsQuitReq(pushList.getCipherMobile(), pushList.getBlackListType(), pushList.getApiCode());
                    //兼容Md5手机号
                    String phone = smsQuitReq.getCipherMobile();
                    if (DecodeGrpcClient.isMd5(phone)) {
                        smsQuitReq.setCipherMobile(Sha256Util.getSHA256Encrypt(RpcClientProxy.decode(phone, "cell", "md5", "")));
                    }
                    Result result;
                    if (marketingCommonConfig.getXiechengICParamSwitch().get(SMS_QUIT)) {
                        result = xieChengService.sendSmsQuitDataNew(smsQuitReq);
                    } else {
                        result = xieChengService.sendSmsQuitData(smsQuitReq);
                    }
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
        localFile.setPushStatus("2");
        localFileMapper.updateByPrimaryKeySelective(localFile);
        xieChengSendAlarm(failNum, "携程短信退订接口推送异常，请检查");
    }


    @Override
    public Result pushXieChengToDbData(String data) {
        try {
            JSONObject jsonObject = JSONObject.parseObject(data);
            Long id = jsonObject.getLong("localId");
            List<XieChengData> xieChengDatalist = xieChengDataMapper.selectByLocalId(id);
            for (int i = 0; i < xieChengDatalist.size(); i++) {
                XieChengData xieChengData = xieChengDatalist.get(i);
                xieChengThreadPool.submit(() -> pushXieChengData(xieChengData));
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                    "携程上报异常，" + "errorMessage=" + e.getMessage()), e);
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
    }

    @Override
    public void retryPushXieChengSmsCollidingToDbData(Long localId) {
        // 创建线程池
        ThreadPoolExecutor xieChengSmsCollidingRetryThread =
                BrExecutors.getThreadPool(marketingCommonConfig.getXieChengSmsCollidingRetryThread(),
                        marketingCommonConfig.getXieChengSmsCollidingRetryThread());

        // 根据id匹配 进行数据查询 每批次查询 20000
        Long minId = null;
        AtomicInteger failNum = new AtomicInteger(0);
        while (true) {
            List<XieChengSmsCollidingData> xieChengSmsCollidingDataRetryList =
                    xieChengSmsCollidingDataMapper.selectByRetryCount(localId, minId);
            if (xieChengSmsCollidingDataRetryList.size() == 0 && (
                    TimeUtils.timeCompare(
                            marketingCommonConfig.getXieChengSmsCollidingRetryWarnAllTime().get(0),
                            marketingCommonConfig.getXieChengSmsCollidingRetryWarnAllTime().get(1)
                    )
                            ||
                            TimeUtils.timeCompare(
                                    marketingCommonConfig.getXieChengSmsCollidingRetryWarnAllTime().get(2),
                                    marketingCommonConfig.getXieChengSmsCollidingRetryWarnAllTime().get(3))
            )
            ) {
                xieChengSmsCollidingDataRetryList =
                        xieChengSmsCollidingDataMapper.selectByRetryCountThree(localId, minId);
            }
            if (xieChengSmsCollidingDataRetryList.size() == 0) {
                break;
            }
            // 更新minId 为当前集合最大的id
            minId = xieChengSmsCollidingDataRetryList.get(xieChengSmsCollidingDataRetryList.size() - 1).getId();
            // 将查询出来的明细数据进行分组，每组50个数据
            List<List<XieChengSmsCollidingData>> xieChengSmsCollidingDataPartitions =
                    Lists.partition(xieChengSmsCollidingDataRetryList, XIECHENGSMSCOLLIDINGPARTATIONNUM);
            for (List<XieChengSmsCollidingData> xieChengSmsCollidingDataListRetryPartition : xieChengSmsCollidingDataPartitions) {
                xieChengSmsCollidingRetryThread.submit(() ->
                        pushXieChengSmsCollidingData(xieChengSmsCollidingDataListRetryPartition, failNum, localId));
            }
        }

        xieChengSmsCollidingRetryThread.shutdown();
        try {
            while (!xieChengSmsCollidingRetryThread.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("携程撞库线程池关闭");
            }
        } catch (InterruptedException ex) {
            xieChengSmsCollidingRetryThread.shutdownNow();
            log.error("日志保存线程池结束异常！", ex);
            Thread.currentThread().interrupt();
        }

        xieChengSendAlarm(failNum, "携程短信撞库接口重试推送异常，请检查");
    }


    @Override
    public void pushXieChengSmsCollidingToDbData(String data) {
        try {
            JSONObject jsonObject = JSONObject.parseObject(data);
            Long localId = jsonObject.getLong("localId");
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
                    log.info("携程撞库线程池关闭");
                }
            } catch (InterruptedException ex) {
                xieChengSmsCollidingThread.shutdownNow();
                log.error("日志保存线程池结束异常！", ex);
                Thread.currentThread().interrupt();
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
                changeTpProperties(result.xieChengSmsCollidingThreadLogSaveVt, result.xieChengSmsCollidingThreadVt, result.xieChengSmsCollidingThreadLogUpdateVt);

                // 查询需要推送的基础数据
                List<XieChengSmsCollidingDataVt> xieChengSmsCollidingDataVtList =
                        xieChengSmsCollidingDataVtMapper.selectByLocalIdVttikv_(indexId, localId, sendDate, marketingCommonConfig.getXieChengSmsCollidingDataVtPageSize());
                if (xieChengSmsCollidingDataVtList.isEmpty()) break;
                indexId = xieChengSmsCollidingDataVtList.get(xieChengSmsCollidingDataVtList.size() - 1).getId();
                // 存储推送日志
                List<List<XieChengSmsCollidingDataVt>> partition = Lists.partition(xieChengSmsCollidingDataVtList, 1000);
                List<Callable<Integer>> saveLogListTask = new ArrayList<>();
                partition.forEach(p -> {
                    saveLogListTask.add(() -> {
                        return saveXieChengSmsCollidingDataLogVts(sendDate, p);
                    });
                });
                List<Future<Integer>> futures = result.xieChengSmsCollidingThreadLogSaveVt.invokeAll(saveLogListTask);
                for (int i = 0; i < futures.size(); i++) {
                    Integer integer = futures.get(i).get();
                    if (integer == 0) {
                        log.error("插入数据库异常");
                    }
                }
                // 将查询出来的明细数据进行分组，每组50个数据
                sendXieChengDataVt(result, xieChengSmsCollidingDataVtList, sendDate);
            }
            // 线程池关门
            closedThreadPoll(result);

            // 发送异常统计信息
            sendAlertMessage(sendDate);

            // 更新文件推送状态
            LocalFile localFile = new LocalFile();
            localFile.setId(localId);
            localFile.setPushStatus("2");
            localFileMapper.updateByPrimaryKeySelective(localFile);
        } catch (Exception e) {
            log.error("携程短信撞库【VT】推送异常: {}", e);

        }
    }

    private void sendAlertMessage(Integer sendDate) {
        XieChengSmsCollidingDataLogVtExample xevt = new XieChengSmsCollidingDataLogVtExample();
        xevt.createCriteria().andStatusEqualTo(3).andSendDateEqualTo(sendDate);
        AtomicInteger failNum = new AtomicInteger(xieChengSmsCollidingDataLogVtMapper.countByExample(xevt));
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


    private void sendXieChengDataVt(ThreadResult result, List<XieChengSmsCollidingDataVt> xieChengSmsCollidingDataVtList, Integer sendDate) {
        List<List<XieChengSmsCollidingDataVt>> xieChengSmsCollidingDataVtPartitions =
                Lists.partition(xieChengSmsCollidingDataVtList, XIECHENGSMSCOLLIDINGPARTATIONNUM);
        for (List<XieChengSmsCollidingDataVt> xieChengSmsCollidingDataListVtPartition : xieChengSmsCollidingDataVtPartitions) {
            result.xieChengSmsCollidingThreadVt.submit(() ->
                    pushXieChengSmsCollidingDataVt(xieChengSmsCollidingDataListVtPartition, result, sendDate));
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
        ThreadResult result = new ThreadResult(xieChengSmsCollidingThreadLogSaveVt, xieChengSmsCollidingThreadVt, xieChengSmsCollidingThreadLogUpdateVt);
        return result;
    }

    private static class ThreadResult {

        public final ThreadPoolExecutor xieChengSmsCollidingThreadLogSaveVt;
        public final ThreadPoolExecutor xieChengSmsCollidingThreadVt;
        public final ThreadPoolExecutor xieChengSmsCollidingThreadLogUpdateVt;


        public ThreadResult(ThreadPoolExecutor xieChengSmsCollidingThreadLogSaveVt, ThreadPoolExecutor xieChengSmsCollidingThreadVt, ThreadPoolExecutor xieChengSmsCollidingThreadLogUpdateVt) {
            this.xieChengSmsCollidingThreadLogSaveVt = xieChengSmsCollidingThreadLogSaveVt;
            this.xieChengSmsCollidingThreadVt = xieChengSmsCollidingThreadVt;
            this.xieChengSmsCollidingThreadLogUpdateVt = xieChengSmsCollidingThreadLogUpdateVt;
        }
    }

    private void changeTpProperties(ThreadPoolExecutor xieChengSmsCollidingThreadLogSaveVt, ThreadPoolExecutor xieChengSmsCollidingThreadVt, ThreadPoolExecutor xieChengSmsCollidingThreadLogUpdateVt) {
        ThreadPoolAdjustmentUtil.adjustThreadPoolSize(
                xieChengSmsCollidingThreadLogSaveVt, marketingCommonConfig.getXieChengSmsCollidingThreadLogSaveVt());
        ThreadPoolAdjustmentUtil.adjustThreadPoolSize(
                xieChengSmsCollidingThreadVt, marketingCommonConfig.getXieChengSmsCollidingThreadVt());
        ThreadPoolAdjustmentUtil.adjustThreadPoolSize(
                xieChengSmsCollidingThreadLogUpdateVt, marketingCommonConfig.getXieChengSmsCollidingThreadLogUpdateVt());
    }

    private Integer saveXieChengSmsCollidingDataLogVts(Integer sendDate, List<XieChengSmsCollidingDataVt> xieChengSmsCollidingDataVtList) {

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
        return xieChengSmsCollidingDataLogVtMapper.saveBatchLogVt(xcvtList);

    }

    public void pushXieChengSmsCollidingDataVt(List<XieChengSmsCollidingDataVt> xieChengSmsCollidingDataVtPartition,
                                               ThreadResult result, Integer sendDate) {
        try {
            List<String> sha256CodeList = xieChengSmsCollidingDataVtPartition.stream()
                    .map(XieChengSmsCollidingDataVt::getSha256CodeList).collect(Collectors.toList());
            Map<String, Long> cellToIds = xieChengSmsCollidingDataVtPartition.stream()
                    .collect(Collectors.toMap(XieChengSmsCollidingDataVt::getSha256CodeList, XieChengSmsCollidingDataVt::getId));
            if (!sha256CodeList.isEmpty()) {
                // 携程短信撞库接口
                Result<String> postResult = xieChengService.pushXieChengSmsCollidingDataVt(sha256CodeList);
                JSONObject resultJson = JSONObject.parseObject(postResult.getData());
                // 请求正常
                if (postResult.getCode().equals(ResultCode.SUCCESS.getValue())) {

                    JSONArray returnDataList = resultJson.getJSONArray("data");
                    // 线程池更新日志
                    List<XieChengSmsCollidingDataLogVt> xieChengSmsCollidingDataLogVtList = initLogVt(returnDataList, sendDate);
                    for (int i = 0; i < xieChengSmsCollidingDataLogVtList.size(); i++) {
                        XieChengSmsCollidingDataLogVt xieChengSmsCollidingDataLogVt = xieChengSmsCollidingDataLogVtList.get(i);
                        result.xieChengSmsCollidingThreadLogUpdateVt.submit(() -> {
                            try {
                                if (null != xieChengSmsCollidingDataLogVt.getNextPushTime()) {
                                    XieChengSmsCollidingDataVt xieChengSmsCollidingDataVt = new XieChengSmsCollidingDataVt();
                                    xieChengSmsCollidingDataVt.setId(cellToIds.get(xieChengSmsCollidingDataLogVt.getSha256CodeList()));
                                    xieChengSmsCollidingDataVt.setNextPushTime(xieChengSmsCollidingDataLogVt.getNextPushTime());
                                    xieChengSmsCollidingDataVtMapper.updateByPrimaryKeySelective(xieChengSmsCollidingDataVt);
                                }
                                xieChengSmsCollidingDataLogVtMapper.updateSelectiveVt(xieChengSmsCollidingDataLogVt);
                            } catch (Exception e) {
                                log.error("携程cps更新日志异常！,cell=" + xieChengSmsCollidingDataLogVt.getSha256CodeList(), e);
                            }
                        });
                    }

                    // 推送外呼
                    List<XieChengCpsCollidingDataLog> xieChengCpsCollidingDataLogList = xieChengSmsCollidingDataLogVtList.stream().map(t -> {
                        XieChengCpsCollidingDataLog xieChengCpsCollidingDataLog = new XieChengCpsCollidingDataLog();
                        xieChengCpsCollidingDataLog.setCellSha256CodeList(t.getSha256CodeList());
                        xieChengCpsCollidingDataLog.setResult(t.getResult());
                        return xieChengCpsCollidingDataLog;
                    }).collect(Collectors.toList());
                    cpsLogService.pushRobotMessage(xieChengCpsCollidingDataLogList);
                } else {
                    // 异常请求 只更新日志表状态3
                    String msg = resultJson.getString("msg");
                    xieChengSmsCollidingDataLogVtMapper.updateBatchVt(sha256CodeList, 3, msg, sendDate);
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
            if (StringUtils.isNotBlank(returnData.getString("releaseTime"))) {
                xieChengSmsCollidingDataLogVt.setNextPushTime(
                        DateUtil.parse(returnData.getString("releaseTime"), DatePattern.NORM_DATETIME_PATTERN));
            }
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
        String jsonString = JSON.toJSONString(sha256CodeListFalseList);
        if (rocketMqSwitch.rocketMQSwitchFlag(null, MarketingOutsideInterfaceConstants.TAG_MARKETING_XIECHENGSMSCOLLIDINGVT_CUSTOMER)) {
            rocketMqSwitch.syncSend(MarketingOutsideInterfaceConstants.TOPIC
                    , MarketingOutsideInterfaceConstants.TAG_MARKETING_XIECHENGSMSCOLLIDINGVT_CUSTOMER, jsonString);
        } else {
            producter.send(ROUTING_KEY_XIECHENG_SMSCOLLIDINGVT_CUSTOMER
                    , jsonString);
        }
    }


    private String getEndTime(Boolean isNewFile) {
        String endTime = getTimeDay(marketingCommonConfig.getXieChengSmsCollidingDays());
        // 新增件 next_push_time 是空的
        if (isNewFile) {
            endTime = null;
        }
        return endTime;
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

    private void pushXieChengData(XieChengData xieChengData) {
        try {
            AdReqDTO adReqDTO = new AdReqDTO();
            BeanUtils.copyProperties(xieChengData, adReqDTO);
            XieChengData resultData = new XieChengData();
            resultData.setId(adReqDTO.getId());
            //region 获取配置信息
            String apiCode = adReqDTO.getApiCode();
            HashMap<String, JSONObject> xieChengCallPushCondition = marketingCommonConfig.getXieChengCallPushCondition();
            if (xieChengCallPushCondition == null) {
                xieChengCallPushCondition = new HashMap<>();
                xieChengCallPushCondition.put("3710058", getJo("1", Arrays.asList("3710058", "3710078"), "3710058"));
                xieChengCallPushCondition.put("3710078", getJo("1", Arrays.asList("3710058", "3710078"), "3710058"));
                xieChengCallPushCondition.put("3710090", getJo("2", Arrays.asList("3710090", "3710091"), "3710090"));
                xieChengCallPushCondition.put("3710091", getJo("2", Arrays.asList("3710090", "3710091"), "3710090"));
            }
            JSONObject condition = xieChengCallPushCondition.get(apiCode);
            if (condition == null) {
                resultData.setStatus(2);
                resultData.setDataMessage("该apiCode未配置规则数据");
                xieChengDataMapper.updateByPrimaryKeySelective(resultData);
                return;
            }
            String conditionKey = condition.getString("condition");
            JSONArray soleCellApiCodes = condition.getJSONArray("soleCellApiCodes");
            JSONArray isBlackApiCodes = condition.getJSONArray("isBlackApiCodes");
            JSONArray convTypeApiCodes = condition.getJSONArray("convTypeApiCodes");
            String mainApiCode = condition.getString("mainApiCode");
            //按撞库锁定周期上报去重，目前只对于3710058
            Boolean offRepeatByPeriod = condition.getBoolean("offRepeatByPeriod") == null
                    ? false : condition.getBoolean("offRepeatByPeriod");
            Integer offRepeatCount = offRepeatByPeriod ? condition.getInteger("offRepeatCount") : null;

            adReqDTO.setConditionKey(conditionKey);
            String tcId = tableCreateService.getTcId(apiCode);
            // 字段修改兼容
            String sha256Tel = xieChengData.getSha256Tel();
            // 获取redis 锁
            String key = RedisKeyConstant.pushXieChengLock.concat(":")
                    .concat(conditionKey)
                    .concat(sha256Tel);
            String value = UUID.randomUUID().toString();
            redisChgService.lock(key, value);
            try {
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
                if ("1".equals(conditionKey)) {
                    //region 剔除规则1 查询黑名单和有效期内命中convType=106或107或110
                    MarketingTransferSyncUser xcTransferBlack = marketingTransferSyncUserMapper.getXcTransferNoAdDataByOnlyBlack(tcId, sha256Tel, isBlackApiCodes);
                    if (xcTransferBlack != null) {
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
                    XieChengCollidingDataLog selectLog = xieChengCollidingDataLogMapper.selectlog(sha256Tel);
                    if (selectLog == null) {
                        resultData.setDataMessage("当前数据在日志表中未查到");
                        resultData.setStatus(2);
                        xieChengDataMapper.updateByPrimaryKeySelective(resultData);
                        redisChgService.unlock(key, value);
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode()
                                , "当前数据在日志表中未查到"));
                        return;
                    }
                    adReqDTO.setMktChannel(selectLog.getOrgChannel());
                    //endregion
                } else {
                    //region 剔除规则2 查询黑名单和当日撞库结果
                    MarketingTransferSyncUser xcTransferBlack = marketingTransferSyncUserMapper.getXcTransferNoAdDataByOnlyBlack(tcId, sha256Tel, isBlackApiCodes);
                    if (xcTransferBlack != null) {
                        resultData.setDataMessage("命中黑名单");
                        resultData.setStatus(2);
                        xieChengDataMapper.updateByPrimaryKeySelective(resultData);
                        redisChgService.unlock(key, value);
                        return;
                    }
                    XieChengCpsCollidingDataLog dataLogVt = xieChengCpsCollidingDataLogMapper.selectLatestCpsLog(sha256Tel);
                    if (null == dataLogVt) {
                        resultData.setDataMessage("撞库释放时间小于当前时间或无返回true的撞库日志");
                        resultData.setStatus(2);
                        xieChengDataMapper.updateByPrimaryKeySelective(resultData);
                        redisChgService.unlock(key, value);
                        return;
                    }
                    if (StringUtils.isBlank(dataLogVt.getOrgChannel())) {
                        resultData.setDataMessage("撞库日志获取orgChannel为空");
                        resultData.setStatus(2);
                        xieChengDataMapper.updateByPrimaryKeySelective(resultData);
                        redisChgService.unlock(key, value);
                        return;
                    }
                    adReqDTO.setMktChannel(dataLogVt.getOrgChannel());
                }
                //endregion
                Boolean isPush;
                Boolean isDelete = false;
                if (offRepeatByPeriod) {
                    List<Integer> reportCountInPeriod = xieChengDataMapper.getReportPushStatusInPeriod(sha256Tel, apiCode);
                    if (CollectionUtils.isEmpty(reportCountInPeriod)) {
                        isPush = false;
                        isDelete = true;
                    } else {
                        Integer pushCount = reportCountInPeriod.stream().filter(pushStatus -> pushStatus == 2)
                                .collect(Collectors.toList()).size();
                        isPush = pushCount < offRepeatCount;
                    }
                } else {
                    List<XieChengData> xieChengRepeatDatalist = xieChengDataMapper.getByCellToday(sha256Tel, soleCellApiCodes);
                    isPush = CollectionUtils.isEmpty(xieChengRepeatDatalist);
                }
                if (isPush) {
                    // 组装 clickId 13位时间戳+ 随机5位数字字母 + sha256tel
                    String clickId = System.currentTimeMillis() + getCode(5) + sha256Tel;
                    adReqDTO.setClickId(clickId);
                    Boolean xieChengCpaAndCpsSwitch = marketingCommonConfig.getXieChengCpaAndCpsSwitch();
                    Result result;
                    if (xieChengCpaAndCpsSwitch) {
                        // 携程推送新接口
                        result = xieChengService.pushXieChengDataNew(adReqDTO);
                    } else {
                        // 携程推送旧接口
                        result = xieChengService.pushXieChengData(adReqDTO);
                    }

                    if (result.getCode().equals(ResultCode.SUCCESS.getValue())) {
                        resultData.setPushStatus(2);
                    } else {
                        resultData.setPushStatus(3);
                    }
                    resultData.setClickId(clickId);
                    resultData.setDataMessage(result.getMessage());
                } else {
                    resultData.setId(xieChengData.getId());
                    resultData.setStatus(2);
                    String dataMessage = "";
                    if (offRepeatByPeriod) {
                        if (isDelete) {
                            dataMessage = "数据不在锁定期内，不可推送";
                        } else {
                            dataMessage = "数据在锁定期内已推送过" + offRepeatCount + "次";
                        }
                    } else {
                        dataMessage = "数据重复未推送";
                    }
                    resultData.setDataMessage(dataMessage);
                }
                //endregion
                xieChengDataMapper.updateByPrimaryKeySelective(resultData);
                redisChgService.unlock(key, value);
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                        "携程上报异常，id=" + xieChengData.getId() + "，localId=" + xieChengData.getLocalId() + "errorMessage=" + e.getMessage()), e);
                redisChgService.unlock(key, value);
            }

        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                    "携程上报异常,errorMessage=" + e.getMessage()), e);
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

    private JSONObject getJo(String condition, List<String> soleCellApiCodes, String mainApiCode) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("condition", condition);
        jsonObject.put("isBlackApiCodes", soleCellApiCodes);
        jsonObject.put("convTypeApiCodes", soleCellApiCodes);
        jsonObject.put("soleCellApiCodes", soleCellApiCodes);
        jsonObject.put("mainApiCode", mainApiCode);
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
                    for (int i = 0; i < returnDataList.size(); i++) {
                        JSONObject returnData = returnDataList.getJSONObject(i);
                        String sha256Code = returnData.getString("sha256Code");
                        Boolean result = returnData.getBoolean("result");
                        String orgChannel = returnData.getString("orgChannel");
                        String mktLevel = returnData.getString("mktLevel");
                        String info = returnData.getString("info");
                        String releaseTime = returnData.getString("releaseTime");

                        XieChengSmsCollidingDataLog xieChengSmsCollidingDataLog = new XieChengSmsCollidingDataLog();
                        xieChengSmsCollidingDataLog.setSha256CodeList(sha256Code);
                        xieChengSmsCollidingDataLog.setInfo(info);
                        xieChengSmsCollidingDataLog.setMktLevel(mktLevel);
                        xieChengSmsCollidingDataLog.setResult(result);
                        xieChengSmsCollidingDataLog.setOrgChannel(orgChannel);
                        xieChengSmsCollidingDataLog.setStatus(2);
                        xieChengSmsCollidingDataLog.setLocalId(localId);
                        xieChengSmsCollidingDataLog.setReleaseTime(releaseTime);
                        XieChengSmsCollidingDataLogExample xe = new XieChengSmsCollidingDataLogExample();
                        xe.createCriteria()
                                .andStatusEqualTo(1)
                                .andSha256CodeListEqualTo(sha256Code);
                        xieChengSmsCollidingDataLogMapper.updateByExampleSelective(xieChengSmsCollidingDataLog, xe);
                    }

                } else {
                    // 异常请求 更新日志表状态3 更新数据data 表 push_status =3 不更新 next_push_time
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
                    // 更新 retry_count + 1
                    xieChengSmsCollidingDataMapper.updateBatchRetryCount(collect);
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
        Boolean setnx = redisChgService.setnx(key, status, 3);
        //已经被其他数据抢占锁了
        if (!setnx) {
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

    private void modifyThreadPool(ThreadPoolExecutor threadPool, Integer poolSize) {
        ThreadPoolAdjustmentUtil.adjustThreadPoolSize(threadPool, poolSize);
    }

    @Override
    public Result pushCsosDassData(Long id) {
        Boolean actionMark = true;
        Long minId = null;
        String key = "dass:push:threadnum";
        Integer threadNum = 5;
        if (redisChgService.exists(key) && StringUtils.isNotBlank(redisChgService.get(key))) {
            threadNum = Integer.valueOf(redisChgService.get(key));
        }
        modifyThreadPool(pushDassThreadPool, threadNum);

        LocalFile localFile = localFileMapper.selectByPrimaryKey(id);
        if (localFile == null) {
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("文件不存在");
        }

        localFile.setPushStartTime(new Date());

        Integer number = 0;
        List<CompletableFuture<Void>> futures = Lists.newArrayList();
        while (actionMark) {
            List<DaasCsosDataDTO> phoneSales = csosPhoneSaleMapper.getPushCsosDassData(id, minId);
            number += phoneSales.size();
            if (phoneSales.size() > 0) {
                DaasCsosDataDTO phoneSale = phoneSales.get(phoneSales.size() - 1);
                DaasCsosDataAdapDTO dto = new DaasCsosDataAdapDTO();
                dto.setDaasCsosDataDTOList(phoneSales);
                minId = phoneSale.getId();

                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        Result result = dassServiceClient.postCsosData(dto);
                        if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                            RetryMainLog mainLog = new RetryMainLog();
                            mainLog.setRetryType(1);
                            mainLog.setRetryParam(JSON.toJSONString(dto));
                            mainLog.setRetryParamType(dto.getClass().getName());
                            mainLog.setRetryService("dassServiceClient");
                            mainLog.setRetryMethod("postCsosData");
                            mainLog.setRetryNum(0);
                            mainLog.setRetryMaxNum(3);
                            mainLog.setRetryStatus(1);
                            mainLog.setCreateTime(new Date());
                            mainLog.setIncrId(redisChgService.incr(RedisKeyConstant.retryid));
                            retryMainLogMapper.insertSelective(mainLog);
                        }
                    } catch (Exception e) {
                        log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DAASERROR.getCode(),
                                "sftp文件推送Dass财富接口子线程异常，异常日志：" + e.getMessage()), e);
                    }
                }, pushDassThreadPool);
                futures.add(future);
            } else {
                actionMark = false;
            }
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        localFile.setPushEndTime(new Date());
        localFile.setPushNumber(number);
        localFileMapper.updateByPrimaryKeySelective(localFile);
        if (SftpFileTypeEnum.DX.getValue().equals(localFile.getFileType())) {
            StringBuilder content = new StringBuilder();
            content.append("apiCode：".concat(localFile.getApiCode()).concat("\r\n"))
                    .append("fileName：".concat(localFile.getFileName()).concat("\r\n"))
                    .append("数量：".concat(number.toString()).concat("\r\n"))
                    .append("文件推送dass(财富接口)结束".concat("\r\n"));
            alarmClient.sendAlarm(content.toString(), "Dass结果文件推送", AlarmSendCodeEnum.SUCCESS_UPLOAD.getCode());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public Result pushUpdateDassData(Long id) {
        Boolean actionMark = true;
        Long minId = null;
        Integer threadNum = ObjectUtils.isEmpty(marketingCommonConfig.getPushDassThreadNum()) ? 5 : marketingCommonConfig.getPushDassThreadNum();
        modifyThreadPool(pushDassThreadPool, threadNum);

        LocalFile localFile = localFileMapper.selectByPrimaryKey(id);
        if (localFile == null) {
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("文件不存在");
        }

        localFile.setPushStartTime(new Date());
        log.info("开始推送人工业务数据更新，文件ID: {}, 文件名: {}", id, localFile.getFileName());

        Integer totalNumber = 0;
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<CompletableFuture<Void>> futures = Lists.newArrayList();

        while (actionMark) {
            List<DaasUpdateDataDTO> updateDataList = updatePhoneSaleMapper.getPushUpdateDassData(id, minId);
            totalNumber += updateDataList.size();

            if (updateDataList.size() > 0) {
                DaasUpdateDataDTO lastUpdate = updateDataList.get(updateDataList.size() - 1);
                minId = lastUpdate.getId();

                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        // 批量处理结果统计
                        int batchSuccessCount = 0;
                        int batchFailCount = 0;
                        List<String> failMessages = new ArrayList<>();

                        for (DaasUpdateDataDTO updateData : updateDataList) {
                            try {
                                String requestId = "req_" + updateData.getUid() + "_" + updateData.getId() + "_" +
                                        DigestUtils.md5DigestAsHex((updateData.getUid() + updateData.getId()).getBytes()).substring(0, 8);
                                updateData.setRequestId(requestId);

                                // 调用接口
                                Result result = dassServiceClient.postWealthUpdateData(updateData);

                                // 处理单条数据的响应结果
                                if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                                    batchSuccessCount++;
                                    log.debug("数据更新成功: uid={}, requestId={}", updateData.getUid(), updateData.getRequestId());
                                } else {
                                    batchFailCount++;
                                    String errorMsg = "uid=" + updateData.getUid() + ": " + result.getMessage();
                                    failMessages.add(errorMsg);
                                    log.warn("人工业务数据更新失败：{}", errorMsg);
                                }
                            } catch (Exception e) {
                                batchFailCount++;
                                String errorMsg = "uid=" + updateData.getUid() + ": 处理异常 - " + e.getMessage();
                                failMessages.add(errorMsg);
                                log.error("处理单条数据异常：{}", errorMsg, e);
                            }
                        }

                        // 更新计数器
                        if (batchSuccessCount > 0) {
                            successCount.addAndGet(batchSuccessCount);
                        }
                        if (batchFailCount > 0) {
                            failCount.addAndGet(batchFailCount);
                        }

                        log.debug("批次推送结果，总数: {}, 成功: {}, 失败: {}",
                                updateDataList.size(), batchSuccessCount, batchFailCount);
                    } catch (Exception e) {
                        failCount.addAndGet(updateDataList.size());
                        log.error("推送人工业务数据更新接口子线程异常，批次大小: {}, 异常信息: {}",
                                updateDataList.size(), e.getMessage(), e);

                        // 发送告警
                        try {
                            alarmClient.sendAlarm(
                                String.format("人工业务数据更新推送异常，文件ID: %s, 批次大小: %d, 异常: %s",
                                            id, updateDataList.size(), e.getMessage()),
                                "人工业务数据更新推送异常",
                                AlarmSendCodeEnum.PUSHING_DAASERROR.getCode()
                            );
                        } catch (Exception alarmEx) {
                            log.error("发送告警失败", alarmEx);
                        }
                    }
                }, pushDassThreadPool);
                futures.add(future);
            } else {
                actionMark = false;
            }
        }

        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        localFile.setPushEndTime(new Date());
        localFile.setPushNumber(successCount.get());
        localFile.setErrorActualNumber(failCount.get());
        localFileMapper.updateByPrimaryKeySelective(localFile);

        // 推送完成后发送通知
        if (SftpFileTypeEnum.DX.getValue().equals(localFile.getFileType())) {
            StringBuilder content = new StringBuilder();
            content.append("apiCode：").append(localFile.getApiCode()).append("\r\n")
                    .append("fileName：").append(localFile.getFileName()).append("\r\n")
                    .append("总数量：").append(totalNumber).append("\r\n")
                    .append("成功数量：").append(successCount.get()).append("\r\n")
                    .append("失败数量：").append(failCount.get()).append("\r\n")
                    .append("成功率：").append(totalNumber > 0 ? String.format("%.2f%%", (double)successCount.get() / totalNumber * 100) : "0%").append("\r\n")
                    .append("人工业务数据更新接口推送结束").append("\r\n");

            AlarmSendCodeEnum alarmCode = failCount.get() == 0 ?
                AlarmSendCodeEnum.SUCCESS_UPLOAD : AlarmSendCodeEnum.PUSHING_DAASERROR;

            alarmClient.sendAlarm(content.toString(), "人工业务数据更新接口推送", alarmCode.getCode());
        }

        log.info("人工业务数据更新推送完成，文件ID: {}, 总数量: {}, 成功: {}, 失败: {}",
                id, totalNumber, successCount.get(), failCount.get());

        return new Result().setCode(ResultCode.SUCCESS.getValue())
                .setMessage(String.format("推送完成，总数量: %d, 成功: %d, 失败: %d",
                          totalNumber, successCount.get(), failCount.get()));
    }

    @Override
    public Result pushWeiZhongDassData(Long id) {
        String TITLE = "微众推人工 ";
        log.warn(TITLE + "开始，文件ID: {}", id);
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(10, 10);
        Boolean isContiue = false;
        LocalFile localFile = localFileMapper.selectByPrimaryKey(id);
        if (localFile == null) {
            log.warn(TITLE + "文件不存在, 文件ID: {}", id);
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("文件不存在").setDate(isContiue);
        }

        localFile.setPushStartTime(new Date());

        // 检查是否有数据需要处理
        Integer totalPhoneCount = phoneSaleMapper.getGroupByPhoneCount(String.valueOf(id));
        if (totalPhoneCount == null || totalPhoneCount == 0) {
            log.warn(TITLE + "未查询到分组数据 文件ID: {}", id);
            localFile.setPushEndTime(new Date());
            localFile.setPushNumber(0);
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("未查询到分组数据，id：" + id).setDate(isContiue);
        }

        log.warn(TITLE + "总共需要处理 {} 个手机号分组", totalPhoneCount);

        // 分页处理手机号分组
        Integer processedPhoneCount = 0;
        List<DassImportDataDTO> dataDTOS = new ArrayList<>();
        Integer phoneOffset = 0;

        try {
            while (phoneOffset < totalPhoneCount) {
                // 分页获取手机号分组
                List<String> phoneGroup = phoneSaleMapper.getGroupByPhoneWithPaging(String.valueOf(id), phoneOffset, PHONE_PAGE_SIZE);

                if (phoneGroup.isEmpty()) {
                    break;
                }

                log.warn(TITLE + "处理手机号分组，offset: {}, size: {}", phoneOffset, phoneGroup.size());

                // 处理当前批次的手机号
                for (String phone : phoneGroup) {
                    Boolean actionMark = true;
                    Long minId = null;
                    List<DassWeiZhongDTO> list = new ArrayList<>();
                    // 保存第一条记录用于后续处理
                    DassImportDataDTO firstDataDTO = null;

                    while (actionMark) {
                        List<DassImportDataDTO> phoneSales = phoneSaleMapper.getWeiZhongData(String.valueOf(id), phone, minId);
                        if (phoneSales.isEmpty()) {
                            actionMark = false;
                            continue;
                        }
                        // 保存第一条记录（只在第一次循环时保存）
                        if (firstDataDTO == null) {
                            firstDataDTO = phoneSales.get(0);
                        }
                        // 处理当前批次的所有记录，将5个字段合并到list中
                        for (DassImportDataDTO dataDTO : phoneSales) {
                            String extend = dataDTO.getExtend();
                            if (StringUtils.isNotBlank(extend)) {
                                try {
                                    JSONObject jsonParam = JSON.parseObject(extend);
                                    DassWeiZhongDTO dassWeiZhongDTO = new DassWeiZhongDTO();
                                    dassWeiZhongDTO.setAudit_time(jsonParam.getString("audit_time") == null ? "" : jsonParam.getString("audit_time"));
                                    dassWeiZhongDTO.setQualifyscore(jsonParam.getString("qualifyscore") == null ? "" : jsonParam.getString("qualifyscore"));
                                    dassWeiZhongDTO.setAuditRate(jsonParam.getString("auditRate") == null ? "" : jsonParam.getString("auditRate"));
                                    dassWeiZhongDTO.setActivity(jsonParam.getString("activity") == null ? "" : jsonParam.getString("activity"));
                                    dassWeiZhongDTO.setRegion(jsonParam.getString("region") == null ? "" : jsonParam.getString("region"));
                                    list.add(dassWeiZhongDTO);
                                } catch (Exception e) {
                                    log.warn(TITLE + "解析extend字段异常，跳过该记录: {}", extend, e);
                                }
                            }
                        }
                        minId = phoneSales.get(phoneSales.size() - 1).getId();
                    }

                    // 只有当找到了数据才进行处理
                    if (firstDataDTO != null && !list.isEmpty()) {
                        // 将合并后的5个字段列表转换为JSON数组格式
                        JSONArray couponsArray = new JSONArray();
                        for (DassWeiZhongDTO dto : list) {
                            JSONObject couponObj = new JSONObject();
                            couponObj.put("audit_time", dto.getAudit_time());
                            couponObj.put("qualifyscore", dto.getQualifyscore());
                            couponObj.put("auditRate", dto.getAuditRate());
                            couponObj.put("activity", dto.getActivity());
                            couponObj.put("region", dto.getRegion());
                            couponsArray.add(couponObj);
                        }

                        // 更新第一条记录的extend字段，加入合并后的优惠券列表
                        try {
                            JSONObject jsonObject = JSON.parseObject(firstDataDTO.getExtend());
                            if (jsonObject == null) {
                                jsonObject = new JSONObject();
                            }
                            jsonObject.put("couponsList", couponsArray);
                            firstDataDTO.setExtend(jsonObject.toJSONString());
                            dataDTOS.add(firstDataDTO);
                            // 每处理一个手机号就+1
                            processedPhoneCount++;
                        } catch (Exception e) {
                            log.error(TITLE + "构建合并数据异常，跳过该手机号: {}", phone, e);
                        }
                    }

                    // 达到批次大小时推送数据
                    if (dataDTOS.size() >= BATCH_SIZE) {
                        DassImportAdapDTO dto = new DassImportAdapDTO();
                        dto.setInterfaceExtendInfo(id.toString());
                        dto.setList(new ArrayList<>(dataDTOS));
                        threadPool.submit(() -> {
                            try {
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
                            } catch (Exception e) {
                                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DAASERROR.getCode(),
                                        TITLE + "sftp文件推送Dass子线程异常，异常日志：" + e.getMessage()), e);
                            }
                        });
                        dataDTOS.clear();
                    }
                }

                phoneOffset += PHONE_PAGE_SIZE;
            }

            // 推送剩余数据
            if (!dataDTOS.isEmpty()) {
                DassImportAdapDTO dto = new DassImportAdapDTO();
                dto.setInterfaceExtendInfo(id.toString());
                dto.setList(new ArrayList<>(dataDTOS));
                threadPool.submit(() -> {
                    try {
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
                    } catch (Exception e) {
                        log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DAASERROR.getCode(),
                                TITLE + "sftp文件推送Dass子线程异常，异常日志：" + e.getMessage()), e);
                    }
                });
                dataDTOS.clear();
            }

        } catch (Exception ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.WEIZHONG_SERVICEERROR.getCode(), TITLE + "业务异常！"), ex);
        }
        // 关闭线程池
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.warn(TITLE + "等待线程池结束");
            }
        } catch (InterruptedException ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.WEIZHONG_SERVICEERROR.getCode(), TITLE + "线程池停止异常！"), ex);
            Thread.currentThread().interrupt();
        }

        localFile.setPushEndTime(new Date());
        localFile.setPushNumber(processedPhoneCount);
        localFileMapper.updateByPrimaryKeySelective(localFile);
        
        if (SftpFileTypeEnum.DX.getValue().equals(localFile.getFileType())) {
            StringBuilder content = new StringBuilder();
            content.append("apiCode：".concat(localFile.getApiCode()).concat("\r\n"))
                    .append("fileName：".concat(localFile.getFileName()).concat("\r\n"))
                    .append("数量：".concat(processedPhoneCount.toString()).concat("\r\n"))
                    .append("微众文件推送dass结束".concat("\r\n"));
            alarmClient.sendAlarm(content.toString(), "微众Dass结果文件推送", AlarmSendCodeEnum.SUCCESS_UPLOAD.getCode());
        }

        log.warn(TITLE + "推送完成，总手机号: {}, 处理成功: {}", totalPhoneCount, processedPhoneCount);
        
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(isContiue);
    }
    
    @Override
    public Result pushSpecialDassData(Long id, String filePrefix, JSONObject prefixConfig) {
        String TITLE = "特殊文件推人工[" + filePrefix + "] ";
        log.warn(TITLE + "开始，文件ID: {}, 配置: {}", id, prefixConfig);
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(10, 10);
        Boolean isContiue = false;
        LocalFile localFile = localFileMapper.selectByPrimaryKey(id);
        if (localFile == null) {
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("文件不存在").setDate(isContiue);
        }
        localFile.setPushStartTime(new Date());

        // 检查是否有数据需要处理
        Integer totalPhoneCount = phoneSaleMapper.getGroupByPhoneCount(String.valueOf(id));
        if (totalPhoneCount == null || totalPhoneCount == 0) {
            localFile.setPushEndTime(new Date());
            localFile.setPushNumber(0);
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("未查询到分组数据，id：" + id).setDate(isContiue);
        }
        log.warn(TITLE + "总共需要处理 {} 个手机号分组", totalPhoneCount);

        // 分页处理手机号分组
        Integer processedPhoneCount = 0;
        List<DassImportDataDTO> dataDTOS = new ArrayList<>();
        Integer phoneOffset = 0;

        try {
            while (phoneOffset < totalPhoneCount) {
                // 分页获取手机号分组
                List<String> phoneGroup = phoneSaleMapper.getGroupByPhoneWithPaging(String.valueOf(id), phoneOffset, PHONE_PAGE_SIZE);

                if (phoneGroup.isEmpty()) {
                    break;
                }
                log.warn(TITLE + "处理手机号分组，offset: {}, size: {}", phoneOffset, phoneGroup.size());

                // 处理当前批次的手机号
                for (String phone : phoneGroup) {
                    // 一次性查询该手机号的所有记录（按create_time排序，第一条就是最早的）
                    List<DassImportDataDTO> phoneSales = phoneSaleMapper.getSpecialDataAll(String.valueOf(id), phone);
                    if (phoneSales.isEmpty()) {
                        continue;
                    }
                    
                    // 第一条记录（create_time最早的）作为基准
                    DassImportDataDTO firstDataDTO = phoneSales.get(0);
                    
                    // 解析第一条记录的extend作为基础数据
                    JSONObject firstExtend = JSON.parseObject(firstDataDTO.getExtend());
                    if (firstExtend == null) {
                        firstExtend = new JSONObject();
                    }
                    
                    // 用于存储合并的数据
                    JSONObject mergedDataMap = new JSONObject();
                    
                    // 遍历所有记录，合并extend字段
                    if (prefixConfig != null && !prefixConfig.isEmpty()) {
                        // 初始化合并数据结构
                        for (String configKey : prefixConfig.keySet()) {
                            mergedDataMap.put(configKey, new JSONArray());
                        }
                        
                        // 遍历配置中的所有字段组（如"list"、"couponsList"等）
                        for (String configKey : prefixConfig.keySet()) {
                            List<String> fieldNames = prefixConfig.getJSONArray(configKey).toJavaList(String.class);
                            JSONArray mergedArray = mergedDataMap.getJSONArray(configKey);
                            
                            // 遍历所有记录，从每条记录的extend中提取配置的字段
                            for (DassImportDataDTO dataDTO : phoneSales) {
                                String extend = dataDTO.getExtend();
                                if (StringUtils.isNotBlank(extend)) {
                                    try {
                                        JSONObject jsonParam = JSON.parseObject(extend);
                                        JSONObject extractedData = new JSONObject();
                                        
                                        // 提取配置中指定的字段
                                        for (String fieldName : fieldNames) {
                                            String value = jsonParam.getString(fieldName);
                                            extractedData.put(fieldName, value == null ? "" : value);
                                        }
                                        
                                        // 将提取的字段添加到合并数组中
                                        mergedArray.add(extractedData);
                                    } catch (Exception e) {
                                        log.warn(TITLE + "解析extend字段异常，跳过该记录: {}", extend, e);
                                    }
                                }
                            }
                        }
                    }
                    
                    // 处理完所有记录后，组装最终数据
                    try {
                        // 将合并后的数组放入第一条记录的extend中
                        for (String configKey : mergedDataMap.keySet()) {
                            firstExtend.put(configKey, mergedDataMap.getJSONArray(configKey));
                            
                            // 移除已经合并到数组中的字段，避免重复
                            List<String> fieldNames = prefixConfig.getJSONArray(configKey).toJavaList(String.class);
                            for (String fieldName : fieldNames) {
                                firstExtend.remove(fieldName);
                            }
                        }
                        
                        // 更新第一条记录的extend字段
                        firstDataDTO.setExtend(firstExtend.toJSONString());
                        dataDTOS.add(firstDataDTO);
                        // 每处理一个手机号就+1
                        processedPhoneCount++;
                    } catch (Exception e) {
                        log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DAASERROR.getCode(),
                                TITLE + "构建合并数据异常，跳过该手机号: " + phone + ", 配置:" + prefixConfig + e.getMessage()), e);
                    }

                    // 达到批次大小时推送数据
                    if (dataDTOS.size() >= BATCH_SIZE) {
                        DassImportAdapDTO dto = new DassImportAdapDTO();
                        dto.setInterfaceExtendInfo(id.toString());
                        dto.setList(new ArrayList<>(dataDTOS));
                        threadPool.submit(() -> {
                            try {
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
                            } catch (Exception e) {
                                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DAASERROR.getCode(),
                                        TITLE + "sftp文件推送Dass子线程异常，异常日志：" + e.getMessage()), e);
                            }
                        });
                        dataDTOS.clear();
                    }
                }
                phoneOffset += PHONE_PAGE_SIZE;
            }

            // 推送剩余数据
            if (!dataDTOS.isEmpty()) {
                DassImportAdapDTO dto = new DassImportAdapDTO();
                dto.setInterfaceExtendInfo(id.toString());
                dto.setList(new ArrayList<>(dataDTOS));
                threadPool.submit(() -> {
                    try {
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
                    } catch (Exception e) {
                        log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DAASERROR.getCode(),
                                TITLE + "sftp文件推送Dass子线程异常，异常日志：" + e.getMessage()), e);
                    }
                });
                dataDTOS.clear();
            }
        } catch (Exception ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DAASERROR.getCode(), TITLE + "业务异常！"), ex);
        }
        // 关闭线程池
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.warn(TITLE + "等待线程池结束");
            }
        } catch (InterruptedException ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DAASERROR.getCode(), TITLE + "线程池停止异常！"), ex);
            Thread.currentThread().interrupt();
        }
        localFile.setPushEndTime(new Date());
        localFile.setPushNumber(processedPhoneCount);
        localFileMapper.updateByPrimaryKeySelective(localFile);
        sendSpecialFileAlarm(localFile, filePrefix, processedPhoneCount);
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(isContiue);
    }

    /**
     * 发送特殊文件推送告警
     */
    private void sendSpecialFileAlarm(LocalFile localFile, String filePrefix, Integer processedCount) {
        if (SftpFileTypeEnum.DX.getValue().equals(localFile.getFileType())) {
            StringBuilder content = new StringBuilder();
            content.append("apiCode：").append(localFile.getApiCode()).append("\r\n")
                    .append("fileName：").append(localFile.getFileName()).append("\r\n")
                    .append("数量：").append(processedCount).append("\r\n")
                    .append("特殊文件[").append(filePrefix).append("]推送dass结束\r\n");
            alarmClient.sendAlarm(content.toString(), "特殊文件Dass结果文件推送", AlarmSendCodeEnum.SUCCESS_UPLOAD.getCode());
        }
    }

    @Override
    public Result pushDynamicGroupDassData(Long id, String filePrefix, JSONObject prefixConfig) {
        String TITLE = "动态分组文件推人工[" + filePrefix + "] ";
        log.warn(TITLE + "开始，文件ID: {}, 配置: {}", id, prefixConfig);
        
        JSONObject groupByConfig = prefixConfig.getJSONObject("groupByField");
        if (groupByConfig == null) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("配置错误：缺少groupByField配置");
        }
        
        String groupFieldName = groupByConfig.getString("fieldName");
        String groupFieldSource = groupByConfig.getString("fieldSource");
        JSONObject mergeConfig = prefixConfig.getJSONObject("mergeConfig");
        
        if (StringUtils.isBlank(groupFieldName) || StringUtils.isBlank(groupFieldSource)) {
            return new Result().setCode(ResultCode.FAIL.getValue())
                    .setMessage("配置错误：groupByField.fieldName或fieldSource为空");
        }
        
        if ("base".equals(groupFieldSource)) {
            return pushByBaseField(id, filePrefix, groupFieldName, mergeConfig);
        } else if ("extend".equals(groupFieldSource)) {
            return pushByExtendField(id, filePrefix, groupFieldName, mergeConfig);
        } else {
            return new Result().setCode(ResultCode.FAIL.getValue())
                    .setMessage("不支持的字段来源类型: " + groupFieldSource);
        }
    }

    /**
     * 基础字段分组处理
     */
    private Result pushByBaseField(Long id, String filePrefix, String groupFieldName, JSONObject mergeConfig) {
        String TITLE = "基础字段分组[" + groupFieldName + "] ";
        log.warn(TITLE + "开始处理，文件ID: {}", id);
        
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(10, 10);
        Boolean isContiue = false;
        
        LocalFile localFile = localFileMapper.selectByPrimaryKey(id);
        if (localFile == null) {
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("文件不存在").setDate(isContiue);
        }
        localFile.setPushStartTime(new Date());
        
        // 获取分组总数（使用动态字段）
        Integer totalGroupCount = phoneSaleMapper.getGroupByFieldCount(String.valueOf(id), groupFieldName);
        
        if (totalGroupCount == null || totalGroupCount == 0) {
            localFile.setPushEndTime(new Date());
            localFile.setPushNumber(0);
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("未查询到分组数据").setDate(isContiue);
        }
        
        log.warn(TITLE + "总共需要处理 {} 个分组", totalGroupCount);
        
        Integer processedCount = 0;
        List<DassImportDataDTO> dataDTOS = new ArrayList<>();
        Integer groupOffset = 0;
        
        try {
            while (groupOffset < totalGroupCount) {
                // 分页获取分组值
                List<String> groupValues = phoneSaleMapper.getGroupByFieldWithPaging(
                        String.valueOf(id), groupFieldName, groupOffset, PHONE_PAGE_SIZE);
                
                if (groupValues.isEmpty()) {
                    break;
                }
                
                log.warn(TITLE + "处理分组，offset: {}, size: {}", groupOffset, groupValues.size());
                
                for (String groupValue : groupValues) {
                    // 获取该分组的所有记录
                    List<DassImportDataDTO> records = phoneSaleMapper.getDataByGroupField(
                            String.valueOf(id), groupFieldName, groupValue);
                    
                    if (records.isEmpty()) {
                        continue;
                    }
                    
                    // 合并数据（复用现有逻辑）
                    DassImportDataDTO mergedData = mergeRecords(records, mergeConfig, TITLE);
                    if (mergedData != null) {
                        dataDTOS.add(mergedData);
                        processedCount++;
                    }
                    
                    // 批量推送
                    if (dataDTOS.size() >= BATCH_SIZE) {
                        pushBatchData(dataDTOS, id, threadPool, TITLE);
                        dataDTOS.clear();
                    }
                }
                groupOffset += PHONE_PAGE_SIZE;
            }
            
            // 推送剩余数据
            if (!dataDTOS.isEmpty()) {
                pushBatchData(dataDTOS, id, threadPool, TITLE);
                dataDTOS.clear();
            }
            
        } catch (Exception ex) {
            log.warn(AlertLog.buildErrorMessage(
                    AlarmSendCodeEnum.PUSHING_DAASERROR.getCode(), 
                    TITLE + "业务异常！"), ex);
        }
        
        // 关闭线程池并等待完成
        shutdownThreadPool(threadPool, TITLE);
        
        localFile.setPushEndTime(new Date());
        localFile.setPushNumber(processedCount);
        localFileMapper.updateByPrimaryKeySelective(localFile);
        sendSpecialFileAlarm(localFile, filePrefix, processedCount);
        
        log.warn(TITLE + "推送完成，处理数量: {}", processedCount);
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(isContiue);
    }

    /**
     * Extend字段分组处理
     */
    private Result pushByExtendField(Long id, String filePrefix, String groupFieldName, JSONObject mergeConfig) {
        String TITLE = "Extend字段分组[" + groupFieldName + "] ";
        log.warn(TITLE + "开始处理，文件ID: {}", id);
        
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(10, 10);
        Boolean isContiue = false;
        
        LocalFile localFile = localFileMapper.selectByPrimaryKey(id);
        if (localFile == null) {
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("文件不存在").setDate(isContiue);
        }
        localFile.setPushStartTime(new Date());
        
        // 查询所有数据（因为需要从 extend JSON 中提取分组字段）
        List<DassImportDataDTO> allRecords = phoneSaleMapper.getAllDataByFileId(String.valueOf(id));
        
        if (allRecords.isEmpty()) {
            localFile.setPushEndTime(new Date());
            localFile.setPushNumber(0);
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("未查询到数据").setDate(isContiue);
        }
        
        // 手动分组：从 extend 中提取分组字段值
        LinkedHashMap<String, List<DassImportDataDTO>> groupedData = new LinkedHashMap<>();
        for (DassImportDataDTO record : allRecords) {
            String extend = record.getExtend();
            if (StringUtils.isBlank(extend)) {
                continue;
            }
            
            try {
                JSONObject extendJson = JSON.parseObject(extend);
                String groupValue = extendJson.getString(groupFieldName);
                
                if (StringUtils.isNotBlank(groupValue)) {
                    groupedData.computeIfAbsent(groupValue, k -> new ArrayList<>()).add(record);
                }
            } catch (Exception e) {
                log.warn(TITLE + "解析extend字段异常，跳过该记录", e);
            }
        }
        
        log.warn(TITLE + "总共需要处理 {} 个分组", groupedData.size());
        
        Integer processedCount = 0;
        List<DassImportDataDTO> dataDTOS = new ArrayList<>();
        
        try {
            for (Map.Entry<String, List<DassImportDataDTO>> entry : groupedData.entrySet()) {
                List<DassImportDataDTO> records = entry.getValue();
                
                // 合并数据（复用现有逻辑）
                DassImportDataDTO mergedData = mergeRecords(records, mergeConfig, TITLE);
                if (mergedData != null) {
                    dataDTOS.add(mergedData);
                    processedCount++;
                }
                
                // 批量推送
                if (dataDTOS.size() >= BATCH_SIZE) {
                    pushBatchData(dataDTOS, id, threadPool, TITLE);
                    dataDTOS.clear();
                }
            }
            
            // 推送剩余数据
            if (!dataDTOS.isEmpty()) {
                pushBatchData(dataDTOS, id, threadPool, TITLE);
                dataDTOS.clear();
            }
            
        } catch (Exception ex) {
            log.warn(AlertLog.buildErrorMessage(
                    AlarmSendCodeEnum.PUSHING_DAASERROR.getCode(),
                    TITLE + "业务异常！"), ex);
        }
        
        // 关闭线程池并等待完成
        shutdownThreadPool(threadPool, TITLE);
        
        localFile.setPushEndTime(new Date());
        localFile.setPushNumber(processedCount);
        localFileMapper.updateByPrimaryKeySelective(localFile);
        sendSpecialFileAlarm(localFile, filePrefix, processedCount);
        
        log.warn(TITLE + "推送完成，处理数量: {}", processedCount);
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(isContiue);
    }

    /**
     * 合并记录（复用现有逻辑）
     */
    private DassImportDataDTO mergeRecords(List<DassImportDataDTO> records, JSONObject mergeConfig, String TITLE) {
        if (records.isEmpty()) {
            return null;
        }
        
        // 第一条记录作为基准
        DassImportDataDTO firstDataDTO = records.get(0);
        JSONObject firstExtend = JSON.parseObject(firstDataDTO.getExtend());
        if (firstExtend == null) {
            firstExtend = new JSONObject();
        }
        
        // 用于存储合并的数据
        JSONObject mergedDataMap = new JSONObject();
        
        if (mergeConfig != null && !mergeConfig.isEmpty()) {
            // 初始化合并数据结构
            for (String configKey : mergeConfig.keySet()) {
                mergedDataMap.put(configKey, new JSONArray());
            }
            
            // 遍历配置中的所有字段组
            for (String configKey : mergeConfig.keySet()) {
                List<String> fieldNames = mergeConfig.getJSONArray(configKey).toJavaList(String.class);
                JSONArray mergedArray = mergedDataMap.getJSONArray(configKey);
                
                // 遍历所有记录，提取字段
                for (DassImportDataDTO dataDTO : records) {
                    String extend = dataDTO.getExtend();
                    if (StringUtils.isNotBlank(extend)) {
                        try {
                            JSONObject jsonParam = JSON.parseObject(extend);
                            JSONObject extractedData = new JSONObject();
                            
                            for (String fieldName : fieldNames) {
                                String value = jsonParam.getString(fieldName);
                                extractedData.put(fieldName, value == null ? "" : value);
                            }
                            
                            mergedArray.add(extractedData);
                        } catch (Exception e) {
                            log.warn(TITLE + "解析extend字段异常，跳过该记录: {}", extend, e);
                        }
                    }
                }
            }
            
            // 将合并后的数组放入 extend 中
            for (String configKey : mergedDataMap.keySet()) {
                firstExtend.put(configKey, mergedDataMap.getJSONArray(configKey));
                
                // 移除已合并的字段
                List<String> fieldNames = mergeConfig.getJSONArray(configKey).toJavaList(String.class);
                for (String fieldName : fieldNames) {
                    firstExtend.remove(fieldName);
                }
            }
            
            firstDataDTO.setExtend(firstExtend.toJSONString());
        }
        
        return firstDataDTO;
    }

    /**
     * 批量推送数据
     */
    private void pushBatchData(List<DassImportDataDTO> dataDTOS, Long fileId,
                              ThreadPoolExecutor threadPool, String TITLE) {
        DassImportAdapDTO dto = new DassImportAdapDTO();
        dto.setInterfaceExtendInfo(fileId.toString());
        dto.setList(new ArrayList<>(dataDTOS));
        
        threadPool.submit(() -> {
            try {
                Result result = dassServiceClient.postHermesUserData(dto);
                if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                    // 记录重试日志
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
            } catch (Exception e) {
                log.warn(AlertLog.buildErrorMessage(
                        AlarmSendCodeEnum.PUSHING_DAASERROR.getCode(),
                        TITLE + "推送异常：" + e.getMessage()), e);
            }
        });
    }

    /**
     * 关闭线程池
     */
    private void shutdownThreadPool(ThreadPoolExecutor threadPool, String TITLE) {
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.warn(TITLE + "等待线程池结束");
            }
        } catch (InterruptedException ex) {
            log.warn(AlertLog.buildErrorMessage(
                    AlarmSendCodeEnum.PUSHING_DAASERROR.getCode(),
                    TITLE + "线程池停止异常！"), ex);
            Thread.currentThread().interrupt();
        }
    }
}
