package com.br.marketing.monkeydata.handle.yixin;

import com.br.marketing.entity.MarketingTransferSyncUser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class YxTransferFilter02 implements YxTransferFilter<MarketingTransferSyncUser>{

    @Override
    public List<MarketingTransferSyncUser> filter(List<MarketingTransferSyncUser> list) {

        if(list == null){
            return new ArrayList<>();
        }

        List<MarketingTransferSyncUser> filteredList = new ArrayList<>();
        Iterator<MarketingTransferSyncUser> iterator = list.iterator();
        while(iterator.hasNext()){
            MarketingTransferSyncUser next = iterator.next();
            if("1".equals(next.getIfApply()) && "2".equals(next.getApplyResult())){
                filteredList.add(next);
                iterator.remove();
            }
        }
        return filteredList;
    }
}
