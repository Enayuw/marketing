package com.br.marketing.monkey.job.didi;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.SftpFileTypeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.DidiDataExample;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.enums.DiDiAllowMarketingEnum;
import com.br.marketing.mapper.DidiDataMapper;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.monkeydata.entity.didi.DiDiAllowCondition;
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
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;


@Component
@Slf4j
public class DidiAllowJob extends AbstractSimpleElasticJob {

    @Resource
    LocalFileMapper localFileMapper;

    @Autowired
    IMonkeyDataHandle diDiAllowHandle;

    @Resource
    DidiDataMapper didiDataMapper;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    @Qualifier("jobManagerByDidiServiceImpl")
    IJobManagerService iJobManagerService;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        LocalDate now = LocalDate.now();
        String actionDay = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String jobParameter = jobExecutionMultipleShardingContext.getJobParameter();
        String apiCode = StringUtils.isNotBlank(jobParameter) ? jobParameter :"3710083";
        List<LocalFile> localFiles = localFileMapper.getLocalFileByPushNoOrError(apiCode,SftpFileTypeEnum.DD.getValue());
        for (LocalFile localFile : localFiles) {
            Result<TransferActionFront> allowExecute = iJobManagerService.isAllowExecute(apiCode, 2, actionDay, localFile);
            if(!ResultCode.SUCCESS.getValue().equals(allowExecute.getCode())){
                continue;
            }
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
            updaEntity.setPushNumber(successNum);
            updaEntity.setPushEndTime(new Date());
            //更新任务
            Result<TransferActionFront> transferActionFrontResult = iJobManagerService.updateJobStatus(allowExecute.getData(), ResultCode.SUCCESS.getValue().equals(action.getCode()));
            if(ResultCode.SUCCESS.getValue().equals(transferActionFrontResult.getCode())){
                updaEntity.setPushStatus("2");
            }else{
                if ("1".equals(transferActionFrontResult.getMessage())) {
                    updaEntity.setPushStatus("3");
                } else {
                    updaEntity.setPushStatus("4");
                }
            }
            localFileMapper.updateByPrimaryKeySelective(updaEntity);
            //endregion

            //region 生成有效期配置记录
//            Long validDays = marketingCommonConfig.getDidiValidDays()!=null && marketingCommonConfig.getDidiValidDays()>0 ?marketingCommonConfig.getDidiValidDays()-1L:29L;
//            List<String> pushDates = didiDataMapper.getPushDateByLocalId(localFile.getId());
//            if(pushDates.size()>0){
//                MarketingDataValidConfigExample configExample = new MarketingDataValidConfigExample();
//                configExample.createCriteria().andApiCodeEqualTo(localFile.getApiCode())
//                        .andValidTypeEqualTo(1)
//                        .andAppletDateIn(pushDates)
//                        .andIsDelEqualTo(Constants.DATA_VALID);
//                List<MarketingDataValidConfig> validConfigs = dataValidConfigMapper.selectByExample(configExample);
//                for (String pushDate : pushDates) {
//                    String endDate = LocalDate.parse(pushDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")).plusDays(validDays).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
//                    Optional<MarketingDataValidConfig> first = validConfigs.stream().filter(t -> t.getAppletDate().equals(pushDate)).findFirst();
//                    if(!first.isPresent()){
//                        MarketingDataValidConfig marketingDataValidConfig = new MarketingDataValidConfig();
//                        marketingDataValidConfig.setApiCode(apiCode);
//                        marketingDataValidConfig.setAppletDate(pushDate);
//                        marketingDataValidConfig.setUserType("1");
//                        marketingDataValidConfig.setValidStartDate(pushDate);
//                        marketingDataValidConfig.setValidEndDate(endDate);
//                        marketingDataValidConfig.setValidType(1);
//                        marketingDataValidConfig.setCreateTime(new Date());
//                        marketingDataValidConfig.setIsDel(Constants.DATA_VALID);
//                        dataValidConfigMapper.insertSelective(marketingDataValidConfig);
//                    }
//                }
//            }
            //endregion
        }
    }
}
