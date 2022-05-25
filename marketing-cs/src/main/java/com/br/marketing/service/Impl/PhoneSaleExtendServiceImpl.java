package com.br.marketing.service.Impl;

import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PhoneSaleExtendServiceImpl {

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    public boolean haluoSaleJudge(List<PhoneSaleExtendInfo> phoneSales,String dataStatus){
        phoneSales.sort((t,t1)->{return t.getAppletDate().compareTo(t1.getAppletDate());});
        Integer taskTimeDays = 35;
        Integer abcTimeDays = 5;
        Integer dTimeDays = 4;
        HashSet status = new HashSet();
        status.add("a");
        status.add("b");
        status.add("d");
        HashMap<String, String> haluoTransferRule = marketingCommonConfig.getHaluoTransferRule();
        if(haluoTransferRule != null){
            taskTimeDays = Integer.valueOf(haluoTransferRule.getOrDefault("taskIddate", "35"));
            abcTimeDays = Integer.valueOf(haluoTransferRule.getOrDefault("ABCdate", "5"));
            dTimeDays = Integer.valueOf(haluoTransferRule.getOrDefault("dtimes", "4"));
            String statusStr = haluoTransferRule.getOrDefault("status", "a,b,d");
            status = new HashSet<>(Arrays.asList(statusStr.split(",")));
        }
        if(dataStatus.equals("d")){

        }
    }
}
