package com.br.marketing.check.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.check.service.OriginPeriodPredicateService;
import com.br.marketing.client.intelligentcustomerservice.input.PolicyRetryByRuleSoleDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailDTO;
import com.br.marketing.common.enums.DistributeSourceTypeEnum;
import com.br.marketing.common.enums.DistributeTypeEnum;
import com.br.marketing.dto.DataJoinLogDTO;
import com.br.marketing.entity.MarketingTransferSyncUserCell;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.*;

/**
 * @author GuangChao.Zhang
 * @version 1.0
 * @date 2023/3/17 16:04
 */
@Service
@Slf4j
public class OriginPeriodPredicateToJueCeServiceImpl implements OriginPeriodPredicateService {

    private final static Set<String> statusSet = new HashSet<String>(){{
        add("d");
    }};


    @Autowired
    private MethodRetryHandlerService methodRetryHandlerService;
    @Autowired
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public void transferDataPeriod(String apiCode,String status, Set<MarketingTransferSyncUserCell> marketingTransferSyncUserCellList) {
        // 推决策
        if(statusSet.contains(status)){
            ArrayList<DataJoinLogDTO> logList = new ArrayList<>();
            ArrayList<PushMarketingUserDetailDTO> pushs = new ArrayList<>();

            marketingTransferSyncUserCellList.forEach(marketingTransferSyncUserCell -> {
                PushMarketingUserDetailDTO marketingUserDetailDTO = new PushMarketingUserDetailDTO();
                marketingUserDetailDTO.setCaseNumber(marketingTransferSyncUserCell.getCustNum());
                // log解密  md5加密
                String cell = DigestUtils.md5DigestAsHex(BrCipherMaker.getInstance().decode(marketingTransferSyncUserCell.getCell()).getBytes());
                marketingUserDetailDTO.setPhone(cell);
                marketingUserDetailDTO.setVariables(new JSONObject());
                pushs.add(marketingUserDetailDTO);
                // 把封装的日志插入到数组中
                logList.add(methodRetryHandlerService.dataJoinLogFix(marketingUserDetailDTO, DistributeTypeEnum.POLICYDATA
                        , apiCode, marketingTransferSyncUserCell.getCustNum(), marketingTransferSyncUserCell.getCell()
                        , null, DistributeSourceTypeEnum.TRANSFER, status,null));
                    }
            );
            PolicyRetryByRuleSoleDTO retryByRuleDTO = new PolicyRetryByRuleSoleDTO();
            retryByRuleDTO.setApiCode(apiCode);
            retryByRuleDTO.setBatchNumber(DateFormatUtils.format(new Date(), "yyyyMMdd")+"d_"+apiCode);
            retryByRuleDTO.setStrategyCode(marketingCommonConfig.getOriginStrategyMap().get(apiCode));
            retryByRuleDTO.setData(pushs);
            retryByRuleDTO.setDetailLogList(logList);
            //传参去重
            retryByRuleDTO.setIsSole(true);
            //2-根据apicode cell,status 维度去重
            retryByRuleDTO.setSoleField(3);
            methodRetryHandlerService.callPolicySoleData(retryByRuleDTO, 0);
        }

    }
}
