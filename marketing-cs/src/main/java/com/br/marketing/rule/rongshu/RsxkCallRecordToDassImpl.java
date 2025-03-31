package com.br.marketing.rule.rongshu;

import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.BatchRealTimeUserDataDTO;
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
import com.br.marketing.rpcclient.RpcClientProxy;
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
public class RsxkCallRecordToDassImpl implements AssembleData<BatchRealTimeUserDataDTO> {

    @Resource
    MarketingCommonConfig marketingCommonConfig;

    @Resource
    RsxkClient rsxkClient;

    @Override
    public BatchRealTimeUserDataDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        CallRecordBO bo = (CallRecordBO) transmitFact;
        RsxkCollectDataImpl.RsxkRuleNecessaryData ruleNecessaryData =
                (RsxkCollectDataImpl.RsxkRuleNecessaryData) context.getRuleNecessaryData();
        Map<String, SyncUserValidityPeriodBO> syncUserPeriodMap = ruleNecessaryData.getSyncUserPeriodMap();
        SyncUserValidityPeriodBO syncUserData = syncUserPeriodMap.get(bo.getCaseNum());
        MarketingSyncUser syncUser = syncUserData.getSyncUser();
//        return buildRealTimeUserDataSoleDTO(bo, syncUser);
        return buildBatchRealTimeUserDataDTO(bo, syncUser);
    }

    private BatchRealTimeUserDataDTO buildBatchRealTimeUserDataDTO(CallRecordBO bo, MarketingSyncUser syncUser) {
        BatchRealTimeUserDataDTO batchRealTimeUserDataDTO = new BatchRealTimeUserDataDTO();
        DassImportDataDTO dassImportDataDTO = new DassImportDataDTO();
        PhoneSaleExtendInfo phoneSaleExtendInfo = new PhoneSaleExtendInfo();
        batchRealTimeUserDataDTO.setDassImportDataDTO(dassImportDataDTO);
        batchRealTimeUserDataDTO.setPhoneSaleExtendInfo(phoneSaleExtendInfo);
        JSONObject userTypeConfig = marketingCommonConfig.getRsxkToDassUserTypeConfig();
        JSONObject configForApiCode = userTypeConfig.getJSONObject(bo.getApiCode());
        String userType = configForApiCode.getString(syncUser.getUserType());
        buildDassImportDataDTO(syncUser, userType, dassImportDataDTO);
        buildPhoneSaleExtendInfo(phoneSaleExtendInfo, bo, syncUser, userType);
        return batchRealTimeUserDataDTO;
    }

    private void buildDassImportDataDTO(MarketingSyncUser syncUser, String userType, DassImportDataDTO dassImportDataDTO) {
        JSONObject rvF = JSONObject.parseObject(syncUser.getReserveField1());
        String gender = StringUtils.isNotBlank(rvF.getString("gender")) ? rvF.getString("gender") : "";
        dassImportDataDTO.setGender(gender.equals("0") ? "女" : (gender.equals("1") ? "男" : ""));
        String name = syncUser.getName();
        if (StringUtils.isNotBlank(name)) {
            try {
                name = BrCipherMaker.getInstance().decode(name);
                if (!syncUser.getName().equals(name)) {
                    dassImportDataDTO.setName(name);
                } else {
                    dassImportDataDTO.setName("1");
                }
            } catch (Exception e) {
                dassImportDataDTO.setName("1");
            }
        }
        dassImportDataDTO.setOrgname("rongshuxinke");
        dassImportDataDTO.setPhone(syncUser.getCell());
        dassImportDataDTO.setUid(syncUser.getCustNum());
        dassImportDataDTO.setUserType(userType);
        dassImportDataDTO.setRegisterTime(rvF.getString("registerTime"));
        dassImportDataDTO.setLoginTime(rvF.getString("loginTime"));
        dassImportDataDTO.setLoginTime(rvF.getString("loginTime"));
        dassImportDataDTO.setSource("45");
        dassImportDataDTO.setAuditTime(rvF.getString("auditTime"));
        JSONObject extend = new JSONObject();
        String planId = rvF.getString("planId");
        String tid = rvF.getString("tid");
        if (StringUtils.isNotBlank(planId)) {
            extend.put("planId", planId);
        }
        if (StringUtils.isNotBlank(tid)) {
            extend.put("tid", tid);
        }
        dassImportDataDTO.setExtend(extend.toString());
        dassImportDataDTO.setAuditAmount(rvF.getString("auditAmount"));
        dassImportDataDTO.setLentAmount(StringUtils.isBlank(rvF.getString("lentAmount")) ? null :
                new BigDecimal(rvF.getString("lentAmount")).setScale(0, RoundingMode.HALF_UP).toString());
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
        phoneSaleExtendInfo.setUserType(userType);
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
        dassSingleImportDataDTO.setRegisterTime(rvF.getString("registerTime"));
        dassSingleImportDataDTO.setLoginTime(rvF.getString("loginTime"));
        dassSingleImportDataDTO.setLoginTime(rvF.getString("loginTime"));
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
        dassSingleImportDataDTO.setLentAmount(StringUtils.isBlank(rvF.getString("lentAmount")) ? null :
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
        return InterfaceHandlerEnum.ARTIFICIAL_BATCH_REALTIME_DATA.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.RSXK_DATA_COLLECTION.getCode();
    }
}
