package com.br.marketing.monkey.job.wuba;

import com.br.marketing.entity.WubaCollidingBatchNo;
import com.br.marketing.monkeydata.entity.commonobj.Page2Condition;
import com.br.marketing.service.Impl.wuba.WuBaQueryConversionResultService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @Description 58新客提交营销名单结果查询
 * @Author lixiang
 * @Date 2024-07-08
 */
@Component
@Slf4j
public class WuBaQueryConversionResultJob extends AbstractSimpleElasticJob {

    private final static String TITLE = "【58新客提交营销名单结果查询】";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private WuBaQueryConversionResultService service;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        try {
            log.warn(TITLE + "调度开始");
            // switch
            if(!checkJobSwitch()) return;

            // action
            WubaCollidingBatchNo param = new WubaCollidingBatchNo();
            param.setBatchType(2);
            param.setPushTime(new Date());
            param.setQueryStatus(0);
            Page2Condition<WubaCollidingBatchNo> condition = new Page2Condition<>();
            condition.setParam(param);
            service.action(condition);

            log.warn(TITLE + "调度结束");
        } catch (Exception e) {
            log.error(TITLE + "调度异常", e);
        }
    }

    private boolean checkJobSwitch() throws Exception {
        String wuBaQueryConversionResultSwitch = marketingCommonConfig.getWuBaQueryConversionResultSwitch();
        if ("1".equals(wuBaQueryConversionResultSwitch)) {
            log.warn(TITLE + "开关打开");
            return true;
        }
        log.warn(TITLE + "开关关闭");
        return false;
    }

    private String parseJobParameter(String parameter) throws Exception {
        String apiCode = "3710155";
        if (StringUtils.isNotEmpty(parameter)) {
            apiCode = parameter;
        }
        return apiCode;
    }
}
