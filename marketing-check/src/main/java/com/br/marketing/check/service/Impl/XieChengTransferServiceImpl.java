package com.br.marketing.check.service.Impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.check.service.XieChengTransferService;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailByRuleDTO;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.XieChengSmsCollidingDataLog;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.XieChengSmsCollidingDataLogMapper;
import com.br.marketing.origin.MqFact;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.PolicySoleHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhenLi
 * @Description 携程转化数据处理service
 * @Date 2023/03/08 20:09
 */
@Service
@Slf4j
public class XieChengTransferServiceImpl implements XieChengTransferService {

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Resource
    private XieChengSmsCollidingDataLogMapper xieChengSmsCollidingDataLogMapper;

    @Resource
    private PolicySoleHandler policySoleHandler;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    private static String SOURCEAPICODE = "3710058";

    private static String TARGETAPICODE = "3710078";

    @Override
    public void pushDataToPolicy() {
        if (!CollectionUtils.isEmpty(marketingCommonConfig.getXieChengPushPolicyApiCode())) {
            SOURCEAPICODE = marketingCommonConfig.getXieChengPushPolicyApiCode().get(0);
        }
        String requestDate = LocalDate.now().minusDays(1).toString();
        String tcId = tableCreateService.getTcId(SOURCEAPICODE);
        Set<String> cellSets = new HashSet<>();
        //PushStatusAHandler(requestDate, tcId, cellSets);
        PushStatusBHandler(requestDate, tcId, cellSets);
        PushStatusCHandler(requestDate, tcId, cellSets);
    }

