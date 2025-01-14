package com.br.marketing.check.job.carclue;

import com.br.marketing.common.enums.SftpFileTypeEnum;
import com.br.marketing.entity.CallRecordExample;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.mapper.CallRecordMapper;
import com.br.marketing.service.clean.CarClue.CarCluesDataCleanService;
import com.br.marketing.service.clean.smy.SmyDataCleanService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;

/**
 * 车线索数据入上传表
 * @author guangxiu.li
 * @date 2025/1/14
 * @description
 */
@Component
@Slf4j
public class CarCluesDataCleanJob extends AbstractSimpleElasticJob {
    @Resource
    CarCluesDataCleanService carCluesDataCleanService;
    @Resource
    MarketingCommonConfig marketingCommonConfig;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        List<String> carClueApiCodes = marketingCommonConfig.getCarClueApiCodes();
        String date = LocalDate.now().toString();
        log.warn("车线索数据入上传表清洗开始");
        long start = System.currentTimeMillis();
        carCluesDataCleanService.cleanCallDetailsData(carClueApiCodes, date);
        long end = System.currentTimeMillis();
        log.warn("车线索数据入上传表清洗结束，耗时：" + (end - start));
    }
}
