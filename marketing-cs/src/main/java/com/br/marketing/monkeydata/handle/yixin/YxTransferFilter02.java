package com.br.marketing.monkeydata.handle.yixin;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.entity.MarketingTransferSyncUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class YxTransferFilter02 implements YxTransferFilter<MarketingTransferSyncUser>{

    private final static String TITLE = "【宜信转化过滤推送百应】-YxTransferFilter02-";

    @Override
    public List<MarketingTransferSyncUser> filter(List<MarketingTransferSyncUser> list) {

        if(list == null){
            return new ArrayList<>();
        }

        List<MarketingTransferSyncUser> filteredList = new ArrayList<>();
        Iterator<MarketingTransferSyncUser> iterator = list.iterator();
        while(iterator.hasNext()){
            MarketingTransferSyncUser next = iterator.next();
            try {
                if("1".equals(next.getIfApply()) && "2".equals(next.getApplyResult())){
                    filteredList.add(next);
                    iterator.remove();
                }
            } catch (Exception e) {
                log.error(TITLE + "filter error, {}", JSONObject.toJSONString(next.getCustNum()));
            }
        }

        Set<String> custNumSets = filteredList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
        log.warn(TITLE + "filteredSet: {}", JSONObject.toJSONString(custNumSets));
        return filteredList;
    }
}
