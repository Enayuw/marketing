package com.br.marketing.rule.ppd;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.AESUtil;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.PPDCollectDataImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import com.br.marketing.origin.MqFact;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.IScoreResultService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;


@Service
public class PPdOldCustomerAutoArtificialTransferImpl implements AssembleData<MqFact> {

    @Autowired
    IScoreResultService iScoreResultService;

    @Resource
    PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Value("${api.dass.aesKey:00}")
    private String aesKey;

    @Override
    public MqFact assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        MqFact mqFact = new MqFact();
        mqFact.setSourceId(transfer.getId());
        return mqFact;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if(transmitFact instanceof MarketingTransferSyncUser){
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            if(StringUtils.isBlank(transfer.getReserveField1())){
                return false;
            }
            JSONObject jsonObject = JSON.parseObject(transfer.getReserveField1());
            String ifLogin = jsonObject.getString("ifLogin");
            if(!"1".equals(ifLogin)){
                return false;
            }
            if (StringUtils.isNotBlank(transfer.getIfLent())) {
                return false;
            }

            PPDCollectDataImpl.PPDRuleNecessaryData ruleNecessaryData =
                    (PPDCollectDataImpl.PPDRuleNecessaryData) context.getRuleNecessaryData();
            Map<String, MarketingSyncUser> customerMap = ruleNecessaryData.getCustomerMap();
            MarketingSyncUser marketingSyncUser = getSyncUser(customerMap, transfer.getCustNum());
            if (marketingSyncUser == null) {
                return false;
            }


            Result<String> conditionRes = iScoreResultService.isFilterScoreByTransfer(context.getApiCode(), this.label());
            if(!ResultCode.SUCCESS.getValue().equals(conditionRes.getCode())){
                return false;
            }
            Result<String> stringResult = iScoreResultService.filterScoreResByTransfer(context.getApiCode(), transfer.getCustNum(), conditionRes.getData());
            return ResultCode.SUCCESS.getValue().equals(stringResult.getCode());
        }
        return false;

    }

    @Override
    public String label() {
        return "PPD_TransferData_ArtificialBatch_Delay";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.BATCH_MESSAGE_DELAY.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.PPD_DATA_COLLECTION.getCode();
    }

    private void savePhoneSaleExtendInfo(MarketingTransferSyncUser transfer,String cusBatch) {
        PhoneSaleExtendInfo phoneSaleExtendInfo = new PhoneSaleExtendInfo();
        phoneSaleExtendInfo.setApiCode(transfer.getApiCode());
        phoneSaleExtendInfo.setCustNum(transfer.getCustNum());
        phoneSaleExtendInfo.setTaskId(cusBatch);
        phoneSaleExtendInfo.setUserType(transfer.getUserType());
        phoneSaleExtendInfo.setAppletDate(transfer.getRequestData());
        phoneSaleExtendInfo.setAppletTime(transfer.getRequestTime());
        phoneSaleExtendInfo.setPStatus(1);
        phoneSaleExtendInfo.setStatus("a");
        phoneSaleExtendInfo.setCreateTime(new Date());
        phoneSaleExtendInfo.setType(transfer.getType());
        phoneSaleExtendInfo.setPushDxTime(new Date());
        phoneSaleExtendInfo.setTransformType("0");
        phoneSaleExtendInfo.setSourceId(transfer.getId());
        phoneSaleExtendInfoMapper.insertSelective(phoneSaleExtendInfo);
    }

    private DassImportDataDTO packageDassImportData(MarketingTransferSyncUser transfer, MarketingSyncUser syncUser) {
        DassImportDataDTO batchImportData = new DassImportDataDTO();
        batchImportData.setId(transfer.getId());
        String cell = BrCipherMaker.getInstance().decode(syncUser.getCell());
        String phone = AESUtil.aesEncrypty(cell, aesKey);
        String decodeName;
        String name = org.springframework.util.StringUtils.hasText(syncUser.getName()) ?
                (syncUser.getName().equals(decodeName = BrCipherMaker.getInstance().decode(syncUser.getName())) ? "1"
                        : decodeName) : "1";
        // 根据custNum取上传接口最新的name转成明文传输
        batchImportData.setName(name);
        batchImportData.setOrgname("ppdai");
        // 根据custNum取上传接口最新的cell转aes加密
        batchImportData.setPhone(phone);
        batchImportData.setUid(transfer.getCustNum());
        batchImportData.setUserType("1");
        batchImportData.setSource("18");
        batchImportData.setType("8");
        return batchImportData;
    }
}
