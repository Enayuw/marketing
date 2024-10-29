package com.br.marketing.check.job.sushang;

import com.br.common.log.AlertLog;
import com.br.marketing.check.service.Impl.sushang.SuShangPushService;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.SftpFileTypeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.SushangTransferDataMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.github.pagehelper.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

    @Resource
    private SushangTransferDataMapper sushangTransferDataMapper;

    final static String EXECUTE_TIME = "02:00:00";



    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        String suShangFileDate = marketingCommonConfig.getSuShangFileDate();
        String dateToday = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String dateTodayReduceTwo = "";

        // 原有T-2的逻辑在2.1版本改为了T-1日
        dateTodayReduceTwo = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 查询苏商通话明细文件
        LocalFileExample exampleCallRecord = new LocalFileExample();
        exampleCallRecord.createCriteria().andFileTypeEqualTo(SftpFileTypeEnum.SUSHANG_CALLRECORD.getValue())
                .andFileNameLike("%" + dateToday + "%")
                .andStatusEqualTo("2")
                .andPushStatusIsNull()
                .andApiCodeIn(marketingCommonConfig.getSuShangApiCodes());
        List<LocalFile> callRecordFiles = localFileMapper.selectByExample(exampleCallRecord);

        // T日通话明细可推送
        if (CollectionUtils.isEmpty(callRecordFiles)) {
            log.warn("苏商自动化回传，通话明细文件为空");
            return;
        }

        List<LocalFile> transferFiles = new ArrayList<>();
        // 配置了 指定日期 进行量级比较
        if (StringUtil.isNotEmpty(suShangFileDate)){
            // 查询 T-1日 待推送文件
            LocalFileExample example = new LocalFileExample();
            example.createCriteria().andFileTypeEqualTo(SftpFileTypeEnum.SUSHANG_TRANSFER.getValue())
                    .andFileNameLike("%" + dateTodayReduceTwo + "%")
                    .andStatusEqualTo("2")
                    .andPushStatusIsNull()
                    .andApiCodeIn(marketingCommonConfig.getSuShangApiCodes());
            transferFiles = localFileMapper.selectByExample(example);

            // 修改指定日期文件状态
            updateNewTransferFilesStatus(suShangFileDate);

            // 查询 指定日期 待推送文件
            LocalFileExample newExample = new LocalFileExample();
            newExample.createCriteria().andFileTypeEqualTo(SftpFileTypeEnum.SUSHANG_TRANSFER.getValue())
                    .andFileNameLike("%" + suShangFileDate + "%")
                    .andStatusEqualTo("2")
                    .andPushStatusIsNull()
                    .andApiCodeIn(marketingCommonConfig.getSuShangApiCodes());
            List<LocalFile> newTransferFiles = localFileMapper.selectByExample(newExample);

            // T-1日转化数据记录为空
            if (CollectionUtils.isEmpty(transferFiles)) {
                transferFiles = newTransferFiles;
            } else {
                // 判断两个日期的量级，返回大量级的localFile集合
                transferFiles = getTransferFiles(transferFiles, newTransferFiles);
            }
        } else {
            //查询 T日 待推送文件
            LocalFileExample example = new LocalFileExample();
            example.createCriteria().andFileTypeEqualTo(SftpFileTypeEnum.SUSHANG_TRANSFER.getValue())
                    .andFileNameLike("%" + dateTodayReduceTwo + "%")
                    .andStatusEqualTo("2")
                    .andPushStatusIsNull()
                    .andApiCodeIn(marketingCommonConfig.getSuShangApiCodes());
            transferFiles = localFileMapper.selectByExample(example);
        }

        // T日通话明细和T-1日转化数据(或指定日期转化数据)
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
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.SUNING_SERVICEERROR.getCode(), "苏商推送通话明细异常！"), e);
            }
        });
    }

    private void updateNewTransferFilesStatus(String suShangFileDate) {
        String extractTime = StringUtils.isBlank(marketingCommonConfig.getSuShangFileExecTime())
                ? EXECUTE_TIME : marketingCommonConfig.getSuShangFileExecTime();
        LocalTime localTime = LocalTime.parse(extractTime);
        if (LocalTime.now().isAfter(localTime)) {
            // 查询 指定日期 待推送文件
            LocalFileExample newExample = new LocalFileExample();
            newExample.createCriteria().andFileTypeEqualTo(SftpFileTypeEnum.SUSHANG_TRANSFER.getValue())
                    .andFileNameLike("%" + suShangFileDate + "%")
                    .andStatusEqualTo("2")
                    .andApiCodeIn(marketingCommonConfig.getSuShangApiCodes());
            List<LocalFile> newTransferFiles = localFileMapper.selectByExample(newExample);
            if (newTransferFiles.isEmpty()){
                return;
            }
            for (LocalFile localFile : newTransferFiles) {
                localFile.setPushStatus(null);
                localFileMapper.updateByPrimaryKey(localFile);
            }
        }
    }

    private List<LocalFile> getTransferFiles(List<LocalFile> transferFiles, List<LocalFile> newTransferFiles) {
        List<LocalFile> localFiles = new ArrayList<>();

        int transferFileCount = 0;
        for (LocalFile file : transferFiles) {
            transferFileCount += sushangTransferDataMapper.countByLocalId(file.getId());
        }

        int newTransferFileCount = 0;
        for (LocalFile file : newTransferFiles) {
            newTransferFileCount += sushangTransferDataMapper.countByLocalId(file.getId());
        }

        if (transferFileCount >= newTransferFileCount) {
            localFiles.addAll(transferFiles);
        } else {
            localFiles.addAll(newTransferFiles);
        }

        return localFiles;
    }


}
