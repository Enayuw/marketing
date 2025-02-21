package com.br.marketing.check.job.mark;

import cn.hutool.core.lang.UUID;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.DataMarkConfig;
import com.br.marketing.entity.DataMarkConfigExample;
import com.br.marketing.entity.FlagData;
import com.br.marketing.enums.DataMarkEnum;
import com.br.marketing.mapper.DataMarkConfigMapper;
import com.br.marketing.mapper.FlagDataMapper;
import com.br.marketing.service.mark.PpRonShuMarkService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @Description 客群、利率标签打标
 * @Author hong.chen
 * @CreateTime 2025/02/20
 */
@Component
@Slf4j
public class DataRiskGroupMarkJob extends AbstractSimpleElasticJob {
    @Resource
    private RedisChgService redisChgService;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private FlagDataMapper flagDataMapper;
    private final ThreadPoolExecutor pool = BrExecutors.getThreadPool(30, 30);
    @Resource
    private PpRonShuMarkService ppRonShuMarkService;
    @Resource
    DataMarkConfigMapper dataMarkConfigMapper;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        marketingCommonConfig.getDataMarkApiCodes().forEach((String apiCode) -> {
            while (true) {
                // 获取redis锁
                String lockKey = RedisKeyConstant.prefix.concat(DataMarkEnum.MARK_RISKGROUP.getMarkRedisKey()).concat(":").concat(apiCode);
                String lockValue = UUID.fastUUID().toString();
                List<FlagData> flagData;
                try {
                    redisChgService.lock(lockKey, lockValue);
                    // 查询数据
                    flagData = flagDataMapper.queryRiskGroupAndInterestData(apiCode, marketingCommonConfig.getDataMarkPageSize());
                    if (CollectionUtils.isEmpty(flagData)) {
                        break;
                    }
                    // 更新数据状态
                    List<Long> ids = flagData.stream().map(FlagData::getId).collect(Collectors.toList());
                    flagDataMapper.batchUpdateFlagStatusById(ids, 0);
                } catch (Exception e) {
                    String subject = "pp榕树客群、利率标签打标异常";
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), e.getMessage()
                            , subject), e);
                    break;
                } finally {
                    // 释放锁
                    redisChgService.unlock(lockKey, lockValue);
                }

                // 更新数据标签
                markData(flagData, apiCode);
            }
        });
    }

    private void markData(List<FlagData> flagData, String apiCode) {
        DataMarkConfigExample markConfigExample = new DataMarkConfigExample();
        markConfigExample.createCriteria().andIsDelEqualTo(1)
                .andApiCodeEqualTo(apiCode);
        List<DataMarkConfig> dataMarkConfigs = dataMarkConfigMapper.selectByExample(markConfigExample);

        pool.setCorePoolSize(marketingCommonConfig.getDataGroupThreadNum());
        pool.setMaximumPoolSize(marketingCommonConfig.getDataGroupThreadNum());
        Lists.partition(flagData, 2000).forEach(partition -> {
            pool.submit(() -> {
                List<FlagData> list = new ArrayList<>(partition);
                ppRonShuMarkService.markAndUpdateFlagStatus(list, apiCode, dataMarkConfigs);
            });
        });
    }
}
