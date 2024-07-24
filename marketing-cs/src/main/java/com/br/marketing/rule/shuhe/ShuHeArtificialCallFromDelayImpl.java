package com.br.marketing.rule.shuhe;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportAdapDTO;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.RealTimeUserDataDTO;
import com.br.marketing.client.robotaiapi.RobotaiApiServiceClient;
import com.br.marketing.client.robotaiapi.input.BlackQueryDetailDTO;
import com.br.marketing.client.robotaiapi.input.PhoneEncryptTypeEnum;
import com.br.marketing.client.robotaiapi.input.ReqBlackPhoneQueryDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.ShuHeRuleCollectDataImpl;
import com.br.marketing.dto.shuhe.strategy.BaseUserType;
import com.br.marketing.dto.shuhe.strategy.CuFuJie;
import com.br.marketing.dto.shuhe.strategy.CuShouJie;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.CallRecordMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import com.br.marketing.mapper.ShuheTransferStopPushRecordMapper;
import com.br.marketing.origin.DataLoadingHandlerService;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.Impl.CaseUserServiceImpl;
import com.br.marketing.service.PushDataService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数禾转化推送人工电销 业务
 *
 * @author Guo Zeqiang
 * @dateTime 2022/3/18 14:45
 */
@Service
@Slf4j
public class ShuHeArtificialCallFromDelayImpl implements AssembleData<RealTimeUserDataDTO> {

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
    @Resource
    private DataLoadingHandlerService handlerService;
    @Resource
    private PushDataService pushDataService;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private CallRecordMapper callRecordMapper;
    @Resource
    private ShuheTransferStopPushRecordMapper shuheTransferStopPushRecordMapper;
    @Resource
    private RedisChgService redisChgService;
    @Resource
    private PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;
    @Autowired
    CaseUserServiceImpl caseUserService;

    @Resource
    private RobotaiApiServiceClient robotaiApiServiceClient;

    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[:SSS]");

    /**
     * apiCoid:cusNum:情况
     */
    public final static String KEY = "marketing:api:transfer:shuhe:%s:%s:%s";

