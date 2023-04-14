package com.br.marketing.check.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.bo.JobPushDecisionParameterBO;
import com.br.marketing.check.service.AutomatedPushDecisionService;
import com.br.marketing.client.intelligentcustomerservice.input.PolicyRetryByRuleDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserTaskInfoDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.enums.CustomerPushDecisionActionEnum;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.TransferActionFrontMapper;
import com.br.marketing.service.ICustomerConfigService;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.strategy.MethodRetryHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * D20230406众安自动化转决策-3710048
 * http://c.100credit.cn/pages/viewpage.action?pageId=108627044
 *
 * @author Guo Zeqiang
 * @dateTime 2023-04-12 13:51
 */
@Service
@Slf4j
public class ZhongAnAutomatedPushDecisionServiceImpl implements AutomatedPushDecisionService {


    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Resource
    private ICustomerConfigService iCustomerConfigService;

    @Override
    public CustomerPushDecisionActionEnum customerAction() {
        return CustomerPushDecisionActionEnum.ZHONG_AN;
    }

    @Override
    public List<TransferActionFront> createActionFrontRows(JobPushDecisionParameterBO parameter
            , TransferActionFrontMapper mapper, String jobParameter) {
        List<TransferActionFront> resultList = new ArrayList<>();
        String extractTime = parameter.getTimeStr();
        if (StringUtils.isEmpty(extractTime)) {
            extractTime = "05:00:00";
        }
        if (StringUtils.isBlank(extractTime)) {
            return resultList;
        }
        LocalTime localTime = LocalTime.parse(extractTime);
        String apiCode = parameter.getApiCode();
        if (LocalTime.now().isAfter(localTime)) {
            String dateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            int actionType = 1;
            List<TransferActionFront> actionFrontList = getActionFrontList(apiCode, actionType, dateStr, mapper);
            if (CollectionUtils.isEmpty(actionFrontList)) {
                TransferActionFront actionFront = new TransferActionFront();
                actionFront.setActionType(actionType);
                actionFront.setStatus(actionType);
                actionFront.setCreateTime(new Date());
                actionFront.setIsDel(actionType);
                actionFront.setApiCode(apiCode);
                actionFront.setActionData(dateStr);
                resultList.add(actionFront);
            }
        }
        return resultList;
    }

