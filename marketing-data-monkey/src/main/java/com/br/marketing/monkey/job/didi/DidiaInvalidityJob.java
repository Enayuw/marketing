package com.br.marketing.monkey.job.didi;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.SftpFileTypeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.SyncUserTypeNumDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.MarketingDataValidConfigMapper;
import com.br.marketing.mapper.MarketingSyncReportMapper;
import com.br.marketing.monkeydata.entity.didi.DiDiFailedCondition;
import com.br.marketing.monkeydata.handle.IMonkeyDataHandle;
import com.br.marketing.service.IJobManagerService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@Component
@Slf4j
public class DidiaInvalidityJob extends AbstractSimpleElasticJob {

    @Resource
    LocalFileMapper localFileMapper;

    @Autowired
    IMonkeyDataHandle diDiInvalidityHandle;


    @Resource
    MarketingDataValidConfigMapper dataValidConfigMapper;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    @Qualifier("jobManagerByDidiServiceImpl")
    IJobManagerService iJobManagerService;

    @Resource
    MarketingSyncReportMapper syncReportMapper;


    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        LocalDate now = LocalDate.now();
        String actionDay = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Date from = Date.from(now.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(now.plusDays(1L).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        String jobParameter = jobExecutionMultipleShardingContext.getJobParameter();
        String apiCode = StringUtils.isNotBlank(jobParameter) ? jobParameter : "3710083";

        LocalFileExample fileExample = new LocalFileExample();
        fileExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andFileTypeEqualTo(SftpFileTypeEnum.DD.getValue())
                .andStatusEqualTo("2")
                .andCreateTimeGreaterThanOrEqualTo(from)
                .andCreateTimeLessThan(end);
        List<LocalFile> localFiles = localFileMapper.selectByExample(fileExample);

        for (LocalFile localFile : localFiles) {
            Result<TransferActionFront> allowExecute = iJobManagerService.isAllowExecute(apiCode, 4, actionDay, localFile);
            if (!ResultCode.SUCCESS.getValue().equals(allowExecute.getCode())) {
                continue;
            }
            DiDiFailedCondition condition = new DiDiFailedCondition();
            condition.setLocalId(localFile.getId());
            condition.setDay(actionDay);
            condition.setPageSize(2000);

            //执行撞库逻辑
            Result action = diDiInvalidityHandle.action(condition);
            //更新任务
            iJobManagerService.updateJobStatus(allowExecute.getData(), ResultCode.SUCCESS.getValue().equals(action.getCode()));


            //region 删除有效期配置记录
            List<SyncUserTypeNumDTO> syncUserTypeNumDTOS = syncReportMapper.uploadSyncCount(apiCode, actionDay);
            MarketingDataValidConfigExample configExample = new MarketingDataValidConfigExample();
            configExample.createCriteria().andApiCodeEqualTo(localFile.getApiCode())
                    .andValidTypeEqualTo(1)
                    .andAppletDateEqualTo(actionDay)
                    .andIsDelEqualTo(Constants.DATA_VALID);
            List<MarketingDataValidConfig> validConfigs = dataValidConfigMapper.selectByExample(configExample);
            for (MarketingDataValidConfig validConfig : validConfigs) {
                Optional<SyncUserTypeNumDTO> first = syncUserTypeNumDTOS.stream().filter(t -> t.getUserType().equals(validConfig.getUserType())).findFirst();
                if (!first.isPresent()) {
                    MarketingDataValidConfig marketingDataValidConfig = new MarketingDataValidConfig();
                    marketingDataValidConfig.setId(validConfig.getId());
                    marketingDataValidConfig.setIsDel(9);
                    dataValidConfigMapper.updateByPrimaryKeySelective(marketingDataValidConfig);
                }
            }
            //endregion
        }
    }
}
