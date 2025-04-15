package com.br.marketing.rule.rongshu;

import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportAdapSoleDTO;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.RealTimeUserDataSoleDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.DistributeSourceTypeEnum;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.RsxkCollectDataImpl;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.dto.rsxk.CallStatusDTO;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.origin.MqFact;
import com.br.marketing.rsxk.RsxkClient;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

/**
 * @Author: dongshuo.he
 * @Date: 2025/3/26 15:27
 * D20250314榕树新客自运营ab意向自动化转Daas-4004739/4004713-推送规则
 * https://c.100credit.cn/pages/viewpage.action?pageId=201076403
 */
@Service
@Slf4j
public class RsxkCallRecordToDassImpl implements AssembleData<RealTimeUserDataSoleDTO> {

    @Resource
    MarketingCommonConfig marketingCommonConfig;

    @Resource
    RsxkClient rsxkClient;

    @Override
    public RealTimeUserDataSoleDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        CallRecordBO bo = (CallRecordBO) transmitFact;
        RsxkCollectDataImpl.RsxkRuleNecessaryData ruleNecessaryData =
                (RsxkCollectDataImpl.RsxkRuleNecessaryData) context.getRuleNecessaryData();
        Map<String, SyncUserValidityPeriodBO> syncUserPeriodMap = ruleNecessaryData.getSyncUserPeriodMap();
        SyncUserValidityPeriodBO syncUserData = syncUserPeriodMap.get(bo.getCaseNum());
        MarketingSyncUser syncUser = syncUserData.getSyncUser();
        return buildRealTimeUserDataSoleDTO(bo, syncUser);
    }

    /**
     * 构建推送Dass数据
     * @param bo
     * @param syncUser
     * @return
     */
    private RealTimeUserDataSoleDTO buildRealTimeUserDataSoleDTO(CallRecordBO bo, MarketingSyncUser syncUser) {
        RealTimeUserDataSoleDTO realTimeUserDataSoleDTO = new RealTimeUserDataSoleDTO();
        DassSingleImportAdapSoleDTO dassSingleImportAdapSoleDTO = new DassSingleImportAdapSoleDTO();
        JSONObject userTypeConfig = marketingCommonConfig.getRsxkToDassUserTypeConfig();
        JSONObject configForApiCode = userTypeConfig.getJSONObject(bo.getApiCode());
        String userType = configForApiCode.getString(syncUser.getUserType());
        buildDassSingleImportAdapSoleDTO(dassSingleImportAdapSoleDTO, syncUser, userType);
        PhoneSaleExtendInfo phoneSaleExtendInfo = new PhoneSaleExtendInfo();
        buildPhoneSaleExtendInfo(phoneSaleExtendInfo, bo, syncUser, userType);
        realTimeUserDataSoleDTO.setDassSingleImportAdapDTO(dassSingleImportAdapSoleDTO);
        realTimeUserDataSoleDTO.setPhoneSaleExtendInfo(phoneSaleExtendInfo);
        realTimeUserDataSoleDTO.setSoleType(1);
        realTimeUserDataSoleDTO.setSoleField(SoleFieldEnum.CUST_NUM_STATUS_SOLE.getValue());
        realTimeUserDataSoleDTO.setDistributeSourceTypeEnum(DistributeSourceTypeEnum.CALL_RECORD);
        return realTimeUserDataSoleDTO;
    }

    private void buildPhoneSaleExtendInfo(PhoneSaleExtendInfo phoneSaleExtendInfo, CallRecordBO bo
            , MarketingSyncUser syncUser, String userType) {
        phoneSaleExtendInfo.setApiCode(syncUser.getApiCode());
        phoneSaleExtendInfo.setCustNum(syncUser.getCustNum());
        phoneSaleExtendInfo.setCell(syncUser.getCell());
        phoneSaleExtendInfo.setTaskId(syncUser.getCusBatch());
        phoneSaleExtendInfo.setAppletDate(bo.getCreateTime().toInstant().atZone(ZoneId.systemDefault())
                .toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        phoneSaleExtendInfo.setAppletTime(bo.getCreateTime().toInstant().atZone(ZoneId.systemDefault())
                .toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        phoneSaleExtendInfo.setStatus(userType);
        phoneSaleExtendInfo.setCreateTime(new Date());
        phoneSaleExtendInfo.setSourceId(bo.getId());
        phoneSaleExtendInfo.setPushDxTime(new Date());
        phoneSaleExtendInfo.setDxUserType(userType);
    }


    private void buildDassSingleImportAdapSoleDTO(DassSingleImportAdapSoleDTO dassSingleImportAdapSoleDTO
            , MarketingSyncUser syncUser, String userType) {
        DassSingleImportDataDTO dassSingleImportDataDTO = new DassSingleImportDataDTO();
        JSONObject rvF = JSONObject.parseObject(syncUser.getReserveField1());
        String gender = StringUtils.isNotBlank(rvF.getString("gender")) ? rvF.getString("gender") : "";
        dassSingleImportDataDTO.setGender(gender.equals("0") ? "女" : (gender.equals("1") ? "男" : ""));
        String name = syncUser.getName();
        if (StringUtils.isNotBlank(name)) {
            try {
                name = BrCipherMaker.getInstance().decode(name);
                if (!syncUser.getName().equals(name)) {
                    dassSingleImportDataDTO.setName(name);
                } else {
                    dassSingleImportDataDTO.setName("1");
                }
            } catch (Exception e) {
                dassSingleImportDataDTO.setName("1");
            }
        }
        dassSingleImportDataDTO.setOrgname("rongshuxinke");
        dassSingleImportDataDTO.setPhone(BrCipherMaker.getInstance().decode(syncUser.getCell()));
        dassSingleImportDataDTO.setUid(syncUser.getCustNum());
        dassSingleImportDataDTO.setUserType(userType);
        dassSingleImportDataDTO.setRegisterTime(rvF.getString("signInTimeStr"));
        dassSingleImportDataDTO.setLoginTime(rvF.getString("loginTimeStr"));
        dassSingleImportDataDTO.setSource("45");
        dassSingleImportDataDTO.setAuditTime(rvF.getString("auditTime"));
        JSONObject extend = new JSONObject();
        String planId = rvF.getString("planId");
        String tid = rvF.getString("tid");
        if (StringUtils.isNotBlank(planId)) {
            extend.put("planId", planId);
        }
        if (StringUtils.isNotBlank(tid)) {
            extend.put("tid", tid);
        }
        dassSingleImportDataDTO.setExtend(extend.toString());
        dassSingleImportDataDTO.setAuditAmount(rvF.getString("auditAmount"));
        dassSingleImportDataDTO.setLentAmount(StringUtils.isBlank(rvF.getString("lentAmount")) ? "" :
                new BigDecimal(rvF.getString("lentAmount")).setScale(0, RoundingMode.HALF_UP).toString());
        dassSingleImportAdapSoleDTO.setDassSingleImportDataDTO(dassSingleImportDataDTO);
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MqFact mqFact = context.getMqFact();
        Integer isDelay = mqFact.getIsDelay();
        if (isDelay == null || isDelay != 1) {
            return false;
        }
        if(transmitFact instanceof CallRecordBO){
            CallRecordBO bo = (CallRecordBO) transmitFact;
            RsxkCollectDataImpl.RsxkRuleNecessaryData ruleNecessaryData =
                    (RsxkCollectDataImpl.RsxkRuleNecessaryData) context.getRuleNecessaryData();
            Map<String, SyncUserValidityPeriodBO> syncUserPeriodMap = ruleNecessaryData.getSyncUserPeriodMap();
            SyncUserValidityPeriodBO syncUserData = syncUserPeriodMap.get(bo.getCaseNum());
            if (syncUserData == null) {
                return false;
            }
            MarketingSyncUser syncUser = syncUserData.getSyncUser();
            return isCall(syncUser);
        }
        return false;
    }

    /**
     * @description 调用榕树接口，判断是否可以推送Dass
     * @param syncUser
     * @return java.lang.Boolean
     * @author hedongshuo
     * @date 2025/3/28 10:53
     **/
    private Boolean isCall(MarketingSyncUser syncUser) {
        //数据静置后，最新上传数据的user_type可能会变化，以最新的user_type为准，这里需要做二次场景过滤
        JSONObject userTypeConfig = marketingCommonConfig.getRsxkToDassUserTypeConfig();
        JSONObject configForApiCode = userTypeConfig.getJSONObject(syncUser.getApiCode());
        if (!configForApiCode.containsKey(syncUser.getUserType())) {
            return false;
        }
        Result<CallStatusDTO> result = rsxkClient.queryCallStatus(syncUser);
        if(!ResultCode.SUCCESS.getValue().equals(result.getCode())){
            return false;
        }
        CallStatusDTO callStatusDTO = result.getData();
        if (2 == callStatusDTO.getCallFlag()) {
            return false;
        }
        return true;
    }

    @Override
    public String label() {
        return "Rsxk_CallRecordData_ToDaas";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.ARTIFICIAL_REAL_TIME_USERDATA_SOLE.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.RSXK_DATA_COLLECTION.getCode();
    }
}
