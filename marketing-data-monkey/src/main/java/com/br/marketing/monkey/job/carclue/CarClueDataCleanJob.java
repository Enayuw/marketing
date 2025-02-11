package com.br.marketing.monkey.job.carclue;

import com.br.common.log.AlertLog;
import com.br.common.util.StringUtils;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.service.carclue.CarClueService;
import com.br.marketing.service.carclue.clueenums.CarClueDataStatusEnum;
import com.br.marketing.service.carclue.strategy.ClueChannelConfigService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 * 车线索数据清洗作业job
 *
 * @author zhen.Li1
 * @dateTime 2025/01/05 14:13
 */
@Component
@Slf4j
public class CarClueDataCleanJob extends AbstractSimpleElasticJob {

    @Resource
    private CarClueInfoMapper carClueInfoMapper;

    @Resource
    private CarClueService carClueService;

    @Resource
    private CarChannelConfigMapper carChannelConfigMapper;

    @Resource
    private CarClueProvincesInformationMapper carClueProvincesInformationMapper;

    @Resource
    private CarClueSeriesInformationMapper carClueSeriesInformationMapper;

    @Resource
    private CarClueRelationalMappingMapper carClueRelationalMappingMapper;

    @Resource
    MarketingCommonConfig marketingCommonConfig;


    @Override
    public void process(JobExecutionMultipleShardingContext context) {

        // 通话明细apiCode
        Map<String, List<String>> carClueStorageConfig = marketingCommonConfig.getCarClueStorageConfig();
        List<String> carClueApiCodes = carClueStorageConfig.get("carClueApiCodes");
        String apiCode = carClueApiCodes.get(0);
        CarClueInfoExample carClueInfoExample = new CarClueInfoExample();
        carClueInfoExample.createCriteria().andApiCodeEqualTo(apiCode).andClueDataStatusEqualTo(CarClueDataStatusEnum.READY.getValue());
        carClueInfoExample.setOrderByClause("create_time asc limit 2000");
        List<CarClueInfo> carClueInfoList = carClueInfoMapper.selectByExample(carClueInfoExample);
        //查询城市，车型配置
        String proviceCleanDate = carClueProvincesInformationMapper.getMaxCleanDate();
        String seriesCleanDate = carClueSeriesInformationMapper.getMaxCleanDate();
        String relationCleanDate = carClueRelationalMappingMapper.getMaxCleanDate();
        if (StringUtils.isEmpty(proviceCleanDate) || StringUtils.isEmpty(seriesCleanDate) || StringUtils.isEmpty(relationCleanDate)) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(), "车线索清洗配置最大清洗日期为空，请关注"));
            return;
        }
        CarClueProvincesInformationExample provincesInformationExample = new CarClueProvincesInformationExample();
        provincesInformationExample.createCriteria().andAppletDateEqualTo(proviceCleanDate);
        List<CarClueProvincesInformation> carClueProvincesInfoList = carClueProvincesInformationMapper.selectByExample(provincesInformationExample);
        CarClueSeriesInformationExample seriesInformationExample = new CarClueSeriesInformationExample();
        seriesInformationExample.createCriteria().andAppletDateEqualTo(seriesCleanDate);
        List<CarClueSeriesInformation> carClueSeriesInfoList = carClueSeriesInformationMapper.selectByExample(seriesInformationExample);
        CarClueRelationalMappingExample carClueRelationalMappingExample = new CarClueRelationalMappingExample();
        carClueRelationalMappingExample.createCriteria().andAppletDateEqualTo(relationCleanDate).andMatchingTypeEqualTo(0);
        List<CarClueRelationalMapping> carClueRelationalMappingList = carClueRelationalMappingMapper.selectByExample(carClueRelationalMappingExample);
        if (CollectionUtils.isEmpty(carClueProvincesInfoList) || CollectionUtils.isEmpty(carClueSeriesInfoList) ||
                CollectionUtils.isEmpty(carClueRelationalMappingList)) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(), "车线索清洗配置为空，请关注"));
            return;
        }
        //查询渠道商配置
        CarChannelConfigExample channelConfigExample = new CarChannelConfigExample();
        channelConfigExample.createCriteria().andIsDelEqualTo(1);
        List<CarChannelConfig> channelConfigList = carChannelConfigMapper.selectByExample(channelConfigExample);
        channelConfigList.sort(Comparator.comparingInt(t -> t.getOrder()));
        carClueInfoList.forEach(carClueInfo -> {
            try {
                //清除错误信息
                carClueInfo.setClueErrorReason("");
                List<CarChannelConfig> configList = new ArrayList<>();
                configList.addAll(channelConfigList);
                carClueService.carClueCleanHandler(carClueInfo, carClueProvincesInfoList, carClueSeriesInfoList, carClueRelationalMappingList,
                        configList);
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(), "车线索清洗异常，请关注"), e);
            }
        });

    }
}
