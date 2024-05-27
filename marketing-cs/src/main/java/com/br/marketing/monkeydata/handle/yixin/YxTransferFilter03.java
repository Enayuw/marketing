package com.br.marketing.monkeydata.handle.yixin;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingTransferSyncUser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class YxTransferFilter03 implements YxTransferFilter<MarketingTransferSyncUser>{

    @Override
    public List<MarketingTransferSyncUser> filter(List<MarketingTransferSyncUser> list) {
        List<MarketingTransferSyncUser> filteredList = new ArrayList<>();
        Iterator<MarketingTransferSyncUser> iterator = list.iterator();
        while(iterator.hasNext()){
            MarketingTransferSyncUser next = iterator.next();
            String reserveField1 = next.getReserveField1();
            if(!StringUtils.isEmpty(reserveField1)){
                continue;
            }
            JSONObject jo = JSONObject.parseObject(reserveField1);
            String applyLoan = jo.getString("applyLoan");
            if(!"1".equals(applyLoan)){
                continue;
            }
            String ifLent = next.getIfLent();
            if(!"0".equals(ifLent)){
                continue;
            }
            filteredList.add(next);
            iterator.remove();
        }
        return filteredList;
    }
}
