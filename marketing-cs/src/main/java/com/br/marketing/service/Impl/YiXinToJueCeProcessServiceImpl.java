package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.intelligentcustomerservice.input.PolicyRetryByRuleSoleDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailDTO;
import com.br.marketing.common.enums.DistributeSourceTypeEnum;
import com.br.marketing.common.enums.DistributeTypeEnum;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.dto.DataJoinLogDTO;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUserCell;
import com.br.marketing.service.Impl.yixin.YiXinProcessExcludeRuleData;
import com.br.marketing.service.Impl.yixin.YiXinProcessGetBaseDataService;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.service.YiXinToJueCeProcessService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 宜信推决策流程
 * @author GuangChao.Zhang
 * @version 1.0
 * @date 2023/6/16 17:23
 */
@Service
@Slf4j
public class YiXinToJueCeProcessServiceImpl implements YiXinToJueCeProcessService {

    @Resource
    private YiXinProcessGetBaseDataService yiXinProcessGetBaseDataService;

    @Resource
    private YiXinProcessExcludeRuleData yiXinProcessExcludeRuleData;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;


    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;


    private static final  int PARTITION = 2000;


    @Override
    public void doProcess(TreeMap<String, String> actionTypeTree, String tcId) {
        actionTypeTree.forEach((String k, String v) -> {
            switch (k) {
                case "A":
                    pushMarketingTransferSyncUsersA(k, v, tcId);
                    break;
                case "B":
                    pushMarketingTransferSyncUsersB(k, v, tcId);
                    break;
                case "C":
                case "D":
                case "E":
                case "F":
                case "G":
                case "H":
                case "I":
                    getMarketingTransferSyncUsersCtoI(k, v, tcId);
                    break;
                default:
                    log.warn("宜信转化数据推决策类型异常");
                    break;
            }
        });


    }

    /**
     * 情况 a
     * @param tcId cid
     */
    private void pushMarketingTransferSyncUsersA(String actionType, String type, String tcId) {
        Long idIndex = null;
        while (true) {
            List<MarketingTransferSyncUser> marketingTransferSyncUserList =
                    yiXinProcessGetBaseDataService.getMarketingTransferSyncUserListA(tcId,
                    type, idIndex);
            if (marketingTransferSyncUserList.isEmpty()) {
                break;
            }
            idIndex = marketingTransferSyncUserList.get(marketingTransferSyncUserList.size() - 1).getId();
            yiXinProcessExcludeRuleData.excludeActionA(marketingTransferSyncUserList);
            pushToJueCe(actionType, marketingTransferSyncUserList);
        }
    }


    /**
     * 情况 b
     * @param tcId cid
     */
    private void pushMarketingTransferSyncUsersB(String actionType, String type, String tcId) {
        Long idIndex = null;
        while (true) {
            List<MarketingTransferSyncUser> marketingTransferSyncUserList =
                    yiXinProcessGetBaseDataService.getMarketingTransferSyncUserListB(tcId,
                    type, idIndex);
            if (marketingTransferSyncUserList.isEmpty()) {
                break;
            }
            idIndex = marketingTransferSyncUserList.get(marketingTransferSyncUserList.size() - 1).getId();
            yiXinProcessExcludeRuleData.excludeActionB(marketingTransferSyncUserList);
            pushToJueCe(actionType, marketingTransferSyncUserList);
        }
    }

    /**
     * 情况 c~i
     * @param tcId cid
     */
    private void getMarketingTransferSyncUsersCtoI(String actionType, String type, String tcId) {

        Long idIndex = null;
        while (true) {
            List<MarketingTransferSyncUser> marketingTransferSyncUserList =
                    yiXinProcessGetBaseDataService.getMarketingTransferSyncUserListCtoI(tcId, type, idIndex);
            if (marketingTransferSyncUserList.isEmpty()) {
                break;
            }
            idIndex = marketingTransferSyncUserList.get(marketingTransferSyncUserList.size() - 1).getId();
            yiXinProcessExcludeRuleData.excludeActionCtoI(marketingTransferSyncUserList);
            pushToJueCe(actionType, marketingTransferSyncUserList);
        }
    }

    private static List<List<MarketingTransferSyncUser>> getPartitionSyncUser(List<MarketingTransferSyncUser> marketingTransferSyncUserList) {
        // 2000 拆分一组
        return ListUtils.partition(marketingTransferSyncUserList, PARTITION);
    }


    private void pushToJueCe(String actionType, List<MarketingTransferSyncUser> e) {
        if (CollectionUtils.isNotEmpty(e)) {
            pushJc(actionType, getMarketingTransferSyncUserCells(e));
        }
    }

    /**
     * 获取上传数据最新的一条数据
     * @param marketingTransferSyncUserList 转化数据
     * @return 最新的数据
     */
    private List<MarketingTransferSyncUserCell> getMarketingTransferSyncUserCells(List<MarketingTransferSyncUser> marketingTransferSyncUserList) {
        return marketingTransferSyncUserList.stream().map(jc -> transferDataValidityPeriodService.getNewValidityPeriodTransferData(jc, null))
                .collect(Collectors.toList()).stream().filter(Objects::nonNull)
                .collect(Collectors.toList());
    }


