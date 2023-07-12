package com.br.marketing.service.Impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.entity.MarketingDataValidConfig;
import com.br.marketing.entity.MarketingDataValidConfigExample;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.mapper.MarketingDataValidConfigMapper;
import com.br.marketing.service.IMarketingDataValidService;
import com.br.marketing.service.IMarketingSyncUserService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MarketingDataValidServiceImpl implements IMarketingDataValidService {

    @Resource
    MarketingDataValidConfigMapper marketingDataValidConfigMapper;

    @Resource
    private IMarketingSyncUserService marketingSyncUserService;

    @Override
    public Result<List<MarketingDataValidConfig>> getDataValidConfigByType(String apiCode, Integer validType) {

        MarketingDataValidConfigExample configExample = new MarketingDataValidConfigExample();
        configExample.createCriteria()
                .andValidTypeEqualTo(validType)
                .andIsDelEqualTo(Constants.DATA_VALID)
                .andApiCodeEqualTo(apiCode);

        List<MarketingDataValidConfig> marketingDataValidConfigs = marketingDataValidConfigMapper.selectByExample(configExample);
        if (marketingDataValidConfigs.size() >= 0) {
            return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(marketingDataValidConfigs);
        }
        return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("未获取指定类型的有效期配置");
    }

    @Override
    public Boolean isValidByThreeType(Map<String, Integer> userTypeTN, MarketingSyncUser syncUser) {
        Integer day = userTypeTN.get(syncUser.getUserType());
        if (day == null) {
            return Boolean.FALSE;
        }
        Boolean periodOfValidity = marketingSyncUserService.isPeriodOfValidity(new Date(), day, syncUser.getAppletTime());
        if (periodOfValidity) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    @Override
    public Map<String, MarketingDataValidConfig> getDataValidConfig(String apiCode) {
        MarketingDataValidConfigExample configExample = new MarketingDataValidConfigExample();
        configExample.createCriteria()
                .andIsDelEqualTo(Constants.DATA_VALID)
                .andApiCodeEqualTo(apiCode);
        List<MarketingDataValidConfig> list = marketingDataValidConfigMapper.selectByExample(configExample);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        } else {
            return list.stream().collect(
                    Collectors.toConcurrentMap(l -> l.getUserType() + l.getAppletDate()
                            , Function.identity()
                            , BinaryOperator.maxBy(Comparator.comparing(MarketingDataValidConfig::getCreateTime))));
        }
    }

    @Override
    public boolean isNotValid(MarketingDataValidConfig validConfig, MarketingSyncUser syncUser) {
        return !isValid(validConfig, syncUser);
    }

    @Override
    public boolean isValid(MarketingDataValidConfig validConfig, MarketingSyncUser syncUser) {
        String validStartDate = validConfig.getValidStartDate();
        String validEndDate = validConfig.getValidEndDate();
        LocalDate startDate = LocalDate.parse(validStartDate, DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate endDate = LocalDate.parse(validEndDate, DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate localDate = syncUser.getAppletTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        // 比较是否在范围内
        return ((startDate.isBefore(localDate) || startDate.isEqual(localDate))
                && (localDate.isBefore(endDate) || localDate.isEqual(endDate)));
    }
}
