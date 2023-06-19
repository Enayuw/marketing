package com.br.marketing.service.Impl;

import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.service.Impl.yixin.YiXinProcessExcludeRuleData;
import com.br.marketing.service.Impl.yixin.YiXinProcessGetBaseDataService;
import com.br.marketing.service.YiXinToJueCeProcessService;
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

    @Override
    public void doProcess(String actionType) {
        //Todo 1. 查询 a 情况基础数据
        //     2. 数据剔除
        //     3. 数据组装推送
        switch (actionType) {
            // a 情况推送
            case "A":
                pushMarketingTransferSyncUsers_A();
                break;
            // b 情况推送
            case "B":
                pushMarketingTransferSyncUsers_B();
                break;
            // c~i 情况推送
            case "C":
            case "D":
            case "E":
            case "F":
            case "G":
            case "H":
            case "I":
                getMarketingTransferSyncUsers_C_to_I(actionType);
                break;
            default:
                break;
        }

    }

    private void getMarketingTransferSyncUsers_C_to_I(String actionType) {
        Long minId = null;;
        while (true) {
            List<MarketingTransferSyncUser> marketingTransferSyncUserList = yiXinProcessGetBaseDataService.getMarketingTransferSyncUserList_C_to_I(actionType, minId);
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

    private void pushMarketingTransferSyncUsers_B() {
        Long minId = null;;
        while (true) {
            List<MarketingTransferSyncUser>  marketingTransferSyncUserList = yiXinProcessGetBaseDataService.getMarketingTransferSyncUserList_B(minId);
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


    private void pushMarketingTransferSyncUsers_A() {
        Long minId = null;;
        while (true) {
            List<MarketingTransferSyncUser> marketingTransferSyncUserList = yiXinProcessGetBaseDataService.getMarketingTransferSyncUserList_A(minId);
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
