package com.br.marketing.rule.yixin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.BatchRealTimeUserDataDTO;
import com.br.marketing.common.utils.AESUtil;
import com.br.marketing.commonmethod.YiXinUtils;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.YiXinRuleCollectDataImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.origin.TransferSource;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;

/**
 * 非实时转化数据推送客服
 *
 * @author Guo Zeqiang
 * @dateTime 2022/3/29 14:45
 */
@Service
@Slf4j
public class YiXinNonRealTimeDxImpl implements AssembleData<BatchRealTimeUserDataDTO> {


    @Value("${api.dass.aesKey:00}")
    private String aesKey;

    @Resource
    MarketingTransferSyncUserMapper transferSyncUserMapper;

    @Override
    public BatchRealTimeUserDataDTO assemble(Object transmitFact, ProcessHandlerContext context) {
/*        YiXinRuleCollectDataImpl.YiXinRuleNecessaryData ruleNecessaryData =
                (YiXinRuleCollectDataImpl.YiXinRuleNecessaryData) context.getRuleNecessaryData();
        Map<String, List<String>> callRecordMap = ruleNecessaryData.getCallRecordMap();
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = ruleNecessaryData.getCustomerMap().get(transfer.getCustNum());
        MarketingSyncUser marketingSyncUser = syncUserValidityPeriodsBO.getSyncUsers().get(0);
        if (marketingSyncUser == null) {
            return null;
        }
        String cell = BrCipherMaker.getInstance().decode(marketingSyncUser.getCell());
        if(StringUtils.isEmpty(cell)){
            return null;
        }
        //String phone = AESUtil.aesEncrypty(cell, aesKey);
        MarketingSyncUser syncUser = new MarketingSyncUser();
        BeanUtils.copyProperties(marketingSyncUser,syncUser);
        //syncUser.setCell(phone);
        List<String> grades = callRecordMap.get(transfer.getCustNum());
        String grade = (grades != null && grades.size() > 0) ? grades.get(0) : "";
        BatchRealTimeUserDataDTO batchRealTimeUserDataDTO = new BatchRealTimeUserDataDTO();
        batchRealTimeUserDataDTO.setDassImportDataDTO(packageDassImportData(transfer, syncUser, grade));
        batchRealTimeUserDataDTO.setPhoneSaleExtendInfo(packagePhoneSaleExtendInfo(transfer, syncUser));
        */
        return new BatchRealTimeUserDataDTO();
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        String reserveField1 = transfer.getReserveField1();
        if (!StringUtils.isEmpty(reserveField1)) {
            JSONObject json = JSON.parseObject(reserveField1);
            boolean transformType = "1".equals(json.getString("transformType"));
            if (transformType) {
                return Boolean.FALSE;
            }
            //过滤type=13，registerChannel！=1的数据
            if ("13".equals(transfer.getType())) {
                boolean registerChannel = !"1".equals(json.getString("registerChannel"));
                if (registerChannel) {
                    return Boolean.FALSE;
                }
            }

        }
        if (context.getRuleNecessaryData() == null
                || !(context.getRuleNecessaryData() instanceof YiXinRuleCollectDataImpl.YiXinRuleNecessaryData)) {
            return Boolean.FALSE;
        }
        if (!context.getMqFact().getSource().equals(TransferSource.TRANSFER_DATA_SET_PROCESS.getCode())) {
            return Boolean.FALSE;
        }
        String tCid = transfer.gettCid();
        String apiCode = transfer.getApiCode();
        Set<String> custNums = Sets.newHashSet(transfer.getCustNum());
        List caseEffectiveCust = transferSyncUserMapper.getByInCustAndCaseEffective(tCid, apiCode,custNums);
        if (!CollectionUtils.isEmpty(caseEffectiveCust)) {
            log.warn("id:{} cust_num:{}caseEffetive=0 剔除", transfer.getId(), transfer.getCustNum());
            return false;
        }
        return Boolean.TRUE;
    }

    @Override
    public String label() {
        return "YiXin_NonRealTime_Dx";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.ARTIFICIAL_BATCH_REALTIME_DATA.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.YI_XIN_DATA_COLLECTION.getCode();
    }

