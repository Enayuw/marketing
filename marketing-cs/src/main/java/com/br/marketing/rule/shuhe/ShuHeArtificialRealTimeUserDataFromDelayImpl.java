package com.br.marketing.rule.shuhe;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportAdapDTO;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.RealTimeUserDataDTO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.ShuHeRuleCollectDataImpl;
import com.br.marketing.dto.shuhe.strategy.IUserType;
import com.br.marketing.entity.CaseShuheUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUserExample;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.origin.DataLoadingHandlerService;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.PushDataService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

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

    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    @Override
    public RealTimeUserDataDTO assemble(Object transmitFact, ProcessHandlerContext context) {
        ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData shuHeContext =
                (ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData) context.getRuleNecessaryData();
        MarketingTransferSyncUser transfer = shuHeContext.getTransfer();
        RealTimeUserDataDTO realTimeUserDataDTO = new RealTimeUserDataDTO();
        realTimeUserDataDTO.setDassSingleImportAdapDTO(getDassSingleImportAdap(shuHeContext));
        realTimeUserDataDTO.getDassSingleImportAdapDTO().setTransferInfoId(context.getTransferInfoId());
        realTimeUserDataDTO.setPhoneSaleExtendInfo(getPhoneSaleExtendShuhe(transfer, shuHeContext));
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
                bool = !iUserType.ifGiveUp(caseShuheUser, shuHeContext.getCreatTime())
                        && pushDataService.pushShDXSingleMutex(transfer.getApiCode(), transfer.getCustNum()
                        , "a", transfer.getUserType());
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
        CaseShuheUser caseShuheUser = shuHeContext.getCaseShuheUser();
        IUserType iUserType = shuHeContext.getIUserType();
        DassSingleImportAdapDTO adapDTO = new DassSingleImportAdapDTO();
        adapDTO.setDassSingleImportDataDTO(getDassSingleImportData(caseShuheUser, iUserType));
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
        phoneSaleExtendInfo.setAppletTime(localDateTime.format(DATE_TIME_FORMATTER));
        phoneSaleExtendInfo.setStatus("a");
        phoneSaleExtendInfo.setApiCode(transfer.getApiCode());
        phoneSaleExtendInfo.setUserType(transfer.getUserType());
        phoneSaleExtendInfo.setTaskId(shuHeContext.getTaskId());
        return phoneSaleExtendInfo;
    }

    private DassSingleImportDataDTO getDassSingleImportData(CaseShuheUser caseShuheUser, IUserType iUserType) {
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
        dataDTO.setExtend(extend.toJSONString());
        dataDTO.setUid(caseShuheUser.getCustNum());
        dataDTO.setAuditAmount(caseShuheUser.getClcUsrAdtLmtItr());
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
}
