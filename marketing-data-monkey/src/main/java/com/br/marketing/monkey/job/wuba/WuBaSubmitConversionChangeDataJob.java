package com.br.marketing.monkey.job.wuba;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.common.util.DateUtils;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.dto.wuba.WuBaChangeSubmitDataDto;
import com.br.marketing.monkeydata.entity.commonobj.Page2Condition;
import com.br.marketing.service.Impl.wuba.WuBaSubmitConversionChangeDataService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

/**
 * @Description 58新客提交营销名单修改上报数据
 * @Author lixiang
 * @Date 2024-07-08
 */
@Component
@Slf4j
public class WuBaSubmitConversionChangeDataJob extends AbstractSimpleElasticJob {

    private final static String TITLE = "【58新客提交营销名单修改上报数据】";
    private static final String MARKETING_TIME_START = "marketingTimeStart";
    private static final String MARKETING_TIME_END = "marketingTimeEnd";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private WuBaSubmitConversionChangeDataService service;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        try {
            log.warn(TITLE + "调度开始");
            // switch
            if(!checkJobSwitch()) {
                return;
            }

            // parseParameter
            List<Map<String, String>> paramList = parseParameter();

            // action
            action(paramList);

            log.warn(TITLE + "调度结束");
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(),e.getMessage(), TITLE), e);
        }
    }

    private boolean checkJobSwitch(){
        String wuBaSubmitConversionSwitch = marketingCommonConfig.getWuBaSubmitConversionChangeDataSwitch();
        if ("1".equals(wuBaSubmitConversionSwitch)) {
            log.warn(TITLE + "开关打开");
            return true;
        }
        log.warn(TITLE + "开关关闭");
        return false;
    }

    private void action(List<Map<String, String>> paramList) {
        Map<String, String> threadConfig = marketingCommonConfig.getWuBaSubmitConversionChangeDataThreadConfig();
        Integer pageSize = Integer.parseInt(threadConfig.get("pageSize"));
        // action
        for (Map<String, String> param: paramList) {
            action(param, pageSize);
        }
    }

    private void action(Map<String, String> param, Integer pageSize) {
        String apiCode = param.get("apiCode");
        String marketingTimeStart = param.get(MARKETING_TIME_START);
        String marketingTimeEnd = param.get(MARKETING_TIME_END);

        WuBaChangeSubmitDataDto conditionParam = new WuBaChangeSubmitDataDto();
        conditionParam.setApiCode(apiCode);
        conditionParam.setMarketingTimeStart(marketingTimeStart);
        conditionParam.setMarketingTimeEnd(marketingTimeEnd);

        Page2Condition<WuBaChangeSubmitDataDto> condition = new Page2Condition<>();
        condition.setParam(conditionParam);
        condition.setPageSize(pageSize);
        service.action(condition);
    }

    /**
     * 解析Job参数，格式如下：
     * e.g [{"apiCode":"3710155","bizDate":"-6"},{"apiCode":"3710155","bizDate":"-5"}]
     */
    private List<Map<String, String>> parseParameter() throws Exception {
        List<Map<String, String>> paramList = new ArrayList<>();
        List<Map<String, String>> configList = marketingCommonConfig.getWuBaSubmitConversionChangeDataParams();
        LocalDate curLocalDate = LocalDate.now();
        log.warn(TITLE + "curDate: {}", curLocalDate);

        for(Map<String, String> configMap : configList){
            Map<String, String> paramMap = new HashMap<>();
            // apiCode
            String apiCode = configMap.get("apiCode");
            if(StringUtils.isEmpty(apiCode)){
                throw new Exception("Job参数apiCode格式不正确");
            }
            paramMap.put("apiCode", apiCode);

            // bizDate
            String bizDateStr = configMap.get("bizDate");
            if(StringUtils.isEmpty(bizDateStr)){
                throw new Exception("Job参数bizDate格式不正确");
            }
            Long bizDateLong = Long.parseLong(bizDateStr);
            LocalDate startLocalDate = curLocalDate.plusDays(bizDateLong);
            LocalDate endLocalDate = startLocalDate.plusDays(1);

            Date startDate = Date.from(startLocalDate.atStartOfDay(ZoneOffset.ofHours(8)).toInstant());
            Date endDate = Date.from(endLocalDate.atStartOfDay(ZoneOffset.ofHours(8)).toInstant());

            String marketingTimeStart = DateUtils.format(startDate, "yyyy-MM-dd 00:00:00");
            String marketingTimeEnd = DateUtils.format(endDate, "yyyy-MM-dd 00:00:00");

            paramMap.put(MARKETING_TIME_START, marketingTimeStart);
            paramMap.put(MARKETING_TIME_END, marketingTimeEnd);

            paramList.add(paramMap);
        }
        log.warn(TITLE + "paramList: {}", JSONObject.toJSONString(paramList));
        return paramList;
    }
}
