package com.br.marketing.check.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.bo.JobPushDecisionParameterBO;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.check.service.AutomatedPushDecisionService;
import com.br.marketing.client.intelligentcustomerservice.input.*;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.enums.CustomerPushDecisionActionEnum;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.TransferActionFrontMapper;
import com.br.marketing.origin.MqFact;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.rpcclient.rpcclientImpl.DecodeGrpcClient;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.strategy.MethodRetryHandlerService;
import com.br.marketing.strategy.PolicySoleHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
    private PolicySoleHandler policySoleHandler;


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
            int actionType = 2;
            List<TransferActionFront> actionFrontList = getActionFrontList(apiCode, actionType, dateStr, mapper);
            if (CollectionUtils.isEmpty(actionFrontList)) {
                TransferActionFront actionFront = new TransferActionFront();
                actionFront.setActionType(actionType);
                actionFront.setStatus(1);
                actionFront.setCreateTime(new Date());
                actionFront.setIsDel(1);
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
        if (parameter.getParamList() != null && parameter.getParamList().size() > 0) {
            return buShuData(actionFront, parameter, jobParameter, methodRetryHandlerService);
        }
        String apiCode = parameter.getApiCode();
        String tcId = tableCreateService.getTcId(apiCode);
        MarketingTransferSyncUser syncUser = new MarketingTransferSyncUser();
        syncUser.settCid(tcId);
        syncUser.setApiCode(apiCode);
        syncUser.setRequestData(LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE));
        int page = 0;
        int offset = 500;
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

    private TransferActionFront buShuData(TransferActionFront actionFront
            , JobPushDecisionParameterBO parameter
            , String jobParameter
            , MethodRetryHandlerService methodRetryHandlerService) {
        String newJobParameter = jobParameter;
        Object o = parameter.getParamList().get(0);
        String requestDate = (String) o;
        String apiCode = parameter.getApiCode();
        String tcId = tableCreateService.getTcId(apiCode);
        MarketingTransferSyncUser syncUser = new MarketingTransferSyncUser();
        syncUser.settCid(tcId);
        syncUser.setApiCode(apiCode);
        syncUser.setRequestData(requestDate);
        int page = 0;
        int offset = 500;
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
        MethodRetryHandlerService methodRetryHandlerServiceNew = methodRetryHandlerService;
        Map<String, Object> paramMap = parameter.getParamMap();
        int sum = 0;
        if (CollectionUtils.isEmpty(paramMap)) {
            log.error("{}_{}未配置场景,配置参数:{}", customerAction(), apiCode, parameter);
            return sum;
        }
        //{"ZHONG_AN":[{"apiCode":"3710048","timeStr":"09:00:00","paramMap":{"1":"a","2":"b"}},
        // {"apiCode":"7410906","timeStr":"09:00:00","paramMap":{"1":"a","2":"b"}}]}
        Set<String> custNumLists = list.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
        // 循环配置的场景
        for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
            String userType = entry.getKey();
            Object o = entry.getValue();
            List<PushMarketingUserDetailByRuleDTO> pushMarketingUserDetailByRuleDTOList = new ArrayList<>();
            Map<String, SyncUserValidityPeriodsBO> validityPeriodsByCustNumAndUserType = transferDataValidityPeriodService
                    .getValidityPeriodsByCustNumAndUserType(custNumLists, userType, apiCode, new Date());
            if (validityPeriodsByCustNumAndUserType != null) {
                for (MarketingTransferSyncUser transferSyncUser : list) {
                    try {
                        SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = validityPeriodsByCustNumAndUserType.get(transferSyncUser.getCustNum());
                        // 有效
                        if (syncUserValidityPeriodsBO != null) {
                            String reserveField1 = transferSyncUser.getReserveField1();
                            if (!StringUtils.isBlank(reserveField1)) {
                                JSONObject jsonObjectReserveField1 = JSON.parseObject(reserveField1);
                                if (!jsonObjectReserveField1.isEmpty() && (
                                        isEventType(jsonObjectReserveField1, userType)
                                                || isType(jsonObjectReserveField1, userType)
                                                || isEventTypeTwo(jsonObjectReserveField1, userType)
                                                || isTypeTwo(jsonObjectReserveField1, userType)
                                )
                                ) {
                                    String value = String.valueOf(o);
                                    String[] values = value.split("&");
                                    String strategyCode;
                                    String status = values[0];
                                    if (values.length > 1) {
                                        strategyCode = values[1];
                                    } else {
                                        strategyCode = "";
                                    }
                                    String cell = jsonObjectReserveField1.getString("initCustNum");
                                    if (StringUtils.isBlank(cell)) {
                                        continue;
                                    }
                                    // 推送
                                    PushMarketingUserDetailByRuleDTO pushMarketingUserDetailByRuleDTO = new PushMarketingUserDetailByRuleDTO();
                                    pushMarketingUserDetailByRuleDTO.setCaseNumber(transferSyncUser.getCustNum());
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("userType", transferSyncUser.getUserType());
                                    pushMarketingUserDetailByRuleDTO.setVariables(jsonObject);
                                    pushMarketingUserDetailByRuleDTO.setStrategyCode(strategyCode);
                                    pushMarketingUserDetailByRuleDTO.setStatus(status);
                                    pushMarketingUserDetailByRuleDTO.setPhone(cell);
                                    pushMarketingUserDetailByRuleDTO.setCell(decodePhone(cell));
                                    pushMarketingUserDetailByRuleDTO.setInitId(transferSyncUser.getId());
                                    pushMarketingUserDetailByRuleDTO.setBatchNumber(
                                            LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "_" + apiCode + "_" + status);

                                    pushMarketingUserDetailByRuleDTOList.add(pushMarketingUserDetailByRuleDTO);
                                }
                            }

                        }
                    } catch (Exception e) {
                        log.warn(e.getMessage(), e);
                    }
                }
            }
            sum = pushMarketingUserDetailByRuleDTOList.size();
            ProcessHandlerContext context = new ProcessHandlerContext();
            context.setApiCode(apiCode);
            context.setMqFact(new MqFact());
            //推送决策
            policySoleHandler.call(pushMarketingUserDetailByRuleDTOList, context);
        }
        return sum;
    }

    private static boolean isTypeTwo(JSONObject jsonObjectReserveField1, String userType) {
        return "APP_LAUNCH".equals(jsonObjectReserveField1.get("eventType")) && "2".equals(userType);
    }

    private static boolean isEventTypeTwo(JSONObject jsonObjectReserveField1, String userType) {
        return "LOGIN".equals(jsonObjectReserveField1.get("eventType")) && "2".equals(userType);
    }

    private static boolean isType(JSONObject jsonObjectReserveField1, String userType) {
        return "APP_LAUNCH".equals(jsonObjectReserveField1.get("eventType")) && "1".equals(userType);
    }

    private static boolean isEventType(JSONObject jsonObjectReserveField1, String userType) {
        return "APP_LOGIN".equals(jsonObjectReserveField1.get("eventType")) && "1".equals(userType);
    }

    private String decodePhone(String cell) {
        if (DecodeGrpcClient.isMd5(cell)) {
            //cell md5
            return RpcClientProxy.decode(cell, "cell", "md5", "");
        } else if (cell.length() == 64) {
            RpcClientProxy.decode(cell, "cell", "sha", "");
        }
        return "";
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
