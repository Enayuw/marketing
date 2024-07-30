package com.br.marketing.rule.shuhe;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.ShuHeRuleCollectDataImpl;
import com.br.marketing.dto.shuhe.strategy.BaseUserType;
import com.br.marketing.dto.shuhe.strategy.CuFuJie;
import com.br.marketing.dto.shuhe.strategy.CuShouDeng;
import com.br.marketing.dto.shuhe.strategy.CuShouJie;
import com.br.marketing.entity.CaseShuheUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUserExample;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.origin.DataLoadingHandlerService;
import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.TransferSource;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.service.Impl.SystemExceptionServiceImpl;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.List;

/**
 * 符合人工的数据进入延迟
 *
 * @author Guo Zeqiang
 * @dateTime 2022/3/18 14:45
 */
@Service
@Slf4j
public class ShuHeArtificialCallToDelayImpl implements AssembleData<MqFact> {
    @Resource
    private IMarketingSyncUserService iMarketingSyncUserService;
    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
    @Resource
    private RedisChgService redisChgService;
    @Resource
    private DataLoadingHandlerService handlerService;
    @Resource
    private SystemExceptionServiceImpl systemExceptionService;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private TableCreateServiceImpl tableCreateService;

    /**
     * apiCoid:userType:cusNum
     */
    public final static String KEY = "marketing:api:transfer:shuhe:%s:%s:%s";

    @Override
    public MqFact assemble(Object transmitFact, ProcessHandlerContext context) {
        ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData shuHeContext =
                (ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData) context.getRuleNecessaryData();
        MqFact mqFact = context.getMqFact();
        MqFact mqFactNew = new MqFact();
        mqFactNew.setSource(TransferSource.UNIVERSAL_TRANSFER_PROCESS.getCode());
        mqFactNew.setIsDelay(1);
        mqFactNew.setSourceId(mqFact.getSourceId());
        mqFactNew.setIncludeRules(mqFact.getIncludeRules());
        mqFact.setIsDelay(0);
        if (shuHeContext.getBaseUserType() instanceof CuFuJie) {
            mqFactNew.setDelayTime(0.5F);
            JSONObject jsonObject = new JSONObject();
            CaseShuheUser caseShuheUser = shuHeContext.getCaseShuheUser();
            jsonObject.put("status", caseShuheUser.getReserveField2());
            jsonObject.put("prioritySymbol", caseShuheUser.getJsonObject().getOrDefault("prioritySymbol", ""));
            jsonObject.put("typeSign", caseShuheUser.getJsonObject().getOrDefault("typeSign", ""));
            mqFactNew.setMessage(jsonObject.toJSONString());
        }
        return mqFactNew;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws IllegalAccessException {
        boolean bool = Boolean.FALSE;
        if (transmitFact instanceof MarketingTransferSyncUser) {
            ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData shuHeContext =
                    (ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData) context.getRuleNecessaryData();
            final BaseUserType baseUserType = shuHeContext.getBaseUserType();
            if (baseUserType instanceof CuShouDeng) {
                return false;
            }
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            final Integer isDelay = context.getMqFact().getIsDelay();
            boolean typeBool = (isDelay == null || isDelay != 1);
            if (typeBool) {
                final CaseShuheUser caseShuheUser = shuHeContext.getCaseShuheUser();
                shuHeContext.setTransfer(transfer);
                Date creatTime = shuHeContext.getCreatTime();
                Integer day = handlerService.getShuHePeriodOfValidityDay(caseShuheUser.getUserType());
                boolean b = baseUserType.dataPeriodOfValidity(iMarketingSyncUserService
                        , transfer.getCreateTime(), day, creatTime);
                if (b && baseUserType instanceof CuShouJie && !(baseUserType.getApiCodes().contains(transfer.getApiCode()))
                ) {
                    systemExceptionService.sendAlarm(String.format(
                            "检测到数禾客户推送转化数据存在异常：该apiCode下不应该出现该场景的数据！" +
                                    "\n场景:%s\nApiCode:%s\n案件编号:%s\n请及时跟进^_^"
                            , transfer.getUserType(), transfer.getApiCode(), transfer.getCustNum())
                            , "MARKETING-INNER-API");
                    b = Boolean.FALSE;
                }
                if (baseUserType instanceof CuFuJie) {
                    if (b) {
                        boolean phoneSale = ((CuFuJie) baseUserType).isSatisfyPhoneSale(caseShuheUser, creatTime
                                , marketingCommonConfig);
                        if (phoneSale) {
                            bool = periodOfValidityTransform(caseShuheUser, day, creatTime)
                                    && cacheExists(transfer, shuHeContext, day);
                        }
                    }
                    shuHeContext.setCaseShuheUser(caseShuheUser);
                } else {
                    bool = (b && baseUserType.isSatisfyPhoneSale(caseShuheUser, creatTime)
                            && cacheExists(transfer, shuHeContext, day));
                }
                shuHeContext.setTransfer(null);
            }
        }
        return bool;
    }

    @Override
    public String label() {
        return "ShuHe_3_TransferData_ArtificialRealTimeUserDataToDelay";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.MESSAGE_DELAY.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.SHU_HE_RULE_DATA_COLLECTION.getCode();
    }

    /**
     * 获取当前时间到第二天凌晨的秒
     */
    private long getKeyExpiration() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime localDateTime = now.plusDays(1);
        // 第二天凌晨
        final ZonedDateTime zonedDateTime = localDateTime.toLocalDate().atStartOfDay().atZone(ZoneId.systemDefault());
        return ChronoUnit.SECONDS.between(now, zonedDateTime);
    }

