package com.br.marketing.strategy;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.intelligentcustomerservice.input.*;
import com.br.marketing.common.enums.DistributeSourceTypeEnum;
import com.br.marketing.common.enums.DistributeTypeEnum;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.DataJoinLogDTO;
import com.br.marketing.es.util.BrCipherMaker;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
/**
 * @Description : 推送决策去重调用方法
 * @Author : lizhen
 * @Date : Create in 2023/03/13 16:11
 */
public class PolicySoleHandler extends AbstractExternalInterfaceHandler<PushMarketingUserDetailByRuleDTO> {

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;


    @Override
    public JSONObject call(List<PushMarketingUserDetailByRuleDTO> policyByRuleList, ProcessHandlerContext context) {
        if (policyByRuleList.size() <= 0) {
            return null;
        }
        String pushApiCode = policyByRuleList.get(0).getPushApiCode();
        Map<String, List<PushMarketingUserDetailByRuleDTO>> batchMap = policyByRuleList.stream().collect(Collectors.
                groupingBy(PushMarketingUserDetailByRuleDTO::getBatchNumber));
        for (String batch : batchMap.keySet()) {
            List<PushMarketingUserDetailByRuleDTO> ruleLists = batchMap.get(batch);
            Map<String, List<PushMarketingUserDetailByRuleDTO>> strategyMap = ruleLists.stream().collect(Collectors.
                    groupingBy(PushMarketingUserDetailByRuleDTO::getStrategyCode));
            for (String strategy : strategyMap.keySet()) {
                //数据日志数组
                ArrayList<DataJoinLogDTO> logList = new ArrayList<>();
                List<PushMarketingUserDetailByRuleDTO> datas = strategyMap.get(strategy);
                ArrayList<PushMarketingUserDetailDTO> pushs = new ArrayList<>();
                List<Long> sourceIds = new ArrayList<>();
                for (PushMarketingUserDetailByRuleDTO t : datas) {
                    PushMarketingUserDetailDTO entity = new PushMarketingUserDetailDTO();
                    BeanUtils.copyProperties(t, entity);
                    pushs.add(entity);
                    sourceIds.add(t.getInitId());
                    // 把封装的日志插入到数组中
                    logList.add(methodRetryHandlerService.dataJoinLogFix(entity, DistributeTypeEnum.POLICYDATA
                            , StringUtils.isEmpty(pushApiCode) ? context.getApiCode() : pushApiCode, t.getCaseNumber(),
                            BrCipherMaker.getInstance().encode(t.getCell())
                            , Long.valueOf(t.getInitId()), DistributeSourceTypeEnum.TRANSFER, t.getStatus(), t.getExpireEndDate()));

                }
                PolicyRetryByRuleSoleDTO retryByRuleDTO = new PolicyRetryByRuleSoleDTO();
                retryByRuleDTO.setApiCode(StringUtils.isEmpty(pushApiCode) ? context.getApiCode() : pushApiCode);
                retryByRuleDTO.setBatchNumber(batch);
                retryByRuleDTO.setStrategyCode(strategy);
                retryByRuleDTO.setIds(sourceIds);
                retryByRuleDTO.setInfoId(context.getMqFact().getSourceId());
                retryByRuleDTO.setData(pushs);
                retryByRuleDTO.setDetailLogList(logList);
                //传参去重
                retryByRuleDTO.setIsSole(true);
                //去重字段维度,根据传入值赋值，默认为cell维度去重
                if (datas.get(0).getSoleField() != null) {
                    retryByRuleDTO.setSoleField(datas.get(0).getSoleField());
                } else {
                    retryByRuleDTO.setSoleField(SoleFieldEnum.CELL_SOLE.getValue());
                }
                //去重范围,根据传入值赋值，默认当天去重
                if (datas.get(0).getSoleType() != null) {
                    retryByRuleDTO.setSoleDay(datas.get(0).getSoleType());
                } else {
                    retryByRuleDTO.setSoleDay(1);
                }
                methodRetryHandlerService.callPolicySoleData(retryByRuleDTO, 0);
            }
        }
        return null;
    }

    @Override
    public InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.INIT_TO_POLICY_SOLE;
    }

}
