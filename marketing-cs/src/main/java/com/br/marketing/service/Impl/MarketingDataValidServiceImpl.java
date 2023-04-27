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

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    public Result isValidByThreeType(Map<String, Integer> userTypeTN, MarketingSyncUser syncUser) {
        Integer day = userTypeTN.get(syncUser.getUserType());
        if(day == null){
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("该场景未配置T+N有效期");
        }
        Boolean periodOfValidity = marketingSyncUserService.isPeriodOfValidity(new Date(), day, syncUser.getAppletTime());
        if(periodOfValidity){
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }else{
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("该数据已失效");
        }
    }
}
