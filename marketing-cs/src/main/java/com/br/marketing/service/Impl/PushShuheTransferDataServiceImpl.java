package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.common.encryption.Md5Utils;
import com.br.marketing.adapter.transfer.TransferSyncAdapter;
import com.br.marketing.adapter.transfer.adaptee.CaseShuheUserAdaptee;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.dto.PushShDXDTO;
import com.br.marketing.dto.ResponseCustomDTO;
import com.br.marketing.dto.shuhe.ResponseShuheDTO;
import com.br.marketing.dto.shuhe.ShuheTransferJsonDTO;
import com.br.marketing.dto.shuhe.factory.CaseShuheUserFactory;
import com.br.marketing.dto.shuhe.factory.UserTypeStrategyFactory;
import com.br.marketing.dto.shuhe.strategy.CuShenWan;
import com.br.marketing.dto.shuhe.strategy.IUserType;
import com.br.marketing.dto.shuhe.strategy.UnknownUserType;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.CaseShuheUserMapper;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.MarketingTransferInfoMapper;
import com.br.marketing.mapper.PhoneBlackMapper;
import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.ShuHeProcessHandlerContext;
import com.br.marketing.origin.TransferSource;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.service.IPushShuheTransferDataService;
import com.br.marketing.service.ITransferSyncUserService;
import com.br.marketing.service.PushDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

/**
 * 数禾转化实现类
 *
 * @author Guo Zeqiang
 * @dateTime 2022/2/10 14:25
 */
@Service
@Slf4j
public class PushShuheTransferDataServiceImpl implements IPushShuheTransferDataService {

    @Resource
    private CaseShuheUserMapper caseShuheUserMapper;
    @Resource
    private IMarketingSyncUserService iMarketingSyncUserService;
    @Resource
    private ITransferSyncUserService iTransferSyncUserService;
    @Resource
    private PushDataService pushDataService;
    @Resource
    private RedisChgService redisChgService;
    @Resource
    private MarketingTransferInfoMapper marketingTransferInfoMapper;
    @Resource
    private LocalFileMapper localFileMapper;
    @Resource
    private PhoneBlackMapper phoneBlackMapper;
    @Resource
    private RabbitMqProducter producter;
    @Resource
    private TableCreateServiceImpl tableCreateService;
    @Resource
    private AlarmApiClient alarmClient;
    @Value("${otherConfig.alarm.secretKey:00}")
    private String secretKey;
    @Value("${otherConfig.alarm.appName:00}")
    private String appName;

