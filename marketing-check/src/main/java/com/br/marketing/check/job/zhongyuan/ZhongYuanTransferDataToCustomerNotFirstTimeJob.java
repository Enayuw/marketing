package com.br.marketing.check.job.zhongyuan;

import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.ZhongYuanService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
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
 * 中原转化数据推客服转化过滤非首次以及转化规则2(转化数据推daas的数据推客服)
 * http://c.100credit.cn/pages/viewpage.action?pageId=125085427
 * @program: marketing
 * @ClassName ZhongYuanTransferDataToCustomerNotFirstTimeJob
 * @author: chenh
 * @create: 2023-11-10 19:34
 * @Version 1.0
 * --------------------------------------
 **/
@Component
@Slf4j
public class ZhongYuanTransferDataToCustomerNotFirstTimeJob extends AbstractSimpleElasticJob {

    @Resource
    private ZhongYuanService zhongYuanService;
    @Resource
    private TableCreateServiceImpl tableCreateService;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        String parameter = context.getJobParameter();
        Set<String> zhongYouJobApiCodes = getZhongYuanApiCodes();
        if (!zhongYouJobApiCodes.isEmpty()) {
            zhongYouJobApiCodes.forEach(apiCode -> {
                String tcId = tableCreateService.getTcId(apiCode);
                Long indexId = null;
                // 线程池创建
                ThreadPoolExecutor zhongYuanTransferToDaasAndCustomerFilterThreadPool = createThreadPoolExecutor();
                while (true) {
                    // 基础数据获取
                    List<MarketingTransferSyncUser> marketingTransferSyncUserList = getMarketingTransferSyncUsers(apiCode, parameter, tcId, indexId);
                    if (marketingTransferSyncUserList == null) break;
                    indexId = marketingTransferSyncUserList.get(marketingTransferSyncUserList.size() - 1).getId();
                    // 数据处理逻辑
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
     * @param zhongYuanTransferToDaasAndCustomerFilterThreadPool 线程池
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
    private void dealTransferDataWithThread(ThreadPoolExecutor zhongYuanTransferToDaasAndCustomerFilterThreadPool
            , List<MarketingTransferSyncUser> marketingTransferSyncUserList) {
        ThreadPoolAdjustmentUtil.adjustThreadPoolSize(
                zhongYuanTransferToDaasAndCustomerFilterThreadPool
                , marketingCommonConfig.getZhongYuanTransferDataToDaasAndCustomerFilterThreadNum());
        zhongYuanTransferToDaasAndCustomerFilterThreadPool.execute(() -> threadDoProcess(marketingTransferSyncUserList));
    }
    /**
     * 执行推电销 和客服逻辑
     *
     * @param marketingTransferSyncUserList 转化数据集
     */
    private void threadDoProcess(List<MarketingTransferSyncUser> marketingTransferSyncUserList) {

        // 推客服转化 非首次 （registerTime非空）
        zhongYuanService.zhongYuanTransferDataToCustomerFilter(marketingTransferSyncUserList);

        // 转化数据推客服 规则1 因非首次包含规则1 推送条件 为了防止多次判断，增加效率，规则1 代码注释掉，如果规则1 有修改
        // 可在此代码基础上进行修改 （registerTime非空）
//        zhongYuanService.zhongYuanTransferDataToCustomerFilterRuleFirst(marketingTransferSyncUserList);

        // 转化数据推客服 规则2
        zhongYuanService.zhongYuanTransferDataToCustomerFilterByDaasTwo(marketingTransferSyncUserList);

    }
    /**
     * 获取基础转化数据
     * @param apiCode apiCode
     * @param parameter job 入参
     * @param tcId tcid
     * @param indexId 起始id
     * @return
     */
    private List<MarketingTransferSyncUser> getMarketingTransferSyncUsers(
            String apiCode, String parameter, String tcId, Long indexId) {
        String startDate = LocalDate.now().toString();
        String endDate = LocalDate.now().toString();
        if (StringUtils.isNotBlank(parameter)) {
            String[] split = parameter.split(",");
            startDate = LocalDate.parse(split[0], DateTimeFormatter.ofPattern("yyyy-MM-dd")).toString();
            endDate = LocalDate.parse(split[1], DateTimeFormatter.ofPattern("yyyy-MM-dd")).toString();
        }
        List<MarketingTransferSyncUser> marketingTransferSyncUserList =
                zhongYuanService.getMarketingTransferSyncUserListWithValidityPeriodNoRegisterTime(tcId, apiCode, indexId, startDate, endDate);
        if (marketingTransferSyncUserList.isEmpty()) {
            return null;
        }
        return marketingTransferSyncUserList;
    }
    /**
     * 获取中邮apiCode
     *
     * @return 返回 apiCode
     */
    private Set<String> getZhongYuanApiCodes() {
        return marketingCommonConfig.getZhongYuanJobApiCodes();
    }

    /**
     * 创建线程池
     *
     * @return 返回新建的集合
     */
    private ThreadPoolExecutor createThreadPoolExecutor() {
        return BrExecutors.getThreadPool(
                marketingCommonConfig.getZhongYuanTransferDataToDaasAndCustomerFilterThreadNum(),
                marketingCommonConfig.getZhongYuanTransferDataToDaasAndCustomerFilterThreadNum()
        );
    }
}
