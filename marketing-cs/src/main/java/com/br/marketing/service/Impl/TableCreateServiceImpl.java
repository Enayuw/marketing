package com.br.marketing.service.Impl;


import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.entity.MarketingCustomerExample;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.mapper.MarketingUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;

@Service
public class TableCreateServiceImpl {
    private static HashSet tableNameSet;

    final static String marketingPreUserTable = "b_marketing_sync_";

    final static String marketingUserTable = "b_marketing_user_";

    final static String marketingTransferUserTable = "b_marketing_transfer_sync_";

    @PostConstruct
    void init(){
        tableNameSet = new HashSet<String>();
    }

    @Autowired
    MarketingUserMapper marketingUserMapper;

    @Autowired
    MarketingCustomerMapper marketingCustomerMapper;

    /**
     * 根据apiCode查询tcid
     * @param apiCode
     * @return
     */
    public String getTcId(String apiCode) {
        MarketingCustomerExample customerExample = new MarketingCustomerExample();
        customerExample.createCriteria().andApiCodeEqualTo(apiCode).andStatusEqualTo(Byte.valueOf("1"));
        List<MarketingCustomer> marketingCustomers = marketingCustomerMapper.selectByExample(customerExample);
        if (marketingCustomers.size() == 0) {
            return null;
        }
        String s1 = marketingCustomers.get(0).getCid().replaceFirst("-", "");
        return s1;
    }

    /**
     * 根据apiCode查询cid
     *
     * @author Guo Zeqiang
     * @dateTime 2022/2/16 17:42
     */
    public String getCId(String apiCode) {
        MarketingCustomerExample customerExample = new MarketingCustomerExample();
        customerExample.createCriteria().andApiCodeEqualTo(apiCode).andStatusEqualTo(Byte.valueOf("1"));
        List<MarketingCustomer> marketingCustomers = marketingCustomerMapper.selectByExample(customerExample);
        if (marketingCustomers.size() == 0) {
            return null;
        }
        return marketingCustomers.get(0).getCid();
    }

    public void createMarketingSyncUserTable(String apiCode) {
        String tableName = marketingPreUserTable.concat(apiCode);
        if (!tableNameSet.contains(tableName)) {
            marketingUserMapper.createMarketingPreUserTable(tableName);
            tableNameSet.add(tableName);
        }
    }

    public void createMarketingTransferUserTable(String cid){
        String tableName = marketingTransferUserTable.concat(cid);
        if(!tableNameSet.contains(tableName)){
            marketingUserMapper.createMarketingTransferUserTable(tableName);
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
