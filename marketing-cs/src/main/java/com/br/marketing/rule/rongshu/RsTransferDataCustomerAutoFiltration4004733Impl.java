package com.br.marketing.rule.rongshu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.robotaiapi.input.ConversionDataDTO;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.RsCollectDataImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;


/**
 * 榕树转化数据自动过滤推客服
 * 需求地址：https://c.100credit.cn/pages/viewpage.action?pageId=186209870
 * 走自动化规则的 转化数据来源是  4004643 给转化接口上传的数据，
 * 然后按照需求处理规则，填补手机号去4004643上传表找，最后传给客服过滤接口的时候把参数中的apiCode换成 4004733
 * @Author yu.xia@brgroup.com
 * @Date 2024/11/18 11:49
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RsTransferDataCustomerAutoFiltration4004733Impl implements AssembleData<ConversionDataDTO> {

    private final static String INVERSION_STATUS_2="2";
    private final static String CASE_EFFECTIVE_0="0";
    private final MarketingCommonConfig marketingCommonConfig;
    private final TableCreateServiceImpl tableCreateService;
    private final TransferDataValidityPeriodService transferDataValidityPeriodService;
    private final MarketingSyncUserMapper marketingSyncUserMapper;

    @Override
    public ConversionDataDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        ConversionDataDTO conversionData = new ConversionDataDTO();
        String apiCode = context.getApiCode();
        conversionData.setCid(tableCreateService.getCId(apiCode));
        String custNum = transfer.getCustNum();
        conversionData.setCaseNum(custNum);
        conversionData.setDataId(transfer.getId().toString());
        if(isBlack1(transfer)){
            // ExpireDate不进行设置(永久)
            conversionData.setInversionStatus(INVERSION_STATUS_2);
            MarketingSyncUser marketingSyncUser = marketingSyncUserMapper.selectSynsUserByCustNumLastWithStatus(apiCode, custNum);
            if(null != marketingSyncUser && StringUtils.isNotBlank(marketingSyncUser.getCell())){
                conversionData.setPhone(BrCipherMaker.getInstance().decode(marketingSyncUser.getCell()));
            }else{
                log.warn("apiCode[{}]custNum[{}]榕树转化数据自动过滤推客服isBlack=1未发现手机号"
                        , apiCode, custNum);
                return null;
            }
        }else{
            // 新版本有效期判断
            Set<String> custNumSet = new HashSet<>();
            custNumSet.add(custNum);
            Map<String, SyncUserValidityPeriodsBO> validityPeriodsByCustNum =
                    transferDataValidityPeriodService.getValidityPeriodsByCustNum(custNumSet, apiCode, new Date());
            SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = validityPeriodsByCustNum.get(custNum);
            if (syncUserValidityPeriodsBO == null || null == syncUserValidityPeriodsBO.getSyncUsers()) {
                log.warn("apiCode[{}]custNum[{}]不满足rs案件编号[有效期内]条件", apiCode, custNum);
                return null;
            }
            PeriodOfValidityBO periodOfValidityBO = syncUserValidityPeriodsBO.getBuilders().get(0).addDateTimeString().builder();
            String enDateTimeString = periodOfValidityBO.getEnDateTimeStr();
            conversionData.setExpireDate(enDateTimeString);
            if(isCaseEffective0(transfer)){
                conversionData.setInversionStatus(INVERSION_STATUS_2);
            }else{
                log.warn("apiCode[{}]custNum[{}]出现rs运营自动化过滤未预期的结果", apiCode, custNum);
            }
            MarketingSyncUser marketingSyncUser = syncUserValidityPeriodsBO.getSyncUsers().get(0);
            if (marketingSyncUser != null) {
                conversionData.setPhone(BrCipherMaker.getInstance().decode(marketingSyncUser.getCell()));
            }
        }
        if (!StringUtils.isEmpty(transfer.getCreateTime())){
            conversionData.setPartnerProcessDate(DateUtils.format(transfer.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        }
        HashMap<String, JSONObject> strategyCodeMap = marketingCommonConfig.getRongShuPushPolicyStrategyCode();
        JSONObject apiCodeReplace = strategyCodeMap.get("autoFiltrationApiCodeReplace");
        // 2024-11-18 apiCode:4004643转化数据，按照规则生成后4004733数据推送至过滤
        if(null != apiCodeReplace && !apiCodeReplace.isEmpty()){
            if(com.br.common.util.StringUtils.isNotBlank(apiCodeReplace.getString(apiCode))){
                apiCode = apiCodeReplace.getString(apiCode);
            }else{
                log.warn("未发现rs-apiCode[{}]替换配置[{}]",apiCode, strategyCodeMap);
            }
        }
        conversionData.setApiCode(apiCode);
        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            if(isBlack1(transfer)){
                return true;
            }
            RsCollectDataImpl.RsRuleNecessaryData ruleNecessaryData =
                    (RsCollectDataImpl.RsRuleNecessaryData) context.getRuleNecessaryData();
            Map<String, MarketingSyncUser> customerMap = ruleNecessaryData.getCustomerMap();
            MarketingSyncUser marketingSyncUser = getSyncUser(customerMap, transfer.getCustNum());
            //用户非空判断
            if (marketingSyncUser == null) {
                return false;
            }
            // caseEffective=0
            return isCaseEffective0(transfer);
        }
        return false;
    }

    private boolean isBlack1(MarketingTransferSyncUser transfer){
        String reserveField1 = transfer.getReserveField1();
        String isBlack = null;
        if (StringUtils.isNotBlank(reserveField1)) {
            JSONObject reserveField1Json = JSON.parseObject(reserveField1);
            isBlack = reserveField1Json.getString("isBlack");
        }
        if(StringUtils.isNotBlank(isBlack) && "1".equals(isBlack)){
            return true;
        }
        return false;
    }
    private boolean isCaseEffective0(MarketingTransferSyncUser transfer){
        String caseEffective = transfer.getCaseEffective();
        if(StringUtils.isNotBlank(caseEffective) && CASE_EFFECTIVE_0.equalsIgnoreCase(caseEffective)){
            return true;
        }
        return false;
    }

    @Override
    public String label() {
        return "RongShu_TransferData_Customer_Auto_Filtration4004733";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_AUTO_FILTRATION_RS.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.RS_DATA_COLLECTION.getCode();
    }
}
