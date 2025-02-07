package com.br.marketing.service.carclue.impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.CarClueInfoMapper;
import com.br.marketing.service.carclue.CarClueService;
import com.br.marketing.service.carclue.callback.AbstractClueChannelCallBack;
import com.br.marketing.service.carclue.clueenums.CarClueMatchTypeEnum;
import com.br.marketing.service.carclue.filter.AbstractClueChannelFilter;
import com.br.marketing.service.carclue.match.AbstractClueChannelMatch;
import com.br.marketing.service.carclue.push.AbstractClueChannelPush;
import com.br.marketing.service.carclue.strategy.ClueChannelConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
    public void pushCarClueHandler(List<CarClueInfo> carClueInfoList, AbstractClueChannelPush channelPushImpl) {
        for (CarClueInfo carClueInfo : carClueInfoList) {
            channelPushImpl.push(carClueInfo);
        }
    }

    @Override
    public void carClueCallBackHandler(List<CarClueInfo> carClueInfoList, AbstractClueChannelCallBack channelCallBackImpl) {
        for (CarClueInfo carClueInfo : carClueInfoList) {
            channelCallBackImpl.callback(carClueInfo);
        }
    }

    @Override
    public void carClueCleanHandler(CarClueInfo carClueInfo, List<CarClueProvincesInformation> carClueProvincesInfoList,
                                    List<CarClueSeriesInformation> carClueSeriesInfoList, List<CarClueRelationalMapping> carClueRelationalMappingList, List<CarChannelConfig> channelConfigList) {
        for (CarChannelConfig config : channelConfigList) {
            String configApicode = config.getApiCode();
            List<AbstractClueChannelFilter> channelFilterList = clueChannelConfigService.getChannelFilter(configApicode);
            //命中过滤规则，进入下次循环
            if (isFilterHandler(carClueInfo, configApicode, channelFilterList)) {
                continue;
            }
            //线索匹配实现
            AbstractClueChannelMatch channelMatch = clueChannelConfigService.getChannelMatchImpl(configApicode);

            List<CarClueProvincesInformation> provincesInfoConfig = carClueProvincesInfoList.stream().filter(carClueProvinces ->
                    carClueProvinces.getApiCode().equals(configApicode)).collect(Collectors.toList());
            List<CarClueSeriesInformation> seriesInfoConfig = carClueSeriesInfoList.stream().filter(carClueSeriesInfo ->
                    carClueSeriesInfo.getApiCode().equals(configApicode)).collect(Collectors.toList());
            List<CarClueRelationalMapping> relationalMappingConfig = carClueRelationalMappingList.stream().filter(carClueRelationalMapping ->
                    carClueRelationalMapping.getApiCode().equals(configApicode)).collect(Collectors.toList());
            Result matchResult = channelMatch.action(carClueInfo, provincesInfoConfig, seriesInfoConfig, relationalMappingConfig);
            if (matchResult.isSuccess() && carClueInfo.getMatchBrandSeriesType().equals(CarClueMatchTypeEnum.COMPLETE_MATCH.getValue())) {
                break;
            }
        }
        //更新线索状态
        carClueInfo.setCleanTime(new Date());
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
