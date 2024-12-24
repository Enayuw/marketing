package com.br.marketing.check.job.smy;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.service.clean.smy.SmyDataCleanService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.google.common.base.Splitter;
import java.util.List;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SmyCustomizedTransferDataCleanJob extends AbstractSimpleElasticJob {

    @Resource
    private SmyDataCleanService smyDataCleanService;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        JSONObject smyCustomizeDataConfig = marketingCommonConfig.getSmyCustomizeDataConfig();
        String apiCode = smyCustomizeDataConfig == null ? "3710196" : smyCustomizeDataConfig.getString("transferApiCode");
        String date = DateUtil.today();
        String jobParameter = context.getJobParameter();
        if (StringUtils.isNotEmpty(jobParameter)) {
            List<String> params = Splitter.on("#").splitToList(jobParameter);
            apiCode = params.get(0);
            date = params.get(1);
        }
        log.warn("萨摩耶定制转化数据清洗开始");
        long start = System.currentTimeMillis();
        smyDataCleanService.cleanCustomizedTransferData(apiCode, date);
        long end = System.currentTimeMillis();
        log.warn("萨摩耶定制转化数据清洗结束，耗时：" + (end - start));
    }
}
