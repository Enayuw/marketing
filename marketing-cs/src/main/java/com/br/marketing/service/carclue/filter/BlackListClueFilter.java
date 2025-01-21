package com.br.marketing.service.carclue.filter;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.CarClueInfo;
import com.br.marketing.service.carclue.clueenums.ChannelRule;
import com.br.marketing.service.carclue.common.MatchPatternCommon;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class BlackListClueFilter extends AbstractClueChannelFilter {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    Result<String> action(CarClueInfo carClueInfo, String apiCode) {
        String brand = carClueInfo.getBrand();
        String series = carClueInfo.getSeries();
        Map<String, List<String>> blackListConfig = marketingCommonConfig.getCarClueBlackListConfig();
        List<String> blackListData = blackListConfig.get(apiCode);
        //黑名单为空
        if (CollectionUtils.isEmpty(blackListData)) {
            return new Result().setCode(ResultCode.FAIL.getValue());
        }
        //品牌命中黑名单
        if (MatchPatternCommon.fuzzyMatch(brand, blackListData)) {
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }
        //车系命中黑名单
        if (MatchPatternCommon.fuzzyMatch(series, blackListData)) {
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }

        return new Result().setCode(ResultCode.FAIL.getValue());
    }

    @Override
    public String label() {
        return ChannelRule.FilterChannelRuleEnum.BLACK_LIST_FILTER.getLabel();
    }
}
