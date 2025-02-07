package com.br.marketing.check.job.carclue;

import com.br.marketing.service.carclue.todb.CarCluesDataToDBService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 车线索明细数据入库
 * @author guangxiu.li
 * @date 2025/1/14
 * @description
 */
@Component
@Slf4j
public class CarCluesDataToDBJob extends AbstractSimpleElasticJob {
    @Resource
    CarCluesDataToDBService carCluesDataCleanService;
    @Resource
    MarketingCommonConfig marketingCommonConfig;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        // 通话明细apiCode
        Map<String, List<String>> carClueStorageConfig = marketingCommonConfig.getCarClueStorageConfig();
        List<String> carClueApiCodes = carClueStorageConfig.get("carClueApiCodes");
        String date = LocalDate.now().minusDays(1).toString();
        log.warn("车线索数据入库清洗开始");
        long start = System.currentTimeMillis();
        carCluesDataCleanService.cleanCallDetailsData(carClueApiCodes, date);
        long end = System.currentTimeMillis();
        log.warn("车线索数据入库清洗结束，耗时：" + (end - start));
    }
}
