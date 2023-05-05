package com.br.marketing.monkey.job.didi;
import java.util.Date;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.SftpFileTypeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.enums.DiDiAllowMarketingEnum;
import com.br.marketing.mapper.DidiDataMapper;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.MarketingDataValidConfigMapper;
import com.br.marketing.monkeydata.entity.didi.DiDiAllowCondition;
import com.br.marketing.monkeydata.handle.IMonkeyDataHandle;
import com.br.marketing.monkeydata.handle.didi.DidiCallRecordHandle;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Component
@Slf4j
public class DidiAllowJob extends AbstractSimpleElasticJob {

    @Resource
    LocalFileMapper localFileMapper;

    @Autowired
    IMonkeyDataHandle diDiAllowHandle;

    @Resource
    DidiDataMapper didiDataMapper;

    @Resource
    MarketingDataValidConfigMapper dataValidConfigMapper;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        Date from = Date.from(LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        String jobParameter = jobExecutionMultipleShardingContext.getJobParameter();
        String apiCode = StringUtils.isNotBlank(jobParameter) ? jobParameter :"";
        List<LocalFile> localFiles = localFileMapper.getLocalFileByPushNoOrError(apiCode,SftpFileTypeEnum.DD.getValue());
        for (LocalFile localFile : localFiles) {
            //创建修改的文件对象
            LocalFile updaEntity = new LocalFile();
            updaEntity.setId(localFile.getId());

            DiDiAllowCondition condition = new DiDiAllowCondition();
            condition.setPageSize(2000);
            condition.setLocalId(localFile.getId());
            if(localFile.getPushStartTime() == null){
                updaEntity.setPushStartTime(new Date());
            }
            //执行撞库逻辑
            Result action = diDiAllowHandle.action(condition);

            //region 更新文件表
            DidiDataExample dataExample = new DidiDataExample();
            dataExample.createCriteria()
                    .andLocalIdEqualTo(localFile.getId())
                    .andPushStatusEqualTo(2)
                    .andStatusEqualTo(1)
                    .andIsMarketingEqualTo(DiDiAllowMarketingEnum.YES.getValue());
            int successNum = didiDataMapper.countByExample(dataExample);
            DidiDataExample allExample = new DidiDataExample();
            allExample.createCriteria()
                    .andLocalIdEqualTo(localFile.getId());
            int allNum = didiDataMapper.countByExample(allExample);
            updaEntity.setPushNumber(successNum);
            updaEntity.setErrorActualNumber(allNum - successNum);
            if(ResultCode.FAIL.getValue().equals(action.getCode())){
                updaEntity.setPushStatus("3");
            }
            if(ResultCode.SUCCESS.getValue().equals(action.getCode())){
                updaEntity.setPushStatus("4");
            }
            localFileMapper.updateByPrimaryKeySelective(updaEntity);
            //endregion

            //region 生成有效期配置记录
            List<String> pushDates = didiDataMapper.getPushDateByLocalId(localFile.getId());
            MarketingDataValidConfigExample configExample = new MarketingDataValidConfigExample();
            configExample.createCriteria().andApiCodeEqualTo(localFile.getApiCode())
                    .andValidTypeEqualTo(1)
                    .andAppletDateIn(pushDates)
                    .andIsDelEqualTo(Constants.DATA_VALID);
            List<MarketingDataValidConfig> validConfigs = dataValidConfigMapper.selectByExample(configExample);
            for (String pushDate : pushDates) {
                String endDate = LocalDate.parse(pushDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")).plusDays(30L).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                Optional<MarketingDataValidConfig> first = validConfigs.stream().filter(t -> t.getAppletDate().equals(pushDate)).findFirst();
                if(!first.isPresent()){
                    MarketingDataValidConfig marketingDataValidConfig = new MarketingDataValidConfig();
                    marketingDataValidConfig.setApiCode(apiCode);
                    marketingDataValidConfig.setAppletDate(pushDate);
                    marketingDataValidConfig.setUserType("1");
                    marketingDataValidConfig.setValidStartDate(pushDate);
                    marketingDataValidConfig.setValidEndDate(endDate);
                    marketingDataValidConfig.setValidType(1);
                    marketingDataValidConfig.setCreateTime(new Date());
                    marketingDataValidConfig.setIsDel(Constants.DATA_VALID);
                    dataValidConfigMapper.insertSelective(marketingDataValidConfig);
                }
            }
            //endregion
        }
    }
}
