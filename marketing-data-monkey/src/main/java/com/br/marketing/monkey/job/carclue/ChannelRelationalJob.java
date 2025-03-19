package com.br.marketing.monkey.job.carclue;

import com.br.marketing.common.utils.Constants;
import com.br.marketing.entity.CarChannelConfig;
import com.br.marketing.entity.CarChannelConfigExample;
import com.br.marketing.entity.CarClueRelationalMappingExample;
import com.br.marketing.mapper.CarChannelConfigMapper;
import com.br.marketing.mapper.CarClueRelationalMappingMapper;
import com.br.marketing.service.carclue.ChannelRelationalService;
import com.br.marketing.service.carclue.clueenums.ChannelRule;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;

/**
 * @ClassName ChannelRelationalJob
 * @Description 外采渠道商信息维护
 * @Author kongbx
 * @Date 2025/1/19 17:04
 */
@Component
@Slf4j
public class ChannelRelationalJob extends AbstractSimpleElasticJob {
    @Resource
    private ChannelRelationalService channelRelationalService;
    @Resource
    CarClueRelationalMappingMapper carClueRelationalMappingMapper;
    @Resource
    CarChannelConfigMapper carChannelConfigMapper;
    private static final String TITLE = "【外采渠道商信息维护】";

    @Override
    public void process(JobExecutionMultipleShardingContext context) {

        String apiCode = "";
        CarChannelConfigExample example = new CarChannelConfigExample();
        example.createCriteria().andIsDelEqualTo(Constants.DATA_VALID);
        List<CarChannelConfig> carChannelConfigs = carChannelConfigMapper.selectByExample(example);

        for (CarChannelConfig config : carChannelConfigs) {
            if(ChannelRule.MatchChannelRuleEnum.DAILY_LIMITED.getLabel().equals(config.getStrategyMatch())){
                apiCode = config.getApiCode();
            }
        }

        CarClueRelationalMappingExample carClueRelationalMappingExample = new CarClueRelationalMappingExample();
        carClueRelationalMappingExample.createCriteria()
                .andAppletDateEqualTo(LocalDate.now().toString())
                .andApiCodeEqualTo(apiCode)
                .andIsDelEqualTo(Constants.DATA_VALID);
        int i = carClueRelationalMappingMapper.countByExample(carClueRelationalMappingExample);
        if(i > 0){
            log.warn(TITLE + "今日外采渠道信息已维护！");
            return;
        }

        log.warn(TITLE + "start");
        long start = System.currentTimeMillis();
        //获取当天的 易车KA 外采初始配置
        channelRelationalService.getInitMapping();
        //获取省市/车辆信息
        channelRelationalService.getProvinceAndCity();
        //维护外采渠道商信息
        channelRelationalService.relationalMapping();
        long end = System.currentTimeMillis();
        log.warn(TITLE + "end, 耗时{}ms", end-start);

    }
}
