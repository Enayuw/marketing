package com.br.marketing.service.Impl.carclue;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.CarChannelConfig;
import com.br.marketing.entity.CarClueInfo;
import com.br.marketing.mapper.CarClueInfoMapper;
import com.br.marketing.service.carclue.filter.AbstractClueChannelFilter;
import com.br.marketing.service.carclue.match.AbstractClueChannelMatch;
import com.br.marketing.service.carclue.strategy.ClueChannelConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 车线索service
 *
 * @author zhen.Li1
 * @date 2025-01-15 16:58
 */
@Service
@Slf4j
public class CarClueServiceImpl implements CarClueService {

    @Autowired
    private ClueChannelConfigService clueChannelConfigService;

    @Autowired
    private CarClueInfoMapper carClueInfoMapper;


    @Override
    public void carClueCleanHandler(CarClueInfo carClueInfo, Object brandCitycConfig, List<CarChannelConfig> channelConfigList) {

        for (CarChannelConfig config : channelConfigList) {
            List<AbstractClueChannelFilter> channelFilterList = clueChannelConfigService.getChannelFilter(config.getApiCode());
            //命中过滤规则，进入下次循环
            if (isFilterHandler(carClueInfo, config.getApiCode(), channelFilterList)) {
                continue;
            }
            //线索匹配实现
            AbstractClueChannelMatch channelMatch = clueChannelConfigService.getChannelMatchImpl(config.getApiCode());
            Result matchResult = channelMatch.action(carClueInfo);
            if (matchResult.isSuccess()) {
                break;
            }
        }
        //更新线索状态
        carClueInfoMapper.updateByPrimaryKeySelective(carClueInfo);

    }

    private Boolean isFilterHandler(CarClueInfo carClueInfo, String apiCode, List<AbstractClueChannelFilter> channelFilterList) {

        for (AbstractClueChannelFilter clueChannelFilter : channelFilterList) {
            Result result = clueChannelFilter.filter(carClueInfo, apiCode);
            //命中过滤规则
            if (result.isSuccess()) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;

    }


}
