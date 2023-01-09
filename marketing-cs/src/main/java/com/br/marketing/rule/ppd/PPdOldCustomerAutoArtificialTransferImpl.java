package com.br.marketing.rule.ppd;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.BatchRealTimeUserDataDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.AESUtil;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.PPDCollectDataImpl;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import com.br.marketing.origin.MqFact;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.IScoreResultService;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Service
public class PPdOldCustomerAutoArtificialTransferImpl implements AssembleData<BatchRealTimeUserDataDTO> {

    @Autowired
    IScoreResultService iScoreResultService;

    @Resource
    PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Resource
    MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Autowired
    TableCreateServiceImpl tableCreateService;

    @Value("${api.dass.aesKey:00}")
    private String aesKey;

    @Autowired
    RedisChgService redisChgService;

    @Override
    public BatchRealTimeUserDataDTO assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        BatchRealTimeUserDataDTO batchRealTimeUserDataDTO = new BatchRealTimeUserDataDTO();

        PPDCollectDataImpl.PPDRuleNecessaryData ruleNecessaryData =
                (PPDCollectDataImpl.PPDRuleNecessaryData) context.getRuleNecessaryData();
        Map<String, MarketingSyncUser> customerMap = ruleNecessaryData.getCustomerMap();
        MarketingSyncUser marketingSyncUser = getSyncUser(customerMap, transfer.getCustNum());
        batchRealTimeUserDataDTO.setDassImportDataDTO(packageDassImportData(transfer, marketingSyncUser));
        return batchRealTimeUserDataDTO;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            if (StringUtils.isBlank(transfer.getReserveField1())) {
                return false;
            }
            JSONObject jsonObject = JSON.parseObject(transfer.getReserveField1());
            String ifLogin = jsonObject.getString("ifLogin");
            if (!"1".equals(ifLogin)) {
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

            Integer ppdValidityDay = marketingCommonConfig.getPpdValidityDay() != null ? marketingCommonConfig.getPpdValidityDay() : null;
            if (ppdValidityDay != null) {
                LocalDate startDate = LocalDate.now().minusDays(ppdValidityDay);
                LocalDate dataDate = LocalDate.parse(marketingSyncUser.getAppletDate());
                if (dataDate.compareTo(startDate) < 0) {
                    return false;
                }
            } else {
                return false;
            }

            String tcId = tableCreateService.getTcId(context.getApiCode());
            MarketingTransferSyncUserExample transferSyncUserExample = new MarketingTransferSyncUserExample();
            transferSyncUserExample.setOrderByClause(" id limit 1");
            transferSyncUserExample.createCriteria()
                    .andTCidEqualTo(tcId)
                    .andApiCodeEqualTo(context.getApiCode())
                    .andCustNumEqualTo(transfer.getCustNum())
                    .andIfLentEqualTo("Y");
            List<MarketingTransferSyncUser> marketingTransferSyncUsers = marketingTransferSyncUserMapper.selectByExample(transferSyncUserExample);
            if (marketingTransferSyncUsers.size() > 0) {
                return false;
            }

            Result<String> conditionRes = iScoreResultService.isFilterScoreByTransfer(context.getApiCode(), this.label());
            if (!ResultCode.SUCCESS.getValue().equals(conditionRes.getCode())) {
                return false;
            }
            Result<String> stringResult = iScoreResultService.filterScoreResByTransfer(context.getApiCode(), transfer.getCustNum(), conditionRes.getData());
            if (!ResultCode.SUCCESS.getValue().equals(stringResult.getCode())) {
                return false;
            }

            String _7Day = LocalDate.now().minusDays(7L).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            //分布式锁，控制推电销判断逻辑顺序执行
            String key = RedisKeyConstant.ppdOldPushDx.concat(":")
                    .concat(transfer.getApiCode()).concat(":")
                    .concat(transfer.getCustNum());
            String value = UUID.randomUUID().toString();

            redisChgService.lock(key, value);
            PhoneSaleExtendInfoExample extendInfoExample = new PhoneSaleExtendInfoExample();
            extendInfoExample.createCriteria().andApiCodeEqualTo(transfer.getApiCode()).
                    andCustNumEqualTo(transfer.getCustNum()).
                    andAppletDateBetween(_7Day, transfer.getRequestData());
            int count = phoneSaleExtendInfoMapper.countByExample(extendInfoExample);
            if (count > 0) {
                redisChgService.unlock(key, value);
                return false;
            } else {
                savePhoneSaleExtendInfo(transfer, marketingSyncUser.getCusBatch());
                redisChgService.unlock(key, value);
                return true;
            }
        }
        return false;

    }

    @Override
    public String label() {
        return "PPDOld_TransferData_ArtificialBatch";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.BATCH_MESSAGE_DELAY.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.PPD_DATA_COLLECTION.getCode();
    }

    private void savePhoneSaleExtendInfo(MarketingTransferSyncUser transfer, String cusBatch) {
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
