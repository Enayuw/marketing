package com.br.marketing.service.carclue.impl;

import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.CarClueInfoMapper;
import com.br.marketing.mapper.CarClueRelationalMappingMapper;
import com.br.marketing.mapper.ClueRelationshipMapper;
import com.br.marketing.service.carclue.CarClueService;
import com.br.marketing.service.carclue.callback.AbstractClueChannelCallBack;
import com.br.marketing.service.carclue.clueenums.CarClueDataStatusEnum;
import com.br.marketing.service.carclue.clueenums.CarClueMatchTypeEnum;
import com.br.marketing.service.carclue.clueenums.ChannelRule;
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

import javax.annotation.Resource;
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
    @Resource
    RedisChgService redisChgService;
    @Resource
    private CarClueRelationalMappingMapper carClueRelationalMappingMapper;
    @Resource
    private ClueRelationshipMapper clueRelationshipMapper;
    private static final String TITLE = "【车线索匹配】";

    @Override
    public void pushCarClueHandler(List<CarClueInfo> carClueInfoList, AbstractClueChannelPush channelPushImpl) {
        for (CarClueInfo carClueInfo : carClueInfoList) {
            channelPushImpl.push(carClueInfo,0);
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
                                    List<CarClueSeriesInformation> carClueSeriesInfoList, List<CarClueRelationalMapping> carClueRelationalMappingList,
                                    List<CarChannelConfig> channelConfigList) throws Exception {
        List<Result<CarClueInfo>> resultList = new ArrayList<>();
        Iterator<CarChannelConfig> iterator = channelConfigList.iterator();
        StringBuilder filterError = new StringBuilder();
        StringBuilder matchError = new StringBuilder();
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
            //匹配规则
            Result<CarClueInfo> matchResult = channelMatch.action(config,filterClueInfo, provincesInfoConfig, seriesInfoConfig,
                    relationalMappingConfig);
            resultList.add(matchResult);

        }
        //线索匹配结果处理
        List<Result<CarClueInfo>> completeResults = resultList.stream()
                .filter(result -> result.isSuccess() && result.getData().getMatchBrandSeriesType()
                        .equals(CarClueMatchTypeEnum.COMPLETE_MATCH.getValue()))
                .collect(Collectors.toList());

        for (Result<CarClueInfo> completeResult : completeResults){
            //线索匹配实现
            CarClueInfo data = completeResult.getData();
            //是否限量：true-限量
            if(!checkDailyLimited(data)){
                BeanUtils.copyProperties(data, carClueInfo);
                carClueInfo.setCleanTime(new Date());
                carClueInfo.setUpdateTime(new Date());
                carClueInfoMapper.updateByPrimaryKeySelective(carClueInfo);
                return;
            }
        }

        //模糊匹配
        List<Result<CarClueInfo>> fuzzyResults = resultList.stream()
                .filter(result -> result.isSuccess() && result.getData().getMatchBrandSeriesType()
                        .equals(CarClueMatchTypeEnum.FUZZY_MATCH.getValue()))
                .collect(Collectors.toList());

        for (Result<CarClueInfo> fuzzyResult : fuzzyResults){
            //线索匹配实现
            CarClueInfo data = fuzzyResult.getData();
            //是否限量：true-限量
            if(!checkDailyLimited(data)){
                BeanUtils.copyProperties(data, carClueInfo);
                carClueInfo.setCleanTime(new Date());
                carClueInfo.setUpdateTime(new Date());
                carClueInfoMapper.updateByPrimaryKeySelective(carClueInfo);
                return;
            }
        }
        //异常线索
        //判断线索状态是 已限量状态
        if(!CarClueDataStatusEnum.LIMITED_LACK_CLUE.getValue().equals(carClueInfo.getClueDataStatus())){
            //全部渠道线索均为 有效线索(外采缺失)，则线索状态为 有效线索(外采缺失)
            Result<CarClueInfo> abnormalResult = resultList.stream().filter(result -> result.getData().getClueDataStatus().equals
                    (CarClueDataStatusEnum.ABNORMAL_CLUE.getValue())).findFirst().orElse(null);
            if (!Objects.isNull(abnormalResult)) {
                carClueInfo.setClueDataStatus(CarClueDataStatusEnum.ABNORMAL_CLUE.getValue());
            } else {
                carClueInfo.setClueDataStatus(resultList.get(0).getData().getClueDataStatus());
            }
        }
        resultList.forEach(result -> matchError.append(result.getData().getClueErrorReason()).append("|"));
        carClueInfo.setClueErrorReason(matchError.toString().substring(0, matchError.length() - 1));
        carClueInfo.setCleanTime(new Date());
        carClueInfo.setUpdateTime(new Date());
        carClueInfoMapper.updateByPrimaryKeySelective(carClueInfo);
    }

    private Boolean checkDailyLimited(CarClueInfo carClueInfo) {
        Boolean isLimited = Boolean.FALSE;
        AbstractClueChannelMatch channelMatch = clueChannelConfigService.getChannelMatchImpl(carClueInfo.getCluePushChannel());
        if(ChannelRule.MatchChannelRuleEnum.DAILY_LIMITED.getLabel().equals(channelMatch.label())){

            ClueRelationshipExample clueRelationshipExample = new ClueRelationshipExample();
            clueRelationshipExample.createCriteria().andClueInfoIdEqualTo(carClueInfo.getId()).andApiCodeEqualTo(carClueInfo.getApiCode());
            List<ClueRelationship> clueRelationships = clueRelationshipMapper.selectByExample(clueRelationshipExample);
            if(CollectionUtils.isEmpty(clueRelationships)){
                log.warn("未匹配到线索-外采映射关系");
                return Boolean.TRUE;
            }
            ClueRelationship clueRelationship = clueRelationships.get(0);
            String key = RedisKeyConstant.UPDATE_DAILY_LIMITED.concat(":").concat(String.valueOf(clueRelationship.getMappingId()));
            String lockValue = UUID.randomUUID().toString();
            try {
                redisChgService.lock(key, lockValue);
                CarClueRelationalMapping carClueRelationalMapping = carClueRelationalMappingMapper.selectByPrimaryKey(clueRelationship.getMappingId());
                Integer dailyLimited = carClueRelationalMapping.getDailyLimited();
                Integer matchDailyLimited = carClueRelationalMapping.getMatchDailyLimited();
                //已限量
                if(0 == dailyLimited || matchDailyLimited >= dailyLimited){
                    carClueInfo.setCluePushStatus(CarClueDataStatusEnum.LIMITED_LACK_CLUE.getValue());
                    isLimited = Boolean.TRUE;
                }else {
                    //增加推送次数
                    CarClueRelationalMapping carClueRelational = new CarClueRelationalMapping();
                    carClueRelational.setId(carClueRelationalMapping.getId());
                    carClueRelational.setMatchDailyLimited(carClueRelationalMapping.getMatchDailyLimited() + 1);
                    carClueRelationalMappingMapper.updateByPrimaryKeySelective(carClueRelational);
                }
                //释放锁
                try {
                    redisChgService.unlock(key, lockValue);
                } catch (Exception e) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.PP_MARKING_SERVICEERROR.getCode(),
                            TITLE + "释放锁出现异常，" + "errorMessage=" + e.getMessage()), e);
                    //回滚推送次数
                    if(!isLimited){
                        CarClueRelationalMapping carClueRelational = new CarClueRelationalMapping();
                        carClueRelational.setId(carClueRelationalMapping.getId());
                        carClueRelational.setMatchDailyLimited(carClueRelationalMapping.getMatchDailyLimited() - 1);
                        carClueRelationalMappingMapper.updateByPrimaryKeySelective(carClueRelational);
                    }
                    redisChgService.unlock(key, lockValue);
                }

            }catch (Exception e){
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.PP_MARKING_SERVICEERROR.getCode(),
                        TITLE + "抢锁出现异常，" + "errorMessage=" + e.getMessage()), e);
                redisChgService.unlock(key, lockValue);
            }
        }
        return isLimited;
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
