package com.br.marketing.bridge.job.clean;

import com.br.marketing.common.enums.DataTypeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.entity.SyncConfig;
import com.br.marketing.entity.SyncConfigExample;
import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.entity.TransferActionFrontExample;
import com.br.marketing.enums.TransferActionFrontActionTypeEnum;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.mapper.SyncConfigMapper;
import com.br.marketing.mapper.TransferActionFrontMapper;
import com.br.marketing.service.Impl.JobManager;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;

/**
 * @ClassName HaiErFileCleanTransferDateJob
 * @Description 海尔转化数据每日清洗(sftp->api)-3710018 https://c.100credit.cn/pages/viewpage.action?pageId=204927045
 * @Author kongbx
 * @Date 2025/5/19 16:12
 */
@Component
@Slf4j
public class HaiErFileCleanTransferDateJob extends AbstractSimpleElasticJob {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private TransferActionFrontMapper transferActionFrontMapper;
    @Resource
    private JobManager jobManager;
    @Resource
    MarketingSyncUserMapper marketingSyncUserMapper;
    @Resource
    SyncConfigMapper syncConfigMapper;

    private static final String TITLE = "【海尔转化数据每日清洗】";

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        log.warn("{}开始执行", TITLE);
        long start = System.currentTimeMillis();

        String apiCode = context.getJobParameter();
        if (StringUtils.isEmpty(apiCode)) {
            apiCode = "3710018";
        }
        clean(apiCode);

        long end = System.currentTimeMillis();
        log.warn("{}执行完成，耗时{}ms", TITLE, end - start);
    }

    private void clean(String apiCode) {

        //判断今日是否执行过
        TransferActionFrontExample frontExample = new TransferActionFrontExample();
        frontExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andActionDataEqualTo(LocalDate.now().toString())
                .andActionTypeEqualTo(TransferActionFrontActionTypeEnum.ONE.getValue())
                .andStatusEqualTo(2)
                .andIsDelEqualTo(Constants.DATA_VALID);
        List<TransferActionFront> transferActionFronts = transferActionFrontMapper.selectByExample(frontExample);

        if (!CollectionUtils.isEmpty(transferActionFronts)) {
            log.warn(TITLE + "今日已执行！");
            return;
        }

        //新增执行记录 setStatus 任务状态 1-未执行；2-执行结束；3-本地文件已生成
        TransferActionFront transferActionFront = jobManager.saveFront(apiCode, LocalDate.now().toString(), TransferActionFrontActionTypeEnum.ONE.getValue());


        //SyncConfigExample syncConfigExample = new SyncConfigExample();
        //syncConfigExample.createCriteria()
        //        .andApiCodeEqualTo(apiCode)
        //        .andDataTypeEqualTo(DataTypeEnum.MARKETINGTRANSFERDATA.getValue())
        //        .andTargetPathLike("%/download/marketing%")
        //        .andStatusEqualTo(1)
        //        .andTypeEqualTo(1);
        //List<SyncConfig> syncConfigs = syncConfigMapper.selectByExample(syncConfigExample);
        //if (CollectionUtils.isEmpty(syncConfigs)) {
        //    log.warn(TITLE + "SFTP配置不存在！");
        //    continue;
        //}
        //SyncConfig syncConfig = syncConfigs.get(0);



        System.out.println(1);


    }

}
