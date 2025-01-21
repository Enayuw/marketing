package com.br.marketing.monkey.job.carclue;

import cn.hutool.core.collection.CollectionUtil;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.*;;
import com.br.marketing.mapper.CarClueInfoMapper;
import com.br.marketing.service.Impl.carclue.CarClueService;
import com.br.marketing.service.carclue.clueenums.CarCluePushStatusEnum;
import com.br.marketing.service.carclue.push.AbstractClueChannelPush;
import com.br.marketing.service.carclue.strategy.ClueChannelConfigService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName CarCluePushDataJob
 * @Description 车线索推送
 * @Author kongbx
 * @Date 2025/1/15 15:18
 */
@Component
@Slf4j
public class CarCluePushDataJob extends AbstractSimpleElasticJob {

    @Resource
    private CarClueInfoMapper carClueInfoMapper;

    @Resource
    private CarClueService carClueService;

    @Autowired
    private ClueChannelConfigService clueChannelConfigService;

    private static final String TITLE = "【推送车线索】";

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        log.warn(TITLE + "start");
        long start = System.currentTimeMillis();
        List<String> channels = carClueInfoMapper.queryApiCodes(CarCluePushStatusEnum.READY.getValue());
        if(CollectionUtil.isEmpty(channels)){
            return;
        }
        pushCarClue(channels);

        long end = System.currentTimeMillis();
        log.warn(TITLE + "end, 耗时{}ms", end-start);
    }

    private void pushCarClue(List<String> channels) {
        ThreadPoolExecutor pushCarClueThread =
                BrExecutors.getThreadPool(5, 5);

        for (String channel : channels) {

            AbstractClueChannelPush channelPushImpl = clueChannelConfigService.getChannelPushImpl(channel);

            if(channelPushImpl == null){
                log.warn(TITLE + "未找到推送实现，channel：{}", channel);
                continue;
            }

            Long minId = null;
            boolean isContiue = Boolean.TRUE;
            while (isContiue) {
                CarClueInfoExample carClueInfoExample = new CarClueInfoExample();
                carClueInfoExample.setOrderByClause("id limit 2000");

                CarClueInfoExample.Criteria criteria = carClueInfoExample.createCriteria()
                        .andCluePushChannelEqualTo(channel)
                        .andCluePushStatusEqualTo(CarCluePushStatusEnum.READY.getValue());

                if (minId != null) {
                    criteria.andIdGreaterThan(minId);
                }

                List<CarClueInfo> carClueInfoList = carClueInfoMapper.selectByExample(carClueInfoExample);
                if (CollectionUtil.isEmpty(carClueInfoList)) {
                    isContiue = Boolean.FALSE;
                    continue;
                }
                minId = carClueInfoList.get(carClueInfoList.size() - 1).getId();
                pushCarClueThread.submit(() -> carClueService.pushCarClueHandler(carClueInfoList, channelPushImpl));
            }
        }
        pushCarClueThread.shutdown();
        try {
            while (!pushCarClueThread.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.warn("推送车线索线程池关闭");
            }
        } catch (InterruptedException ex) {
            pushCarClueThread.shutdownNow();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ES_RETRY_DATAERROR.getCode(), "推送车线索线程池关闭！异常"), ex);
            Thread.currentThread().interrupt();
        }
    }

}
