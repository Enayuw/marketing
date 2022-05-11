package com.br.marketing.rule.shuhe;

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
import com.br.marketing.dto.shuhe.strategy.CuFuJie;
import com.br.marketing.dto.shuhe.strategy.IUserType;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.CallRecordMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.ShuheTransferStopPushRecordMapper;
import com.br.marketing.origin.DataLoadingHandlerService;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.IDxService;
import com.br.marketing.service.PushDataService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
public class ShuHeArtificialRealTimeUserDataFromDelayImpl implements AssembleData<RealTimeUserDataDTO> {

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
    @Resource
    private DataLoadingHandlerService handlerService;
    @Resource
    private PushDataService pushDataService;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private IDxService iDxService;
    @Resource
    private CallRecordMapper callRecordMapper;
    @Resource
    private ShuheTransferStopPushRecordMapper shuheTransferStopPushRecordMapper;
    @Resource
    private RedisChgService redisChgService;

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
                IUserType iUserType = shuHeContext.getIUserType();
                if (iUserType instanceof CuFuJie) {
                    String message = context.getMqFact().getMessage();
                    JSONObject jsonObject = JSONObject.parseObject(message);
                    String status = jsonObject.get("status").toString();
                    caseShuheUser.getJsonObject().putAll(jsonObject);
                    caseShuheUser.setReserveField2(status);
                    boolean boolIfGiveUp = iUserType.ifGiveUp(caseShuheUser, shuHeContext.getCreatTime());
                    String cell = shuHeContext.getCustomerMap().getOrDefault(transfer.getCustNum()
                            , new MarketingSyncUser()).getCell();
                    if (boolIfGiveUp || queryBlackFlag(transfer, cell)) {
                        log.warn("促复借判断boolIfGiveUp结果：{}； 判断黑名单结果：{}", boolIfGiveUp, true);
                        return false;
                    }
                    switch (status) {
                        case "a":
                            bool = pushDataService.pushShDXSingleMutex(transfer.getApiCode(), transfer.getCustNum()
                                    , "a", transfer.getUserType());
                            log.warn("促复借a情况：一天只推送一次判断结果：{}", bool);
                            break;
                        case "b":
                            if (queryCallRecord(transfer)) {
                                bool = pushDataService.pushShDXSingleMutex(transfer.getApiCode(), transfer.getCustNum()
                                        , "b", transfer.getUserType());
                                log.warn("促复借b情况：一天只推送一次判断结果：{}", bool);
                            }
                            break;
                        default:
                    }
                    shuHeContext.setCaseShuheUser(caseShuheUser);
                } else {
                    bool = !iUserType.ifGiveUp(caseShuheUser, shuHeContext.getCreatTime())
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
        if (shuHeContext.getIUserType() instanceof CuFuJie) {
            phoneSaleExtendInfo.setStatus(shuHeContext.getCaseShuheUser().getReserveField2());
        }
        phoneSaleExtendInfo.setApiCode(transfer.getApiCode());
        phoneSaleExtendInfo.setUserType(transfer.getUserType());
        phoneSaleExtendInfo.setTaskId(shuHeContext.getTaskId());
        return phoneSaleExtendInfo;
    }

