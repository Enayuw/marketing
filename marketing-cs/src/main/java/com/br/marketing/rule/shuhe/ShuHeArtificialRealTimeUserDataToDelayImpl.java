package com.br.marketing.rule.shuhe;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.dto.shuhe.strategy.CuShenWan;
import com.br.marketing.dto.shuhe.strategy.IUserType;
import com.br.marketing.entity.CaseShuheUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUserExample;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.origin.*;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.service.IPushShuheTransferDataService;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
    private IPushShuheTransferDataService iPushShuheTransferDataService;
    @Resource
    private IMarketingSyncUserService iMarketingSyncUserService;
    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
    @Resource
    private RedisChgService redisChgService;
    @Resource
    private DataLoadingHandlerService handlerService;

    @Resource
    private TableCreateServiceImpl tableCreateService;

    /**
     * apiCoid:userType:cusNum
     */
    public final static String KEY = "marketing:api:transfer:shuhe:%s:%s:%s";

    @Override
    public MqFact assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        MqFact mqFact = new MqFact();
        mqFact.setSourceId(transfer.getId());
        mqFact.setSource(TransferSource.UNIVERSAL_TRANSFER_PROCESS.getCode());
        mqFact.setIsDelay(1);
        return mqFact;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        if (!(context instanceof ShuHeProcessHandlerContext)) {
            ShuHeProcessHandlerContext shuHeContext = new ShuHeProcessHandlerContext(context);
            iPushShuheTransferDataService.handlerContext(shuHeContext, transfer);
            context = shuHeContext;
        }
        ShuHeProcessHandlerContext shuHeContext = (ShuHeProcessHandlerContext) context;
        final IUserType iUserType = shuHeContext.getiUserType();
        if ((iUserType instanceof CuShenWan) && (shuHeContext.getMqFact().getIsDelay() != 1)) {
            final CaseShuheUser caseShuheUser = shuHeContext.getCaseShuheUser();
            final Date creatTime = shuHeContext.getCreatTime();
            boolean b = iUserType.dataPeriodOfValidity(iMarketingSyncUserService, creatTime);
            if (b && ((CuShenWan) iUserType).isSatisfyPhoneSale(caseShuheUser, creatTime) && cacheExists(transfer)) {
                log.warn("##数禾满足延迟队列规则##{}:{}:{}", transfer.getCustNum(),
                        transfer.getApiCode(), transfer.getUserType());
                return true;
            }
        }
        log.warn("@@数禾不满足延迟队列规则@@{}:{}:{}", transfer.getCustNum(),
                transfer.getApiCode(), transfer.getUserType());
        return false;
    }

    @Override
    public String label() {
        return "ShuHe_TransferData_ArtificialRealTimeUserDataToDelay";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.MESSAGE_DELAY.getCode();
    }

    /**
     * 获取当前时间到第二天凌晨的秒
     */
    private long getKeyExpiration() {
        LocalDateTime now = LocalDateTime.now();
        // 当前毫秒数
        long l = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        LocalDateTime localDateTime = now.plusDays(1);
        // 第二天凌晨
        final ZonedDateTime zonedDateTime = localDateTime.toLocalDate().atStartOfDay().atZone(ZoneId.systemDefault());
        System.out.println(zonedDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
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
                String tCid = handlerService.getTcIdFromRedis(apiCode);
                if (getDbTransferSyncUser(custNum, apiCode, userType, transfer.getId(), tCid, transfer.getCreateTime())) {
                    long setnx = redisChgService.setnx(key, tCid, (int) getKeyExpiration());
                    return setnx == 1;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            String tCid = tableCreateService.getTcId(apiCode);
            return getDbTransferSyncUser(custNum, apiCode, userType, transfer.getId(), tCid, transfer.getCreateTime());
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
