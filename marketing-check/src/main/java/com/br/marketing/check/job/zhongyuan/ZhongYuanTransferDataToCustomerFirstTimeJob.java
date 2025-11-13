package com.br.marketing.check.job.zhongyuan;

import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.ValidityPeriodDataService;
import com.br.marketing.service.ZhongYuanService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import cn.hutool.core.lang.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 中原转化数据推客服转化过滤
 * http://c.100credit.cn/pages/viewpage.action?pageId=125085427
 * @program: marketing
 * @ClassName ZhongYuanTransferDataToCustomerFirstTimeJob
 * @author: chenh
 * @create: 2023-08-25 19:34
 * @Version 1.0
 * --------------------------------------
 **/
@Component
@Slf4j
public class ZhongYuanTransferDataToCustomerFirstTimeJob extends AbstractSimpleElasticJob {

    @Resource
    private ZhongYuanService zhongYuanService;
    @Resource
    private TableCreateServiceImpl tableCreateService;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private ValidityPeriodDataService validityPeriodDataService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        log.warn("中原转化数据推送客服转化过滤，首次JOB开始");
        Set<String> zhongYouJobApiCodes = marketingCommonConfig.getZhongYuanJobApiCodes();
        if (!zhongYouJobApiCodes.isEmpty()) {
            zhongYouJobApiCodes.forEach(apiCode -> {
                String tcId = tableCreateService.getTcId(apiCode);

                // requestDate可配置，param样例：2023-08-08,2023-08-28
                String parameter = context.getJobParameter();
                String startDate;
                String endDate;
                if (StringUtils.isNotBlank(parameter)) {
                    String[] split = parameter.split(",");
                    startDate = LocalDate.parse(split[0], DateTimeFormatter.ofPattern("yyyy-MM-dd")).toString();
                    endDate = LocalDate.parse(split[1], DateTimeFormatter.ofPattern("yyyy-MM-dd")).toString();
                } else {
                    Pair<String, String> validityRange =
                            validityPeriodDataService.getMarketingTransferDataWithValidityRange(apiCode);
                    startDate = validityRange.getKey();
                    endDate = validityRange.getValue();
                }

                Long indexId = null;

                // 开启线程池
                Integer threadNum =
                        marketingCommonConfig.getZhongYuanTransferDataToDaasAndCustomerFilterThreadNum();
                ThreadPoolExecutor pool = BrExecutors.getThreadPool(threadNum, threadNum);
                while (true) {
                    List<MarketingTransferSyncUser> marketingTransferSyncUserList =
                            zhongYuanService.getMarketingTransferSyncUserListWithValidityPeriod(tcId, apiCode
                                    , indexId, startDate, endDate);
                    if (marketingTransferSyncUserList.isEmpty()) {
                        break;
                    }

                    indexId = marketingTransferSyncUserList.get(marketingTransferSyncUserList.size() - 1).getId();

                    modifyCorePoolSize(pool);
                    pool.execute(() -> zhongYuanService.zhongYuanTransferDataToCustomerFilter(marketingTransferSyncUserList));
                }

                // 关闭线程池
                pool.shutdown();
                try {
                    while (!pool.awaitTermination(10L, TimeUnit.SECONDS)) {
                        log.info("等待线程池结束");
                    }
                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                }
            });
        } else {
            log.error("中原转化数据推daas job未配置apiCode,请检查配置字段 【zhongYouJobApiCodes】");
        }
        log.warn("中原转化数据推送客服转化过滤，首次JOB结束");
    }

    private void modifyCorePoolSize(ThreadPoolExecutor pool){
        Integer threadNum =
                marketingCommonConfig.getZhongYuanTransferDataToDaasAndCustomerFilterThreadNum();
        ThreadPoolAdjustmentUtil.adjustThreadPoolSize(pool, threadNum);
    }
}
