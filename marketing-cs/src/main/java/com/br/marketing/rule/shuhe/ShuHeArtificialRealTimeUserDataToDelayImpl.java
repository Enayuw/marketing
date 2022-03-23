package com.br.marketing.rule.shuhe;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.ShuHeRuleCollectDataImpl;
import com.br.marketing.dto.shuhe.strategy.IUserType;
import com.br.marketing.entity.CaseShuheUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUserExample;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.origin.DataLoadingHandlerService;
import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.TransferSource;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

/**
 * 符合人工的数据进入延迟
 */
@Service
@Slf4j
public class ShuHeArtificialRealTimeUserDataToDelayImpl implements AssembleData<MqFact> {
    @Resource
    private IMarketingSyncUserService iMarketingSyncUserService;
    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
    @Resource
    private RedisChgService redisChgService;
    @Resource
    private DataLoadingHandlerService handlerService;

    /**
     * apiCoid:userType:cusNum
     */
    public final static String KEY = "marketing:api:transfer:shuhe:%s:%s:%s";

    @Override
    public MqFact assemble(Object transmitFact, ProcessHandlerContext context) {
        MqFact mqFact = context.getMqFact();
        MqFact mqFactNew = new MqFact();
        mqFactNew.setSource(TransferSource.UNIVERSAL_TRANSFER_PROCESS.getCode());
        mqFactNew.setIsDelay(1);
        mqFactNew.setSourceId(mqFact.getSourceId());
        mqFactNew.setMessage(mqFact.getMessage());
        mqFactNew.setIncludeRules(mqFact.getIncludeRules());
        mqFact.setIsDelay(0);
        return mqFactNew;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        boolean bool = Boolean.FALSE;
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData shuHeContext =
                    (ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData) context.getRuleNecessaryData();
            final IUserType iUserType = shuHeContext.getIUserType();
            final Integer isDelay = context.getMqFact().getIsDelay();
            boolean typeBool = (isDelay == null || isDelay != 1);
            if (typeBool) {
                final CaseShuheUser caseShuheUser = shuHeContext.getCaseShuheUser();
                final Date creatTime = shuHeContext.getCreatTime();
                boolean b = iUserType.dataPeriodOfValidity(iMarketingSyncUserService, creatTime);
                bool = (b && iUserType.isSatisfyPhoneSale(caseShuheUser, creatTime)
                        && cacheExists(transfer));
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

    private boolean cacheExists(MarketingTransferSyncUser transfer) {
        final String custNum = transfer.getCustNum();
        final String apiCode = transfer.getApiCode();
        final String userType = transfer.getUserType();
        String key = String.format(KEY, apiCode, userType, custNum);
        try {
            boolean exists = redisChgService.exists(key);
            if (!exists) {
                String tCid = StringUtils.isEmpty(transfer.gettCid()) ? handlerService.getTcIdFromRedis(apiCode)
                        : transfer.gettCid();
                if (getDbTransferSyncUser(custNum, apiCode, userType, transfer.getId(), tCid
                        , transfer.getCreateTime())) {
                    long setnx = redisChgService.setnx(key, tCid, (int) getKeyExpiration());
                    return setnx == 1;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return getDbTransferSyncUser(custNum, apiCode, userType, transfer.getId(), transfer.gettCid()
                    , transfer.getCreateTime());
        }
        return false;
    }

    /**
     * 查询db获取cusNum当天最早的数据
     */
    private boolean getDbTransferSyncUser(String custNum, String apiCode, String userType, long id
            , String tCid
            , Date createTime
    ) {
        MarketingTransferSyncUserExample example = new MarketingTransferSyncUserExample();
        example.createCriteria().andApiCodeEqualTo(apiCode).andUserTypeEqualTo(userType)
                .andCustNumEqualTo(custNum).andCreateTimeBetween(Date.from(
                LocalDateTime.now().toLocalDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())
                , createTime);
        example.settCid(tCid);
        example.setOrderByClause("create_time asc limit 0,1");
        List<MarketingTransferSyncUser> transferList = marketingTransferSyncUserMapper.selectByExample(example);
        return transferList.size() > 0 && transferList.get(0).getId().equals(id);
    }

}
