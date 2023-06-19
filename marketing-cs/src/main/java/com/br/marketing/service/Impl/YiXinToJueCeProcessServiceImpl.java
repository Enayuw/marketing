package com.br.marketing.service.Impl;

import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.service.Impl.yixin.YiXinProcessExcludeRuleData;
import com.br.marketing.service.Impl.yixin.YiXinProcessGetBaseDataService;
import com.br.marketing.service.YiXinToJueCeProcessService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

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
    private TableCreateServiceImpl tableCreateService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public void doProcess(String actionType,String tcId) {
        //Todo 1. 查询 a 情况基础数据
        //     2. 数据剔除
        //     3. 数据组装推送
        switch (actionType) {
            // a 情况推送
            case "A":
                pushMarketingTransferSyncUsers_A(tcId);
                break;
            // b 情况推送
            case "B":
                pushMarketingTransferSyncUsers_B(tcId);
                break;
            // c~i 情况推送
            case "C":
            case "D":
            case "E":
            case "F":
            case "G":
            case "H":
            case "I":
                getMarketingTransferSyncUsers_C_to_I(actionType,tcId);
                break;
            default:
                log.warn("宜信转化数据推决策类型异常");
                break;
        }

    }

    private void getMarketingTransferSyncUsers_C_to_I(String actionType,String tcId) {
        Long minId = null;

        while (true) {
            List<MarketingTransferSyncUser> marketingTransferSyncUserList = yiXinProcessGetBaseDataService.getMarketingTransferSyncUserList_C_to_I(tcId,actionType, minId);
            minId = marketingTransferSyncUserList.get(marketingTransferSyncUserList.size() - 1).getId();
            if (marketingTransferSyncUserList.size() == 0) {
                break;
            }
            marketingTransferSyncUserList.removeIf((e -> yiXinProcessExcludeRuleData.action_C_to_I(e)));
            if (marketingTransferSyncUserList.size() > 0) {
                // 调用推送决策接口
            }

        }

    }

    private void pushMarketingTransferSyncUsers_B(String tcId) {
        Long minId = null;
        while (true) {

            List<MarketingTransferSyncUser>  marketingTransferSyncUserList = yiXinProcessGetBaseDataService.getMarketingTransferSyncUserList_B(tcId,minId);
            minId = marketingTransferSyncUserList.get(marketingTransferSyncUserList.size() - 1).getId();
            if (marketingTransferSyncUserList.size() == 0) {
                break;
            }
            marketingTransferSyncUserList.removeIf((e -> yiXinProcessExcludeRuleData.action_B(e)));
            if (marketingTransferSyncUserList.size() > 0) {
                // 调用推送决策接口
            }
        }
    }


    private void pushMarketingTransferSyncUsers_A(String tcId) {
        Long minId = null;
        while (true) {
            List<MarketingTransferSyncUser> marketingTransferSyncUserList = yiXinProcessGetBaseDataService.getMarketingTransferSyncUserList_A(tcId,minId);
            minId = marketingTransferSyncUserList.get(marketingTransferSyncUserList.size() - 1).getId();
            if (marketingTransferSyncUserList.size() == 0) {
                break;
            }
            marketingTransferSyncUserList.removeIf((e -> yiXinProcessExcludeRuleData.action_A(e)));
            if (marketingTransferSyncUserList.size() > 0) {
                // 调用推送决策接口
            }
        }

    }

}
