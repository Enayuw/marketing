package com.br.marketing.service.carclue.strategy.impl;

import com.alibaba.fastjson.JSON;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.entity.CarChannelConfig;
import com.br.marketing.entity.CarChannelConfigExample;
import com.br.marketing.mapper.CarChannelConfigMapper;
import com.br.marketing.service.carclue.callback.AbstractClueChannelCallBack;
import com.br.marketing.service.carclue.clueenums.ChannelConfigTypeEnum;
import com.br.marketing.service.carclue.config.AbstractClueChannelConfig;
import com.br.marketing.service.carclue.filter.AbstractClueChannelFilter;
import com.br.marketing.service.carclue.match.AbstractClueChannelMatch;
import com.br.marketing.service.carclue.push.AbstractClueChannelPush;
import com.br.marketing.service.carclue.strategy.ClueChannelConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ClueChannelConfigServiceImpl implements ClueChannelConfigService {

    @Resource
    CarChannelConfigMapper carChannelConfigMapper;

    @Resource
    RedisChgService redisChgService;

    @Resource
    Map<String, AbstractClueChannelMatch> abstractClueChannelMatchMap;

    Map<String, AbstractClueChannelMatch> clueChannelMatchMapByLabel;

    @Resource
    Map<String, AbstractClueChannelConfig> abstractClueChannelConfigMap;

    Map<String, AbstractClueChannelConfig> clueChannelConfigMapByLabel;

    @Resource
    Map<String, AbstractClueChannelFilter> abstractClueChannelFilterMap;

    Map<String, AbstractClueChannelFilter> clueChannelFilterMapByLabel;

    @Resource
    Map<String, AbstractClueChannelPush> abstractClueChannelPushMap;

    Map<String, AbstractClueChannelPush> clueChannelPushMapByLabel;

    @Resource
    Map<String, AbstractClueChannelCallBack> abstractClueChannelCallBackMap;

    Map<String, AbstractClueChannelCallBack> clueChannelCallBackMapByLabel;


    @PostConstruct
    void init() {
        clueChannelMatchMapByLabel = new HashMap<>();
        for (Map.Entry<String, AbstractClueChannelMatch> stringAbstractClueChannelMatchEntry : abstractClueChannelMatchMap.entrySet()) {
            AbstractClueChannelMatch value = stringAbstractClueChannelMatchEntry.getValue();
            clueChannelMatchMapByLabel.put(value.label(), value);
        }

        clueChannelFilterMapByLabel = new HashMap<>();
        for (Map.Entry<String, AbstractClueChannelFilter> stringAbstractClueChannelFilterEntry : abstractClueChannelFilterMap.entrySet()) {
            AbstractClueChannelFilter value = stringAbstractClueChannelFilterEntry.getValue();
            clueChannelFilterMapByLabel.putIfAbsent(value.label(), value);
        }

        clueChannelPushMapByLabel = new HashMap<>();
        for (Map.Entry<String, AbstractClueChannelPush> stringAbstractClueChannelPushEntry : abstractClueChannelPushMap.entrySet()) {
            AbstractClueChannelPush value = stringAbstractClueChannelPushEntry.getValue();
            clueChannelPushMapByLabel.putIfAbsent(value.label(), value);
        }

        clueChannelCallBackMapByLabel = new HashMap<>();
        for (Map.Entry<String, AbstractClueChannelCallBack> stringAbstractClueChannelCallBackEntry : abstractClueChannelCallBackMap.entrySet()) {
            AbstractClueChannelCallBack value = stringAbstractClueChannelCallBackEntry.getValue();
            clueChannelCallBackMapByLabel.putIfAbsent(value.label(), value);
        }

        clueChannelConfigMapByLabel = new HashMap<>();
        for (Map.Entry<String, AbstractClueChannelConfig> stringAbstractClueChannelConfigEntry : abstractClueChannelConfigMap.entrySet()) {
            AbstractClueChannelConfig value = stringAbstractClueChannelConfigEntry.getValue();
            clueChannelConfigMapByLabel.putIfAbsent(value.label(), value);
        }
    }

    @Override
    public String getChannelApiCode(String label, Integer type) {
        List<CarChannelConfig> configs = getChannelConfig();
        if (ChannelConfigTypeEnum.MATCH_CONFIG.getValue().equals(type)) {
            Optional<CarChannelConfig> first = configs.stream().filter(t -> label.equals(t.getStrategyMatch())).findFirst();
            return first.isPresent() ? first.get().getApiCode() : null;
        }

        if (ChannelConfigTypeEnum.PUSH_CONFIG.getValue().equals(type)) {
            Optional<CarChannelConfig> first = configs.stream().filter(t -> label.equals(t.getStrategyPush())).findFirst();
            return first.isPresent() ? first.get().getApiCode() : null;
        }

        if (ChannelConfigTypeEnum.CALLBACK_CONFIG.getValue().equals(type)) {
            Optional<CarChannelConfig> first = configs.stream().filter(t -> label.equals(t.getStrategyCallback())).findFirst();
            return first.isPresent() ? first.get().getApiCode() : null;
        }
        return null;
    }

    @Override
    public List<AbstractClueChannelMatch> getChannelMatch() {
        ArrayList<AbstractClueChannelMatch> matchs = new ArrayList<>();
        List<CarChannelConfig> configs = getChannelConfig();
        configs.sort(Comparator.comparingInt(t -> t.getOrder()));
        for (CarChannelConfig config : configs) {
            if (clueChannelMatchMapByLabel.containsKey(config.getStrategyMatch())) {
                matchs.add(clueChannelMatchMapByLabel.get(config.getStrategyMatch()));
            }
        }
        return matchs;
    }


    @Override
    public List<AbstractClueChannelFilter> getChannelFilter(String apiCodeChannel) {
        List<CarChannelConfig> configs = getChannelConfig();
        List<String> filterLabels = configs.stream().filter(t -> apiCodeChannel.equals(t.getApiCode()))
                .flatMap(t -> Arrays.stream(t.getStrategyFitler().split(",")))
                .collect(Collectors.toList());
        List<AbstractClueChannelFilter> filters = clueChannelFilterMapByLabel
                .entrySet()
                .stream()
                .filter(t -> filterLabels.contains(t.getKey()))
                .map(t -> t.getValue())
                .collect(Collectors.toList());
        return filters;
    }

    @Override
    public AbstractClueChannelMatch getChannelMatchImpl(String apiCodeChannel) {
        List<CarChannelConfig> configs = getChannelConfig();
        Optional<String> pushOpt = configs.stream().filter(t -> apiCodeChannel.equals(t.getApiCode()))
                .map(t -> t.getStrategyMatch()).findFirst();
        return pushOpt.isPresent() ? clueChannelMatchMapByLabel.get(pushOpt.get()) : null;
    }

    @Override
    public AbstractClueChannelConfig getChannelConfigImpl(String apiCodeChannel) {
        List<CarChannelConfig> configs = getChannelConfig();
        Optional<String> pushOpt = configs.stream().filter(t -> apiCodeChannel.equals(t.getApiCode()))
                .map(t -> t.getStrategyConfigInfo()).findFirst();
        return pushOpt.isPresent() ? clueChannelConfigMapByLabel.get(pushOpt.get()) : null;
    }

    @Override
    public AbstractClueChannelCallBack getChannelCallBackImpl(String apiCodeChannel) {
        List<CarChannelConfig> configs = getChannelConfig();
        Optional<String> callOpt = configs.stream().filter(t -> apiCodeChannel.equals(t.getApiCode()))
                .map(t -> t.getStrategyCallback()).findFirst();
        return callOpt.isPresent() ? clueChannelCallBackMapByLabel.get(callOpt.get()) : null;
    }

    @Override
    public AbstractClueChannelPush getChannelPushImpl(String apiCodeChannel) {
        List<CarChannelConfig> configs = getChannelConfig();
        Optional<String> pushOpt = configs.stream().filter(t -> apiCodeChannel.equals(t.getApiCode()))
                .map(t -> t.getStrategyPush()).findFirst();
        return pushOpt.isPresent() ? clueChannelPushMapByLabel.get(pushOpt.get()) : null;
    }

    public List<CarChannelConfig> getChannelConfig() {
        try {
            if (redisChgService.exists(RedisKeyConstant.CLUE_CONFIG)) {
                String s = redisChgService.get(RedisKeyConstant.CLUE_CONFIG);
                List<CarChannelConfig> carChannelConfigs = JSON.parseArray(s, CarChannelConfig.class);
                return carChannelConfigs;
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        CarChannelConfigExample example = new CarChannelConfigExample();
        example.createCriteria().andIsDelEqualTo(Constants.DATA_VALID);
        List<CarChannelConfig> carChannelConfigs = carChannelConfigMapper.selectByExample(example);
        if (carChannelConfigs.size() > 0) {
            try {
                String content = JSON.toJSONString(carChannelConfigs);
                redisChgService.setex(RedisKeyConstant.CLUE_CONFIG, content, 3600);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }
        return carChannelConfigs;
    }


    @Override
    public Result updateClueConfig() {
        return null;
    }
}
