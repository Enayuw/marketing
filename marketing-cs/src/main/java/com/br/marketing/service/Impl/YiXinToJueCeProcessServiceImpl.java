package com.br.marketing.service.Impl;

import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.service.Impl.YiXin.YiXinProcessExcludeRuleData;
import com.br.marketing.service.Impl.YiXin.YiXinProcessGetBaseDataService;
import com.br.marketing.service.YiXinToJueCeProcessService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Case;
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
    private static List<String> actonTypeList = new ArrayList<String>(){
        {
            add("A");
            add("B");
            add("C");
            add("D");
            add("E");
            add("F");
            add("G");
            add("H");
            add("I");
        }
    };
    @Resource
    private YiXinProcessGetBaseDataService yiXinProcessGetBaseDataService;

    @Resource
    private YiXinProcessExcludeRuleData yiXinProcessExcludeRuleData;
    @Override
    public void doProcess(String actionType) {
    //Todo 1. 查询 a 情况基础数据
    //     2. 数据剔除
    //     3. 数据组装推送
        List<MarketingTransferSyncUser> marketingTransferSyncUserList;
        switch (actionType){
            case "A":
                marketingTransferSyncUserList = yiXinProcessGetBaseDataService.getMarketingTransferSyncUserList_A();
                marketingTransferSyncUserList.removeIf((e -> yiXinProcessExcludeRuleData.action_A(e)));
                break;
            case "B":
                marketingTransferSyncUserList = yiXinProcessGetBaseDataService.getMarketingTransferSyncUserList_B();
                marketingTransferSyncUserList.removeIf((e -> yiXinProcessExcludeRuleData.action_B(e)));
                break;
            default:
                marketingTransferSyncUserList = yiXinProcessGetBaseDataService.getMarketingTransferSyncUserList_C_to_I(actionType);
                marketingTransferSyncUserList.removeIf((e -> yiXinProcessExcludeRuleData.action_C_to_I(e)));
                break;

        }
        if(marketingTransferSyncUserList!=null && marketingTransferSyncUserList.size()>0){
            // 调用推送决策接口
        }

    }

}
