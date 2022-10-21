package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.br.common.encryption.Md5Utils;
import com.br.marketing.adapter.transfer.TransferSyncAdapter;
import com.br.marketing.adapter.transfer.adaptee.CaseShuheUserAdaptee;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.exception.BusinessException;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.dto.PushShDXDTO;
import com.br.marketing.dto.ResponseCustomDTO;
import com.br.marketing.dto.shuhe.Response2ShuheDTO;
import com.br.marketing.dto.shuhe.ResponseShuheDTO;
import com.br.marketing.dto.shuhe.ShuheTransferJsonDTO;
import com.br.marketing.dto.shuhe.factory.CaseShuheUserFactory;
import com.br.marketing.dto.shuhe.factory.UserTypeStrategyFactory;
import com.br.marketing.dto.shuhe.strategy.CuShenWan;
import com.br.marketing.dto.shuhe.strategy.IUserType;
import com.br.marketing.dto.shuhe.strategy.UnknownUserType;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.TransferSource;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.service.IPushShuheDataService;
import com.br.marketing.service.ITransferSyncUserService;
import com.br.marketing.service.PushDataService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ShuHeAESencUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/**
 * 数禾转化实现类
 *
 * @author Guo Zeqiang
 * @dateTime 2022/2/10 14:25
 */
@Service
@Slf4j
public class PushShuheDataServiceImpl implements IPushShuheDataService {

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
    private CaseShuheUploadDataMapper caseShuheUploadDataMapper;
    @Resource
    private MarketingUserMapper marketingUserMapper;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    ;
    @Resource
    private AlarmApiClient alarmClient;
    @Value("${otherConfig.alarm.secretKey:00}")
    private String secretKey;
    @Value("${otherConfig.alarm.appName:00}")
    private String appName;

    private static final ThreadPoolExecutor BR_EXECUTORS = BrExecutors.getThreadPool(1, 2);
    private final String title = "数禾转化数据定制化清洗入库";
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter yyMMddHH = DateTimeFormatter.ofPattern("yyMMdd");
    private static final Set<String> FIELD_SET = Collections.newSetFromMap(new ConcurrentHashMap<>());

