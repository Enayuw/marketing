package com.br.marketing.sync.job;

import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.sync.service.TcyrCpaPushFileSyncService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 同程易融新场景推送文件同步任务（内部 SFTP → 运营 SFTP）。
 * <p>
 * 具体逻辑见 {@link TcyrCpaPushFileSyncService#fileSync}：先查 SFTP 配置（含 type、customizedType、dataType=18 等完整条件），
 * 再按 task 的 scene 与配置 {@code remark} 匹配，写入 {@code opeSftpPath} 并同步。
 * </p>
 * document https://c.100credit.cn/pages/viewpage.action?pageId=217148341
 *
 * @author hedongshuo
 * @date 2025/9/1 15:39
 **/
@Component
@Slf4j
public class TcCpaPushFileSyncJob extends AbstractSimpleElasticJob {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TcyrCpaPushFileSyncService tcyrCpaPushFileSyncService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        if (!marketingCommonConfig.getTcyrCpaPushFileConfig().getBoolean("isSync")) {
            return;
        }
        tcyrCpaPushFileSyncService.fileSync(shardingContext.getJobParameter());
    }
}
