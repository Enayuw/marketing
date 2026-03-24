package com.br.marketing.check.job.dataclean;

import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.DataTypeEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.enums.clean.DataCleanConfigRunStatusEnum;
import com.br.marketing.enums.clean.DataProcessEnum;
import com.br.marketing.mapper.MarketingCleanDataFileMapper;
import com.br.marketing.mapper.SyncConfigMapper;
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
import java.util.*;
import java.util.regex.Pattern;

@Component
@Slf4j
/**
 * @author zhen.Li1
 * @Classname FileUploadDataCleanTaskJob
 * @Description 文件上传数据清洗JOB
 * @Date 2025/06/17
 */
public class FileUploadDataCleanTaskJob extends AbstractSimpleElasticJob {

    private static final Pattern EMPTY_PATH_PATTERN = Pattern.compile("^$");

    @Resource
    private MarketingDataCleanGeneralConfigMapper cleanGeneralConfigMapper;

    @Resource
    private MarketingCleanDataFileMapper marketingCleanDataFileMapper;

    @Resource
    RedisChgService redisChgService;


    @Resource
    private DataCleanService dataCleanService;

    @Resource
    private TrackingService trackingService;

    @Resource
    SyncConfigMapper syncConfigMapper;

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
        queryParam.setStatus(DataProcessEnum.RuleStatusEnum.PRE_SUCCESS.getCode());
        // 执行查询
        List<MarketingDataCleanGeneralConfig> ruleList = cleanGeneralConfigMapper.selectRuleList(queryParam);
        ruleList.forEach(config -> {
            SyncConfigExample syncConfigCycle = new SyncConfigExample();
            SyncConfigExample.Criteria criteriaCycle = syncConfigCycle.createCriteria();
            criteriaCycle.andStatusEqualTo(1).andDataTypeEqualTo(DataTypeEnum.MARKETING_UP_CYCLE_DATA.getValue())
                    .andApiCodeEqualTo(config.getApiCode()).andSrcPathEqualTo(config.getSftpPath()).andTypeEqualTo(1);
            List<SyncConfig> syncCycleConfigs = syncConfigMapper.selectByExample(syncConfigCycle);
            if (CollectionUtils.isEmpty(syncCycleConfigs)) {
                return;
            }
            String localPath = syncCycleConfigs.get(0).getTargetPath();
            while (true) {
                MarketingCleanDataFile cleanFile = getCleanFileTask(config, appletDateList, localPath);
                if (Objects.isNull(cleanFile)) {
                    break;
                }
                try {
                    dataCleanService.fileUploadDataClean(cleanFile, config);
                    MarketingCleanDataFile update = new MarketingCleanDataFile();
                    update.setStatus(DataProcessEnum.FileStatusEnum.SUCCESS.getCode());
                    update.setId(cleanFile.getId());
                    marketingCleanDataFileMapper.updateByPrimaryKeySelective(update);
                } catch (Exception e) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SERVICEERROR_UNKNOWN.getCode(),
                            String.format("文件清洗失败, cleanFileId=%s, apiCode=%s", cleanFile.getId(), config.getApiCode())), e);
                    MarketingCleanDataFile failUpdate = new MarketingCleanDataFile();
                    failUpdate.setId(cleanFile.getId());
                    failUpdate.setStatus(DataProcessEnum.FileStatusEnum.FAIL.getCode());
                    marketingCleanDataFileMapper.updateByPrimaryKeySelective(failUpdate);
                }
            }
        });

    }

    private MarketingCleanDataFile getCleanFileTask(MarketingDataCleanGeneralConfig config, List<String> appletDateList, String localPath) {

        String reidsKey = RedisKeyConstant.DATA_CLEAN_TASK_LOCK.concat(":").concat(config.getApiCode()).concat(":").concat(config.getDataType().toString())
                .concat(":").concat(config.getAcceptType().toString());
        String value = UUID.randomUUID().toString();
        try {
            redisChgService.lockLoop(reidsKey, value, 10000L, 30000L);
            MarketingCleanDataFileExample fileExample = new MarketingCleanDataFileExample();
            MarketingCleanDataFileExample.Criteria criteria = fileExample.createCriteria();
            criteria.andApiCodeEqualTo(config.getApiCode()).andStatusEqualTo(DataProcessEnum.FileStatusEnum.READY.getCode())
                    .andIsDelEqualTo(1).andReceiveDateIn(appletDateList);
            boolean pathHasDatePlaceholder = localPath != null
                    && (localPath.contains("yyyyMMdd") || localPath.contains("yyyy-MM-dd"));
            if (!pathHasDatePlaceholder) {
                criteria.andLocalPathEqualTo(localPath);
            }
            fileExample.setOrderByClause("create_time desc");
            List<MarketingCleanDataFile> cleanDataFiles = marketingCleanDataFileMapper.selectByExample(fileExample);
            if (pathHasDatePlaceholder && !cleanDataFiles.isEmpty()) {
                Pattern pathPattern = templateToPathRegex(localPath);
                cleanDataFiles = new ArrayList<>(cleanDataFiles);
                cleanDataFiles.removeIf(f -> f.getLocalPath() == null || !pathPattern.matcher(f.getLocalPath()).matches());
            }
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

    /** 将含 yyyyMMdd、yyyy-MM-dd 的路径模板转成匹配“任意日期”的正则 */
    private Pattern templateToPathRegex(String template) {
        if (template == null) {
            return EMPTY_PATH_PATTERN;
        }
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < template.length()) {
            if (template.startsWith("yyyy-MM-dd", i)) {
                sb.append("\\d{4}-\\d{2}-\\d{2}");
                i += 10;
            } else if (template.startsWith("yyyyMMdd", i)) {
                sb.append("\\d{8}");
                i += 8;
            } else {
                sb.append(Pattern.quote(template.substring(i, i + 1)));
                i++;
            }
        }
        return Pattern.compile(sb.toString());
    }
}