    private void PushStatusCHandler(String requestDate, String tcId, Set<String> cellSets) {

        Integer page = 0;
        Boolean mark = Boolean.TRUE;
        while (mark) {
            List<MarketingTransferSyncUser> transferSyncUserList = marketingTransferSyncUserMapper.getConvtypeData(tcId, page * 500, requestDate, "105");
            if (CollectionUtils.isEmpty(transferSyncUserList)) {
                mark = Boolean.FALSE;
                continue;
            }
            page++;
            List<String> custNums = transferSyncUserList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toList());
            List<String> filterCustNums = marketingTransferSyncUserMapper.getCustNumAndConvtypeData(tcId, custNums, requestDate, "reserve_field1->>'$.convType' ='106'");
            transferSyncUserList.removeIf(marketingTransferSyncUser -> filterCustNums.contains(marketingTransferSyncUser.getCustNum()) || cellSets.contains(marketingTransferSyncUser.getCustNum()));
            //获取orgChannel和result
            Map<String, XieChengSmsCollidingDataLog> smsCollidingDataLogMap = getSmsCollidingData(custNums);
            //推送决策
            pushPolicy(transferSyncUserList, smsCollidingDataLogMap, "c", "105");
        }
    }

    private void PushStatusBHandler(String requestDate, String tcId, Set<String> cellSets) {

        Integer page = 0;
        Boolean mark = Boolean.TRUE;
        while (mark) {
            List<MarketingTransferSyncUser> transferSyncUserList = marketingTransferSyncUserMapper.getConvtypeData(tcId, page * 500, requestDate, "108");
            if (CollectionUtils.isEmpty(transferSyncUserList)) {
                mark = Boolean.FALSE;
                continue;
            }
            page++;
            List<String> custNums = transferSyncUserList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toList());
            List<String> filterCustNums = marketingTransferSyncUserMapper.getCustNumAndConvtypeData(tcId, custNums, requestDate, "reserve_field1->>'$.convType' !='108'");
            //去重
            transferSyncUserList.removeIf(marketingTransferSyncUser -> filterCustNums.contains(marketingTransferSyncUser.getCustNum()) || cellSets.contains(marketingTransferSyncUser.getCustNum()));
            cellSets.addAll(transferSyncUserList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toList()));
            //获取orgChannel和result
            Map<String, XieChengSmsCollidingDataLog> smsCollidingDataLogMap = getSmsCollidingData(custNums);
            //推送决策
            pushPolicy(transferSyncUserList, smsCollidingDataLogMap, "b", "108");
        }
        page = 0;
        mark = Boolean.TRUE;
        while (mark) {
            List<MarketingTransferSyncUser> marketingTransferSyncUserList = marketingTransferSyncUserMapper.getConvtypeData(tcId, page * 500, requestDate, "214");
            if (CollectionUtils.isEmpty(marketingTransferSyncUserList)) {
                mark = Boolean.FALSE;
                continue;
            }
            page++;
            List<String> Conv214Nums = marketingTransferSyncUserList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toList());
            List<String> Conv214And108Nums = marketingTransferSyncUserMapper.getCustNumAndConvtypeData(tcId, Conv214Nums, requestDate, "reserve_field1->>'$.convType' ='108'");
            if (CollectionUtils.isEmpty(Conv214And108Nums)) {
                continue;
            }
            List<String> filterCustNums = marketingTransferSyncUserMapper.getCustNumAndConvtypeData(tcId, Conv214And108Nums, requestDate, "(reserve_field1->>'$.convType' !='214'  and reserve_field1->>'$.convType' !='108')");
            //去重
            marketingTransferSyncUserList.removeIf(marketingTransferSyncUser -> !Conv214And108Nums.contains(marketingTransferSyncUser.getCustNum()));
            marketingTransferSyncUserList.removeIf(marketingTransferSyncUser -> filterCustNums.contains(marketingTransferSyncUser.getCustNum()) || cellSets.contains(marketingTransferSyncUser.getCustNum()));
            cellSets.addAll(marketingTransferSyncUserList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toList()));
            //获取orgChannel和result
            Map<String, XieChengSmsCollidingDataLog> smsCollidingDataLogMap = getSmsCollidingData(Conv214And108Nums);
            //推送决策
            pushPolicy(marketingTransferSyncUserList, smsCollidingDataLogMap, "b", "108");
        }
    }

    private void PushStatusAHandler(String requestDate, String tcId, Set<String> cellSets) {
        Integer page = 0;
        Boolean mark = Boolean.TRUE;
        while (mark) {
            List<MarketingTransferSyncUser> transferSyncUserList = marketingTransferSyncUserMapper.getConvtypeData(tcId, page * 500, requestDate, "214");
            if (CollectionUtils.isEmpty(transferSyncUserList)) {
                mark = Boolean.FALSE;
                continue;
            }
            page++;
            List<String> custNums = transferSyncUserList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toList());
            List<String> filterCustNums = marketingTransferSyncUserMapper.getCustNumAndConvtypeData(tcId, custNums, requestDate, "reserve_field1->>'$.convType' !='214'");
            custNums.removeAll(filterCustNums);
            cellSets.addAll(custNums);
            //获取orgChannel和result
            Map<String, XieChengSmsCollidingDataLog> smsCollidingDataLogMap = getSmsCollidingData(custNums);
            //推送决策
            transferSyncUserList.removeIf(marketingTransferSyncUser -> filterCustNums.contains(marketingTransferSyncUser.getCustNum()));
            pushPolicy(transferSyncUserList, smsCollidingDataLogMap, "a", "214");
        }
    }

    private void pushPolicy(List<MarketingTransferSyncUser> pushDataList, Map<String, XieChengSmsCollidingDataLog> smsCollidingDataLogMap, String status, String convtype) {
        if (!CollectionUtils.isEmpty(marketingCommonConfig.getXieChengPushPolicyStatusToApiCode())) {
            TARGETAPICODE = marketingCommonConfig.getXieChengPushPolicyStatusToApiCode().get(status);
        }
        List<PushMarketingUserDetailByRuleDTO> pushMarketingUserDetailByRuleDTOList = new ArrayList<>();
        pushDataList.forEach(marketingTransferSyncUser -> {
            PushMarketingUserDetailByRuleDTO pushMarketingUserDetailByRuleDTO = new PushMarketingUserDetailByRuleDTO();
            XieChengSmsCollidingDataLog xieChengSmsCollidingDataLog = smsCollidingDataLogMap.get(marketingTransferSyncUser.getCustNum());
            pushMarketingUserDetailByRuleDTO.setCaseNumber(marketingTransferSyncUser.getCustNum());
            pushMarketingUserDetailByRuleDTO.setBatchNumber(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + status+"_"+TARGETAPICODE);
            pushMarketingUserDetailByRuleDTO.setPhone(marketingTransferSyncUser.getCustNum());
            //明文cell
            pushMarketingUserDetailByRuleDTO.setCell(RpcClientProxy.decode(marketingTransferSyncUser.getCustNum(), "cell", "sha", ""));
            pushMarketingUserDetailByRuleDTO.setInitId(marketingTransferSyncUser.getId());
            JSONObject varDto = new JSONObject();
            varDto.put("status", status);
            varDto.put("coveType", convtype);
            varDto.put("requestTime", marketingTransferSyncUser.getRequestData());
            if (ObjectUtil.isNotEmpty(xieChengSmsCollidingDataLog)) {
                varDto.put("result", Objects.isNull(xieChengSmsCollidingDataLog.getResult())?"":xieChengSmsCollidingDataLog.getResult());
                varDto.put("orgChannel", Objects.isNull(xieChengSmsCollidingDataLog.getOrgChannel())?"":xieChengSmsCollidingDataLog.getOrgChannel());
            } else {
                varDto.put("result", "");
                varDto.put("orgChannel", "");
            }
            pushMarketingUserDetailByRuleDTO.setVariables(varDto);
            pushMarketingUserDetailByRuleDTO.setStrategyCode("");
            pushMarketingUserDetailByRuleDTO.setStatus(status);
            pushMarketingUserDetailByRuleDTO.setSoleField(SoleFieldEnum.CELL_STATUS_SOLE.getValue());
            pushMarketingUserDetailByRuleDTO.setSoleType(marketingCommonConfig.getXieChengPushPolicyValidityDay());
            pushMarketingUserDetailByRuleDTOList.add(pushMarketingUserDetailByRuleDTO);
        });
        ProcessHandlerContext context = new ProcessHandlerContext();
        context.setApiCode(TARGETAPICODE);
        context.setMqFact(new MqFact());
        policySoleHandler.call(pushMarketingUserDetailByRuleDTOList, context);
        log.warn("携程推送决策情况apiCode={},status={},convtype={},pushNum={}", TARGETAPICODE, status, convtype, pushDataList.size());
    }

    private Map<String, XieChengSmsCollidingDataLog> getSmsCollidingData(List<String> cellSets) {
        List<XieChengSmsCollidingDataLog> xieChengSmsCollidingDataLogs = xieChengSmsCollidingDataLogMapper.getDataByCells(cellSets);

        Map<String, XieChengSmsCollidingDataLog> smsCollidingDataLogMap = xieChengSmsCollidingDataLogs.stream().collect(
                Collectors.groupingBy(XieChengSmsCollidingDataLog::getSha256CodeList
                        , Collectors.collectingAndThen(
                                Collectors.reducing((v1, v2) ->
                                        v1.getCreateTime().compareTo(v2.getCreateTime()) > 0 ? v1 : v2)
                                , Optional::get)));
        return smsCollidingDataLogMap;

    }
}