    /**
     * 检查缓存中是否存在过满足规则的cusNum
     */
    private boolean cacheExists(MarketingTransferSyncUser transfer
            , ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData shuHeContext
            , Integer day) {
        final String custNum = transfer.getCustNum();
        final String apiCode = transfer.getApiCode();
        final String userType = transfer.getUserType();
        final Date createTime = transfer.getCreateTime();
        String key = String.format(KEY, apiCode, userType, custNum);
        try {
            return redisChgService.setnx(key, "{\"millis\":\""
                            + System.currentTimeMillis() + "\",\"id\":\"" + transfer.getId() + "\"}"
                    , (int) getKeyExpiration());
//            if (ret == 1) {
//                String tCid = StringUtils.isEmpty(transfer.gettCid()) ? handlerService.getTcIdFromRedis(apiCode)
//                        : transfer.gettCid();
//                boolean b = checkDbData(custNum, apiCode, userType, tCid, createTime, transfer.getId(), day, shuHeContext);
//                if (!b) {
//                    // 更新缓存中的值为当天案件编号为首次满足规则的id
//                    redisChgService.setex(String.format(KEY, apiCode, userType, custNum)
//                            , "{\"millis\":\"" + System.currentTimeMillis()
//                                    + "\",\"id\":\"" + shuHeContext.getTransfer().getId() + "\"}"
//                            , (int) getKeyExpiration());
//                    shuHeContext.setTransfer(null);
//                }
//                return b;
//            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return checkDbData(custNum, apiCode, userType, transfer.gettCid(), createTime, transfer.getId(), day, shuHeContext);
        }
//        return false;
    }

    /**
     * 查询db获取cusNum当天符合延迟规则的最新的数据集合
     */
    private List<MarketingTransferSyncUser> getDbTransferSyncUser(String custNum, String apiCode, String userType, String tCid, Date createTime) {
        MarketingTransferSyncUserExample example = new MarketingTransferSyncUserExample();
        example.createCriteria().andApiCodeEqualTo(apiCode).andUserTypeEqualTo(userType)
                .andCustNumEqualTo(custNum).andCreateTimeBetween(Date.from(
                LocalDateTime.now().toLocalDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())
                , createTime);
        example.settCid(tCid);
        example.setOrderByClause("create_time DESC limit 0,2000");
        return marketingTransferSyncUserMapper.selectByExample(example);
    }

    /**
     * 检查db获取cusNum当天的数据集合，判断当前cusNum是否是符合延迟规则的最早的cusNum
     */
    private boolean checkDbData(String custNum,
                                String apiCode,
                                String userType,
                                String tCid,
                                Date createTime,
                                Long id,
                                Integer day,
                                ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData shuHeContext) {
        List<MarketingTransferSyncUser> list = getDbTransferSyncUser(
                custNum, apiCode, userType, tCid, createTime);
        if (list.size() == 1 && list.get(0).getId().equals(id)) {
            return true;
        }
        int size = list.size();
        int mark = 0;
        for (int i = 0; i < size; i++) {
            if (list.get(i).getId().equals(id)) {
                mark = i + 1;
                break;
            }
        }
        CaseShuheUser caseShuheUser = shuHeContext.getCaseShuheUser();
        BaseUserType baseUserType = shuHeContext.getBaseUserType();
        Date creatTime = shuHeContext.getCreatTime();
        for (int i = mark; i < size; i++) {
            MarketingTransferSyncUser transferSyncUser = list.get(i);
            shuHeContext.setTransfer(transferSyncUser);
            boolean b = baseUserType.dataPeriodOfValidity(iMarketingSyncUserService
                    , transferSyncUser.getCreateTime(), day, creatTime)
                    && baseUserType.isSatisfyPhoneSale(caseShuheUser, creatTime);
            if (b) {
                return false;
            }
        }
        return true;
    }

    /**
     * 2022/5/9 17:22
     * 查询有效期内是否存在已转化的数据
     */
    private boolean periodOfValidityTransform(CaseShuheUser caseShuheUser, Integer day, Date creatTime) {
        String cId = redisChgService.get("marketing:api:shuhe:transfer:cid:"
                .concat(caseShuheUser.getApiCode()));
        String tcId;
        if (StringUtils.isEmpty(cId)) {
            tcId = tableCreateService.getTcId(caseShuheUser.getApiCode());
        } else {
            tcId = cId.replaceFirst("-", "");
        }
        if (ObjectUtils.isEmpty(creatTime)) {
            creatTime = new Date();
        }
        LocalDateTime dateTime;
        if (day == null) {
            dateTime = creatTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().with(
                    TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59).atZone(
                    ZoneId.systemDefault()).toLocalDateTime();
        } else {
            dateTime = creatTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().plusDays(day).withHour(23)
                    .withMinute(59).withSecond(59).atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        LocalDateTime time = creatTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                .atStartOfDay().atZone(ZoneId.systemDefault()).toLocalDateTime();
        MarketingTransferSyncUserExample example = new MarketingTransferSyncUserExample();
        example.settCid(tcId);
        example.createCriteria().andApiCodeEqualTo(caseShuheUser.getApiCode())
                .andCustNumEqualTo(caseShuheUser.getCustNum())
                .andUserTypeEqualTo(caseShuheUser.getUserType()).andCreateTimeBetween(
                Date.from(time.atZone(ZoneId.systemDefault()).toInstant())
                , Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()))
                .andIfTransformEqualTo("1");
        int count = marketingTransferSyncUserMapper.countByExample(example);
        return count < 1;
    }

}