    /**
     * 推送决策逻辑
     * @param actionType                         情况说明
     * @param marketingTransferSyncUserCellLists 带电话的转化数据
     */
    private void pushJc(String actionType, List<MarketingTransferSyncUserCell> marketingTransferSyncUserCellLists) {
        String apiCodeJc = marketingCommonConfig.getYiXinTransferToJueCeApiCode();
        // 2000 拆分一组
        List<List<MarketingTransferSyncUserCell>> partition = ListUtils.partition(marketingTransferSyncUserCellLists, 2000);
        partition.forEach((List<MarketingTransferSyncUserCell> m)  -> {
            ArrayList<DataJoinLogDTO> logList = new ArrayList<>();
            ArrayList<PushMarketingUserDetailDTO> pushs = new ArrayList<>();
            // 决策数据初始化 pushs
            pushDataInit(actionType, apiCodeJc, m, logList, pushs);
            // 封装重试参数
            PolicyRetryByRuleSoleDTO retryByRuleDTO = getPolicyRetryByRuleSoleDTO(actionType, apiCodeJc, logList, pushs);
            // 推送决策方法
            methodRetryHandlerService.callPolicySoleData(retryByRuleDTO, 0);
        });

    }

    private PolicyRetryByRuleSoleDTO getPolicyRetryByRuleSoleDTO(String actionType,
                                                                 String apiCodeJc,
                                                                 ArrayList<DataJoinLogDTO> logList,
                                                                 ArrayList<PushMarketingUserDetailDTO> pushs) {
        PolicyRetryByRuleSoleDTO retryByRuleDTO = new PolicyRetryByRuleSoleDTO();
        retryByRuleDTO.setApiCode(apiCodeJc);
        retryByRuleDTO.setBatchNumber(DateFormatUtils.format(new Date(), "yyyyMMdd") + "_" + actionType.toLowerCase() + "_" + apiCodeJc);
        retryByRuleDTO.setStrategyCode(marketingCommonConfig.getYxXinToJueCeStrategyMap().get(apiCodeJc));
        retryByRuleDTO.setData(pushs);
        retryByRuleDTO.setDetailLogList(logList);
        //传参去重
        retryByRuleDTO.setIsSole(Boolean.TRUE);
        if ("A".equals(actionType)) {
            // apiCode,cell,status
            retryByRuleDTO.setSoleField(SoleFieldEnum.CUST_NUM_STATUS_SOLE.getValue());
            // 周一的数据 下周一推送判断的范围是周一到周日。
            retryByRuleDTO.setSoleDay(7);
        } else {
            // apiCode,custNum
            retryByRuleDTO.setSoleField(SoleFieldEnum.CUST_NUM_SOLE.getValue());
        }

        return retryByRuleDTO;
    }

    private void pushDataInit(String actionType,
                              String apiCodeJc,
                              List<MarketingTransferSyncUserCell> marketingTransferSyncUserCells,
                              ArrayList<DataJoinLogDTO> logList,
                              ArrayList<PushMarketingUserDetailDTO> pushs) {
        marketingTransferSyncUserCells.forEach( (MarketingTransferSyncUserCell m) -> {
                    PushMarketingUserDetailDTO marketingUserDetailDTO = new PushMarketingUserDetailDTO();
                    marketingUserDetailDTO.setCaseNumber(m.getCustNum());
                    // log解密  md5加密
                    String cell = DigestUtils.md5DigestAsHex(
                            BrCipherMaker.getInstance().decode(m.getCell()).getBytes(StandardCharsets.UTF_8));
                    marketingUserDetailDTO.setPhone(cell);
                    marketingUserDetailDTO.setVariables(variablesInit(m, cell, actionType));
                    pushs.add(marketingUserDetailDTO);
                    // 把封装的日志插入到数组中
                    logList.add(methodRetryHandlerService.dataJoinLogFix(marketingUserDetailDTO, DistributeTypeEnum.POLICYDATA
                            , apiCodeJc, m.getCustNum(), m.getCell()
                            , null, DistributeSourceTypeEnum.TRANSFER, actionType, null));
                }
        );
    }

    private JSONObject variablesInit(MarketingTransferSyncUserCell marketingTransferSyncUserCell, String cell, String actionType) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("custNum", marketingTransferSyncUserCell.getCustNum());
        jsonObject.put("cell", cell);
        jsonObject.put("userType", marketingTransferSyncUserCell.getUserType());
        switch (actionType) {
            case "D":
                jsonObject.put("unlentAmount", marketingTransferSyncUserCell.getUnlentAmount());
                JSONObject parsed = JSONObject.parseObject(marketingTransferSyncUserCell.getReserveField1());
                jsonObject.put("availableAmount", parsed.get("availableAmount"));
                break;
            case "E":
            case "F":
            case "G":
                JSONObject parse = JSONObject.parseObject(marketingTransferSyncUserCell.getReserveField1());
                jsonObject.put("raiseLimiType", parse.get("raiseLimiType"));
                jsonObject.put("raiseLimiSuccess", parse.get("raiseLimiSuccess"));
                jsonObject.put("recommendType", parse.get("recommendType"));
                break;
            case "H":
                JSONObject parseh = JSONObject.parseObject(marketingTransferSyncUserCell.getReserveField1());
                jsonObject.put("availableAmount", parseh.get("availableAmount"));
                break;
            default:
                break;
        }
        return jsonObject;
    }

}
