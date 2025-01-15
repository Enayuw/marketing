package com.br.marketing.service.carclue.strategy.impl;

import com.alibaba.fastjson.JSON;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.entity.CarChannelConfig;
import com.br.marketing.entity.CarChannelConfigExample;
import com.br.marketing.mapper.CarChannelConfigMapper;
import com.br.marketing.service.carclue.clueenums.ChannelConfigTypeEnum;
import com.br.marketing.service.carclue.match.AbstractClueChannelMatch;
import com.br.marketing.service.carclue.strategy.ClueChannelConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Service
public class ClueChannelConfigServiceImpl implements ClueChannelConfigService {

    @Resource
    CarChannelConfigMapper carChannelConfigMapper;

    @Resource
    RedisChgService redisChgService;

    @Resource
    Map<String, AbstractClueChannelMatch> abstractClueChannelMatchMap;

    Map<String, AbstractClueChannelMatch> clueChannelMatchMap;

    @PostConstruct
    void init() {
        clueChannelMatchMap = new HashMap<>();
        for (Map.Entry<String, AbstractClueChannelMatch> stringAbstractClueChannelMatchEntry : abstractClueChannelMatchMap.entrySet()) {
            AbstractClueChannelMatch value = stringAbstractClueChannelMatchEntry.getValue();
            clueChannelMatchMap.put(value.label(), value);
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
        configs.sort(Comparator.comparingInt(t->t.getOrder()));
        for (CarChannelConfig config : configs) {
            if(clueChannelMatchMap.containsKey(config.getStrategyMatch())){
                matchs.add(clueChannelMatchMap.get(config.getStrategyMatch()));
            }
        }
        return matchs;
    }

    private List<CarChannelConfig> getChannelConfig() {
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
