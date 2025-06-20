package com.br.marketing.check.job.dataclean;

import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingCleanDataFile;
import com.br.marketing.entity.MarketingCleanDataFileExample;
import com.br.marketing.entity.MarketingCleanDataTask;
import com.br.marketing.entity.MarketingDataCleanGeneralConfig;
import com.br.marketing.enums.clean.DataCleanConfigRunStatusEnum;
import com.br.marketing.enums.clean.DataProcessEnum;
import com.br.marketing.mapper.MarketingCleanDataFileMapper;
import com.br.marketing.mapper.rulecleaning.MarketingDataCleanGeneralConfigMapper;
import com.br.marketing.service.clean.common.DataCleanService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;

@Component
@Slf4j
/**
 * @author zhen.Li1
 * @Classname FileDataCleanTaskJob
 * @Description 文件上传数据清洗JOB
 * @Date 2025/06/17
 */
public class FileUploadDataCleanTaskJob extends AbstractSimpleElasticJob {


    @Resource
    private MarketingDataCleanGeneralConfigMapper cleanGeneralConfigMapper;

    @Resource
    private MarketingCleanDataFileMapper marketingCleanDataFileMapper;

    @Resource
    RedisChgService redisChgService;


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
        //查询规则条件
        MarketingDataCleanGeneralConfig queryParam = new MarketingDataCleanGeneralConfig();
        queryParam.setAcceptType(DataProcessEnum.AcceptTypeEnum.FTP.getCode());
        queryParam.setDataType(DataProcessEnum.DataTypeEnum.UPLOAD.getCode());
        queryParam.setApiCode(apiCode);
        // 执行查询
        List<MarketingDataCleanGeneralConfig> ruleList = cleanGeneralConfigMapper.selectRuleList(queryParam);
        ruleList.forEach(config -> {
            //获取待清洗的文件任务
            MarketingCleanDataFile cleanFile = getCleanFileTask(config, appletDateList);
            if (Objects.isNull(cleanFile)) {
                return;
            }
            //文件清洗
            dataCleanService.fileUploadDataClean(cleanFile, config);
            MarketingCleanDataFile update = new MarketingCleanDataFile();
            update.setStatus(DataProcessEnum.FileStatusEnum.SUCCESS.getCode());
            update.setId(cleanFile.getId());
            marketingCleanDataFileMapper.updateByPrimaryKeySelective(update);
        });
    }

    private MarketingCleanDataFile getCleanFileTask(MarketingDataCleanGeneralConfig config, List<String> appletDateList) {

        String reidsKey = RedisKeyConstant.DATA_CLEAN_TASK_LOCK.concat(":").concat(config.getApiCode()).concat(":").concat(config.getDataType().toString())
                .concat(":").concat(config.getAcceptType().toString());
        String value = UUID.randomUUID().toString();
        try {
            redisChgService.lockLoop(reidsKey, value, 10000L, 30000L);
            MarketingCleanDataFileExample fileExample = new MarketingCleanDataFileExample();
            fileExample.createCriteria().andApiCodeEqualTo(config.getApiCode()).andStatusEqualTo(DataProcessEnum.FileStatusEnum.READY.getCode())
                    .andIsDelEqualTo(1).andTargetSftpPathEqualTo(config.getSftpPath()).andReceiveDateIn(appletDateList);
            fileExample.setOrderByClause("create_time desc");
            List<MarketingCleanDataFile> cleanDataFiles = marketingCleanDataFileMapper.selectByExample(fileExample);
            if (cleanDataFiles.isEmpty()) {
                return null;
            }
            MarketingCleanDataFile cleanDataFile = cleanDataFiles.get(0);
            // 任务设置为清洗中
            cleanDataFile.setStatus(DataProcessEnum.FileStatusEnum.RUNNING.getCode());
            marketingCleanDataFileMapper.updateByPrimaryKeySelective(cleanDataFile);
            return cleanDataFile;
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SERVICEERROR_UNKNOWN.getCode(), "清洗任务异常，Redis 锁已经释放！"));
            return null;
        } finally {
            redisChgService.unlock(reidsKey, value);
        }
    }
}
