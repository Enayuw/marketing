package com.br.marketing.check.job.zhongyuan;

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
        Set<String> zhongYouJobApiCodes = marketingCommonConfig.getZhongYouJobApiCodes();
        if(!zhongYouJobApiCodes.isEmpty()){
            zhongYouJobApiCodes.forEach(apiCode -> {
                String tcId = tableCreateService.getTcId(apiCode);
                Long indexId = null;
                while(true){
                    List<MarketingTransferSyncUser> marketingTransferSyncUserList = zhongYuanService.getMarketingTransferSyncUserListWithValidityPeriod(tcId, apiCode, indexId, LocalDate.now().toString(),LocalDate.now().toString());
                    if(marketingTransferSyncUserList.isEmpty()) break;
                    indexId = marketingTransferSyncUserList.get(marketingTransferSyncUserList.size()-1).getId();
                    // 推daas
                    zhongYuanService.zhongYuanTransferDataToDaas(marketingTransferSyncUserList);

                    // 推客服转化
                    zhongYuanService.zhongYuanTransferDataToCustomerFilter(marketingTransferSyncUserList);
                }

            });
        }else {
            log.error("中原转化数据推daas job未配置apiCode,请检查配置字段 【zhongYouJobApiCodes】");
        }

        // T日的转化数据 registerTime <> null

    }
}
