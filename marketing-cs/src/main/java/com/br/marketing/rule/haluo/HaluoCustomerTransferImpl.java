package com.br.marketing.rule.haluo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.client.robotaiapi.input.BlackDetailDTO;
import com.br.marketing.common.utils.AESUtil;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.HaluoRuleCollectDataImpl;
import com.br.marketing.dto.MultipleDassAndCustomerBlackDTO;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.PhoneSaleExtendHaluo;
import com.br.marketing.entity.TaskTime;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.Impl.PhoneSaleExtendServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class HaluoCustomerTransferImpl implements AssembleData<MultipleDassAndCustomerBlackDTO> {

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    final static DateTimeFormatter ymd = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    PhoneSaleExtendServiceImpl phoneSaleExtendService;


    @Value("${api.dass.aesKey:00}")
    private String aesKey;

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        Integer taskTimeDays = phoneSaleExtendService.getTaskIdDays();
        MarketingTransferSyncUser transferSyncUser = (MarketingTransferSyncUser)transmitFact;
        HaluoRuleCollectDataImpl.HaluoRuleNecessaryData ruleNecessaryData =
                (HaluoRuleCollectDataImpl.HaluoRuleNecessaryData) context.getRuleNecessaryData();
        MarketingSyncUser syncUser = ruleNecessaryData.getCustomerMap().get(transferSyncUser.getCustNum());
        List<PhoneSaleExtendHaluo> phoneSaleExtendInfos = ruleNecessaryData.getPhoneSaleExtendInfoMap().get(transferSyncUser.getCustNum());
        Map<String, List<TaskTime>> taskIdDateMap = ruleNecessaryData.getTaskIdDateMap();

        if (syncUser == null) {
            return false;
        }
        List<TaskTime> tasks = taskIdDateMap.get(syncUser.getCusBatch());
        if(tasks==null||tasks.size()<=0){
            return false;
        }
        TaskTime taskTimeEntity = tasks.get(0);
        LocalDate nowDate = LocalDate.now();
        LocalDate taskStartTime = LocalDate.parse(taskTimeEntity.getStartDate(), ymd);
        long untilDate = taskStartTime.until(nowDate, ChronoUnit.DAYS);
        if(untilDate>=taskTimeDays){
            return false;
        }
        String haluoStatus = phoneSaleExtendService.getHaluoStatus(transferSyncUser, syncUser);
        if(StringUtils.isEmpty(haluoStatus)){
            return false;
        }
        return phoneSaleExtendService.haluoSaleJudge(phoneSaleExtendInfos,haluoStatus,syncUser.getCusBatch());
    }

    @Override
    public MultipleDassAndCustomerBlackDTO assemble(Object transmitFact, ProcessHandlerContext context) {
        MultipleDassAndCustomerBlackDTO multipleDassAndCustomerBlackDTO = new MultipleDassAndCustomerBlackDTO();
        PhoneSaleExtendHaluo phoneSaleExtendHaluo = new PhoneSaleExtendHaluo();
        DassImportDataDTO dassImportDataDTO = new DassImportDataDTO();
        BlackDetailDTO blackDetailDTO = new BlackDetailDTO();
        multipleDassAndCustomerBlackDTO.setPhoneSaleExtendHaluo(phoneSaleExtendHaluo);
        multipleDassAndCustomerBlackDTO.setDassImportAdapDTO(dassImportDataDTO);
        multipleDassAndCustomerBlackDTO.setReqBlackPhoneParentDTO(blackDetailDTO);

        MarketingTransferSyncUser transferSyncUser = (MarketingTransferSyncUser)transmitFact;
        HaluoRuleCollectDataImpl.HaluoRuleNecessaryData ruleNecessaryData =
                (HaluoRuleCollectDataImpl.HaluoRuleNecessaryData) context.getRuleNecessaryData();
        MarketingSyncUser syncUser = ruleNecessaryData.getCustomerMap().get(transferSyncUser.getCustNum());
        String haluoStatus = phoneSaleExtendService.getHaluoStatus(transferSyncUser, syncUser);
        phoneSaleExtendHaluo.setCustNum(transferSyncUser.getCustNum());
        phoneSaleExtendHaluo.setApiCode(context.getApiCode());
        phoneSaleExtendHaluo.setTaskId(syncUser.getCusBatch());
        phoneSaleExtendHaluo.setAppletDate(transferSyncUser.getRequestData());
        phoneSaleExtendHaluo.setAppletTime(transferSyncUser.getRequestTime());
        phoneSaleExtendHaluo.setStatus(haluoStatus);
        phoneSaleExtendHaluo.setCreateTime(new Date());
        phoneSaleExtendHaluo.setUpdateTime(new Date());
        phoneSaleExtendHaluo.setSourceId(transferSyncUser.getId());

        String cell = BrCipherMaker.getInstance().decode(syncUser.getCell());
        String s = AESUtil.aesEncrypty(cell, aesKey);
        String decodeName;
        String name = org.apache.commons.lang3.StringUtils.isNotBlank(syncUser.getName()) ?
                (syncUser.getName().equals(decodeName = BrCipherMaker.getInstance().decode(syncUser.getName())) ? "1"
                        : decodeName) : "";
        dassImportDataDTO.setUid(transferSyncUser.getCustNum());
        dassImportDataDTO.setPhone(s);
        dassImportDataDTO.setName(name);
        dassImportDataDTO.setOrgname("hellobike");
        dassImportDataDTO.setSource("96");
        dassImportDataDTO.setUserType("d".equals(haluoStatus) ? "3" : "2");
        dassImportDataDTO.setLoginTime(phoneSaleExtendService.haluoBydxTimeFormat(transferSyncUser.getLoginTime()));
        dassImportDataDTO.setIfApply(transferSyncUser.getIfApply());
        dassImportDataDTO.setApplyDt(phoneSaleExtendService.haluoBydxTimeFormat(transferSyncUser.getApplyDt()));
        dassImportDataDTO.setAuditTime(phoneSaleExtendService.haluoBydxTimeFormat(transferSyncUser.getAuditTime()));
        dassImportDataDTO.setAuditAmount(transferSyncUser.getAuditAmount());
        dassImportDataDTO.setUnlentAmount(transferSyncUser.getUnlentAmount());
        if (org.apache.commons.lang3.StringUtils.isNotBlank(transferSyncUser.getReserveField1())) {
            JSONObject jsonObject = JSON.parseObject(transferSyncUser.getReserveField1());
            if (jsonObject != null) {
                String applyInformation = jsonObject.getString("applyInformation");
                if (org.apache.commons.lang3.StringUtils.isNotBlank(applyInformation)) {
                    JSONObject jsonObject1 = new JSONObject();
                    jsonObject1.put("applyInformation", applyInformation);
                    dassImportDataDTO.setExtend(JSON.toJSONString(jsonObject1));
                }
            }
        }
        String expiredate = LocalDate.parse(transferSyncUser.getRequestData(), DateTimeFormatter.ofPattern("yyyy-MM-dd")).plusDays(6)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 23:59:59";
        blackDetailDTO.setDataId(transferSyncUser.getId().toString());
        blackDetailDTO.setName(name);
        blackDetailDTO.setPhone(cell);
        blackDetailDTO.setExpireDate(expiredate);

        return multipleDassAndCustomerBlackDTO;
    }

    @Override
    public String label() {
        return "HaLuo_Transfer_DassAndCustomerBlack";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.MULTIPLE_DASSBATCH_CUSTOMERBLACK.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.HALUO_DASS_COLLECTION.getCode();
    }


}
