package com.br.marketing.monkey.job.wuba;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.common.util.DateUtils;
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
import java.util.*;

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
            List<Map<String, String>> paramList = parseJobParameter(context.getJobParameter());
            // pageSize
            Integer pageSize = marketingCommonConfig.getWuBaSubmitConversionPageSize();

            // action
            for (Map<String, String> paramMap : paramList) {
                String apiCode = paramMap.get("apiCode");
                String bizDate = paramMap.get("bizDate");
                Integer createDate = Integer.parseInt(bizDate.replace("-", ""));
                action(apiCode, createDate, pageSize);
            }

            log.warn(TITLE + "调度结束");
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(),e.getMessage()
                    , TITLE), e);
        }
    }

    private void action(String apiCode, Integer createDate, Integer pageSize) {
        WubaSubmitConversionData param = new WubaSubmitConversionData();
        param.setApiCode(apiCode);
        param.setStatus(1);
        param.setPushStatus(0);
        param.setCreateDate(createDate);
        Page2Condition<WubaSubmitConversionData> condition = new Page2Condition<>();
        condition.setParam(param);
        condition.setPageSize(pageSize);
        service.action(condition);
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

    /**
     * 解析Job参数，格式如下：
     * e.g [{"apiCode":"3710155","bizDate":"2024-07-11"},{"apiCode":"3710155","bizDate":"2024-07-12"}]
     */
    private List<Map<String, String>> parseJobParameter(String parameter) throws Exception {
        List<Map<String, String>> paramList = new ArrayList<>();
        String curDate = DateUtils.format(new Date(), "yyyy-MM-dd");

        if (StringUtils.isNotEmpty(parameter)) {
            paramList = JSONObject.parseObject(parameter, List.class);
            for(Map<String, String> map : paramList){
                if(StringUtils.isEmpty(map.get("apiCode"))){
                    throw new Exception("Job参数格式不正确");
                }
                if(StringUtils.isEmpty(map.get("bizDate"))){
                    map.put("bizDate", curDate);
                }
            }
            return paramList;
        }

        Map<String, String> map = new HashMap<>();
        map.put("apiCode", "3710155");
        map.put("bizDate", curDate);
        paramList.add(map);
        return paramList;
    }
}
