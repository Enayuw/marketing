package com.br.marketing.monkey.job.wuba;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.WubaSubmitConversionData;
import com.br.marketing.monkeydata.entity.commonobj.Page2Condition;
import com.br.marketing.service.Impl.wuba.WuBaSubmitConversionService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Description 58新客提交营销名单
 * @Author lixiang
 * @Date 2024-07-08
 */
@Component
@Slf4j
public class WuBaSubmitConversionJob extends AbstractSimpleElasticJob {

    private final static String TITLE = "【58新客提交营销名单】";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private WuBaSubmitConversionService service;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        try {
            log.warn(TITLE + "调度开始");
            // switch
            if(!checkJobSwitch()) {
                return;
            }
            // jobParameter
            String apiCode = parseJobParameter(context.getJobParameter());
            // pageSize
            Integer pageSize = marketingCommonConfig.getWuBaSubmitConversionPageSize();

            // action
            WubaSubmitConversionData param = new WubaSubmitConversionData();
            param.setApiCode(apiCode);
            param.setStatus(1);
            param.setPushStatus(0);
            Page2Condition<WubaSubmitConversionData> condition = new Page2Condition<>();
            condition.setParam(param);
            condition.setPageSize(pageSize);
            service.action(condition);

            log.warn(TITLE + "调度结束");
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(),e.getMessage()
                    , TITLE), e);
        }
    }

    private boolean checkJobSwitch(){
        String wuBaSubmitConversionSwitch = marketingCommonConfig.getWuBaSubmitConversionSwitch();
        if ("1".equals(wuBaSubmitConversionSwitch)) {
            log.warn(TITLE + "开关打开");
            return true;
        }
        log.warn(TITLE + "开关关闭");
        return false;
    }

    private String parseJobParameter(String parameter){
        String apiCode = "3710155";
        if (StringUtils.isNotEmpty(parameter)) {
            apiCode = parameter;
        }
        return apiCode;
    }
}