    @Override
    public RealTimeUserDataDTO assemble(Object transmitFact, ProcessHandlerContext context) {
        ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData shuHeContext =
                (ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData) context.getRuleNecessaryData();
        MarketingTransferSyncUser transfer = shuHeContext.getTransfer();
        RealTimeUserDataDTO realTimeUserDataDTO = new RealTimeUserDataDTO();
        realTimeUserDataDTO.setDassSingleImportAdapDTO(getDassSingleImportAdap(shuHeContext));
        realTimeUserDataDTO.getDassSingleImportAdapDTO().setTransferInfoId(context.getTransferInfoId());
        PhoneSaleExtendInfo phoneSaleExtendShuhe = getPhoneSaleExtendShuhe(transfer, shuHeContext);
        phoneSaleExtendShuhe.setSourceId(context.getMqFact().getSourceId());
        realTimeUserDataDTO.setPhoneSaleExtendInfo(phoneSaleExtendShuhe);
        return realTimeUserDataDTO;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        boolean bool = Boolean.FALSE;
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            Integer isDelay = context.getMqFact().getIsDelay();
            if (isDelay != null && isDelay == 1) {
                String tCid = StringUtils.isEmpty(transfer.gettCid())
                        ? handlerService.getTcIdFromRedis(transfer.getApiCode()) : transfer.gettCid();
                MarketingTransferSyncUser dbTransferSyncUser = getDbTransferSyncUser(transfer.getCustNum()
                        , transfer.getApiCode(), transfer.getUserType(), tCid, transfer.getCreateTime());
                ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData shuHeContext =
                        (ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData) context.getRuleNecessaryData();
                shuHeContext.setTransfer(dbTransferSyncUser);
                CaseShuheUser caseShuheUser = shuHeContext.getCaseShuheUser();
                BaseUserType baseUserType = shuHeContext.getBaseUserType();
                if (baseUserType instanceof CuFuJie) {
                    String message = context.getMqFact().getMessage();
                    JSONObject jsonObject = JSONObject.parseObject(message);
                    String status = jsonObject.get("status").toString();
                    caseShuheUser.getJsonObject().putAll(jsonObject);
                    caseShuheUser.setReserveField2(status);
                    boolean boolIfGiveUp = baseUserType.ifGiveUp(caseShuheUser, shuHeContext.getCreatTime(), caseUserService);
                    String cell = shuHeContext.getCustomerMap().getOrDefault(transfer.getCustNum()
                            , new MarketingSyncUser()).getCell();
                    if (boolIfGiveUp || queryBlackFlag(transfer, cell)) {
                        return false;
                    }
                    switch (status) {
                        case "a":
                            if (!queryStopPushRecord(transfer)) {
                                bool = pushDataService.pushShDXSingleMutex(transfer.getApiCode(), transfer.getCustNum()
                                        , "a", transfer.getUserType());
                            }
                            break;
                        case "b":
                            // 查询是不是首次命中b
                            if (!phoneSaleExtendInfo(transfer.getCustNum(), transfer.getApiCode(), transfer.getUserType())) {
                                log.info("促复借b情况，非首次");
                                // 判断是不是在停止推送时间内
                                if (queryStopPushRecord(transfer)) {
                                    bool = pushDataService.pushShDXSingleMutex(transfer.getApiCode(), transfer.getCustNum()
                                            , "b", transfer.getUserType());
                                    break;
                                }
                            }
                            if (!queryCallRecord(transfer)) {
                                bool = pushDataService.pushShDXSingleMutex(transfer.getApiCode(), transfer.getCustNum()
                                        , "b", transfer.getUserType());
                            }
                            break;
                        default:
                    }
                    shuHeContext.setCaseShuheUser(caseShuheUser);
                } else {
                    bool = !baseUserType.ifGiveUp(caseShuheUser, shuHeContext.getCreatTime(), caseUserService)
                            && pushDataService.pushShDXSingleMutex(transfer.getApiCode(), transfer.getCustNum()
                            , "a", transfer.getUserType());
                }
            }
        }
        return bool;
    }

    @Override
    public String label() {
        return "ShuHe_TransferData_ArtificialRealTimeUserDataFromDelay";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.ARTIFICIAL_REAL_TIME_USERDATA.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.SHU_HE_RULE_DATA_COLLECTION.getCode();
    }

    /**
     * 查询db获取cusNum当天新的数据
     */
    private MarketingTransferSyncUser getDbTransferSyncUser(String custNum, String apiCode, String userType
            , String tCid, Date createTime) {
        MarketingTransferSyncUserExample example = new MarketingTransferSyncUserExample();
        String time = marketingCommonConfig.getMessageQueueExpireTime();
        long s = 3600L;
        try {
            if (StringUtils.hasText(time)) {
                // 转换成秒
                s = Long.parseLong(time) / 1000L + 3;
            }
        } catch (NumberFormatException e) {
            log.error(e.getMessage(), e);
        }
        LocalDateTime localDateTime = createTime.toInstant().atZone(
                ZoneId.systemDefault()).toLocalDateTime().plusSeconds(s);
        example.createCriteria().andApiCodeEqualTo(apiCode).andUserTypeEqualTo(userType)
                .andCustNumEqualTo(custNum).andCreateTimeGreaterThanOrEqualTo(createTime)
                .andCreateTimeLessThanOrEqualTo(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()));
        example.settCid(tCid);
        example.setOrderByClause("create_time desc limit 0,1");
        List<MarketingTransferSyncUser> transferList = marketingTransferSyncUserMapper.selectByExample(example);
        return transferList.get(0);
    }

    /**
     * 封装电销接口数据
     */
    private DassSingleImportAdapDTO getDassSingleImportAdap(
            ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData shuHeContext) {
        DassSingleImportAdapDTO adapDTO = new DassSingleImportAdapDTO();
        adapDTO.setDassSingleImportDataDTO(getDassSingleImportData(shuHeContext));
        return adapDTO;
    }

    /**
     * 封装电销扩展数据
     */
    private PhoneSaleExtendInfo getPhoneSaleExtendShuhe(MarketingTransferSyncUser transfer
            , ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData shuHeContext) {
        PhoneSaleExtendInfo phoneSaleExtendInfo = new PhoneSaleExtendInfo();
        LocalDateTime localDateTime = transfer.getCreateTime().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDate localDate = localDateTime.toLocalDate();
        phoneSaleExtendInfo.setAppletDate(localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        phoneSaleExtendInfo.setCustNum(transfer.getCustNum());
        phoneSaleExtendInfo.setAppletTime(localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        phoneSaleExtendInfo.setStatus("a");
        if (shuHeContext.getBaseUserType() instanceof CuFuJie) {
            phoneSaleExtendInfo.setStatus(shuHeContext.getCaseShuheUser().getReserveField2());
        }
        phoneSaleExtendInfo.setApiCode(transfer.getApiCode());
        phoneSaleExtendInfo.setUserType(transfer.getUserType());
        phoneSaleExtendInfo.setTaskId(shuHeContext.getTaskId());
        return phoneSaleExtendInfo;
    }

    private DassSingleImportDataDTO getDassSingleImportData(ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData shuHeContext) {
        CaseShuheUser caseShuheUser = shuHeContext.getCaseShuheUser();
        BaseUserType baseUserType = shuHeContext.getBaseUserType();
        MarketingSyncUser marketingUserByCell = shuHeContext.getMarketingSyncUserByCell();
        DassSingleImportDataDTO dataDTO = new DassSingleImportDataDTO();
        dataDTO.setPrioritySymbol("1");
        JSONObject extend = new JSONObject();
        extend.put("face_recognitiion", valuableAndCurrentDay(caseShuheUser.getClcUsrIsoPhoTim()));
        extend.put("is_usr_idt", valuableAndCurrentDay(caseShuheUser.getClcUsrIsoIdtTim()));
        extend.put("is_bindcard", valuableAndCurrentDay(caseShuheUser.getClcUsrIsoCrdTim()));
        extend.put("is_usr_inf", valuableAndCurrentDay(caseShuheUser.getClcUsrIsoInfTim()));
        extend.put("is_usr_lst_app_sta_tim", valuableAndCurrentDay(caseShuheUser.getClcUsrLstAppStaTim()));
        extend.put("typeSign", "1");
        dataDTO.setPhone(caseShuheUser.getCell());
        dataDTO.setLoginTime(caseShuheUser.getClcUsrLstAppStaTim());
        dataDTO.setName("1");
        baseUserType.getPrivateInfo(dataDTO);
        dataDTO.setUid(caseShuheUser.getCustNum());
        if (baseUserType instanceof CuShouJie && !Objects.isNull(marketingUserByCell)) {
            JSONObject parseObject = JSON.parseObject(marketingUserByCell.getReserveField1());
            String IfCoupon = parseObject.getOrDefault("if_coupon", "").toString();
            if (org.apache.commons.lang3.StringUtils.isNotBlank(IfCoupon)) {
                extend.put("if_coupon", IfCoupon);
            }
            String IfTie = parseObject.getOrDefault("if_tie", "").toString();
            if (org.apache.commons.lang3.StringUtils.isNotBlank(IfTie)) {
                extend.put("if_tie", IfTie);
            }
            String aftLmt = parseObject.getOrDefault("aft_lmt", "").toString();
            if (org.apache.commons.lang3.StringUtils.isNotBlank(aftLmt)) {
                extend.put("aft_lmt", aftLmt);
            }
            String IfCs = parseObject.getOrDefault("if_cs", "").toString();
            if (org.apache.commons.lang3.StringUtils.isNotBlank(IfCs)) {
                extend.put("if_cs", IfCs);
            }
        }
        if (baseUserType instanceof CuFuJie) {
            JSONObject jsonObject = caseShuheUser.getJsonObject();
            String lv0 = jsonObject.getOrDefault("clc_usr_avl_lmt_lv0", "").toString();
            if (org.apache.commons.lang3.StringUtils.isNotBlank(lv0)) {
                extend.put("clc_usr_avl_lmt_lv0", lv0);
            }
            String typeSign = jsonObject.getOrDefault("typeSign", "").toString();
            if (org.apache.commons.lang3.StringUtils.isNotBlank(typeSign)) {
                extend.put("typeSign", typeSign);
            }
            if (!Objects.isNull(marketingUserByCell)) {
                JSONObject parseObject = JSON.parseObject(marketingUserByCell.getReserveField1());
                String IfCoupon = parseObject.getOrDefault("if_coupon", "").toString();
                if (org.apache.commons.lang3.StringUtils.isNotBlank(IfCoupon)) {
                    extend.put("if_coupon", IfCoupon);
                }
                String IfTie = parseObject.getOrDefault("if_tie", "").toString();
                if (org.apache.commons.lang3.StringUtils.isNotBlank(IfTie)) {
                    extend.put("if_tie", IfTie);
                }
                String aftLmt = parseObject.getOrDefault("aft_lmt", "").toString();
                if (org.apache.commons.lang3.StringUtils.isNotBlank(aftLmt)) {
                    extend.put("aft_lmt", aftLmt);
                }
            }
            dataDTO.setPrioritySymbol(jsonObject.getOrDefault("prioritySymbol", "").toString());
            Map<String, MarketingSyncUser> customerMap = shuHeContext.getCustomerMap();
            if (customerMap.containsKey(caseShuheUser.getCustNum())) {
                MarketingSyncUser syncUser = customerMap.get(caseShuheUser.getCustNum());
                String name = syncUser.getName();
                if (!StringUtils.isEmpty(name)) {
                    try {
                        name = BrCipherMaker.getInstance().decode(syncUser.getName());
                        if (!syncUser.getName().equals(name)) {
                            dataDTO.setName(name);
                        }
                    } catch (Exception ignored) {
                    }
                }
                String defaultValue = "";
                extend.put("special1", defaultValue);
                String reserveField1 = syncUser.getReserveField1();
                if (StringUtils.hasText(reserveField1)) {
                    try {
                        JSONObject JSONObj = JSONObject.parseObject(reserveField1);
                        if (JSONObj != null) {
                            extend.put("special1", JSONObj.getOrDefault("special1", defaultValue));
                        }
                    } catch (Exception e) {
                        log.error("reserveField1:" + reserveField1 + "\n" + e.getMessage(), e);
                    }
                }
            }
            dataDTO.setAuditAmount(jsonObject.getOrDefault("clc_usr_adt_lmt_lv0", "").toString());
        } else {
            dataDTO.setAuditAmount(caseShuheUser.getClcUsrAdtLmtItr());
        }
        dataDTO.setExtend(extend.toJSONString());
        return dataDTO;
    }

    /**
     * 判断有值且日期为当天
     */
    private String valuableAndCurrentDay(String dateTimeStr) {
        String value = "0";
        if (StringUtils.isEmpty(dateTimeStr)) {
            return value;
        }
        LocalDate localDate = LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER)
                .atZone(ZoneId.systemDefault()).toLocalDate();
        return LocalDate.now().isEqual(localDate) ? "1" : value;
    }

    /**
     * 查询黑名单
     * true 命中黑名单
     * false 没有命中黑名单
     */
    public boolean queryBlackFlag(MarketingTransferSyncUser transfer, String phone) {
        if (StringUtils.isEmpty(phone)) {
            String reserveField1 = transfer.getReserveField1();
            if (org.apache.commons.lang3.StringUtils.isNotBlank(reserveField1)) {
                JSONObject jsonObject = JSONObject.parseObject(reserveField1);
                phone = jsonObject.getOrDefault("cell", "").toString();
            }
        }
        String key = String.format(KEY, transfer.getApiCode(), transfer.getUserType()
                , transfer.getCustNum()).concat(":" + phone);
        if (redisChgService.exists(key)) {
            return true;
        }
        List<BlackQueryDetailDTO> blackQueryList = new ArrayList<>();
        ReqBlackPhoneQueryDTO dto = new ReqBlackPhoneQueryDTO();
        dto.setApiCode(transfer.getApiCode());
        dto.setDetailBlackPhoneDTO(blackQueryList);
        BlackQueryDetailDTO blackQueryDetailDTO = new BlackQueryDetailDTO();
        String dataId = StringUtils.isEmpty(transfer.getId()) ? null : transfer.getId().toString();
        blackQueryDetailDTO.setDataId(dataId);
        blackQueryDetailDTO.setApiCode(transfer.getApiCode());
        blackQueryDetailDTO.setCaseNum(transfer.getCustNum());
        if (org.apache.commons.lang3.StringUtils.isNotBlank(phone)) {
            blackQueryDetailDTO.setPhone(phone);
            blackQueryDetailDTO.setEncryptType(PhoneEncryptTypeEnum.LOG_TYPE.getEncryptType());
        }
        blackQueryList.add(blackQueryDetailDTO);
        Result<Map<String, String>> result = robotaiApiServiceClient.queryBlackPhone(dto);
        if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            String blackFlag = result.getData().getOrDefault(dataId, "");
            if ("Y".equals(blackFlag)) {
                redisChgService.setex(key, dataId, 3600 * 12);
                return true;
            }
            return false;
        }
        return true;
    }

    /**
     * 查询拨打记录
     * true 存在拨打记录
     * false 不存在拨打记录
     */
    private boolean queryCallRecord(MarketingTransferSyncUser transfer) {
        if (!queryStopPushRecord(transfer)) {
            CallRecordExample example = new CallRecordExample();
            example.createCriteria().andApiCodeEqualTo(transfer.getApiCode())
                    .andCaseNumEqualTo(transfer.getCustNum())
                    .andCreateTimeBetween(
                            Date.from(LocalDateTime.now().toLocalDate().atStartOfDay()
                                    .atZone(ZoneId.systemDefault()).toInstant()),
                            Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
            example.setOrderByClause("create_time desc limit 0,5000");
            List<CallRecord> callRecords = callRecordMapper.selectByExample(example);
            List<CallRecord> collect = callRecords.parallelStream().filter(c ->
                    c.getUserProperties().contains(transfer.getUserType())
                            && org.apache.commons.lang3.StringUtils.isNotBlank(c.getIntentionGrade()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(collect)) {
                return false;
            }
            ShuheTransferStopPushRecord record = new ShuheTransferStopPushRecord();
            ZonedDateTime createTime = LocalDateTime.now().atZone(ZoneId.systemDefault());
            record.setCreateTime(Date.from(createTime.toInstant()));
            ZonedDateTime failureTime = createTime.plusDays(6)
                    .withHour(23).withMinute(59).withSecond(59).withNano(0);
            record.setFailureTime(Date.from(failureTime.toInstant()));
            record.setCaseNum(transfer.getCustNum());
            record.setDay("7");
            record.setUserType(transfer.getUserType());
            record.setTransferSyncCidId(transfer.getId().toString());
            record.setApiCode(transfer.getApiCode());
            record.setStatus("b");
            record.setChannel(0);
            record.setUpdateTime(record.getCreateTime());
            List<Long> ids = collect.parallelStream().map(CallRecord::getId).collect(Collectors.toList());
            record.setCallRecordId(Joiner.on(",").join(ids));
            shuheTransferStopPushRecordMapper.insert(record);
            String key = String.format(KEY, transfer.getApiCode(), transfer.getCustNum(), "b");
            redisChgService.setex(key, "7", (int) ChronoUnit.SECONDS.between(createTime, failureTime));
        }
        return true;
    }

    /**
     * 2022/5/11 15:14
     * 查询暂停推送记录 true 有记录, false 无记录
     */
    public boolean queryStopPushRecord(MarketingTransferSyncUser transfer) {
        String key = String.format(KEY, transfer.getApiCode(), transfer.getCustNum(), "b");
        boolean exists = redisChgService.exists(key);
        if (exists) {
            return true;
        }
        ShuheTransferStopPushRecordExample recordExample = new ShuheTransferStopPushRecordExample();
        recordExample.createCriteria().andApiCodeEqualTo(transfer.getApiCode())
                .andCaseNumEqualTo(transfer.getCustNum()).andUserTypeEqualTo(transfer.getUserType())
                .andFailureTimeGreaterThanOrEqualTo(ObjectUtils.isEmpty(transfer.getCreateTime())
                        ? new Date() : transfer.getCreateTime()).andStatusEqualTo("b").andChannelEqualTo(0);
        int count = shuheTransferStopPushRecordMapper.countByExample(recordExample);
        return count > 0;
    }

    /**
     * 2022/5/9 18:20
     * 是否首次命中b情况
     * true 首次
     * false 非首次
     */
    private boolean phoneSaleExtendInfo(String custNum, String apiCode, String userType) {
        PhoneSaleExtendInfoExample example = new PhoneSaleExtendInfoExample();
        example.createCriteria().andStatusEqualTo("b")
                .andApiCodeEqualTo(apiCode).andUserTypeEqualTo(userType)
                .andCustNumEqualTo(custNum);
        int count = phoneSaleExtendInfoMapper.countByExample(example);
        return count < 1;
    }
}
