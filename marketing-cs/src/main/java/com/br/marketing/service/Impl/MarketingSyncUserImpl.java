package com.br.marketing.service.Impl;

import com.br.marketing.dos.PeriodOfValidityDO;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.vo.TodayIdTimeBySoleVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class MarketingSyncUserImpl implements IMarketingSyncUserService {

    @Autowired
    MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Override
    public Long countRepeat(String execSql) {
        return marketingSyncInfoMapper.countRepeat(execSql);
    }

    @Override
    public TodayIdTimeBySoleVo getSoleValidUser(String execSql) {
        return marketingSyncInfoMapper.getSoleValidUser(execSql);
    }


    @Override
    public Integer updateRepeatUserStatus(String execSql) {
        return marketingSyncInfoMapper.updateRepeatUserStatus(execSql);
    }

    @Override
    public Boolean isPeriodOfValidity(String apiCode, String custNum, PeriodOfValidityDO periodOfValidityDO) {
        try {
            long count = marketingSyncInfoMapper.getPeriodOfValiditySum(apiCode, custNum, periodOfValidityDO);
            return count > 0;
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public String getUserTypeLatestByCustNum(String apiCode, String custNum) {
        return marketingSyncInfoMapper.getUserTypeLatestByCustNum(apiCode, custNum);
    }

    @Override
    public String getTaskIdLatestByCustNum(String apiCode, String custNum) {
        return marketingSyncInfoMapper.getTaskIdLatestByCustNum(apiCode, custNum);
    }

    @Override
    public Date getAppletTimeByCustNumAndUserType(String apiCode, String custNum, String userType) {
        return marketingSyncInfoMapper.getAppletTimeByCustNumAndUserType(apiCode, custNum, userType);
    }
}
