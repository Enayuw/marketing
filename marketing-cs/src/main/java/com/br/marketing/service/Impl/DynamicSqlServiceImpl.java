package com.br.marketing.service.Impl;

import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.service.IDynamicSqlService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
public class DynamicSqlServiceImpl implements IDynamicSqlService {

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Resource
    MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Override
    public Integer countByRuleScoreWithDate(String apiCode, String whereStr) {

        HashMap<String, Integer> sqlType = marketingCommonConfig.getSqlType();
        Integer type = sqlType==null?0:sqlType.getOrDefault("buildTaskNum", 0);
        Integer count = 0;
        long start = System.currentTimeMillis();
        if (type.equals(1)) {
            count = marketingSyncInfoMapper.countByRuleScoreWithDatetiflash_(apiCode, whereStr);
            if(log.isWarnEnabled()){
                log.warn(String.format("执行buildTaskNum-tiflash耗时:【%d】",System.currentTimeMillis()-start));
            }
        } else {
            count = marketingSyncInfoMapper.countByRuleScoreWithDate(apiCode, whereStr);
            if(log.isWarnEnabled()){
                log.warn(String.format("执行buildTaskNum-tikv耗时:【%d】",System.currentTimeMillis()-start));
            }
        }
        return count;
    }

    @Override
    public Long minIdRuleScoreWithDate(String apiCode, String whereStr) {
        HashMap<String, Integer> sqlType = marketingCommonConfig.getSqlType();
        Integer type = sqlType==null?0:sqlType.getOrDefault("scoreMinId", 0);
        Long mid = 0L;
        long start = System.currentTimeMillis();
        if (type.equals(1)) {
            mid = marketingSyncInfoMapper.minIdRuleScoreWithDatetiflash_(apiCode, whereStr);
            if(log.isWarnEnabled()){
                log.warn(String.format("执行scoreMinId-tiflash耗时:【%d】",System.currentTimeMillis()-start));
            }
        } else {
            mid = marketingSyncInfoMapper.minIdRuleScoreWithDate(apiCode, whereStr);
            if(log.isWarnEnabled()){
                log.warn(String.format("执行scoreMinId-tikv耗时:【%d】",System.currentTimeMillis()-start));
            }
        }
        return mid;
    }

    @Override
    public List<MarketingSyncUser> selectDataRuleScoreWithDate(String apiCode, String whereStr, Long id,Integer pageSize) {
        HashMap<String, Integer> sqlType = marketingCommonConfig.getSqlType();
        Integer type = sqlType==null?0:sqlType.getOrDefault("scoreData", 0);
        List<MarketingSyncUser> users = new ArrayList<>();
        long start = System.currentTimeMillis();
        if (type.equals(1)) {
            users = marketingSyncInfoMapper.selectDataRuleScoreWithDatetiflash_(apiCode, whereStr,id,pageSize);
            if(log.isWarnEnabled()){
                log.warn(String.format("执行scoreData-tiflash耗时:【%d】",System.currentTimeMillis()-start));
            }
        } else {
            users = marketingSyncInfoMapper.selectDataRuleScoreWithDate(apiCode, whereStr,id,pageSize);
            if(log.isWarnEnabled()){
                log.warn(String.format("执行scoreData-tikv耗时:【%d】",System.currentTimeMillis()-start));
            }
        }
        return users;
    }
}
