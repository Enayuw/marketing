package com.br.marketing.monkey.job.carclue;

import com.br.common.log.AlertLog;
import com.br.common.util.StringUtils;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.service.carclue.CarClueService;
import com.br.marketing.service.carclue.clueenums.CarClueDataStatusEnum;
import com.br.marketing.service.carclue.strategy.ClueChannelConfigService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

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

    public static ThreadPoolExecutor pushCluePool = BrExecutors.getThreadPool(10, 10);


    @Override
    public void process(JobExecutionMultipleShardingContext context) {

        // 通话明细apiCode
        Map<String, List<String>> carClueStorageConfig = marketingCommonConfig.getCarClueStorageConfig();
        List<String> carClueApiCodes = carClueStorageConfig.get("carClueApiCodes");
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
        List<CompletableFuture<Void>> futures = Lists.newArrayList();
        Boolean mark = Boolean.TRUE;
        Long minId = null;
        while (mark) {
            List<CarClueInfo> carClueInfoList = carClueInfoMapper.selectCarClueByMinId(carClueApiCodes, CarClueDataStatusEnum.READY.getValue(), minId);
            if (carClueInfoList.size() <= 0) {
                mark = Boolean.FALSE;
                continue;
            }
            minId = carClueInfoList.get(carClueInfoList.size() - 1).getId();
            List<List<CarClueInfo>> partitions = Lists.partition(carClueInfoList, 500);
            for (List<CarClueInfo> partition : partitions) {
                futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        cleanClueList(partition, channelConfigList, carClueProvincesInfoList, carClueSeriesInfoList, carClueRelationalMappingList);
                    } catch (Exception e) {
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(), "车线索清洗线程处理异常，请关注"), e);
                    }
                }, pushCluePool));

            }

        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }


    private void cleanClueList(List<CarClueInfo> carClueInfoList, List<CarChannelConfig> channelConfigList, List<CarClueProvincesInformation>
            carClueProvincesInfoList, List<CarClueSeriesInformation> carClueSeriesInfoList, List<CarClueRelationalMapping> carClueRelationalMappingList) {
        Long start=System.currentTimeMillis();
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
        log.warn("车线索清洗单批次，耗时：{}ms",System.currentTimeMillis()-start);

    }
}
