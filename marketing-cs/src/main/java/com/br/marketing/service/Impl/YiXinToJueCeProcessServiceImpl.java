package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.intelligentcustomerservice.input.PolicyRetryByRuleSoleDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailDTO;
import com.br.marketing.common.enums.DistributeSourceTypeEnum;
import com.br.marketing.common.enums.DistributeTypeEnum;
import com.br.marketing.dto.DataJoinLogDTO;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUserCell;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.service.Impl.yixin.YiXinProcessExcludeRuleData;
import com.br.marketing.service.Impl.yixin.YiXinProcessGetBaseDataService;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.service.YiXinToJueCeProcessService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 宜信推决策流程
 *
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



    @Override
    public void doProcess(String actionType, String tcId) {
        switch (actionType) {
            case "A":
                pushMarketingTransferSyncUsers_A(tcId);
                break;
            case "B":
                pushMarketingTransferSyncUsers_B(tcId);
                break;
            case "C":
            case "D":
            case "E":
            case "F":
            case "G":
            case "H":
            case "I":
                getMarketingTransferSyncUsers_C_to_I(actionType, tcId);
                break;
            default:
                log.warn("宜信转化数据推决策类型异常");
                break;
        }

    }
    /**
     * 情况 c~i
     * @param tcId cid
     */
    private void getMarketingTransferSyncUsers_C_to_I(String actionType, String tcId) {

        Long minId = null;
        while (true) {
            List<MarketingTransferSyncUser> marketingTransferSyncUserList = yiXinProcessGetBaseDataService.getMarketingTransferSyncUserList_C_to_I(tcId, actionType, minId);
            minId = marketingTransferSyncUserList.get(marketingTransferSyncUserList.size() - 1).getId();
            if (marketingTransferSyncUserList.size() == 0) {
                break;
            }
            marketingTransferSyncUserList.removeIf((e -> yiXinProcessExcludeRuleData.action_C_to_I(e)));
            if (marketingTransferSyncUserList.size() > 0) {
                pushJc(actionType, getMarketingTransferSyncUserCells(marketingTransferSyncUserList));
            }
        }
    }
    /**
     * 情况 b
     * @param tcId cid
     */
    private void pushMarketingTransferSyncUsers_B(String tcId) {
        Long minId = null;
        while (true) {
            List<MarketingTransferSyncUser> marketingTransferSyncUserList = yiXinProcessGetBaseDataService.getMarketingTransferSyncUserList_B(tcId, minId);
            minId = marketingTransferSyncUserList.get(marketingTransferSyncUserList.size() - 1).getId();
            if (marketingTransferSyncUserList.size() == 0) {
                break;
            }
            marketingTransferSyncUserList.removeIf((e -> yiXinProcessExcludeRuleData.action_B(e)));
            if (marketingTransferSyncUserList.size() > 0) {
                // 调用推送决策接口
                pushJc("b", getMarketingTransferSyncUserCells(marketingTransferSyncUserList));
            }
        }
    }


    /**
     * 情况 a
     * @param tcId cid
     */
    private void pushMarketingTransferSyncUsers_A(String tcId) {
        Long minId = null;
        while (true) {
            List<MarketingTransferSyncUser> marketingTransferSyncUserList = yiXinProcessGetBaseDataService.getMarketingTransferSyncUserList_A(tcId, minId);
            minId = marketingTransferSyncUserList.get(marketingTransferSyncUserList.size() - 1).getId();
            if (marketingTransferSyncUserList.size() == 0) {
                break;
            }
            marketingTransferSyncUserList.removeIf((e -> yiXinProcessExcludeRuleData.action_A(e)));
            if (marketingTransferSyncUserList.size() > 0) {
                pushJc("a", getMarketingTransferSyncUserCells(marketingTransferSyncUserList));
            }
        }
    }

    /**
     * 获取上传数据最新的一条数据
     *
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
     *
     * @param actionType                         情况说明
     * @param marketingTransferSyncUserCellLists 带电话的转化数据
     */
    private void pushJc(String actionType, List<MarketingTransferSyncUserCell> marketingTransferSyncUserCellLists) {
        String apiCodeJc = marketingCommonConfig.getYiXinTransferToJueCeApiCode();
        // 2000 拆分一组
        List<List<MarketingTransferSyncUserCell>> partition = ListUtils.partition(marketingTransferSyncUserCellLists, 2000);
        partition.forEach(marketingTransferSyncUserCells->{
            ArrayList<DataJoinLogDTO> logList = new ArrayList<>();
            ArrayList<PushMarketingUserDetailDTO> pushs = new ArrayList<>();
            // 决策数据初始化 pushs
            pushDataInit(actionType, apiCodeJc, marketingTransferSyncUserCells, logList, pushs);
            // 封装重试参数
            PolicyRetryByRuleSoleDTO retryByRuleDTO = getPolicyRetryByRuleSoleDTO(actionType, apiCodeJc, logList, pushs);
            // 推送决策方法
            methodRetryHandlerService.callPolicySoleData(retryByRuleDTO, 0);
        });

    }

    private PolicyRetryByRuleSoleDTO getPolicyRetryByRuleSoleDTO(String actionType, String apiCodeJc, ArrayList<DataJoinLogDTO> logList, ArrayList<PushMarketingUserDetailDTO> pushs) {
        PolicyRetryByRuleSoleDTO retryByRuleDTO = new PolicyRetryByRuleSoleDTO();
        retryByRuleDTO.setApiCode(apiCodeJc);
        retryByRuleDTO.setBatchNumber(DateFormatUtils.format(new Date(), "yyyyMMdd") + apiCodeJc + actionType);
        retryByRuleDTO.setStrategyCode(marketingCommonConfig.getYxXinToJueCeStrategyMap().get(apiCodeJc));
        retryByRuleDTO.setData(pushs);
        retryByRuleDTO.setDetailLogList(logList);
        //传参去重
        retryByRuleDTO.setIsSole(true);
        if (actionType.equals("a")) {
            // a 情况 7 天配置

            //2-根据apicode cell,status 维度去重
            retryByRuleDTO.setSoleField(3);

        }else {
            // 1-apiCode,cell
            retryByRuleDTO.setSoleField(2);
        }
        return retryByRuleDTO;
    }

    private void pushDataInit(String actionType, String apiCodeJc, List<MarketingTransferSyncUserCell> marketingTransferSyncUserCells, ArrayList<DataJoinLogDTO> logList, ArrayList<PushMarketingUserDetailDTO> pushs) {
        marketingTransferSyncUserCells.forEach(marketingTransferSyncUserCell -> {
                    PushMarketingUserDetailDTO marketingUserDetailDTO = new PushMarketingUserDetailDTO();
                    marketingUserDetailDTO.setCaseNumber(marketingTransferSyncUserCell.getCustNum());
                    // log解密  md5加密
                    String cell = DigestUtils.md5DigestAsHex(BrCipherMaker.getInstance().decode(marketingTransferSyncUserCell.getCell()).getBytes());
                    marketingUserDetailDTO.setPhone(cell);
                    marketingUserDetailDTO.setVariables(variablesInit(marketingTransferSyncUserCell, cell, actionType));
                    pushs.add(marketingUserDetailDTO);
                    // 把封装的日志插入到数组中
                    logList.add(methodRetryHandlerService.dataJoinLogFix(marketingUserDetailDTO, DistributeTypeEnum.POLICYDATA
                            , apiCodeJc, marketingTransferSyncUserCell.getCustNum(), marketingTransferSyncUserCell.getCell()
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
            case "d":
                jsonObject.put("unlentAmount", marketingTransferSyncUserCell.getUnlentAmount());
                break;
            case "e":
            case "f":
            case "g":
                JSONObject parse = JSONObject.parseObject(marketingTransferSyncUserCell.getReserveField1());
                jsonObject.put("raiseLimiType", parse.get("raiseLimiType"));
                jsonObject.put("raiseLimiSuccess", parse.get("raiseLimiSuccess"));
                break;
            default:
                break;
        }
        return jsonObject;
    }

}
