package com.br.marketing.check.job.zhongyuan;

import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.ZhongYuanService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 描述：： 中原转化数据推Daas
 * <p>
 * ------------------------------------
 *
 * @program: marketing
 * @ClassName ZhongYuanTransferDataToDaasJob
 * @author: it-yml
 * @create: 2023-08-25 19:34
 * @Version 1.0
 * --------------------------------------
 **/
@Component
@Slf4j
public class ZhongYuanTransferDataToDaasJob extends AbstractSimpleElasticJob {

    @Resource
    private ZhongYuanService zhongYuanService;
    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;


    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        Set<String> zhongYouJobApiCodes = getZhongYuanApiCodes();
        if (!zhongYouJobApiCodes.isEmpty()) {
            zhongYouJobApiCodes.forEach(apiCode -> {
                String tcId = tableCreateService.getTcId(apiCode);
                Long indexId = null;
                ThreadPoolExecutor zhongYuanTransferToDaasAndCustomerFilterThreadPool = createThreadPoolExecutor();
                while (true) {
                    List<MarketingTransferSyncUser> marketingTransferSyncUserList = zhongYuanService.getMarketingTransferSyncUserListWithValidityPeriod(tcId, apiCode, indexId, LocalDate.now().toString(), LocalDate.now().toString());
                    if (marketingTransferSyncUserList.isEmpty()) break;
                    indexId = marketingTransferSyncUserList.get(marketingTransferSyncUserList.size() - 1).getId();
                    dealTransferDataWithThread(zhongYuanTransferToDaasAndCustomerFilterThreadPool, marketingTransferSyncUserList);
                }
                threadClosed(zhongYuanTransferToDaasAndCustomerFilterThreadPool);
            });
        } else {
            log.error("中原转化数据推daas job未配置apiCode,请检查配置字段 【zhongYouJobApiCodes】");
        }
    }

    /**
     * 关闭线程池
     * @param zhongYuanTransferToDaasAndCustomerFilterThreadPool
     */
    private static void threadClosed(ThreadPoolExecutor zhongYuanTransferToDaasAndCustomerFilterThreadPool) {
        zhongYuanTransferToDaasAndCustomerFilterThreadPool.shutdown();
        try {
            while (!zhongYuanTransferToDaasAndCustomerFilterThreadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("等待线程池结束");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    private void dealTransferDataWithThread(ThreadPoolExecutor zhongYuanTransferToDaasAndCustomerFilterThreadPool, List<MarketingTransferSyncUser> marketingTransferSyncUserList) {
        zhongYuanTransferToDaasAndCustomerFilterThreadPool.setCorePoolSize(marketingCommonConfig.getZhongYuanTransferDataToDaasAndCustomerFilterThreadNum());
        zhongYuanTransferToDaasAndCustomerFilterThreadPool.setMaximumPoolSize(marketingCommonConfig.getZhongYuanTransferDataToDaasAndCustomerFilterThreadNum());
        zhongYuanTransferToDaasAndCustomerFilterThreadPool.execute(() -> threadDoProcess(marketingTransferSyncUserList));
    }

    /**
     * 创建线程池
     * @return
     */
    private ThreadPoolExecutor createThreadPoolExecutor() {
        ThreadPoolExecutor zhongYuanTransferToDaasAndCustomerFilterThreadPool =
                BrExecutors.getThreadPool(
                        marketingCommonConfig.getZhongYuanTransferDataToDaasAndCustomerFilterThreadNum(),
                        marketingCommonConfig.getZhongYuanTransferDataToDaasAndCustomerFilterThreadNum()
                );
        return zhongYuanTransferToDaasAndCustomerFilterThreadPool;
    }

    /**
     * 获取中邮apiCode
     * @return
     */
    private Set<String> getZhongYuanApiCodes() {
        Set<String> zhongYouJobApiCodes = marketingCommonConfig.getZhongYouJobApiCodes();
        return zhongYouJobApiCodes;
    }

    /**
     * 执行推电销 和客服逻辑
     * @param marketingTransferSyncUserList
     */
    private void threadDoProcess(List<MarketingTransferSyncUser> marketingTransferSyncUserList) {
        // 推daas
        zhongYuanService.zhongYuanTransferDataToDaas(marketingTransferSyncUserList);

        // 推客服转化
        zhongYuanService.zhongYuanTransferDataToCustomerFilter(marketingTransferSyncUserList);
    }
}
