package com.br.marketing.service.Impl;

import com.br.marketing.mapper.MarketingUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashSet;

@Service
public class TableCreateServiceImpl {
    private static HashSet tableNameSet;

    final static String marketingPreUserTable = "b_marketing_sync_";

    final static String marketingUserTable = "b_marketing_user_";

    @PostConstruct
    void init(){
        tableNameSet = new HashSet<String>();
    }

    @Autowired
    MarketingUserMapper marketingUserMapper;

    public void createMarketingSyncUserTable(String apiCode){
        String tableName = marketingPreUserTable.concat(apiCode);
        if(!tableNameSet.contains(tableName)){
            marketingUserMapper.createMarketingPreUserTable(tableName);
            tableNameSet.add(tableName);
        }
    }

    public void createMarketingUserTable(String apiCode){
        String tableName = marketingUserTable.concat(apiCode);
        if(!tableNameSet.contains(tableName)){
            marketingUserMapper.createUserTable(tableName);
            tableNameSet.add(tableName);
        }
    }
}
