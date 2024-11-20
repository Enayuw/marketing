package com.br.marketing.strategy;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.intelligentcustomerservice.input.*;
import com.br.marketing.context.ProcessHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@Slf4j
public class PolicyHandler extends AbstractExternalInterfaceHandler<PushMarketingUserDetailByRuleDTO> {

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Override
    public JSONObject call(List<PushMarketingUserDetailByRuleDTO> policyByRuleList, ProcessHandlerContext context) {

        Map<String, List<PushMarketingUserDetailByRuleDTO>> batchMap = policyByRuleList.stream().collect(Collectors.groupingBy(PushMarketingUserDetailByRuleDTO::getBatchNumber));
        for (String batch : batchMap.keySet()) {
            List<PushMarketingUserDetailByRuleDTO> ruleLists = batchMap.get(batch);
            Map<String, List<PushMarketingUserDetailByRuleDTO>> strategyMap = ruleLists.stream().collect(Collectors.groupingBy(PushMarketingUserDetailByRuleDTO::getStrategyCode));
            for (String strategy : strategyMap.keySet()) {
                List<PushMarketingUserDetailByRuleDTO> datas = strategyMap.get(strategy);
                Map<String, List<PushMarketingUserDetailByRuleDTO>> batchNameMap = datas.stream()
                        .collect(Collectors.groupingBy(dto -> {
                            String batchName = dto.getBatchName();
                            return batchName != null ? batchName : ""; // 判空并返回默认值
                        }));
                for (String batchName : batchNameMap.keySet()) {
                    ArrayList<PushMarketingUserDetailDTO> pushs = new ArrayList<>();
                    List<PushMarketingUserDetailByRuleDTO> value = batchNameMap.get(batchName);
                    List<Long> sourceIds = new ArrayList<>();
                    value.forEach(t->{
                        PushMarketingUserDetailDTO entity = new PushMarketingUserDetailDTO();
                        BeanUtils.copyProperties(t, entity);
                        pushs.add(entity);
                        sourceIds.add(t.getInitId());
                    });

                    PushMarketingUserTaskInfoDTO taskInfoDTO = new PushMarketingUserTaskInfoDTO();
                    taskInfoDTO.setData(pushs);
                    taskInfoDTO.setAccessNumber(UUID.randomUUID().toString());
                    taskInfoDTO.setMethod("caseAdd");
                    taskInfoDTO.setBatchNumber(batch);
                    taskInfoDTO.setStrategyCode(strategy);
                    if(!batchName.isEmpty()){
                        taskInfoDTO.setBatchName(batchName);
                    }
                    PushMarketingUserDTO pushMarketingUserDTO = new PushMarketingUserDTO();
                    pushMarketingUserDTO.setApiCode(context.getApiCode());
                    pushMarketingUserDTO.setJsonData(taskInfoDTO);

                    PolicyRetryByRuleDTO retryByRuleDTO = new PolicyRetryByRuleDTO();
                    retryByRuleDTO.setIds(sourceIds);
                    retryByRuleDTO.setInfoId(context.getMqFact().getSourceId());
                    retryByRuleDTO.setPushMarketingUserDTO(pushMarketingUserDTO);
                    methodRetryHandlerService.callPolicyData(retryByRuleDTO,null);
                }
            }
        }
        return null;
    }

    @Override
    public InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.INIT_TO_POLICY;
    }
}
