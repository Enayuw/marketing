package com.br.marketing.monkey.job.carclue;

import cn.hutool.core.collection.CollectionUtil;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.CarClueInfo;
import com.br.marketing.entity.CarClueInfoExample;
import com.br.marketing.mapper.CarClueInfoMapper;
import com.br.marketing.service.carclue.CarClueService;
import com.br.marketing.service.carclue.callback.AbstractClueChannelCallBack;
import com.br.marketing.service.carclue.clueenums.CarClueCallBackStatusEnum;
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
 * @ClassName CarClueCallbackJob
 * @Description 车线索回调
 * @Author kongbx
 * @Date 2025/1/15 15:18
 */
@Component
@Slf4j
public class CarClueCallbackJob extends AbstractSimpleElasticJob {

    @Resource
    private CarClueInfoMapper carClueInfoMapper;

    @Resource
    private CarClueService carClueService;

    @Autowired
    private ClueChannelConfigService clueChannelConfigService;
    private static final String TITLE = "【车线索回调】";

    @Override
    public void process(JobExecutionMultipleShardingContext context) {

        log.warn(TITLE + "start");
        long start = System.currentTimeMillis();

        List<String> apiCodes = carClueInfoMapper.queryApiCodes(CarClueCallBackStatusEnum.READY.getValue());
        if(CollectionUtil.isEmpty(apiCodes)){
            return;
        }
        carClueCallback(apiCodes);

        long end = System.currentTimeMillis();
        log.warn(TITLE + "end, 耗时{}ms", end-start);
    }

    private void carClueCallback(List<String> apiCodes) {
        ThreadPoolExecutor pushCarClueThread =
                BrExecutors.getThreadPool(5, 5);

        for (String apiCode : apiCodes) {

            AbstractClueChannelCallBack channelCallBackImpl = clueChannelConfigService.getChannelCallBackImpl(apiCode);

            Long minId = null;
            boolean isContiue = Boolean.TRUE;
            while (isContiue) {
                CarClueInfoExample carClueInfoExample = new CarClueInfoExample();
                carClueInfoExample.setOrderByClause("id limit 2000");

                CarClueInfoExample.Criteria criteria = carClueInfoExample.createCriteria()
                        .andApiCodeEqualTo(apiCode)
                        .andClueCallbackStatusEqualTo(CarClueCallBackStatusEnum.READY.getValue());

                if (minId != null) {
                    criteria.andIdGreaterThan(minId);
                }

                List<CarClueInfo> carClueInfoList = carClueInfoMapper.selectByExample(carClueInfoExample);
                if (CollectionUtil.isEmpty(carClueInfoList)) {
                    isContiue = Boolean.FALSE;
                    continue;
                }
                minId = carClueInfoList.get(carClueInfoList.size() - 1).getId();
                pushCarClueThread.submit(() -> carClueService.carClueCallBackHandler(carClueInfoList, channelCallBackImpl));
            }
        }
        pushCarClueThread.shutdown();
        try {
            while (!pushCarClueThread.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.warn("车线索回调线程池关闭");
            }
        } catch (InterruptedException ex) {
            pushCarClueThread.shutdownNow();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ES_RETRY_DATAERROR.getCode(), "车线索回调线程池关闭！异常"), ex);
            Thread.currentThread().interrupt();
        }
    }

}
