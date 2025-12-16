package com.br.marketing.check.job.dataclean;

import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingDataCleanGeneralConfig;
import com.br.marketing.enums.clean.DataCleanConfigRunStatusEnum;
import com.br.marketing.mapper.rulecleaning.MarketingCustomerOriginalDataMapper;
import com.br.marketing.mapper.rulecleaning.MarketingDataCleanGeneralConfigMapper;
import com.br.marketing.service.clean.common.DataCleanService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.marketingkit.tracking.model.indicator.DataFlowDirection;
import com.marketingkit.tracking.service.TrackingService;
import com.marketingkit.tracking.util.TrackingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


@Component
@Slf4j
/**
 * @author zhen.Li1
 * @Classname CustomUploadDataCleanJob
 * @Description 定制上传数据清洗JOB
 * @Date 2025/05/06
 */
public class CustomUploadDataCleanJob extends AbstractSimpleElasticJob {

    @Resource
    RedisChgService redisChgService;


    @Resource
    MarketingDataCleanGeneralConfigMapper marketingDataCleanGeneralConfigMapper;

    @Resource
    MarketingCustomerOriginalDataMapper marketingCustomerOriginalDataMapper;

    @Resource
    private DataCleanService dataCleanService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {

        String parameter = context.getJobParameter();
        String apiCode = null;
        List<String> appletDateList = new ArrayList<>();
        if (StringUtils.isNotEmpty(parameter)) {
            String[] split = parameter.split("#");
            apiCode = split[0];
            appletDateList.add(split[1]);
        }
        if (CollectionUtils.isEmpty(appletDateList)) {
            appletDateList.add(LocalDate.now().toString());
            appletDateList.add(LocalDate.now().minusDays(1).toString());
        }

        List<MarketingDataCleanGeneralConfig> configList = marketingDataCleanGeneralConfigMapper.getCustomUploadConfig(apiCode);
        configList.forEach(config -> {
                    //获取未运行的清洗任务
                    MarketingDataCleanGeneralConfig cleanDataTask = getCleanDataTask(config, appletDateList);
                    if (Objects.isNull(cleanDataTask)) {
                        return;
                    }
                    dataCleanService.customUploadDataClean(cleanDataTask, appletDateList);
                    //更新配置运行状态：
                    config.setCustomRunStatus(DataCleanConfigRunStatusEnum.READY.getCode());
                    marketingDataCleanGeneralConfigMapper.updateByPrimaryKeySelective(config);
                }
        );
    }

    private MarketingDataCleanGeneralConfig getCleanDataTask(MarketingDataCleanGeneralConfig config, List<String> appletDateList) {
        String reidsKey = RedisKeyConstant.DATA_CLEAN_TASK_LOCK.concat(":").concat(config.getApiCode()).concat(":").concat(config.getDataType().toString())
                .concat(":").concat(config.getAcceptType().toString());
        String value = UUID.randomUUID().toString();
        try {
            redisChgService.lockLoop(reidsKey, value, 10000L, 30000L);
            MarketingDataCleanGeneralConfig cleanConfig = marketingDataCleanGeneralConfigMapper.selectByPrimaryKey(config.getId());
            if (cleanConfig.getCustomRunStatus().equals(DataCleanConfigRunStatusEnum.RUNNING.getCode())) {
                return null;
            }
            //查询是否有 待清洗数据
            Long dataNum = marketingCustomerOriginalDataMapper.getCustomUploadDataNum(config.getApiCode(), appletDateList);
            if (dataNum == 0L) {
                log.warn("apiCode={},上传数据-待清洗数据为空", config.getApiCode());
                return null;
            }
            cleanConfig.setCustomRunStatus(DataCleanConfigRunStatusEnum.RUNNING.getCode());
            marketingDataCleanGeneralConfigMapper.updateByPrimaryKeySelective(cleanConfig);
            return config;
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SERVICEERROR_UNKNOWN.getCode(), "清洗任务异常，Redis 锁已经释放！"));
            return null;
        } finally {
            redisChgService.unlock(reidsKey, value);
        }
    }
}
