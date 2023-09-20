package com.br.marketing.context.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.RuleNecessaryData;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.XieChengJudgeConvTypeValue;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.service.ValidityPeriodDataService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import javafx.util.Pair;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author chenh
 * @version 1.0
 * @date 2023/09/16 16:45
 * 携程规则所需要的数据
 */
@Service
@Slf4j
public class XieChengVTRuleCollectDataImpl extends CommonMethodHandlerService {

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
    @Resource
    private TableCreateServiceImpl tableCreateService;
    @Resource
    private ValidityPeriodDataService validityPeriodDataService;
    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;
    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Override
    public void ruleNecessaryData(List transmitFacts, ProcessHandlerContext context) {
        HashMap<String, JSONObject> xieChengCallPushCondition = marketingCommonConfig.getXieChengCallPushCondition();
        if (xieChengCallPushCondition == null) {
            xieChengCallPushCondition = new HashMap<>();
            xieChengCallPushCondition.put("3710090", getJo("2", Arrays.asList("3710090", "3710091"), "3710090"));
            xieChengCallPushCondition.put("3710091", getJo("2", Arrays.asList("3710090", "3710091"), "3710090"));
        }
        JSONObject condition = xieChengCallPushCondition.get(context.getApiCode());
        // 查询有效期使用的apiCode
        String apiCode = condition.getString("mainApiCode");
        // 查询转化数据convType使用的apiCode
        JSONArray convTypeApiCodes = condition.getJSONArray("convTypeApiCodes");
        String tcid = tableCreateService.getTcId(apiCode);

        // 获取有效期范围
        Pair<String, String> validityRange =
                validityPeriodDataService.getMarketingTransferDataWithValidityRange(apiCode);
        String startDate = validityRange.getKey();
        String endDate = validityRange.getValue();

        if (!transmitFacts.isEmpty()) {
            Object o = transmitFacts.get(0);
            XieChengRuleNecessaryData ruleNecessaryData = new XieChengRuleNecessaryData();
            Set<String> set = new HashSet<>();
            Integer type = 0;
            if (o instanceof MarketingTransferSyncUser) {
                List<MarketingTransferSyncUser> list = (List<MarketingTransferSyncUser>) transmitFacts;
                set = list.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
                type = 1;
            } else if (o instanceof CallRecordBO) {
                List<CallRecordBO> list = (List<CallRecordBO>) transmitFacts;
                set = list.stream()
                        .map(CallRecordBO::getCaseNum).collect(Collectors.toSet());
                type = 2;
            }

            // 封装有110，106，107的custNum集合
            buildData(ruleNecessaryData, convTypeApiCodes, tcid, startDate, endDate, set, type);
            // 封装有效期数据集合
            buildValidData(apiCode, ruleNecessaryData, set);
            // 设置回上下文
            context.setRuleNecessaryData(ruleNecessaryData);
        }
    }

    private void buildValidData(String apiCode, XieChengRuleNecessaryData ruleNecessaryData, Set<String> set) {
        Map<String, SyncUserValidityPeriodBO> syncUser =
                transferDataValidityPeriodService.getValidityPeriodCustNumBatchFirstVersion(set, apiCode, new Date());
        ruleNecessaryData.setValidMap(syncUser);
    }

    private void buildData(XieChengRuleNecessaryData ruleNecessaryData, JSONArray convTypeApiCodes, String tcid, String startDate, String endDate,
                           Set<String> set, Integer type) {
        List<XieChengJudgeConvTypeValue> xieChengJudgeConvType = marketingTransferSyncUserMapper.getXieChengJudgeConvType(tcid, convTypeApiCodes,
                startDate, endDate, set);
        Map<String, XieChengJudgeConvTypeValue> map =
                xieChengJudgeConvType.stream().collect(Collectors.toMap(XieChengJudgeConvTypeValue::getCustNum, Function.identity()));
        if (CollectionUtils.isEmpty(map)) {
            String msg = type == 1 ? "携程转化数据推送客服转化过滤接口" : "携程通话明细数据推送客户接口";
            log.warn(msg + "，根据custNum和有效期范围没查询到转化数据，custNum：{}，startDate：{}，endDate：{}", String.join(",", set), startDate, endDate);
        }
        ruleNecessaryData.setMap(map);
    }

    @Override
    public RuleDataCollectionEnum label() {
        return RuleDataCollectionEnum.XIECHENG_DATA_COLLECTION_VT;
    }

    @Data
    public class XieChengRuleNecessaryData extends RuleNecessaryData {
        private Map<String, XieChengJudgeConvTypeValue> map;
        private Map<String, SyncUserValidityPeriodBO> validMap;
    }

    private JSONObject getJo(String condition, List<String> soleCellApiCodes, String mainApiCode) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("condition", condition);
        jsonObject.put("isBlackApiCodes", soleCellApiCodes);
        jsonObject.put("convTypeApiCodes", soleCellApiCodes);
        jsonObject.put("soleCellApiCodes", soleCellApiCodes);
        jsonObject.put("mainApiCode", mainApiCode);
        return jsonObject;
    }
}
