package com.br.marketing.rule.yixin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.client.dassservice.input.userdata.BatchRealTimeUserDataDTO;
import com.br.marketing.client.dassservice.input.userdata.DassBatchImportDataDTO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.common.utils.AESUtil;
import com.br.marketing.commonmethod.YiXinUtils;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.HaiErRuleCollectDataImpl;
import com.br.marketing.context.impl.YiXinNoRealTimeDxRuleCollectDataImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.br.marketing.vo.TransferSyncUserToRobotAiVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 非实时转化数据推送客服
 *
 * @author Guo Zeqiang
 * @dateTime 2022/3/29 14:45
 */
@Service
public class YiXinNonRealTimeDxImpl implements AssembleData<BatchRealTimeUserDataDTO> {


    @Value("${api.dass.aesKey:00}")
    private String aesKey;

    @Override
    public BatchRealTimeUserDataDTO assemble(Object transmitFact, ProcessHandlerContext context) {
        YiXinNoRealTimeDxRuleCollectDataImpl.YiXinNoRealTimeDxContextRuleNecessaryData ruleNecessaryData =
                (YiXinNoRealTimeDxRuleCollectDataImpl.YiXinNoRealTimeDxContextRuleNecessaryData) context.getRuleNecessaryData();
        Map<String, MarketingSyncUser> customerMap = ruleNecessaryData.getCustomerMap();
        Map<String, List<String>> callRecordMap = ruleNecessaryData.getCallRecordMap();
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        MarketingSyncUser marketingSyncUser = customerMap.get(transfer.getCustNum());
        if(marketingSyncUser == null){
            return null;
        }
        List<String> grades = callRecordMap.get(transfer.getCustNum());
        String grade = (grades!=null&&grades.size()>0)?grades.get(0):"";
        BatchRealTimeUserDataDTO batchRealTimeUserDataDTO = new BatchRealTimeUserDataDTO();
            batchRealTimeUserDataDTO.setDassImportDataDTO(packageDassImportData(transfer,marketingSyncUser,grade));
            batchRealTimeUserDataDTO.setPhoneSaleExtendInfo(packagePhoneSaleExtendInfo(transfer,marketingSyncUser));
        return batchRealTimeUserDataDTO;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        if(context.getRuleNecessaryData()==null
                ||context.getRuleNecessaryData() instanceof YiXinNoRealTimeDxRuleCollectDataImpl.YiXinNoRealTimeDxContextRuleNecessaryData){
            return Boolean.FALSE;
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
        return RuleDataCollectionEnum.YIXIN_NOREAL_DX_RULE_DATA_COLLECTION.getCode();
    }

    private DassBatchImportDataDTO packageDassImportData(MarketingTransferSyncUser transfer
            , MarketingSyncUser syncUser,String phoneGrade) {
        JSONObject json = JSON.parseObject(transfer.getReserveField1());
        DassBatchImportDataDTO batchImportData = new DassBatchImportDataDTO();
        batchImportData.setId(transfer.getId());
        // 根据custNum取上传接口最新的gender（0女1男）传男女
        String reserveField1 = syncUser.getReserveField1();
        batchImportData.setGender(YiXinUtils.getActivity(json.getString("gender")));
        String cell = BrCipherMaker.getInstance().decode(syncUser.getCell());
        String phone = AESUtil.aesEncrypty(cell, aesKey);
        String name = StringUtils.hasText(syncUser.getName()) ?
                BrCipherMaker.getInstance().decode(syncUser.getName())
                : "";
        // 根据custNum取上传接口最新的name转成明文传输
        batchImportData.setName(name);
        batchImportData.setOrgname("yixin");
        // 根据custNum取上传接口最新的cell转aes加密
        batchImportData.setPhone(phone);
        batchImportData.setUid(transfer.getCustNum());
        batchImportData.setUserType("A");
        batchImportData.setSource("6");
        batchImportData.setType(YiXinUtils.getDxType(transfer.getType()));
        batchImportData.setLevel(YiXinUtils.getLevel(phoneGrade));
        batchImportData.setAuditAmount(transfer.getAuditAmount());
        // 根据custNum取转化接口的rate rate=1 -> activity=2 rate=2 -> activity=4
        batchImportData.setActivity(YiXinUtils.getActivity(json.getString("rate")));
        batchImportData.setPrioritySymbol(YiXinUtils.getPrioritySymbol(transfer.getType()));
        batchImportData.setApplyTime(transfer.getApplyDt().replaceAll("\\.d{3}",""));
        JSONObject extend = new JSONObject();
        String raiseLimiSuccess = json.getString("raiseLimiSuccess");
        String raiseLimiType = json.getString("raiseLimiType");
        if(StringUtils.isEmpty(raiseLimiType)){
            extend.put("raiseLimiType",raiseLimiType);
        }
        if(StringUtils.isEmpty(raiseLimiSuccess)){
            extend.put("raiseLimiSuccess",raiseLimiSuccess);
        }
        batchImportData.setExtend(extend.keySet().size()>0?JSON.toJSONString(extend):null);
        return batchImportData;
    }



    private PhoneSaleExtendInfo packagePhoneSaleExtendInfo(MarketingTransferSyncUser transfer,MarketingSyncUser syncUser) {
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
        return phoneSaleExtendInfo;
    }
}
