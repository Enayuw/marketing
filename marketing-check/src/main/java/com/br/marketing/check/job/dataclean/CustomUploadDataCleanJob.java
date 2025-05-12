package com.br.marketing.check.job.dataclean;

import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingDataCleanGeneralConfig;
import com.br.marketing.mapper.rulecleaning.MarketingCustomerOriginalDataMapper;
import com.br.marketing.mapper.rulecleaning.MarketingDataCleanGeneralConfigMapper;
import com.br.marketing.service.clean.common.DataCleanService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
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
        MarketingDataCleanGeneralConfig config = getCleanDataTask(apiCode, appletDateList);
        if (Objects.isNull(config)) {
            return;
        }
        dataCleanService.customUploadDataClean(config, appletDateList);
        //TODO 更新配置状态
        config.setCustomRunStatus(0);
        marketingDataCleanGeneralConfigMapper.updateByPrimaryKeySelective(config);


    }

    private MarketingDataCleanGeneralConfig getCleanDataTask(String apiCode, List<String> appletDateList) {
        String reidsKey = RedisKeyConstant.DATA_CLEAN_TASK_LOCK;
        String value = UUID.randomUUID().toString();
        try {
            redisChgService.lockLoop(reidsKey, value, 10000L, 30000L);
            MarketingDataCleanGeneralConfig config = marketingDataCleanGeneralConfigMapper.getCustomUploadConfig(apiCode);
            if (Objects.isNull(config)) {
                log.warn("上传数据-清洗配置为空");
                return null;
            }
            //查询是否有 待清洗数据
            Long dataNum = marketingCustomerOriginalDataMapper.getCustomUploadDataNum(config.getApiCode(), appletDateList);
            if (dataNum == 0L) {
                log.warn("上传数据-待清洗数据为空");
                return null;
            }
            MarketingDataCleanGeneralConfig ruleConfig = marketingDataCleanGeneralConfigMapper.selectByPrimaryKey(config.getId());
            if (ruleConfig.getCustomRunStatus() == 1) {
                log.warn("上传数据-可能出现锁超时释放");
                return null;
            }
            ruleConfig.setCustomRunStatus(1);
            marketingDataCleanGeneralConfigMapper.updateByPrimaryKeySelective(ruleConfig);
            return config;
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SERVICEERROR_UNKNOWN.getCode(), "清洗任务异常，Redis 锁已经释放！"));
            return null;
        } finally {
            redisChgService.unlock(reidsKey, value);
        }
    }
}
