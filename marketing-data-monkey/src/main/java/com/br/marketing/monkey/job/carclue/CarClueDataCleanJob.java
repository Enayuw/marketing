package com.br.marketing.monkey.job.carclue;

import com.br.marketing.entity.*;
import com.br.marketing.mapper.CarChannelConfigMapper;
import com.br.marketing.mapper.CarClueInfoMapper;
import com.br.marketing.service.Impl.carclue.CarClueService;
import com.br.marketing.service.carclue.clueenums.CarClueDataStatusEnum;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

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

    @Override
    public void process(JobExecutionMultipleShardingContext context) {

        String apiCode = "3710012";
        CarClueInfoExample carClueInfoExample = new CarClueInfoExample();
        carClueInfoExample.createCriteria().andApiCodeEqualTo(apiCode).andClueDataStatusEqualTo(CarClueDataStatusEnum.READY.getValue())
                .andCreateTimeGreaterThan(Date.from(LocalDate.now().minusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        carClueInfoExample.setOrderByClause("create_time asc limit 2000");
        List<CarClueInfo> carClueInfoList = carClueInfoMapper.selectByExample(carClueInfoExample);
        //TODO 查询城市，车型配置
        Object brandCitycConfig = new Object();

        //查询渠道商配置
        CarChannelConfigExample channelConfigExample = new CarChannelConfigExample();
        channelConfigExample.createCriteria().andIsDelEqualTo(1);
        channelConfigExample.setOrderByClause("order desc");
        List<CarChannelConfig> channelConfigList =  carChannelConfigMapper.selectByExample(channelConfigExample);


        carClueInfoList.forEach(carClueInfo -> {

            carClueService.carClueCleanHandler(carClueInfo, brandCitycConfig,channelConfigList);


        });


    }
}
