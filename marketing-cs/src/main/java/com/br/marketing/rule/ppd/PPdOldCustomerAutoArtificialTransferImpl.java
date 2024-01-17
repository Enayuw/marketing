package com.br.marketing.rule.ppd;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
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
import com.br.marketing.context.impl.PpdLodCollectDataImpl;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.IScoreResultService;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;


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

        PpdLodCollectDataImpl.PpdLodRuleNecessaryData ruleNecessaryData =
                (PpdLodCollectDataImpl.PpdLodRuleNecessaryData) context.getRuleNecessaryData();
        Map<String, SyncUserValidityPeriodsBO> userValidityPeriodsBoMap = ruleNecessaryData.getUserValidityPeriodsBoMap();
        MarketingSyncUser marketingSyncUser = userValidityPeriodsBoMap.get(transfer.getCustNum()).getSyncUsers().get(0);
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

            PpdLodCollectDataImpl.PpdLodRuleNecessaryData ruleNecessaryData =
                    (PpdLodCollectDataImpl.PpdLodRuleNecessaryData) context.getRuleNecessaryData();
            Map<String, SyncUserValidityPeriodsBO> userValidityPeriodsBoMap = ruleNecessaryData.getUserValidityPeriodsBoMap();
            SyncUserValidityPeriodsBO userValidityPeriodsBO = userValidityPeriodsBoMap.get(transfer.getCustNum());
            // 为null时不在有效期
            if (userValidityPeriodsBO == null) {
                return false;
            }
            List<PeriodOfValidityBO.Builder> builders = userValidityPeriodsBO.getBuilders();
            int size = builders.size();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < size; i++) {
                PeriodOfValidityBO bo = builders.get(i).addDateString().builder();
                String beginDateStr = bo.getBeginDateStr();
                String enDateStr = bo.getEnDateStr();
                sb.append("(request_data between '")
                        .append(beginDateStr)
                        .append("' and  '")
                        .append(enDateStr)
                        .append("')");
                if (i != (size - 1)) {
                    sb.append(" or ");
                }
            }
            int length = sb.length();
            if (length < 1) {
                sb.append("1!=1");
            }
            String tcId = tableCreateService.getTcId(context.getApiCode());
            MarketingTransferSyncUserExample transferSyncUserExample = new MarketingTransferSyncUserExample();
            transferSyncUserExample.settCid(tcId);
            transferSyncUserExample.createCriteria()
                    .andTCidEqualTo(tcId)
                    .andApiCodeEqualTo(context.getApiCode())
                    .andCustNumEqualTo(transfer.getCustNum())
                    .andIfLentEqualTo("Y");
            int countTransferSyncUser = marketingTransferSyncUserMapper.countByExampleSql(transferSyncUserExample
                    , " and (" + sb + ")");
            if (countTransferSyncUser > 0) {
                return false;
            }

            Result<String> conditionRes = iScoreResultService.isFilterScoreByTransfer(context.getApiCode(), this.label());
            if (ResultCode.SUCCESS.getValue().equals(conditionRes.getCode())) {
                Result<String> stringResult = iScoreResultService.filterScoreResByTransfer(context.getApiCode()
                        , transfer.getCustNum(), conditionRes.getData());
                if (!ResultCode.SUCCESS.getValue().equals(stringResult.getCode())) {
                    return false;
                }
            }

//            int ppdOldPhoneValidityDay = marketingCommonConfig.getPpdOldPhoneValidityDay() != null
//                    ? marketingCommonConfig.getPpdOldPhoneValidityDay() : 7;
//            // 计算时包括当天，所以需要在ppdOldPhoneValidityDay配置的中减少一天
//            String _7Day = LocalDate.now().minusDays(ppdOldPhoneValidityDay > 0 ? ppdOldPhoneValidityDay - 1
//                    : ppdOldPhoneValidityDay).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            //分布式锁，控制推电销判断逻辑顺序执行
            String key = RedisKeyConstant.ppdOldPushDx.concat(":")
                    .concat(transfer.getApiCode()).concat(":")
                    .concat(transfer.getCustNum());
            String value = UUID.randomUUID().toString();

            redisChgService.lock(key, value);
            PeriodOfValidityBO builder = builders.get(0).addDateString().builder();
            PhoneSaleExtendInfoExample extendInfoExample = new PhoneSaleExtendInfoExample();
            extendInfoExample.createCriteria().andApiCodeEqualTo(transfer.getApiCode())
                    .andCustNumEqualTo(transfer.getCustNum()).andStatusEqualTo("a")
                    .andAppletDateGreaterThanOrEqualTo(builder.getBeginDateStr());
            int count = phoneSaleExtendInfoMapper.countByExample(extendInfoExample);
            if (count > 0) {
                redisChgService.unlock(key, value);
                return false;
            } else {
                String cusBatch = userValidityPeriodsBO.getSyncUsers().get(0).getCusBatch();
                savePhoneSaleExtendInfo(transfer, cusBatch);
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
        return InterfaceHandlerEnum.ARTIFICIAL_BATCH_REALTIME_DATA.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.PPD_LOD_DATA_COLLECTION.getCode();
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