    private static final ThreadPoolExecutor BR_EXECUTORS = BrExecutors.getThreadPool(1, 2);
    private final String title = "数禾转化数据定制化清洗入库";
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public ResponseShuheDTO insertShuheTransferData(String apiCode, String jsonData) {
        long first = System.currentTimeMillis();
        ResponseShuheDTO responseShuheDTO = new ResponseShuheDTO();
        ShuheTransferJsonDTO jsonDTO = null;
        try {
            jsonDTO = JSONObject.parseObject(jsonData, new TypeReference<ShuheTransferJsonDTO>() {
            }.getType());
            String msg = nonNullCheck(jsonDTO);
            if (!"".equals(msg)) {
                responseShuheDTO.failed("抱歉,缺失必填参数！缺失参数为：".concat(msg));
                msg = "缺失必填参数:".concat(msg).concat("\napiCode“").concat(apiCode).concat("”\nuserType“")
                        .concat(jsonDTO.getBizType()).concat("”\n案件编号“").concat(jsonDTO.getOrderId())
                        .concat("”\n").concat("请及时跟进或与数禾客户及时沟通^_^");
                log.warn("shuhe-1:{}", msg);
                this.sendAlarmMgs(title, msg, appName, secretKey, alarmClient);
                return responseShuheDTO;
            }
            String userType = jsonDTO.getBizType();
            if (StringUtils.isEmpty(userType)) {
                /*
                 * 对bizType字段做兜底，对应营销userType,
                 * 当bizType未传时，需要主动去上传接口中查找，
                 * 如果未查到需要返回给客户提示信息，并将数据落库到本地
                 */
                userType = iMarketingSyncUserService.getUserTypeLatestByCustNum(apiCode, jsonDTO.getOrderId());
            }
            final ShuheTransferJsonDTO finalJsonDTO = jsonDTO;
            Future<String> futureTaskId = BR_EXECUTORS.submit(() -> iMarketingSyncUserService.getTaskIdLatestByCustNum(
                    apiCode, finalJsonDTO.getOrderId(), finalJsonDTO.getBizType()));
            final IUserType iUserType = UserTypeStrategyFactory.getUserTypeStrategy(userType);
            CaseShuheUser caseShuheUser = CaseShuheUserFactory.newInstance().getCaseShuheUser(
                    iUserType, jsonDTO, apiCode, jsonData);
            String taskId;
            if (futureTaskId.isDone()) {
                taskId = futureTaskId.get();
            } else {
                futureTaskId.isCancelled();
                taskId = iMarketingSyncUserService.getTaskIdLatestByCustNum(
                        apiCode, jsonDTO.getOrderId(), userType);
            }
            MarketingTransferSyncUser transferSyncUser = new TransferSyncAdapter((CaseShuheUserAdaptee) caseShuheUser)
                    .transferSyncUserRequest(taskId);
            List<Future<String>> futureList = new ArrayList<>();
            if (iUserType instanceof UnknownUserType) {
                msg = "未知的业务类型\"" + userType + "\"!";
                responseShuheDTO.failed("抱歉,".concat(msg));
                caseShuheUser.setErrorInfo("#1" + responseShuheDTO.getDesc());
                log.warn("shuhe-2:{}", responseShuheDTO.getDesc());
                this.sendAlarmMgs(title, msg.concat("\napiCode“").concat(apiCode).concat("”\n案件编号“")
                                .concat(jsonDTO.getOrderId()).concat("”\n").concat("请及时跟进或与数禾客户及时沟通^_^")
                        , appName, secretKey, alarmClient);
            } else {
                responseShuheDTO.success();
                caseShuheUser.setErrorInfo("");
                // 按规则推送数据
                asyncPushByRule(caseShuheUser, iUserType, transferSyncUser, futureList);
            }
            // 转化信息入转化标准库
            asyncSaveTransfer(caseShuheUser, transferSyncUser, futureList);
            // 获取异步执行结果
            getAsyncResults(caseShuheUser, futureList);
            // D20220209数禾转化数据定制化清洗入库
            int row = caseShuheUserMapper.insertSelective(caseShuheUser);
            if (row < 1) {
                msg = "数禾推送数据保存失败！";
                this.sendAlarmMgs(title, msg.concat("\napiCode“").concat(apiCode).concat("”\nuserType“")
                        .concat(userType).concat("”\n案件编号“").concat(jsonDTO.getOrderId()).concat("”\n")
                        .concat("请尽快处理^_^"), appName, secretKey, alarmClient);
                log.error(msg);
                responseShuheDTO.failed("抱歉，".concat(msg));
                faultTolerantInsert(jsonDTO, caseShuheUser.getIsTransfer(), jsonData, apiCode, msg);
            }
            long last = System.currentTimeMillis();
            log.warn("接收数禾转化数据(apiCode={};custNum={};userType={})共耗时:{}ms"
                    , last - first, apiCode, jsonDTO.getOrderId(), jsonDTO.getBizType());
            return responseShuheDTO;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            exceptionSave(jsonDTO, jsonData, apiCode, e);
            return responseShuheDTO.failed();
        }
    }

    private String nonNullCheck(ShuheTransferJsonDTO jsonDTO) {
        String msg = "";
        if (StringUtils.isEmpty(jsonDTO.getOrderId())) {
            msg += "orderId,释义：批量上传案件编号；";
        }
        if (StringUtils.isEmpty(jsonDTO.getMobile())) {
            msg += "mobile,释义：手机号；";
        }
        if (jsonDTO.getDataItem() == null || jsonDTO.getDataItem().size() < 1) {
            msg += "dataItem,释义：扩展字段；";
        }
        return msg;
    }

