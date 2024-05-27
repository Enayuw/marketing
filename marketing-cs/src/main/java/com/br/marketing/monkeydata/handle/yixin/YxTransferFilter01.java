package com.br.marketing.monkeydata.handle.yixin;

import com.br.marketing.entity.MarketingTransferSyncUser;

import java.util.*;

public class YxTransferFilter01 implements YxTransferFilter<MarketingTransferSyncUser>{

    @Override
    public List<MarketingTransferSyncUser> filter(List<MarketingTransferSyncUser> list) {

        if(list == null){
            return new ArrayList<>();
        }

        List<MarketingTransferSyncUser> filteredList = new ArrayList<>();
        for (MarketingTransferSyncUser marketingTransferSyncUser : list) {
            if("1".equals(marketingTransferSyncUser.getIfApply()) && "0".equals(marketingTransferSyncUser.getApplyResult())){
                filteredList.add(marketingTransferSyncUser);
            }
        }
        return filteredList;
    }
}
