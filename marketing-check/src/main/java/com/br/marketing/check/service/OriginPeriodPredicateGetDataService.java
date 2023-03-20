package com.br.marketing.check.service;

import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author GuangChao.Zhang
 * @version 1.0
 * @date 2023/3/20 20:16
 */
public interface OriginPeriodPredicateGetDataService {

    List<MarketingTransferSyncUser> getJuZiRuleData(String status,String tcid,Long minId);

    @Service
    @Slf4j
     class OriginPeriodPredicateGetARuleDataServiceImpl implements OriginPeriodPredicateGetDataService {
        @Resource
        private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

        @Override
        public List<MarketingTransferSyncUser> getJuZiRuleData(String status,String tcid, Long minId) {
            if("a".equals(status)){
                return  marketingTransferSyncUserMapper.getJuZiARuleData(tcid, minId);
            }
            return new ArrayList<>();
        }
    }


    @Service
    @Slf4j
     class OriginPeriodPredicateGetBRuleDataServiceImpl implements OriginPeriodPredicateGetDataService {
        @Resource
        private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

        @Override
        public List<MarketingTransferSyncUser> getJuZiRuleData(String status,String tcid, Long minId) {
            if("b".equals(status)){
                return  marketingTransferSyncUserMapper.getJuZiBRuleData(tcid, minId);
            }
            return new ArrayList<>();
        }
    }

    @Service
    @Slf4j
     class OriginPeriodPredicateGetCRuleDataServiceImpl implements OriginPeriodPredicateGetDataService {
        @Resource
        private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

        @Override
        public List<MarketingTransferSyncUser> getJuZiRuleData(String status,String tcid, Long minId) {
            if("c".equals(status)){
                return  marketingTransferSyncUserMapper.getJuZiCRuleData(tcid, minId);
            }
            return new ArrayList<>();
        }
    }

    @Service
    @Slf4j
     class OriginPeriodPredicateGetDRuleDataServiceImpl implements OriginPeriodPredicateGetDataService {
        @Resource
        private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

        @Override
        public List<MarketingTransferSyncUser> getJuZiRuleData(String status ,String tcid, Long minId) {
            if("d".equals(status)){
                return marketingTransferSyncUserMapper.getJuZiDRuleData(tcid, minId);
            }
            return new ArrayList<>();
        }
    }
}