    /**
     * 异常保存，只保存必要字段
     */
    private void exceptionSave(ShuheTransferJsonDTO jsonDTO, String jsonData
            , String apiCode, Exception e) {
        CaseShuheUser user = new CaseShuheUser();
        user.setJsonData(jsonData);
        user.setApiCode(apiCode);
        user.setCreateTime(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        user.setUploadDate(LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE));
        user.setErrorInfo("#5".concat(e.toString()));
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        user.setStatus(2);
        if (jsonDTO != null) {
            user.setMobile(jsonDTO.getMobile());
            user.setBiztype(jsonDTO.getBizType());
            user.setCustNum(jsonDTO.getOrderId());
        }
        try {
            caseShuheUserMapper.insertSelective(user);
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
            this.sendAlarmMgs(title, ("apiCode“").concat(apiCode)
                    .concat("”\n").concat(exception.toString()), appName, secretKey, alarmClient);
        }
        this.sendAlarmMgs(title, ("apiCode“").concat(apiCode)
                .concat("”\n").concat(e.toString()), appName, secretKey, alarmClient);
    }

    /**
     * 保存失败后容错保存，只保存必要字段
     */
    private void faultTolerantInsert(ShuheTransferJsonDTO jsonDTO, int isTransfer, String jsonData
            , String apiCode, String msg) {
        CaseShuheUser caseShuheUser = new CaseShuheUser();
        caseShuheUser.setSaveStatus(1);
        caseShuheUser.setJsonData(jsonData);
        caseShuheUser.setCustNum(jsonDTO.getOrderId());
        caseShuheUser.setApiCode(apiCode);
        caseShuheUser.setMobile(jsonDTO.getMobile());
        caseShuheUser.setBiztype(jsonDTO.getBizType());
        caseShuheUser.setErrorInfo("#4" + caseShuheUser.getErrorInfo() + (";").concat(msg));
        caseShuheUser.setIsTransfer(isTransfer);
        caseShuheUser.setCreateTime(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        caseShuheUser.setUploadDate(LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE));
        caseShuheUserMapper.insertSelective(caseShuheUser);
    }