    @Override
    public TransferActionFront actionData(TransferActionFront actionFront
            , JobPushDecisionParameterBO parameter
            , String jobParameter
            , MethodRetryHandlerService methodRetryHandlerService) {
        String apiCode = parameter.getApiCode();
        String tcId = tableCreateService.getTcId(apiCode);
        MarketingTransferSyncUser syncUser = new MarketingTransferSyncUser();
        syncUser.settCid(tcId);
        syncUser.setApiCode(apiCode);
        syncUser.setRequestData(LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE));
        int page = 0;
        int offset = 2000;
        int sum = 0;
        for (; ; ) {
            int rowCount = page * offset;
            List<MarketingTransferSyncUser> list = marketingTransferSyncUserMapper
                    .findTransferByApiCodeAndCreateTimePage(syncUser, null, null, ""
                            , rowCount, offset);
            if (CollectionUtils.isEmpty(list)) {
                break;
            }
            page++;
            sum += checkData(list, apiCode, parameter, methodRetryHandlerService);
            if (list.size() < offset) {
                break;
            }
        }
        TransferActionFront actionFrontUpdate = new TransferActionFront();
        actionFrontUpdate.setId(actionFront.getId());
        actionFrontUpdate.setRemark(String.valueOf(sum));
        actionFrontUpdate.setStatus(2);
        return actionFrontUpdate;
    }

    private int checkData(List<MarketingTransferSyncUser> list
            , String apiCode
            , JobPushDecisionParameterBO parameter
            , MethodRetryHandlerService methodRetryHandlerService) {
        List<PushMarketingUserDetailDTO> dtoList = new ArrayList<>();
        List<Long> ids = new ArrayList<>();
        int sum = 0;
        for (MarketingTransferSyncUser transferSyncUser : list) {
            String reserveField1 = transferSyncUser.getReserveField1();
            if (StringUtils.isBlank(reserveField1)) {
                continue;
            }
            String userType = transferSyncUser.getUserType();
            Map<String, Object> paramMap = parameter.getParamMap();
            if (CollectionUtils.isEmpty(paramMap)) {
                log.error("{}_{}未配置场景,配置参数:{}", customerAction(), apiCode, parameter);
                continue;
            }
            Object o = paramMap.get(userType);
            if (ObjectUtils.isEmpty(o)) {
                log.warn("{}_{}匹配到配置场景,配置参数:{}", customerAction(), apiCode, parameter);
                continue;
            }
            String value = String.valueOf(o);
            String[] values = value.split("&");
            String strategyCode;
            String cell = null;
            String status = values[0];
            if (values.length > 1) {
                strategyCode = values[1];
            } else {
                strategyCode = "";
            }
            try {
                JSONObject jsonObject = JSON.parseObject(reserveField1);
                // TODO: 2023-04-14 添加有效期判断 ,使用当前时间
                if (!"LOGIN".equals(jsonObject.get("eventType"))) {
                    continue;
                }
                cell = jsonObject.getString("initCustNum");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                continue;
            }
            PushMarketingUserDetailDTO dto = new PushMarketingUserDetailDTO();
            dto.setPhone(iCustomerConfigService.getThreeKeyLogToDig(apiCode, cell).getData());
            dto.setCaseNumber(transferSyncUser.getCustNum());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("userType", transferSyncUser.getUserType());
            dto.setVariables(jsonObject);
            dtoList.add(dto);
            ids.add(transferSyncUser.getId());
            sum += pushDecision(dtoList, ids, strategyCode, apiCode, methodRetryHandlerService, status);
        }
        return sum;
    }


    /**
     * 2023-03-13 17:43
     * 发送数据
     */
    private int pushDecision(List<PushMarketingUserDetailDTO> dtoList
            , List<Long> ids, String strategyCode, String apiCode
            , MethodRetryHandlerService methodRetryHandlerService, String status) {
        SecureRandom secureRandom = new SecureRandom();
        int sum = 0;
        int pageSize = 500;
        int totalCount = dtoList.size();
        int pageCount = totalCount % pageSize == 0 ? totalCount / pageSize : totalCount / pageSize + 1;
        for (int i = 1; i <= pageCount; i++) {
            List<PushMarketingUserDetailDTO> subList;
            List<Long> subIds;
            if (i == pageCount) {
                subList = dtoList.subList((i - 1) * pageSize, totalCount);
                subIds = ids.subList((i - 1) * pageSize, totalCount);
            } else {
                subList = dtoList.subList((i - 1) * pageSize, pageSize * (i));
                subIds = ids.subList((i - 1) * pageSize, pageSize * (i));
            }
            PushMarketingUserTaskInfoDTO taskInfoDTO = new PushMarketingUserTaskInfoDTO();
            taskInfoDTO.setStrategyCode(strategyCode);
            taskInfoDTO.setData(subList);
            taskInfoDTO.setAccessNumber(System.nanoTime() + String.format("%05d", secureRandom.nextInt(10000)));
            taskInfoDTO.setMethod("caseAdd");
            taskInfoDTO.setBatchNumber(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "_" + apiCode + "_" + status);
            PushMarketingUserDTO<PushMarketingUserTaskInfoDTO> pushMarketingUserDTO = new PushMarketingUserDTO<>();
            pushMarketingUserDTO.setJsonData(taskInfoDTO);
            pushMarketingUserDTO.setApiCode(apiCode);
            PolicyRetryByRuleDTO retryByRuleDTO = new PolicyRetryByRuleDTO();
            retryByRuleDTO.setIds(subIds);
            retryByRuleDTO.setInfoId(null);
            retryByRuleDTO.setPushMarketingUserDTO(pushMarketingUserDTO);
            try {
                Result<?> result = pushDecision(retryByRuleDTO, methodRetryHandlerService);
                if (result != null && ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                    sum += subList.size();
                } else {
                    log.error("客户[{}]自动化转决策失败!apiCode={}", customerAction(), apiCode);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return sum;
    }
}
