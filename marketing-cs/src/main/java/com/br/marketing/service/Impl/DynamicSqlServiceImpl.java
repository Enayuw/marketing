package com.br.marketing.service.Impl;

import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.service.IDynamicSqlService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@Service
public class DynamicSqlServiceImpl implements IDynamicSqlService {

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Resource
    MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Override
    public Integer countByRuleScoreWithDate(String apiCode, String whereStr) {

        HashMap<String, Integer> sqlType = marketingCommonConfig.getSqlType();
        Integer type = sqlType==null?0:sqlType.getOrDefault("buildTaskNum", 0);
        if (type.equals(1)) {
            return marketingSyncInfoMapper.countByRuleScoreWithDatetiflash_(apiCode, whereStr);
        } else {
            return marketingSyncInfoMapper.countByRuleScoreWithDate(apiCode, whereStr);
        }
    }

    @Override
    public Long minIdRuleScoreWithDate(String apiCode, String whereStr) {
        HashMap<String, Integer> sqlType = marketingCommonConfig.getSqlType();
        Integer type = sqlType==null?0:sqlType.getOrDefault("scoreMinId", 0);
        if (type.equals(1)) {
            return marketingSyncInfoMapper.minIdRuleScoreWithDatetiflash_(apiCode, whereStr);
        } else {
            return marketingSyncInfoMapper.minIdRuleScoreWithDate(apiCode, whereStr);
        }
    }

    @Override
    public List<MarketingSyncUser> selectDataRuleScoreWithDate(String apiCode, String whereStr, Long id,Integer pageSize) {
        HashMap<String, Integer> sqlType = marketingCommonConfig.getSqlType();
        Integer type = sqlType==null?0:sqlType.getOrDefault("scoreData", 0);
        if (type.equals(1)) {
            return marketingSyncInfoMapper.selectDataRuleScoreWithDatetiflash_(apiCode, whereStr,id,pageSize);
        } else {
            return marketingSyncInfoMapper.selectDataRuleScoreWithDate(apiCode, whereStr,id,pageSize);
        }
    }
}