    private DassSingleImportDataDTO getDassSingleImportData(ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData shuHeContext) {
        CaseShuheUser caseShuheUser = shuHeContext.getCaseShuheUser();
        IUserType iUserType = shuHeContext.getIUserType();
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
        iUserType.getPrivateInfo(dataDTO);
        dataDTO.setUid(caseShuheUser.getCustNum());
        dataDTO.setAuditAmount(caseShuheUser.getClcUsrAdtLmtItr());
        if (iUserType instanceof CuFuJie) {
            JSONObject jsonObject = caseShuheUser.getJsonObject();
            String lv0 = jsonObject.getOrDefault("clc_usr_avl_lmt_lv0", "").toString();
            if (org.apache.commons.lang3.StringUtils.isNotBlank(lv0)) {
                extend.put("clc_usr_avl_lmt_lv0", lv0);
            }
            String typeSign = jsonObject.getOrDefault("typeSign", "").toString();
            if (org.apache.commons.lang3.StringUtils.isNotBlank(typeSign)) {
                extend.put("typeSign", typeSign);
            }
            dataDTO.setPrioritySymbol(jsonObject.getOrDefault("prioritySymbol", "").toString());
            String name = shuHeContext.getCustomerMap().get(caseShuheUser.getCustNum()).getName();
            if (!StringUtils.isEmpty(name)) {
                try {
                    dataDTO.setName(BrCipherMaker.getInstance().decode(name));
                } catch (Exception ignored) {
                }
            }
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
     */
    public boolean queryBlackFlag(MarketingTransferSyncUser transfer, String phone) {
        List<BlackQueryDetailDTO> blackQueryDetailDTOS = new ArrayList<>();
        ReqBlackPhoneQueryDTO dto = new ReqBlackPhoneQueryDTO();
        dto.setApiCode(transfer.getApiCode());
        dto.setDetailBlackPhoneDTO(blackQueryDetailDTOS);
        BlackQueryDetailDTO blackQueryDetailDTO = new BlackQueryDetailDTO();
        blackQueryDetailDTO.setDataId(transfer.getId().toString());
        blackQueryDetailDTO.setApiCode(transfer.getApiCode());
        blackQueryDetailDTO.setCaseNum(transfer.getCustNum());
        if (StringUtils.isEmpty(phone)) {
            String reserveField1 = transfer.getReserveField1();
            if (org.apache.commons.lang3.StringUtils.isNotBlank(reserveField1)) {
                JSONObject jsonObject = JSONObject.parseObject(reserveField1);
                phone = jsonObject.getOrDefault("cell", "").toString();
            }
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(phone)) {
            blackQueryDetailDTO.setPhone(phone);
            blackQueryDetailDTO.setEncryptType(PhoneEncryptTypeEnum.LOG_TYPE.getEncryptType());
        }
        blackQueryDetailDTOS.add(blackQueryDetailDTO);
        Result<Map<String, String>> result = robotaiApiServiceClient.queryBlackPhone(dto);
        log.warn("#促复借 查询黑名单条件{}\n结果:状态码:{}\n消息:{}", dto, result.getCode(), result.getData());
        if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            String blackFlag = result.getData().getOrDefault(transfer.getId().toString(), "");
            return "Y".equals(blackFlag);
        }
        return true;
    }

    /**
     * 查询拨打记录
     */
    private boolean queryCallRecord(MarketingTransferSyncUser transfer) {
        String key = String.format(KEY, transfer.getApiCode(), transfer.getCustNum(), "b");
        boolean exists = redisChgService.exists(key);
        if (exists) {
            log.warn("#促复借 查询拨打记录结果是否已经存在缓存中:{}", exists);
            return false;
        }
        ShuheTransferStopPushRecordExample recordExample = new ShuheTransferStopPushRecordExample();
        recordExample.createCriteria().andApiCodeEqualTo(transfer.getApiCode())
                .andCaseNumEqualTo(transfer.getCustNum()).andUserTypeEqualTo(transfer.getUserType())
                .andFailureTimeGreaterThanOrEqualTo(new Date());
        List<ShuheTransferStopPushRecord> list = shuheTransferStopPushRecordMapper.selectByExample(recordExample);
        if (CollectionUtils.isEmpty(list)) {
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
                log.warn("#促复借 查询拨打记录结果记录过滤后结果:{}", Arrays.toString(collect.toArray()));
                return true;
            }
            ShuheTransferStopPushRecord record = new ShuheTransferStopPushRecord();
            LocalDateTime localDateTime = LocalDateTime.now();
            record.setCreateTime(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()));
            record.setFailureTime(Date.from(localDateTime.plusDays(6).atZone(ZoneId.systemDefault()).toInstant()));
            record.setCaseNum(transfer.getCustNum());
            record.setDay("6");
            record.setUserType(transfer.getUserType());
            record.setTransferSyncCidId(transfer.getId().toString());
            record.setApiCode(transfer.getApiCode());
            record.setStatus("b");
            record.setChannel(0);
            record.setUpdateTime(record.getCreateTime());
            List<Long> ids = collect.parallelStream().map(CallRecord::getId).collect(Collectors.toList());
            record.setCallRecordId(Joiner.on(",").join(ids));
            shuheTransferStopPushRecordMapper.insert(record);
            redisChgService.setex(key, "6", 6 * 24 * 60 * 60);
            log.warn("#促复借 查询拨打记录结果记录到数据库的ShuheTransferStopPushRecord表中:{}", record);
        }
        return false;
    }
}
