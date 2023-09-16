package com.br.marketing.context.impl;

import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.RuleNecessaryData;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.XieChengJudgeConvTypeValue;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.ValidityPeriodDataService;
import javafx.util.Pair;
import lombok.Data;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author chenh
 * @version 1.0
 * @date 2023/09/16 16:45
 * 携程规则所需要的数据
 */
@Service
public class XieChengVTRuleCollectDataImpl extends CommonMethodHandlerService {

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
    @Resource
    private TableCreateServiceImpl tableCreateService;
    @Resource
    private ValidityPeriodDataService validityPeriodDataService;

    @Override
    public void ruleNecessaryData(List transmitFacts, ProcessHandlerContext context) {
        if (!transmitFacts.isEmpty()) {
            Object o = transmitFacts.get(0);
            XieChengRuleNecessaryData ruleNecessaryData = new XieChengRuleNecessaryData();
            if (o instanceof MarketingTransferSyncUser) {
                String apiCode = ((MarketingTransferSyncUser) o).getApiCode();
                String tcid = tableCreateService.getTcId(apiCode);
                Pair<String, String> validityRange =
                        validityPeriodDataService.getMarketingTransferDataWithValidityRange(apiCode);
                String startDate = validityRange.getKey();
                String endDate = validityRange.getValue();

                // 遍历，根据每条的custnum110的数量，再根据每条的
                List<MarketingTransferSyncUser> list = (List<MarketingTransferSyncUser>) transmitFacts;
                Set<String> set = list.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());

                buildData(ruleNecessaryData, apiCode, tcid, startDate, endDate, set);
            } else if (o instanceof CallRecordBO) {
                String apiCode = ((CallRecordBO) o).getApiCode();
                String tcid = tableCreateService.getTcId(apiCode);
                Pair<String, String> validityRange =
                        validityPeriodDataService.getMarketingTransferDataWithValidityRange(apiCode);
                String startDate = validityRange.getKey();
                String endDate = validityRange.getValue();
                List<CallRecordBO> list = (List<CallRecordBO>) transmitFacts;
                Set<String> set = list.stream()
                        .map(CallRecordBO::getCaseNum).collect(Collectors.toSet());

                buildData(ruleNecessaryData, apiCode, tcid, startDate, endDate, set);

            }
            context.setRuleNecessaryData(ruleNecessaryData);
        }
    }

    private void buildData(XieChengRuleNecessaryData ruleNecessaryData, String apiCode, String tcid, String startDate, String endDate,
                           Set<String> set) {
        List<XieChengJudgeConvTypeValue> xieChengJudgeConvType = marketingTransferSyncUserMapper.getXieChengJudgeConvType(tcid, apiCode,
                startDate, endDate, set);
        Map<String, XieChengJudgeConvTypeValue> map =
                xieChengJudgeConvType.stream().collect(Collectors.toMap(XieChengJudgeConvTypeValue::getCustNum, Function.identity()));
        ruleNecessaryData.setMap(map);
    }

    @Override
    public RuleDataCollectionEnum label() {
        return RuleDataCollectionEnum.XIECHENG_DATA_COLLECTION;
    }

    @Data
    public class XieChengRuleNecessaryData extends RuleNecessaryData {
        private Map<String, XieChengJudgeConvTypeValue> map;
    }
}
