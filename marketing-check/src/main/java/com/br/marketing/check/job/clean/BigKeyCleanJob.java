package com.br.marketing.check.job.clean;

import com.br.marketing.check.service.Impl.CleanScoreCustomerServiceImpl;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.mapper.DataDistributeDetailLogMapper;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Component
@Slf4j
public class BigKeyCleanJob extends AbstractSimpleElasticJob {


    @Autowired
    RedisChgService redisChgService;

    @Autowired
    CleanScoreCustomerServiceImpl cleanScoreCustomerService;
    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        long uploadFirstTime = System.currentTimeMillis();
        String uploadKey = RedisKeyConstant.uploadKey.concat(":")
                .concat(LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        if(StringUtils.isNotBlank(context.getJobParameter())){
            uploadKey = RedisKeyConstant.uploadKey.concat(":")
                    .concat(context.getJobParameter());
        }
        redisChgService.delBigSet(uploadKey,3000);
        log.warn(String.format("删除代运营key：%s,耗时：%dms",uploadKey,System.currentTimeMillis()-uploadFirstTime));
        long transferFirstTime = System.currentTimeMillis();
        String transferKey = RedisKeyConstant.transferKey.concat(":")
                .concat(LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        if(StringUtils.isNotBlank(context.getJobParameter())){
            transferKey = RedisKeyConstant.transferKey.concat(":")
                    .concat(context.getJobParameter());
        }
        redisChgService.delBigSet(transferKey,3000);

        log.warn(String.format("删除转化key：%s,耗时：%dms",transferKey,System.currentTimeMillis()-transferFirstTime));

        long scoreTime = System.currentTimeMillis();
        cleanScoreCustomerService.cleanScoreCustomerBigKey();
        log.warn(String.format("删除跑分推送客户大key：%s,耗时：%dms",transferKey,System.currentTimeMillis()-scoreTime));
    }

}