    /*private DassImportDataDTO packageDassImportData(MarketingTransferSyncUser transfer
            , MarketingSyncUser syncUser, String phoneGrade) {
        DassImportDataDTO batchImportData = new DassImportDataDTO();
        batchImportData.setId(transfer.getId());

        String decodeName;
        String name = StringUtils.hasText(syncUser.getName()) ?
                (syncUser.getName().equals(decodeName = BrCipherMaker.getInstance().decode(syncUser.getName())) ? "1"
                        : decodeName) : "";
        // 根据custNum取上传接口最新的name转成明文传输
        batchImportData.setName(name);
        batchImportData.setOrgname("yixin");
        // 根据custNum取上传接口最新的cell转aes加密
        String cell = BrCipherMaker.getInstance().decode(syncUser.getCell());
        String phone = AESUtil.aesEncrypty(cell, aesKey);
        batchImportData.setPhone(phone);
        batchImportData.setUid(transfer.getCustNum());
        batchImportData.setUserType("A");
        batchImportData.setSource("16");
        batchImportData.setType(YiXinUtils.getDxType(transfer.getType()));
        batchImportData.setLevel(YiXinUtils.getLevel(phoneGrade));
        batchImportData.setAuditAmount(transfer.getAuditAmount());
        batchImportData.setPrioritySymbol(YiXinUtils.getPrioritySymbol(transfer.getType()));
        batchImportData.setApplyTime(StringUtils.isEmpty(transfer.getApplyDt()) ? "" : transfer.getApplyDt().replaceAll(":\\d{3}", ""));

        if (!StringUtils.isEmpty(syncUser.getReserveField1())) {
            try {
                JSONObject syncUserJson = JSON.parseObject(syncUser.getReserveField1());
                if (syncUserJson != null) {
                    batchImportData.setGender(YiXinUtils.getGender(syncUserJson.getString("gender")));
                }
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }
        if (!StringUtils.isEmpty(transfer.getReserveField1())) {
            try {
                JSONObject json = JSON.parseObject(transfer.getReserveField1());
                if (json != null) {
                    batchImportData.setActivity(YiXinUtils.getActivity(json.getString("rate")));
                    JSONObject extend = new JSONObject();
                    String raiseLimiSuccess = json.getString("raiseLimiSuccess");
                    String raiseLimiType = json.getString("raiseLimiType");
                    String availableAmount = json.getString("availableAmount");
                    String recommendType = json.getString("recommendType");
                    if (!StringUtils.isEmpty(raiseLimiType)) {
                        extend.put("raiseLimiType", raiseLimiType);
                    }
                    if (!StringUtils.isEmpty(raiseLimiSuccess)) {
                        extend.put("raiseLimiSuccess", raiseLimiSuccess);
                    }
                    if (!StringUtils.isEmpty(availableAmount)) {
                        extend.put("availableAmount", availableAmount);
                    }
                    if (!StringUtils.isEmpty(recommendType)) {
                        extend.put("recommendType", recommendType);
                    }
                    batchImportData.setExtend(extend.keySet().size() > 0 ? JSON.toJSONString(extend) : null);
                }
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }
        return batchImportData;
    }*/


/*    private PhoneSaleExtendInfo packagePhoneSaleExtendInfo(MarketingTransferSyncUser transfer, MarketingSyncUser syncUser) {
        PhoneSaleExtendInfo phoneSaleExtendInfo = new PhoneSaleExtendInfo();
        phoneSaleExtendInfo.setApiCode(transfer.getApiCode());
        phoneSaleExtendInfo.setCustNum(transfer.getCustNum());
        phoneSaleExtendInfo.setTaskId(syncUser.getCusBatch());
        phoneSaleExtendInfo.setUserType(transfer.getUserType());
        phoneSaleExtendInfo.setAppletDate(transfer.getRequestData());
        phoneSaleExtendInfo.setAppletTime(transfer.getRequestTime());
        phoneSaleExtendInfo.setPStatus(1);
        phoneSaleExtendInfo.setCreateTime(new Date());
        phoneSaleExtendInfo.setType(transfer.getType());
        phoneSaleExtendInfo.setDxType(YiXinUtils.getDxType(transfer.getType()));
        phoneSaleExtendInfo.setPushDxTime(new Date());
        phoneSaleExtendInfo.setTransformType("0");
        phoneSaleExtendInfo.setSourceId(transfer.getId());
        phoneSaleExtendInfo.setCell(syncUser.getCell());
        return phoneSaleExtendInfo;
    }*/
}