    static {
        // D20220824数禾定制版上传接口改造一期 初始化字段 2022-9-1 16:49:53
        FIELD_SET.addAll(Arrays.asList(
                "listInfo"
                , "templateCode"
                , "extraInfo"
                , "templateName"
                , "outboundFrequency"
                , "operatingCycle"
                , "mobile"
                , "orderId"
                , "bizId"
                , "varData"
                , "bizType"
                , "cus_name"
                , "idt_no"
                , "clc_usr_adt_tim_rcn_lon"
                , "clc_usr_adt_lmt_fst_all"
                , "clc_usr_adt_lmt_lv0"
                , "clc_usr_hvy_max_3_avl_lmt"
                , "clc_usr_lst_app_sta_tim"
                , "clc_usr_lst_non_dcp_trs_tim"
                , "clc_usr_new_adt_rat_btr"
                , "clc_usr_new_adt_rat_csh"
                , "clc_usr_new_adt_rat_hgl"
                , "off_usr_last_adjlmt_add_lmt"
                , "off_usr_lsh_out_day_flg"
                , "off_usr_lst_adj_lmt_tim_micro_all"
                , "off_usr_lst_ord_tim_all"
        ));
    }

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
                    , apiCode, jsonDTO.getOrderId(), jsonDTO.getBizType(), last - first);
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
        user.setErrorInfo("#5" + e.getMessage());
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        user.setStatus(2);
        if (e instanceof SQLException) {
            user.setSaveStatus(1);
        }
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
                if (StringUtils.isEmpty(cId)) {
                    return;
                }
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
                    , apiCode, caseShuheUser.getCustNum(), caseShuheUser.getUserType(), last - first);
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
                    , apiCode, caseShuheUser.getCustNum(), caseShuheUser.getUserType(), last - first);
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
                    , apiCode, caseShuheUser.getCustNum(), caseShuheUser.getUserType(), last - first);
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
        ResponseShuheDTO responseShuheDTO = new ResponseShuheDTO();
        responseShuheDTO.success();
        ShuheTransferJsonDTO jsonDTO = null;
        String requestId = "";
        try {
            jsonDTO = JSONObject.parseObject(jsonData, new TypeReference<ShuheTransferJsonDTO>() {
            }.getType());
            // 1、校验参数合法性
            String msg = nonNullCheck(jsonDTO);
            if (!"".equals(msg)) {
                responseShuheDTO.failed("抱歉,缺失必填参数！缺失参数为：".concat(msg));
                msg = "缺失必填参数:".concat(msg).concat("\napiCode“" + apiCode).concat("”\nuserType“"
                        + jsonDTO.getBizType()).concat("”\n案件编号“" + jsonDTO.getOrderId())
                        .concat("”\n").concat("请及时跟进或与数禾客户及时沟通^_^");
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
                caseShuheUser.setStatus(1);
                msg = "未知的业务类型\"" + userType + "\"!";
                responseShuheDTO.failed("抱歉,".concat(msg));
                caseShuheUser.setErrorInfo("#1" + responseShuheDTO.getDesc());
                this.sendAlarmMgs(title, msg.concat("\napiCode“").concat(apiCode).concat("”\n案件编号“")
                                .concat(jsonDTO.getOrderId()).concat("”\n").concat("请及时跟进或与数禾客户及时沟通^_^")
                        , appName, secretKey, alarmClient);
            } else if (!iUserType.getApiCodes().contains(apiCode)) {
                log.warn("场景(".concat(iUserType.getApiCodes().toString()).concat(")与对应apiCode不匹配\n")
                        .concat(userType).concat("\napiCode“").concat(apiCode).concat("”\n案件编号“")
                        .concat(jsonDTO.getOrderId()).concat("”\n").concat("请及时跟进或与数禾客户及时沟通^_^"));
            }
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
            // 4、客户转化数据适配标准转化数据
            MarketingTransferSyncUser transferSyncUser = new TransferSyncAdapter(
                    (CaseShuheUserAdaptee) caseShuheUser).transferSyncUserRequest(taskId, jsonDTO);
            SecureRandom random = new SecureRandom();
            requestId = Md5Utils.cell32(caseShuheUser.getJsonData()
                    .concat("@" + System.currentTimeMillis()).concat("#" + random.nextInt(10000)));
            caseShuheUser.setReserveField2(requestId);
            transferSyncUser.setRequestId(requestId);
            // 6、数据落前置库
            try {
                int row = caseShuheUserMapper.insertSelective(caseShuheUser);
                if (row < 1) {
                    throw new BusinessException("入库失败!影响的记录数：" + row);
                }
            } catch (Exception e) {
                msg = "数禾推送数据前置表保存失败！";
                this.sendAlarmMgs(title, msg.concat("\napiCode“").concat(apiCode).concat("”\nuserType“")
                        .concat(userType).concat("”\n案件编号“").concat(jsonDTO.getOrderId()).concat("”\n")
                        .concat("请尽快处理^_^"), appName, secretKey, alarmClient);
                log.error(msg.concat("" + e.getMessage()), e);
                responseShuheDTO.failed("抱歉，".concat(msg));
                faultTolerantInsert(jsonDTO, 0, jsonData, apiCode, msg);
            }
            // 5、转化信息入转化标准库
            goTransferNew(apiCode, caseShuheUser, transferSyncUser, !sendToQueueBool);
            if (caseShuheUser.getSaveStatus() != null) {
                updateCaseShuhe(caseShuheUser);
            }
            return responseShuheDTO;
        } catch (Exception e) {
            log.error(String.format("shuhe_error apicode:%s;jsondata:%s;requestId:%s"
                    , apiCode, jsonData, requestId));
            log.error(e.getMessage(), e);
            exceptionSave(jsonDTO, jsonData, apiCode, e);
            return responseShuheDTO.failed();
        }
    }

    private void updateCaseShuhe(CaseShuheUser caseShuheUser) {
        BR_EXECUTORS.execute(() -> {
            CaseShuheUser csu = new CaseShuheUser();
            csu.setId(caseShuheUser.getId());
            csu.setSaveStatus(caseShuheUser.getSaveStatus());
            csu.setErrorInfo(caseShuheUser.getErrorInfo());
            csu.setUpdateTime(new Date());
            caseShuheUserMapper.updateByPrimaryKeySelective(csu);
        });
    }

    /**
     * 去转化新方法 适应框架
     */
    private void goTransferNew(String apiCode, CaseShuheUser caseShuheUser
            , MarketingTransferSyncUser transferSyncUser, boolean sendToQueueBool) {
        this.setCid(transferSyncUser);
        if (StringUtils.isEmpty(transferSyncUser.gettCid())) {
            this.sendAlarmMgs(title, "数禾客户信息未维护...".concat("\napiCode“").concat(caseShuheUser.getApiCode())
                    .concat("”\nuserType“").concat(caseShuheUser.getUserType())
                    .concat("”\n案件编号“").concat(caseShuheUser.getCustNum())
                    .concat("”\n").concat("尽快处理^_^"), appName, secretKey, alarmClient);
            return;
        }
        LocalDateTime localDateTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toLocalDateTime();
        transferSyncUser.setInsertTime(localDateTime.format(dateTimeFormatter));
        transferSyncUser.setRequestData(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        transferSyncUser.setRequestTime(transferSyncUser.getInsertTime());
        MarketingTransferInfo transferInfo = new MarketingTransferInfo();
        transferInfo.setApiCode(apiCode);
        transferInfo.setRequestId(transferSyncUser.getRequestId());
        transferInfo.setCreateTime(new Date());
        transferInfo.setJsonData(JSONObject.toJSONString(transferSyncUser));
        transferInfo.setActualNum(1);
        Long id = iTransferSyncUserService.insertInfoAndSync(transferSyncUser, transferInfo, caseShuheUser);
        if (id == null) {
            alarmMgs(caseShuheUser);
        }
        List<String> universalProcessApiCode = marketingCommonConfig.getUniversalProcessApiCode();
        if (sendToQueueBool && universalProcessApiCode.contains(apiCode)) {
            final MqFact mqFact = new MqFact();
            mqFact.setSourceId(transferInfo.getId());
            mqFact.setSource(TransferSource.UNIVERSAL_TRANSFER_PROCESS.getCode());
            producter.sendToUniversalTransferQueue(mqFact);
        }
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
    public ResponseCustomDTO saveUploadData(String apiCode, String jsonData) {
        CaseShuheUploadData shuheUploadData = new CaseShuheUploadData();
        shuheUploadData.setJsonData(jsonData);
        shuheUploadData.setUploadDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        shuheUploadData.setCreateTime(new Date());
        shuheUploadData.setUpdateTime(shuheUploadData.getCreateTime());
        shuheUploadData.setApiCode(apiCode);
        shuheUploadData.setRequestId(getSerialNumber(apiCode));
        Response2ShuheDTO response2ShuheDTO = new Response2ShuheDTO();
        response2ShuheDTO.setMsgId(shuheUploadData.getRequestId());
        if (org.apache.commons.lang3.StringUtils.isBlank(jsonData)) {
            response2ShuheDTO.failed(",内容不可为空");
            exceptionSave(shuheUploadData, response2ShuheDTO, null);
            return response2ShuheDTO;
        }
        final JSONObject uploadDataDTO;
        try {
            uploadDataDTO = JSONObject.parseObject(jsonData);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            response2ShuheDTO.failed(",JSON结构解析失败");
            exceptionSave(shuheUploadData, response2ShuheDTO, e);
            return response2ShuheDTO;
        }
        final JSONArray listInfo = uploadDataDTO.getJSONArray("listInfo");
        if (requiredCheck(listInfo, response2ShuheDTO)) {
            exceptionSave(shuheUploadData, response2ShuheDTO, null);
            return response2ShuheDTO;
        }
        if (uploadDataDTO.containsKey("extraInfo")) {
            String userType = uploadDataDTO.getString("extraInfo");
            shuheUploadData.setUserType(StringUtils.isEmpty(userType) ? "" : userType);
        }
        try {
            int i = caseShuheUploadDataMapper.insertSelective(shuheUploadData);
            if (i != 1) {
                String mgs = "数禾上传数据前置表入库失败";
                BusinessException exception = new BusinessException(mgs);
                exception.setExceptionMessage(mgs);
            }
            response2ShuheDTO.setMsgId(serialNumberAddId(shuheUploadData));
            shuheUploadData.setRequestId(response2ShuheDTO.getMsgId());
        } catch (Exception e) {
            log.error(e.getMessage()
                    + "\nrequestId:" + shuheUploadData.getRequestId()
                    + "\napiCode:" + apiCode
                    + "\njsonData:" + jsonData, e);
            response2ShuheDTO.setMsgId(shuheUploadData.getRequestId());
            return response2ShuheDTO.failed();
        }
        saveSyncInfo(adapterMarketingPreUserDTO(uploadDataDTO, listInfo, shuheUploadData), shuheUploadData);
        BR_EXECUTORS.execute(() -> checkField(uploadDataDTO, listInfo));
        return response2ShuheDTO.success();
    }

    /**
     * 2022/9/1 11:46
     * 适配标准上传接口结构
     */
    private MarketingPreUserDTO adapterMarketingPreUserDTO(JSONObject uploadDataDTO, JSONArray listInfo
            , CaseShuheUploadData shuheUploadData) {
        try {
            MarketingPreUserDTO userDTO = new MarketingPreUserDTO();
            userDTO.setTaskId(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
            userDTO.setRequestId(shuheUploadData.getRequestId());
            userDTO.setLast("0");
            userDTO.setTotal("0");
            List<MarketingPreUserDetailDTO> list = new ArrayList<>();
            MarketingPreUserDetailDTO dto;
            String type = shuheUploadData.getUserType();
            JSONObject varData;
            Map<String, Object> reserveField1;
            int size = listInfo.size();
            for (int i = 0; i < size; i++) {
                JSONObject info = listInfo.getJSONObject(i);
                reserveField1 = new HashMap<>(32);
                dto = new MarketingPreUserDetailDTO();
                String mobile = info.getString("mobile");
                try {
                    dto.setCell(org.apache.commons.lang3.StringUtils.isNotBlank(mobile)
                            ? ShuHeAESencUtil.decrypt(mobile) : mobile);
                } catch (Exception e) {
                    dto.setCell(mobile);
                    log.error(e.getMessage(), e);
                }
                dto.setGroupType(type);
                dto.setCustNum(info.getString("orderId"));
                varData = info.getJSONObject("varData");
                if (!CollectionUtils.isEmpty(varData)) {
                    String keyId = "idt_no";
                    String keyName = "cus_name";
                    if (varData.containsKey(keyId)) {
                        dto.setId(varData.getString(keyId));
                        varData.remove(keyId);
                    }
                    if (varData.containsKey(keyName)) {
                        dto.setName(varData.getString(keyName));
                        varData.remove(keyName);
                    }
                    reserveField1.putAll(varData);
                }
                reserveField1.putAll(info);
                reserveField1.putAll(uploadDataDTO);
                reserveField1.remove("listInfo");
                reserveField1.remove("mobile");
                reserveField1.remove("varData");
                reserveField1.remove("orderId");
                reserveField1.remove("extraInfo");
                dto.setReserveField1(JSON.toJSONString(reserveField1, SerializerFeature.WriteNullStringAsEmpty
                        , SerializerFeature.WriteNullListAsEmpty));
                list.add(dto);
            }
            userDTO.setDataItems(list);
            return userDTO;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 2022/9/1 11:45
     * 保存到info表
     */
    private void saveSyncInfo(MarketingPreUserDTO userDTO, CaseShuheUploadData shuheUploadData) {
        if (ObjectUtils.isEmpty(userDTO)) {
            return;
        }
        MarketingSyncInfo syncInfo = new MarketingSyncInfo();
        CaseShuheUploadData record = new CaseShuheUploadData();
        record.setId(shuheUploadData.getId());
        record.setRequestId(shuheUploadData.getRequestId());
        syncInfo.setApiCode(shuheUploadData.getApiCode());
        syncInfo.setCusBatch(userDTO.getTaskId());
        syncInfo.setRequestBatch(userDTO.getRequestId());
        syncInfo.setLast((byte) 0);
        syncInfo.setTotal(0L);
        syncInfo.setCreateTime(new Date());
        syncInfo.setActualNum(userDTO.getDataItems().size());
        try {
            syncInfo.setJsonData(JSON.toJSONString(userDTO, SerializerFeature.WriteNullStringAsEmpty
                    , SerializerFeature.WriteNullListAsEmpty));
            int i = marketingUserMapper.insertMarketingPreUserByText(syncInfo);
            if (i == 1 && syncInfo.getId() != null) {
                try {
                    producter.send(MQConstants.ROUTING_KEY_MARKETING_PRE_USER_SHUHERECEIVE, syncInfo.getId().toString());
                } catch (Exception e) {
                    record.setSaveInfoStatus(2);
                    log.error(e.getMessage(), e);
                }
            } else {
                record.setSaveInfoStatus(1);
            }
        } catch (Exception e) {
            record.setSaveInfoStatus(1);
            log.error(e.getMessage(), e);
        } finally {
            try {
                int u = caseShuheUploadDataMapper.updateByPrimaryKeySelective(record);
                if (u != 1) {
                    String mgs = "数禾上传数据前置表更新信息失败";
                    BusinessException exception = new BusinessException(mgs);
                    exception.setExceptionMessage(mgs);
                    throw exception;
                }
            } catch (Exception e) {
                log.error(e.getMessage()
                        + "\n前置表Id:" + shuheUploadData.getId()
                        + "\nrequestId:" + shuheUploadData.getRequestId()
                        + "\napiCode:" + shuheUploadData.getApiCode()
                        + "\njsonData:" + shuheUploadData.getJsonData(), e);
            }
        }
    }

    /**
     * 2022/9/1 9:06
     * 异常保存
     */
    private void exceptionSave(CaseShuheUploadData shuheUploadData, Response2ShuheDTO response2ShuheDTO, Exception e) {
        shuheUploadData.setErrorInfo(response2ShuheDTO.getDesc() + (e == null ? "" : "\n" + e.getMessage()));
        shuheUploadData.setStatus(1);
        shuheUploadData.setSaveInfoStatus(1);
        try {
            int i = caseShuheUploadDataMapper.insertSelective(shuheUploadData);
            if (i == 1) {
                response2ShuheDTO.setMsgId(serialNumberAddId(shuheUploadData));
            } else {
                String mgs = "数禾上传数据异常数据入库失败";
                BusinessException exception = new BusinessException(mgs);
                exception.setExceptionMessage(mgs);
                throw exception;
            }
        } catch (Exception exception) {
            log.error(exception.getMessage()
                    + "\nrequestId:" + shuheUploadData.getRequestId()
                    + "\napiCode:" + shuheUploadData.getApiCode()
                    + "\njsonData:" + shuheUploadData.getJsonData(), exception);
        }
    }

    /**
     * 2022/9/1 10:45
     * 必填检查
     */
    private boolean requiredCheck(JSONArray listInfo, Response2ShuheDTO response2ShuheDTO) {
        if (CollectionUtils.isEmpty(listInfo)) {
            try {
                response2ShuheDTO.failed(",名单列表内容为空");
                BusinessException exception = new BusinessException(response2ShuheDTO.getDesc());
                exception.setExceptionMessage(response2ShuheDTO.getDesc());
                throw exception;
            } catch (BusinessException e) {
                log.error(e.getMessage(), e);
                return true;
            }
        }
        return false;
    }

    /**
     * 2022/9/1 10:45
     * 新增字段检查
     */
    private void checkField(JSONObject uploadDataDTO, JSONArray listInfo) {
        try {
            Set<String> keySet = new HashSet<>(uploadDataDTO.keySet());
            StringBuilder fieldStr = new StringBuilder();
            int size = listInfo.size();
            for (int i = 0; i < size; i++) {
                JSONObject info = listInfo.getJSONObject(i);
                keySet.addAll(info.keySet());
                JSONObject varData = info.getJSONObject("varData");
                if (varData != null) {
                    keySet.addAll(varData.keySet());
                }
            }
            String separator = "、";
            for (String key : keySet) {
                if (FIELD_SET.add(key)) {
                    Long aLong = redisChgService.saddMember(RedisKeyConstant.shuHeUploadDataFieldKey, key);
                    if (aLong == 1) {
                        fieldStr.append(fieldStr.length() > 0 ? separator : "\n").append(key);
                    }
                }
            }
            if (fieldStr.length() > 0) {
                alarmClient.sendAlarm("本次请求发现新增字段："
                                .concat(fieldStr.toString())
                                .concat("\n请及时与客户沟通确认^_^"), "数禾上传数据接口字段新增检查", appName, secretKey,
                        Constants.sendCodeMap.get("apiSaveDbException"));
                Long rSum = redisChgService.scard(RedisKeyConstant.shuHeUploadDataFieldKey);
                if (rSum == null || rSum < FIELD_SET.size()) {
                    redisChgService.sadd(RedisKeyConstant.shuHeUploadDataFieldKey, new ArrayList<>(FIELD_SET));
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }


    /**
     * 2022/8/30 16:24
     * 流水号生成规则：
     * 1.年取倒数2位+月（两位）+日（两位）+apicode取倒数4位+纳秒倒数3~8位（共6位）+3位随机数
     * 2.流水线长：2+2+2+4+6+3
     * eg:
     * 2208300004377960345
     */
    private String getSerialNumber(String apiCode) {
        SecureRandom random = new SecureRandom();
        int length = apiCode.length();
        return LocalDateTime.now().format(yyMMddHH)
                + apiCode.substring(length - 4, length)
                + (String.format(("%06d"), (System.nanoTime() / 100 % 1000000)))
                + (random.nextInt(900) + 100);
    }

    /**
     * 2022/8/30 18:04
     * 业务流水流水号生成规则：
     * 1.流水号+数据库id
     * 2.业务流水号总长：19+id长度，如使用bigint类型，则：19+19=38
     * eg:
     * 2208300004611020223123456 id为123456
     */
    private String serialNumberAddId(CaseShuheUploadData data) {
        return data.getRequestId() + (data.getId() > 0 ? data.getId() : "");
    }
}
