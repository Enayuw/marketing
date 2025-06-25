package com.br.marketing.monkey.job.sanliuling;

import com.br.marketing.common.enums.SftpFileTypeEnum;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.service.Impl.sanliuling.SanLiuLingApiService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName SanLiuLingTrafficJob
 * @Description 360-pp流量业务营销接口
 * https://c.100credit.cn/pages/viewpage.action?pageId=212666799
 * @Author kongbx
 * @Date 2025/6/20 14:47
 */
@Component
@Slf4j
public class SanLiuLingTrafficJob extends AbstractSimpleElasticJob {
    @Resource
    private LocalFileMapper localFileMapper;

    @Resource
    private SanLiuLingApiService sanLiuLingApiService;

    private final static String TITLE = "【360-pp流量业务营销】";

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        log.warn(TITLE + "start");
        long start = System.currentTimeMillis();

        String apiCode = shardingContext.getJobParameter();
        if (StringUtils.isEmpty(apiCode)) {
            apiCode = "3710185";
        }
        LocalFileExample example = new LocalFileExample();
        //查询待推送文件
        example.createCriteria().andFileTypeEqualTo(SftpFileTypeEnum.SANLIULING_PP.getValue())
                .andStatusEqualTo("2").andPushStatusIsNull().andApiCodeEqualTo(apiCode);
        List<LocalFile> localFiles = localFileMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(localFiles)) {
            log.warn(TITLE + "查询文件为空，apiCode：" + apiCode);
            return;
        }

        localFiles.forEach((LocalFile localFile) -> {
            try {
                sanLiuLingApiService.pushTrafficData(localFile);
            }catch (Exception e){
                //推送异常更新状态,更新为失败status=3
                LocalFile localFile1 = new LocalFile();
                localFile1.setPushStatus("3");
                localFile1.setId(localFile.getId());
                localFileMapper.updateByPrimaryKeySelective(localFile1);
                log.error(TITLE + "推送异常", e);
            }
        });

        long end = System.currentTimeMillis();
        log.warn(TITLE + "end, 耗时{}ms", end - start);
    }

}
