package com.br.marketing.strategy;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.intelligentcustomerservice.input.*;
import com.br.marketing.common.enums.DistributeSourceTypeEnum;
import com.br.marketing.common.enums.DistributeTypeEnum;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.DataDistributeDetailLog;
import com.br.marketing.es.util.BrCipherMaker;
import com.br.marketing.mapper.DataDistributeDetailLogMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class YiXinToPolicyHandler extends AbstractExternalInterfaceHandler<PushMarketingUserDetailByRuleDTO> {

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private DataDistributeDetailLogMapper dataDistributeDetailLogMapper;

    @Override
    public JSONObject call(List<PushMarketingUserDetailByRuleDTO> policyByRuleList, ProcessHandlerContext context) {

        Map<String, List<PushMarketingUserDetailByRuleDTO>> batchMap = policyByRuleList.stream().collect(Collectors.
                groupingBy(PushMarketingUserDetailByRuleDTO::getBatchNumber));
        for (String batch : batchMap.keySet()) {
            List<PushMarketingUserDetailByRuleDTO> ruleLists = batchMap.get(batch);
            Map<String, List<PushMarketingUserDetailByRuleDTO>> strategyMap = ruleLists.stream().collect(Collectors.
                    groupingBy(PushMarketingUserDetailByRuleDTO::getStrategyCode));
            for (String strategy : strategyMap.keySet()) {
                List<PushMarketingUserDetailByRuleDTO> datas = strategyMap.get(strategy);
                ArrayList<PushMarketingUserDetailDTO> pushs = new ArrayList<>();
                List<Long> sourceIds = new ArrayList<>();
                datas.forEach((PushMarketingUserDetailByRuleDTO t) -> {
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

                PushMarketingUserDTO pushMarketingUserDTO = new PushMarketingUserDTO();
                pushMarketingUserDTO.setApiCode(marketingCommonConfig.getYiXinToPolicyApiCode());
                pushMarketingUserDTO.setJsonData(taskInfoDTO);

                PolicyRetryByRuleDTO retryByRuleDTO = new PolicyRetryByRuleDTO();
                retryByRuleDTO.setIds(sourceIds);
                retryByRuleDTO.setInfoId(context.getMqFact().getSourceId());
                retryByRuleDTO.setPushMarketingUserDTO(pushMarketingUserDTO);
                methodRetryHandlerService.callPolicyData(retryByRuleDTO, null);
            }
        }
        //插入到日志表，用于非实时推送决策剔除
        List<DataDistributeDetailLog> detailLogList = new ArrayList<>();
        policyByRuleList.forEach((PushMarketingUserDetailByRuleDTO ruleDTO)->{
            DataDistributeDetailLog detailLog = new DataDistributeDetailLog();
            detailLog.setSourceId(ruleDTO.getInitId());
            detailLog.setSourceType(DistributeSourceTypeEnum.TRANSFER.getValue());
            detailLog.setApiCode(marketingCommonConfig.getYiXinToPolicyApiCode());
            detailLog.setCell(BrCipherMaker.getInstance().encode(ruleDTO.getCell()));
            detailLog.setCustNum(ruleDTO.getCaseNumber());
            detailLog.setpStatus(2);
            detailLog.setStatus(ruleDTO.getStatus());
            detailLog.setDistributeType(DistributeTypeEnum.YIXIN_REALTIME_POLICYDATA.getValue());
            detailLog.setDistributeDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            detailLog.setCreateTime(new Date());
            detailLog.setUpdateTime(new Date());
            detailLog.setSuccessDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            detailLogList.add(detailLog);
        });
        dataDistributeDetailLogMapper.insertBatch(detailLogList);
        return null;
    }


    @Override
    InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.YIXIN_REALTIME_TO_POLICY;
    }
}
