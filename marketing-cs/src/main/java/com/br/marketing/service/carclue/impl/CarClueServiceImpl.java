package com.br.marketing.service.carclue.impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.CarClueInfoMapper;
import com.br.marketing.service.carclue.CarClueService;
import com.br.marketing.service.carclue.callback.AbstractClueChannelCallBack;
import com.br.marketing.service.carclue.clueenums.CarClueDataStatusEnum;
import com.br.marketing.service.carclue.clueenums.CarClueMatchTypeEnum;
import com.br.marketing.service.carclue.common.ObjectCopyCommon;
import com.br.marketing.service.carclue.filter.AbstractClueChannelFilter;
import com.br.marketing.service.carclue.match.AbstractClueChannelMatch;
import com.br.marketing.service.carclue.push.AbstractClueChannelPush;
import com.br.marketing.service.carclue.strategy.ClueChannelConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
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
                                    List<CarClueSeriesInformation> carClueSeriesInfoList, List<CarClueRelationalMapping> carClueRelationalMappingList, List<CarChannelConfig> channelConfigList) throws Exception {
        List<Result<CarClueInfo>> resultList = new ArrayList<>();
        Iterator<CarChannelConfig> iterator = channelConfigList.iterator();
        StringBuffer filterError = new StringBuffer();
        StringBuffer matchError = new StringBuffer();
        while (iterator.hasNext()) {
            CarChannelConfig config = iterator.next();
            String configApicode = config.getApiCode();
            List<AbstractClueChannelFilter> channelFilterList = clueChannelConfigService.getChannelFilter(configApicode);
            CarClueInfo filterClueInfo = ObjectCopyCommon.deepCopyBean(carClueInfo, CarClueInfo.class);
            Result<CarClueInfo> result = isFilterHandler(filterClueInfo, configApicode, channelFilterList);
            //命中过滤规则，剔除渠道
            if (result.isSuccess()) {
                filterError.append(result.getData().getClueErrorReason());
                iterator.remove();
            }
        }
        //渠道全部被剔除
        if (CollectionUtils.isEmpty(channelConfigList)) {
            carClueInfo.setClueDataStatus(CarClueDataStatusEnum.INVALID_CLUE.getValue());
            carClueInfo.setClueErrorReason(filterError.toString());
            //更新线索状态
            carClueInfo.setCleanTime(new Date());
            carClueInfo.setUpdateTime(new Date());
            carClueInfoMapper.updateByPrimaryKeySelective(carClueInfo);
            return;
        }
        //线索匹配遍历
        for (CarChannelConfig config : channelConfigList) {
            String configApicode = config.getApiCode();
            //线索匹配实现
            AbstractClueChannelMatch channelMatch = clueChannelConfigService.getChannelMatchImpl(configApicode);
            CarClueInfo filterClueInfo = ObjectCopyCommon.deepCopyBean(carClueInfo, CarClueInfo.class);
            List<CarClueProvincesInformation> provincesInfoConfig = carClueProvincesInfoList.stream().filter(carClueProvinces ->
                    carClueProvinces.getApiCode().equals(configApicode)).collect(Collectors.toList());
            List<CarClueSeriesInformation> seriesInfoConfig = carClueSeriesInfoList.stream().filter(carClueSeriesInfo ->
                    carClueSeriesInfo.getApiCode().equals(configApicode)).collect(Collectors.toList());
            List<CarClueRelationalMapping> relationalMappingConfig = carClueRelationalMappingList.stream().filter(carClueRelationalMapping ->
                    carClueRelationalMapping.getApiCode().equals(configApicode)).collect(Collectors.toList());
            Result<CarClueInfo> matchResult = channelMatch.action(filterClueInfo, provincesInfoConfig, seriesInfoConfig, relationalMappingConfig);
            resultList.add(matchResult);

        }
        //线索匹配结果处理
        //精确匹配
        Result<CarClueInfo> completeResult = resultList.stream().filter(result -> result.isSuccess() && result.getData().getMatchBrandSeriesType()
                .equals(CarClueMatchTypeEnum.COMPLETE_MATCH.getValue())).findFirst().orElse(null);
        if (!Objects.isNull(completeResult)) {
            BeanUtils.copyProperties(completeResult.getData(), carClueInfo);
            carClueInfo.setCleanTime(new Date());
            carClueInfo.setUpdateTime(new Date());
            carClueInfoMapper.updateByPrimaryKeySelective(carClueInfo);
            return;
        }
        //模糊匹配
        Result<CarClueInfo> fuzzyResult = resultList.stream().filter(result -> result.isSuccess() && result.getData().getMatchBrandSeriesType()
                .equals(CarClueMatchTypeEnum.FUZZY_MATCH.getValue())).findFirst().orElse(null);
        if (!Objects.isNull(fuzzyResult)) {
            BeanUtils.copyProperties(fuzzyResult.getData(), carClueInfo);
            //更新线索状态
            carClueInfo.setCleanTime(new Date());
            carClueInfo.setUpdateTime(new Date());
            carClueInfoMapper.updateByPrimaryKeySelective(carClueInfo);
            return;
        }
        //异常线索
        resultList.forEach(result -> matchError.append(result.getData().getClueErrorReason()).append("|"));
        carClueInfo.setClueErrorReason(matchError.toString());
        carClueInfo.setClueDataStatus(resultList.get(0).getData().getClueDataStatus());
        carClueInfo.setCleanTime(new Date());
        carClueInfo.setUpdateTime(new Date());
        carClueInfoMapper.updateByPrimaryKeySelective(carClueInfo);
    }

    private Result<CarClueInfo> isFilterHandler(CarClueInfo carClueInfo, String apiCode, List<AbstractClueChannelFilter> channelFilterList) {

        for (AbstractClueChannelFilter clueChannelFilter : channelFilterList) {
            Result result = clueChannelFilter.filter(carClueInfo, apiCode);
            //命中过滤规则
            if (result.isSuccess()) {
                return result;
            }
        }
        return new Result().setCode(ResultCode.FAIL.getValue());

    }


}