    private void getAsyncResults(CaseShuheUser caseShuheUser, List<Future<String>> futureList) {
        for (Future<String> future : futureList) {
            try {
                String stat = future.get(10, TimeUnit.SECONDS);
                if (StringUtils.isEmpty(stat)) {
                    continue;
                }
                String msg = "数禾异步推送异常，异常逻辑：" + stat;
                this.sendAlarmMgs(title, msg.concat("\napiCode“").concat(caseShuheUser.getApiCode())
                        .concat("”\nuserType“").concat(caseShuheUser.getUserType())
                        .concat("”\n案件编号“").concat(caseShuheUser.getCustNum())
                        .concat("”\n").concat("尽快处理^_^"), appName, secretKey, alarmClient);
                log.error(msg);
                String errorInfo = caseShuheUser.getErrorInfo();
                if (StringUtils.isEmpty(errorInfo)) {
                    caseShuheUser.setErrorInfo("#2" + errorInfo + stat);
                } else {
                    caseShuheUser.setErrorInfo("#2" + errorInfo + (";").concat(stat));
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error("shuhe-3:isDone=" + future.isDone() + (e.getMessage()), e);
                this.sendAlarmMgs(title, ("apiCode“").concat(caseShuheUser.getApiCode())
                                .concat("”\nuserType“").concat(caseShuheUser.getUserType())
                                .concat("”\n案件编号“").concat(caseShuheUser.getCustNum()).concat("”\n")
                                .concat("当前完成情况:" + future.isDone() + "\n")
                                .concat("异步推送任务异常，可能未获取异步返回结果…^_^，原因：\n") + e
                        , appName, secretKey, alarmClient);
                caseShuheUser.setErrorInfo("#3@:isDone=" + future.isDone() + (e));
            }
        }
    }

    private void setCid(MarketingTransferSyncUser transferSyncUser) {
        String key = "marketing:api:shuhe:transfer:cid:".concat(transferSyncUser.getApiCode());
        String cId;
        try {
            cId = redisChgService.get(key);
            if (StringUtils.isEmpty(cId)) {
                cId = tableCreateService.getCId(transferSyncUser.getApiCode());
                // 缓存七天
                redisChgService.setex(key, cId, 7 * 86400);
            }
        } catch (Exception e) {
            cId = tableCreateService.getTcId(transferSyncUser.getApiCode());
            log.error(e.getMessage(), e);
        }
        transferSyncUser.setCid(cId);
        transferSyncUser.settCid(cId.replaceFirst("-", ""));
    }

    /**
     * 按规则异步推送
     */
    private void asyncPushByRule(CaseShuheUser caseShuheUser, IUserType iUserType
            , MarketingTransferSyncUser transferSyncUser, List<Future<String>> futureList) {
        Date creatTime = iMarketingSyncUserService.getCreatTimeByCustNumAndUserType(caseShuheUser.getApiCode()
                , caseShuheUser.getCustNum(), caseShuheUser.getUserType());
        // D20220221数禾定制版V3.0优化一期 检验有数据有效期
        if (iUserType.dataPeriodOfValidity(caseShuheUser, iMarketingSyncUserService, creatTime)) {
            // D20220209数禾转化接口V3.0-客服
            if (iUserType.isBlack(caseShuheUser)) {
                // 黑名单逻辑
                // is_black 字段内容放入transferSyncUser表 reserveField1字段中
                try {
                    futureList.add(BR_EXECUTORS.submit(() -> {
                        final int i = goBlack(caseShuheUser.getApiCode(), caseShuheUser
                                , iUserType.getBlackExpireDate(creatTime));
                        if (i > 1) {
                            return "";
                        }
                        return "@blackFail";
                    }));
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    this.sendAlarmMgs(title, ("apiCode“").concat(caseShuheUser.getApiCode())
                                    .concat("”\nuserType“").concat(caseShuheUser.getUserType())
                                    .concat("”\n案件编号“").concat(caseShuheUser.getCustNum()).concat("”\n")
                                    .concat("转黑名单失败！请尽快处理^_^，原因：\n") + e
                            , appName, secretKey, alarmClient);
                }
                caseShuheUser.setIsTransfer(2);
            } else if (iUserType.isTurn(caseShuheUser) || iUserType.isEmpty(caseShuheUser)) {
                transferSyncUser.setIfTransform("2");
                caseShuheUser.setIsTransfer(1);
            } else if (iUserType.ifTransfer(caseShuheUser, creatTime)) {
                // 转化
                transferSyncUser.setIfTransform("1");
                caseShuheUser.setIsTransfer(1);
            }
            // D20220209数禾申完转电销
            if (iUserType instanceof CuShenWan) {
                try {
                    futureList.add(BR_EXECUTORS.submit(() -> {
                        boolean satis = ((CuShenWan) iUserType).isSatisfyPhoneSale(caseShuheUser, creatTime);
                        if (satis) {
                            int i1 = goShPhoneSale(caseShuheUser.getApiCode(), caseShuheUser, transferSyncUser);
                            if (caseShuheUser.getIsTransfer() != null
                                    && caseShuheUser.getIsTransfer() == 1) {
                                // 1+3=4 释义：即满足转化又满足电销的逻辑，记为4
                                caseShuheUser.setIsTransfer(4);
                            } else {
                                caseShuheUser.setIsTransfer(3);
                            }
                            if (i1 > 0) {
                                return "";
                            }
                            return "@cuShenWanDianXiaoFail";
                        }
                        return "";
                    }));
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    this.sendAlarmMgs(title, ("apiCode“").concat(caseShuheUser.getApiCode())
                                    .concat("”\nuserType“").concat(caseShuheUser.getUserType())
                                    .concat("”\n案件编号“").concat(caseShuheUser.getCustNum()).concat("”\n")
                                    .concat("数禾促申完转电销失败！请尽快处理^_^，原因：\n") + e, appName
                            , secretKey, alarmClient);
                }
            }
        }
    }

    /**
     * 异步保存到标准转化
     */
    private void asyncSaveTransfer(CaseShuheUser caseShuheUser
            , MarketingTransferSyncUser transferSyncUser, List<Future<String>> futureList) {
        this.setCid(transferSyncUser);
        try {
            futureList.add(BR_EXECUTORS.submit(() -> {
                final int i = goTransferSync(caseShuheUser.getApiCode(), caseShuheUser, transferSyncUser);
                if (i > 0) {
                    return "";
                }
                return "@transferFail";
            }));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            this.sendAlarmMgs(title, ("apiCode“").concat(caseShuheUser.getApiCode())
                    .concat("”\nuserType“").concat(caseShuheUser.getUserType())
                    .concat("”\n案件编号“").concat(caseShuheUser.getCustNum()).concat("”\n")
                    .concat("转化入库失败！请尽快处理^_^，原因：\n") + e, appName, secretKey, alarmClient);
        }
    }

    /**
     * 去黑名单
     */
    private int goBlack(String apiCode, CaseShuheUser caseShuheUser, String expireDate) {
        long first = System.currentTimeMillis();
        try {
            LocalFile localFile = new LocalFile();
            localFile.setApiCode(apiCode);
            localFile.setCreateTime(new Date());
            localFile.setFileType("");
            localFile.setFileName("数禾-转化");
            int i = localFileMapper.insertSelective(localFile);
            PhoneBlack phoneBlack = new PhoneBlack();
            phoneBlack.setLocalId(localFile.getId());
            phoneBlack.setName("");
            phoneBlack.setPhone(caseShuheUser.getCell());
            phoneBlack.setCreateTime(new Date());
            phoneBlack.setUpdateTime(new Date());
            phoneBlack.setExpiredate(expireDate);
            int i1 = phoneBlackMapper.insertSelective(phoneBlack);
            if (localFile.getId() != null && localFile.getId() > 0) {
                producter.send(MQConstants.ROUTING_KEY_MARKETING_PUSH_BLACK, localFile.getId().toString());
            }
            long last = System.currentTimeMillis();
            log.warn("数禾转化->黑名单(apiCode={};custNum={};userType={})共耗时:{}ms"
                    , last - first, apiCode, caseShuheUser.getCustNum(), caseShuheUser.getUserType());
            return i + i1;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            this.sendAlarmMgs(title, ("apiCode“").concat(caseShuheUser.getApiCode())
                    .concat("”\nuserType“").concat(caseShuheUser.getUserType())
                    .concat("”\n案件编号“").concat(caseShuheUser.getCustNum()).concat("”\n")
                    .concat("保存到黑名单失败\n") + e, appName, secretKey, alarmClient);
            return -1;
        }
    }

    /**
     * 去转化
     */
    private int goTransferSync(String apiCode, CaseShuheUser caseShuheUser
            , MarketingTransferSyncUser transferSyncUser) {
        long first = System.currentTimeMillis();
        try {
            SecureRandom random = new SecureRandom();
            transferSyncUser.setRequestId(Md5Utils.cell32(caseShuheUser.getJsonData()
                    .concat("@" + System.currentTimeMillis()).concat("#" + random.nextInt(10000))));
            int rowSync = iTransferSyncUserService.insertSelective(transferSyncUser);
            if (rowSync > 0) {
                MarketingTransferInfo transferInfo = new MarketingTransferInfo();
                transferInfo.setApiCode(apiCode);
                transferInfo.setRequestId(transferSyncUser.getRequestId());
                transferInfo.setCreateTime(new Date());
                transferInfo.setJsonData(caseShuheUser.getJsonData());
                transferInfo.setActualNum(1);
                int rowInfo = marketingTransferInfoMapper.insertSelective(transferInfo);
                boolean isSendMq = rowInfo > 0 && caseShuheUser.getIsTransfer() != null
                        && (caseShuheUser.getIsTransfer() == 1 || caseShuheUser.getIsTransfer() == 4);
                if (isSendMq) {
                    producter.send(MQConstants.ROUTING_KEY_MARKETING_TRANSFER_PUSH_CUSTOMER
                            , transferInfo.getId().toString());
                }
                return rowSync + rowInfo;
            }
            long last = System.currentTimeMillis();
            log.warn("数禾转化->标准转化(apiCode={};custNum={};userType={})共耗时:{}ms"
                    , last - first, apiCode, caseShuheUser.getCustNum(), caseShuheUser.getUserType());
            return rowSync;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            this.sendAlarmMgs(title, ("apiCode“").concat(caseShuheUser.getApiCode())
                    .concat("”\nuserType“").concat(caseShuheUser.getUserType())
                    .concat("”\n案件编号“").concat(caseShuheUser.getCustNum()).concat("”\n")
                    .concat("保存到转化信息失败\n") + e, appName, secretKey, alarmClient);
            return -1;
        }
    }

    /**
     * 去电销
     */
    private int goShPhoneSale(String apiCode, CaseShuheUser caseShuheUser
            , MarketingTransferSyncUser transferSyncUser) {
        long first = System.currentTimeMillis();
        try {
            LocalFile localFile = new LocalFile();
            PhoneSale phoneSale = new PhoneSale();
            PhoneSaleExtendShuhe phoneSaleExtendShuhe = new PhoneSaleExtendShuhe();
            localFile.setCid(transferSyncUser.getCid());
            localFile.setApiCode(apiCode);
            localFile.setFileName("数禾-转化");
            phoneSale.setUid(transferSyncUser.getCustNum());
            //明文
            phoneSale.setPhone(caseShuheUser.getMobile());
            phoneSale.setName("");
            phoneSale.setOrgname("shuheshenwan");
            phoneSale.setSource("16");
            phoneSale.setUserType("2");
            phoneSale.setLoginTime(transferSyncUser.getLoginTime());
            phoneSale.setType("2");
            String field = String.format("{\"clc_usr_iso_idt_tim\":\"%s\",\"clc_usr_iso_crd_tim\":\"%s\"" +
                            ",\"clc_usr_iso_inf_tim\":\"%s\",\"clc_usr_iso_pho_tim\":\"%s\"}"
                    , caseShuheUser.getClcUsrIsoIdtTim(), caseShuheUser.getClcUsrIsoCrdTim()
                    , caseShuheUser.getClcUsrIsoInfTim(), caseShuheUser.getClcUsrIsoPhoTim());
            phoneSale.setExtend(field);
            phoneSaleExtendShuhe.setCustNum(transferSyncUser.getCustNum());
            LocalDateTime localDateTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toLocalDateTime();
            LocalDate localDate = localDateTime.toLocalDate();
            phoneSaleExtendShuhe.setAppletDate(localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            phoneSaleExtendShuhe.setAppletTime(localDateTime.format(dateTimeFormatter));
            phoneSaleExtendShuhe.setStatus("a");
            PushShDXDTO pushShDxDTO = new PushShDXDTO()
                    .setLocalFile(localFile)
                    .setPhoneSale(phoneSale)
                    .setPhoneSaleExtendShuhe(phoneSaleExtendShuhe);
            Result<Boolean> booleanResult = pushDataService.pushShDX(pushShDxDTO);
            if (booleanResult.getData()) {
                log.info("推送电销成功");
            } else {
                String msg = String.format("数禾(custNum=%s)推送电销（人工）失败！失败信息：%s"
                        , transferSyncUser.getCustNum(), booleanResult.getData());
                log.error(msg);
            }
            long last = System.currentTimeMillis();
            log.warn("数禾转化->电销(apiCode={};custNum={};userType={})共耗时:{}ms"
                    , last - first, apiCode, caseShuheUser.getCustNum(), caseShuheUser.getUserType());
            return 1;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            this.sendAlarmMgs(title, ("apiCode“").concat(caseShuheUser.getApiCode())
                    .concat("”\nuserType“").concat(caseShuheUser.getUserType())
                    .concat("”\n案件编号“").concat(caseShuheUser.getCustNum()).concat("”\n")
                    .concat("保存到电销失败\n") + e, appName, secretKey, alarmClient);
            return -1;
        }
    }

    @Override
    public ResponseCustomDTO saveShuheTransferData(String apiCode, String jsonData) {
        long first = System.currentTimeMillis();
        ResponseShuheDTO responseShuheDTO = new ResponseShuheDTO();
        ShuheTransferJsonDTO jsonDTO = null;
        try {
            jsonDTO = JSONObject.parseObject(jsonData, new TypeReference<ShuheTransferJsonDTO>() {
            }.getType());
            // 1、校验参数合法性
            String msg = nonNullCheck(jsonDTO);
            if (!"".equals(msg)) {
                responseShuheDTO.failed("抱歉,缺失必填参数！缺失参数为：".concat(msg));
                msg = "缺失必填参数:".concat(msg).concat("\napiCode“").concat(apiCode).concat("”\nuserType“")
                        .concat(jsonDTO.getBizType()).concat("”\n案件编号“").concat(jsonDTO.getOrderId())
                        .concat("”\n").concat("请及时跟进或与数禾客户及时沟通^_^");
                log.warn("shuhe-1:{}", msg);
                this.sendAlarmMgs(title, msg, appName, secretKey, alarmClient);
                return responseShuheDTO;
            }
            String userType = jsonDTO.getBizType();
            // 2、判断场景类型
            if (StringUtils.isEmpty(userType)) {
                /*
                 * 对bizType字段做兜底，对应营销userType,
                 * 当bizType未传时，需要主动去上传接口中查找，
                 * 如果未查到需要返回给客户提示信息，并将数据落库到本地
                 */
                userType = iMarketingSyncUserService.getUserTypeLatestByCustNum(apiCode, jsonDTO.getOrderId());
            }
            // 3、异步查询db获取相应TaskId
            final ShuheTransferJsonDTO finalJsonDTO = jsonDTO;
            final IUserType iUserType = UserTypeStrategyFactory.getUserTypeStrategy(userType);
            String taskId;
            CaseShuheUser caseShuheUser = CaseShuheUserFactory.newInstance().getCaseShuheUser(
                    iUserType, jsonDTO, apiCode, jsonData);
            boolean sendToQueueBool = iUserType instanceof UnknownUserType;
            if (sendToQueueBool) {
                taskId = null;
                caseShuheUser.setStatus(1);
                msg = "未知的业务类型\"" + userType + "\"!";
                responseShuheDTO.failed("抱歉,".concat(msg));
                caseShuheUser.setErrorInfo("#1" + responseShuheDTO.getDesc());
                log.warn("shuhe-2:{}", responseShuheDTO.getDesc());
                this.sendAlarmMgs(title, msg.concat("\napiCode“").concat(apiCode).concat("”\n案件编号“")
                                .concat(jsonDTO.getOrderId()).concat("”\n").concat("请及时跟进或与数禾客户及时沟通^_^")
                        , appName, secretKey, alarmClient);
            } else {
                Future<String> futureTaskId = BR_EXECUTORS.submit(() ->
                        iMarketingSyncUserService.getTaskIdLatestByCustNum(
                                apiCode, finalJsonDTO.getOrderId(), finalJsonDTO.getBizType()));
                // 3.1、异步任务是否完成
                if (futureTaskId.isDone()) {
                    taskId = futureTaskId.get();
                } else {
                    // 3.2、取消异步任务，使用同步任务获取
                    futureTaskId.isCancelled();
                    taskId = iMarketingSyncUserService.getTaskIdLatestByCustNum(
                            apiCode, jsonDTO.getOrderId(), userType);
                }
            }
            // 4、客户转化数据适配标准转化数据
            MarketingTransferSyncUser transferSyncUser = new TransferSyncAdapter(
                    (CaseShuheUserAdaptee) caseShuheUser).transferSyncUserRequest(taskId);
            // 5、转化信息入转化标准库
            goTransferNew(apiCode, caseShuheUser, transferSyncUser, !sendToQueueBool);
            // 6、数据落前置库
            int row = caseShuheUserMapper.insertSelective(caseShuheUser);
            if (row < 1) {
                msg = "数禾推送数据保存失败！";
                this.sendAlarmMgs(title, msg.concat("\napiCode“").concat(apiCode).concat("”\nuserType“")
                        .concat(userType).concat("”\n案件编号“").concat(jsonDTO.getOrderId()).concat("”\n")
                        .concat("请尽快处理^_^"), appName, secretKey, alarmClient);
                log.error(msg);
                responseShuheDTO.failed("抱歉，".concat(msg));
                faultTolerantInsert(jsonDTO, caseShuheUser.getIsTransfer(), jsonData, apiCode, msg);
            }
            long last = System.currentTimeMillis();
            log.warn("接收数禾转化数据(apiCode={};custNum={};userType={})共耗时:{}ms"
                    , last - first, apiCode, jsonDTO.getOrderId(), jsonDTO.getBizType());
            return responseShuheDTO;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            exceptionSave(jsonDTO, jsonData, apiCode, e);
            return responseShuheDTO.failed();
        }
    }

    /**
     * 去转化新方法 适应框架
     */
    private void goTransferNew(String apiCode, CaseShuheUser caseShuheUser
            , MarketingTransferSyncUser transferSyncUser, boolean sendToQueueBool) {
        long first = System.currentTimeMillis();
        SecureRandom random = new SecureRandom();
        transferSyncUser.setRequestId(Md5Utils.cell32(caseShuheUser.getJsonData()
                .concat("@" + System.currentTimeMillis()).concat("#" + random.nextInt(10000))));
        try {
            int rowSync = iTransferSyncUserService.insertSelective(transferSyncUser);
            if (rowSync < 1) {
                caseShuheUser.setSaveStatus(3);
                caseShuheUser.setErrorInfo("#1.1saveTransferInfo:保存到标准转化详情失败");
                alarmMgs(caseShuheUser);
                return;
            }
        } catch (Exception e) {
            caseShuheUser.setErrorInfo("#1.2saveTransferInfo:保存到标准转化详情异常:" + e);
            caseShuheUser.setSaveStatus(3);
            log.error(e.getMessage(), e);
            alarmMgs(caseShuheUser, e);
        }
        MarketingTransferInfo transferInfo = new MarketingTransferInfo();
        transferInfo.setApiCode(apiCode);
        transferInfo.setRequestId(transferSyncUser.getRequestId());
        transferInfo.setCreateTime(new Date());
        transferInfo.setJsonData(caseShuheUser.getJsonData());
        transferInfo.setActualNum(1);
        try {
            int rowInfo = marketingTransferInfoMapper.insertSelective(transferInfo);
            if (rowInfo > 0) {
                final MqFact mqFact = new MqFact();
                mqFact.setSourceId(transferInfo.getId());
                mqFact.setSource(TransferSource.UNIVERSAL_TRANSFER_PROCESS.getCode());
                if (sendToQueueBool) {
                    producter.sendToUniversalTransferQueue(mqFact);
                }
            } else {
                caseShuheUser.setErrorInfo("#2.1saveTransferInfo:保存到标准转化信息失败");
                caseShuheUser.setSaveStatus(2);
                alarmMgs(caseShuheUser);
            }
        } catch (Exception e) {
            caseShuheUser.setSaveStatus(2);
            caseShuheUser.setErrorInfo("#2.2saveTransferInfo:保存到标准转化信息异常:" + e);
            log.error(e.getMessage(), e);
            alarmMgs(caseShuheUser, e);
        }
        long last = System.currentTimeMillis();
        log.warn("数禾转化->标准转化(apiCode={};custNum={};userType={})共耗时:{}ms"
                , last - first, apiCode, caseShuheUser.getCustNum(), caseShuheUser.getUserType());
    }


    private void alarmMgs(CaseShuheUser caseShuheUser, Exception e) {
        this.sendAlarmMgs(title, ("apiCode“").concat(caseShuheUser.getApiCode())
                        .concat("”\nuserType“").concat(caseShuheUser.getUserType())
                        .concat("”\n案件编号“").concat(caseShuheUser.getCustNum()).concat("”\n")
                        .concat(caseShuheUser.getErrorInfo()).concat(e == null ? "" : e.toString())
                , appName, secretKey, alarmClient);
    }

    private void alarmMgs(CaseShuheUser caseShuheUser) {
        alarmMgs(caseShuheUser, null);
    }

    @Override
    public void handlerContext(ShuHeProcessHandlerContext context, MarketingTransferSyncUser transfer) {
        Date creatTime = iMarketingSyncUserService.getCreatTimeByCustNumAndUserType(transfer.getApiCode()
                , transfer.getCustNum(), transfer.getUserType());
        context.setCreatTime(creatTime);
        IUserType iUserType = UserTypeStrategyFactory.getUserTypeStrategy(transfer.getUserType());
        context.setiUserType(iUserType);
        context.setContinueJudgeRule(true);
        String reserveField1 = transfer.getReserveField1();
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(reserveField1)) {
            JSONObject object = JSONObject.parseObject(reserveField1);
            CaseShuheUser caseShuheUser = new CaseShuheUser();
            caseShuheUser.setIsTurn(object.getString("is_turn"));
            caseShuheUser.setIsBlack(object.getString("is_black"));
            caseShuheUser.setClcUsrLstAppStaTim(object.getString("clc_usr_lst_app_sta_tim"));
            caseShuheUser.setClcUsrIsoPhoTim(object.getString("clc_usr_iso_pho_tim"));
            caseShuheUser.setClcUsrIsoIdtTim(object.getString("clc_usr_iso_idt_tim"));
            caseShuheUser.setClcUsrIsoCrdTim(object.getString("clc_usr_iso_crd_tim"));
            caseShuheUser.setClcUsrIsoInfTim(object.getString("clc_usr_iso_inf_tim"));
            caseShuheUser.setClcUsrFrtFqOrdTim(object.getString("applyLoanTime"));
            caseShuheUser.setCell(object.getString("cell"));
            context.setCaseShuheUser(caseShuheUser);
            context.setTaskId(object.getString("taskId"));
        }

    }
}
