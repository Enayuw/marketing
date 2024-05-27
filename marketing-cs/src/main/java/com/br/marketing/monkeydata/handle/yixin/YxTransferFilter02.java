package com.br.marketing.monkeydata.handle.yixin;

import com.br.marketing.entity.MarketingTransferSyncUser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class YxTransferFilter02 implements YxTransferFilter<MarketingTransferSyncUser>{

    @Override
    public List<MarketingTransferSyncUser> filter(List<MarketingTransferSyncUser> list) {

        if(list == null){
            return new ArrayList<>();
        }

        List<MarketingTransferSyncUser> filteredList = list.stream()
                .filter(marketingTransferSyncUser -> "1".equals(marketingTransferSyncUser.getIfApply())
                        && "2".equals(marketingTransferSyncUser.getApplyResult()))
                .collect(Collectors.toList());

        return filteredList;
    }
}
