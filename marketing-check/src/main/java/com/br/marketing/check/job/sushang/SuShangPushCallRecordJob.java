package com.br.marketing.check.job.sushang;

import com.br.common.log.AlertLog;
import com.br.marketing.check.service.Impl.sushang.SuShangPushService;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.SftpFileTypeEnum;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @Description 苏商推送通话明细任务
 * @Author zhen.Li1
 * @CreateTime 2024/07/15
 */
@Component
@Slf4j
public class SuShangPushCallRecordJob extends AbstractSimpleElasticJob {


    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private LocalFileMapper localFileMapper;

    @Resource
    private SuShangPushService suShangPushService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        String dateToday = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        //T-2
        String dateTodayReduceTwo = LocalDate.now().minusDays(2).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        //查询待推送文件
        LocalFileExample example = new LocalFileExample();
        example.createCriteria().andFileTypeEqualTo(SftpFileTypeEnum.SUSHANG_TRANSFER.getValue()).andFileNameLike("%" + dateTodayReduceTwo + "%")
                .andStatusEqualTo("2").andPushStatusIsNull().andApiCodeIn(marketingCommonConfig.getSuShangApiCodes());
        List<LocalFile> transferFiles = localFileMapper.selectByExample(example);
        LocalFileExample exampleCallRecord = new LocalFileExample();
        exampleCallRecord.createCriteria().andFileTypeEqualTo(SftpFileTypeEnum.SUSHANG_CALLRECORD.getValue()).andFileNameLike("%" + dateToday + "%")
                .andStatusEqualTo("2").andPushStatusIsNull().andApiCodeIn(marketingCommonConfig.getSuShangApiCodes());
        List<LocalFile> callRecordFiles = localFileMapper.selectByExample(exampleCallRecord);
        //T日通话明细和T-2日转化数据
        if (CollectionUtils.isEmpty(transferFiles) || CollectionUtils.isEmpty(callRecordFiles)) {
            log.warn("苏商自动化回传，通话明细或转化文件为空");
            return;
        }
        transferFiles.forEach((LocalFile localFile) -> {
            try {
                suShangPushService.pushCallRecordHandler(localFile, callRecordFiles.get(0));
            } catch (Exception e) {
                //推送异常更新状态,更新为失败status=3
                localFile.setPushStatus("3");
                localFile.setId(localFile.getId());
                localFileMapper.updateByPrimaryKeySelective(localFile);
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.SERVICEERROR_UNKNOWN.getCode(), "苏商推送通话明细异常！"), e);
            }
        });
    }


}
