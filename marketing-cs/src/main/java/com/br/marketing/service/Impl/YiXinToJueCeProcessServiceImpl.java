package com.br.marketing.service.Impl;

import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.service.Impl.YiXin.YiXinProcessExcludeRuleData;
import com.br.marketing.service.Impl.YiXin.YiXinProcessGetBaseDataService;
import com.br.marketing.service.YiXinToJueCeProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

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
    @Override
    public void doProcess(String actionType) {
    //Todo 1. 查询 a 情况基础数据
    //     2. 数据剔除
    //     3. 数据组装推送
        List<MarketingTransferSyncUser> marketingTransferSyncUserList = new ArrayList<>();
        int index=0;
        switch (actionType){
            // a 情况推送
            case "A":
                marketingTransferSyncUserList = getMarketingTransferSyncUsers_A(index);
                break;
           // b 情况推送
            case "B":
                marketingTransferSyncUserList = getMarketingTransferSyncUsers_B(index);
                break;
           // c~i 情况推送
            case "C":
            case "D":
            case "E":
            case "F":
            case "G":
            case "H":
            case "I":
                marketingTransferSyncUserList = getMarketingTransferSyncUsers_C_to_I(actionType, index);
                break;
            default:
                break;
        }
        if(marketingTransferSyncUserList.size()>0){
            // 调用推送决策接口
        }

    }

    private List<MarketingTransferSyncUser> getMarketingTransferSyncUsers_C_to_I(String actionType, int index) {
        List<MarketingTransferSyncUser> marketingTransferSyncUserList;
        while (true){
            marketingTransferSyncUserList = yiXinProcessGetBaseDataService.getMarketingTransferSyncUserList_C_to_I(actionType, index);
            index = marketingTransferSyncUserList.size()-1;
            if(marketingTransferSyncUserList.size()==0){
                break;
            }
            marketingTransferSyncUserList.removeIf((e -> yiXinProcessExcludeRuleData.action_C_to_I(e)));
        }
        return marketingTransferSyncUserList;
    }

    private List<MarketingTransferSyncUser> getMarketingTransferSyncUsers_B(int index) {
        List<MarketingTransferSyncUser> marketingTransferSyncUserList;
        while (true){
            marketingTransferSyncUserList = yiXinProcessGetBaseDataService.getMarketingTransferSyncUserList_B(index);
            index = marketingTransferSyncUserList.size()-1;
            if(marketingTransferSyncUserList.size()==0){
                break;
            }
            marketingTransferSyncUserList.removeIf((e -> yiXinProcessExcludeRuleData.action_B(e)));
        }
        return marketingTransferSyncUserList;
    }

    private List<MarketingTransferSyncUser> getMarketingTransferSyncUsers_A(int index) {
        List<MarketingTransferSyncUser> marketingTransferSyncUserList;
        while (true){
            marketingTransferSyncUserList = yiXinProcessGetBaseDataService.getMarketingTransferSyncUserList_A(index);
            index = marketingTransferSyncUserList.size()-1;
            if (marketingTransferSyncUserList.size()==0){
                break;
            }
            marketingTransferSyncUserList.removeIf((e -> yiXinProcessExcludeRuleData.action_A(e)));
        }
        return marketingTransferSyncUserList;
    }

}
