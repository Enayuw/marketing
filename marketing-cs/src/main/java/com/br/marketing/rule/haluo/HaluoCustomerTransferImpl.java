package com.br.marketing.rule.haluo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.HaiErRuleCollectDataImpl;
import com.br.marketing.context.impl.HaluoRuleCollectDataImpl;
import com.br.marketing.entity.*;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.br.marketing.vo.TransferSyncUserToRobotAiVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;


@Service
@Slf4j
public class HaluoCustomerTransferImpl implements AssembleData<ConversionData> {

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    final static DateTimeFormatter ymd = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        HashMap<String, String> haluoTransferRule = marketingCommonConfig.getHaluoTransferRule();
        Integer taskTimeDays = 35;
        Integer abcTimeDays = 5;
        Integer dTimeDays = 4;
        HashSet status = new HashSet();
        status.add("a");
        status.add("b");
        status.add("d");
        if(haluoTransferRule != null){
            taskTimeDays = Integer.valueOf(haluoTransferRule.getOrDefault("taskIddate", "35"));
            abcTimeDays = Integer.valueOf(haluoTransferRule.getOrDefault("ABCdate", "5"));
            dTimeDays = Integer.valueOf(haluoTransferRule.getOrDefault("dtimes", "4"));
            String statusStr = haluoTransferRule.getOrDefault("status", "a,b,d");
            status = new HashSet<>(Arrays.asList(statusStr.split(",")));
        }

        MarketingTransferSyncUser transferSyncUser = (MarketingTransferSyncUser)transmitFact;
        HaluoRuleCollectDataImpl.HaluoRuleNecessaryData ruleNecessaryData =
                (HaluoRuleCollectDataImpl.HaluoRuleNecessaryData) context.getRuleNecessaryData();
        MarketingSyncUser syncUser = ruleNecessaryData.getCustomerMap().get(transferSyncUser.getCustNum());
        List<PhoneSaleExtendInfo> phoneSaleExtendInfos = ruleNecessaryData.getPhoneSaleExtendInfoMap().get(transferSyncUser.getCustNum());
        Map<String, List<TaskTime>> taskIdDateMap = ruleNecessaryData.getTaskIdDateMap();
        //region check
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
        //endregion

        JSONObject jb = JSON.parseObject(transferSyncUser.getReserveField1());
        boolean a = "1".equals(transferSyncUser.getIfLogin())
                && (jb != null && org.apache.commons.lang3.StringUtils.isNotBlank(jb.getString("applyInformation")) && "0".equals(jb.getString("applyInformation")))
                && !"1".equals(transferSyncUser.getIfApply());

        boolean b = "1".equals(transferSyncUser.getIfLogin())
                && (jb != null && org.apache.commons.lang3.StringUtils.isNotBlank(jb.getString("applyInformation")) && "1".equals(jb.getString("applyInformation")))
                && !"1".equals(transferSyncUser.getIfApply());

        boolean c = "1".equals(transferSyncUser.getIfLogin())
                && (jb != null && org.apache.commons.lang3.StringUtils.isNotBlank(jb.getString("applyInformation")) && "1".equals(jb.getString("applyInformation")))
                && "1".equals(transferSyncUser.getIfApply())
                && "0".equals(transferSyncUser.getApplyResult());
        Double unlentAmount = Double.valueOf(org.apache.commons.lang3.StringUtils.isNotBlank(transferSyncUser.getUnlentAmount()) ? transferSyncUser.getUnlentAmount() : "0");
        boolean d = unlentAmount > 0;

        boolean groupa = (status.contains("a")&&a) || (status.contains("b")&&b) || status.contains("c")&&c;
        boolean groupb = status.contains("d")&&d;
        if (!groupa && !groupb) {
            return false;
        }
        if(groupb){

        }
        return false;
    }

    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) {

        transferSyncUser transferSyncUser = (transferSyncUser)transmitFact;
        HaiErRuleCollectDataImpl.HaiErRuleNecessaryData necessaryData =
                (HaiErRuleCollectDataImpl.HaiErRuleNecessaryData) context.getRuleNecessaryData();

        MarketingSyncUser syncUser = necessaryData.getCustomerMap().get(transferSyncUser.getCustNum());
        try {
            if (syncUser == null) {
                log.error(String.format("海尔该转化数据没有匹配到原始上传数据 dataId:%d",transferSyncUser.getId()));
                return null;
            }
            String status = "";
            if ("4".equals(transferSyncUser.getUserType())) {
                status = "0";
            } else {
                if(transferSyncUser.getApplyDt() == null){
                    return null;
                }
                Date applydt = null;
                try {
                    applydt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(transferSyncUser.getApplyDt());
                    Date appletTime = syncUser.getAppletTime();
                    if ("0".equals(transferSyncUser.getApplyResult()) && applydt.compareTo(appletTime) > 0) {
                        status = "2";
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            if (StringUtils.isEmpty(status)) {
                return null;
            }
            ConversionData conversionData = new ConversionData();
            conversionData.setDataId(transferSyncUser.getId().toString());
            conversionData.setCid(transferSyncUser.getCid());
            conversionData.setCaseNum(transferSyncUser.getCustNum());
            conversionData.setGroupType(transferSyncUser.getUserType());
            conversionData.setPhone(BrCipherMaker.getInstance().decode(syncUser.getCell()));
            conversionData.setInversionStatus(status);
            if (!StringUtils.isEmpty(transferSyncUser.getCreateTime())) {
                conversionData.setPartnerProcessDate(DateUtils.format(transferSyncUser.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
            }
            TransferSyncUserToRobotAiVO vo = new TransferSyncUserToRobotAiVO();
            BeanUtils.copyProperties(transferSyncUser, vo);
            conversionData.setInversionInfo(JSON.toJSONString(vo));
            return conversionData;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return null;
    }

    @Override
    public String label() {
        return "Haier_OverdueData_CustomerTransfer";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.MULTIPLE_DASSBATCH_CUSTOMERBLACK.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.HAI_ER_RULE_DATA_COLLECTION.getCode();
    }
}
